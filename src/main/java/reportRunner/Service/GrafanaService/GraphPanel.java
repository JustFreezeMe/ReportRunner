package reportRunner.Service.GrafanaService;

import lombok.Data;


@Data
public class GraphPanel {
    private String panelId;
    private String panelName;
    private String application;
}
