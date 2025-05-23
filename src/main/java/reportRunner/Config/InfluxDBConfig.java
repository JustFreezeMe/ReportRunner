package reportRunner.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "influx-db")
@Data
public class InfluxDBConfig {
    private String influxdbUrl = "";
    private String databaseName = "default";
}
