package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.usermapping;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepExecutionListener;
import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.data.dao.SkipItemDao;
import ch.uzh.extension.campuscourse.data.dao.BatchJobAndUserMappingStatisticDao;
import ch.uzh.extension.campuscourse.data.entity.BatchJobStatistic;
import ch.uzh.extension.campuscourse.data.entity.BatchJobAndUserMappingStatistic;
import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import org.olat.core.commons.persistence.DB;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Martin Schraner
 */
@Component
@Scope("step")
public class UserMappingBatchStepExecutionListener extends CampusBatchStepExecutionListener {

	private final BatchJobAndUserMappingStatisticDao batchJobAndUserMappingStatisticDao;

	@Autowired
	public UserMappingBatchStepExecutionListener(
			DB dbInstance,
			SkipItemDao skipItemDao,
			DaoManager daoManager,
			CampusCourseConfiguration campusCourseConfiguration,
			BatchJobAndUserMappingStatisticDao batchJobAndUserMappingStatisticDao) {
		super(dbInstance, skipItemDao, daoManager, campusCourseConfiguration);
		this.batchJobAndUserMappingStatisticDao = batchJobAndUserMappingStatisticDao;
	}

	@Override
	protected BatchJobStatistic createAndPersistBatchJobStatistic(StepExecution stepExecution) {
		BatchJobAndUserMappingStatistic batchJobAndUserMappingStatistic = new BatchJobAndUserMappingStatistic(campusBatchStepName, stepExecution);
		batchJobAndUserMappingStatisticDao.save(batchJobAndUserMappingStatistic);
		return batchJobAndUserMappingStatistic;
	}
}
