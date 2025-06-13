package reportRunner.Config.InfrastructureConfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
//@Component
@ConfigurationProperties(prefix = "infrastructure")
public class InfrastructureConfig {
    private Map<String,ComponentWrapper> components;

    @Data
    public static class ComponentWrapper {
        private ComponentDTO configuration;
    }
}
