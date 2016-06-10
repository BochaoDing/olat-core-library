package ch.uzh.campus.data;


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
public class StudentCourseDao implements CampusDao<StudentIdCourseId> {

    private static final OLog LOG = Tracing.createLoggerFor(StudentCourseDao.class);

    @Autowired
    private DB dbInstance;

    public void save(StudentCourse studentCourse) {
        dbInstance.saveObject(studentCourse);
        studentCourse.getStudent().getStudentCourses().add(studentCourse);
        studentCourse.getCourse().getStudentCourses().add(studentCourse);
    }

    public void save(StudentIdCourseId studentIdCourseId) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        Student student = em.find(Student.class, studentIdCourseId.getStudentId());
        Course course = em.find(Course.class, studentIdCourseId.getCourseId());
        if (student == null || course == null) {
            logStudentCourseNotFoundAndThrowException(studentIdCourseId, student, course);
        }
        StudentCourse studentCourse = new StudentCourse(student, course, studentIdCourseId.getModifiedDate());
        save(studentCourse);
    }

    @Override
    public void save(List<StudentIdCourseId> studentIdCourseIds) {
        for (StudentIdCourseId studentIdCourseId : studentIdCourseIds) {
            save(studentIdCourseId);
        }
    }

    public void saveOrUpdate(StudentCourse studentCourse) {
        studentCourse = dbInstance.getCurrentEntityManager().merge(studentCourse);
        studentCourse.getStudent().getStudentCourses().add(studentCourse);
        studentCourse.getCourse().getStudentCourses().add(studentCourse);
    }

    public void saveOrUpdateList(List<StudentCourse> studentCourses) {
    	for(StudentCourse studentCourse:studentCourses) {
    		saveOrUpdate(studentCourse);
    	}    	
    }

    public void saveOrUpdate(StudentIdCourseId studentIdCourseId) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        Student student = em.find(Student.class, studentIdCourseId.getStudentId());
        Course course = em.find(Course.class, studentIdCourseId.getCourseId());
        if (student == null || course == null) {
            logStudentCourseNotFoundAndThrowException(studentIdCourseId, student, course);
            return;
        }
        StudentCourse studentCourse = new StudentCourse(student, course, studentIdCourseId.getModifiedDate());
        saveOrUpdate(studentCourse);
    }

    @Override
    public void saveOrUpdate(List<StudentIdCourseId> studentIdCourseIds) {
        for (StudentIdCourseId studentIdCourseId : studentIdCourseIds) {
            saveOrUpdate(studentIdCourseId);
        }
    }

    private void logStudentCourseNotFoundAndThrowException(StudentIdCourseId studentIdCourseId, Student student, Course course) {
        String warningMessage = "";
        if (student == null) {
            warningMessage = "No student found with id " + studentIdCourseId.getStudentId();
        }
        if (course == null) {
            warningMessage = "No course found with id " + studentIdCourseId.getCourseId();
        }
        warningMessage = warningMessage + ". Skipping entry " + studentIdCourseId.getStudentId() + ", " + studentIdCourseId.getCourseId() + " for table ck_student_course.";
        LOG.warn(warningMessage);
        throw new EntityNotFoundException(warningMessage);
    }

    public StudentCourse getStudentCourseById(Long studentId, Long courseId) {
        return dbInstance.getCurrentEntityManager().find(StudentCourse.class, new StudentCourseId(studentId, courseId));
    }

    public void delete(StudentCourse studentCourse) {
        deleteStudentCourseBidirectionally(studentCourse);
    }

    public int deleteAllNotUpdatedSCBooking(Date date) {
        // Subtract one second since modifiedDate (used in query) is rounded to seconds
        List<StudentCourse> studentCoursesToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(StudentCourse.GET_ALL_NOT_UPDATED_SC_BOOKING, StudentCourse.class)
                .setParameter("lastImportDate", DateUtil.addSecondsToDate(date, -1))
                .getResultList();
        for (StudentCourse studentCourse : studentCoursesToBeDeleted) {
            deleteStudentCourseBidirectionally(studentCourse);
        }
        return studentCoursesToBeDeleted.size();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteAllNotUpdatedSCBookingAsBulkDelete(Date date) {
        // Subtract one second from date since modifiedDate (used in query) is rounded to seconds
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(StudentCourse.DELETE_ALL_NOT_UPDATED_SC_BOOKING)
                .setParameter("lastImportDate", DateUtil.addSecondsToDate(date, -1))
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteByStudentIdsAsBulkDelete(List<Long> studentIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(StudentCourse.DELETE_BY_STUDENT_IDS)
                .setParameter("studentIds", studentIds)
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteByCourseIdsAsBulkDelete(List<Long> courseIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(StudentCourse.DELETE_BY_COURSE_IDS)
                .setParameter("courseIds", courseIds)
                .executeUpdate();
    }

    private void deleteStudentCourseBidirectionally(StudentCourse studentCourse) {
        studentCourse.getStudent().getStudentCourses().remove(studentCourse);
        studentCourse.getCourse().getStudentCourses().remove(studentCourse);
        dbInstance.deleteObject(studentCourse);
    }
}
