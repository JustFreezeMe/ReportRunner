package reportRunner.Confluence.Templates;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import reportRunner.Config.ConfirmMaxConfig;
import reportRunner.Config.InfluxDBConfig;
import reportRunner.Config.MaxPerformanceConfig;
import reportRunner.Config.ReliabilityConfig;
import reportRunner.Config.UtilityConfig;
import reportRunner.Config.VictoriaMetricsConfig;
import reportRunner.Csv.CsvUtility;
import reportRunner.FaultTolerance.FaultToleranceProperties;
import reportRunner.ResultsCreator.TestTypes.MaxPerformanceTestType;
import reportRunner.ResultsCreator.TestTypes.ReliabilityTestType;
import reportRunner.Util.ProfileReader;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static reportRunner.Confluence.ReportTemplates.PROFILE_FILE_PATH;

@Component
public class ConclusionTemplates {

    CsvUtility csv = new CsvUtility();
    ProfileReader profileReader = new ProfileReader();
    ReliabilityTestType reliabilityTestType;
    MaxPerformanceTestType maxPerformanceTestType;

    private final MaxPerformanceConfig maxPerformanceConfig;
    private final ConfirmMaxConfig confirmMaxConfig;
    private final ReliabilityConfig reliabilityConfig;
    private final FaultToleranceProperties faultToleranceProperties;

    public ConclusionTemplates(MaxPerformanceConfig maxPerformanceConfig, ConfirmMaxConfig confirmMaxConfig, ReliabilityConfig reliabilityConfig, FaultToleranceProperties faultToleranceProperties, UtilityConfig utilityConfig, InfluxDBConfig influxDBConfig, VictoriaMetricsConfig victoriaMetricsConfig) {
        this.maxPerformanceConfig = maxPerformanceConfig;
        this.confirmMaxConfig = confirmMaxConfig;
        this.reliabilityConfig = reliabilityConfig;
        this.faultToleranceProperties = faultToleranceProperties;
        this.maxPerformanceTestType = new MaxPerformanceTestType(utilityConfig, influxDBConfig, victoriaMetricsConfig);
        this.reliabilityTestType = new ReliabilityTestType(utilityConfig, influxDBConfig, victoriaMetricsConfig);
    }



    public String createPageHeader(String jiraTaskNumber) {
        return "<h1 style=\"text-align: center;\">Отчет по результатам нагрузочного тестирования</h1>\n" +
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
        return createTableinCycleForProfile(len - 1);
    }
    private String createHref(String pageId) {
        return "<a href=\"https://confluence.moscow.alfaintra.net/pages/viewpage.action?pageId=" + pageId + "\">" + "Ссылка на отчет по НТ</a>";
    }
    @SneakyThrows
    private String createTableinCycleForProfile(Long len) {

        List<String[]> csvData = csv.readCsv(PROFILE_FILE_PATH);
        StringBuilder sb = new StringBuilder();
        sb.append("<h2><b>7. Профиль нагрузочного тестирования</b></h2>")
                .append("<table>")
                .append("<tr><th>№</th><th>Описание сценария</th><th>Название скрипта</th><th>Целевая интенсивность, операций/час</th><th>SLA, сек</th></tr>");
        for (int i = 0; i < len; i++) {
            sb.append("<tr><td>").append(i + 1).append("</td><td>").append(csvData.get(i + 1)[0]).append("</td><td>").append(csvData.get(i + 1)[1]).append("</td><td>").append(csvData.get(i + 1)[2]).append("</td><td>").append(csvData.get(i + 1)[3]).append("</td></tr>");
        }
        sb.append("<tr><td><b>Итого</b></td><td></td><td></td><td><b>").append(csv.calculateSumOfIntensity(PROFILE_FILE_PATH)).append("</b></td><td></td></tr>")
                .append("</table>");

        return sb.toString();
    }

    //TODO: добавить обработку времени отклика
    public String createTableForTests(Map<String,String> pageIds) {
        String template = "<h2><b>8. Перечень проведенных тестов</b></h2>\n"
                + "<table>"
                + "<tr><th>Тип теста</th><th>Время теста</th><th>Описание теста</th><th>Ссылка на отчет по тесту</th></tr>";
        if (maxPerformanceConfig.getEnable())
            template += "<tr><td>Тест поиска максимальной производительности</td><td>"+calculateTestDates(maxPerformanceConfig.getTestEndTime(),maxPerformanceConfig.getStepDuration())+"</td><td>Описание теста</td><td>"+createHref(pageIds.get("Поиск максимальной производительности"))+"</td></tr>";
        if (confirmMaxConfig.getEnable())
            template += "<tr><td>Тест подтверждения максимальной производительности</td><td>"+calculateTestDates(confirmMaxConfig.getTestEndTime(),confirmMaxConfig.getStepDuration())+"</td><td>Описание теста</td><td>"+createHref(pageIds.get("Подтверждение максимальной производительности"))+"</td></tr>";
        if (reliabilityConfig.getEnable())
            template += "<tr><td>Тест надежности</td><td>"+calculateTestDates(reliabilityConfig.getTestEndTime(),reliabilityConfig.getStepDuration())+"</td><td>Описание теста</td><td>"+createHref(pageIds.get("Тест надежности"))+"</td></tr>";
        if (faultToleranceProperties.getEnable())
            template += "<tr><td>Тест отказоустойчивости</td><td>DD.MM.YYYY HH:MM-HH:MM</td><td>Описание теста</td><td>"+createHref(pageIds.get("Тест отказоустойчивости"))+"</td></tr>";

        template += "</table>";
        return template;
    }

    public String createAttachmentsBlock(){
        return "<h2><b>9. Приложение</b></h2>\n"+
                "Различные приложения типа PG_PROFILE отчета, если есть";
    }

    private String calculateTestDates(String endTime, String duration) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyy HH:mm");
        LocalDateTime end = LocalDateTime.parse(endTime,inputFormatter);
       Duration dur = Duration.ofMinutes(Long.parseLong(duration));
        LocalDateTime start = end.minus(dur);
        return start.format(outputFormatter) + " - " + end.format(outputFormatter);
    }

}
