package ch.uzh.campus.data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Martin Schraner
 */
@Entity
@Table(name="ck_student_course")
@IdClass(StudentCourseId.class)
@NamedQueries({
        @NamedQuery(name = StudentCourse.GET_ALL_NOT_UPDATED_SC_BOOKING_OF_CURRENT_SEMESTER, query = "select new ch.uzh.campus.data.StudentIdCourseId(sc.student.id, sc.course.id) from StudentCourse sc " +
                "where sc.modifiedDate < :lastImportDate and sc.course.shortSemester = (select max(c.shortSemester) from Course c)"),
        @NamedQuery(name = StudentCourse.DELETE_BY_STUDENT_IDS, query = "delete from StudentCourse sc where sc.student.id in :studentIds"),
        @NamedQuery(name = StudentCourse.DELETE_BY_COURSE_IDS, query = "delete from StudentCourse sc where sc.course.id in :courseIds"),
        @NamedQuery(name = StudentCourse.DELETE_BY_STUDENT_ID_COURSE_ID, query = "delete from StudentCourse sc where sc.student.id = :studentId and sc.course.id = :courseId"),
        @NamedQuery(name = StudentCourse.DELETE_ALL_SC_BOOKING_TOO_FAR_IN_THE_PAST, query = "delete from StudentCourse sc where sc.modifiedDate < :nYearsInThePast")
})
public class StudentCourse {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date", nullable = false)
    private Date modifiedDate;

    public StudentCourse() {
    }

    public StudentCourse(Student student, Course course, Date modifiedDate) {
        this.student = student;
        this.course = course;
        this.modifiedDate = modifiedDate;
    }

    static final String GET_ALL_NOT_UPDATED_SC_BOOKING_OF_CURRENT_SEMESTER = "getAllNotUpdatedSCBookingOfCurrentSemester";
    static final String DELETE_BY_STUDENT_IDS = "deleteStudentCourseByStudentIds";
    static final String DELETE_BY_COURSE_IDS = "deleteStudentCourseByCourseIds";
    static final String DELETE_BY_STUDENT_ID_COURSE_ID = "deleteByStudentIdCourseId";
    static final String DELETE_ALL_SC_BOOKING_TOO_FAR_IN_THE_PAST = "deleteAllSCBookingTooFarInThePast";

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

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
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
