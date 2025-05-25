package reportRunner.Controller;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reportRunner.Config.ConfirmMaxConfig;
import reportRunner.Config.Grafana.GrafanaConfig;
import reportRunner.Config.JiraConfig;
import reportRunner.Config.UtilityConfig;
import reportRunner.Confluence.ConfluenceService;
import reportRunner.Grafana.GrafanaService;
import reportRunner.Grafana.GraphGroup;
import reportRunner.ResultsCreator.ReportResult;
import reportRunner.Service.GraphGroupService;
import reportRunner.Util.TestUtility;
import reportRunner.tsdb.Prometheus.PrometheusController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ConfirmMaxController {

    private static final String RESULTS_FOLDER = "results/";
    private static final String GRAFANA_IMG_PATH = "grafana_img/";

    private final UtilityConfig utilityConfig;
    private final JiraConfig jiraConfig;
    private final ConfirmMaxConfig confirmMaxConfig;
    private final GraphGroupService graphGroupService;
    private final GrafanaConfig grafanaConfig;
    private final ConfluenceService confluenceService;
    private final TestUtility testUtility;

    @Autowired
    public ConfirmMaxController(UtilityConfig utilityConfig, JiraConfig jiraConfig,
                                ConfirmMaxConfig confirmMaxConfig, GraphGroupService graphGroupService,
                                GrafanaConfig grafanaConfig, ConfluenceService confluenceService,
                                TestUtility testUtility) {
        this.utilityConfig = utilityConfig;
        this.jiraConfig = jiraConfig;
        this.confirmMaxConfig = confirmMaxConfig;
        this.graphGroupService = graphGroupService;
        this.grafanaConfig = grafanaConfig;
        this.confluenceService = confluenceService;
        this.testUtility = testUtility;
    }

    public ReportResult processConfirmMaxReport(String pageId) {
        String path = buildGrafanaImgPath();
        Map<String, Long> timestamps = calculateTimestamps();
        GrafanaService grafanaService = createGrafanaApi(timestamps, path);
        List<GraphGroup> groupOfGraphs = loadAndProcessGraphGroups(timestamps, grafanaService);
        String uploadResult = uploadToConfluence(pageId, groupOfGraphs, grafanaService);

        return new ReportResult(timestamps, groupOfGraphs, uploadResult);
    }

    private String buildGrafanaImgPath() {
        return utilityConfig.getResultsFolder() + "/" + jiraConfig.getTaskId() + "/" + GRAFANA_IMG_PATH;
    }

    private Map<String, Long> calculateTimestamps() {
        Map<String, LocalDateTime> times = testUtility.calclateTimestampsForGatling(
                confirmMaxConfig.getTestEndTime(), confirmMaxConfig.getStepDuration());

        return testUtility.convertToTimestamp(times.get("startTime"), times.get("endTime"));
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
