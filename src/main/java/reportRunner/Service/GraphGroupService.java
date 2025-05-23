package reportRunner.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reportRunner.Config.UtilityConfig;
import reportRunner.Config.Grafana.GrafanaConfigProperties;
import reportRunner.Grafana.GraphGroup;
import reportRunner.Grafana.GraphPanel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GraphGroupService {

    private final GrafanaConfigProperties grafanaConfigProperties;
    private final UtilityConfig utilityConfig;

    public List<GraphGroup> loadChartGroupsFromConfig() {
        return grafanaConfigProperties.getGrafanaGroups().stream()
                .flatMap(groupConfig -> {
                    // Базовые параметры группы
                    String groupName = groupConfig.getDashboardName();
                    String groupId = groupConfig.getDashboardUUID();
                    String title = groupConfig.getTitle();
                    boolean needPod = groupConfig.isNeedPod();

                    // Создаем список панелей
                    List<GraphPanel> panels = groupConfig.getPanels().stream()
                            .map(id -> {
                                GraphPanel panel = new GraphPanel();
                                panel.setPanelId(String.valueOf(id));
                                panel.setPanelName("");  // Имя панели будет определено позже через Grafana API
                                panel.setApplication("");  // URL панели также формируется позже
                                return panel;
                            })
                            .collect(Collectors.toList());

                    // Загружаем список переменных (applications)
                    List<String> variables = groupConfig.getApplications().isEmpty()
                            ? Collections.singletonList("")  // Пустая строка, если переменные не заданы
                            : groupConfig.getApplications();

                    // Генерируем список групп
                    return variables.stream().map(variable -> {
                        GraphGroup group = new GraphGroup(utilityConfig);
                        group.setGroupName(groupName);
                        group.setGroupId(groupId);
                        group.setTitle(title);
                        group.setNeedPod(needPod);
                        group.setPanels(panels.stream()
                                .map(originalPanel -> {
                                    GraphPanel copy = new GraphPanel();
                                    copy.setPanelId(originalPanel.getPanelId());
                                    copy.setPanelName(originalPanel.getPanelName());
                                    copy.setApplication(originalPanel.getApplication());
                                    return copy;
                                })
                                .collect(Collectors.toList()));  // Копируем панели
                        group.setVariable(variable);  // Устанавливаем текущую переменную
                        return group;
                    });
                })
                .collect(Collectors.toList());
    }
}
