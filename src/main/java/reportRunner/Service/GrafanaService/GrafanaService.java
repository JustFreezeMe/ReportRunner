package reportRunner.Service.GrafanaService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import reportRunner.Config.GrafanaConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@Slf4j
public class GrafanaService {

    Long startTimestamp;
    Long endTimestamp;
    String path;
    private final GrafanaConfig grafanaConfig;

    public GrafanaService(GrafanaConfig grafanaConfig) {
        this.grafanaConfig = grafanaConfig;
    }

    @SneakyThrows
    public URIBuilder createUriBuilder(int panelId, String dashboardUuid, String dashboardName) {

        URIBuilder uriBuilder = new URIBuilder(String.format("%s/render/d-solo/%s/%s", grafanaConfig.getGrafanaUrl(),
                dashboardUuid, dashboardName));

        uriBuilder.addParameter("orgId", "1");
        uriBuilder.addParameter("panelId", String.valueOf(panelId));
        uriBuilder.addParameter("from", String.valueOf(getStartTimestamp()));
        uriBuilder.addParameter("to", String.valueOf(getEndTimestamp()));
        uriBuilder.addParameter("var-g", "5s");
        uriBuilder.addParameter("width", String.valueOf(grafanaConfig.getGrafanaWidth()));
        uriBuilder.addParameter("height", String.valueOf(grafanaConfig.getGrafanaHeight()));

        return uriBuilder;

    }

    public Map<String, File> readGraphs(String pathGraph, List<GraphGroup> groupOfGraphs, long timestamp) {
        Map<String, File> graphsMap = new HashMap<>();
        File file;

        for (GraphGroup graphGroup : groupOfGraphs) {

            for (int panelCount = 0; panelCount < graphGroup.getPanels().size(); panelCount++) {
                String uniqueKey = graphGroup.getPanels().get(panelCount).getPanelName() + "_" + timestamp;

                file = new File(pathGraph + "/" + uniqueKey + ".png");
                graphsMap.put(uniqueKey, file);
            }
        }

        return graphsMap;
    }

    @SneakyThrows
    public String createGetGraphsRequest(GraphGroup group) {

        String dashboardUuid = group.getGroupId();
        String url = String.format("%s/api/dashboards/uid/%s", grafanaConfig.getGrafanaUrl(), dashboardUuid);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", "Bearer " + grafanaConfig.getGrafanaApiKey());

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                } else {
                    log.error("Request failed with status {}: {}", statusCode, response.getStatusLine().getReasonPhrase());
                }
            }
        } catch (IOException e) {
            log.error("Error while executing request: {}", e.getMessage(), e);
        }

        return null;
    }

    public void parseGraphNames(String responseBody, List<GraphPanel> panels) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(responseBody);
        JsonNode rootNode = json.get("dashboard").get("panels");
        for (GraphPanel panel : panels) {
            for (int i = 0; i < rootNode.size(); i++) {
                JsonNode node = rootNode.get(i).get("id"); // Извлекаем id узла
                String panelId = node.asText(); // Преобразуем id в строку

                if (panelId.equals(panel.getPanelId())) {
                    panel.setPanelName(rootNode.get(i).get("title").asText()
                            .replaceAll("\\s+", "_")
                            .replaceAll("\\.", "")
                            .replaceAll("[\\\\:\\.]", "_")
                            .replace("/", "_")
                            .replace(":", "_"));
                }
            }
        }
    }

    public InputStream downloadSinglePanel(GraphGroup group, String panelId, String application, String podName)
            throws URISyntaxException, IOException {
        // Создаем HTTP-клиент
        CloseableHttpClient client = HttpClientBuilder.create().build();

        // Получаем данные дашборда из GraphGroup
        String dashboardUuid = group.getGroupId();
        String dashboardName = group.getGroupName();

        // Создаем URI на основе ID панели, UUID и имени дашборда
        URIBuilder uriBuilder = createUriBuilder(Integer.parseInt(panelId), dashboardUuid, dashboardName);

        // Добавляем переменные из GraphGroup (например, application)
        uriBuilder.addParameter("var-application", application);
        uriBuilder.addParameter("var-instance", application);
        uriBuilder.addParameter("var-namespace", grafanaConfig.getKubernetes().getNamespaceName());
       // uriBuilder.addParameter("var-job", "staticScrape/monitoring/node-exporter-db/0");
        uriBuilder.addParameter("var-node", application);
        if (podName != null && !podName.isEmpty()) {
            uriBuilder.addParameter("var-pod_name", podName);
        }


        log.info("URI для панели: {} is {}", panelId, uriBuilder);

        // Создаем запрос
        HttpGet request = new HttpGet(uriBuilder.build());
        request.setHeader("Authorization", "Bearer " + grafanaConfig.getGrafanaApiKey());

        // Выполняем запрос
        HttpResponse response = client.execute(request);

        // Проверяем статус ответа
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new IOException("Не удалось скачать график для панели " + panelId
                    + ": " + statusCode + " " + response.getStatusLine().getReasonPhrase());
        }

        // Возвращаем поток для сохранения изображения
        return response.getEntity().getContent();
    }

    public void downloadImage(String graphName, GraphGroup group, String panelId, String application, String podName) throws URISyntaxException, IOException {
        String timestamp = String.valueOf(endTimestamp);
        try (InputStream is = downloadSinglePanel(group, panelId, application, podName)) {
            saveImgToFile(is, graphName + "_" + timestamp);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void saveImgToFile(InputStream inputStream, String nameImg) throws IOException {

        //File outputFile = new File(RESULT_FOLDER + "/" + TASK_ID + "/grafana_img/" + nameImg + ".png");
        File outputFile = new File(path + nameImg + ".png");
        FileUtils.copyInputStreamToFile(inputStream, outputFile);
        log.info("Graph saved to file {}", outputFile.getAbsolutePath());

    }
}
