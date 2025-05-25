package reportRunner.Config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileConfig {
    private List<Script> profile;

    @Data
    public static class Script {
        private String scenarioName; // может быть null
        private String scriptName;
        private String intensity;
        private String sla;
    }
}
