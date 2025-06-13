package reportRunner.Service.FaultToleranceService;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Data
//Заготовка под класс для тестов отказоустойчивости
public class FaultToleranceScenario {
    private String faultScenarioTestName;
    private String faultScenarioTestEnd;
    private String faultScenarioTestLength;
    private String faultScenarioStabilizationLength;
    private Map<String, Long> timestamps;
    private String confluenceChildredPageId;


    public String getFaultScenarioStartTime(String faultScenarioEndTime, String faultScenarioTestLength) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime endDuration = LocalDateTime.parse(faultScenarioEndTime, formatter);
        Duration duration = Duration.ofMinutes(Long.parseLong(faultScenarioTestLength));
        LocalDateTime testStartTime = endDuration.minus(duration);

        return String.valueOf(testStartTime);
    }

    public String getEndTimeWithStabilization(String endTime, String faultScenarioTestStabilization) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime end = LocalDateTime.parse(endTime, formatter);

        Duration dur = Duration.ofMinutes(Long.parseLong(faultScenarioTestStabilization));

        LocalDateTime trueEnd = end.plus(dur);

        return trueEnd.format(formatter);
    }
}
