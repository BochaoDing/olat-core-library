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
public class StudentCourseDao implements CampusDao<StudentIdCourseIdModifiedDate> {

    private static final OLog LOG = Tracing.createLoggerFor(StudentCourseDao.class);

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    private DB dbInstance;

    public void save(StudentCourse studentCourse) {
        dbInstance.saveObject(studentCourse);
        studentCourse.getStudent().getStudentCourses().add(studentCourse);
        studentCourse.getCourse().getStudentCourses().add(studentCourse);
    }

    public void save(StudentIdCourseIdModifiedDate studentIdCourseIdModifiedDate) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        Student student = em.find(Student.class, studentIdCourseIdModifiedDate.getStudentId());
        Course course = em.find(Course.class, studentIdCourseIdModifiedDate.getCourseId());
        if (student == null || course == null) {
            logStudentCourseNotFoundAndThrowException(studentIdCourseIdModifiedDate, student, course);
        }
        StudentCourse studentCourse = new StudentCourse(student, course, studentIdCourseIdModifiedDate.getModifiedDate());
        save(studentCourse);
    }

    @Override
    public void save(List<StudentIdCourseIdModifiedDate> studentIdCourseIdModifiedDates) {
        for (StudentIdCourseIdModifiedDate studentIdCourseIdModifiedDate : studentIdCourseIdModifiedDates) {
            save(studentIdCourseIdModifiedDate);
        }
    }

    void saveOrUpdate(StudentCourse studentCourse) {
        studentCourse = dbInstance.getCurrentEntityManager().merge(studentCourse);
        studentCourse.getStudent().getStudentCourses().add(studentCourse);
        studentCourse.getCourse().getStudentCourses().add(studentCourse);
    }

    void saveOrUpdateList(List<StudentCourse> studentCourses) {
    	for(StudentCourse studentCourse:studentCourses) {
    		saveOrUpdate(studentCourse);
    	}    	
    }

    void saveOrUpdate(StudentIdCourseIdModifiedDate studentIdCourseIdModifiedDate) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        Student student = em.find(Student.class, studentIdCourseIdModifiedDate.getStudentId());
        Course course = em.find(Course.class, studentIdCourseIdModifiedDate.getCourseId());
        if (student == null || course == null) {
            logStudentCourseNotFoundAndThrowException(studentIdCourseIdModifiedDate, student, course);
            return;
        }
        StudentCourse studentCourse = new StudentCourse(student, course, studentIdCourseIdModifiedDate.getModifiedDate());
        saveOrUpdate(studentCourse);
    }

    @Override
    public void saveOrUpdate(List<StudentIdCourseIdModifiedDate> studentIdCourseIdModifiedDates) {
        for (StudentIdCourseIdModifiedDate studentIdCourseIdModifiedDate : studentIdCourseIdModifiedDates) {
            saveOrUpdate(studentIdCourseIdModifiedDate);
        }
    }

    private void logStudentCourseNotFoundAndThrowException(StudentIdCourseIdModifiedDate studentIdCourseIdModifiedDate, Student student, Course course) {
        String warningMessage = "";
        if (student == null) {
            warningMessage = "No student found with id " + studentIdCourseIdModifiedDate.getStudentId();
        }
        if (course == null) {
            warningMessage = "No course found with id " + studentIdCourseIdModifiedDate.getCourseId();
        }
        warningMessage = warningMessage + ". Skipping entry " + studentIdCourseIdModifiedDate.getStudentId() + ", " + studentIdCourseIdModifiedDate.getCourseId() + " for table ck_student_course.";
        LOG.warn(warningMessage);
        throw new EntityNotFoundException(warningMessage);
    }

    StudentCourse getStudentCourseById(Long studentId, Long courseId) {
        return dbInstance.getCurrentEntityManager().find(StudentCourse.class, new StudentCourseId(studentId, courseId));
    }

    List<StudentIdCourseId> getAllNotUpdatedSCBookingOfCurrentSemester(Date date) {
        // Subtract one second since modifiedDate (used in query) is rounded to seconds
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(StudentCourse.GET_ALL_NOT_UPDATED_SC_BOOKING_OF_CURRENT_SEMESTER, StudentIdCourseId.class)
                .setParameter("lastImportDate", DateUtil.addSecondsToDate(date, -1))
                .getResultList();
    }

    void delete(StudentCourse studentCourse) {
        deleteStudentCourseBidirectionally(studentCourse);
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    int deleteAllSCBookingTooFarInThePastAsBulkDelete(Date date) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(StudentCourse.DELETE_ALL_SC_BOOKING_TOO_FAR_IN_THE_PAST)
                .setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    int deleteByStudentIdsAsBulkDelete(List<Long> studentIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(StudentCourse.DELETE_BY_STUDENT_IDS)
                .setParameter("studentIds", studentIds)
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    int deleteByCourseIdsAsBulkDelete(List<Long> courseIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(StudentCourse.DELETE_BY_COURSE_IDS)
                .setParameter("courseIds", courseIds)
                .executeUpdate();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    int deleteByStudentIdCourseIdsAsBulkDelete(List<StudentIdCourseId> studentIdCourseIds) {
        int count = 0;
        for (StudentIdCourseId studentIdCourseId : studentIdCourseIds) {
            count += dbInstance.getCurrentEntityManager()
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
