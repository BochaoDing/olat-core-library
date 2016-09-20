package ch.uzh.campus.data;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.utils.DateUtil;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
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

    private static final OLog LOG = Tracing.createLoggerFor(StudentDao.class);

    private final CampusCourseConfiguration campusCourseConfiguration;
    private final DB dbInstance;

    @Autowired
    public StudentDao(CampusCourseConfiguration campusCourseConfiguration, DB dbInstance) {
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.dbInstance = dbInstance;
    }

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

    public void addMapping(Long studentId, Identity identity) {
        Student student = getStudentById(studentId);
        if (student != null) {
            student.setMappedIdentity(identity);
            student.setKindOfMapping("AUTO");
            student.setDateOfMapping(new Date());
        } else {
            LOG.warn("No student found with id " + studentId + " for table ck_student.");
        }
    }

    public void removeMapping(Long studentId) {
        Student student = getStudentById(studentId);
        if (student != null) {
            student.setMappedIdentity(null);
            student.setKindOfMapping(null);
            student.setDateOfMapping(null);
        } else {
            LOG.warn("No student found with id " + studentId + " for table ck_student.");
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

    List<Long> getAllNotManuallyMappedOrTooOldOrphanedStudents(Date date) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_ALL_NOT_MANUALLY_MAPPED_OR_TOO_OLD_ORPHANED_STUDENTS, Long.class)
                .setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
                .getResultList();
    }

    List<Student> getAllStudentsWithCreatedOrNotCreatedCreatableCourses() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_ALL_STUDENTS_WITH_CREATED_OR_NOT_CREATED_CREATABLE_COURSES, Student.class)
                .getResultList();
    }

    public List<Student> getStudentsMappedToOlatUserName(String olatUserName) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_STUDENTS_MAPPED_TO_OLAT_USER_NAME, Student.class)
                .setParameter("olatUserName", olatUserName)
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
