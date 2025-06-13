package reportRunner.Service.ResultsService.impl;

import reportRunner.Config.ProfileConfig;
import reportRunner.Utility.ProfileReader;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;
import reportRunner.Config.InfluxDBConfig;
import reportRunner.Config.UtilityConfig;
import reportRunner.Config.VictoriaMetricsConfig;
import reportRunner.Model.Test;
import reportRunner.Model.TestResults;
import reportRunner.Service.ResultsService.ResultsService;
import reportRunner.Service.TimeSeriesDatabaseService.InfluxDB.InfluxService;
import reportRunner.Service.TimeSeriesDatabaseService.VictoriaMetrics.VictoriaMetricsService;
import reportRunner.Utility.ReportUtility;
import reportRunner.Service.TimeSeriesDatabaseService.TimeSeriesDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReliabilityResultService implements ResultsService {

    ProfileReader profileReader = new ProfileReader();
    ReportUtility testUtility = new ReportUtility();
    private final UtilityConfig utilityConfig;
    private final InfluxDBConfig influxDBConfig;
    private final VictoriaMetricsConfig victoriaMetricsConfig;

    public ReliabilityResultService(UtilityConfig utilityConfig, InfluxDBConfig influxDBConfig, VictoriaMetricsConfig victoriaMetricsConfig) {
        this.utilityConfig = utilityConfig;
        this.influxDBConfig = influxDBConfig;
        this.victoriaMetricsConfig = victoriaMetricsConfig;
    }

    @SneakyThrows
    @Override
    public String createTableForResults(Long len, String path, @NotNull Test testExemplar) {
        TestResults test = new TestResults();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, LocalDateTime> times = testUtility.calculateTimestampsForGatling(testExemplar.getEndTime(), testExemplar.getStepDuration());
        Map<String, String> timestamps = testUtility.convertToTimestampInflux(times.get("startTime"), times.get("endTime"));

        TimeSeriesDatabase model = null;
        if (StringUtils.hasText(influxDBConfig.getInfluxdbUrl())) {
            model = new InfluxService(timestamps.get("startTimestamp"), timestamps.get("endTimestamp"), influxDBConfig, utilityConfig);
        } else if (StringUtils.hasText(victoriaMetricsConfig.getVmUrl())) {
            model = new VictoriaMetricsService(timestamps.get("startTimestamp"), timestamps.get("endTimestamp"),victoriaMetricsConfig);
        }

        test.setMultiplier(Double.parseDouble(testExemplar.getStartPercent()) / 100);

        StringBuilder sb = new StringBuilder();

        sb.append("<h2><b>Результаты нагрузочного тестирования</b></h2>")
                .append("<table>")
                .append("<tr>" + "<th colspan=\"14\" style=\"text-align: center;\">Уровень нагрузки от профиля: ").append(testExemplar.getStartPercent())
                .append("%, время ступени: ").append(times.get("startTime").format(formatter))
                .append(" - ").append(times.get("endTime").format(formatter))
                .append("; Длительность ступени: ").append(testUtility.calculateTestDuration(times.get("startTime"), times.get("endTime"))).append(" минут")
                .append(" </th>").append("</tr>")
                .append("<tr>" + "<th colspan=\"7\" style=\"text-align: center;\">Статистика по запросам</th><th colspan=\"7\" style=\"text-align: center;\">Статистика по временам отклика</th></tr>")
                .append("<tr><th>№</th><th>Описание сценария</th><th>Название скрипта</th><th>Целевая интенсивность, операций/час</th><th>Количество успешных операций</th><th>Количество неуспешных операций</th><th>Точность</th><th>90 перцентиль времени отклика, ms</th><th>SLA по времени отклика, сек</th><th>Avg время отклика</th><th>Min время отклика</th><th>Max время отклика</th><th>СКО времени отклика</th><th>CV времени отклика</th></tr>");

        sb.append(createResultsAppend(test, model, len, path, testExemplar.getStepDuration()));

        Integer profileIntensityDeviated = (int) (testUtility.calculateProfileDeviation(
                profileReader.totalIntensity(path),
                testExemplar.getStepDuration()) * test.getMultiplier()
        );
        String totalAccuracy = testUtility.calculateAccuracy(String.valueOf(profileIntensityDeviated), String.valueOf(test.getTotalOkValue()));

        sb.append("<tr><td></td><td><b>Итого</b></td><td></td><td>").append(profileIntensityDeviated)
                .append("</td><td>").append(test.getTotalOkValue())
                .append("</td><td>").append(test.getTotalKoValue())
                .append("</td><td>").append(totalAccuracy)
                .append("</td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>")
                .append("</table>");

        return sb.toString();
    }

    @SneakyThrows
    @Override
    public String createResultsAppend(TestResults test, TimeSeriesDatabase model, Long len, String path, String duration) {
        ProfileReader profileReader = new ProfileReader();
        ProfileConfig config = profileReader.readYaml(path);
        List<ProfileConfig.Script> profile = config.getProfile();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {

            ProfileConfig.Script script = profile.get(i);
            String transactionDescribe = script.getScenarioName();
            String transactionName = script.getScriptName();
            String profileData = script.getIntensity();
            String sla = script.getSla();

            boolean hasOkValues = model.queryContains("okValues", transactionName);
            boolean hasKoValues = model.queryContains("koValues", transactionName);
            boolean hasPercValues = model.queryContains("percValues", transactionName);

            if (hasOkValues && hasPercValues) {
                test.setOkValue(model.getQueryResults().get("okValues").get(transactionName));

                if (hasKoValues) {
                    test.setKoValue(model.getQueryResults().get("koValues").get(transactionName));
                } else {
                    test.setKoValue(0);
                }

                test.setPctValue(model.getQueryResults().get("percValues").get(transactionName));
                test.setMinValue(model.getQueryResults().get("minValues").get(transactionName));
                test.setMaxValue(model.getQueryResults().get("maxValues").get(transactionName));
                test.setStddevValue(model.getQueryResults().get("stddevValues").get(transactionName));
                test.setAvgValue(model.getQueryResults().get("avgValues").get(transactionName));
                test.setCvValue(model.getQueryResults().get("cvValues").get(transactionName));
                test.setProfileData(test.multiplyProfileData(
                        testUtility.calculateProfileDeviation(profileData, duration),
                        test.getMultiplier()));
                test.setAccuracy(testUtility.calculateAccuracy(test.getProfileData(), test.getOkValue()));
                test.setAccuracySla(testUtility.isAccuracyDeviated(test.getAccuracy(), 98));

                test.setResponseTimeSla(testUtility.isSlaDeviated(test.getPctValue(), sla));

                test.addTotalOkValue(Long.parseLong(test.getOkValue()));
                test.addTotalKoValue(Long.parseLong(test.getKoValue()));

                boolean isSlaViolated = test.isAccuracySla() || test.isResponseTimeSla();

                sb.append("<tr style=\"")
                        .append(isSlaViolated ? "background-color: #FF6666;" : "")
                        .append("\"><td>").append(i + 1)
                        .append("</td><td>").append(transactionDescribe)
                        .append("</td><td>").append(transactionName)
                        .append("</td><td>").append(test.getProfileData())
                        .append("</td><td>").append(test.getOkValue())
                        .append("</td><td>").append(test.getKoValue())
                        .append("</td><td>").append(test.getAccuracy()).append("%")
                        .append("</td><td>").append(test.getPctValue())
                        .append("</td><td>").append(sla)
                        .append("</td><td>").append(test.getAvgValue())
                        .append("</td><td>").append(test.getMinValue())
                        .append("</td><td>").append(test.getMaxValue())
                        .append("</td><td>").append(test.getStddevValue())
                        .append("</td><td>").append(test.getCvValue())
                        .append("</td></tr>");
            }
        }
        return sb.toString();
    }
}
