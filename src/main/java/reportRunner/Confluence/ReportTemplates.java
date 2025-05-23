package reportRunner.Confluence;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import reportRunner.Config.ConfirmMaxConfig;
import reportRunner.Config.InfluxDBConfig;
import reportRunner.Config.MaxPerformanceConfig;
import reportRunner.Config.ReliabilityConfig;
import reportRunner.Config.UtilityConfig;
import reportRunner.Config.VictoriaMetricsConfig;
import reportRunner.Csv.CsvUtility;
import reportRunner.FaultTolerance.FaultToleranceProperties;
import reportRunner.FaultTolerance.FaultToleranceScenario;
import reportRunner.Grafana.GraphGroup;
import reportRunner.Model.Test;
import reportRunner.Results.TestTypes.MaxPerformanceTestType;
import reportRunner.Results.TestTypes.ReliabilityTestType;

import java.util.List;

@Service
public class ReportTemplates {

    CsvUtility csv = new CsvUtility();
    ReliabilityTestType reliabilityTestType;
    MaxPerformanceTestType maxPerformanceTestType;

    private final MaxPerformanceConfig maxPerformanceConfig;
    private final ConfirmMaxConfig confirmMaxConfig;
    private final ReliabilityConfig reliabilityConfig;
    private final FaultToleranceProperties faultToleranceProperties;

    public ReportTemplates(MaxPerformanceConfig maxPerformanceConfig, ConfirmMaxConfig confirmMaxConfig, ReliabilityConfig reliabilityConfig, FaultToleranceProperties faultToleranceProperties, UtilityConfig utilityConfig, InfluxDBConfig influxDBConfig, VictoriaMetricsConfig victoriaMetricsConfig) {
        this.maxPerformanceConfig = maxPerformanceConfig;
        this.confirmMaxConfig = confirmMaxConfig;
        this.reliabilityConfig = reliabilityConfig;
        this.faultToleranceProperties = faultToleranceProperties;
        this.maxPerformanceTestType = new MaxPerformanceTestType(utilityConfig, influxDBConfig,victoriaMetricsConfig);
        this.reliabilityTestType = new ReliabilityTestType(utilityConfig, influxDBConfig,victoriaMetricsConfig);
    }

    public String createExpandForText(String body, String title) {

        return "<ac:structured-macro ac:name=\"expand\">\n" +
                "  <ac:parameter ac:name=\"title\">" + title + "</ac:parameter>\n" +
                "  <ac:rich-text-body>\n" +
                "    \n" +
                "<p>" + body + "</p>\n" +
                "  </ac:rich-text-body>\n" +
                "</ac:structured-macro>";
    }

    public String createTableForLtCriteria() {
        return "<h2><b>2. Критерии качества прохождения нагрузочного тестирования</b></h2>\n"
                + "<table>"
                + "<tr><th>№</th><th>Критерии качества прохождения НТ</th><th>Результат</th></tr>"
                + "<tr><td>1</td><td>Блокирующие и важные дефекты производительности отсутствуют</td><td>Отсутствуют</td></tr>"
                + "<tr><td>2</td><td>Запланированный объем регламентных тестов НТ выполнен</td><td>Выполнен</td></tr>"
                + "<tr><td>3</td><td>Профили нагрузки при проведении НТ соответствовали запланированным</td><td>Соответствовали</td></tr>"
                + "<tr><td>4</td><td>Бизнес-характеристики соответствуют согласованным требованиям</td><td>Соответствуют</td></tr>"
                + "<tr><td>5</td><td>Технические характеристики соответствуют согласованным требованиям</td><td>Соответствуют</td></tr>"
                + "<tr><td>6</td><td>Улучшение/ухудшение работы системы</td><td>Без изменений</td></tr>"
                + "<tr><td></td><td><b>Внедрение в части производительности</b></td><td><b>Рекомендовано</b></td></tr>"
                + "</table>";
    }

    @SneakyThrows
    public String createTableForLtProfile() {
        Long len = csv.csvSize("src/main/resources/profile.csv");
        return createTableinCycleForProfile(len - 1, "src/main/resources/profile.csv");
    }

