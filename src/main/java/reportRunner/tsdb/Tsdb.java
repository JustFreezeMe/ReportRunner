package reportRunner.tsdb;

import lombok.SneakyThrows;

import java.util.Map;


public interface Tsdb {
    Map<String, Double> getOkValues();
    Map<String, Double> getKoValues();
    Map<String, Double> getPercentileValues();

    boolean queryContains(String mainKey, String key);

    Map<String, Map<String, Double>> getQueryResults();

    @SneakyThrows
    void setQueryResults(String startTimestamp, String endTimestamp);

    void fillQueryResults(String okValues, Map<String, Double> okValues1);
}
