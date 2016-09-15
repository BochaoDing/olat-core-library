package ch.uzh.campus.connectors;

import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.data.LecturerIdCourseId;
import ch.uzh.campus.data.StudentIdCourseId;
import ch.uzh.campus.metric.CampusNotifier;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Initial date: 2016-07-05<br />
 * @author sev26 (UZH)
 */
public class CampusImportJobInterceptor implements JobExecutionListener {

	private static final OLog LOG = Tracing.createLoggerFor(CampusImportJobInterceptor.class);

    private final DB dbInstance;
	private final DaoManager daoManager;
	private final CampusNotifier campusNotifier;

	@Autowired
	public CampusImportJobInterceptor(DB dbInstance, DaoManager daoManager, CampusNotifier campusNotifier) {
		this.dbInstance = dbInstance;
		this.daoManager = daoManager;
		this.campusNotifier = campusNotifier;
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
		LOG.info("beforeJob " + jobExecution.getJobInstance().getJobName());
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		LOG.info("afterJob " + jobExecution.getJobInstance().getJobName());
		removeOldDataIfExist(jobExecution);
		campusNotifier.notifyJobExecution(jobExecution);
	}

	/**
	 * Delegates the actual deletion of old data to the {@link DaoManager} in
	 * the case of a successful job processing.
	 *
	 * @param jobExecution
	 *            the JobExecution
	 */
	private void removeOldDataIfExist(JobExecution jobExecution) {
		if ((BatchStatus.COMPLETED == jobExecution.getStatus()) == false) {
			return;
		}

		int lecturerCoursesToBeRemoved = daoManager.deleteAllLCBookingTooFarInThePast(jobExecution.getStartTime());
		dbInstance.intermediateCommit();
		List<LecturerIdCourseId> lecturerIdCourseIdsToBeRemoved = daoManager.getAllNotUpdatedLCBookingOfCurrentSemester(jobExecution.getStartTime());
		lecturerCoursesToBeRemoved += lecturerIdCourseIdsToBeRemoved.size();
		LOG.info("LECTURER_COURSES TO BE REMOVED ["  + lecturerCoursesToBeRemoved + "]");
		if (!lecturerIdCourseIdsToBeRemoved.isEmpty()) {
			daoManager.deleteLCBookingByLecturerIdCourseIds(lecturerIdCourseIdsToBeRemoved);
			dbInstance.intermediateCommit();
		}

		int studentCoursesToBeRemoved = daoManager.deleteAllSCBookingTooFarInThePast(jobExecution.getStartTime());
		dbInstance.intermediateCommit();
		List<StudentIdCourseId> studentIdCourseIdsToBeRemoved = daoManager.getAllNotUpdatedSCBookingOfCurrentSemester(jobExecution.getStartTime());
		studentCoursesToBeRemoved += studentIdCourseIdsToBeRemoved.size();
		LOG.info("STUDENT_COURSES TO BE REMOVED [" + studentCoursesToBeRemoved + "]");
		if (!studentIdCourseIdsToBeRemoved.isEmpty()) {
			daoManager.deleteSCBookingByStudentIdCourseIds(studentIdCourseIdsToBeRemoved);
			dbInstance.intermediateCommit();
		}

		List<Long> studentsToBeRemoved = daoManager.getAllStudentsToBeDeleted();
		LOG.info("STUDENTS TO BE REMOVED [" + studentsToBeRemoved.size() + "]");
		if (!studentsToBeRemoved.isEmpty()) {
			daoManager.deleteStudentsAndBookingsByStudentIds(studentsToBeRemoved);
			dbInstance.intermediateCommit();
		}

		List<Long> lecturersToBeRemoved = daoManager.getAllLecturersToBeDeleted();
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