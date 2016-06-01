package ch.uzh.campus.importers;

import ch.uzh.campus.data.*;
import com.opencsv.CSVReader;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class Importer {

    @Autowired
    private DB dbInstance;

    @Autowired
    private SkipItemDao skipItemDao;

    @Autowired
    protected ImportStatisticDao statisticDao;

    final static int RECORDS_PER_HASH = 100;
    final static int HASHES_PER_LINE = 80;
    final static int COMMIT_INTERVAL = 20;

    final static String SKIP_REASON_MALFORMED_CSV = "MALFORMED_CSV";
    final static String SKIP_REASON_FILTERED_OUT = "FILTERED_OUT";
    final static String SKIP_REASON_DUPLICATE_ID = "DUPLICATE_ID";
    final static String SKIP_REASON_FAILED_TO_PROCESS = "SKIP_REASON_FAILED_TO_PROCESS";
    final static String SKIP_REASON_FAILED_TO_PERSIST = "SKIP_REASON_FAILED_TO_PERSIST";

    final static String STATUS_COMPLETED = "COMPLETED";
    final static String STATUS_FAILED = "FAILED";

    protected long stepId;
    protected String stepName;
    protected String stepStatus;

    protected int line = 0;
    protected int cntSuccess = 0;
    protected int cntFailed = 0;
    protected int cntSkipped = 0;
    protected int cntSkippedRead = 0;
    protected int cntSkippedProcess = 0;
    protected int cntSkippedWrite = 0;
    protected int cntCommits = 0;

    // TODO Started/LastUpdated
    protected Date startTime;
    protected Date lastUpdated;

    protected static final OLog LOG = Tracing.createLoggerFor(CampusBatchlessProcess.class);

    abstract void processEntry(String[] entry);
    abstract int getEntryFieldCount();
    abstract void persist();

    public long getStepId() {
        return stepId;
    }

    public void setStepId(long stepId) {
        this.stepId = stepId;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getStepStatus() {
        return stepStatus;
    }

    public void setStepStatus(String stepStatus) {
        this.stepStatus = stepStatus;
    }

    protected void beforeImport() {
        startTime = new Date();
    }

    protected void afterImport() {
        statisticDao.save(createImportStatistic());
        // TODO metrics?
    }

    public void process(String filePath) {

        beforeImport();
        try {
            line = 1;
            CSVReader reader = new CampusCSVReader(filePath, line);
            List allEntries = reader.readAll();
            LOG.info("CSV file contains " + allEntries.size() + " data entries");
            for (Object entry : allEntries) {
                String[] lineParts = (String[]) entry;
                if (lineParts.length != getEntryFieldCount()) {
                    LOG.warn("CSV File is corrupt" + filePath + " at line " + line);
                    skipEntry(lineParts, SKIP_REASON_MALFORMED_CSV);
                }
                // when entry is processed, entity it is put into the buffer that gets persisted with COMMIT_INTERVAL
                processEntry(lineParts);
                // Show progress with hashes in STDOUT
                showProgressInConsole(line);
                line++;
            }
            persist(); // whatever has left in the buffer
            System.out.println("Finished processing " + line + " lines. Succeeded importing " + cntSuccess + " lines, failed to import " + cntFailed + " lines");
            setStepStatus(Importer.STATUS_COMPLETED);
        } catch (IOException ioe) {
            System.out.println("IO problem: " + ioe.getMessage());
            setStepStatus(Importer.STATUS_FAILED);
        }
        afterImport();

    }

    void failEntry(String[] entry, String reason) {
        failEntry(entry, reason, "");
    }

    void failEntry(String[] entry, String reason, String moreDetails) {
        failEntry(String.join(";", entry), reason, moreDetails);
    }

    void failEntry(String serializedEntry, String reason, String moreDetails) {
        LOG.info("Skipped entry(" + reason + "):" + serializedEntry); // TODO convert to LOG.debug()
        switch (reason) {
            case Importer.SKIP_REASON_FAILED_TO_PROCESS:
                cntSkippedProcess++;
                skipItemDao.save(createSkipItem("READ", serializedEntry, "Error while instantiating entity. " + moreDetails));
                break;
            case Importer.SKIP_REASON_FAILED_TO_PERSIST:
                cntSkippedRead++;
                skipItemDao.save(createSkipItem("WRITE", serializedEntry, "Failed to persist. " + moreDetails));
                break;
        }

        skipItemDao.save(createSkipItem("WRITE", serializedEntry, moreDetails));
        cntFailed++;
    }

    void skipEntry(String[] entry, String reason) {
        this.skipEntry(entry, reason, "");
    }

    void skipEntry(String[] entry, String reason, String moreDetails) {
        String originalLine = String.join(";", entry);
        LOG.info("Skipped entry(" + reason + "):" + originalLine); // TODO convert to LOG.debug()
        switch (reason) {
            case Importer.SKIP_REASON_MALFORMED_CSV:
                cntSkippedRead++;
                skipItemDao.save(createSkipItem("READ", originalLine, "CSV malformed at line: " + line));
                break;
            case Importer.SKIP_REASON_FILTERED_OUT:
                cntSkippedProcess++;
                skipItemDao.save(createSkipItem("PROCESS", originalLine, moreDetails + " at line: " + line));
                break;
            case Importer.SKIP_REASON_DUPLICATE_ID:
                cntSkippedProcess++;
                skipItemDao.save(createSkipItem("PROCESS", originalLine, "Duplicate ID found at line: " + line));
                break;
        }
        cntSkipped++;
    }

    protected <T> void persistList(List<T> list, CampusDao<T> dao) {
        try {
            int batchSize = list.size();
            LOG.info("Ready to persist " + batchSize + " objects");
            dao.saveOrUpdate(list);
            dbInstance.intermediateCommit();
            cntCommits++;
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
                wrapper.add(item);
                dao.saveOrUpdate(wrapper);
                cntSuccess++;
            } catch (Exception e) {
                failEntry(item.toString(), Importer.SKIP_REASON_FAILED_TO_PERSIST, e.getMessage());
            }
        }
        dbInstance.intermediateCommit();
        cntCommits++;
    }

    private void showProgressInConsole(int line) {
        if (line % RECORDS_PER_HASH == 0) {
            System.out.print('#');
            if (line % (RECORDS_PER_HASH * HASHES_PER_LINE) == 0) {
                System.out.println();
            }
        }
    }

    private SkipItem createSkipItem(String type, String item, String msg) {
        SkipItem skipItem = new SkipItem();
        skipItem.setType(type);
        skipItem.setItem(item);
        skipItem.setMsg(msg);
// TODO which values to use outside of springframework.batch context?
//        skipItem.setJobExecutionId(getStepExecution().getJobExecutionId());
//        skipItem.setJobName(getStepExecution().getJobExecution().getJobInstance().getJobName());
        skipItem.setStepExecutionId(stepId);
        skipItem.setStepName(stepName);
        skipItem.setStepStartTime(startTime);
        return skipItem;
    }

    private ImportStatistic createImportStatistic() {
        ImportStatistic statistic = new ImportStatistic();
        statistic.setStepId(stepId);
        statistic.setStepName(stepName);
        statistic.setStatus(stepStatus);
        statistic.setReadCount(line);
        statistic.setReadSkipCount(cntSkippedRead);
        statistic.setWriteCount(cntSuccess);
        statistic.setWriteSkipCount(cntSkippedWrite);
        statistic.setProcessSkipCount(cntSkippedProcess);
        statistic.setCommitCount(cntCommits);
// TODO which values to use outside of springframework.batch context?
//        statistic.setRollbackCount(se.getRollbackCount());
        statistic.setStartTime(startTime);
//        statistic.setEndTime(se.getLastUpdated());
        return statistic;
    }





}
