package reportRunner.Config.Grafana;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "grafana.groups")
@Data
public class GrafanaConfigProperties {
    private List<GraphGroupConfig> grafanaGroups = new ArrayList<>();
}
