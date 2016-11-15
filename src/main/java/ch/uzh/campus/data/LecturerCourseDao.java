package ch.uzh.campus.data;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.utils.DateUtil;
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

    @Autowired
    public LecturerCourseDao(CampusCourseConfiguration campusCourseConfiguration, DB dbInstance) {
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.dbInstance = dbInstance;
    }

    public void save(LecturerCourse lecturerCourse) {
        dbInstance.saveObject(lecturerCourse);
        lecturerCourse.getLecturer().getLecturerCourses().add(lecturerCourse);
        lecturerCourse.getCourse().getLecturerCourses().add(lecturerCourse);
    }

    public void save(LecturerIdCourseIdDateOfImport lecturerIdCourseIdDateOfImport) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        Lecturer lecturer = em.find(Lecturer.class, lecturerIdCourseIdDateOfImport.getLecturerId());
        if (lecturer == null) {
            logLecturerNotFoundAndThrowException(lecturerIdCourseIdDateOfImport);
            return;
        }
		Course course = em.find(Course.class, lecturerIdCourseIdDateOfImport.getCourseId());
		if (course == null) {
			logCourseNotFoundAndThrowException(lecturerIdCourseIdDateOfImport);
			return;
		}
        LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, lecturerIdCourseIdDateOfImport.getDateOfImport());
        save(lecturerCourse);
    }

    public void save(List<LecturerIdCourseIdDateOfImport> lecturerIdCourseIdDateOfImports) {
        lecturerIdCourseIdDateOfImports.forEach(this::save);
    }

    public void saveOrUpdate(LecturerCourse lecturerCourse) {
        lecturerCourse = dbInstance.getCurrentEntityManager().merge(lecturerCourse);
        lecturerCourse.getLecturer().getLecturerCourses().add(lecturerCourse);
        lecturerCourse.getCourse().getLecturerCourses().add(lecturerCourse);
    }

    /**
     * For efficient insert or update without loading lecturer and course.
     * NB: Inserted or updated lecturerCourse must not be used before reloading it from the database!
     */
    void saveOrUpdateWithoutBidirectionalUpdate(LecturerCourse lecturerCourse) {
        dbInstance.getCurrentEntityManager().merge(lecturerCourse);
    }

    /**
     * For efficient insert or update without loading lecturer and course.
     * NB: Inserted or updated lecturerCourse must not be used before reloading it from the database!
     */
    public void saveOrUpdateWithoutBidirectionalUpdate(LecturerIdCourseIdDateOfImport lecturerIdCourseIdDateOfImport) {
		EntityManager em = dbInstance.getCurrentEntityManager();
        Lecturer lecturer = em.getReference(Lecturer.class, lecturerIdCourseIdDateOfImport.getLecturerId());
        try {
            // To get a (potential) EntityNotFoundException the object has to be accessed
            lecturer.getPersonalNr();
        } catch (EntityNotFoundException e) {
            logLecturerNotFoundAndThrowException(lecturerIdCourseIdDateOfImport);
        }
        Course course = em.getReference(Course.class, lecturerIdCourseIdDateOfImport.getCourseId());
        try {
            // To get a (potential) EntityNotFoundException the object has to be accessed
            course.getId();
        } catch (EntityNotFoundException e) {
            logCourseNotFoundAndThrowException(lecturerIdCourseIdDateOfImport);
        }
        LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, lecturerIdCourseIdDateOfImport.getDateOfImport());
        saveOrUpdateWithoutBidirectionalUpdate(lecturerCourse);
    }

    private void logLecturerNotFoundAndThrowException(LecturerIdCourseIdDateOfImport lecturerIdCourseIdDateOfImport) {
        String warningMessage = "No lecturer found with id " + lecturerIdCourseIdDateOfImport.getLecturerId();
        warningMessage = warningMessage + ". Skipping entry " + lecturerIdCourseIdDateOfImport.getLecturerId() + ", " + lecturerIdCourseIdDateOfImport.getCourseId() + " for table ck_lecturer_course.";
        // Here we only log on the debug level to avoid duplicated warnings (LOG.warn is already called by CampusWriter)
        LOG.debug(warningMessage);
        throw new EntityNotFoundException(warningMessage);
    }

	private void logCourseNotFoundAndThrowException(LecturerIdCourseIdDateOfImport lecturerIdCourseIdDateOfImport) {
		String warningMessage = "No course found with id " + lecturerIdCourseIdDateOfImport.getCourseId();
		warningMessage = warningMessage + ". Skipping entry " + lecturerIdCourseIdDateOfImport.getLecturerId() + ", " + lecturerIdCourseIdDateOfImport.getCourseId() + " for table ck_lecturer_course.";
		// Here we only log on the debug level to avoid duplicated warnings (LOG.warn is already called by CampusWriter)
		LOG.debug(warningMessage);
		throw new EntityNotFoundException(warningMessage);
	}

    LecturerCourse getLecturerCourseById(Long lecturerId, Long courseId) {
        return dbInstance.getCurrentEntityManager().find(LecturerCourse.class, new LecturerCourseId(lecturerId, courseId));
    }

    List<LecturerIdCourseId> getAllNotUpdatedLCBookingOfCurrentSemester(Date date) {
        // Subtract one second since modifiedDate (used in query) is rounded to seconds
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(LecturerCourse.GET_ALL_NOT_UPDATED_LC_BOOKING_OF_CURRENT_SEMESTER, LecturerIdCourseId.class)
                .setParameter("lastDateOfImport", DateUtil.addSecondsToDate(date, -1))
                .getResultList();
    }

    void delete(LecturerCourse lecturerCourse) {
        deleteLecturerCourseBidirectionally(lecturerCourse);
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    int deleteAllLCBookingTooFarInThePastAsBulkDelete(Date date) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(LecturerCourse.DELETE_ALL_LC_BOOKING_TOO_FAR_IN_THE_PAST)
                .setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    int deleteByLecturerIdsAsBulkDelete(List<Long> lecturerIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(LecturerCourse.DELETE_BY_LECTURER_IDS)
                .setParameter("lecturerIds", lecturerIds)
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    int deleteByCourseIdsAsBulkDelete(List<Long> courseIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(LecturerCourse.DELETE_BY_COURSE_IDS)
                .setParameter("courseIds", courseIds)
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    int deleteByLecturerIdCourseIdsAsBulkDelete(List<LecturerIdCourseId> lecturerIdCourseIds) {
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
