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
public class VictoriaMetricsService implements TimeSeriesDatabase {

    private static final String RESPONSE_TIME_QUANTILE = "0.95";
    private final Map<String, Map<String, Double>> queryResults = new HashMap<>();
    private Map<String, String> okValues;
    private Map<String, String> koValues;
    private Map<String, String> percentileValues;
    private Map<String, String> minResponseTimeValues;
    private Map<String, String> maxResponseTimeValues;
    private Map<String, String> stddevResponseTimeValues;
    private Map<String, String> cvResponseTimeValues;
    private VictoriaMetricsController victoriaMetricsController;
    private VictoriaMetricsQuery victoriaMetricsQuery;

    public VictoriaMetricsService(String startTimestamp, String endTimestamp, VictoriaMetricsConfig victoriaMetricsConfig) throws IOException, InterruptedException {
        this.okValues = new HashMap<>();
        this.koValues = new HashMap<>();
        this.percentileValues = new HashMap<>();
        this.minResponseTimeValues = new HashMap<>();
        this.maxResponseTimeValues = new HashMap<>();
        this.stddevResponseTimeValues = new HashMap<>();
        this.cvResponseTimeValues = new HashMap<>();
        this.victoriaMetricsController = new VictoriaMetricsController(victoriaMetricsConfig);
        this.victoriaMetricsQuery = new VictoriaMetricsQuery();
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
    public Map<String,Double> getMinResponseTimeValues() {
        return convertToDouble(minResponseTimeValues);
    }

    @Override
    public Map<String,Double> getMaxResponseTimeValues() {
        return convertToDouble(maxResponseTimeValues);
    }

    @Override
    public Map<String,Double> getStddevResponseTimeValues() {
        return convertToDouble(stddevResponseTimeValues);
    }

    @Override
    public Map<String,Double> getCvResponseTimeValues() {
        return convertToDouble(cvResponseTimeValues);
    }

    @Override
    public boolean queryContains(String mainKey, String key) {
        return queryResults.get(mainKey).containsKey(key);
    }

    @SneakyThrows
    public void setQueryResults(String startTimestamp, String endTimestamp) {
        String duration = calculateTestDuration(startTimestamp,endTimestamp) + "m";

        okValues.putAll(convertToQueryAndParse(getOkQuery(duration),startTimestamp,endTimestamp));
        koValues.putAll(convertToQueryAndParse(getKoQuery(duration),startTimestamp,endTimestamp));
        percentileValues.putAll(convertToQueryAndParse(getResponseTimeQuery(duration),startTimestamp,endTimestamp));
        minResponseTimeValues.putAll(convertToQueryAndParse(getMinResponseTimeQuery(duration),startTimestamp,endTimestamp));
        maxResponseTimeValues.putAll(convertToQueryAndParse(getMaxResponseTimeQuery(duration),startTimestamp,endTimestamp));
        stddevResponseTimeValues.putAll(convertToQueryAndParse(getStddevResponseTimeQuery(duration),startTimestamp,endTimestamp));
        cvResponseTimeValues.putAll(convertToQueryAndParse(getCvResponseTimeQuery(duration),startTimestamp,endTimestamp));

        queryResults.put("okValues",convertToDouble(okValues));
        queryResults.put("koValues",convertToDouble(koValues));
        queryResults.put("percValues",convertToDouble(percentileValues));
        queryResults.put("minValues",convertToDouble(minResponseTimeValues));
        queryResults.put("maxValues",convertToDouble(maxResponseTimeValues));
        queryResults.put("stddevValues",convertToDouble(stddevResponseTimeValues));
        queryResults.put("cvValues",convertToDouble(cvResponseTimeValues));
    }

    @Override
    public void fillQueryResults(String okValues, Map<String, Double> okValues1) {

    }

    private String getOkQuery(String duration) {
        return victoriaMetricsQuery.getRequestsCountQueryVm("OK",duration);

    }

    private String getKoQuery(String duration) {
        return victoriaMetricsQuery.getRequestsCountQueryVm("KO",duration);
    }

    private String getResponseTimeQuery(String duration) {
        return victoriaMetricsQuery.getResponseTimeQueryVm(RESPONSE_TIME_QUANTILE,duration);
    }

    private String getMinResponseTimeQuery(String duration) {
        return victoriaMetricsQuery.getMinResponseTimeQueryVm(duration);
    }

    private String getMaxResponseTimeQuery(String duration) {
        return victoriaMetricsQuery.getMaxResponseTimeQueryVm(duration);
    }

    private String getStddevResponseTimeQuery(String duration) {
        return victoriaMetricsQuery.getStddevResponseTimeQueryVm(duration);
    }

    private String getCvResponseTimeQuery(String duration) {
        return victoriaMetricsQuery.getCVResponseTimeQueryVm(duration);
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

    private Map<String,String> convertToQueryAndParse(String query,String start, String end) throws IOException, InterruptedException {
        String rawResponse = victoriaMetricsController.sendRequestToVm(query,start,end);
        return victoriaMetricsController.parseRequestResult(rawResponse);
    }
}
