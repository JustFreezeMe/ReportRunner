package reportRunner.Service.ResultsService;

import reportRunner.Model.Test;
import reportRunner.Model.TestResults;
import reportRunner.Service.TimeSeriesDatabaseService.TimeSeriesDatabase;
import lombok.SneakyThrows;

public interface ResultsService {
    @SneakyThrows
    String createTableForResults(Long len, String path, Test testExemplar);

    @SneakyThrows
    String createResultsAppend(TestResults test, TimeSeriesDatabase model, Long len, String path, String duration);
}