    public String createLoadTestTargets() {

        StringBuilder sb = new StringBuilder("<h2><b>3. Цели нагрузочного тестирования</b></h2>").append("<ul>");
        sb.append("<p>Тестирование проводится с целью (напишите цель тестирования)</p>");
        if (reliabilityConfig.getEnable())
            sb.append("<li>Тест надёжности - по результатам тестирования определяется возможность системы работать длительное время под нагрузкой</li>");
        if (confirmMaxConfig.getEnable())
            sb.append("<li>Тест подтверждения максимальной производительности - повторный тест на ступени длительностью 1 час для проверки результата теста максимальной производительности</li>");
        if (maxPerformanceConfig.getEnable())
            sb.append("<li>Тест максимальной производительности - по результатам тестирования определяется предельная производительность системы, после которой начинаются деградации системы</li>");
        if (faultToleranceProperties.getEnable())
            sb.append("<li>Тест отказоустойчивости при отключении внешних систем - по результатам тестирования определяется фактическое поведение системы под нагрузкой в случае отключения интеграций</li>");
        sb.append("</ul>");

        return sb.toString();
    }

    public String createConfirmMaxTemplateV2(Long len, List<GraphGroup> groupOfGraphs, long timestamp, Test test) {
        String template = "<h3>Результаты теста подтверждения максимальной производительности</h3>\n" +
                "<p>Тест подтверждения максимальной производительности системы проводился на протяжении X часов. Производительность системы подтверждена на следующей нагрузке: X</p>\n" +
                "<p>Аномалий и проблем с производительностью не выявлено</p>\n" +
                "<p>В течении всего теста ошибок – 0%.</p>\n" +
                "<p>Превышений SLA по времени отклика нет</p>\n";
        template += createExpandForText(reliabilityTestType.createTableForResults(len - 1, "src/main/resources/profile.csv", test)
                , "Результаты теста подтверждения максимальной производительности");
        StringBuilder graphTemplate = new StringBuilder();
        //template += createExpandForText(createTableForResults(), "Результаты теста надежности");

        for (GraphGroup groupOfGraph : groupOfGraphs) {
            graphTemplate.append(createExpandForMetrics(groupOfGraph, groupOfGraph.getVariable(), timestamp));

        }
        template += createExpandForText(String.valueOf(graphTemplate), "Графики по результатам тестирования");

        return template;
    }

    public String createFaultToleranceResults() {
        return "<h2><b>3. Результаты нагрузочного тестирования</b></h2>";
    }

    public String createLoadTestResultsV2(List<GraphGroup> groups, long timestamp) {
        return "<h2><b>9. Результаты нагрузочного тестирования</b></h2>";
    }

    @SneakyThrows
    public String createMaxPerfResults(List<GraphGroup> groups, long timestamp) {
        Long len = csv.csvSize("src/main/resources/profile.csv");
        Test maxPerf = new Test.Builder()
                .withSeparatedRamp(maxPerformanceConfig.getSeparatingRamp())
                .withRampTime(maxPerformanceConfig.getRampTime())
                .withStepPercent(maxPerformanceConfig.getStepPercent())
                .withStabilization(maxPerformanceConfig.getStabilizationTime())
                .withStartPercent(maxPerformanceConfig.getLoadLevel())
                .withStartTime(maxPerformanceConfig.getTestStartTime())
                .withStepsCount(maxPerformanceConfig.getStepsCount())
                .withStepDuration(maxPerformanceConfig.getStepDuration())
                .build();

        return createMaxPerfTemplateV2(len, groups, timestamp, maxPerf);
    }

    @SneakyThrows
    public String createConfirmMaxResults(List<GraphGroup> groups, long timestamp) {
        Long len = csv.csvSize("src/main/resources/profile.csv");
        Test confirmMax = new Test.Builder()
                .withStartPercent(confirmMaxConfig.getLoadLevel())
                .withEndTime(confirmMaxConfig.getTestEndTime())
                .withStepDuration(confirmMaxConfig.getStepDuration())
                .build();

        return createConfirmMaxTemplateV2(len, groups, timestamp, confirmMax);
    }

    @SneakyThrows
    public String createReliabilityResults(List<GraphGroup> groups, long timestamp) {
        Long len = csv.csvSize("src/main/resources/profile.csv");
        Test reliability = new Test.Builder()
                .withStartPercent(reliabilityConfig.getLoadLevel())
                .withEndTime(reliabilityConfig.getTestEndTime())
                .withStepDuration(reliabilityConfig.getStepDuration())
                .build();

        return createReliabilityTemplateV2(len, groups, timestamp, reliability);
    }

    @SneakyThrows
    public String createFaulToleranceResults(List<GraphGroup> groups, long timestamp, FaultToleranceScenario scenario) {
        Long len = csv.csvSize("src/main/resources/profile.csv");
        Test test = new Test.Builder()
                .withStartPercent(faultToleranceProperties.getLoadLevel())
                .withEndTime(scenario.getFaultScenarioTestEnd())
                .withStepDuration(scenario.getFaultScenarioTestLength())
                .build();

        return createFaultToleranceTemplate(len, groups, timestamp, test);
    }

