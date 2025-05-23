package reportRunner.Grafana;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GrafanaDashboardClient {
    private final HttpClient httpClient;
    private final String grafanaUrl;
    private final String apiToken;

    public GrafanaDashboardClient(String grafanaUrl, String apiToken) {
        this.grafanaUrl = grafanaUrl;
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newHttpClient();
    }

    public JsonNode getDashboardByUid(String uid) {
        String url = grafanaUrl + "/api/dashboards/uid/" + uid;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiToken)
                .build();
        return sendRequest(request);
    }

    public byte[] downloadPanelImage(String renderUrl) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(renderUrl))
                .header("Authorization", "Bearer " + apiToken)
                .build();
        return sendRequestForBytes(request);
    }

    private JsonNode sendRequest(HttpRequest request) {
        // simplified error handling
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new ObjectMapper().readTree(response.body());
        } catch (Exception e) {
            throw new RuntimeException("Failed to query Grafana", e);
        }
    }

    private byte[] sendRequestForBytes(HttpRequest request) {
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download panel image", e);
        }
    }
}
