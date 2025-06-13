package reportRunner.Service.TestService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reportRunner.Config.TestConfig.TestConfig;
import reportRunner.Config.TestConfig.TestListConfig;
import reportRunner.Controller.ConfirmMaxController;
import reportRunner.Controller.MaxPerfController;
import reportRunner.Controller.ReliabilityController;
import reportRunner.Model.enums.TestTypes;
import reportRunner.Service.ResultsService.ReportResult;

import javax.annotation.PostConstruct;
import java.util.List;

@Data
@Service
@RequiredArgsConstructor
public class TestService {

    private final TestListConfig testListConfig;
    private List<TestConfig> testList;
    private final MaxPerfController maxPerfController;
    private final ConfirmMaxController confirmMaxController;
    private final ReliabilityController reliabilityController;

//    public TestService(TestListConfig testListConfig) {
//        this.testListConfig = testListConfig;
//        setTestList(testListConfig.loadTestsFromConfig());
//    }

    @PostConstruct
    public void init(){
        this.testList = testListConfig.loadTestsFromConfig();
    }
    public ReportResult dispatchTest(TestConfig test,String childPageId) {
          return  switch (TestTypes.fromTitle(test.getTestType())) {
                case MAX_PERFORMANCE -> handleMaxPerformance(test,childPageId);
                case CONFIRM_MAX -> handleConfirmMax(test,childPageId);
                case RELIABILITY -> handleReliability(test,childPageId);
                default -> throw new IllegalArgumentException("Неизвестный тип теста");
//                case FAULT_TOLERANCE -> handleMaxPerformance(test);
        };
    }

    private ReportResult handleMaxPerformance(TestConfig test, String childPageId) {
       return maxPerfController.processMaxPerformanceReport(test,childPageId);
    }

    private ReportResult handleConfirmMax(TestConfig test, String childPageId) {
        return confirmMaxController.processConfirmMaxReport(test,childPageId);
    }

    private ReportResult handleReliability(TestConfig test, String childPageId) {
        return reliabilityController.processReliabilityReport(test,childPageId);
    }
}
