package ch.uzh.campus.connectors;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.data.*;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * This class is an implementation listener that will be notified in the case of:
 * <ul>
 * <li>skipped items while reading, processing and writing an item.
 * <li>before, after and in case of any exception thrown while writing a list of items.
 * <li>before and after the execution of the step's processing
 * </ul>
 * <br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */
public class CampusInterceptor<T, S> implements StepExecutionListener, ItemWriteListener<S>, SkipListener<T, S>, ChunkListener {

	private static final OLog LOG = Tracing.createLoggerFor(CampusInterceptor.class);

    private final DB dbInstance;
	private final ImportStatisticDao statisticDao;
	private final SkipItemDao skipItemDao;
	private final DaoManager daoManager;
    private final CampusCourseConfiguration campusCourseConfiguration;

    private StepExecution stepExecution;
    private int fixedNumberOfFilesToBeExported;
    private int chunkCount;
    private long chunkStartTime;

	@Autowired
	public CampusInterceptor(DB dbInstance, ImportStatisticDao statisticDao, SkipItemDao skipItemDao,
                             DaoManager daoManager, CampusCourseConfiguration campusCourseConfiguration) {
		this.dbInstance = dbInstance;
		this.statisticDao = statisticDao;
		this.skipItemDao = skipItemDao;
		this.daoManager = daoManager;
        this.campusCourseConfiguration = campusCourseConfiguration;
	}

    /**
     * Processes some cleanups depending on the appropriate step.
     * 
     * @param se
     *            the StepExecution
     */
    @Override
    public void beforeStep(StepExecution se) {
        try {
            LOG.info(se.toString());

			this.stepExecution = se;
            // Chunk count and duration is being logged for sync step since this may be slow and potentially break timeout
            if (CampusProcessStep.CAMPUSSYNCHRONISATION.name().equalsIgnoreCase(se.getStepName())) {
                chunkCount = 0;
            }
            // Before importing Texts, delete all old ones
            if (CampusProcessStep.IMPORT_TEXTS.name().equalsIgnoreCase(se.getStepName())) {
                daoManager.deleteAllTexts();
            }
            // DISABLED FOR NOW
            // if (CampusImportStep.IMPORT_EVENTS.name().equalsIgnoreCase(se.getStepName())) {
            // daoManager.deleteAllEvents();
            // }

            dbInstance.commitAndCloseSession();
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            throw t;
        }
    }

    /**
     * Generates an appropriate statistic of the processed, <br>
     * delegates the cleanup and the metric notification.
     * 
     * @param se
     *            the StepExecution
     */
    @Override
    public ExitStatus afterStep(StepExecution se) {
        try {
            LOG.info(se.toString());
            statisticDao.saveOrUpdate(createImportStatistic(se));
			dbInstance.commitAndCloseSession();
            if (CampusProcessStep.IMPORT_CONTROLFILE.name().equalsIgnoreCase(se.getStepName())) {
                if (se.getWriteCount() != getFixedNumberOfFilesToBeExported()) {
                    return ExitStatus.FAILED;
                }
            }
            return null;
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            throw t;
        }
    }

    /**
     * @param items
     *            the list of items to be written in the database.
     */
    @Override
    public void afterWrite(List<? extends S> items) {
        if (items != null) {
            LOG.debug("afterWrite: " + items);
        }
    }

    /**
     * @param items
     *            the list of items to be written in the database.
     */
    @Override
    public void beforeWrite(List<? extends S> items) {
        if (items != null) {
            LOG.debug("beforeWrite: " + items);
        }
    }

    /**
     * @param items
     *            the list of items to be written in the database.
     */
    @Override
    public void onWriteError(Exception ex, List<? extends S> items) {
    }

    /**
     * Writes the skipped item with the caused failure while processing in the database.
     * 
     * @param item
     *            the failed item
     * @param ex
     *            the cause of failure
     */
    @Override
    public void onSkipInProcess(T item, Throwable ex) {
        try {
            LOG.debug("onSkipInProcess: " + item);
            skipItemDao.save(createSkipItem("PROCESS", item.toString(), ex.getMessage()));
            dbInstance.commitAndCloseSession();
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            throw t;
        }
    }

    /**
     * Writes the caused failure while reading in the database.
     * 
     * @param ex
     *            the cause of failure
     */
    @Override
    public void onSkipInRead(Throwable ex) {
        try {
            LOG.debug("onSkipInRead: ");
            skipItemDao.save(createSkipItem("READ", null, ex.getMessage()));
            dbInstance.commitAndCloseSession();
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            throw t;
        }
    }

