package reportRunner.Grafana;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class GrafanaRenderUriBulder {
    private final String grafanaUrl;
    private final Map<String, String> defaultVariables;

    public String buildRenderUrl(String dashboardUid, int panelId) {
        StringBuilder url = new StringBuilder(grafanaUrl)
                .append("/render/d-solo/")
                .append(dashboardUid)
                .append("/dummy?panelId=")
                .append(panelId)
                .append("&width=1000&height=500");

        for (Map.Entry<String, String> entry : defaultVariables.entrySet()) {
            url.append("&var-").append(entry.getKey()).append("=").append(entry.getValue());
        }

        return url.toString();
    }
}
