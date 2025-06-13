package reportRunner.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "grafana")
@Data
public class GrafanaConfig {
    private String grafanaUrl;
    private String grafanaApiKey;
    private Integer grafanaWidth;
    private Integer grafanaHeight;
    private String prometheusUrl;
    private String environment;
    private KubernetesConfig kubernetes;

    @Data
    public static class KubernetesConfig {
        private String namespaceName;
    }
}
