package reportRunner.Repository.JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.stereotype.Service;
import reportRunner.Config.JiraConfig;
import reportRunner.Config.UtilityConfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
@Service
public class JsonPageIdRepository {
    private final JiraConfig jiraConfig;
    private final UtilityConfig utilityConfig;
    private final File file;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PageIdRepository pageIdRepository;
    private final Map<String, String> data; // Старая Map<String, String>

    public JsonPageIdRepository(JiraConfig jiraConfig, UtilityConfig utilityConfig) throws IOException {
        this.jiraConfig = jiraConfig;
        this.utilityConfig = utilityConfig;
        this.file = new File(utilityConfig.getResultsFolder() + "/" + jiraConfig.getTaskId() + "/" + "pageIds.json");

        if (file.exists()) {
            this.pageIdRepository = readJson();
            this.data = new HashMap<>(pageIdRepository.getChildPages()); // Копируем старые данные
        } else {
            this.pageIdRepository = new PageIdRepository();
            this.data = new HashMap<>();
            ensureFileExists();
        }
    }

    public boolean isFileExists() {
        return file.exists() && pageIdRepository.getRootPage() != null;
    }

    public void setRootPageId(String rootPageId) throws IOException {
        pageIdRepository.setRootPage(rootPageId);
        writeJson();
    }

    public void addChildPageId(String pageName, String pageId) throws IOException {
        pageIdRepository.getChildPages().put(pageName, pageId);
        writeJson();
    }

    public String getRootPageId() {
        return pageIdRepository.getRootPage();
    }

    public String getChildPageId(String pageName) {
        return pageIdRepository.getChildPages().get(pageName);
    }

    public Map<String, String> getChildPages() {
        return pageIdRepository.getChildPages();
    }

    public void updatePageId(String scenario, String pageId) throws IOException {
        data.put(scenario, pageId);
        pageIdRepository.getChildPages().put(scenario, pageId); // Обновляем обе структуры
        writeJson();
    }

    public String getPageId(String scenario) {
        return data.get(scenario);
    }


    private PageIdRepository readJson() throws IOException {
        return MAPPER.readValue(file, PageIdRepository.class);
    }

    private void writeJson() throws IOException {
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, pageIdRepository);
    }

    private void ensureFileExists() throws IOException {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        file.createNewFile();
        writeJson(); // Записываем пустой JSON
    }
}
