package reportRunner.Config.Grafana;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GraphGroupConfig {
    private String dashboardName;
    private String dashboardUUID;
    private String title;
    private boolean needPod;
    private List<Integer> panels = new ArrayList<>();
    private List<String> applications = new ArrayList<>();
    private Map<String, List<String>> grafanaVariables = new HashMap<>(); // новые переменные
}
