package reportRunner.Util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;
import reportRunner.Config.ProfileConfig;

import java.io.File;
import java.util.List;

public class ProfileReader {

    public ProfileConfig readYaml(String path) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(new File(path), ProfileConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse YAML: " + path, e);
        }
    }

    @SneakyThrows
    public Long yamlSize(String path) {
        ProfileConfig config = readYaml(path);
        List<ProfileConfig.Script> scriptList = config.getProfile();

        return scriptList.stream()
                .filter(op -> op.getScriptName() != null && !op.getScriptName().isEmpty())
                .count();
    }
}
