package ch.uzh.extension.campuscourse.data.entity;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.Calendar;
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
		@NamedQuery(name = GET_NUMBER_OF_COMPLETED_BATCH_STEPS_OF_LAST_SAP_IMPORT, query = "select count(s) from BatchJobAndSapImportStatistic s where " +
				"s.status = :status " +
				"and s.startTime >= (select max(s2.startTime) from BatchJobAndSapImportStatistic s2 where s2.status = :status and s2.campusBatchStepName = :importOrgs)"),
		@NamedQuery(name = GET_NUMBER_OF_COMPLETED_BATCH_STEPS_OF_SAP_IMPORT_OF_TODAY, query = "select count(s) from BatchJobAndSapImportStatistic s where " +
				"s.status = :status " +
				"and s.startTime >= :lastMidnight " +
				"and s.startTime < :nextMidnight"),
		@NamedQuery(name = GET_START_TIME_OF_MOST_RECENT_COMPLETED_COURSE_IMPORT, query = "select max(s.startTime) from BatchJobAndSapImportStatistic s where " +
				"s.campusBatchStepName = :importCourses " +
				"and s.status = :status")
})
public class BatchJobAndSapImportStatistic extends BatchJobStatistic {

	public static final String GET_NUMBER_OF_COMPLETED_BATCH_STEPS_OF_LAST_SAP_IMPORT = "getNumberOfCompletedBatchStepsOfLastSapImport";
	public static final String GET_NUMBER_OF_COMPLETED_BATCH_STEPS_OF_SAP_IMPORT_OF_TODAY = "getNumberOfCompletedBatchStepsOfSapImportOfToday";
	public static final String GET_START_TIME_OF_MOST_RECENT_COMPLETED_COURSE_IMPORT = "getStartTimeOfMostRecentCompletedCourseImport";

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_of_sync")
	private Calendar dateOfSync;

	@Column(name = "next")
	private Boolean next;

	public BatchJobAndSapImportStatistic() {
	}

	public BatchJobAndSapImportStatistic(CampusBatchStepName campusBatchStepName, BatchStatus status, Date startTime,
										 Date endTime, int readCount, int writeCount, int readSkipCount,
										 int writeSkipCount, int processSkipCount, int commitCount, int rollbackCount,
										 Calendar dateOfSync, Boolean next) {
		super(campusBatchStepName, status, startTime, endTime, readCount, writeCount, readSkipCount, writeSkipCount, processSkipCount, commitCount, rollbackCount);
		this.dateOfSync = dateOfSync;
		this.next = next;
	}

	public BatchJobAndSapImportStatistic(CampusBatchStepName campusBatchStepName, StepExecution stepExecution,
										 Calendar dateOfSync, Boolean next) {
		super(campusBatchStepName, stepExecution);
		this.dateOfSync = dateOfSync;
		this.next = next;
	}

	public Calendar getDateOfSync() {
		return dateOfSync;
	}

	public void setDateOfSync(Calendar dateOfSync) {
		this.dateOfSync = dateOfSync;
	}

	public Boolean isNext() {
		return next;
	}

	public void setNext(Boolean next) {
		this.next = next;
	}
}
