package reportRunner.Service.TimeSeriesDatabase.VictoriaMetrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import reportRunner.Config.VictoriaMetricsConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Data
public class VictoriaMetricsController {

    private VictoriaMetricsConfig victoriaMetricsConfig;

    public VictoriaMetricsController(VictoriaMetricsConfig victoriaMetricsConfig) {
        this.victoriaMetricsConfig = victoriaMetricsConfig;
    }

    public String getResponseTimeQueryVm(String quantile, String testDuration) {
        return "quantile_over_time(quantileValue,requests_duration{db=\"gatling\",result=\"OK\"}[testDuration])" //%7B %7D это {}
                        .replace("quantileValue", quantile)
                        .replace("testDuration", testDuration);
    }

    public String getRequestsCountQueryVm(String result, String testDuration) {
        String query = "count_over_time(requests_duration{db=\"gatling\",result=\"requestResult\"}[testDuration])";

        return query.replace("requestResult", result)
                .replace("testDuration", testDuration);
    }

    public String sendRequestToVm(String query, String startTimestamp, String endTimestamp) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(victoriaMetricsConfig.getVmUrl() + "/select/0/prometheus/api/v1/query_range?query=" + encodedQuery + "&start=" + startTimestamp + "&end=" + endTimestamp + "&step=15s"))
                .GET()
                .build();
        System.out.println(request.uri());
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public Map<String, String> parseRequestResult(String responseBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(responseBody);
        JsonNode rootNode = json.path("data").path("result");
        Map<String, String> results = new HashMap<>();
        if (rootNode.isArray()) {
            for (JsonNode result : rootNode) {
                JsonNode metricNode = result.path("metric").path("name");
                String metric = mapper.convertValue(metricNode, new TypeReference<>() {
                });

                String lastValue = null;

                if (result.has("values")) {
                    JsonNode values = result.get("values");
                    if (values.isArray()) {
                        JsonNode last = values.get(values.size() - 1);
                        if (last.isArray() && last.size() >= 2) {
                            lastValue = last.get(1).asText();
                        }
                    }
                } else if (result.has("value")) {
                    JsonNode value = result.get("value");
                    if (value.isArray() && value.size() >= 2) {
                        lastValue = value.get(1).asText();
                    }
                }
                if (lastValue != null) {
                    results.put(metric, lastValue);
                }

            }
        }
        return results;//.get(0).get("metric").get("pod").asText();
    }
}
