package reportRunner.Confluence.Templates;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import reportRunner.Config.ConfirmMaxConfig;
import reportRunner.Config.InfluxDBConfig;
import reportRunner.Config.Infrastructure.ComponentConfiguration;
import reportRunner.Config.Infrastructure.InfrastructureProperties;
import reportRunner.Config.MaxPerformanceConfig;
import reportRunner.Config.ReliabilityConfig;
import reportRunner.Config.UtilityConfig;
import reportRunner.Config.VictoriaMetricsConfig;
import reportRunner.Confluence.ResultProcessor.ReportProcessor;
import reportRunner.Confluence.Templates.Tables.MicroserviceLoader;
import reportRunner.Confluence.Templates.Tables.MicroservicesInfo;
import reportRunner.Csv.CsvUtility;
import reportRunner.FaultTolerance.FaultToleranceProperties;
import reportRunner.Service.GrafanaService.GraphGroup;
import reportRunner.Model.Test;
import reportRunner.ResultsCreator.TestTypes.MaxPerformanceTestType;
import reportRunner.ResultsCreator.TestTypes.ReliabilityTestType;
import reportRunner.Util.ProfileReader;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static reportRunner.Confluence.ReportTemplates.PROFILE_FILE_PATH;

@Component
public class ReportTemplatesV2 {
    CsvUtility csv = new CsvUtility();
    ProfileReader profileReader = new ProfileReader();
    ReliabilityTestType reliabilityTestType;
    MaxPerformanceTestType maxPerformanceTestType;
    ReportProcessor reportProcessor;
    private final InfrastructureProperties infrastructureProperties;
    private final MaxPerformanceConfig maxPerformanceConfig;
    private final ConfirmMaxConfig confirmMaxConfig;
    private final ReliabilityConfig reliabilityConfig;
    private final FaultToleranceProperties faultToleranceProperties;

    public ReportTemplatesV2(MaxPerformanceConfig maxPerformanceConfig, ConfirmMaxConfig confirmMaxConfig, ReliabilityConfig reliabilityConfig, FaultToleranceProperties faultToleranceProperties, UtilityConfig utilityConfig, InfluxDBConfig influxDBConfig, VictoriaMetricsConfig victoriaMetricsConfig, InfrastructureProperties infrastructureProperties) {
        this.maxPerformanceConfig = maxPerformanceConfig;
        this.confirmMaxConfig = confirmMaxConfig;
        this.reliabilityConfig = reliabilityConfig;
        this.faultToleranceProperties = faultToleranceProperties;
        this.infrastructureProperties = infrastructureProperties;
        this.maxPerformanceTestType = new MaxPerformanceTestType(utilityConfig, influxDBConfig,victoriaMetricsConfig);
        this.reliabilityTestType = new ReliabilityTestType(utilityConfig, influxDBConfig, victoriaMetricsConfig);
        this.reportProcessor = new ReportProcessor(reliabilityTestType,maxPerformanceTestType);
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
        infrastructureProperties.getComponents().forEach((name, component) -> {
            ComponentConfiguration config = component.getConfiguration();
            sb.append("<tr><td>").append(name).append("</td>").append(config.toHtmlRow()).append("</tr>");
            //String table = createComponentsTable(config.getCpu(), config.getRam(), config.getNodesCount());
            //sb.append(reportProcessor.createExpandForText(table, name));
        });
        sb.append("</table>");
        return sb.toString();
    }

    public String createComponentsTable(String cpu, String ram, String nodesCount){
        return "<table>"
                + "<tr><th>Количество нод</th><th>Конфигурация CPU</th><th>Конфигурация RAM</th></tr>"
                + "<tr><td>"+nodesCount+"</td><td>"+cpu+"</td><td>"+ram+"</td></tr>"
                + "</table>";
    };

    @SneakyThrows
    public String createTableForLtProfile() {
        Long len = profileReader.yamlSize(PROFILE_FILE_PATH);
        return reportProcessor.createTableinCycleForProfile(len - 1, PROFILE_FILE_PATH);
    }

    public String createLoadTestResultsV2(List<GraphGroup> groups, long timestamp) {
        return "<h2><b>4. Результаты нагрузочного тестирования</b></h2>";
    }

    @SneakyThrows
    public String createMaxPerfResults(List<GraphGroup> groups, long timestamp) {
        Long len = profileReader.yamlSize(PROFILE_FILE_PATH);
        Test maxPerf = new Test.Builder()
                .withSeparatedRamp(maxPerformanceConfig.getSeparatingRamp())
                .withRampTime(maxPerformanceConfig.getRampTime())
                .withStepPercent(maxPerformanceConfig.getStepPercent())
                .withStabilization(maxPerformanceConfig.getStabilizationTime())
                .withStartPercent(maxPerformanceConfig.getLoadLevel())
                .withStartTime(maxPerformanceConfig.getTestStartTime())
                .withEndTime(maxPerformanceConfig.getTestEndTime())
                .withStepsCount(maxPerformanceConfig.getStepsCount())
                .withStepDuration(maxPerformanceConfig.getStepDuration())
                .build();

        return reportProcessor.createMaxPerfTemplateV2(len, groups, timestamp, maxPerf);
    }

    @SneakyThrows
    public String createConfirmMaxResults(List<GraphGroup> groups, long timestamp) {
        Long len = profileReader.yamlSize(PROFILE_FILE_PATH);
        Test confirmMax = new Test.Builder()
                .withStartPercent(confirmMaxConfig.getLoadLevel())
                .withEndTime(confirmMaxConfig.getTestEndTime())
                .withStepDuration(confirmMaxConfig.getStepDuration())
                .build();

        return reportProcessor.createConfirmMaxTemplateV2(len, groups, timestamp, confirmMax);
    }

    @SneakyThrows
    public String createReliabilityResults(List<GraphGroup> groups, long timestamp) {
        Long len = profileReader.yamlSize(PROFILE_FILE_PATH);
        Test reliability = new Test.Builder()
                .withStartPercent(reliabilityConfig.getLoadLevel())
                .withEndTime(reliabilityConfig.getTestEndTime())
                .withStepDuration(reliabilityConfig.getStepDuration())
                .build();

        return reportProcessor.createReliabilityTemplateV2(len, groups, timestamp, reliability);
    }

}
