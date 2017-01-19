package ch.uzh.extension.campuscourse.data.entity;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import java.util.Date;

import static ch.uzh.extension.campuscourse.data.entity.BatchJobAndSapImportStatistic.*;

/**
 * @author Martin Schraner
 */
@Entity
@DiscriminatorValue("SAP_IMPORT")
@Repository
@Scope("prototype")
@NamedQueries({
		@NamedQuery(name = GET_NUMBER_OF_SUCCESSFULLY_PROCESSED_IMPORT_FILES_OF_LAST_SAP_IMPORT, query = "select count(s) from BatchJobAndSapImportStatistic s where " +
				"s.campusBatchStepName != :importControlFile " +
				"and s.status = :status " +
				"and s.startTime >= (select max(s2.startTime) from BatchJobAndSapImportStatistic s2 where s2.status = :status and s2.campusBatchStepName = :importControlFile)"),
		@NamedQuery(name = GET_NUMBER_OF_SUCCESSFULLY_PROCESSED_IMPORT_FILES_OF_SAP_IMPORT_OF_TODAY, query = "select count(s) from BatchJobAndSapImportStatistic s where " +
				"s.campusBatchStepName != :importControlFile " +
				"and s.status = :status " +
				"and s.startTime >= :midnight"),
		@NamedQuery(name = GET_START_TIME_OF_MOST_RECENT_COMPLETED_COURSE_IMPORT, query = "select max(s.startTime) from BatchJobAndSapImportStatistic s where " +
				"s.campusBatchStepName = :importCourses " +
				"and s.status = :status")
})
public class BatchJobAndSapImportStatistic extends BatchJobStatistic {

	public static final String GET_NUMBER_OF_SUCCESSFULLY_PROCESSED_IMPORT_FILES_OF_LAST_SAP_IMPORT = "getNumberOfSuccessfullyProcessedImportFilesOfLastSapImport";
	public static final String GET_NUMBER_OF_SUCCESSFULLY_PROCESSED_IMPORT_FILES_OF_SAP_IMPORT_OF_TODAY = "getNumberOfSuccessfullyProcessedImportFilesOfSapImportOfToday";
	public static final String GET_START_TIME_OF_MOST_RECENT_COMPLETED_COURSE_IMPORT = "getStartTimeOfMostRecentCompletedCourseImport";

	public BatchJobAndSapImportStatistic() {
	}

	public BatchJobAndSapImportStatistic(CampusBatchStepName campusBatchStepName, BatchStatus status, Date startTime,
										 Date endTime, int readCount, int writeCount, int readSkipCount,
										 int writeSkipCount, int processSkipCount, int commitCount, int rollbackCount) {
		super(campusBatchStepName, status, startTime, endTime, readCount, writeCount, readSkipCount, writeSkipCount, processSkipCount, commitCount, rollbackCount);
	}

	public BatchJobAndSapImportStatistic(CampusBatchStepName campusBatchStepName, StepExecution stepExecution) {
		super(campusBatchStepName, stepExecution);
	}
}
