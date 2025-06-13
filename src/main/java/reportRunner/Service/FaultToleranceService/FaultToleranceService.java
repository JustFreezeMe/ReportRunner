package reportRunner.Service.FaultToleranceService;

import org.springframework.stereotype.Service;
import reportRunner.Config.FaultToleranceConfig;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FaultToleranceService {

    private final FaultToleranceConfig faultToleranceConfig;

    public FaultToleranceService(FaultToleranceConfig faultToleranceConfig) {
        this.faultToleranceConfig = faultToleranceConfig;
    }

    public List<FaultToleranceScenario> loadFaultToleranceScenariosFromConfig() {
        // Загружаем конфигурацию из файла
        // Преобразуем конфиг в объекты ChartGroup с разделением по переменным
        return faultToleranceConfig.getStages().stream().map(scenarioConfig -> {
            FaultToleranceScenario scenario = new FaultToleranceScenario();
            scenario.setFaultScenarioTestName(scenarioConfig.getFaultScenarioTestName());
            scenario.setFaultScenarioTestEnd(scenarioConfig.getFaultScenarioTestEnd());
            scenario.setFaultScenarioTestLength(scenarioConfig.getFaultScenarioTestLength());
            scenario.setFaultScenarioStabilizationLength(scenarioConfig.getFaultScenarioStabilizationLength());
            return scenario;
        }).collect(Collectors.toList());
    }
}
