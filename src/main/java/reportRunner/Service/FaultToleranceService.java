package reportRunner.Service;

import org.springframework.stereotype.Service;
import reportRunner.FaultTolerance.FaultToleranceProperties;
import reportRunner.FaultTolerance.FaultToleranceScenario;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FaultToleranceService {

    private final FaultToleranceProperties faultToleranceProperties;

    public FaultToleranceService(FaultToleranceProperties faultToleranceProperties) {
        this.faultToleranceProperties = faultToleranceProperties;
    }

    public List<FaultToleranceScenario> loadFaultToleranceScenariosFromConfig() {
        // Загружаем конфигурацию из файла
        // Преобразуем конфиг в объекты ChartGroup с разделением по переменным
        return faultToleranceProperties.getStages().stream().map(scenarioConfig -> {
            FaultToleranceScenario scenario = new FaultToleranceScenario();
            scenario.setFaultScenarioTestName(scenarioConfig.getFaultScenarioTestName());
            scenario.setFaultScenarioTestEnd(scenarioConfig.getFaultScenarioTestEnd());
            scenario.setFaultScenarioTestLength(scenarioConfig.getFaultScenarioTestLength());
            scenario.setFaultScenarioStabilizationLength(scenarioConfig.getFaultScenarioStabilizationLength());
            return scenario;
        }).collect(Collectors.toList());
    }
}
