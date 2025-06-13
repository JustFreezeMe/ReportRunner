package reportRunner.Confluence.ResultProcessor;

import lombok.SneakyThrows;
import reportRunner.Config.ProfileConfig;
import reportRunner.Csv.CsvUtility;
import reportRunner.Service.GrafanaService.GraphGroup;
import reportRunner.Model.Test;
import reportRunner.ResultsCreator.TestTypes.MaxPerformanceTestType;
import reportRunner.ResultsCreator.TestTypes.ReliabilityTestType;
import reportRunner.Util.ProfileReader;

import java.util.List;

public class ReportProcessor {

    CsvUtility csv = new CsvUtility();
    ProfileReader profileReader = new ProfileReader();
    ReliabilityTestType reliabilityTestType;
    MaxPerformanceTestType maxPerformanceTestType;

    private static final String PROFILE_FILE_PATH = "src/main/resources/profile.yaml";

    public ReportProcessor(ReliabilityTestType reliabilityTestType, MaxPerformanceTestType maxPerformanceTestType) {
        this.reliabilityTestType = reliabilityTestType;
        this.maxPerformanceTestType = maxPerformanceTestType;
    }

    public String createImageMacro(String filename) {
        return "<ac:image>\n" + "<ri:attachment ri:filename=\n" + "\"" + filename + ".png\" />\n" + "</ac:image>";
    }

    public String createExpandForText(String body, String title) {

        return "<ac:structured-macro ac:name=\"expand\">\n" + "  <ac:parameter ac:name=\"title\">" + title + "</ac:parameter>\n" + "  <ac:rich-text-body>\n" + "    \n" + "<p>" + body + "</p>\n" + "  </ac:rich-text-body>\n" + "</ac:structured-macro>";
    }

    @SneakyThrows
    public String createTableinCycleForProfile(Long len, String path) {

        ProfileConfig config = profileReader.readYaml(path);
        List<ProfileConfig.Script> profile = config.getProfile();
        StringBuilder sb = new StringBuilder();
        sb.append("<h2><b>3. Профиль нагрузочного тестирования</b></h2>").append("<table>").append("<tr><th>№</th><th>Описание сценария</th><th>Название скрипта</th><th>Целевая интенсивность, операций/час</th><th>SLA, сек</th></tr>");
        for (int i = 0; i < len; i++) {
            ProfileConfig.Script script = profile.get(i);
            sb.append("<tr><td>").append(i + 1).append("</td><td>").append(script.getScenarioName()).append("</td><td>").append(script.getScriptName()).append("</td><td>").append(script.getIntensity()).append("</td><td>").append(script.getSla()).append("</td></tr>");
        }
        sb.append("<tr><td><b>Итого</b></td><td></td><td></td><td><b>").append(profileReader.totalIntensity(path)).append("</b></td><td></td></tr>").append("</table>");

        return sb.toString();
    }

