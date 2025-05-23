package reportRunner.Grafana;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class GrafanaDashboardParser {
    public List<GraphPanel> parseGraphPanels(JsonNode dashboardJson) {
        List<GraphPanel> panels = new ArrayList<>();
        JsonNode panelsNode = dashboardJson.at("/dashboard/panels");

        for (JsonNode panel : panelsNode) {
            if ("graph".equals(panel.get("type").asText())) {
                String title = panel.get("title").asText();
                int panelId = panel.get("id").asInt();
                panels.add(new GraphPanel(title, panelId));
            }
        }
        return panels;
    }
}
