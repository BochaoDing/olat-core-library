package ch.uzh.extension.campuscourse.data.entity;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.usermapping.UserMappingStatistic;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.persistence.*;

import java.util.Date;

import static ch.uzh.extension.campuscourse.data.entity.BatchJobAndUserMappingStatistic.GET_LAST_CREATED_USER_MAPPING_STATISTIC_FOR_CAMPUS_BATCH_STEP_NAME;

/**
 * @author Martin Schraner
 */
@Entity
@DiscriminatorValue("USER_MAPPING")
@Repository
@Scope("prototype")
@NamedQueries({
		@NamedQuery(name = GET_LAST_CREATED_USER_MAPPING_STATISTIC_FOR_CAMPUS_BATCH_STEP_NAME, query = "select s from BatchJobAndUserMappingStatistic s where " +
				"s.campusBatchStepName = :campusBatchStepName " +
				"and s.startTime >= (select max(s2.startTime) from BatchJobAndUserMappingStatistic s2 where s2.campusBatchStepName = :campusBatchStepName)")
})
public class BatchJobAndUserMappingStatistic extends BatchJobStatistic {

	public static final String GET_LAST_CREATED_USER_MAPPING_STATISTIC_FOR_CAMPUS_BATCH_STEP_NAME = "getLastCreatedUserMappingStatisticForCampusBatchStepName";

	@Column(name = "already_mapped")
	private int alreadyMapped;

	@Column(name = "new_mapping_by_email")
	private int newMappingByEmail;

	@Column(name = "new_mapping_by_matriculation_number")
	private int newMappingByMatriculationNumber;

	@Column(name = "new_mapping_by_personal_number")
	private int newMappingByPersonalNumber;

	@Column(name = "new_mapping_by_additional_personal_number")
	private int newMappingByAdditionalPersonalNumber;

	@Column(name = "could_be_mapped_manually")
	private int couldBeMappedManually;

	@Column(name = "could_not_map")
	private int couldNotMap;

	public BatchJobAndUserMappingStatistic() {
	}

	public BatchJobAndUserMappingStatistic(CampusBatchStepName campusBatchStepName, BatchStatus status, Date startTime,
										   Date endTime, int readCount, int writeCount, int readSkipCount,
										   int writeSkipCount, int processSkipCount, int commitCount, int rollbackCount,
										   int alreadyMapped, int newMappingByEmail, int newMappingByMatriculationNumber,
										   int newMappingByPersonalNumber, int newMappingByAdditionalPersonalNumber,
										   int couldBeMappedManually, int couldNotMap) {
		super(campusBatchStepName, status, startTime, endTime, readCount, writeCount, readSkipCount, writeSkipCount, processSkipCount, commitCount, rollbackCount);
		this.alreadyMapped = alreadyMapped;
		this.newMappingByEmail = newMappingByEmail;
		this.newMappingByMatriculationNumber = newMappingByMatriculationNumber;
		this.newMappingByPersonalNumber = newMappingByPersonalNumber;
		this.newMappingByAdditionalPersonalNumber = newMappingByAdditionalPersonalNumber;
		this.couldBeMappedManually = couldBeMappedManually;
		this.couldNotMap = couldNotMap;
	}

	public BatchJobAndUserMappingStatistic(CampusBatchStepName campusBatchStepName, StepExecution stepExecution) {
		super(campusBatchStepName, stepExecution);
	}

	public void setUserMappingStatistic(UserMappingStatistic userMappingStatistic) {
		alreadyMapped = userMappingStatistic.getAlreadyMapped().get();
		newMappingByEmail = userMappingStatistic.getNewMappingByEmail().get();
		newMappingByMatriculationNumber = userMappingStatistic.getNewMappingByMatriculationNumber().get();
		newMappingByPersonalNumber = userMappingStatistic.getNewMappingByPersonalNumber().get();
		newMappingByAdditionalPersonalNumber = userMappingStatistic.getNewMappingByAdditionalPersonalNumber().get();
		couldBeMappedManually = userMappingStatistic.getCouldBeMappedManually().get();
		couldNotMap = userMappingStatistic.getCouldNotMap().get();
	}

	public int getAlreadyMapped() {
		return alreadyMapped;
	}

	public void setAlreadyMapped(int alreadyMapped) {
		this.alreadyMapped = alreadyMapped;
	}

	public int getNewMappingByEmail() {
		return newMappingByEmail;
	}

	public void setNewMappingByEmail(int newMappingByEmail) {
		this.newMappingByEmail = newMappingByEmail;
	}

	public int getNewMappingByMatriculationNumber() {
		return newMappingByMatriculationNumber;
	}

	public void setNewMappingByMatriculationNumber(int newMappingByMatriculationNumber) {
		this.newMappingByMatriculationNumber = newMappingByMatriculationNumber;
	}

	public int getNewMappingByPersonalNumber() {
		return newMappingByPersonalNumber;
	}

	public void setNewMappingByPersonalNumber(int getNewMappingByPersonalNumber) {
		this.newMappingByPersonalNumber = getNewMappingByPersonalNumber;
	}

	public int getNewMappingByAdditionalPersonalNumber() {
		return newMappingByAdditionalPersonalNumber;
	}

	public void setNewMappingByAdditionalPersonalNumber(int getNewMappingByAdditionalPersonalNumber) {
		this.newMappingByAdditionalPersonalNumber = getNewMappingByAdditionalPersonalNumber;
	}

	public int getCouldBeMappedManually() {
		return couldBeMappedManually;
	}

	public void setCouldBeMappedManually(int couldBeMappedManually) {
		this.couldBeMappedManually = couldBeMappedManually;
	}

	public int getCouldNotMap() {
		return couldNotMap;
	}

	public void setCouldNotMap(int couldNotMap) {
		this.couldNotMap = couldNotMap;
	}
}

