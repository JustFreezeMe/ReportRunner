package reportRunner.Run;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reportRunner.Config.ConfirmMaxConfig;
import reportRunner.Config.ConfluenceConfig;
import reportRunner.Config.JiraConfig;
import reportRunner.Config.MaxPerformanceConfig;
import reportRunner.Config.ReliabilityConfig;
import reportRunner.Config.TestControllerFactory;
import reportRunner.Config.UtilityConfig;
import reportRunner.Confluence.ConfluenceService;
import reportRunner.Controller.TestController;
import reportRunner.FaultTolerance.FaultToleranceProperties;
import reportRunner.FaultTolerance.FaultToleranceTest;
import reportRunner.Results.ReportResult;
import reportRunner.JSONStorage.JsonPageIdStorage;
import reportRunner.Util.TestUtility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Component
public class ReportRunner {
    private final TestControllerFactory testControllerFactory;
    private final ReliabilityConfig reliabilityConfig;
    private final MaxPerformanceConfig maxPerformanceConfig;
    private final ConfirmMaxConfig confirmMaxConfig;
    private final ConfluenceConfig confluenceConfig;
    private final UtilityConfig utilityConfig;
    private final JiraConfig jiraConfig;
    private final FaultToleranceProperties faultToleranceProperties;
    private final ConfluenceService confluenceService;

    public void run() throws IOException {

        JsonPageIdStorage pageIdStorage = new JsonPageIdStorage(jiraConfig, utilityConfig);
        TestUtility testUtility = new TestUtility();
        FaultToleranceTest faultToleranceTest = new FaultToleranceTest(
                faultToleranceProperties.getTestEndTime(),
                faultToleranceProperties.getLoadLevel(),
                faultToleranceProperties.getStages());

        Map<String, Long> testNames = new HashMap<>();
        List<ReportResult> faultToleranceResult = new ArrayList<>();

        if (pageIdStorage.isFileExists()) {
            processExistingReport(testUtility, testNames, faultToleranceResult, faultToleranceTest, pageIdStorage);
        } else {
            utilityConfig.setGraphsNeeded(true);
            processNewReport(testUtility, testNames, faultToleranceResult, faultToleranceTest);
        }
    }

