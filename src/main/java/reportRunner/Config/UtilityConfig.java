package reportRunner.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "utility")
@Data
public class UtilityConfig {
    private String resultsFolder;
    private String loadStation;
    private Boolean graphsNeeded;
}
