package reportRunner.Csv;

import com.opencsv.CSVReader;
import lombok.SneakyThrows;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvUtility {

    public List<String[]> readCsv(String path) {
        List<String[]> records = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(new FileReader(path))) {

            String[] values;

            while ((values = csvReader.readNext()) != null) {
                records.add(values);
            }
            return records;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public Long csvSize(String path) {

        int colIndex = 1;
        int colLength = 0;
        try (CSVReader csvReader = new CSVReader(new FileReader(path))) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                if (colIndex < values.length && !values[colIndex].isEmpty()) {
                    colLength++;
                }
            }
        }
        return (long) colLength;
    }

    @SneakyThrows
    public Long calculateSumOfIntensity(String path) {
        List<String[]> records = new ArrayList<>();
        Long size = csvSize(path);
        long resultIntensity = 0L;
        try (CSVReader csvReader = new CSVReader(new FileReader(path))) {

            String[] values;
            while ((values = csvReader.readNext()) != null) {
                records.add(values);

            }
            for (int i = 1; i < size; i++) {
                resultIntensity += Long.parseLong(records.get(i)[2]);
            }
        }
        return resultIntensity;
    }
}
