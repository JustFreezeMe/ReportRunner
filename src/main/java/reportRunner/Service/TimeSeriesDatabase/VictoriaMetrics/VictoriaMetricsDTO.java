package reportRunner.Service.TimeSeriesDatabase.VictoriaMetrics;

import lombok.Data;
import lombok.SneakyThrows;
import reportRunner.Config.VictoriaMetricsConfig;
import reportRunner.Service.TimeSeriesDatabase.TimeSeriesDatabase;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class VictoriaMetricsDTO implements TimeSeriesDatabase {

    private static final String RESPONSE_TIME_QUANTILE = "0.95";
    private final Map<String, Map<String, Double>> queryResults = new HashMap<>();
    private Map<String, String> okValues;
    private Map<String, String> koValues;
    private Map<String, String> percentileValues;
    private VictoriaMetricsController victoriaMetricsController;

    public VictoriaMetricsDTO(String startTimestamp, String endTimestamp, VictoriaMetricsConfig victoriaMetricsConfig) throws IOException, InterruptedException {
        this.okValues = new HashMap<>();
        this.koValues = new HashMap<>();
        this.percentileValues = new HashMap<>();
        this.victoriaMetricsController = new VictoriaMetricsController(victoriaMetricsConfig);
        setQueryResults(startTimestamp, endTimestamp);
    }

    @Override
    public Map<String,Double> getOkValues() {
        return convertToDouble(okValues);
    }

    @Override
    public Map<String,Double> getKoValues() {
        return convertToDouble(koValues);
    }

    @Override
    public Map<String,Double> getPercentileValues() {
        return convertToDouble(percentileValues);
    }

    @Override
    public boolean queryContains(String mainKey, String key) {
        return queryResults.get(mainKey).containsKey(key);
    }

    @SneakyThrows
    public void setQueryResults(String startTimestamp, String endTimestamp) {
        String duration = calculateTestDuration(startTimestamp,endTimestamp) + "m";
        Map<String,String> requestResult = victoriaMetricsController.parseRequestResult(victoriaMetricsController.sendRequestToVm(getOkQuery(duration),startTimestamp,endTimestamp));
        this.okValues.putAll(requestResult);
        koValues.putAll(victoriaMetricsController.parseRequestResult(victoriaMetricsController.sendRequestToVm(getKoQuery(duration),startTimestamp,endTimestamp)));
        percentileValues.putAll(victoriaMetricsController.parseRequestResult(victoriaMetricsController.sendRequestToVm(getResponseTimeQuery(duration),startTimestamp,endTimestamp)));
        queryResults.put("okValues",convertToDouble(okValues));
        queryResults.put("koValues",convertToDouble(koValues));
        queryResults.put("percValues",convertToDouble(percentileValues));
    }

    @Override
    public void fillQueryResults(String okValues, Map<String, Double> okValues1) {

    }

    private String getOkQuery(String duration) {
        return victoriaMetricsController.getRequestsCountQueryVm("OK",duration);

    }

    private String getKoQuery(String duration) {
        return victoriaMetricsController.getRequestsCountQueryVm("KO",duration);
    }

    private String getResponseTimeQuery(String duration) {
        return victoriaMetricsController.getResponseTimeQueryVm(RESPONSE_TIME_QUANTILE,duration);
    }

    private Map<String, Double> convertToDouble(Map<String, String> source) {
        if (source == null) return Collections.emptyMap();
        return source.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> {
                            try {
                                return Double.parseDouble(e.getValue());
                            } catch (NumberFormatException ex) {
                                return 0.0;
                            }
                        }));
    }

    private String calculateTestDuration(String startTimestamp,String endTimestamp) {
        Instant start = Instant.ofEpochMilli(Long.parseLong(startTimestamp));
        Instant end = Instant.ofEpochMilli(Long.parseLong(endTimestamp));
        return String.valueOf(Duration.between(start, end).toMinutes());
    }
}
