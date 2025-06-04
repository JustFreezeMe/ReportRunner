package reportRunner.Service.TimeSeriesDatabase.Prometheus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import reportRunner.Config.Grafana.GrafanaConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Setter
public class PrometheusController {

    String url;
    String podName;

    private final GrafanaConfig grafanaConfig;

    public PrometheusController(String url, GrafanaConfig grafanaConfig) {
        this.url = url;
        this.grafanaConfig = grafanaConfig;
    }

    public String getPodNamesQuery(String namespace, String application) {

        return "kube_pod_container_info%7Bnamespace='$namespace',container='$application'%7D" //%7B %7D это {}
                .replace("$namespace", namespace)
                .replace("$application", application);
    }

    public String getMesosInstanceQuery(String application) {
        return "jvm_memory_used_bytes%7Bapplication='$application'%7D"
                .replace("$application", application);
    }

    public String sendRequestToPrometheus(String query, String startTimestamp, String endTimestamp) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/api/v1/query_range?query=" + query + "&start=" + startTimestamp + "&end=" + endTimestamp + "&step=30m"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public String parsePodName(String responseBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(responseBody);
        JsonNode rootNode = json.get("data");
        return rootNode.get("result").get(0).get("metric").get("pod").asText();
    }

    public String parseMesosInstance(String responseBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(responseBody);
        JsonNode rootNode = json.get("data");
        return rootNode.get("result").get(0).get("metric").get("instance").asText();
    }


    public String getVariableName(String application, String start, String end) throws IOException, InterruptedException {
        String env = grafanaConfig.getEnviroment();

        if ("kubernetes".equalsIgnoreCase(env)) {
            String podQuery = getPodNamesQuery(grafanaConfig.getKubernetes().getNamespaceName(), application);
            return parsePodName(sendRequestToPrometheus(podQuery, start, end));
        } else if ("mesos".equalsIgnoreCase(env)) {
            String mesosQuery = getMesosInstanceQuery(application);
            return parseMesosInstance(sendRequestToPrometheus(mesosQuery, start, end));
        } else {
            throw new IllegalStateException("Неизвестное окружение: " + env + " .В поле grafana.enviroment должно быть либо kubernetes либо mesos");
        }
    }
}
