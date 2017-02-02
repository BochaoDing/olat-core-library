package ch.uzh.extension.campuscourse.batchprocessing;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.data.dao.BatchJobSkippedItemDao;
import ch.uzh.extension.campuscourse.data.entity.BatchJobSkippedItem;
import ch.uzh.extension.campuscourse.data.entity.BatchJobStatistic;
import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
@Component
@Scope("step")
public abstract class AbstractCampusBatchStepExecutionListener<T, S> implements StepExecutionListener, ItemWriteListener<S>, SkipListener<T, S>, ChunkListener {

	private static final OLog LOG = Tracing.createLoggerFor(AbstractCampusBatchStepExecutionListener.class);

    protected final DB dbInstance;
	private final BatchJobSkippedItemDao batchJobSkippedItemDao;
	protected final DaoManager daoManager;
    protected final CampusCourseConfiguration campusCourseConfiguration;

    protected CampusBatchStepName campusBatchStepName;
    private Long batchJobStatisticId;
    private StepExecution stepExecution;

    @Autowired
	public AbstractCampusBatchStepExecutionListener(DB dbInstance, BatchJobSkippedItemDao batchJobSkippedItemDao, DaoManager daoManager, CampusCourseConfiguration campusCourseConfiguration) {
		this.dbInstance = dbInstance;
		this.batchJobSkippedItemDao = batchJobSkippedItemDao;
		this.daoManager = daoManager;
        this.campusCourseConfiguration = campusCourseConfiguration;
	}

    /**
     * Processes some cleanups depending on the appropriate step.
     * 
     * @param stepExecution
     *            the StepExecution
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        LOG.info(stepExecution.toString());

        this.stepExecution = stepExecution;

		campusBatchStepName = CampusBatchStepName.findByName(stepExecution.getStepName());
        if (campusBatchStepName == null) {
        	String errMsg = "'" + stepExecution.getStepName() + "' is not a valid Campus Batch step name!";
        	throw new RuntimeException(errMsg);
		}

		// Create new batch job statistic
		try {
			BatchJobStatistic batchJobStatistic = createAndPersistBatchJobStatistic(stepExecution);
			dbInstance.commitAndCloseSession();
			batchJobStatisticId = batchJobStatistic.getId();
		} catch (Throwable t) {
			dbInstance.rollbackAndCloseSession();
			throw t;
		}
    }

	protected abstract BatchJobStatistic createAndPersistBatchJobStatistic(StepExecution stepExecution);

	/**
     * Generates an appropriate statistic of the processed, <br>
     * delegates the cleanup and the metric notification.
     * 
     * @param stepExecution
     *            the StepExecution
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        LOG.info(stepExecution.toString());

		// Update batch job statistic
		BatchJobStatistic batchJobStatistic = dbInstance.getCurrentEntityManager().find(BatchJobStatistic.class, batchJobStatisticId);
		batchJobStatistic.setBatchJobStatistic(campusBatchStepName, stepExecution);
		dbInstance.commitAndCloseSession();

		return null;
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
     * @param throwable
     *            the cause of failure
     */
    @Override
    public void onSkipInProcess(T item, Throwable throwable) {
		LOG.debug("onSkipInProcess: " + item);
		BatchJobSkippedItem batchJobSkippedItem = new BatchJobSkippedItem(
				stepExecution.getJobExecution().getJobInstance().getJobName(),
				campusBatchStepName,
				BatchJobSkippedItem.TypeOfBatchProcess.PROCESS,
				stepExecution.getStartTime(),
				item.toString(),
				throwable.getMessage());
		batchJobSkippedItemDao.save(batchJobSkippedItem);
		dbInstance.commitAndCloseSession();
    }

    /**
     * Writes the caused failure while reading in the database.
     * 
     * @param throwable
     *            the cause of failure
     */
    @Override
    public void onSkipInRead(Throwable throwable) {
		LOG.debug("onSkipInRead: ");
		BatchJobSkippedItem batchJobSkippedItem = new BatchJobSkippedItem(
				stepExecution.getJobExecution().getJobInstance().getJobName(),
				campusBatchStepName,
				BatchJobSkippedItem.TypeOfBatchProcess.READ,
				stepExecution.getStartTime(),
				null,
				throwable.getMessage());
		batchJobSkippedItemDao.save(batchJobSkippedItem);
		dbInstance.commitAndCloseSession();
    }

    /**
     * Writes the skipped item with the caused failure while writing in the database.
     * 
     * @param item
     *            the failed item
     * @param throwable
     *            the cause of failure
     */
    @Override
    public void onSkipInWrite(S item, Throwable throwable) {
		LOG.debug("onSkipInWrite: " + item);
		BatchJobSkippedItem batchJobSkippedItem = new BatchJobSkippedItem(
				stepExecution.getJobExecution().getJobInstance().getJobName(),
				campusBatchStepName,
				BatchJobSkippedItem.TypeOfBatchProcess.WRITE,
				stepExecution.getStartTime(),
				item.toString(),
				throwable.getMessage());
		batchJobSkippedItemDao.save(batchJobSkippedItem);
		dbInstance.commitAndCloseSession();
    }

    @Override
    public void beforeChunk(ChunkContext chunkContext) {
    }

    @Override
    public void afterChunk(ChunkContext chunkContext) {
    }

    @Override
    public void afterChunkError(ChunkContext chunkContext) {
    }
}
