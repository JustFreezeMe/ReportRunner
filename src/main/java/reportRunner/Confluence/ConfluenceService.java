package reportRunner.Confluence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import reportRunner.Config.CertConfig;
import reportRunner.Config.ConfirmMaxConfig;
import reportRunner.Config.ConfluenceConfig;
import reportRunner.Config.Grafana.GrafanaConfig;
import reportRunner.Config.InfluxDBConfig;
import reportRunner.Config.JiraConfig;
import reportRunner.Config.MaxPerformanceConfig;
import reportRunner.Config.ReliabilityConfig;
import reportRunner.Config.UtilityConfig;
import reportRunner.Config.VictoriaMetricsConfig;
import reportRunner.Confluence.Templates.ConclusionTemplates;
import reportRunner.Confluence.Templates.ReportTemplatesV2;
import reportRunner.FaultTolerance.FaultToleranceProperties;
import reportRunner.FaultTolerance.FaultToleranceScenario;
import reportRunner.FaultTolerance.FaultToleranceTest;
import reportRunner.Grafana.GrafanaApi;
import reportRunner.Grafana.GraphGroup;
import reportRunner.HttpService.HttpRequestService;
import reportRunner.ResultsCreator.ReportResult;
import reportRunner.Util.Utility;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static reportRunner.Constant.Constants.CONFLUENCE_PASSWORD;
import static reportRunner.Constant.Constants.CONFLUENCE_USERNAME;

@Component
@Slf4j
public class ConfluenceService {

    ReportTemplates templates;
    HttpRequestService requests;
    Utility credentialsUtility = new Utility();
    GrafanaApi grafana;
    CertConfig certConfig;
    ReportTemplatesV2 reportTemplatesV2;
    ConclusionTemplates conclusionTemplates;
    private final ConfluenceConfig confluenceConfig;
    private final JiraConfig jiraConfig;

    public ConfluenceService(ConfluenceConfig confluenceConfig, GrafanaConfig grafanaConfig, JiraConfig jiraConfig, MaxPerformanceConfig maxPerformanceConfig, ConfirmMaxConfig confirmMaxConfig, ReliabilityConfig reliabilityConfig, FaultToleranceProperties faultToleranceProperties, UtilityConfig utilityConfig, InfluxDBConfig influxDBConfig, ConclusionTemplates conclusionTemplates, ReportTemplatesV2 reportTemplatesV2) {
        this.confluenceConfig = confluenceConfig;
        this.jiraConfig = jiraConfig;
        this.grafana = new GrafanaApi(grafanaConfig);
        this.requests = new HttpRequestService(confluenceConfig);
        this.templates = new ReportTemplates(maxPerformanceConfig, confirmMaxConfig, reliabilityConfig, faultToleranceProperties, utilityConfig, influxDBConfig, new VictoriaMetricsConfig());
        this.certConfig = new CertConfig(confluenceConfig);
        this.reportTemplatesV2 = reportTemplatesV2;
        this.conclusionTemplates = conclusionTemplates;
    }

    String credentials = credentialsUtility.createCredentials(CONFLUENCE_USERNAME, CONFLUENCE_PASSWORD);
//TODO: Отчет

    @SneakyThrows
    public void confluenceUpdateConclusionPage(String pageTitle, String pageId, String version, Map<String, String> pageIds) {

        String content = conclusionTemplates.createPageHeader(jiraConfig.getTaskId())
                + conclusionTemplates.tableOfContents()
                + conclusionTemplates.createJiraTaskBlock(jiraConfig.getTaskId())
                + conclusionTemplates.createLoadTestTargets()
                + conclusionTemplates.createTableForLtCriteria()
                + conclusionTemplates.createLoadTestRecomendations()
                + conclusionTemplates.createPerformanceBugs()
                + conclusionTemplates.createLoadTestDeviations()
                + conclusionTemplates.createTableForLtProfile()
                + conclusionTemplates.createTableForTests(pageIds)
                + conclusionTemplates.createAttachmentsBlock();

        String body = new JSONObject()
                .put("version", new JSONObject().put("number", version))
                .put("title", pageTitle)
                .put("type", "page")
                .put("body", new JSONObject().put("storage", new JSONObject()
                        .put("value", content)
                        .put("representation", "storage"))).toString();

        HttpRequest putRequest = requests.createPutHttpRequest(body, pageId, credentials);

        HttpClient client = HttpClient.newBuilder().sslContext(certConfig.configureSsl()).build();
        HttpResponse<String> response = client.send(putRequest, HttpResponse.BodyHandlers.ofString());
    }