    public String createLoadTestDeviations() {
        return "<h2><b>7. Отступления от методики нагрузочного тестирования</b></h2>\n" +
                "<p>Отступлений нет</p>";
    }

    public String createLoadTestRestrictions() {
        return "<h2><b>6. Ограничения нагрузочного тестирования</b></h2>\n" +
                "<ul>\n" +
                " <li>Смежные системы заменены заглушками</li>\n" +
                " <li>(допиши сам)</li>\n" +
                "</ul>";
    }

    public String createPageHeader(String jiraTaskNumber) {
        return "<h1 style=\"text-align: center;\">Отчет по результатам нагрузочного тестирования</h1>\n" +
                "<h2 style=\"text-align: center;\">ПК Интернет-анкеты</h2>\n" +
                "<h2 style=\"text-align: center;\">В рамках задач: " + jiraTaskNumber + "</h2>\n" +
                "<br /><br />\n" +
                "<h3 style=\"text-align: center;\">Москва 2025</h3>";

    }

    public String createLoadTestRecomendations() {
        return "<h2><b>4. Рекомендации по итогам нагрузочного тестирования</b></h2>\n<ol>\n" +
                " <li>рекомендация раз</li>\n" +
                " <li>Рекомендация два</li>\n" +
                "  </ol>";
    }

    public String createJiraTaskBlock(String taskId) {
        return "<h2><b>1. Задачи на проведение нагрузочного тестирования</b></h2>\n" +
                "<ac:structured-macro ac:name=\"jira\"><ac:parameter ac:name=\"key\">" + taskId + "</ac:parameter></ac:structured-macro>";

    }

    public String createPerformanceBugs() {
        return "<h2><b>5. Перечень зарегистрированных дефектов во время тестирования</b></h2>\n" +
                "<ol>\n" +
                " <li>дефект раз</li>\n" +
                " <li>дефект два</li>\n" +
                "  </ol>";
    }


    public String createImageMacro(String filename) {
        return "<ac:image>\n" +
                "<ri:attachment ri:filename=\n" +
                "\"" + filename + ".png\" />\n" +
                "</ac:image>";
    }

    @SneakyThrows
    public String createTableinCycleForProfile(Long len, String path) {

        List<String[]> csvData = csv.readCsv(path);
        StringBuilder sb = new StringBuilder();
        sb.append("<h2><b>8. Профиль нагрузочного тестирования</b></h2>")
                .append("<table>")
                .append("<tr><th>№</th><th>Описание сценария</th><th>Название скрипта</th><th>Целевая интенсивность, операций/час</th><th>SLA, сек</th></tr>");
        for (int i = 0; i < len; i++) {
            sb.append("<tr><td>").append(i + 1).append("</td><td>").append(csvData.get(i + 1)[0]).append("</td><td>").append(csvData.get(i + 1)[1]).append("</td><td>").append(csvData.get(i + 1)[2]).append("</td><td>").append(csvData.get(i + 1)[3]).append("</td></tr>");
        }
        sb.append("<tr><td><b>Итого</b></td><td></td><td></td><td><b>").append(csv.calculateSumOfIntensity(path)).append("</b></td><td></td></tr>")
                .append("</table>");

        return sb.toString();
    }

