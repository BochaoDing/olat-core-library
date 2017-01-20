package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import ch.uzh.extension.campuscourse.data.entity.BatchJobAndSapImportStatistic;
import org.olat.core.commons.persistence.DB;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static ch.uzh.extension.campuscourse.data.entity.BatchJobAndSapImportStatistic.GET_NUMBER_OF_COMPLETED_BATCH_STEPS_OF_LAST_SAP_IMPORT;
import static ch.uzh.extension.campuscourse.data.entity.BatchJobAndSapImportStatistic.GET_NUMBER_OF_COMPLETED_BATCH_STEPS_OF_SAP_IMPORT_OF_TODAY;

@Repository
public class BatchJobAndSapImportStatisticDao {

    private final DB dbInstance;

    @Autowired
    public BatchJobAndSapImportStatisticDao(DB dbInstance) {
        this.dbInstance = dbInstance;
    }

    public void save(BatchJobAndSapImportStatistic batchJobAndSapImportStatistic) {
        dbInstance.saveObject(batchJobAndSapImportStatistic);
    }

    public void save(List<BatchJobAndSapImportStatistic> batchJobAndSapImportStatistics) {
        batchJobAndSapImportStatistics.forEach(this::save);
    }

    public int getNumberOfCompletedBatchStepsOfLastSapImport() {
        return (int) (long) dbInstance.getCurrentEntityManager()
                .createNamedQuery(GET_NUMBER_OF_COMPLETED_BATCH_STEPS_OF_LAST_SAP_IMPORT, Long.class)
                .setParameter("status", BatchStatus.COMPLETED)
                .setParameter("importOrgs", CampusBatchStepName.IMPORT_ORGS)
                .getSingleResult();
    }

    public int getNumberOfCompletedBatchStepsOfSapImportOfToday() {
		Calendar lastMidnight = new GregorianCalendar();
		lastMidnight.set(Calendar.HOUR_OF_DAY, 0);
		lastMidnight.set(Calendar.MINUTE, 0);
		lastMidnight.set(Calendar.SECOND, 0);
		lastMidnight.set(Calendar.MILLISECOND, 0);
		Calendar nextMidnight = (Calendar) lastMidnight.clone();
		nextMidnight.add(Calendar.DAY_OF_YEAR, 1);
        return (int) (long) dbInstance.getCurrentEntityManager()
                .createNamedQuery(GET_NUMBER_OF_COMPLETED_BATCH_STEPS_OF_SAP_IMPORT_OF_TODAY, Long.class)
                .setParameter("status", BatchStatus.COMPLETED)
                .setParameter("lastMidnight", lastMidnight.getTime())
				.setParameter("nextMidnight", nextMidnight.getTime())
                .getSingleResult();
    }

    Date getStartTimeOfMostRecentCompletedCourseImport() {
        try {
            return dbInstance.getCurrentEntityManager()
                    .createNamedQuery(BatchJobAndSapImportStatistic.GET_START_TIME_OF_MOST_RECENT_COMPLETED_COURSE_IMPORT, Date.class)
					.setParameter("importCourses", CampusBatchStepName.IMPORT_COURSES)
					.setParameter("status", BatchStatus.COMPLETED)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
