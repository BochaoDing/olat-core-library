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
public class LecturerCourseDao implements CampusDao<LecturerIdCourseIdModifiedDate> {

    private static final OLog LOG = Tracing.createLoggerFor(LecturerCourseDao.class);

    @Autowired
    private DB dbInstance;

    @Autowired
    DaoManager daoManager;

    public void save(LecturerCourse lecturerCourse) {
        dbInstance.saveObject(lecturerCourse);
        lecturerCourse.getLecturer().getLecturerCourses().add(lecturerCourse);
        lecturerCourse.getCourse().getLecturerCourses().add(lecturerCourse);
    }

    public void save(LecturerIdCourseIdModifiedDate lecturerIdCourseIdModifiedDate) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        Lecturer lecturer = em.find(Lecturer.class, lecturerIdCourseIdModifiedDate.getLecturerId());
        Course course = em.find(Course.class, lecturerIdCourseIdModifiedDate.getCourseId());
        if (lecturer == null || course == null) {
            logLecturerCourseNotFoundAndThrowException(lecturerIdCourseIdModifiedDate, lecturer, course);
            return;
        }
        LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, lecturerIdCourseIdModifiedDate.getModifiedDate());
        save(lecturerCourse);
    }

    @Override
    public void save(List<LecturerIdCourseIdModifiedDate> lecturerIdCourseIdModifiedDates) {
        for (LecturerIdCourseIdModifiedDate lecturerIdCourseIdModifiedDate : lecturerIdCourseIdModifiedDates) {
            save(lecturerIdCourseIdModifiedDate);
        }
    }

    public void saveOrUpdate(LecturerCourse lecturerCourse) {
        lecturerCourse = dbInstance.getCurrentEntityManager().merge(lecturerCourse);
        lecturerCourse.getLecturer().getLecturerCourses().add(lecturerCourse);
        lecturerCourse.getCourse().getLecturerCourses().add(lecturerCourse);
    }

    public void saveOrUpdate(LecturerIdCourseIdModifiedDate lecturerIdCourseIdModifiedDate) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        Lecturer lecturer = em.find(Lecturer.class, lecturerIdCourseIdModifiedDate.getLecturerId());
        Course course = em.find(Course.class, lecturerIdCourseIdModifiedDate.getCourseId());
        if (lecturer == null || course == null) {
            logLecturerCourseNotFoundAndThrowException(lecturerIdCourseIdModifiedDate, lecturer, course);
            return;
        }
        LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, lecturerIdCourseIdModifiedDate.getModifiedDate());
        saveOrUpdate(lecturerCourse);
    }

    @Override
    public void saveOrUpdate(List<LecturerIdCourseIdModifiedDate> lecturerIdCourseIdModifiedDates) {
        for (LecturerIdCourseIdModifiedDate lecturerIdCourseIdModifiedDate : lecturerIdCourseIdModifiedDates) {
            saveOrUpdate(lecturerIdCourseIdModifiedDate);
        }
    }

    private void logLecturerCourseNotFoundAndThrowException(LecturerIdCourseIdModifiedDate lecturerIdCourseIdModifiedDate, Lecturer lecturer, Course course) {
        String warningMessage = "";
        if (lecturer == null) {
            warningMessage = "No lecturer found with id " + lecturerIdCourseIdModifiedDate.getLecturerId();
        }
        if (course == null) {
            warningMessage = "No course found with id " + lecturerIdCourseIdModifiedDate.getCourseId();
        }
        warningMessage = warningMessage + ". Skipping entry " + lecturerIdCourseIdModifiedDate.getLecturerId() + ", " + lecturerIdCourseIdModifiedDate.getCourseId() + " for table ck_lecturer_course.";
        LOG.warn(warningMessage);
        throw new EntityNotFoundException(warningMessage);
    }

    public LecturerCourse getLecturerCourseById(Long lecturerId, Long courseId) {
        return dbInstance.getCurrentEntityManager().find(LecturerCourse.class, new LecturerCourseId(lecturerId, courseId));
    }

    public List<LecturerIdCourseId> getAllNotUpdatedLCBookingOfCurrentSemester(Date date) {
        // Subtract one second since modifiedDate (used in query) is rounded to seconds
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(LecturerCourse.GET_ALL_NOT_UPDATED_LC_BOOKING_OF_CURRENT_SEMESTER, LecturerIdCourseId.class)
                .setParameter("lastImportDate", DateUtil.addSecondsToDate(date, -1))
                .getResultList();
    }

    public void delete(LecturerCourse lecturerCourse) {
        deleteLecturerCourseBidirectionally(lecturerCourse);
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteAllLCBookingTooFarInThePastAsBulkDelete(Date date) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(LecturerCourse.DELETE_ALL_LC_BOOKING_TOO_FAR_IN_THE_PAST)
                .setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -CampusCourseConfiguration.MAX_YEARS_TO_KEEP_CK_DATA))
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteByLecturerIdsAsBulkDelete(List<Long> lecturerIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(LecturerCourse.DELETE_BY_LECTURER_IDS)
                .setParameter("lecturerIds", lecturerIds)
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteByCourseIdsAsBulkDelete(List<Long> courseIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(LecturerCourse.DELETE_BY_COURSE_IDS)
                .setParameter("courseIds", courseIds)
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteByLecturerIdCourseIdsAsBulkDelete(List<LecturerIdCourseId> lecturerIdCourseIds) {
        int count = 0;
        for (LecturerIdCourseId lecturerIdCourseId : lecturerIdCourseIds) {
            count += dbInstance.getCurrentEntityManager()
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