    public String createExpandForMetrics(GraphGroup graphGroup, String application, long timestamp) {

        int size = graphGroup.getPanels().size();

        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < size; i++) {
            pattern.append("<p><b>").append(graphGroup.getPanels().get(i).getPanelName()).append("</b></p>");
            pattern.append(createImageMacro(graphGroup.getPanels().get(i).getPanelName() + "_" + timestamp));
        }
        return "<ac:structured-macro ac:name=\"expand\">\n" +
                "  <ac:parameter ac:name=\"title\">" + graphGroup.getTitle() + " " + application.replace(":", "_") + "</ac:parameter>\n" +
                "  <ac:rich-text-body>\n" +
                "    \n" +
                "<p>" + pattern + "</p>\n" +
                "  </ac:rich-text-body>\n" +
                "</ac:structured-macro>";
    }

    public String createFaultToleranceTemplate(Long len, List<GraphGroup> groupOfGraphs, long timestamp, Test test) {
        String template = "<h3>Результаты теста отказоустойчивости</h3>\n";

        template += createExpandForText(reliabilityTestType.createTableForResults(len - 1, "src/main/resources/profile.csv", test)
                , "Результаты теста надежности");
        StringBuilder graphTemplate = new StringBuilder();

        for (GraphGroup groupOfGraph : groupOfGraphs) {
            graphTemplate.append(createExpandForMetrics(groupOfGraph, groupOfGraph.getVariable(), timestamp));

        }
        template += createExpandForText(String.valueOf(graphTemplate), "Графики по результатам тестирования");

        return template;
    }

    public String createReliabilityTemplateV2(Long len, List<GraphGroup> groupOfGraphs, long timestamp, Test test) {
        String template = "<h3>Результаты теста надежности</h3>\n" +
                "<p>Тест надежности системы проводился на протяжении X часов. Надежность системы подтверждена на следующей нагрузке</p>\n" +
                "<p>Аномалий и проблем с производительностью не выявлено</p>\n" +
                "<p>В течении всего теста ошибок – 0%.</p>\n" +
                "<p>Превышений SLA по времени отклика нет</p>\n";
        template += createExpandForText(reliabilityTestType.createTableForResults(len - 1, "src/main/resources/profile.csv", test)
                , "Результаты теста надежности");
        StringBuilder graphTemplate = new StringBuilder();

        for (GraphGroup groupOfGraph : groupOfGraphs) {
            graphTemplate.append(createExpandForMetrics(groupOfGraph, groupOfGraph.getVariable(), timestamp));

        }
        template += createExpandForText(String.valueOf(graphTemplate), "Графики по результатам тестирования");

        return template;
    }

    public String createMaxPerfTemplateV2(Long len, List<GraphGroup> groupOfGraphs, long timestamp, Test test) {
        String body = "<h3>Результаты теста поиска максимальной производительности</h3>\n" +
                "<p>Тест поиска максимальной производительности системы проводился на протяжении X часов. Производительность системы подтверждена на следующей нагрузке: X</p>\n" +
                "<p>На X% тест остановлен ввиду: причина</p>\n" +
                "<p>Аномалий и проблем с производительностью не выявлено</p>\n" +
                "<p>В течении всего теста ошибок – 0%.</p>\n" +
                "<p>Превышений SLA по времени отклика нет</p>\n";

        body += createExpandForText(maxPerformanceTestType.createTableForResults(len - 1, "src/main/resources/profile.csv", test)
                , "Результаты теста поиска максимальной производительности");

        StringBuilder graphTemplate = new StringBuilder();

        for (GraphGroup groupOfGraph : groupOfGraphs) {
            graphTemplate.append(createExpandForMetrics(groupOfGraph, groupOfGraph.getVariable(), timestamp));

        }
        body += createExpandForText(String.valueOf(graphTemplate), "Графики по результатам тестирования");

        return body;
    }

    public String createFaultToleranceBaseInfo(FaultToleranceScenario scenario) {

        return "<h2><b>1. Информация по тесту отказоустойчивости</b></h2>\n" +
                "<p>Отключаемая интеграция: " + scenario.getFaultScenarioTestName() + "</p>\n" +
                "<p><b>Время отключения:</b></p>\n" +
                "<p>Начало отключения: " + scenario.getFaultScenarioStartTime(scenario.getFaultScenarioTestEnd(), scenario.getFaultScenarioTestLength()) + "</p>\n" +
                "<p>Конец отключения: " + scenario.getFaultScenarioTestEnd() + "</p>\n";
    }

    public String createFaultToleranceDescribeTemplate() {
        return "<h2><b>2. Описание воздействия на систему</b></h2>\n" +
                "<p>Ожидаемый результат: (текст)</p>\n" +
                "<p>Фактический результат: (текст)</p>\n" +
                "<p>Описание воздействия на систему: (текст)</p>";
    }

    public String createFaultToleranceMainPageResults() {
        return "<h2><b>10. Результаты отключений</b></h2>" +
                "<p>(Вставьте таблицу с отключениями)</p>";
    }

    private String createHref(String pageId) {
        return "<a href=\"https://confluence.moscow.alfaintra.net/pages/viewpage.action?pageId=" + pageId + "\">" + "протокол</a>";
    }

    public String createHrefBlock(String integrationName, String href) {
        return "<p><b>Отключаемая интеграция: " + integrationName + "</b> Ссылка на результаты тестирования: " + createHref(href) + "</p>";
    }

    public String createFaultToleranceListTemplate(List<FaultToleranceScenario> scenarios) {
        StringBuilder template = new StringBuilder();
        template.append("<h2><b>9. Ссылки на протоколы с результатами тестирования</b></h2>");
        for (FaultToleranceScenario scenario : scenarios) {
            template.append(createHrefBlock(scenario.getFaultScenarioTestName(), scenario.getConfluenceChildredPageId()));
        }
        return template.toString();
    }
}
