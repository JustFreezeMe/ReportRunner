package reportRunner.Service.TimeSeriesDatabaseService.InfluxDB;


import lombok.Getter;
import lombok.Setter;
import reportRunner.Config.InfluxDBConfig;
import reportRunner.Config.UtilityConfig;
import reportRunner.Service.TimeSeriesDatabaseService.TimeSeriesDatabase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class InfluxService implements TimeSeriesDatabase {

    private final Map<String, Map<String, Double>> queryResults = new HashMap<>();
    private final InfluxController influxController;
    private final InfluxDBQuery influxDBQuery;
    private final InfluxDBConfig influxDBConfig;
    private final UtilityConfig utilityConfig;

    public InfluxService(String startTimestamp, String endTimestamp, InfluxDBConfig influxDBConfig, UtilityConfig utilityConfig) {
        this.influxDBQuery = new InfluxDBQuery();
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

        queryResults.put("minValues", influxController.parseQueryResult(
                influxController.sendQueryToInfluxDb(influxDBConfig.getInfluxdbUrl(),
                        influxDBConfig.getDatabaseName(),
                        getMinResponseTimeQuery(startTimestamp, endTimestamp))));

        queryResults.put("avgValues", influxController.parseQueryResult(
                influxController.sendQueryToInfluxDb(influxDBConfig.getInfluxdbUrl(),
                        influxDBConfig.getDatabaseName(),
                        getAvgResponseTimeQuery(startTimestamp, endTimestamp))));

        queryResults.put("maxValues", influxController.parseQueryResult(
                influxController.sendQueryToInfluxDb(influxDBConfig.getInfluxdbUrl(),
                        influxDBConfig.getDatabaseName(),
                        getMaxResponseTimeQuery(startTimestamp, endTimestamp))));

        queryResults.put("stddevValues", influxController.parseQueryResult(
                influxController.sendQueryToInfluxDb(influxDBConfig.getInfluxdbUrl(),
                        influxDBConfig.getDatabaseName(),
                        getStddevResponseTimeQuery(startTimestamp, endTimestamp))));

        queryResults.put("cvValues", influxController.parseQueryResult(
                influxController.sendQueryToInfluxDb(influxDBConfig.getInfluxdbUrl(),
                        influxDBConfig.getDatabaseName(),
                        getCvResponseTimeQuery(startTimestamp, endTimestamp))));
    }

    @Override
    public void fillQueryResults(String okValues, Map<String, Double> okValues1) {

    }

    private String getOkQuery(String startTimestamp, String endTimestamp) {
        return utilityConfig.getLoadStation().equals("gatling")
                ? influxDBQuery.influxDbGatlingSqlQueryForOkCount(startTimestamp, endTimestamp)
                : influxDBQuery.influxDbPerfCenterSqlQueryForOk(startTimestamp, endTimestamp);
    }

    private String getKoQuery(String startTimestamp, String endTimestamp) {
        return utilityConfig.getLoadStation().equals("gatling")
                ? influxDBQuery.influxDbGatlingSqlQueryForKoCount(startTimestamp, endTimestamp)
                : influxDBQuery.influxDbPerfCenterSqlQueryForKo(startTimestamp, endTimestamp);
    }

    private String getResponseTimeQuery(String startTimestamp, String endTimestamp) {
        return utilityConfig.getLoadStation().equals("gatling")
                ? influxDBQuery.influxDbGatlingSqlQueryFor95pct(startTimestamp, endTimestamp)
                : influxDBQuery.influxDbPerfCenterSqlQueryForResponseTime(startTimestamp, endTimestamp);
    }

    private String getMinResponseTimeQuery(String startTimestamp, String endTimestamp) {
        return influxDBQuery.influxDbPerfCenterSqlQueryForMinResponseTime(startTimestamp, endTimestamp);
    }

    private String getMaxResponseTimeQuery(String startTimestamp, String endTimestamp) {
        return influxDBQuery.influxDbPerfCenterSqlQueryForMaxResponseTime(startTimestamp, endTimestamp);
    }

    private String getAvgResponseTimeQuery(String startTimestamp, String endTimestamp) {
        return influxDBQuery.influxDbPerfCenterSqlQueryForAvgResponseTime(startTimestamp, endTimestamp);
    }

    private String getStddevResponseTimeQuery(String startTimestamp, String endTimestamp) {
        return influxDBQuery.influxDbPerfCenterSqlQueryForStddevResponseTime(startTimestamp, endTimestamp);
    }

    private String getCvResponseTimeQuery(String startTimestamp, String endTimestamp) {
        return influxDBQuery.influxDbPerfCenterSqlQueryForCvResponseTime(startTimestamp, endTimestamp);
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

    @Override
    public Map<String, Double> getMinResponseTimeValues() {
        return queryResults.getOrDefault("minValues", Collections.emptyMap());
    }

    @Override
    public Map<String, Double> getMaxResponseTimeValues() {
        return queryResults.getOrDefault("maxValues", Collections.emptyMap());
    }

    @Override
    public Map<String, Double> getAvgResponseTimeValues() {
        return queryResults.getOrDefault("avgValues", Collections.emptyMap());
    }

    @Override
    public Map<String, Double> getStddevResponseTimeValues() {
        return queryResults.getOrDefault("stddevValues", Collections.emptyMap());
    }

    @Override
    public Map<String, Double> getCvResponseTimeValues() {
        return queryResults.getOrDefault("cvValues", Collections.emptyMap());
    }

    public boolean queryContains(String mainKey, String key) {
        return this.queryResults.get(mainKey).containsKey(key);
    }
}
