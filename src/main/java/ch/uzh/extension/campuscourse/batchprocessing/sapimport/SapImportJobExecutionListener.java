package ch.uzh.extension.campuscourse.batchprocessing.sapimport;

import ch.uzh.extension.campuscourse.data.entity.Semester;
import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import ch.uzh.extension.campuscourse.model.LecturerIdCourseId;
import ch.uzh.extension.campuscourse.model.StudentIdCourseId;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initial date: 2016-07-05<br />
 * @author sev26 (UZH)
 */
@Component
public class SapImportJobExecutionListener implements JobExecutionListener {

	private static final OLog LOG = Tracing.createLoggerFor(SapImportJobExecutionListener.class);

    private final DB dbInstance;
	private final DaoManager daoManager;

	@Autowired
	public SapImportJobExecutionListener(DB dbInstance, DaoManager daoManager) {
		this.dbInstance = dbInstance;
		this.daoManager = daoManager;
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
		LOG.info("beforeJob " + jobExecution.getJobInstance().getJobName());
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		LOG.info("afterJob " + jobExecution.getJobInstance().getJobName());
		removeNotUpdatedData(jobExecution);
	}

	private void removeNotUpdatedData(JobExecution jobExecution) {

		if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
			return;
		}

		Semester semesterOfCurrentImportProcess = daoManager.getSemesterOfMostRecentCourseImport();
		if (semesterOfCurrentImportProcess == null) {
			LOG.warn("Current semester of import process could not be determined. Not updated data cannot be removed. Synchronization may not work properly!");
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
