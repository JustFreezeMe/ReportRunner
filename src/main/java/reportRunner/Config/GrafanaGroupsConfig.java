package reportRunner.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import reportRunner.Service.GrafanaService.GraphGroupDTO;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "grafana.groups")
@Data
public class GrafanaGroupsConfig {
    private List<GraphGroupDTO> grafanaGroups = new ArrayList<>();
}
