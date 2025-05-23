package reportRunner.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "performance-center")
@Data
public class PerformanceCenterConfig {
    private String pcUrl;
    private String pcDomain;
    private String pcProject;
}
