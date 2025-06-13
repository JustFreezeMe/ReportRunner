package reportRunner.Service.TimeSeriesDatabaseService.InfluxDB;

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
}
