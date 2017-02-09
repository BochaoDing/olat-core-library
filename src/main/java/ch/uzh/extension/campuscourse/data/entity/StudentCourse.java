package ch.uzh.extension.campuscourse.data.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Martin Schraner
 */
@Entity
@Table(name="ck_student_course")
@IdClass(StudentCourseId.class)
@NamedQueries({
        @NamedQuery(name = StudentCourse.GET_ALL_NOT_UPDATED_SC_BOOKING_OF_CURRENT_IMPORT_PROCESS, query = "select new ch.uzh.extension.campuscourse.model.StudentIdCourseId(sc.student.id, sc.course.id) from StudentCourse sc " +
                "where sc.dateOfImport < :lastDateOfImport and sc.course.semester.id = :semesterIdOfCurrentImportProcess"),
        @NamedQuery(name = StudentCourse.DELETE_BY_STUDENT_IDS, query = "delete from StudentCourse sc where sc.student.id in :studentIds"),
        @NamedQuery(name = StudentCourse.DELETE_BY_COURSE_IDS, query = "delete from StudentCourse sc where sc.course.id in :courseIds"),
        @NamedQuery(name = StudentCourse.DELETE_BY_STUDENT_ID_COURSE_ID, query = "delete from StudentCourse sc where sc.student.id = :studentId and sc.course.id = :courseId"),
        @NamedQuery(name = StudentCourse.DELETE_ALL_SC_BOOKING_TOO_FAR_IN_THE_PAST, query = "delete from StudentCourse sc where sc.dateOfImport < :nYearsInThePast"),
        @NamedQuery(name = StudentCourse.DELETE_ALL_SC_BOOKING_TOO_FAR_IN_THE_PAST_EXCEPT_FOR_COURSES_TO_BE_EXCLUDED, query = "delete from StudentCourse sc where sc.dateOfImport < :nYearsInThePast and sc.course.id not in :courseIdsToBeExcluded")
})
public class StudentCourse {

    public static final String GET_ALL_NOT_UPDATED_SC_BOOKING_OF_CURRENT_IMPORT_PROCESS = "getAllNotUpdatedSCBookingOfCurrentImportProcess";
    public static final String DELETE_BY_STUDENT_IDS = "deleteStudentCourseByStudentIds";
    public static final String DELETE_BY_COURSE_IDS = "deleteStudentCourseByCourseIds";
    public static final String DELETE_BY_STUDENT_ID_COURSE_ID = "deleteByStudentIdCourseId";
    public static final String DELETE_ALL_SC_BOOKING_TOO_FAR_IN_THE_PAST = "deleteAllSCBookingTooFarInThePast";
    public static final String DELETE_ALL_SC_BOOKING_TOO_FAR_IN_THE_PAST_EXCEPT_FOR_COURSES_TO_BE_EXCLUDED = "deleteAllSCBookingTooFarInThePastExceptForCoursesToBeExcluded";

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_student")
    private Student student;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_course")
    private Course course;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_import", nullable = false)
    private Date dateOfImport;

    public StudentCourse() {
    }

    public StudentCourse(Student student, Course course, Date dateOfImport) {
        this.student = student;
        this.course = course;
        this.dateOfImport = dateOfImport;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Date getDateOfImport() {
        return dateOfImport;
    }

    public void setDateOfImport(Date dateOfImport) {
        this.dateOfImport = dateOfImport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StudentCourse that = (StudentCourse) o;

        if (!student.equals(that.student)) return false;
        return course.equals(that.course);

    }

    @Override
    public int hashCode() {
        int result = student.hashCode();
        result = 31 * result + course.hashCode();
        return result;
    }
}
