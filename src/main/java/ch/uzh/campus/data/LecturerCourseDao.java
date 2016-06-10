package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Martin Schraner
 */
@Repository
public class LecturerCourseDao implements CampusDao<LecturerIdCourseId> {

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

    public void save(LecturerIdCourseId lecturerIdCourseId) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        Lecturer lecturer = em.find(Lecturer.class, lecturerIdCourseId.getLecturerId());
        Course course = em.find(Course.class, lecturerIdCourseId.getCourseId());
        if (lecturer == null || course == null) {
            logLecturerCourseNotFoundAndThrowException(lecturerIdCourseId, lecturer, course);
            return;
        }
        LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, lecturerIdCourseId.getModifiedDate());
        save(lecturerCourse);
    }

    @Override
    public void save(List<LecturerIdCourseId> lecturerIdCourseIds) {
        for (LecturerIdCourseId lecturerIdCourseId : lecturerIdCourseIds) {
            save(lecturerIdCourseId);
        }
    }

    public void saveOrUpdate(LecturerCourse lecturerCourse) {
        lecturerCourse = dbInstance.getCurrentEntityManager().merge(lecturerCourse);
        lecturerCourse.getLecturer().getLecturerCourses().add(lecturerCourse);
        lecturerCourse.getCourse().getLecturerCourses().add(lecturerCourse);
    }

    public void saveOrUpdate(LecturerIdCourseId lecturerIdCourseId) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        Lecturer lecturer = em.find(Lecturer.class, lecturerIdCourseId.getLecturerId());
        Course course = em.find(Course.class, lecturerIdCourseId.getCourseId());
        if (lecturer == null || course == null) {
            logLecturerCourseNotFoundAndThrowException(lecturerIdCourseId, lecturer, course);
            return;
        }
        LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, lecturerIdCourseId.getModifiedDate());
        saveOrUpdate(lecturerCourse);
    }

    @Override
    public void saveOrUpdate(List<LecturerIdCourseId> lecturerIdCourseIds) {
        for (LecturerIdCourseId lecturerIdCourseId : lecturerIdCourseIds) {
            saveOrUpdate(lecturerIdCourseId);
        }
    }

    private void logLecturerCourseNotFoundAndThrowException(LecturerIdCourseId lecturerIdCourseId, Lecturer lecturer, Course course) {
        String warningMessage = "";
        if (lecturer == null) {
            warningMessage = "No lecturer found with id " + lecturerIdCourseId.getLecturerId();
        }
        if (course == null) {
            warningMessage = "No course found with id " + lecturerIdCourseId.getCourseId();
        }
        warningMessage = warningMessage + ". Skipping entry " + lecturerIdCourseId.getLecturerId() + ", " + lecturerIdCourseId.getCourseId() + " for table ck_lecturer_course.";
        LOG.warn(warningMessage);
        throw new EntityNotFoundException(warningMessage);
    }

    public LecturerCourse getLecturerCourseById(Long lecturerId, Long courseId) {
        return dbInstance.getCurrentEntityManager().find(LecturerCourse.class, new LecturerCourseId(lecturerId, courseId));
    }

    public void delete(LecturerCourse lecturerCourse) {
        deleteLecturerCourseBidirectionally(lecturerCourse);
    }

    public int deleteAllNotUpdatedLCBooking(Date date) {
        // Subtract one second since modifiedDate (used in query) is rounded to seconds
        Calendar dateMinusOneSecond = Calendar.getInstance();
        dateMinusOneSecond.setTime(date);
        dateMinusOneSecond.add(Calendar.SECOND, -1);
        List<LecturerCourse> lecturerCoursesToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(LecturerCourse.GET_ALL_NOT_UPDATED_LC_BOOKING, LecturerCourse.class)
                .setParameter("lastImportDate", dateMinusOneSecond.getTime())
                .getResultList();
        for (LecturerCourse lecturerCourse : lecturerCoursesToBeDeleted) {
            deleteLecturerCourseBidirectionally(lecturerCourse);
        }
        return lecturerCoursesToBeDeleted.size();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteAllNotUpdatedLCBookingAsBulkDelete(Date date) {
        // Subtract one second since modifiedDate (used in query) is rounded to seconds
        Calendar dateMinusOneSecond = Calendar.getInstance();
        dateMinusOneSecond.setTime(date);
        dateMinusOneSecond.add(Calendar.SECOND, -1);
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(LecturerCourse.DELETE_ALL_NOT_UPDATED_LC_BOOKING)
                .setParameter("lastImportDate", dateMinusOneSecond.getTime())
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

    private void deleteLecturerCourseBidirectionally(LecturerCourse lecturerCourse) {
        lecturerCourse.getLecturer().getLecturerCourses().remove(lecturerCourse);
        lecturerCourse.getCourse().getLecturerCourses().remove(lecturerCourse);
        dbInstance.deleteObject(lecturerCourse);
    }
}
