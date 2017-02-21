package ch.uzh.extension.campuscourse.data.dao;


import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.data.entity.*;
import ch.uzh.extension.campuscourse.model.StudentIdCourseId;
import ch.uzh.extension.campuscourse.model.StudentIdCourseIdDateOfLatestImport;
import ch.uzh.extension.campuscourse.util.DateUtil;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;

/**
 * @author Martin Schraner
 */
@Repository
public class StudentCourseDao {

    private static final OLog LOG = Tracing.createLoggerFor(StudentCourseDao.class);

    private final CampusCourseConfiguration campusCourseConfiguration;
    private final DB dbInstance;
    private final CourseDao courseDao;

    @Autowired
    public StudentCourseDao(CampusCourseConfiguration campusCourseConfiguration, DB dbInstance, CourseDao courseDao) {
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.dbInstance = dbInstance;
		this.courseDao = courseDao;
	}

    public void save(StudentCourse studentCourse) {
		studentCourse.setDateOfFirstImport(studentCourse.getDateOfLatestImport());
        dbInstance.saveObject(studentCourse);
        studentCourse.getStudent().getStudentCourses().add(studentCourse);
        studentCourse.getCourse().getStudentCourses().add(studentCourse);
    }

    public void save(StudentIdCourseIdDateOfLatestImport studentIdCourseIdDateOfLatestImport) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        Student student = em.find(Student.class, studentIdCourseIdDateOfLatestImport.getStudentId());
        if (student == null) {
            logStudentNotFoundAndThrowException(studentIdCourseIdDateOfLatestImport);
			return;
        }
		Course course = em.find(Course.class, studentIdCourseIdDateOfLatestImport.getCourseId());
		if (course == null) {
			logStudentNotFoundAndThrowException(studentIdCourseIdDateOfLatestImport);
			return;
		}
        StudentCourse studentCourse = new StudentCourse(student, course, studentIdCourseIdDateOfLatestImport.getDateOfLatestImport());
        save(studentCourse);
    }

    public void save(List<StudentIdCourseIdDateOfLatestImport> studentIdCourseIdDateOfLatestImports) {
        studentIdCourseIdDateOfLatestImports.forEach(this::save);
    }

	void saveOrUpdate(StudentCourse lecturerCourse) {
		StudentCourse studentCourseFound = getStudentCourseById(lecturerCourse.getStudent().getId(), lecturerCourse.getCourse().getId());
		if (studentCourseFound != null) {
			lecturerCourse.mergeImportedAttributesInto(studentCourseFound);
		} else {
			save(lecturerCourse);
		}
	}

	/**
	 * For efficient insert or update without loading student and course.
	 * NB: Inserted or updated studentCourse must not be used before reloading it from the database!
	 */
	private void saveWithoutBidirectionalUpdate(StudentCourse studentCourse) {
		studentCourse.setDateOfFirstImport(studentCourse.getDateOfLatestImport());
		dbInstance.saveObject(studentCourse);
	}

    /**
     * For efficient insert or update without loading student and course.
     * NB: Inserted or updated studentCourse must not be used before reloading it from the database!
     */
    public void saveOrUpdateWithoutBidirectionalUpdate(StudentIdCourseIdDateOfLatestImport studentIdCourseIdDateOfLatestImport) {
		StudentCourse studentCourseFound = getStudentCourseById(studentIdCourseIdDateOfLatestImport.getStudentId(), studentIdCourseIdDateOfLatestImport.getCourseId());
		if (studentCourseFound != null) {
			studentIdCourseIdDateOfLatestImport.mergeImportedAttributesInto(studentCourseFound);
		} else {
			EntityManager em = dbInstance.getCurrentEntityManager();
			Student student = em.getReference(Student.class, studentIdCourseIdDateOfLatestImport.getStudentId());
			try {
				// To get a (potential) EntityNotFoundException the object has to be accessed
				//noinspection ResultOfMethodCallIgnored
				student.getId();
			} catch (EntityNotFoundException e) {
				logStudentNotFoundAndThrowException(studentIdCourseIdDateOfLatestImport);
				return;
			}
			Course course = em.getReference(Course.class, studentIdCourseIdDateOfLatestImport.getCourseId());
			try {
				// To get a (potential) EntityNotFoundException the object has to be accessed
				//noinspection ResultOfMethodCallIgnored
				course.getId();
			} catch (EntityNotFoundException e) {
				logCourseNotFoundAndThrowException(studentIdCourseIdDateOfLatestImport);
				return;
			}
			StudentCourse studentCourse = new StudentCourse(student, course, studentIdCourseIdDateOfLatestImport.getDateOfLatestImport());
			saveWithoutBidirectionalUpdate(studentCourse);
		}
    }

    private void logStudentNotFoundAndThrowException(StudentIdCourseIdDateOfLatestImport studentIdCourseIdDateOfLatestImport) {
        String warningMessage = "No student found with id " + studentIdCourseIdDateOfLatestImport.getStudentId();
        warningMessage = warningMessage + ". Skipping entry " + studentIdCourseIdDateOfLatestImport.getStudentId() + ", " + studentIdCourseIdDateOfLatestImport.getCourseId() + " for table ck_student_course.";
        // Here we only log on the debug level to avoid duplicated warnings (LOG.warn is already called by CampusWriter)
        LOG.debug(warningMessage);
        throw new EntityNotFoundException(warningMessage);
    }

	private void logCourseNotFoundAndThrowException(StudentIdCourseIdDateOfLatestImport studentIdCourseIdDateOfLatestImport) {
		String warningMessage = "No course found with id " + studentIdCourseIdDateOfLatestImport.getCourseId();
		warningMessage = warningMessage + ". Skipping entry " + studentIdCourseIdDateOfLatestImport.getStudentId() + ", " + studentIdCourseIdDateOfLatestImport.getCourseId() + " for table ck_student_course.";
		// Here we only log on the debug level to avoid duplicated warnings (LOG.warn is already called by CampusWriter)
		LOG.debug(warningMessage);
		throw new EntityNotFoundException(warningMessage);
	}

    StudentCourse getStudentCourseById(Long studentId, Long courseId) {
        return dbInstance.getCurrentEntityManager().find(StudentCourse.class, new StudentCourseId(studentId, courseId));
    }

    public List<StudentIdCourseId> getAllNotUpdatedSCBookingOfCurrentImportProcess(Date date, Semester semesterOfCurrentImportProcess) {
        // Subtract one second since modifiedDate (used in query) is rounded to seconds
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(StudentCourse.GET_ALL_NOT_UPDATED_SC_BOOKING_OF_CURRENT_IMPORT_PROCESS, StudentIdCourseId.class)
                .setParameter("lastDateOfImport", DateUtil.addSecondsToDate(date, -1))
                .setParameter("semesterIdOfCurrentImportProcess", semesterOfCurrentImportProcess.getId())
                .getResultList();
    }

    public void delete(StudentCourse studentCourse) {
        deleteStudentCourseBidirectionally(studentCourse);
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteAllSCBookingOfNotContinuedCoursesTooFarInThePastAsBulkDelete(Date date) {
		List<Long> courseIdsToBeExcluded = courseDao.getIdsOfContinuedCoursesTooFarInThePast(date);
		if (courseIdsToBeExcluded.isEmpty()) {
			// JPA would crash if courseIdsToBeExcluded was empty, so we have to use a query without courseIdsToBeExcluded
			return dbInstance.getCurrentEntityManager()
					.createNamedQuery(StudentCourse.DELETE_ALL_SC_BOOKING_TOO_FAR_IN_THE_PAST)
					.setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
					.executeUpdate();
		} else {
			return dbInstance.getCurrentEntityManager()
					.createNamedQuery(StudentCourse.DELETE_ALL_SC_BOOKING_TOO_FAR_IN_THE_PAST_EXCEPT_FOR_COURSES_TO_BE_EXCLUDED)
					.setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
					.setParameter("courseIdsToBeExcluded", courseIdsToBeExcluded)
					.executeUpdate();
		}
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteByStudentIdsAsBulkDelete(List<Long> studentIds) {
    	if (studentIds.isEmpty()) {
    		return 0;
		}
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(StudentCourse.DELETE_BY_STUDENT_IDS)
                .setParameter("studentIds", studentIds)
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
	int deleteByCourseIdsAsBulkDelete(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return 0;
        }
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(StudentCourse.DELETE_BY_COURSE_IDS)
                .setParameter("courseIds", courseIds)
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteByStudentIdCourseIdsAsBulkDelete(List<StudentIdCourseId> studentIdCourseIds) {
        EntityManager entityManager = dbInstance.getCurrentEntityManager();
        int count = 0;
        for (StudentIdCourseId studentIdCourseId : studentIdCourseIds) {
            count += entityManager
                    .createNamedQuery(StudentCourse.DELETE_BY_STUDENT_ID_COURSE_ID)
                    .setParameter("studentId", studentIdCourseId.getStudentId())
                    .setParameter("courseId", studentIdCourseId.getCourseId())
                    .executeUpdate();
        }
        return count;
    }

    private void deleteStudentCourseBidirectionally(StudentCourse studentCourse) {
        studentCourse.getStudent().getStudentCourses().remove(studentCourse);
        studentCourse.getCourse().getStudentCourses().remove(studentCourse);
        dbInstance.deleteObject(studentCourse);
    }
}
