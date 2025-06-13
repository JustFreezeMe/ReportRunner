package reportRunner.Service.GrafanaService;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GraphGroupDTO {
    private String dashboardName;
    private String dashboardUUID;
    private String title;
    private boolean needPod;
    private List<Integer> panels = new ArrayList<>();
    private List<String> applications = new ArrayList<>();
}
