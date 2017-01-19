package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import ch.uzh.extension.campuscourse.data.entity.BatchJobAndSapImportStatistic;
import org.apache.commons.lang.time.DateUtils;
import org.olat.core.commons.persistence.DB;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static ch.uzh.extension.campuscourse.data.entity.BatchJobAndSapImportStatistic.GET_NUMBER_OF_SUCCESSFULLY_PROCESSED_IMPORT_FILES_OF_SAP_IMPORT_OF_TODAY;
import static ch.uzh.extension.campuscourse.data.entity.BatchJobAndSapImportStatistic.GET_NUMBER_OF_SUCCESSFULLY_PROCESSED_IMPORT_FILES_OF_LAST_SAP_IMPORT;

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

    public int getNumberOfSuccessfullyProcessedImportFilesOfLastSapImport() {
        return (int) (long) dbInstance.getCurrentEntityManager()
                .createNamedQuery(GET_NUMBER_OF_SUCCESSFULLY_PROCESSED_IMPORT_FILES_OF_LAST_SAP_IMPORT, Long.class)
                .setParameter("status", BatchStatus.COMPLETED)
                .setParameter("importControlFile", CampusBatchStepName.IMPORT_CONTROL_FILE)
                .getSingleResult();
    }

    public int getNumberOfSuccessfullyProcessedImportFilesOfSapImportOfToday() {
        Date midnight = DateUtils.truncate(new Date(), Calendar.DATE);
        return (int) (long) dbInstance.getCurrentEntityManager()
                .createNamedQuery(GET_NUMBER_OF_SUCCESSFULLY_PROCESSED_IMPORT_FILES_OF_SAP_IMPORT_OF_TODAY, Long.class)
                .setParameter("status", BatchStatus.COMPLETED)
                .setParameter("importControlFile", CampusBatchStepName.IMPORT_CONTROL_FILE)
                .setParameter("midnight", midnight)
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
