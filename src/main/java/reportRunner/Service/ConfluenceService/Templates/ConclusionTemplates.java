package reportRunner.Service.ConfluenceService.Templates;

import reportRunner.Utility.ProfileReader;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import reportRunner.Config.*;
import reportRunner.Config.TestConfig.TestConfig;
import reportRunner.Config.FaultToleranceConfig;
import reportRunner.Service.ResultsService.impl.MaxPerformanceResultService;
import reportRunner.Service.ResultsService.impl.ReliabilityResultService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class ConclusionTemplates {

    ProfileReader profileReader = new ProfileReader();
    ReliabilityResultService reliabilityResultService;
    MaxPerformanceResultService maxPerformanceResultService;

    private final FaultToleranceConfig faultToleranceConfig;
    public static final String PROFILE_FILE_PATH = "src/main/resources/profile.yaml";
    public ConclusionTemplates(FaultToleranceConfig faultToleranceConfig, UtilityConfig utilityConfig, InfluxDBConfig influxDBConfig, VictoriaMetricsConfig victoriaMetricsConfig) {
        this.faultToleranceConfig = faultToleranceConfig;
        this.maxPerformanceResultService = new MaxPerformanceResultService(utilityConfig, influxDBConfig, victoriaMetricsConfig);
        this.reliabilityResultService = new ReliabilityResultService(utilityConfig, influxDBConfig, victoriaMetricsConfig);
    }


    public String createPageHeader(String jiraTaskNumber) {
        return "<h1 style=\"text-align: center;\">Заключение по результатам нагрузочного тестирования</h1>\n" +
                "<h1 style=\"text-align: center;\">ПК Интернет-анкеты</h1>\n" +
                "<h1 style=\"text-align: center;\">В рамках задач: " + jiraTaskNumber + "</h1>\n" +
                "<br /><br />\n" +
                "<h1 style=\"text-align: center;\">Москва 2025</h1>";
    }

    public String tableOfContents() {
        return "<ac:structured-macro ac:name=\"toc\">" +
                "<ac:parameter ac:name=\"minLevel\">2</ac:parameter>" +
                "</ac:structured-macro>";
    }

    public String createJiraTaskBlock(String taskId) {
        return "<h2><b>1. Задачи на проведение нагрузочного тестирования</b></h2>\n" +
                "<ac:structured-macro ac:name=\"jira\"><ac:parameter ac:name=\"key\">" + taskId + "</ac:parameter></ac:structured-macro>";

    }

    public String createLoadTestTargets() {

        return "<h2><b>2. Цели нагрузочного тестирования</b></h2>" + "<ul>" +
                "<p>Тестирование проводится с целью (напишите цель тестирования)</p>" +
                "<li>Определить максимальную производительность тестируемых компонентов</li>" +
                "<li>Подобрать оптимальную конфигурацию, обеспечивающую стабильную работу сервиса при нагрузке, эквивалентной 200% от текущей промышленной</li>" +
                "<li>Регрессионное тестирование версии X относительно текущей промышленной</li>" +
                "<li>Подтвердить способность тестируемых компонентов стабильно функционировать на протяжении X часов</li>" +
                "<li>Подтвердить способность тестируемых компонентов стабильно функционировать при отключении компонента X (кэш, заглушка, ещё что-нибудь)</li>" +
                "</ul>";
    }

    public String createTableForLtCriteria() {
        return "<h2><b>3. Заключение по результатам нагрузочного тестирования</b></h2>\n"
                + "<table>"
                + "<tr><th>№</th><th>Критерии качества прохождения НТ</th><th>Результат</th></tr>"
                + "<tr><td>1</td><td>Блокирующие и важные дефекты производительности отсутствуют</td><td>Отсутствуют</td></tr>"
                + "<tr><td>2</td><td>Запланированный объем регламентных тестов НТ выполнен</td><td>Выполнен</td></tr>"
                + "<tr><td>3</td><td>Профили нагрузки при проведении НТ соответствовали запланированным</td><td>Соответствовали</td></tr>"
                + "<tr><td>4</td><td>Бизнес-характеристики соответствуют согласованным требованиям</td><td>Соответствуют</td></tr>"
                + "<tr><td>5</td><td>Технические характеристики соответствуют согласованным требованиям</td><td>Соответствуют</td></tr>"
                + "<tr><td>6</td><td>Улучшение/ухудшение работы системы</td><td>Без изменений</td></tr>"
                + "<tr><td></td><td><b>Внедрение в части производительности</b></td><td><b>Рекомендовано</b></td></tr>"
                + "</table>"
                + "<h3>Выводы по результатам тестирования:</h3>\n<ol>\n"
                + "<li>Вывод раз</li>\n</ol>";
    }

    public String createLoadTestRecomendations() {
        return "<h2><b>4. Рекомендации по итогам нагрузочного тестирования</b></h2>\n<ol>\n" +
                " <li>рекомендация раз</li>\n" +
                " <li>Рекомендация два</li>\n" +
                "  </ol>";
    }

    public String createPerformanceBugs() {
        return "<h2><b>5. Перечень зарегистрированных дефектов и оптимизаций во время тестирования</b></h2>\n" +
                "<h3>Дефекты:</h3>" +
                "<ol>\n" +
                " <li>дефект раз</li>\n" +
                "  </ol>" +
                "<h3>Оптимизации:</h3>" +
                "<ol>\n" +
                " <li>Оптимизация раз</li>\n" +
                "  </ol>";
    }

    public String createLoadTestDeviations() {
        return "<h2><b>6. Ограничения нагрузочного тестирования</b></h2>\n" +
                "<p>Отступлений нет</p>";
    }

    @SneakyThrows
    public String createTableForLtProfile() {
        Long len = profileReader.yamlSize(PROFILE_FILE_PATH);
        return createTableinCycleForProfile(len - 1, PROFILE_FILE_PATH);
    }

    private String createHref(String pageId) {
        return "<a href=\"https://confluence.moscow.alfaintra.net/pages/viewpage.action?pageId=" + pageId + "\">" + "Ссылка на отчет по НТ</a>";
    }

    @SneakyThrows
    public String createTableinCycleForProfile(Long len,String path) {

        ProfileConfig config = profileReader.readYaml(path);
        List<ProfileConfig.Script> profile = config.getProfile();
        StringBuilder sb = new StringBuilder();
        sb.append("<h2><b>8. Профиль нагрузочного тестирования</b></h2>")
                .append("<table>")
                .append("<tr><th>№</th><th>Описание сценария</th><th>Название скрипта</th><th>Целевая интенсивность, операций/час</th><th>SLA, сек</th></tr>");
        for (int i = 0; i < len; i++) {

            ProfileConfig.Script script = profile.get(i);

            sb.append("<tr><td>").append(i + 1).append("</td><td>").append(script.getScenarioName()).append("</td><td>").append(script.getScriptName()).append("</td><td>").append(script.getIntensity()).append("</td><td>").append(script.getSla()).append("</td></tr>");
        }
        sb.append("<tr><td><b>Итого</b></td><td></td><td></td><td><b>").append(profileReader.totalIntensity(path)).append("</b></td><td></td></tr>")
                .append("</table>");

        return sb.toString();
    }

    //TODO: добавить обработку времени отклика
    public String createTableForTests(Map<String, String> pageIds, TestConfig test) {
        return "<h2><b>8. Перечень проведенных тестов</b></h2>\n"
                + "<table>"
                + "<tr><th>Тип теста</th><th>Время теста</th><th>Описание теста</th><th>Ссылка на отчет по тесту</th></tr>"
                + "<tr><td>" + test.getTestType() + "</td><td>" + calculateTestDates(test.getTestEndTime(), test.getStepDuration()) + "</td><td>Описание теста</td><td>" + createHref(pageIds.get(test.getTestType())) + "</td></tr>"
                + "</table>";
    }

    public String createAttachmentsBlock() {
        return "<h2><b>9. Приложение</b></h2>\n" +
                "Различные приложения типа PG_PROFILE отчета, если есть";
    }

    private String calculateTestDates(String endTime, String duration) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyy HH:mm");
        LocalDateTime end = LocalDateTime.parse(endTime, inputFormatter);
        Duration dur = Duration.ofMinutes(Long.parseLong(duration));
        LocalDateTime start = end.minus(dur);
        return start.format(outputFormatter) + " - " + end.format(outputFormatter);
    }

}
