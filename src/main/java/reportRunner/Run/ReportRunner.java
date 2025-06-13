package reportRunner.Run;

import reportRunner.Config.ConfluenceConfig;
import reportRunner.Config.JiraConfig;
import reportRunner.Config.UtilityConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reportRunner.Config.*;
import reportRunner.Config.FaultToleranceConfig;
import reportRunner.Config.TestConfig.TestConfig;
import reportRunner.Config.TestConfig.TestListConfig;
import reportRunner.Service.FaultToleranceService.FaultToleranceDTO;
import reportRunner.Service.ConfluenceService.ConfluenceService;
import reportRunner.Repository.JSON.JsonPageIdRepository;
import reportRunner.Service.ResultsService.ReportResult;
import reportRunner.Service.TestService.TestService;
import reportRunner.Utility.ReportUtility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReportRunner {
    private final TestService testService;
    private final ConfluenceConfig confluenceConfig;
    private final UtilityConfig utilityConfig;
    private final JiraConfig jiraConfig;
    private final FaultToleranceConfig faultToleranceConfig;
    private final ConfluenceService confluenceService;
    private final TestListConfig testListConfig;

    public void run() throws IOException {

        JsonPageIdRepository pageIdStorage = new JsonPageIdRepository(jiraConfig, utilityConfig);
        ReportUtility testUtility = new ReportUtility();

        if (pageIdStorage.isFileExists()) {
            processExistingReport(pageIdStorage);
        } else {
            utilityConfig.setGraphsNeeded(true);
            processNewReport(testUtility);
        }
    }

    @SneakyThrows
    private void processExistingReport(JsonPageIdRepository pageIdStorage) {
        Map<String, String> childPageIds = pageIdStorage.getChildPages();

        testListConfig.getTestList().forEach((test) -> {
            String testName = test.getTestType();
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

            ReportResult testResult = testService.dispatchTest(test,  pageId);

            updateConfluencePage(pageId, pageTitle, testResult, test);

            log.info("Ссылка на страницу с обновленным отчетом {}: https://confluence.moscow.alfaintra.net/pages/viewpage.action?pageId={}", pageTitle, pageId);
        });
    }


    @SneakyThrows
    private void processNewReport(ReportUtility testUtility) {

        JsonPageIdRepository pageIdStorage = new JsonPageIdRepository(jiraConfig, utilityConfig);
        Path folderPath = Paths.get(utilityConfig.getResultsFolder(), jiraConfig.getTaskId());
        Files.createDirectories(folderPath);
        Map<String, String> reportPageIds = new HashMap<>();
        String createConclusionPage = confluenceService.confluenceCreateChildrenPage(
                createConclusionPageName(testUtility), confluenceConfig.getConfluenceParentPageId());
        String pageId = confluenceService.getConfluencePageId(createConclusionPage);
        String version = confluenceService.confluenceGetPageVersion(pageId);
        pageIdStorage.setRootPageId(pageId);

        testListConfig.getTestList().forEach(test -> {
            String testName = test.getTestType();
            String childPageTitle = createReportPageName(testUtility, testName);
            String childPageId = confluenceService.getConfluencePageId(
                    confluenceService.confluenceCreateChildrenPage(childPageTitle, pageId)
            );
            ReportResult testResult = testService.dispatchTest(test, childPageId);
            try {
                pageIdStorage.addChildPageId(childPageTitle, childPageId);
                reportPageIds.put(testName, childPageId);
                updateConfluencePage(childPageId, childPageTitle, testResult, test);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        confluenceService.confluenceUpdateConclusionPage(
                createConclusionPageName(testUtility), pageId, version, reportPageIds,testListConfig.getTestList().get(0)
        );

        log.info("Ссылка на страницу с заключением: https://confluence.moscow.alfaintra.net/pages/viewpage.action?pageId={}", pageId);
        reportPageIds.forEach((pageTitle, reportPageId) ->
                log.info("Ссылка на страницу с отчетом {}: https://confluence.moscow.alfaintra.net/pages/viewpage.action?pageId={}", pageTitle, reportPageId)
        );
    }


    @SneakyThrows
    private void updateConfluencePage(String pageId, String pageTitle, ReportResult testResult, TestConfig test) {

        String version = confluenceService.confluenceGetPageVersion(pageId);
        confluenceService.confluenceUpdateReportPageV2(
                pageTitle, pageId, version, testResult.getGroupOfGraphs(), test);
    }

    private String createConclusionPageName(ReportUtility testUtility) {
        String currentDate = testUtility.getCurrentDate();
        return currentDate + " | " + jiraConfig.getTaskTitle() + " | Заключение по НТ";
    }

    private String createReportPageName(ReportUtility testUtility, String testName) {
        String currentDate = testUtility.getCurrentDate();
        return currentDate + " | " + jiraConfig.getTaskTitle() + " | " + testName + " | Отчет по НТ";
    }
}


