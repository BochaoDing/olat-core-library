package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.data.entity.*;
import ch.uzh.extension.campuscourse.model.LecturerIdCourseId;
import ch.uzh.extension.campuscourse.model.LecturerIdCourseIdDateOfLatestImport;
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
public class LecturerCourseDao {

    private static final OLog LOG = Tracing.createLoggerFor(LecturerCourseDao.class);

    private final CampusCourseConfiguration campusCourseConfiguration;
    private final DB dbInstance;
	private final CourseDao courseDao;

    @Autowired
    public LecturerCourseDao(CampusCourseConfiguration campusCourseConfiguration, DB dbInstance, CourseDao courseDao) {
        this.campusCourseConfiguration = campusCourseConfiguration;
		this.dbInstance = dbInstance;
		this.courseDao = courseDao;
    }

    public void save(LecturerCourse lecturerCourse) {
    	lecturerCourse.setDateOfFirstImport(lecturerCourse.getDateOfLatestImport());
        dbInstance.saveObject(lecturerCourse);
        lecturerCourse.getLecturer().getLecturerCourses().add(lecturerCourse);
        lecturerCourse.getCourse().getLecturerCourses().add(lecturerCourse);
    }

    public void save(LecturerIdCourseIdDateOfLatestImport lecturerIdCourseIdDateOfLatestImport) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        Lecturer lecturer = em.find(Lecturer.class, lecturerIdCourseIdDateOfLatestImport.getLecturerId());
        if (lecturer == null) {
            logLecturerNotFoundAndThrowException(lecturerIdCourseIdDateOfLatestImport);
            return;
        }
		Course course = em.find(Course.class, lecturerIdCourseIdDateOfLatestImport.getCourseId());
		if (course == null) {
			logCourseNotFoundAndThrowException(lecturerIdCourseIdDateOfLatestImport);
			return;
		}
        LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, lecturerIdCourseIdDateOfLatestImport.getDateOfLatestImport());
        save(lecturerCourse);
    }

	public void save(List<LecturerIdCourseIdDateOfLatestImport> lecturerIdCourseIdDateOfLatestImports) {
		lecturerIdCourseIdDateOfLatestImports.forEach(this::save);
	}

	void saveOrUpdate(LecturerCourse lecturerCourse) {
    	LecturerCourse lecturerCourseFound = getLecturerCourseById(lecturerCourse.getLecturer().getPersonalNr(), lecturerCourse.getCourse().getId());
    	if (lecturerCourseFound != null) {
			lecturerCourse.mergeImportedAttributesInto(lecturerCourseFound);
		} else {
    		save(lecturerCourse);
		}
	}

	/**
	 * For efficient insert or update without loading student and course.
	 * NB: Inserted or updated studentCourse must not be used before reloading it from the database!
	 */
	private void saveWithoutBidirectionalUpdate(LecturerCourse lecturerCourse) {
		lecturerCourse.setDateOfFirstImport(lecturerCourse.getDateOfLatestImport());
		dbInstance.saveObject(lecturerCourse);
	}

    /**
     * For efficient insert or update without loading lecturer and course.
     * NB: Inserted or updated lecturerCourse must not be used before reloading it from the database!
     */
    public void saveOrUpdateWithoutBidirectionalUpdate(LecturerIdCourseIdDateOfLatestImport lecturerIdCourseIdDateOfLatestImport) {
    	LecturerCourse lecturerCourseFound = getLecturerCourseById(lecturerIdCourseIdDateOfLatestImport.getLecturerId(), lecturerIdCourseIdDateOfLatestImport.getCourseId());
    	if (lecturerCourseFound != null) {
			lecturerIdCourseIdDateOfLatestImport.mergeImportedAttributesInto(lecturerCourseFound);
		} else {
			EntityManager em = dbInstance.getCurrentEntityManager();
			Lecturer lecturer = em.getReference(Lecturer.class, lecturerIdCourseIdDateOfLatestImport.getLecturerId());
			try {
				// To get a (potential) EntityNotFoundException the object has to be accessed
				//noinspection ResultOfMethodCallIgnored
				lecturer.getPersonalNr();
			} catch (EntityNotFoundException e) {
				logLecturerNotFoundAndThrowException(lecturerIdCourseIdDateOfLatestImport);
				return;
			}
			Course course = em.getReference(Course.class, lecturerIdCourseIdDateOfLatestImport.getCourseId());
			try {
				// To get a (potential) EntityNotFoundException the object has to be accessed
				//noinspection ResultOfMethodCallIgnored
				course.getId();
			} catch (EntityNotFoundException e) {
				logCourseNotFoundAndThrowException(lecturerIdCourseIdDateOfLatestImport);
				return;
			}
			LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, lecturerIdCourseIdDateOfLatestImport.getDateOfLatestImport());
			saveWithoutBidirectionalUpdate(lecturerCourse);
		}
    }

    private void logLecturerNotFoundAndThrowException(LecturerIdCourseIdDateOfLatestImport lecturerIdCourseIdDateOfLatestImport) {
        String warningMessage = "No lecturer found with id " + lecturerIdCourseIdDateOfLatestImport.getLecturerId();
        warningMessage = warningMessage + ". Skipping entry " + lecturerIdCourseIdDateOfLatestImport.getLecturerId() + ", " + lecturerIdCourseIdDateOfLatestImport.getCourseId() + " for table ck_lecturer_course.";
        // Here we only log on the debug level to avoid duplicated warnings (LOG.warn is already called by CampusWriter)
        LOG.debug(warningMessage);
        throw new EntityNotFoundException(warningMessage);
    }

	private void logCourseNotFoundAndThrowException(LecturerIdCourseIdDateOfLatestImport lecturerIdCourseIdDateOfLatestImport) {
		String warningMessage = "No course found with id " + lecturerIdCourseIdDateOfLatestImport.getCourseId();
		warningMessage = warningMessage + ". Skipping entry " + lecturerIdCourseIdDateOfLatestImport.getLecturerId() + ", " + lecturerIdCourseIdDateOfLatestImport.getCourseId() + " for table ck_lecturer_course.";
		// Here we only log on the debug level to avoid duplicated warnings (LOG.warn is already called by CampusWriter)
		LOG.debug(warningMessage);
		throw new EntityNotFoundException(warningMessage);
	}

    LecturerCourse getLecturerCourseById(Long lecturerId, Long courseId) {
        return dbInstance.getCurrentEntityManager().find(LecturerCourse.class, new LecturerCourseId(lecturerId, courseId));
    }

    public List<LecturerIdCourseId> getAllNotUpdatedLCBookingOfCurrentImportProcess(Date date, Semester semesterOfCurrentImportProcess) {
        // Subtract one second since modifiedDate (used in query) is rounded to seconds
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(LecturerCourse.GET_ALL_NOT_UPDATED_LC_BOOKING_OF_CURRENT_IMPORT_PROCESS, LecturerIdCourseId.class)
                .setParameter("lastDateOfImport", DateUtil.addSecondsToDate(date, -1))
                .setParameter("semesterIdOfCurrentImportProcess", semesterOfCurrentImportProcess.getId())
                .getResultList();
    }

    public void delete(LecturerCourse lecturerCourse) {
        deleteLecturerCourseBidirectionally(lecturerCourse);
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteAllLCBookingOfNotContinuedCoursesTooFarInThePastAsBulkDelete(Date date) {
    	List<Long> courseIdsToBeExcluded = courseDao.getIdsOfContinuedCoursesTooFarInThePast(date);
    	if (courseIdsToBeExcluded.isEmpty()) {
    		// JPA would crash if courseIdsToBeExcluded was empty, so we have to use a query without courseIdsToBeExcluded
			return dbInstance.getCurrentEntityManager()
					.createNamedQuery(LecturerCourse.DELETE_ALL_LC_BOOKING_TOO_FAR_IN_THE_PAST)
					.setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
					.executeUpdate();
		} else {
			return dbInstance.getCurrentEntityManager()
					.createNamedQuery(LecturerCourse.DELETE_ALL_LC_BOOKING_TOO_FAR_IN_THE_PAST_EXCEPT_FOR_COURSES_TO_BE_EXCLUDED)
					.setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
					.setParameter("courseIdsToBeExcluded", courseIdsToBeExcluded)
					.executeUpdate();
		}
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteByLecturerIdsAsBulkDelete(List<Long> lecturerIds) {
    	if (lecturerIds.isEmpty()) {
    		return 0;
		}
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(LecturerCourse.DELETE_BY_LECTURER_IDS)
                .setParameter("lecturerIds", lecturerIds)
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
                .createNamedQuery(LecturerCourse.DELETE_BY_COURSE_IDS)
                .setParameter("courseIds", courseIds)
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteByLecturerIdCourseIdsAsBulkDelete(List<LecturerIdCourseId> lecturerIdCourseIds) {
        EntityManager entityManager = dbInstance.getCurrentEntityManager();
        int count = 0;
        for (LecturerIdCourseId lecturerIdCourseId : lecturerIdCourseIds) {
            count += entityManager
                    .createNamedQuery(LecturerCourse.DELETE_BY_LECTURER_ID_COURSE_ID)
                    .setParameter("lecturerId", lecturerIdCourseId.getLecturerId())
                    .setParameter("courseId", lecturerIdCourseId.getCourseId())
                    .executeUpdate();
        }
        return count;
    }

    private void deleteLecturerCourseBidirectionally(LecturerCourse lecturerCourse) {
        lecturerCourse.getLecturer().getLecturerCourses().remove(lecturerCourse);
        lecturerCourse.getCourse().getLecturerCourses().remove(lecturerCourse);
        dbInstance.deleteObject(lecturerCourse);
    }
}
