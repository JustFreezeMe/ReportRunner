package reportRunner.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "test-configuration.max-performance")
@Data
public class MaxPerformanceConfig {
    private Boolean enable;
    private String testEndTime;
    private String testStartTime;
    private String loadLevel;
    private String stepDuration;
    private Integer stepsCount;
    private String rampTime;
    private String separatingRamp;
    private Integer stepPercent;
    private String stabilizationTime;
}
