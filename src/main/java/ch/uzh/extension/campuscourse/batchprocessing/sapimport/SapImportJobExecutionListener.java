package ch.uzh.extension.campuscourse.batchprocessing.sapimport;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.data.entity.Semester;
import ch.uzh.extension.campuscourse.model.LecturerIdCourseId;
import ch.uzh.extension.campuscourse.model.StudentIdCourseId;
import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Initial date: 2016-07-05<br />
 * @author sev26 (UZH)
 */
@Component
public class SapImportJobExecutionListener implements JobExecutionListener {

	private static final OLog LOG = Tracing.createLoggerFor(SapImportJobExecutionListener.class);

    private final DB dbInstance;
	private final DaoManager daoManager;
	private final CampusCourseConfiguration campusCourseConfiguration;
	private final SapImportControlFileReader sapImportControlFileReader;

	@Autowired
	public SapImportJobExecutionListener(DB dbInstance, DaoManager daoManager, CampusCourseConfiguration campusCourseConfiguration, SapImportControlFileReader sapImportControlFileReader) {
		this.dbInstance = dbInstance;
		this.daoManager = daoManager;
		this.campusCourseConfiguration = campusCourseConfiguration;
		this.sapImportControlFileReader = sapImportControlFileReader;
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
		LOG.info("beforeJob " + jobExecution.getJobInstance().getJobName());
		checkAvailabilityOfRequiredSapImportFilesAndStopBatchJobIfRequired(jobExecution);
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		LOG.info("afterJob " + jobExecution.getJobInstance().getJobName());
		removeNotUpdatedData(jobExecution);
	}

	private void checkAvailabilityOfRequiredSapImportFilesAndStopBatchJobIfRequired(JobExecution jobExecution) {
		try {
			Set<String> importableSapFiles = sapImportControlFileReader.getFilenamesOfImportableSapImportFilesWithCorrectSuffixNotOlderThanOneDay();
			if (!importableSapFiles.containsAll(campusCourseConfiguration.getSapFilesToBeImported())) {
				LOG.error("Some required SAP import files are missing or older than one day!");
				LOG.info("Available import files according to SAP import control file '" +
						campusCourseConfiguration.getSapImportControlFilenameWithPath() + "':");
				for (String filename : sapImportControlFileReader.getAllFilenamesWithDateOfSync()) {
					LOG.info("   " + filename);
				}
				LOG.info("Required import files (not older than one day):");
				List<String> requiredFilenames = new ArrayList<>(campusCourseConfiguration.getSapFilesToBeImported());
				Collections.sort(requiredFilenames);
				for (String filename : requiredFilenames) {
					LOG.info("   " + filename);
				}
				stopBatchJobExecution(jobExecution);
			}
		} catch (IOException | ParseException e) {
			LOG.error("Error when trying to read SAP import control file '" +
					campusCourseConfiguration.getSapImportControlFilenameWithPath() + "': " + e.getMessage());
			stopBatchJobExecution(jobExecution);
		}
	}

	private void stopBatchJobExecution(JobExecution jobExecution) {
		LOG.error("The SAP import batch process will not be executed!");
		jobExecution.stop();
	}

	private void removeNotUpdatedData(JobExecution jobExecution) {

		if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
			return;
		}

		Semester semesterOfCurrentImportProcess = daoManager.getSemesterOfMostRecentCourseImport();
		if (semesterOfCurrentImportProcess == null) {
			LOG.error("Current semester of import process could not be determined. Not updated data cannot be removed. Synchronization may not work properly!");
			return;
		}

		int lecturerCoursesToBeRemoved = daoManager.deleteAllLCBookingTooFarInThePast(jobExecution.getStartTime());
		dbInstance.intermediateCommit();
		List<LecturerIdCourseId> lecturerIdCourseIdsToBeRemoved = daoManager.getAllNotUpdatedLCBookingOfCurrentImportProcess(jobExecution.getStartTime(), semesterOfCurrentImportProcess);
		lecturerCoursesToBeRemoved += lecturerIdCourseIdsToBeRemoved.size();
		LOG.info("LECTURER_COURSES TO BE REMOVED ["  + lecturerCoursesToBeRemoved + "]");
		if (!lecturerIdCourseIdsToBeRemoved.isEmpty()) {
			daoManager.deleteLCBookingByLecturerIdCourseIds(lecturerIdCourseIdsToBeRemoved);
			dbInstance.intermediateCommit();
		}

		int studentCoursesToBeRemoved = daoManager.deleteAllSCBookingTooFarInThePast(jobExecution.getStartTime());
		dbInstance.intermediateCommit();
		List<StudentIdCourseId> studentIdCourseIdsToBeRemoved = daoManager.getAllNotUpdatedSCBookingOfCurrentImportProcess(jobExecution.getStartTime(), semesterOfCurrentImportProcess);
		studentCoursesToBeRemoved += studentIdCourseIdsToBeRemoved.size();
		LOG.info("STUDENT_COURSES TO BE REMOVED [" + studentCoursesToBeRemoved + "]");
		if (!studentIdCourseIdsToBeRemoved.isEmpty()) {
			daoManager.deleteSCBookingByStudentIdCourseIds(studentIdCourseIdsToBeRemoved);
			dbInstance.intermediateCommit();
		}

		List<Long> studentsToBeRemoved = daoManager.getAllStudentsToBeDeleted(jobExecution.getStartTime());
		LOG.info("STUDENTS TO BE REMOVED [" + studentsToBeRemoved.size() + "]");
		if (!studentsToBeRemoved.isEmpty()) {
			daoManager.deleteStudentsAndBookingsByStudentIds(studentsToBeRemoved);
			dbInstance.intermediateCommit();
		}

		List<Long> lecturersToBeRemoved = daoManager.getAllLecturersToBeDeleted(jobExecution.getStartTime());
		LOG.info("LECTURERS TO BE REMOVED [" + lecturersToBeRemoved.size() + "]");
		if (!lecturersToBeRemoved.isEmpty()) {
			daoManager.deleteLecturersAndBookingsByLecturerIds(lecturersToBeRemoved);
			dbInstance.intermediateCommit();
		}

		List<Long> coursesToBeRemoved = daoManager.getAllCoursesToBeDeleted();
		LOG.info("COURSES TO BE REMOVED [" + coursesToBeRemoved.size() + "]");
		if (!coursesToBeRemoved.isEmpty()) {
			daoManager.deleteCoursesAndBookingsByCourseIds(coursesToBeRemoved);
			dbInstance.intermediateCommit();
		}

		List<Long> orgsToBeRemoved = daoManager.getAllOrgsToBeDeleted();
		LOG.info("ORGS TO BE REMOVED [" + orgsToBeRemoved.size() + "]");
		if (!orgsToBeRemoved.isEmpty()) {
			daoManager.deleteOrgByIds(orgsToBeRemoved);
			dbInstance.intermediateCommit();
		}

		dbInstance.closeSession();
	}
}
