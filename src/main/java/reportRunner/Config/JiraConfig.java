package reportRunner.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jira")
@Data
public class JiraConfig {
    private String taskId;
    private String taskTitle;
}
