package reportRunner.Util;

import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class TestUtility {

    public Map<String, LocalDateTime> calclateTimestampsForGatling(String endTime, String duration) {

        Map<String, LocalDateTime> times = new HashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);
        Duration dur = Duration.ofMinutes(Long.parseLong(duration));
        LocalDateTime start = end.minus(dur);

        times.put("startTime", start);
        times.put("endTime", end);

        return times;
    }

    public LocalDateTime calclateFirstStepDate(LocalDateTime endTime, String duration) {

        Duration dur = Duration.ofMinutes(Long.parseLong(duration));

        return endTime.minus(dur);
    }

    public Map<String, Long> convertToTimestamp(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Long> timestamps = new HashMap<>();

        long startTimestamp = startTime.toInstant(ZoneOffset.ofHours(3)).toEpochMilli();
        long endTimestamp = endTime.toInstant(ZoneOffset.ofHours(3)).toEpochMilli();

        timestamps.put("startTimestamp", startTimestamp);
        timestamps.put("endTimestamp", endTimestamp);

        return timestamps;
    }

    public Map<String, String> convertToTimestampInflux(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, String> timestamps = new HashMap<>();
        ZonedDateTime zonedDateTimeStart = startTime.atZone(ZoneId.systemDefault());
        ZonedDateTime zonedDateTimeEnd = endTime.atZone(ZoneId.systemDefault());
        long startTimestamp = zonedDateTimeStart.toInstant().toEpochMilli();
        long endTimestamp = zonedDateTimeEnd.toInstant().toEpochMilli();

        timestamps.put("startTimestamp", String.valueOf(startTimestamp));
        timestamps.put("endTimestamp", String.valueOf(endTimestamp));

        return timestamps;
    }

    public String getCurrentDate() {
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return localDate.format(dateFormat);
    }


    public String calculateAccuracy(String profile, String okValue) {
        Double intProfile = Double.parseDouble(profile);
        Double intOkValue = Double.parseDouble(okValue);

        double accuracy = intOkValue / intProfile;

        return String.format("%.0f", accuracy * 100);
    }

    public Double calculateProfileDeviation(String targetLoad, String stepDuration) {
        double loadValue = Integer.parseInt(targetLoad);
        double step = Integer.parseInt(stepDuration);

        double rpm = loadValue / 60;

        return rpm * step;
    }

    public String calculateTestDuration(String start, String end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime endDuration = LocalDateTime.parse(end, formatter);
        LocalDateTime startDuration = LocalDateTime.parse(start, formatter);

        Duration testDuration = Duration.between(startDuration, endDuration);

        return String.valueOf(testDuration.toMinutes());
    }

    public String calculateTestDuration(LocalDateTime start, LocalDateTime end) {

        Duration testDuration = Duration.between(start, end);

        return String.valueOf(testDuration.toMinutes());
    }

    public boolean isAccuracyDeviated(String accuracy, int targetAccuracy) {
        int longAccuracy = Integer.parseInt(accuracy.split("\\.")[0]);

        return longAccuracy < targetAccuracy;
    }

    public boolean isSlaDeviated(String responseTime, String sla) {
        int longResponseTime = Integer.parseInt(responseTime.split("\\.")[0]);
        int longSla = Integer.parseInt(sla.split("\\.")[0]) * 1000;

        return longResponseTime > longSla;
    }
}
