package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.data.entity.Course;
import ch.uzh.extension.campuscourse.data.entity.Student;
import ch.uzh.extension.campuscourse.data.entity.StudentCourse;
import ch.uzh.extension.campuscourse.util.DateUtil;
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
public class StudentDao {

    private static final OLog LOG = Tracing.createLoggerFor(StudentDao.class);

    private final CampusCourseConfiguration campusCourseConfiguration;
    private final DB dbInstance;

    @Autowired
    public StudentDao(CampusCourseConfiguration campusCourseConfiguration, DB dbInstance) {
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.dbInstance = dbInstance;
    }

    public void save(Student student) {
        student.setDateOfFirstImport(student.getDateOfLatestImport());
        dbInstance.saveObject(student);
    }

	public void save(List<Student> students) {
		students.forEach(this::save);
	}

    public void saveOrUpdate(Student student) {
        /*
		 * A database merge with a detached course entity would override the
		 * values of the mapping attributes with "null".
		 */
        Student studentFound = getStudentById(student.getId());
        if (studentFound != null) {
			student.mergeImportedAttributesInto(studentFound);
        } else {
			save(student);
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

    public List<Long> getAllNotManuallyMappedOrTooOldOrphanedStudents(Date date) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_ALL_NOT_MANUALLY_MAPPED_OR_TOO_OLD_ORPHANED_STUDENTS, Long.class)
                .setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
                .getResultList();
    }

    public List<Student> getAllStudentsWithCreatedOrNotCreatedCreatableCourses() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_ALL_STUDENTS_WITH_CREATED_OR_NOT_CREATED_CREATABLE_COURSES, Student.class)
                .getResultList();
    }

    public List<Student> getStudentsByMappedIdentityKey(Long mappedIdentityKey) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_STUDENTS_BY_MAPPED_IDENTITY_KEY, Student.class)
                .setParameter("mappedIdentityKey", mappedIdentityKey)
                .getResultList();
    }

    int getNumberOfStudentsOfSpecificCourse(Long courseId) {
        return  (int) (long) dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_NUMBER_OF_STUDENTS_OF_SPECIFIC_COURSE)
                .setParameter("courseId", courseId)
                .getSingleResult();
    }

    int getNumberOfStudentsWithBookingForCourseAndParentCourse(Long courseId) {
        return (int) (long) dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_NUMBER_OF_STUDENTS_WITH_BOOKING_FOR_COURSE_AND_PARENT_COURSE)
                .setParameter("courseId", courseId)
                .getSingleResult();
    }

    public boolean hasMoreThan50PercentOfStudentsOfSpecificCourseBothABookingOfCourseAndParentCourse(Course course) {
        if (course.getParentCourse() == null) {
            return true;
        }
        int numberOfStudentsWithBookingForParentCourse = getNumberOfStudentsOfSpecificCourse(course.getParentCourse().getId());
        return numberOfStudentsWithBookingForParentCourse == 0 || (((double) getNumberOfStudentsWithBookingForCourseAndParentCourse(course.getId()) / (double) numberOfStudentsWithBookingForParentCourse) >= 0.5);
    }

    /**
     * Deletes also according entries of the join able ck_student_course.
     */
    public void delete(Student student) {
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
    public int deleteByStudentIdsAsBulkDelete(List<Long> studentIds) {
        if (studentIds.isEmpty()) {
            return 0;
        }
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