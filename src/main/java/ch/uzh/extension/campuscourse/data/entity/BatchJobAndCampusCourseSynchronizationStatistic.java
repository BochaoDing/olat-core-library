package ch.uzh.extension.campuscourse.data.entity;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.synchronization.CampusCourseSynchronizationStatistic;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.Date;

import static ch.uzh.extension.campuscourse.data.entity.BatchJobAndCampusCourseSynchronizationStatistic.GET_LAST_CREATED_CAMPUS_COURSE_SYNCHRONIZATION_STATISTIC;

/**
 * @author Martin Schraner
 */
@Entity
@DiscriminatorValue("CAMPUS_COURSE_SYNCHRONIZATION")
@Repository
@Scope("prototype")
@NamedQueries({
		@NamedQuery(name = GET_LAST_CREATED_CAMPUS_COURSE_SYNCHRONIZATION_STATISTIC, query = "select s from BatchJobAndCampusCourseSynchronizationStatistic s where " +
				"s.startTime >= (select max(s2.startTime) from BatchJobAndCampusCourseSynchronizationStatistic s2)")
})
public class BatchJobAndCampusCourseSynchronizationStatistic extends BatchJobStatistic {

	public static final String GET_LAST_CREATED_CAMPUS_COURSE_SYNCHRONIZATION_STATISTIC = "getLastCreatedCampusCourseSynchronizationStatistic";

	@Column(name = "added_coaches")
	private int addedCoaches;

	@Column(name = "removed_coaches")
	private int removedCoaches;

	@Column(name = "added_participants")
	private int addedParticipants;

	@Column(name = "removed_participants")
	private int removedParticipants;

	public BatchJobAndCampusCourseSynchronizationStatistic() {
	}

	public BatchJobAndCampusCourseSynchronizationStatistic(CampusBatchStepName campusBatchStepName, BatchStatus status,
														   Date startTime, Date endTime, int readCount, int writeCount,
														   int readSkipCount, int writeSkipCount, int processSkipCount,
														   int commitCount, int rollbackCount, int addedCoaches,
														   int removedCoaches, int addedParticipants, int removedParticipants) {
		super(campusBatchStepName, status, startTime, endTime, readCount, writeCount, readSkipCount, writeSkipCount, processSkipCount, commitCount, rollbackCount);
		this.addedCoaches = addedCoaches;
		this.removedCoaches = removedCoaches;
		this.addedParticipants = addedParticipants;
		this.removedParticipants = removedParticipants;
	}

	public BatchJobAndCampusCourseSynchronizationStatistic(CampusBatchStepName campusBatchStepName, StepExecution stepExecution) {
		super(campusBatchStepName, stepExecution);
	}

	public void setCampusCourseSynchronizationStatistic(CampusCourseSynchronizationStatistic campusCourseSynchronizationStatistic) {
		addedCoaches = campusCourseSynchronizationStatistic.getAddedCoaches().get();
		removedCoaches = campusCourseSynchronizationStatistic.getRemovedCoaches().get();
		addedParticipants = campusCourseSynchronizationStatistic.getAddedParticipants().get();
		removedParticipants = campusCourseSynchronizationStatistic.getRemovedParticipants().get();
	}

	public int getAddedCoaches() {
		return addedCoaches;
	}

	public void setAddedCoaches(int addedCoaches) {
		this.addedCoaches = addedCoaches;
	}

	public int getRemovedCoaches() {
		return removedCoaches;
	}

	public void setRemovedCoaches(int removedCoaches) {
		this.removedCoaches = removedCoaches;
	}

	public int getAddedParticipants() {
		return addedParticipants;
	}

	public void setAddedParticipants(int addedParticipants) {
		this.addedParticipants = addedParticipants;
	}

	public int getRemovedParticipants() {
		return removedParticipants;
	}

	public void setRemovedParticipants(int removedParticipants) {
		this.removedParticipants = removedParticipants;
	}
}

