package reportRunner.tsdb.InfluxDB;

import okhttp3.OkHttpClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import reportRunner.Config.UtilityConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class InfluxController {

    private final UtilityConfig utilityConfig;

    public InfluxController(UtilityConfig utilityConfig) {
        this.utilityConfig = utilityConfig;
    }

    public QueryResult sendQueryToInfluxDb(String influxUrl, String database, String query) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(720, TimeUnit.SECONDS)
                .readTimeout(720, TimeUnit.SECONDS)
                .writeTimeout(720, TimeUnit.SECONDS);

        try (InfluxDB influxDB = InfluxDBFactory.connect(influxUrl, "a", "a", clientBuilder)) {
            influxDB.setDatabase(database);
            return influxDB.query(new Query(query));
        }


    }

    public String influxDbGatlingSqlQueryForOkCount(String startTimestamp, String endTimestamp) {

        return "SELECT sum(count) AS value FROM \"gatling\"" +
                " WHERE (time >= " + startTimestamp + "ms" +
                " AND time <= " + endTimestamp + "ms)" +
                " AND status = 'ok' GROUP BY simulation,scenario,request";
    }

    public String influxDbGatlingSqlQueryForKoCount(String startTimestamp, String endTimestamp) {

        return "SELECT sum(count) AS value FROM \"gatling\"" +
                " WHERE (time >= " + startTimestamp + "ms" +
                " AND time <= " + endTimestamp + "ms)" +
                " AND status = 'ko' GROUP BY simulation,scenario,request";
    }

    public String influxDbGatlingSqlQueryFor95pct(String startTimestamp, String endTimestamp) {

        return "SELECT  percentile(\"percentiles90\", 90) as \"90th pct\" FROM \"gatling\"\n" +
                " WHERE (time >= " + startTimestamp + "ms" +
                " AND time <= " + endTimestamp + "ms)" +
                " GROUP BY simulation,scenario,request\n";
    }

    public Map<String, Double> parseQueryResult(QueryResult result) {
        List<QueryResult.Result> resultList = result.getResults();

        Map<String, Double> resultMap = new HashMap<>();
        ArrayList<QueryResult.Series> series = null;
        for (QueryResult.Result item : resultList) {
            series = (ArrayList<QueryResult.Series>) item.getSeries();
        }
        assert series != null;
        for (QueryResult.Series value : series) {

            var tags = value.getTags();
            Double sum = (Double) value.getValues().get(0).get(1);

            if (utilityConfig.getLoadStation().equals("PC")) {
                resultMap.put(tags.get("Transaction_name"), sum);
            } else {
                resultMap.put(tags.get("request"), sum);
            }
        }
        return resultMap;
    }

    public String influxDbPerfCenterSqlQueryForOk(String startTimestamp, String endTimestamp) {

        return "SELECT count(\"tr_Duration\") FROM \"anketaALL\"\n" +
                "WHERE (time >= " + startTimestamp + "ms and time <= " + endTimestamp + "ms)\n" +
                "AND \"Transaction_status\" = '0'" +
                "GROUP BY \"Transaction_name\" fill(0)";
    }

    public String influxDbPerfCenterSqlQueryForKo(String startTimestamp, String endTimestamp) {

        return "SELECT count(\"tr_Duration\") FROM \"anketaALL\"\n" +
                "WHERE (time >= " + startTimestamp + "ms and time <= " + endTimestamp + "ms)\n" +
                "AND \"Transaction_status\" = '1'" +
                "GROUP BY \"Transaction_name\" fill(0)";
    }

    public String influxDbPerfCenterSqlQueryForResponseTime(String startTimestamp, String endTimestamp) {

        return "SELECT percentile(\"tr_Duration\", 90) FROM \"anketaALL\"\n" +
                "WHERE (time >= " + startTimestamp + "ms and time <= " + endTimestamp + "ms)\n" +
                "GROUP BY time(1m),\"Transaction_name\" fill(0)";
    }
}
