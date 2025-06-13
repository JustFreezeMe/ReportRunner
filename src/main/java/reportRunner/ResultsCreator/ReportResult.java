package reportRunner.ResultsCreator;

import lombok.AllArgsConstructor;
import lombok.Data;
import reportRunner.Service.GrafanaService.GraphGroup;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ReportResult {
    private final Map<String, Long> timestamps;
    private final List<GraphGroup> groupOfGraphs;
    private String reportResult;
}
