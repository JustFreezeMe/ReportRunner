package reportRunner.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "confluence")
public class ConfluenceConfig {
    private String certName;
    private String confluenceUrl;
    private String confluenceSpaceKey;
    private String confluenceParentPageId;
}
