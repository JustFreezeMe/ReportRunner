package reportRunner.Config;


import org.springframework.context.annotation.Configuration;

@Configuration
public class TsdbConfig {

//    @Bean
//    public Tsdb tsdb(InfluxDBConfig influxDBConfig,
//                     VictoriaMetricsConfig victoriaMetrics,
//                     UtilityConfig utilityConfig,
//                     @Value("${TsdbConfig.source}")String tsdbSource,
//                     TestUtility testUtility) {
////        Map<String, String> timestamps = testUtility.convertToTimestampInflux(
////                testUtility.getTimestamp().get("startTime"),
////                testUtility.getTimes().get("endTime")
////        );
////
////        if ("vm".equalsIgnoreCase(tsdbSource)) {
////            return new VictoriaMetricsDTO(
////                    timestamps.get("startTimestamp"),
////                    timestamps.get("endTimestamp"),
////                    victoriaMetrics,
////                    utilityConfig
////            );
////        } else if ("influx".equalsIgnoreCase(tsdbSource)) {
////            return new InfluxDTO(
////                    timestamps.get("startTimestamp"),
////                    timestamps.get("endTimestamp"),
////                    influxDBConfig,
////                    utilityConfig
////            );
////        } else {
////            throw new IllegalArgumentException("Unsupported metric source: " + metricSource);
////        }
//    }
}
