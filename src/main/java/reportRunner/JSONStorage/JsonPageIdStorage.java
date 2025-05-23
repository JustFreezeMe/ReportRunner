package reportRunner.JSONStorage;

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
public class JsonPageIdStorage {
    private final JiraConfig jiraConfig;
    private final UtilityConfig utilityConfig;
    private final File file;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PageIdStorage pageIdStorage;
    private final Map<String, String> data; // Старая Map<String, String>

    public JsonPageIdStorage(JiraConfig jiraConfig, UtilityConfig utilityConfig) throws IOException {
        this.jiraConfig = jiraConfig;
        this.utilityConfig = utilityConfig;
        this.file = new File(utilityConfig.getResultsFolder() + "/" + jiraConfig.getTaskId() + "/" + "pageIds.json");

        if (file.exists()) {
            this.pageIdStorage = readJson();
            this.data = new HashMap<>(pageIdStorage.getChildPages()); // Копируем старые данные
        } else {
            this.pageIdStorage = new PageIdStorage();
            this.data = new HashMap<>();
            ensureFileExists();
        }
    }

    public boolean isFileExists() {
        return file.exists() && pageIdStorage.getRootPage() != null;
    }

    public void setRootPageId(String rootPageId) throws IOException {
        pageIdStorage.setRootPage(rootPageId);
        writeJson();
    }

    public void addChildPageId(String pageName, String pageId) throws IOException {
        pageIdStorage.getChildPages().put(pageName, pageId);
        writeJson();
    }

    public String getRootPageId() {
        return pageIdStorage.getRootPage();
    }

    public String getChildPageId(String pageName) {
        return pageIdStorage.getChildPages().get(pageName);
    }

    public Map<String, String> getChildPages() {
        return pageIdStorage.getChildPages();
    }

    public void updatePageId(String scenario, String pageId) throws IOException {
        data.put(scenario, pageId);
        pageIdStorage.getChildPages().put(scenario, pageId); // Обновляем обе структуры
        writeJson();
    }

    public String getPageId(String scenario) {
        return data.get(scenario);
    }


    private PageIdStorage readJson() throws IOException {
        return MAPPER.readValue(file, PageIdStorage.class);
    }

    private void writeJson() throws IOException {
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, pageIdStorage);
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
