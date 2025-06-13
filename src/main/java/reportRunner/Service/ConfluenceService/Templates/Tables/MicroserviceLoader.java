package reportRunner.Service.ConfluenceService.Templates.Tables;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class MicroserviceLoader {
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static List<MicroservicesInfo> loadFromYaml(String filepath) throws IOException {
        return yamlMapper.readValue(new File(filepath),
                yamlMapper.getTypeFactory().constructCollectionType(List.class, MicroservicesInfo.class));
    }
}
