package reportRunner.Model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TestResults {
    private Long totalOkValue = 0L;
    private Long totalKoValue = 0L;
    private String okValue;
    private String koValue;
    private String pctValue;
    private String profileData;
    private String accuracy;
    private String minValue;
    private String maxValue;
    private String avgValue;
    private String stddevValue;
    private String cvValue;
    private boolean responseTimeSla;
    private boolean accuracySla;
    private Double multiplier;

    public void addTotalOkValue(Long value) {
        this.totalOkValue += value;
    }

    public void addTotalKoValue(Long value) {
        this.totalKoValue += value;
    }

    public void setOkValue(double okValue) {
        this.okValue = String.valueOf((long) okValue);
    }

    public void setKoValue(double koValue) {
        this.koValue = String.valueOf((long) koValue);
    }

    public void setPctValue(double pctValue) {
        this.pctValue = String.valueOf((long) pctValue);
    }

    public void setMinValue(double minValue) {
        this.minValue = String.valueOf((long) minValue);
    }

    public void setAvgValue(double avgValue) {
        this.avgValue = String.valueOf((long) avgValue);
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = String.valueOf((long) maxValue);
    }

    public void setStddevValue(double stddevValue) {
        this.stddevValue = String.valueOf((long) stddevValue);
    }

    public void setCvValue(double cvValue) {
        this.cvValue = String.valueOf( cvValue).substring(0,5);
    }

    public String multiplyProfileData(Double deviation, Double multiplier) {
        long result = (long) (deviation * multiplier);
        return Long.toString(result);
    }
}
