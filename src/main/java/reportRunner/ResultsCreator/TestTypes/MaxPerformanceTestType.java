package reportRunner.ResultsCreator.TestTypes;

import lombok.SneakyThrows;
import org.springframework.util.StringUtils;
import reportRunner.Config.InfluxDBConfig;
import reportRunner.Config.ProfileConfig;
import reportRunner.Config.UtilityConfig;
import reportRunner.Config.VictoriaMetricsConfig;
import reportRunner.Model.Test;
import reportRunner.Model.TestResults;
import reportRunner.ResultsCreator.Results;
import reportRunner.Util.ProfileReader;
import reportRunner.Util.TestUtility;
import reportRunner.Service.TimeSeriesDatabase.InfluxDB.InfluxService;
import reportRunner.Service.TimeSeriesDatabase.TimeSeriesDatabase;
import reportRunner.Service.TimeSeriesDatabase.VictoriaMetrics.VictoriaMetricsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class MaxPerformanceTestType implements Results {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    TestUtility testUtility = new TestUtility();
    private final UtilityConfig utilityConfig;
    private final InfluxDBConfig influxDBConfig;
    private final VictoriaMetricsConfig victoriaMetricsConfig;

    public MaxPerformanceTestType(UtilityConfig utilityConfig, InfluxDBConfig influxDBConfig,VictoriaMetricsConfig victoriaMetricsConfig) {
        this.utilityConfig = utilityConfig;
        this.influxDBConfig = influxDBConfig;
        this.victoriaMetricsConfig = victoriaMetricsConfig;
    }

    @SneakyThrows
    @Override
    public String createTableForResults(Long len, String path, Test testExemplar) {
        TestResults test = new TestResults();
        testExemplar.maxPerfStepsCalculator();
        Map<String, LocalDateTime> stepsTimes = testExemplar.getStepsDates();


        LocalDateTime startStep = testUtility.calclateFirstStepDate(stepsTimes.get(testExemplar.getStartPercent()), testExemplar.getStepDuration());
        Map<String, String> timestamps = testUtility.convertToTimestampInflux(startStep, stepsTimes.get(testExemplar.getStartPercent()));
        TimeSeriesDatabase model = null;
        if (StringUtils.hasText(influxDBConfig.getInfluxdbUrl())) {
            model = new InfluxService(timestamps.get("startTimestamp"), timestamps.get("endTimestamp"), influxDBConfig, utilityConfig);
        } else if (StringUtils.hasText(victoriaMetricsConfig.getVmUrl())) {
            model = new VictoriaMetricsService(timestamps.get("startTimestamp"), timestamps.get("endTimestamp"),victoriaMetricsConfig);
        }
        int percentIncrease = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("<h2><b>Результаты нагрузочного тестирования</b></h2>");
        sb.append("<table>");
        sb.append("<tr><th>№</th><th>Описание сценария</th><th>Название скрипта</th><th>Целевая интенсивность, операций/час</th><th>Количество успешных операций</th><th>Количество неуспешных операций</th><th>Точность, %</th><th>90 перцентиль времени отклика, ms</th><th>SLA по времени отклика, сек</th></tr>");
        sb.append("<tr>" + "<th colspan=\"9\" style=\"text-align: center;\">Уровень нагрузки от профиля: ").append(testExemplar.getStartPercent())
                .append("%, время ступени: ").append(startStep.format(formatter)).append(" - ").append(stepsTimes.get(testExemplar.getStartPercent())).append(" </th>").append("</tr>");

        test.setMultiplier(Double.parseDouble(testExemplar.getStartPercent()) / 100);
        sb.append(createResultsAppend(test, model, len, path, testExemplar.getStepDuration()));

        for (int k = 0; k < testExemplar.getSteps() - 1; k++) {

            percentIncrease += testExemplar.getStepPercent();

            Integer loadlevel = percentIncrease + Integer.parseInt(testExemplar.getStartPercent());
            test.setMultiplier(Double.parseDouble(String.valueOf(loadlevel)) / 100);
            startStep = testUtility.calclateFirstStepDate(stepsTimes.get(String.valueOf(loadlevel)), testExemplar.getStepDuration());
            timestamps = testUtility.convertToTimestampInflux(startStep, stepsTimes.get(String.valueOf(loadlevel)));
            model.setQueryResults(timestamps.get("startTimestamp"), timestamps.get("endTimestamp"));
            model.fillQueryResults("okValues", model.getOkValues());
            model.fillQueryResults("koValues", model.getKoValues());
            model.fillQueryResults("percValues", model.getPercentileValues());


            sb.append("<tr>" + "<th colspan=\"9\" style=\"text-align: center;\">Уровень нагрузки от профиля: ").append(loadlevel)
                    .append("%, время ступени: ").append(startStep.format(formatter)).append(" - ").append(stepsTimes.get(String.valueOf(loadlevel))).append(" </th>").append("</tr>");

            sb.append(createResultsAppend(test, model, len, path, testExemplar.getStepDuration()));
        }

        sb.append("</table>");

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
                        .append("</td></tr>");
            }
        }
        return sb.toString();
    }
}
