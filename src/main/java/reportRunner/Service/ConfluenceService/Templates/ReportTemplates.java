package reportRunner.Service.ConfluenceService.Templates;

import reportRunner.Utility.ProfileReader;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import reportRunner.Config.*;
import reportRunner.Config.InfrastructureConfig.ComponentDTO;
import reportRunner.Config.InfrastructureConfig.InfrastructureConfig;
import reportRunner.Config.TestConfig.TestConfig;
import reportRunner.Model.Test;
import reportRunner.Service.ConfluenceService.ResultProcessor.ReportProcessor;
import reportRunner.Service.ConfluenceService.Templates.Tables.MicroserviceLoader;
import reportRunner.Service.ConfluenceService.Templates.Tables.MicroservicesInfo;
import reportRunner.Config.FaultToleranceConfig;
import reportRunner.Service.GrafanaService.GraphGroup;
import reportRunner.Service.ResultsService.impl.MaxPerformanceResultService;
import reportRunner.Service.ResultsService.impl.ReliabilityResultService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportTemplates {
    ProfileReader profileReader = new ProfileReader();
    public static final String PROFILE_FILE_PATH = "src/main/resources/profile.yaml";
    ReliabilityResultService reliabilityResultService;
    MaxPerformanceResultService maxPerformanceResultService;
    ReportProcessor reportProcessor;
    private final InfrastructureConfig infrastructureConfig;

    public ReportTemplates(FaultToleranceConfig faultToleranceConfig, UtilityConfig utilityConfig, InfluxDBConfig influxDBConfig, VictoriaMetricsConfig victoriaMetricsConfig, InfrastructureConfig infrastructureConfig) {
        this.infrastructureConfig = infrastructureConfig;
        this.maxPerformanceResultService = new MaxPerformanceResultService(utilityConfig, influxDBConfig,victoriaMetricsConfig);
        this.reliabilityResultService = new ReliabilityResultService(utilityConfig, influxDBConfig, victoriaMetricsConfig);
        this.reportProcessor = new ReportProcessor(reliabilityResultService, maxPerformanceResultService);
    }

    public String tableOfContents() {
        return "<ac:structured-macro ac:name=\"toc\"/>";
    }

    public String createJiraTaskBlock(String taskId) {
        return "<h2><b>1. Задача на проведение нагрузочного тестирования</b></h2>\n" +
                "<ac:structured-macro ac:name=\"jira\"><ac:parameter ac:name=\"key\">" + taskId + "</ac:parameter></ac:structured-macro>";
    }

    public String createMethodologyBlock() throws IOException {
        String header = "<h2><b>2. Краткая методика тестирования</b></h2>";
        return header + createMicroservicesBlock() + createInfrastructureBlock();
    }

    public String createMicroservicesBlock() throws IOException {

        List<MicroservicesInfo> services = MicroserviceLoader.loadFromYaml("src/main/resources/tableTemplates/microservices.yaml");

        StringBuilder table = new StringBuilder();
        table.append("<h3><b>Микросервисный слой</b></h3>");
        table.append("<table>");
        table.append("<tr><th>Сервис</th><th>Версия</th><th>app CPU requests/limits</th><th>app RAM requests/limits</th><th>Конфигурация JVM</th></tr>");

        String rows = services.stream().map(MicroservicesInfo::toHtmlRow).collect(Collectors.joining());
        table.append(rows);
        table.append("</table>");
        return table.toString();
    }

    public String createInfrastructureBlock() {
        StringBuilder sb = new StringBuilder("<h3><b>Инфраструктура</b></h3>");
        sb.append("<table>");
        sb.append("<tr><th>Компонент</th><th>Количество нод</th><th>Количество CPU ядер</th><th>Количество RAM, Гб</th></tr>");
        infrastructureConfig.getComponents().forEach((name, configuration) -> {
            ComponentDTO config = configuration.getConfiguration();
            sb.append("<tr><td>").append(name).append("</td>").append(config.toHtmlRow()).append("</tr>");
            //String table = createComponentsTable(config.getCpu(), config.getRam(), config.getNodesCount());
            //sb.append(reportProcessor.createExpandForText(table, name));
        });
        sb.append("</table>");
        return sb.toString();
    }

    @SneakyThrows
    public String createTableForLtProfile() {
        Long len = profileReader.yamlSize(PROFILE_FILE_PATH);
        return reportProcessor.createTableinCycleForProfile(len - 1, "src/main/resources/profile.csv");
    }

    public String createLoadTestResultsV2() {
        return "<h2><b>4. Результаты нагрузочного тестирования</b></h2>";
    }

//    @SneakyThrows
//    public String createMaxPerfResults(List<GraphGroup> groups, long timestamp) {
//        Long len = csv.csvSize("src/main/resources/profile.csv");
//        Test maxPerf = new Test.Builder()
//                .withSeparatedRamp(maxPerformanceConfig.getSeparatingRamp())
//                .withRampTime(maxPerformanceConfig.getRampTime())
//                .withStepPercent(maxPerformanceConfig.getStepPercent())
//                .withStabilization(maxPerformanceConfig.getStabilizationTime())
//                .withStartPercent(maxPerformanceConfig.getLoadLevel())
//                .withStartTime(maxPerformanceConfig.getTestStartTime())
//                .withEndTime(maxPerformanceConfig.getTestEndTime())
//                .withStepsCount(maxPerformanceConfig.getStepsCount())
//                .withStepDuration(maxPerformanceConfig.getStepDuration())
//                .build();
//
//        return reportProcessor.createMaxPerfTemplateV2(len, groups, timestamp, maxPerf);
//    }

    @SneakyThrows
    public String createMaxPerfResultsV2(List<GraphGroup> groups, TestConfig test) {
        Long len = profileReader.yamlSize(PROFILE_FILE_PATH);
        Long timestamp = test.getEndTimestamp();
        Test maxPerf = new Test.Builder()
                .withSeparatedRamp(test.getSeparatingRamp())
                .withRampTime(test.getRampTime())
                .withStepPercent(test.getStepPercent())
                .withStabilization(test.getStabilizationTime())
                .withStartPercent(test.getLoadLevel())
                .withStartTime(test.getTestStartTime())
                .withEndTime(test.getTestEndTime())
                .withStepsCount(test.getStepsCount())
                .withStepDuration(test.getStepDuration())
                .build();

        return reportProcessor.createMaxPerfTemplate(len, groups, timestamp, maxPerf);
    }

    @SneakyThrows
    public String createConfirmMaxResults(List<GraphGroup> groups, TestConfig test) {
        Long len = profileReader.yamlSize(PROFILE_FILE_PATH);
        Test confirmMax = new Test.Builder()
                .withStartPercent(test.getLoadLevel())
                .withEndTime(test.getTestEndTime())
                .withStepDuration(test.getStepDuration())
                .build();

        return reportProcessor.createConfirmMaxTemplate(len, groups, test.getEndTimestamp(), confirmMax);
    }

    @SneakyThrows
    public String createReliabilityResultsV2(List<GraphGroup> groups,TestConfig test) {
        Long len = profileReader.yamlSize(PROFILE_FILE_PATH);
        Test reliability = new Test.Builder()
                .withStartPercent(test.getLoadLevel())
                .withEndTime(test.getTestEndTime())
                .withStepDuration(test.getStepDuration())
                .build();

        return reportProcessor.createReliabilityTemplate(len, groups, test.getEndTimestamp(), reliability);
    }

}
