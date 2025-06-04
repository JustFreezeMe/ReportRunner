package ru.alfabank.ReportRunner.Service.TimeSeriesDatabaseService.VictoriaMetrics;

public class VictoriaMetricsQuery {
    public String getResponseTimeQueryVm(String quantile, String testDuration) {
        return "quantile_over_time(quantileValue,requests_duration{db=\"gatling\",result=\"OK\"}[testDuration])" //%7B %7D это {}
                .replace("quantileValue", quantile)
                .replace("testDuration", testDuration);
    }

    public String getAvgResponseTimeQueryVm(String testDuration) {
        return "avg_over_time(quantileValue,requests_duration{db=\"gatling\",result=\"OK\"}[testDuration])" //%7B %7D это {}
                .replace("testDuration", testDuration);
    }

    public String getMinResponseTimeQueryVm(String testDuration) {
        return "min_over_time(requests_duration{db=\"gatling\",result=\"OK\"}[testDuration])" //%7B %7D это {}
                .replace("testDuration", testDuration);
    }

    public String getMaxResponseTimeQueryVm(String testDuration) {
        return "max_over_time(requests_duration{db=\"gatling\",result=\"OK\"}[testDuration])" //%7B %7D это {}
                .replace("testDuration", testDuration);
    }

    public String getStddevResponseTimeQueryVm(String testDuration) {
        return "stddev_over_time(requests_duration{db=\"gatling\",result=\"OK\"}[testDuration])" //%7B %7D это {}
                .replace("testDuration", testDuration);
    }

    public String getCVResponseTimeQueryVm(String testDuration) {
        return "stddev_over_time(requests_duration{db=\"gatling\",result=\"OK\"}[testDuration])"
                .replace("testDuration", testDuration) +
                "/" +
                "avg_over_time(requests_duration{db=\"gatling\",result=\"OK\"}[testDuration])" //%7B %7D это {}
                        .replace("testDuration", testDuration);
    }

    public String getRequestsCountQueryVm(String result, String testDuration) {
        String query = "count_over_time(requests_duration{db=\"gatling\",result=\"requestResult\"}[testDuration])";

        return query.replace("requestResult", result)
                .replace("testDuration", testDuration);
    }
}
