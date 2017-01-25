package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import ch.uzh.extension.campuscourse.data.entity.BatchJobAndUserMappingStatistic;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static ch.uzh.extension.campuscourse.data.entity.BatchJobAndUserMappingStatistic.GET_LAST_CREATED_USER_MAPPING_STATISTIC_FOR_CAMPUS_BATCH_STEP_NAME;

@Repository
public class BatchJobAndUserMappingStatisticDao {

    private final DB dbInstance;

    @Autowired
    public BatchJobAndUserMappingStatisticDao(DB dbInstance) {
        this.dbInstance = dbInstance;
    }

    public void save(BatchJobAndUserMappingStatistic batchJobAndUserMappingStatistic) {
        dbInstance.saveObject(batchJobAndUserMappingStatistic);
    }

	public void save(List<BatchJobAndUserMappingStatistic> batchJobAndUserMappingStatistics) {
		batchJobAndUserMappingStatistics.forEach(this::save);
	}

    public BatchJobAndUserMappingStatistic getLastCreatedUserMappingStatisticForCampusBatchStepName(CampusBatchStepName campusBatchStepName) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery(GET_LAST_CREATED_USER_MAPPING_STATISTIC_FOR_CAMPUS_BATCH_STEP_NAME, BatchJobAndUserMappingStatistic.class)
				.setParameter("campusBatchStepName", campusBatchStepName)
				.getSingleResult();
    }
}
