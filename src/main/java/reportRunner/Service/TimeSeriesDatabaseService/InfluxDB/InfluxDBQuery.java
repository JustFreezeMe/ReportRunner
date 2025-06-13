package reportRunner.Service.TimeSeriesDatabaseService.InfluxDB;

public class InfluxDBQuery {

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

    public String influxDbPerfCenterSqlQueryForAvgResponseTime(String startTimestamp, String endTimestamp) {

        return "SELECT mean(\"tr_Duration\") FROM \"anketaALL\"\n" +
                "WHERE (time >= " + startTimestamp + "ms and time <= " + endTimestamp + "ms)\n" +
                "GROUP BY time(1m),\"Transaction_name\" fill(0)";
    }

    public String influxDbPerfCenterSqlQueryForMinResponseTime(String startTimestamp, String endTimestamp) {

        return "SELECT min(\"tr_Duration\") FROM \"anketaALL\"\n" +
                "WHERE (time >= " + startTimestamp + "ms and time <= " + endTimestamp + "ms)\n" +
                "GROUP BY time(1m),\"Transaction_name\" fill(0)";
    }

    public String influxDbPerfCenterSqlQueryForMaxResponseTime(String startTimestamp, String endTimestamp) {

        return "SELECT max(\"tr_Duration\") FROM \"anketaALL\"\n" +
                "WHERE (time >= " + startTimestamp + "ms and time <= " + endTimestamp + "ms)\n" +
                "GROUP BY time(1m),\"Transaction_name\" fill(0)";
    }

    public String influxDbPerfCenterSqlQueryForStddevResponseTime(String startTimestamp, String endTimestamp) {

        return "SELECT stddev(\"tr_Duration\") FROM \"anketaALL\"\n" +
                "WHERE (time >= " + startTimestamp + "ms and time <= " + endTimestamp + "ms)\n" +
                "GROUP BY time(1m),\"Transaction_name\" fill(0)";
    }

    public String influxDbPerfCenterSqlQueryForCvResponseTime(String startTimestamp, String endTimestamp) {

        return "SELECT stddev(\"tr_Duration\")/avg(\"tr_Duration\") FROM \"anketaALL\"\n" +
                "WHERE (time >= " + startTimestamp + "ms and time <= " + endTimestamp + "ms)\n" +
                "GROUP BY time(1m),\"Transaction_name\" fill(0)";
    }
}