    @SneakyThrows
    private void processExistingReport(TestUtility testUtility, Map<String, Long> testNames,
                                       List<ReportResult> faultToleranceResult, FaultToleranceTest faultToleranceTest, JsonPageIdStorage pageIdStorage) {
        Map<String, String> childPageIds = pageIdStorage.getChildPages();
        initTestNames(testNames);

        testNames.forEach((testName, timestamp) -> {
            // Ищем страницу, название которой содержит | testName |
            Optional<Map.Entry<String, String>> matchingEntry = childPageIds.entrySet().stream()
                    .filter(entry -> {
                        String[] parts = entry.getKey().split("\\|");
                        for (String part : parts) {
                            if (part.trim().equalsIgnoreCase(testName)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .findFirst();

            if (matchingEntry.isEmpty()) {
                log.warn("Не найдена страница для теста '{}'", testName);
                return;
            }

            String pageTitle = matchingEntry.get().getKey();
            String pageId = matchingEntry.get().getValue();

            TestController tests = testControllerFactory.createTestController(pageId);
            ReportResult testResult = runReport(tests, testNames, faultToleranceResult, testName);

            updateConfluencePage(
                    pageId,
                    pageTitle,
                    testResult,
                    singleTestMap(testName, timestamp),
                    faultToleranceResult,
                    faultToleranceTest,
                    testUtility
            );

            log.info("Ссылка на страницу с обновленным отчетом {}: https://confluence.moscow.alfaintra.net/pages/viewpage.action?pageId={}", pageTitle, pageId);
        });
    }

    @SneakyThrows
    private void processNewReport(TestUtility testUtility, Map<String, Long> testNames,
                                  List<ReportResult> faultToleranceResult, FaultToleranceTest faultToleranceTest) {
        JsonPageIdStorage pageIdStorage = new JsonPageIdStorage(jiraConfig, utilityConfig);
        Path folderPath = Paths.get(utilityConfig.getResultsFolder(), jiraConfig.getTaskId());
        Files.createDirectories(folderPath);
        Map<String, String> reportPageIds = new HashMap<>();
        String createConclusionPage = confluenceService.confluenceCreateChildrenPage(
                createConclusionPageName(testUtility), confluenceConfig.getConfluenceParentPageId());
        String pageId = confluenceService.getConfluencePageId(createConclusionPage);
        String version = confluenceService.confluenceGetPageVersion(pageId);
        pageIdStorage.setRootPageId(pageId);
        initTestNames(testNames);

        testNames.forEach((testName, timestamp) -> {

            String childPageTitle = createReportPageName(testUtility, testName);
            String childPageId = confluenceService.getConfluencePageId(confluenceService.confluenceCreateChildrenPage(childPageTitle, pageId));
            TestController tests = testControllerFactory.createTestController(childPageId);
            ReportResult testResult = runReport(tests, testNames, faultToleranceResult, testName);
            timestamp = testNames.get(testName);
            try {

                pageIdStorage.addChildPageId(childPageTitle, childPageId);
                reportPageIds.put(testName, childPageId);

                updateConfluencePage(childPageId, childPageTitle, testResult, singleTestMap(testName, timestamp), faultToleranceResult, faultToleranceTest, testUtility);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        confluenceService.confluenceUpdateConclusionPage(createConclusionPageName(testUtility), pageId, version, reportPageIds);

        log.info("Ссылка на страницу с заключением: https://confluence.moscow.alfaintra.net/pages/viewpage.action?pageId={}", pageId);
        reportPageIds.forEach((pageTitle, reportPageId) -> {
            log.info("Ссылка на страницу с отчетом {}: https://confluence.moscow.alfaintra.net/pages/viewpage.action?pageId={}", pageTitle, reportPageId);
        });
    }

    @SneakyThrows
    private ReportResult runReport(TestController tests, Map<String, Long> testNames, List<ReportResult> faultToleranceResult, String testName) {
        ReportResult testResult = new ReportResult(new HashMap<>(), new ArrayList<>(), "blank");

        if (testName.equals("Тест надежности") && reliabilityConfig.getEnable()) {
            testResult = tests.processReliabilityReport();
            testNames.replace("Тест надежности", testResult.getTimestamps().get("endTimestamp"));
        }
        if (testName.equals("Поиск максимальной производительности") && maxPerformanceConfig.getEnable()) {
            testResult = tests.processMaxPerfReport();
            testNames.replace("Поиск максимальной производительности", testResult.getTimestamps().get("endTimestamp"));
        }
        if (testName.equals("Подтверждение максимальной производительности") && confirmMaxConfig.getEnable()) {
            testResult = tests.processConfirmMaxReport();
            testNames.replace("Подтверждение максимальной производительности", testResult.getTimestamps().get("endTimestamp"));
        }
        if (faultToleranceProperties.getEnable()) {
            faultToleranceResult.addAll(tests.processFaultToleranceReport());
            testResult = faultToleranceResult.get(0);
            testNames.replace("Тест отказоустойчивости", testResult.getTimestamps().get("endTimestamp"));
        }
        return testResult;
    }

    @SneakyThrows
    private void updateConfluencePage(String pageId, String pageTitle, ReportResult testResult, Map<String, Long> testNames,
                                      List<ReportResult> faultToleranceResult, FaultToleranceTest faultToleranceTest, TestUtility testUtility) {
        String version = confluenceService.confluenceGetPageVersion(pageId);


        if (confirmMaxConfig.getEnable() || maxPerformanceConfig.getEnable() || reliabilityConfig.getEnable()) {
            Map<String, Long> relevantTests = extractTestNamesBetweenPipes(pageTitle, testNames);

            confluenceService.confluenceUpdateReportPage(
                    pageTitle, pageId, version, testResult.getGroupOfGraphs(),
                    testResult.getTimestamps().get("endTimestamp"), relevantTests);
        }

        if (faultToleranceProperties.getEnable()) {
            confluenceService.confluenceUpdateFaultToleranceMainPage(
                    pageTitle, pageId, version, faultToleranceResult, faultToleranceTest);
        }
    }

    private Map<String, Long> extractTestNamesBetweenPipes(String pageTitle, Map<String, Long> testNames) {
        String[] parts = pageTitle.split("\\|");
        return Arrays.stream(parts)
                .map(String::trim)
                .filter(testNames::containsKey)
                .collect(Collectors.toMap(name -> name, testNames::get));
    }

    private String createConclusionPageName(TestUtility testUtility) {
        String currentDate = testUtility.getCurrentDate();
        return currentDate + " | " + jiraConfig.getTaskTitle() + " | Заключение по НТ";
    }

    private String createReportPageName(TestUtility testUtility, String testName) {
        String currentDate = testUtility.getCurrentDate();
        return currentDate + " | " + jiraConfig.getTaskTitle() + " | " + testName + " | Отчет по НТ";
    }

    private void initTestNames(Map<String, Long> testNames) {
        if (reliabilityConfig.getEnable()) {
            testNames.putIfAbsent("Тест надежности", 0L);
        }
        if (maxPerformanceConfig.getEnable()) {
            testNames.putIfAbsent("Поиск максимальной производительности", 0L);
        }
        if (confirmMaxConfig.getEnable()) {
            testNames.putIfAbsent("Подтверждение максимальной производительности", 0L);
        }
        if (faultToleranceProperties.getEnable()) {
            testNames.putIfAbsent("Тест отказоустойчивости", 0L);
        }
    }

    private Map<String, Long> singleTestMap(String testName, Long timestamp) {
        return Collections.singletonMap(testName, timestamp);
    }
}


