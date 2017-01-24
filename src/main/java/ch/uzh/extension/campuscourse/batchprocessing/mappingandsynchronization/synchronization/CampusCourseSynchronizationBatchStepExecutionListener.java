package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.synchronization;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepExecutionListener;
import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.data.dao.BatchJobAndCampusCourseSynchronizationStatisticDao;
import ch.uzh.extension.campuscourse.data.dao.BatchJobSkippedItemDao;
import ch.uzh.extension.campuscourse.data.entity.BatchJobAndCampusCourseSynchronizationStatistic;
import ch.uzh.extension.campuscourse.data.entity.BatchJobStatistic;
import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Martin Schraner
 */
@Component
@Scope("step")
public class CampusCourseSynchronizationBatchStepExecutionListener extends CampusBatchStepExecutionListener {

	private static final OLog LOG = Tracing.createLoggerFor(CampusCourseSynchronizationBatchStepExecutionListener.class);

	private final BatchJobAndCampusCourseSynchronizationStatisticDao batchJobAndCampusCourseSynchronizationStatisticDao;

	private int chunkCount;
	private long chunkStartTime;

	@Autowired
	public CampusCourseSynchronizationBatchStepExecutionListener(
			DB dbInstance,
			BatchJobSkippedItemDao batchJobSkippedItemDao,
			DaoManager daoManager,
			CampusCourseConfiguration campusCourseConfiguration,
			BatchJobAndCampusCourseSynchronizationStatisticDao batchJobAndCampusCourseSynchronizationStatisticDao) {
		super(dbInstance, batchJobSkippedItemDao, daoManager, campusCourseConfiguration);
		this.batchJobAndCampusCourseSynchronizationStatisticDao = batchJobAndCampusCourseSynchronizationStatisticDao;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		super.beforeStep(stepExecution);
		// Chunk count and duration is being logged for sync step since this may be slow and potentially break timeout
		chunkCount = 0;
	}

	@Override
	protected BatchJobStatistic createAndPersistBatchJobStatistic(StepExecution stepExecution) {
		BatchJobAndCampusCourseSynchronizationStatistic batchJobAndCampusCourseSynchronizationStatistic = new BatchJobAndCampusCourseSynchronizationStatistic(campusBatchStepName, stepExecution);
		batchJobAndCampusCourseSynchronizationStatisticDao.save(batchJobAndCampusCourseSynchronizationStatistic);
		return batchJobAndCampusCourseSynchronizationStatistic;
	}

	@Override
	public void beforeChunk(ChunkContext chunkContext) {
		// Chunk count and duration is being logged for sync step since this may be slow and potentially break timeout
		chunkStartTime = System.currentTimeMillis();
	}

	@Override
	public void afterChunk(ChunkContext chunkContext) {
		// Chunk count and duration is being logged for sync step since this may be slow and potentially break timeout
		int timeout = campusCourseConfiguration.getConnectionPoolTimeout(); // milliseconds!
		if (timeout > 0) {
			chunkCount++;
			long chunkProcessingDuration = System.currentTimeMillis() - chunkStartTime;
			if (chunkProcessingDuration > timeout * 0.9) {
				LOG.warn("Chunk no " + chunkCount + " for campus synchronisation took " + chunkProcessingDuration +
						" ms which is more than 90% of configured database connection pool timeout of " + timeout +
						" sec. Please consider to take action in order to avoid a timeout (increase unreturned connection timeout or decrease chunk size).");
			} else {
				LOG.debug("Chunk no " + chunkCount + " for campus synchronisation took " +
						chunkProcessingDuration + " ms (timeout is " + timeout + " s).");
			}
		}
	}
}
