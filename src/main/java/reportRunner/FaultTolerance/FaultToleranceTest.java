package reportRunner.FaultTolerance;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FaultToleranceTest {
    private String testEndTime;
    private String loadLevel;
    private List<FaultToleranceScenario> scenarios;
}
