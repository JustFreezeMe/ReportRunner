package reportRunner.ResultsCreator;

import reportRunner.Model.Test;
import reportRunner.Model.TestResults;
import reportRunner.Service.TimeSeriesDatabase.TimeSeriesDatabase;

import java.io.FileNotFoundException;
import java.io.IOException;


public interface Results {

    String createTableForResults(Long len, String path, Test testExemplar) throws IOException;

    String createResultsAppend(TestResults test, TimeSeriesDatabase model, Long len, String path, String duration) throws FileNotFoundException;

}