    public String createExpandForMetrics(GraphGroup graphGroup, String application, long timestamp) {

        int size = graphGroup.getPanels().size();

        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < size; i++) {
            pattern.append("<p><b>").append(graphGroup.getPanels().get(i).getPanelName()).append("</b></p>");
            pattern.append(createImageMacro(graphGroup.getPanels().get(i).getPanelName() + "_" + timestamp));
        }
        return "<ac:structured-macro ac:name=\"expand\">\n" + "  <ac:parameter ac:name=\"title\">" + graphGroup.getTitle() + " " + application.replace(":", "_") + "</ac:parameter>\n" + "  <ac:rich-text-body>\n" + "    \n" + "<p>" + pattern + "</p>\n" + "  </ac:rich-text-body>\n" + "</ac:structured-macro>";
    }

    public String createFaultToleranceTemplate(Long len, List<GraphGroup> groupOfGraphs, long timestamp, Test test) {
        String template = "<h3>Результаты теста отказоустойчивости</h3>\n";

        template += createExpandForText(reliabilityTestType.createTableForResults(len - 1, PROFILE_FILE_PATH, test), "Результаты теста надежности");
        StringBuilder graphTemplate = new StringBuilder();

        for (GraphGroup groupOfGraph : groupOfGraphs) {
            graphTemplate.append(createExpandForMetrics(groupOfGraph, groupOfGraph.getVariable(), timestamp));

        }
        template += createExpandForText(String.valueOf(graphTemplate), "Графики по результатам тестирования");

        return template;
    }

    public String createReliabilityTemplateV2(Long len, List<GraphGroup> groupOfGraphs, long timestamp, Test test) {
        String template = "<h3>Результаты теста надежности</h3>\n" + "<p>Тест надежности системы проводился на протяжении X часов. Надежность системы подтверждена на следующей нагрузке</p>\n" + "<p>Аномалий и проблем с производительностью не выявлено</p>\n" + "<p>В течении всего теста ошибок – 0%.</p>\n" + "<p>Превышений SLA по времени отклика нет</p>\n";
        template += createExpandForText(reliabilityTestType.createTableForResults(len - 1, PROFILE_FILE_PATH, test), "Результаты теста надежности");
        StringBuilder graphTemplate = new StringBuilder();

        for (GraphGroup groupOfGraph : groupOfGraphs) {
            graphTemplate.append(createExpandForMetrics(groupOfGraph, groupOfGraph.getVariable(), timestamp));

        }
        template += createExpandForText(String.valueOf(graphTemplate), "Графики по результатам тестирования");

        return template;
    }

    public String createMaxPerfTemplateV2(Long len, List<GraphGroup> groupOfGraphs, long timestamp, Test test) {
        String body = "<h3>Результаты теста поиска максимальной производительности</h3>\n" + "<p>Тест поиска максимальной производительности системы проводился на протяжении X часов. Производительность системы подтверждена на следующей нагрузке: X</p>\n" + "<p>На X% тест остановлен ввиду: причина</p>\n" + "<p>Аномалий и проблем с производительностью не выявлено</p>\n" + "<p>В течении всего теста ошибок – 0%.</p>\n" + "<p>Превышений SLA по времени отклика нет</p>\n";

        body += createExpandForText(maxPerformanceTestType.createTableForResults(len - 1, PROFILE_FILE_PATH, test), "Результаты теста поиска максимальной производительности");

        StringBuilder graphTemplate = new StringBuilder();

        for (GraphGroup groupOfGraph : groupOfGraphs) {
            graphTemplate.append(createExpandForMetrics(groupOfGraph, groupOfGraph.getVariable(), timestamp));

        }
        body += createExpandForText(String.valueOf(graphTemplate), "Графики по результатам тестирования");

        return body;
    }

    public String createConfirmMaxTemplateV2(Long len, List<GraphGroup> groupOfGraphs, long timestamp, Test test) {
        String template = "<h3>Результаты теста подтверждения максимальной производительности</h3>\n" + "<p>Тест подтверждения максимальной производительности системы проводился на протяжении X часов. Производительность системы подтверждена на следующей нагрузке: X</p>\n" + "<p>Аномалий и проблем с производительностью не выявлено</p>\n" + "<p>В течении всего теста ошибок – 0%.</p>\n" + "<p>Превышений SLA по времени отклика нет</p>\n";
        template += createExpandForText(reliabilityTestType.createTableForResults(len - 1, PROFILE_FILE_PATH, test), "Результаты теста подтверждения максимальной производительности");
        StringBuilder graphTemplate = new StringBuilder();

        for (GraphGroup groupOfGraph : groupOfGraphs) {
            graphTemplate.append(createExpandForMetrics(groupOfGraph, groupOfGraph.getVariable(), timestamp));

        }
        template += createExpandForText(String.valueOf(graphTemplate), "Графики по результатам тестирования");

        return template;
    }

}
