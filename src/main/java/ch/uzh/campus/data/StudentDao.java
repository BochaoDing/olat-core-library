package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
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

    Student getStudentById(Long id) {
        return dbInstance.findObject(Student.class, id);
    }

    Student getStudentByEmail(String email) {
        List<Student> students = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_STUDENTS_BY_EMAIL, Student.class)
                .setParameter("email", email)
                .getResultList();
        if (students != null && !students.isEmpty()) {
            return students.get(0);
        }
        return null;
    }

    Student getStudentByRegistrationNr(String registrationNr) {
        List<Student> students = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_STUDENTS_WITH_REGISTRATION_NUMBER, Student.class)
                .setParameter("registrationNr", registrationNr)
                .getResultList();
        if (students != null && !students.isEmpty()) {
            return students.get(0);
        }
        return null;
    }

    List<Long> getAllOrphanedStudents() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_ALL_ORPHANED_STUDENTS, Long.class)
                .getResultList();
    }

    List<Student> getAllStudentsWithCreatedOrNotCreatedCreatableCourses() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_ALL_STUDENTS_WITH_CREATED_OR_NOT_CREATED_CREATABLE_COURSES, Student.class)
                .getResultList();
    }

    /**
     * Deletes also according entries of the join able ck_student_course.
     */
    void delete(Student student) {
        deleteStudentBidirectionally(student, dbInstance.getCurrentEntityManager());
    }

    /**
     * Deletes also according entries of the join table ck_student_course.
     */
    void deleteByStudentIds(List<Long> studentIds) {
        int count = 0;
        EntityManager em = dbInstance.getCurrentEntityManager();
        for (Long studentId : studentIds) {
            deleteStudentBidirectionally(em.getReference(Student.class, studentId), em);
            // Avoid memory problems caused by loading too many objects into the persistence context
            // (cf. C. Bauer and G. King: Java Persistence mit Hibernate, 2nd edition, p. 477)
            if (++count % 100 == 0) {
                em.flush();
                em.clear();
            }
        }
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries.
     * Does not delete according entries of join table ck_student_course (-> must be deleted explicitly)!
     * Does not update persistence context!
     */
    int deleteByStudentIdsAsBulkDelete(List<Long> studentIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.DELETE_BY_STUDENT_IDS)
                .setParameter("studentIds", studentIds)
                .executeUpdate();
    }

    private void deleteStudentBidirectionally(Student student, EntityManager em) {
        for (StudentCourse studentCourse : student.getStudentCourses()) {
            studentCourse.getCourse().getStudentCourses().remove(studentCourse);
            // Use em.remove() instead of dbInstance.deleteObject() since the latter calls dbInstance.getCurrentEntityManager()
            // at every call, which may has an impact on the performance
            em.remove(studentCourse);
        }
        em.remove(student);
    }

}
