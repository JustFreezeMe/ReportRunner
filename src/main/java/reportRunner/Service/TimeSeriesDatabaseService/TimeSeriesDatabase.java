package reportRunner.Service.TimeSeriesDatabaseService;

import lombok.SneakyThrows;

import java.util.Map;


public interface TimeSeriesDatabase {
    Map<String, Double> getOkValues();
    Map<String, Double> getKoValues();
    Map<String, Double> getPercentileValues();
    Map<String, Double> getMinResponseTimeValues();
    Map<String, Double> getMaxResponseTimeValues();

    Map<String,Double> getAvgResponseTimeValues();

    Map<String, Double> getStddevResponseTimeValues();
    Map<String, Double> getCvResponseTimeValues();

    boolean queryContains(String mainKey, String key);

    Map<String, Map<String, Double>> getQueryResults();

    @SneakyThrows
    void setQueryResults(String startTimestamp, String endTimestamp);

    void fillQueryResults(String value, Map<String, Double> valuesMap);
}
