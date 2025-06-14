package reportRunner.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reportRunner.Config.GrafanaConfig;
import reportRunner.Config.JiraConfig;
import reportRunner.Config.UtilityConfig;
import reportRunner.Service.ConfluenceService.ConfluenceService;
import reportRunner.Config.FaultToleranceConfig;
import reportRunner.Service.FaultToleranceService.FaultToleranceScenario;
import reportRunner.Service.GrafanaService.GraphGroupService;
import reportRunner.Utility.ReportUtility;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class FaultToleranceController {
    private static final String RESULTS_FOLDER = "results/";
    private static final String GRAFANA_IMG_PATH = "grafana_img/";

    private final UtilityConfig utilityConfig;
    private final JiraConfig jiraConfig;
    private final FaultToleranceConfig faultToleranceConfig;
    private final GraphGroupService graphGroupService;
    private final GrafanaConfig grafanaConfig;
    private final ConfluenceService confluenceService;
    private final ReportUtility testUtility;
    private static final String TITLE_PREFIX = "Протокол теста отказоустойчивости. Отключение: ";

    @Autowired
    public FaultToleranceController(UtilityConfig utilityConfig, JiraConfig jiraConfig,
                                    FaultToleranceConfig faultToleranceConfig, GraphGroupService graphGroupService,
                                    GrafanaConfig grafanaConfig, ConfluenceService confluenceService,
                                    ReportUtility testUtility) {
        this.utilityConfig = utilityConfig;
        this.jiraConfig = jiraConfig;
        this.faultToleranceConfig = faultToleranceConfig;
        this.graphGroupService = graphGroupService;
        this.grafanaConfig = grafanaConfig;
        this.confluenceService = confluenceService;
        this.testUtility = testUtility;
    }

//    @SneakyThrows
//    public List<ReportResult> processFaultToleranceReport(String pageId) {
//        String path = buildGrafanaImgPath();
//        GraphGroup graphGroup = new GraphGroup(utilityConfig);
//        JsonPageIdRepository pageIdStorage = new JsonPageIdRepository(jiraConfig, utilityConfig);
//        PrometheusController prometheus = new PrometheusController(grafanaConfig.getPrometheusUrl(), grafanaConfig);
//        GrafanaService grafanaApi = new GrafanaService(grafanaConfig);
//        List<FaultToleranceScenario> scenarios = faultToleranceConfig.getStages();
//        List<GraphGroup> groupOfGraphs = graphGroupService.loadChartGroupsFromConfig();
//        List<ReportResult> results = new ArrayList<>();
//
//        for (FaultToleranceScenario scenario : scenarios) {
//            String realEndTime = scenario.getFaultScenarioTestEnd();
//            scenario.setFaultScenarioTestEnd(scenario.getEndTimeWithStabilization(scenario.getFaultScenarioTestEnd(), scenario.getFaultScenarioStabilizationLength()));
//            scenario.setTimestamps(calculateTimestamps(scenario));
//            grafanaApi.setEndTimestamp(scenario.getTimestamps().get("endTimestamp"));
//            grafanaApi.setStartTimestamp(scenario.getTimestamps().get("startTimestamp"));
//            grafanaApi.setPath(path + scenario.getFaultScenarioTestName().replace("/", "_") + "/");
//            groupOfGraphs = graphGroup.processGraphGroups(groupOfGraphs, scenario.getTimestamps(), grafanaApi, prometheus);
//
//            scenario.setFaultScenarioTestEnd(realEndTime);
//            scenario.setTimestamps(calculateTimestamps(scenario));
//            if (!pageIdStorage.getData().containsKey(scenario.getFaultScenarioTestName())) {
//                String childrenPage = confluenceService.confluenceCreateChildrenPage(TITLE_PREFIX + scenario.getFaultScenarioTestName(), pageId);
//                scenario.setConfluenceChildredPageId(confluenceService.getConfluencePageId(childrenPage));
//                pageIdStorage.updatePageId(scenario.getFaultScenarioTestName(), scenario.getConfluenceChildredPageId());
//            }
//
//            scenario.setConfluenceChildredPageId(pageIdStorage.getPageId(scenario.getFaultScenarioTestName()));
//
//            String uploadResult = confluenceService.confluenceUploadAttachment(
//                    scenario.getConfluenceChildredPageId(),
//                    "results/" + jiraConfig.getTaskId() + "/grafana_img/" + scenario.getFaultScenarioTestName().replace("/", "_"),
//                    groupOfGraphs,
//                    grafanaApi.getEndTimestamp());
//
//            results.add(new ReportResult(scenario.getTimestamps(), groupOfGraphs, uploadResult));
//
//            String updateResult = confluenceService.confluenceUpdateFaultTolerancePage(TITLE_PREFIX + scenario.getFaultScenarioTestName(),
//                    scenario.getConfluenceChildredPageId(),
//                    confluenceService.confluenceGetPageVersion(scenario.getConfluenceChildredPageId()),
//                    scenario,
//                    groupOfGraphs,
//                    grafanaApi.getEndTimestamp());
//
//            log.warn("Update page statusCode for Scenario: {} (pageId: {}) is {}", scenario.getFaultScenarioTestName(), scenario.getConfluenceChildredPageId(), updateResult);
//        }
//
//        return results;
//    }

    private Map<String, Long> calculateTimestamps(FaultToleranceScenario scenario) {
        Map<String, Long> map = new HashMap<>();
        ReportUtility testUtility = new ReportUtility();

        Map<String, LocalDateTime> times = testUtility.calculateTimestampsForGatling(scenario.getFaultScenarioTestEnd(), scenario.getFaultScenarioTestLength());
        Duration duration = Duration.ofMinutes(Long.parseLong(scenario.getFaultScenarioStabilizationLength()));
        Map<String, Long> timestamps = testUtility.convertToTimestamp(times.get("startTime").minus(duration.multipliedBy(2)), times.get("endTime"));

        map.put("startTimestamp", timestamps.get("startTimestamp"));
        map.put("endTimestamp", timestamps.get("endTimestamp"));

        return map;
    }

    private String buildGrafanaImgPath() {
        return utilityConfig.getResultsFolder() + "/" + jiraConfig.getTaskId() + "/" + GRAFANA_IMG_PATH;
    }
}
