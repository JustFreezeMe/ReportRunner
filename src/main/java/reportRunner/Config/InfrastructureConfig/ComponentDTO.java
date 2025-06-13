package reportRunner.Config.InfrastructureConfig;

import lombok.Data;

@Data
public class ComponentDTO {
    private String cpu;
    private String ram;
    private String nodesCount;

    public String toHtmlRow() {
        return "<td>" + nodesCount + "</td><td>" + cpu + "</td><td>" + ram + "</td>";
    }
}
