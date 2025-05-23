package reportRunner.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "test-configuration.confirm-max")
@Data
public class ConfirmMaxConfig {
    private Boolean enable;
    private String testEndTime;
    private String testDuration;
    private String loadLevel;
    private String stepDuration;
}