    /**
     * Writes the skipped item with the caused failure while writing in the database.
     * 
     * @param item
     *            the failed item
     * @param ex
     *            the cause of failure
     */
    @Override
    public void onSkipInWrite(S item, Throwable ex) {
        try {
            LOG.debug("onSkipInWrite: " + item);
            skipItemDao.save(createSkipItem("WRITE", item.toString(), ex.getMessage()));
            dbInstance.commitAndCloseSession();
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            throw t;
        }
    }

    /**
     * Generates the statistic based on the StepExecution.
     * 
     * @param se
     *            the StepExecution
     */
    private ImportStatistic createImportStatistic(StepExecution se) {
        ImportStatistic statistic = new ImportStatistic();
        statistic.setStepId(se.getId());
        statistic.setStepName(se.getStepName());
        statistic.setStatus(se.getStatus().toString());
        statistic.setReadCount(se.getReadCount());
        statistic.setReadSkipCount(se.getReadSkipCount());
        statistic.setWriteCount(se.getWriteCount());
        statistic.setWriteSkipCount(se.getWriteSkipCount());
        statistic.setProcessSkipCount(se.getProcessSkipCount());
        statistic.setCommitCount(se.getCommitCount());
        statistic.setRollbackCount(se.getRollbackCount());
        statistic.setStartTime(se.getStartTime());
        statistic.setEndTime(se.getLastUpdated());
        return statistic;
    }

    /**
     * Creates a SkipItem based on the given parameters.
     * 
     * @param type
     *            the kind of subprocess (READ, PROCESS, WRITE)
     * @param item
     *            the name of the item be skipped
     * @param msg
     *            the description of the caused failure
     * 
     */
    private SkipItem createSkipItem(String type, String item, String msg) {
        SkipItem skipItem = new SkipItem();
        skipItem.setType(type);
        skipItem.setItem(item);
        skipItem.setMsg(msg);
        skipItem.setJobExecutionId(stepExecution.getJobExecutionId());
        skipItem.setJobName(stepExecution.getJobExecution().getJobInstance().getJobName());
        skipItem.setStepExecutionId(stepExecution.getId());
        skipItem.setStepName(stepExecution.getStepName());
        skipItem.setStepStartTime(stepExecution.getStartTime());
        return skipItem;
    }

    /**
     * Gets the fixedNumberOfFilesToBeExported.
     */
    private int getFixedNumberOfFilesToBeExported() {
        return fixedNumberOfFilesToBeExported;
    }

    /**
     * Sets the fixedNumberOfFilesToBeExported.
     * 
     * @param fixedNumberOfFilesToBeExported
     *            the fixedNumberOfFilesToBeExported
     */
    public void setFixedNumberOfFilesToBeExported(int fixedNumberOfFilesToBeExported) {
        this.fixedNumberOfFilesToBeExported = fixedNumberOfFilesToBeExported;
    }

    @Override
    public void beforeChunk() {
		/*
		 * Chunk count and duration is being logged for sync step since this
		 * may be slow and potentially break timeout.
		 */
        if (CampusProcessStep.CAMPUSSYNCHRONISATION.name().equalsIgnoreCase(stepExecution.getStepName())) {
            chunkStartTime = System.currentTimeMillis();
        }
    }

    @Override
    public void afterChunk() {
		/*
		 * Chunk count and duration is being logged for sync step since this
		 * may be slow and potentially break timeout.
		 */
        int timeout = campusCourseConfiguration.getConnectionPoolTimeout(); // milliseconds!
        if (timeout > 0 && CampusProcessStep.CAMPUSSYNCHRONISATION.name().equalsIgnoreCase(stepExecution.getStepName())) {
            chunkCount++;
            long chunkProcessingDuration = System.currentTimeMillis() - chunkStartTime;
            if (chunkProcessingDuration > timeout * 0.9) {
                LOG.warn("Chunk no "
                        + chunkCount
                        + " for campus synchronisation took "
                        + chunkProcessingDuration
                        + " ms which is more than 90% of configured database connection pool timeout of "
                        + timeout
                        + " sec. Please consider to take action in order to avoid a timeout (increase unreturned connection timeout or decrease chunk size).");
            } else {
                LOG.debug("Chunk no " + chunkCount + " for campus synchronisation took " + chunkProcessingDuration + " ms (timeout is " + timeout + " s).");
            }
        }
    }
}
