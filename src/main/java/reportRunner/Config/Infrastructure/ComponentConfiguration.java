package reportRunner.Config.Infrastructure;

import lombok.Data;

@Data
public class ComponentConfiguration {
    private String cpu;
    private String ram;
    private String nodesCount;

    public String toHtmlRow() {
        return "<td>" + nodesCount + "</td><td>" + cpu + "</td><td>" + ram + "</td>";
    }
}
