package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author Martin Schraner
 */
@Repository
public class LecturerCourseDao implements CampusDao<LecturerIdCourseId> {

    @Autowired
    private DB dbInstance;

    public void save(LecturerCourse lecturerCourse) {
        dbInstance.saveObject(lecturerCourse);
        lecturerCourse.getLecturer().getLecturerCourses().add(lecturerCourse);
        lecturerCourse.getCourse().getLecturerCourses().add(lecturerCourse);
    }

    public void save(LecturerIdCourseId lecturerIdCourseId) {
        Lecturer lecturer = dbInstance.getCurrentEntityManager().getReference(Lecturer.class, lecturerIdCourseId.getLecturerId());
        Course course = dbInstance.getCurrentEntityManager().getReference(Course.class, lecturerIdCourseId.getCourseId());
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
        Lecturer lecturer = dbInstance.getCurrentEntityManager().getReference(Lecturer.class, lecturerIdCourseId.getLecturerId());
        Course course = dbInstance.getCurrentEntityManager().getReference(Course.class, lecturerIdCourseId.getCourseId());
        LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, lecturerIdCourseId.getModifiedDate());
        saveOrUpdate(lecturerCourse);
    }

    @Override
    public void saveOrUpdate(List<LecturerIdCourseId> lecturerIdCourseIds) {
        for (LecturerIdCourseId lecturerIdCourseId : lecturerIdCourseIds) {
            saveOrUpdate(lecturerIdCourseId);
        }
    }

    public LecturerCourse getLecturerCourseById(Long lecturerId, Long courseId) {
        return dbInstance.getCurrentEntityManager().find(LecturerCourse.class, new LecturerCourseId(lecturerId, courseId));
    }

    public void delete(LecturerCourse lecturerCourse) {
        deleteLecturerCourseBidirectionally(lecturerCourse);
    }

    public int deleteAllNotUpdatedSCBooking(Date date) {
        List<LecturerCourse> lecturerCoursesToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(LecturerCourse.GET_ALL_NOT_UPDATED_LC_BOOKING, LecturerCourse.class)
                .setParameter("lastImportDate", date)
                .getResultList();
        for (LecturerCourse lecturerCourse : lecturerCoursesToBeDeleted) {
            deleteLecturerCourseBidirectionally(lecturerCourse);
        }
        return lecturerCoursesToBeDeleted.size();
    }

    private void deleteLecturerCourseBidirectionally(LecturerCourse lecturerCourse) {
        lecturerCourse.getLecturer().getLecturerCourses().remove(lecturerCourse);
        lecturerCourse.getCourse().getLecturerCourses().remove(lecturerCourse);
        dbInstance.deleteObject(lecturerCourse);
    }
}
