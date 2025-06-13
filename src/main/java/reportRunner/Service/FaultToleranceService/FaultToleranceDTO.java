package reportRunner.Service.FaultToleranceService;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FaultToleranceDTO {
    private String testEndTime;
    private String loadLevel;
    private List<FaultToleranceScenario> scenarios;
}
