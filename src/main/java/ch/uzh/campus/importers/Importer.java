package ch.uzh.campus.importers;

import ch.uzh.campus.data.CampusDao;
import com.opencsv.CSVReader;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Importer {

    @Autowired
    private DB dbInstance;

    final static int RECORDS_PER_HASH = 100;
    final static int HASHES_PER_LINE = 80;
    final static int COMMIT_INTERVAL = 20;

    final static String SKIP_REASON_MALFORMED_CSV = "MALFORMED_CSV";
    final static String SKIP_REASON_FILTERED_OUT = "FILTERED_OUT";
    final static String SKIP_REASON_DUPLICATE_ID = "DUPLICATE_ID";

    protected int cntSuccess = 0;
    protected int cntFailed = 0;
    protected int cntSkipped = 0;

    protected static final OLog LOG = Tracing.createLoggerFor(CampusBatchlessProcess.class);

    abstract void processEntry(String[] entry);
    abstract void skipEntry(String[] entry, String reason);
    abstract int getEntryFieldCount();
    abstract void persist();

    public void process(String filePath) {

        try {
            int line = 1;
            CSVReader reader = new CampusCSVReader(filePath, line);
            List allEntries = reader.readAll();
            LOG.info("CSV file contains " + allEntries.size() + " data entries");
            for (Object entry : allEntries) {
                line++;
                String[] lineParts = (String[]) entry;
                if (lineParts.length != getEntryFieldCount()) {
                    LOG.warn("CSV File is corrupt" + filePath + " at line " + line);
                    skipEntry(lineParts, SKIP_REASON_MALFORMED_CSV);
                }
                // when entry is processed, entity it is put into the buffer that gets persisted with COMMIT_INTERVAL
                processEntry(lineParts);
                // Show progress with hashes in STDOUT
                showProgressInConsole(line);
            }
            persist(); // whatever has left in the buffer
            System.out.println("Finished processing " + line + " lines. Succeeded importing " + cntSuccess + " lines, failed to import " + cntFailed + " lines");
        } catch (IOException ioe) {
            System.out.println("IO problem: " + ioe.getMessage());
        }

    }

    protected <T> void persistList(List<T> list, CampusDao<T> dao) {
        try {
            int batchSize = list.size();
            LOG.info("Ready to persist " + batchSize + " objects");
            dao.save(list);
            dbInstance.intermediateCommit();
            cntSuccess = cntSuccess + batchSize;
        } catch (Exception e) {
            // Try to persist item by item
            persistListOneByOne(list, dao);
            // clear the list so the at failed items are not accumulated for the next batch
            list.clear();
        }
    }

    private <T> void persistListOneByOne(List<T> list, CampusDao<T> dao) {
        for (T item : list) {
            try {
                List<T> wrapper = new ArrayList<T>();
                dao.save(wrapper);
                cntSuccess++;
            } catch (Exception e) {
                cntFailed++;
            }
        }
        dbInstance.intermediateCommit();
    }

    private void showProgressInConsole(int line) {
        if (line % RECORDS_PER_HASH == 0) {
            System.out.print('#');
            if (line % (RECORDS_PER_HASH * HASHES_PER_LINE) == 0) {
                System.out.println();
            }
        }
    }



}
