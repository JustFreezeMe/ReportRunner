package reportRunner.tsdb.InfluxDB;

import lombok.Getter;
import lombok.Setter;
import reportRunner.Config.InfluxDBConfig;
import reportRunner.Config.UtilityConfig;
import reportRunner.tsdb.Tsdb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class InfluxDTO implements Tsdb {

    private final Map<String, Map<String, Double>> queryResults = new HashMap<>();
    private final InfluxController influxController;
    private final InfluxDBConfig influxDBConfig;
    private final UtilityConfig utilityConfig;

    public InfluxDTO(String startTimestamp, String endTimestamp, InfluxDBConfig influxDBConfig, UtilityConfig utilityConfig) {
        this.influxDBConfig = influxDBConfig;
        this.utilityConfig = utilityConfig;
        this.influxController = new InfluxController(utilityConfig);
        setQueryResults(startTimestamp, endTimestamp);
    }

    public void setQueryResults(String startTimestamp, String endTimestamp) {
        queryResults.put("okValues", influxController.parseQueryResult(
                influxController.sendQueryToInfluxDb(influxDBConfig.getInfluxdbUrl(),
                        influxDBConfig.getDatabaseName(),
                        getOkQuery(startTimestamp, endTimestamp))));

        queryResults.put("koValues", influxController.parseQueryResult(
                influxController.sendQueryToInfluxDb(influxDBConfig.getInfluxdbUrl(),
                        influxDBConfig.getDatabaseName(),
                        getKoQuery(startTimestamp, endTimestamp))));

        queryResults.put("percValues", influxController.parseQueryResult(
                influxController.sendQueryToInfluxDb(influxDBConfig.getInfluxdbUrl(),
                        influxDBConfig.getDatabaseName(),
                        getResponseTimeQuery(startTimestamp, endTimestamp))));
    }

    @Override
    public void fillQueryResults(String okValues, Map<String, Double> okValues1) {

    }

    private String getOkQuery(String startTimestamp, String endTimestamp) {
        return utilityConfig.getLoadStation().equals("gatling")
                ? influxController.influxDbGatlingSqlQueryForOkCount(startTimestamp, endTimestamp)
                : influxController.influxDbPerfCenterSqlQueryForOk(startTimestamp, endTimestamp);
    }

    private String getKoQuery(String startTimestamp, String endTimestamp) {
        return utilityConfig.getLoadStation().equals("gatling")
                ? influxController.influxDbGatlingSqlQueryForKoCount(startTimestamp, endTimestamp)
                : influxController.influxDbPerfCenterSqlQueryForKo(startTimestamp, endTimestamp);
    }

    private String getResponseTimeQuery(String startTimestamp, String endTimestamp) {
        return utilityConfig.getLoadStation().equals("gatling")
                ? influxController.influxDbGatlingSqlQueryFor95pct(startTimestamp, endTimestamp)
                : influxController.influxDbPerfCenterSqlQueryForResponseTime(startTimestamp, endTimestamp);
    }

    @Override
    public Map<String, Double> getOkValues() {
        return queryResults.getOrDefault("okValues", Collections.emptyMap());
    }

    @Override
    public Map<String, Double> getKoValues() {
        return queryResults.getOrDefault("koValues", Collections.emptyMap());
    }

    @Override
    public Map<String, Double> getPercentileValues() {
        return queryResults.getOrDefault("percValues", Collections.emptyMap());
    }

    public boolean queryContains(String mainKey, String key) {
        return this.queryResults.get(mainKey).containsKey(key);
    }
}
