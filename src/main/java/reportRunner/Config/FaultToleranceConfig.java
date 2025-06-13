package reportRunner.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import reportRunner.Service.FaultToleranceService.FaultToleranceScenario;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "fault-tolerance")
public class FaultToleranceConfig {
    private Boolean enable;
    private String testEndTime;
    private String loadLevel;
    private List<FaultToleranceScenario> stages;
}
