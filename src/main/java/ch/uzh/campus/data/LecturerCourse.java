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
        @NamedQuery(name = LecturerCourse.GET_ALL_NOT_UPDATED_LC_BOOKING_OF_CURRENT_SEMESTER, query = "select new ch.uzh.campus.data.LecturerIdCourseId(lc.lecturer.personalNr, lc.course.id) from LecturerCourse lc " +
                "where lc.dateOfImport < :lastDateOfImport and lc.course.shortSemester = (select max(c.shortSemester) from Course c)"),
        @NamedQuery(name = LecturerCourse.DELETE_BY_LECTURER_IDS, query = "delete from LecturerCourse lc where lc.lecturer.personalNr in :lecturerIds"),
        @NamedQuery(name = LecturerCourse.DELETE_BY_COURSE_IDS, query = "delete from LecturerCourse lc where lc.course.id in :courseIds"),
        @NamedQuery(name = LecturerCourse.DELETE_BY_LECTURER_ID_COURSE_ID, query = "delete from LecturerCourse lc where lc.lecturer.personalNr = :lecturerId and lc.course.id = :courseId"),
        @NamedQuery(name = LecturerCourse.DELETE_ALL_LC_BOOKING_TOO_FAR_IN_THE_PAST, query = "delete from LecturerCourse lc where lc.dateOfImport < :nYearsInThePast")
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
    @Column(name = "date_of_import", nullable = false)
    private Date dateOfImport;

    public LecturerCourse() {
    }

    public LecturerCourse(Lecturer lecturer, Course course, Date modifiedDate) {
        this.lecturer = lecturer;
        this.course = course;
        this.dateOfImport = modifiedDate;
    }

    static final String GET_ALL_NOT_UPDATED_LC_BOOKING_OF_CURRENT_SEMESTER = "getAllNotUpdatedLCBookingOfCurrentSemester";
    static final String DELETE_BY_LECTURER_IDS = "deleteLecturerCourseByLecturerIds";
    static final String DELETE_BY_COURSE_IDS = "deleteLecturerCourseByCourseIds";
    static final String DELETE_BY_LECTURER_ID_COURSE_ID = "deleteLecturerCourseByLecturerIdCourseId";
    static final String DELETE_ALL_LC_BOOKING_TOO_FAR_IN_THE_PAST = "deleteAllLCBookingTooFarInThePast";

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
