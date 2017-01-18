package ch.uzh.extension.campuscourse.batchprocessing.sapimport;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepExecutionListener;
import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.data.dao.BatchJobAndSapImportStatisticDao;
import ch.uzh.extension.campuscourse.data.dao.SkipItemDao;
import ch.uzh.extension.campuscourse.data.entity.BatchJobAndSapImportStatistic;
import ch.uzh.extension.campuscourse.data.entity.BatchJobStatistic;
import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import org.olat.core.commons.persistence.DB;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Martin Schraner
 */
@Component
@Scope("step")
public class SapImportBatchStepExecutionListener extends CampusBatchStepExecutionListener {

	private final BatchJobAndSapImportStatisticDao batchJobAndSapImportStatisticDao;

	@Autowired
	public SapImportBatchStepExecutionListener(
			DB dbInstance,
			SkipItemDao skipItemDao,
			DaoManager daoManager,
			CampusCourseConfiguration campusCourseConfiguration,
			BatchJobAndSapImportStatisticDao batchJobAndSapImportStatisticDao) {
		super(dbInstance, skipItemDao, daoManager, campusCourseConfiguration);
		this.batchJobAndSapImportStatisticDao = batchJobAndSapImportStatisticDao;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		super.beforeStep(stepExecution);
		if (campusBatchStepName == CampusBatchStepName.IMPORT_TEXTS) {
			try {
				daoManager.deleteAllTexts();
				dbInstance.commitAndCloseSession();
			} catch (Throwable t) {
				dbInstance.commitAndCloseSession();
				throw t;
			}
		}
			// DISABLED FOR NOW
//		else if (campusBatchStepName == CampusBatchStepName.IMPORT_EVENTS) {
//			try {
//				daoManager.deleteAllEvents();
//				dbInstance.commitAndCloseSession();
//			} catch (Throwable t) {
//				dbInstance.commitAndCloseSession();
//				throw t;
//			}
//		}
	}

	@Override
	protected BatchJobStatistic createAndPersistBatchJobStatistic(StepExecution stepExecution) {
		BatchJobAndSapImportStatistic batchJobAndSapImportStatistic = new BatchJobAndSapImportStatistic(campusBatchStepName, stepExecution);
		batchJobAndSapImportStatisticDao.save(batchJobAndSapImportStatistic);
		return batchJobAndSapImportStatistic;
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		super.afterStep(stepExecution);
		if (campusBatchStepName == CampusBatchStepName.IMPORT_CONTROL_FILE) {
			if (stepExecution.getWriteCount() != campusCourseConfiguration.getMustCompletedImportedFiles()) {
				return ExitStatus.FAILED;
			}
		}
		return null;
	}

}
