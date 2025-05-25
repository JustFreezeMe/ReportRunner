package reportRunner.Controller;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reportRunner.Config.Grafana.GrafanaConfig;
import reportRunner.Config.JiraConfig;
import reportRunner.Config.ReliabilityConfig;
import reportRunner.Config.UtilityConfig;
import reportRunner.Confluence.ConfluenceService;
import reportRunner.Grafana.GrafanaApi;
import reportRunner.Grafana.GraphGroup;
import reportRunner.ResultsCreator.ReportResult;
import reportRunner.Service.GraphGroupService;
import reportRunner.Util.TestUtility;
import reportRunner.tsdb.Prometheus.PrometheusController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ReliabilityController {
    private static final String RESULTS_FOLDER = "results/";
    private static final String GRAFANA_IMG_PATH = "grafana_img/";

    private final UtilityConfig utilityConfig;
    private final JiraConfig jiraConfig;
    private final ReliabilityConfig reliabilityConfig;
    private final GraphGroupService graphGroupService;
    private final GrafanaConfig grafanaConfig;
    private final ConfluenceService confluenceService;
    private final TestUtility testUtility;

    @Autowired
    public ReliabilityController(UtilityConfig utilityConfig, JiraConfig jiraConfig,
                                 ReliabilityConfig reliabilityConfig, GraphGroupService graphGroupService,
                                 GrafanaConfig grafanaConfig, ConfluenceService confluenceService,
                                 TestUtility testUtility) {
        this.utilityConfig = utilityConfig;
        this.jiraConfig = jiraConfig;
        this.reliabilityConfig = reliabilityConfig;
        this.graphGroupService = graphGroupService;
        this.grafanaConfig = grafanaConfig;
        this.confluenceService = confluenceService;
        this.testUtility = testUtility;
    }

    @SneakyThrows
    public ReportResult processReliabilityReport(String pageId) {
        String path = buildGrafanaImgPath();
        Map<String, Long> timestamps = calculateTimestamps();
        GrafanaApi grafanaApi = createGrafanaApi(timestamps, path);
        List<GraphGroup> groupOfGraphs = loadAndProcessGraphGroups(timestamps, grafanaApi);
        //forE (graph : groupOfGraphs)
        String uploadResult = uploadToConfluence(pageId, groupOfGraphs, grafanaApi);

        return new ReportResult(timestamps, groupOfGraphs, uploadResult);
    }

    private String buildGrafanaImgPath() {
        return utilityConfig.getResultsFolder() + "/" + jiraConfig.getTaskId() + "/" + GRAFANA_IMG_PATH;
    }

    private Map<String, Long> calculateTimestamps() {
        Map<String, LocalDateTime> times = testUtility.calclateTimestampsForGatling(
                reliabilityConfig.getTestEndTime(), reliabilityConfig.getStepDuration());

        return testUtility.convertToTimestamp(times.get("startTime"), times.get("endTime"));
    }

    private GrafanaApi createGrafanaApi(Map<String, Long> timestamps, String path) {
        GrafanaApi grafanaApi = new GrafanaApi(grafanaConfig);
        grafanaApi.setStartTimestamp(timestamps.get("startTimestamp"));
        grafanaApi.setEndTimestamp(timestamps.get("endTimestamp"));
        grafanaApi.setPath(path);
        return grafanaApi;
    }

    @SneakyThrows
    private List<GraphGroup> loadAndProcessGraphGroups(Map<String, Long> timestamps, GrafanaApi grafanaApi) {
        GraphGroup graphGroup = new GraphGroup(utilityConfig);
        List<GraphGroup> groupOfGraphs = graphGroupService.loadChartGroupsFromConfig();
        PrometheusController prometheus = new PrometheusController(grafanaConfig.getPrometheusUrl(), grafanaConfig);
        return graphGroup.processGraphGroups(groupOfGraphs, timestamps, grafanaApi, prometheus);
    }

    private String uploadToConfluence(String pageId, List<GraphGroup> groupOfGraphs, GrafanaApi grafanaApi) {
        return confluenceService.confluenceUploadAttachment(
                pageId,
                RESULTS_FOLDER + jiraConfig.getTaskId() + "/" + GRAFANA_IMG_PATH,
                groupOfGraphs,
                grafanaApi.getEndTimestamp());
    }
}