    @SneakyThrows
    public void confluenceUpdateReportPage(String pageTitle, String pageId, String version,
                                           List<GraphGroup> groups, long timestamp, Map<String, Long> testNames) {

        String content = reportTemplatesV2.tableOfContents()
                + reportTemplatesV2.createJiraTaskBlock(jiraConfig.getTaskId())
                + reportTemplatesV2.createMethodologyBlock()
                + reportTemplatesV2.createTableForLtProfile()
                + reportTemplatesV2.createLoadTestResultsV2(groups, timestamp);
        if (testNames.containsKey("Поиск максимальной производительности"))
            content += reportTemplatesV2.createMaxPerfResults(groups, testNames.get("Поиск максимальной производительности"));
        if (testNames.containsKey("Подтверждение максимальной производительности"))
            content += reportTemplatesV2.createConfirmMaxResults(groups, testNames.get("Подтверждение максимальной производительности"));
        if (testNames.containsKey("Тест надежности"))
            content += reportTemplatesV2.createReliabilityResults(groups, testNames.get("Тест надежности"));

        String body = new JSONObject()
                .put("version", new JSONObject().put("number", version))
                .put("title", pageTitle)
                .put("type", "page")
                .put("body", new JSONObject().put("storage", new JSONObject()
                        .put("value", content)
                        .put("representation", "storage"))).toString();

        HttpRequest putRequest = requests.createPutHttpRequest(body, pageId, credentials);

        HttpClient client = HttpClient.newBuilder().sslContext(certConfig.configureSsl()).build();
        HttpResponse<String> response = client.send(putRequest, HttpResponse.BodyHandlers.ofString());

    }

    @SneakyThrows
    public String confluenceCreateChildrenPage(String pageTitle, String pageId) {

        String body = new JSONObject()
                .put("type", "page")
                .put("title", pageTitle)
                .put("space", new JSONObject().put("key", confluenceConfig.getConfluenceSpaceKey()))
                .put("ancestors", new JSONArray().put(new JSONObject().put("id", Long.valueOf(pageId))))
                .put("body", new JSONObject().put("storage", new JSONObject().put("value", "").put("representation", "storage")))
                .toString();

        HttpRequest Request = requests.postCreateChildrenPage(body, credentials);

        HttpClient client = HttpClient.newBuilder().sslContext(certConfig.configureSsl()).build();
        HttpResponse<String> response = client.send(Request, HttpResponse.BodyHandlers.ofString());

        return String.valueOf(response.body());
    }

    @SneakyThrows
    public void confluenceUpdateFaultToleranceMainPage(String pageTitle, String pageId, String version,
                                                       List<ReportResult> results, FaultToleranceTest test) {
        String content = templates.createPageHeader(jiraConfig.getTaskId())
                + templates.createJiraTaskBlock(jiraConfig.getTaskId())
                + templates.createTableForLtCriteria()
                + templates.createLoadTestTargets()
                + templates.createLoadTestRecomendations()
                + templates.createPerformanceBugs()
                + templates.createLoadTestRestrictions()
                + templates.createLoadTestDeviations()
                + templates.createTableForLtProfile()
                + templates.createFaultToleranceListTemplate(test.getScenarios())
                + templates.createFaultToleranceMainPageResults();

        String body = new JSONObject()
                .put("version", new JSONObject().put("number", version))
                .put("title", pageTitle)
                .put("type", "page")
                .put("body", new JSONObject().put("storage", new JSONObject()
                        .put("value", content)
                        .put("representation", "storage"))).toString();

        HttpRequest putRequest = requests.createPutHttpRequest(body, pageId, credentials);

        HttpClient client = HttpClient.newBuilder().sslContext(certConfig.configureSsl()).build();
        HttpResponse<String> response = client.send(putRequest, HttpResponse.BodyHandlers.ofString());

    }

