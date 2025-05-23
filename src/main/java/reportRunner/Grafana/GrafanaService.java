package reportRunner.Grafana;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GrafanaService {
//    private final GrafanaDashboardClient dashboardClient;
//    private final GrafanaPanelParser panelParser;
//
//    public GrafanaService(GrafanaDashboardClient dashboardClient, GrafanaPanelParser panelParser) {
//        this.dashboardClient = dashboardClient;
//        this.panelParser = panelParser;
//    }
//
//    public List<GraphPanel> getPanelsForApplication(String application) {
//        List<String> dashboardUids = dashboardClient.getDashboardUids(application);
//
//        return dashboardUids.stream()
//                .map(uid -> dashboardClient.getDashboardJson(uid))
//                .flatMap(json -> panelParser.parsePanels(json).stream())
//                .map(panel -> new GraphPanel(
//                        String.valueOf(panel.getId()),
//                        panel.getTitle(),
//                        application
//                ))
//                .collect(Collectors.toList());
//    }
}
