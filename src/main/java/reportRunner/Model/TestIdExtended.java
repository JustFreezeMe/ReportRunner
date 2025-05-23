package reportRunner.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TestIdExtended {
    int id;
    String startTime;
    String endTime;
    int totalPassedTransactions;
    int totalFailedTransactions;
    int totalErrors;
}
