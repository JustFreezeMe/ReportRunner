package reportRunner.Util;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reportRunner.Config.JiraConfig;
import reportRunner.Config.UtilityConfig;

import java.io.File;

@Component
@AllArgsConstructor
public class FileUtils {

    private final UtilityConfig utilityConfig;
    private final JiraConfig jiraConfig;

    //Проверка, отчет уже  существует или нет
    public boolean fileExist() {
        File file = new File(utilityConfig.getResultsFolder() + "/" + jiraConfig.getTaskId() + "/" + jiraConfig.getTaskId() + "_page_id.txt");
        return file.exists();
    }
}
