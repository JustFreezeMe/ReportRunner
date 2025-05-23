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

    public String multiplyProfileData(Double deviation, Double multiplier) {
        long result = (long) (deviation * multiplier);
        return Long.toString(result);
    }
}
