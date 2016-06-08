package ch.uzh.campus.data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Martin Schraner
 */
@Entity
@Table(name="ck_lecturer_course")
@IdClass(LecturerCourseId.class)
@NamedQueries({
        @NamedQuery(name = LecturerCourse.GET_ALL_NOT_UPDATED_LC_BOOKING, query = "select lc from LecturerCourse lc where lc.modifiedDate < :lastImportDate"),
        @NamedQuery(name = LecturerCourse.DELETE_BY_LECTURER_IDS, query = "delete from LecturerCourse lc where lc.lecturer.personalNr in :lecturerIds"),
        @NamedQuery(name = LecturerCourse.DELETE_BY_COURSE_IDS, query = "delete from LecturerCourse lc where lc.course.id in :courseIds"),
        @NamedQuery(name = LecturerCourse.DELETE_ALL_NOT_UPDATED_LC_BOOKING, query = "delete from LecturerCourse lc where lc.modifiedDate < :lastImportDate")
})
public class LecturerCourse {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "lecturer_id")
    private Lecturer lecturer;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date", nullable = false)
    private Date modifiedDate;

    public LecturerCourse() {
    }

    public LecturerCourse(Lecturer lecturer, Course course, Date modifiedDate) {
        this.lecturer = lecturer;
        this.course = course;
        this.modifiedDate = modifiedDate;
    }

    static final String GET_ALL_NOT_UPDATED_LC_BOOKING = "getAllNotUpdatedLCBooking";
    static final String DELETE_BY_LECTURER_IDS = "deleteLecturerCourseByLecturerIds";
    static final String DELETE_BY_COURSE_IDS = "deleteLecturerCourseByCourseIds";
    static final String DELETE_ALL_NOT_UPDATED_LC_BOOKING = "deleteAllNotUpdatedLCBooking";

    public Lecturer getLecturer() {
        return lecturer;
    }

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
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

        LecturerCourse that = (LecturerCourse) o;

        if (!lecturer.equals(that.lecturer)) return false;
        return course.equals(that.course);

    }

    @Override
    public int hashCode() {
        int result = lecturer.hashCode();
        result = 31 * result + course.hashCode();
        return result;
    }
}
