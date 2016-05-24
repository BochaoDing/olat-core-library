package ch.uzh.campus.importers;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class CampusCSVReader extends CSVReader {

    final static char CSV_FIELD_SEPARATOR = ';';
    final static char CSV_FIELD_QUOTE = '"';

    public CampusCSVReader(String filePath) throws IOException {
        this(filePath, 1);
    }

    public CampusCSVReader(String filePath, int line) throws IOException {
        this(new FileReader(filePath), CSV_FIELD_SEPARATOR, CSV_FIELD_QUOTE, line);
    }

    public CampusCSVReader(Reader reader, char separator, char quotechar, int line) {
        super(reader, separator, quotechar, line);
    }

    @Override
    public String[] readNext() throws IOException {
        String[] result = super.readNext();
        if (result != null) {
            // Trim every field of the result
            for (int i = 0; i < result.length; i++) {
                result[i] = result[i].trim();
            }
        }

        return result;
    }
}