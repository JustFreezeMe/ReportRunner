package reportRunner.Model;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Data
public class Test {
    private final String startTime;
    private final String endTime;
    private final Integer steps;
    private final String rampTime;
    private final String separatedRamp;
    private final String stepDuration;
    private final String startPercent;
    private final Integer stepPercent;
    private final String stabilization;
    private Map<String, LocalDateTime> stepsDates = new TreeMap<>();

    public static class Builder {
        private int steps;
        private String rampTime;
        private String startTime;
        private String endTime;
        private String separatedRamp;
        private String stepDuration;
        private String startPercent;
        private Integer stepPercent;
        private String stabilization;
        // Остальные параметры...

        public Builder() {
        }

        public Builder withStepsCount(int steps) {
            this.steps = steps;
            return this;
        }

        public Builder withRampTime(String time) {
            this.rampTime = time;
            return this;
        }

        public Builder withStartTime(String time) {
            this.startTime = time;
            return this;
        }

        public Builder withEndTime(String time) {
            this.endTime = time;
            return this;
        }

        public Builder withSeparatedRamp(String time) {
            this.separatedRamp = time;
            return this;
        }

        public Builder withStepDuration(String time) {
            this.stepDuration = time;
            return this;
        }

        public Builder withStartPercent(String percent) {
            this.startPercent = percent;
            return this;
        }

        public Builder withStepPercent(Integer percent) {
            this.stepPercent = percent;
            return this;
        }

        public Builder withStabilization(String time) {
            this.stabilization = time;
            return this;
        }

        public Test build() {
            return new Test(this);
        }
    }

    private Test(Builder builder) {
        this.steps = builder.steps;
        this.rampTime = builder.rampTime;
        this.startPercent = builder.startPercent;
        this.stabilization = builder.stabilization;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.stepPercent = builder.stepPercent;
        this.stepDuration = builder.stepDuration;
        this.separatedRamp = builder.separatedRamp;
    }

    public void maxPerfStepsCalculator() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(this.startTime, formatter);

        Integer loadLevel = Integer.parseInt(this.startPercent);
        LocalDateTime firstStepDate = start.plus(Duration.ofMinutes(Long.parseLong(this.rampTime)).plus(Duration.ofMinutes(Long.parseLong(this.stabilization))));
        LocalDateTime endStepDate = firstStepDate.plus(Duration.ofMinutes(Long.parseLong(stepDuration)));
        Duration timeBeforeStep = Duration.ofMinutes(Long.parseLong(stabilization)).plus(Duration.ofMinutes(Long.parseLong(separatedRamp)));

        if (this.stepsDates != null) {
            this.stepsDates.put(startPercent, endStepDate);
        }

        for (int i = 1; i < steps; i++) {

            loadLevel += stepPercent;
            LocalDateTime stepEnd = endStepDate
                    .plus(timeBeforeStep.multipliedBy(i))
                    .plus(Duration.ofMinutes((Long.parseLong(stepDuration))).multipliedBy(i)).minus(Duration.ofMinutes(i));

            stepsDates.put(String.valueOf(loadLevel), stepEnd);
        }
    }
}