    @SneakyThrows
    public String confluenceUpdateFaultTolerancePage(String pageTitle, String pageId, String version,
                                                     FaultToleranceScenario scenario, List<GraphGroup> groups, long timestamp) {

        String content = templates.createFaultToleranceBaseInfo(scenario)
                + templates.createFaultToleranceDescribeTemplate()
                + templates.createFaulToleranceResults(groups, timestamp, scenario);

        String body = new JSONObject()
                .put("version", new JSONObject().put("number", version))
                .put("title", pageTitle)
                .put("type", "page")
                .put("body", new JSONObject().put("storage", new JSONObject()
                        .put("value", content)
                        .put("representation", "storage"))).toString();

        HttpRequest putRequest = requests.createPutHttpRequest(body, pageId, credentials);

        HttpClient client = HttpClient.newBuilder().sslContext(certConfig.configureSsl()).build();
        HttpResponse<String> response = client.send(putRequest, HttpResponse.BodyHandlers.ofString());

        return String.valueOf(response.statusCode());
    }


    @SneakyThrows
    public String confluenceGetPageVersion(String pageId) {

        HttpRequest request = requests.getPageVersionRequest(pageId, credentials);

        HttpClient client = HttpClient.newBuilder().sslContext(certConfig.configureSsl()).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();

        return parseVersion(responseBody);
    }


    private static String parseVersion(String responseBody) {
        int versionIndex = responseBody.indexOf("\"number\":") + 9;
        String parsedResponse = responseBody.substring(versionIndex, responseBody.indexOf(",", versionIndex)).trim();
        Integer version = Integer.parseInt(parsedResponse) + 1;
        return String.valueOf(version);
    }

    private static String parseAttachmentId(String request) {
        JSONObject jsonObject = new JSONObject();
        JSONArray resultsArray = jsonObject.getJSONArray("results");

        if (!resultsArray.isEmpty()) {
            JSONObject attachment = resultsArray.getJSONObject(0);
            return attachment.getString("id");
        }
        return null;
    }

    @SneakyThrows
    public String getConfluencePageId(String responseBody) {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);
        return root.path("id").asText();
    }

    @SneakyThrows
    public void deleteConfluenceAttachments(String pageId) {

        HttpClient client = HttpClient.newBuilder().sslContext(certConfig.configureSsl()).build();
        ObjectMapper objectMapper = new ObjectMapper();
        HttpRequest request = requests.getAttachmentsIdsRequest(pageId, credentials);


        while (!client.send(request, HttpResponse.BodyHandlers.ofString()).body().isEmpty()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode attachments = objectMapper.readTree(response.body());
                JsonNode results = attachments.get("results");

                if (results.isEmpty()) {
                    log.info("Вложения не получены сори :pepesadge:");
                    return;
                }
                for (JsonNode attachment : results) {
                    String attachmentId = attachment.get("id").asText();
                    String attachmentName = attachment.get("title").asText();
                    HttpResponse<String> resp = client.send(requests.deleteAttachmentRequest(attachmentId, credentials),
                            HttpResponse.BodyHandlers.ofString());
                    log.info("Вложение {} удалено", attachmentName);
                }
            } else {
                log.info(":pepesadge:");
            }

        }
    }

    public String confluenceUploadAttachment(String pageId, String path, List<GraphGroup> graphGroup, long timestamp) {
        Map<String, File> graphs = grafana.readGraphs(path, graphGroup, timestamp);
        List<HttpPost> requestsList = requests.uploadFileToConfluence(pageId, credentials, graphs);

        try (CloseableHttpClient client = HttpClientBuilder.create().setSSLContext(certConfig.configureSsl()).build()) {
            StringBuilder result = new StringBuilder();

            for (HttpPost request : requestsList) {
                try (CloseableHttpResponse response = client.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        result.append("Attachment uploaded successfully. Status: ").append(statusCode).append("\n");
                    } else {
                        result.append("Failed to upload attachment. Status: ").append(statusCode)
                                .append(", Reason: ").append(response.getStatusLine().getReasonPhrase()).append("\n");
                    }
                } catch (IOException e) {
                    result.append("Error uploading attachment: ").append(e.getMessage()).append("\n");
                }
            }
            return result.toString().trim();
        } catch (Exception e) {
            return "Failed to create HTTP client: " + e.getMessage();
        }
    }


}
