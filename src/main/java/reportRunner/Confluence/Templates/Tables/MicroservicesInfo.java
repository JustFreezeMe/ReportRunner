package reportRunner.Confluence.Templates.Tables;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MicroservicesInfo {
    private String name;
    private String version;
    private String appCpu;
    private String appRam;
    private String jvmOptions;

    public String toHtmlRow() {
        return "<tr><td>" + name + "</td><td>" + version + "</td>" +
                "<td>" + appCpu + "</td><td>" + appRam + "</td><td>" + jvmOptions + "</td></tr>";
    }
}
