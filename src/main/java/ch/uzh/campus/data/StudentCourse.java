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
        @NamedQuery(name = StudentCourse.GET_ALL_NOT_UPDATED_SC_BOOKING, query = "select sc from StudentCourse sc where sc.modifiedDate is not null and sc.modifiedDate < :lastImportDate") })
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
    @Column(name = "modified_date")
    private Date modifiedDate;

    public StudentCourse() {
    }

    public StudentCourse(Student student, Course course, Date modifiedDate) {
        this.student = student;
        this.course = course;
        this.modifiedDate = modifiedDate;
    }

    public static final String GET_ALL_NOT_UPDATED_SC_BOOKING = "getAllNotUpdatedSCBooking";

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
