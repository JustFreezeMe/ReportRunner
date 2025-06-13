package reportRunner.Config.TestConfig;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Component
@Slf4j
@ConfigurationProperties(prefix = "test-list")
public class TestListConfig {
    List<TestConfig> testList;

    @PostConstruct
    public void post(){
        log.info("loaded from yaml: {}",testList);
    }
    public List<TestConfig> loadTestsFromConfig() {

        return this.getTestList().stream().map(testConfig -> {
            TestConfig test = new TestConfig();
            test.setTestType(testConfig.getTestType());
            test.setTestEndTime(testConfig.getTestEndTime());
            test.setLoadLevel(testConfig.getLoadLevel());
            test.setStepDuration(testConfig.getStepDuration());
            if (test.getTestStartTime() != null && !test.getTestStartTime().isEmpty()) test.setTestStartTime(testConfig.getTestStartTime());
            if (test.getRampTime() != null && !test.getRampTime().isEmpty()) test.setRampTime(testConfig.getRampTime());
            if (test.getSeparatingRamp() != null && !test.getSeparatingRamp().isEmpty()) test.setSeparatingRamp(testConfig.getSeparatingRamp());
            if (test.getStabilizationTime() != null && !test.getStabilizationTime().isEmpty()) test.setStabilizationTime(testConfig.getStabilizationTime());
            if (test.getStepPercent() != null) test.setStepPercent(testConfig.getStepPercent());
            if (test.getStepsCount() != null) test.setStepsCount(testConfig.getStepsCount());

            return test;
        }).collect(Collectors.toList());
    }
}
