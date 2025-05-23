package reportRunner.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "test-configuration.reliability")
@Data
public class ReliabilityConfig {
    private Boolean enable;
    private String testEndTime;
    private String loadLevel;
    private String stepDuration;
}
