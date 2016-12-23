package ch.uzh.campus.connectors;

import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;

/**
 * Avoid that batch reader stops in case of a blank line.
 *
 * Source: http://stackoverflow.com/questions/29673524/how-to-skip-blank-lines-in-csv-using-flatfileitemreader-and-chunks
 *
 * @author Martin Schraner
 */
public class BlankLineRecordSeparatorPolicy extends SimpleRecordSeparatorPolicy {

    @Override
    public boolean isEndOfRecord(final String line) {
        return line.trim().length() != 0 && super.isEndOfRecord(line);
    }

    @Override
    public String postProcess(final String record) {
        if (record == null || record.trim().length() == 0) {
            return null;
        }
        return super.postProcess(record);
    }

}
