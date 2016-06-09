package ch.uzh.campus.data;

import org.apache.commons.lang.time.DateUtils;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Initial Date: 04.06.2012 <br>
 *
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */

@Repository
public class StudentDao implements CampusDao<Student> {

    @Autowired
    private DB dbInstance;

    @Override
    public void save(List<Student> students) {
        for (Student student : students) {
            dbInstance.saveObject(student);
        }
    }

    @Override
    public void saveOrUpdate(List<Student> students) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        for (Student student : students) {
            em.merge(student);
        }
    }

    public Student getStudentById(Long id) {
        return dbInstance.findObject(Student.class, id);
    }

    public Student getStudentByEmail(String email) {
        List<Student> students = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_STUDENTS_BY_EMAIL, Student.class)
                .setParameter("email", email)
                .getResultList();
        if (students != null && !students.isEmpty()) {
            return students.get(0);
        }
        return null;
    }

    public Student getStudentByRegistrationNr(String registrationNr) {
        List<Student> students = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_STUDENTS_WITH_REGISTRATION_NUMBER, Student.class)
                .setParameter("registrationNr", registrationNr)
                .getResultList();
        if (students != null && !students.isEmpty()) {
            return students.get(0);
        }
        return null;
    }

    public List<Student> getAllStudents() {
        // return genericDao.findAll();
        return getAllPilotStudents();
    }

    public List<Long> getAllNotUpdatedStudents(Date date) {
        // Round to seconds since modifiedDate (used in query) is also rounded to seconds
        Date roundedToSeconds = DateUtils.round(date, Calendar.SECOND);
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_ALL_NOT_UPDATED_STUDENTS, Long.class)
                .setParameter("lastImportDate", roundedToSeconds)
                .getResultList();
    }

    public List<Student> getAllPilotStudents() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_ALL_PILOT_STUDENTS, Student.class)
                .getResultList();
    }

    /**
     * Deletes also according entries of the join able ck_student_course.
     */
    public void delete(Student student) {
        deleteStudentBidirectionally(student);
    }

    /**
     * Deletes also according entries of the join table ck_student_course.
     */
    public void deleteByStudentIds(List<Long> studentIds) {
        for (Long studentId : studentIds) {
            deleteStudentBidirectionally(dbInstance.getCurrentEntityManager().getReference(Student.class, studentId));
        }
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries.
     * Does not delete according entries of join table ck_student_course (-> must be deleted explicitly)!
     * Does not update persistence context!
     */
    public int deleteByStudentIdsAsBulkDelete(List<Long> studentIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.DELETE_BY_STUDENT_IDS)
                .setParameter("studentIds", studentIds)
                .executeUpdate();
    }

    private void deleteStudentBidirectionally(Student student) {
        for (StudentCourse studentCourse : student.getStudentCourses()) {
            studentCourse.getCourse().getStudentCourses().remove(studentCourse);
            dbInstance.deleteObject(studentCourse);
        }
        dbInstance.deleteObject(student);
    }

}
