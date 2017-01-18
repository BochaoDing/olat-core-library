package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.data.entity.BatchJobAndCampusCourseSynchronizationStatistic;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static ch.uzh.extension.campuscourse.data.entity.BatchJobAndCampusCourseSynchronizationStatistic.GET_LAST_CREATED_CAMPUS_COURSE_SYNCHRONIZATION_STATISTIC;

@Repository
public class BatchJobAndCampusCourseSynchronizationStatisticDao {

    private final DB dbInstance;

    @Autowired
    public BatchJobAndCampusCourseSynchronizationStatisticDao(DB dbInstance) {
        this.dbInstance = dbInstance;
    }

    public void save(BatchJobAndCampusCourseSynchronizationStatistic batchJobAndCampusCourseSynchronizationStatistic) {
        dbInstance.saveObject(batchJobAndCampusCourseSynchronizationStatistic);
    }

	public void save(List<BatchJobAndCampusCourseSynchronizationStatistic> batchJobAndCampusCourseSynchronizationStatistics) {
		batchJobAndCampusCourseSynchronizationStatistics.forEach(this::save);
	}

    public BatchJobAndCampusCourseSynchronizationStatistic getLastCreatedBatchJobAndCampusCourseSynchronizationStatistic() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(GET_LAST_CREATED_CAMPUS_COURSE_SYNCHRONIZATION_STATISTIC, BatchJobAndCampusCourseSynchronizationStatistic.class)
                .getSingleResult();
    }
}
