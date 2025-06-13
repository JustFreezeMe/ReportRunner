package reportRunner.Config.TestConfig;

import lombok.Data;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Data
public class TestConfig {
    private String testType;
    private String testEndTime;
    private String testStartTime;
    private String loadLevel;
    private String stepDuration;
    private Integer stepsCount;
    private String rampTime;
    private String separatingRamp;
    private Integer stepPercent;
    private String stabilizationTime;
    private Long endTimestamp ;


    public Long getEndTimestamp(){
        if(endTimestamp == null && testEndTime != null){
            endTimestamp = convertTimeToTimestamp(testEndTime);
        }
        return  endTimestamp;
    }
    private Long convertTimeToTimestamp(String testEndTime) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime end = LocalDateTime.parse(testEndTime, formatter);
        return end.toInstant(ZoneOffset.ofHours(3)).toEpochMilli();
    }
}
