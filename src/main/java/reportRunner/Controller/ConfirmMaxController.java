package reportRunner.Controller;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reportRunner.Config.GrafanaConfig;
import reportRunner.Config.JiraConfig;
import reportRunner.Config.TestConfig.TestConfig;
import reportRunner.Config.UtilityConfig;
import reportRunner.Service.ConfluenceService.ConfluenceService;
import reportRunner.Service.GrafanaService.GrafanaService;
import reportRunner.Service.GrafanaService.GraphGroup;
import reportRunner.Service.ResultsService.ReportResult;
import reportRunner.Service.GrafanaService.GraphGroupService;
import reportRunner.Utility.ReportUtility;
import reportRunner.Service.TimeSeriesDatabaseService.Prometheus.PrometheusController;

import java.util.List;
import java.util.Map;

@Component
public class ConfirmMaxController {

    private static final String RESULTS_FOLDER = "results/";
    private static final String GRAFANA_IMG_PATH = "grafana_img/";

    private final UtilityConfig utilityConfig;
    private final JiraConfig jiraConfig;
    private final GraphGroupService graphGroupService;
    private final GrafanaConfig grafanaConfig;
    private final ConfluenceService confluenceService;
    private final ReportUtility testUtility;

    @Autowired
    public ConfirmMaxController(UtilityConfig utilityConfig, JiraConfig jiraConfig,
                                GraphGroupService graphGroupService,
                                GrafanaConfig grafanaConfig, ConfluenceService confluenceService,
                                ReportUtility testUtility) {
        this.utilityConfig = utilityConfig;
        this.jiraConfig = jiraConfig;
        this.graphGroupService = graphGroupService;
        this.grafanaConfig = grafanaConfig;
        this.confluenceService = confluenceService;
        this.testUtility = testUtility;
    }

    public ReportResult processConfirmMaxReport(TestConfig test, String pageId) {
        String path = buildGrafanaImgPath();
        Map<String, Long> timestamps = testUtility.calculateTimestamps(test);
        GrafanaService grafanaService = createGrafanaApi(timestamps, path);
        List<GraphGroup> groupOfGraphs = loadAndProcessGraphGroups(timestamps, grafanaService);
        String uploadResult = uploadToConfluence(pageId, groupOfGraphs, grafanaService);

        return new ReportResult(timestamps, groupOfGraphs, uploadResult);
    }

    private String buildGrafanaImgPath() {
        return utilityConfig.getResultsFolder() + "/" + jiraConfig.getTaskId() + "/" + GRAFANA_IMG_PATH;
    }

    private GrafanaService createGrafanaApi(Map<String, Long> timestamps, String path) {
        GrafanaService grafanaService = new GrafanaService(grafanaConfig);
        grafanaService.setStartTimestamp(timestamps.get("startTimestamp"));
        grafanaService.setEndTimestamp(timestamps.get("endTimestamp"));
        grafanaService.setPath(path);
        return grafanaService;
    }

    @SneakyThrows
    private List<GraphGroup> loadAndProcessGraphGroups(Map<String, Long> timestamps, GrafanaService grafanaService)  {
        GraphGroup graphGroup = new GraphGroup(utilityConfig);
        List<GraphGroup> groupOfGraphs = graphGroupService.loadChartGroupsFromConfig();
        PrometheusController prometheus = new PrometheusController(grafanaConfig.getPrometheusUrl(), grafanaConfig);
        return graphGroup.processGraphGroups(groupOfGraphs, timestamps, grafanaService, prometheus);
    }

    private String uploadToConfluence(String pageId, List<GraphGroup> groupOfGraphs, GrafanaService grafanaService) {
        return confluenceService.confluenceUploadAttachment(
                pageId,
                RESULTS_FOLDER + jiraConfig.getTaskId() + "/" + GRAFANA_IMG_PATH,
                groupOfGraphs,
                grafanaService.getEndTimestamp());
    }
}
