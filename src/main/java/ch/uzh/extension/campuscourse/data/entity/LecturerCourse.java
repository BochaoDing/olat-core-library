package ch.uzh.extension.campuscourse.data.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Martin Schraner
 */
@Entity
@Table(name="ck_lecturer_course")
@IdClass(LecturerCourseId.class)
@NamedQueries({
        @NamedQuery(name = LecturerCourse.GET_ALL_NOT_UPDATED_LC_BOOKING_OF_CURRENT_IMPORT_PROCESS, query = "select new ch.uzh.extension.campuscourse.model.LecturerIdCourseId(lc.lecturer.personalNr, lc.course.id) from LecturerCourse lc " +
                "where lc.dateOfImport < :lastDateOfImport and lc.course.semester.id = :semesterIdOfCurrentImportProcess"),
        @NamedQuery(name = LecturerCourse.DELETE_BY_LECTURER_IDS, query = "delete from LecturerCourse lc where lc.lecturer.personalNr in :lecturerIds"),
        @NamedQuery(name = LecturerCourse.DELETE_BY_COURSE_IDS, query = "delete from LecturerCourse lc where lc.course.id in :courseIds"),
        @NamedQuery(name = LecturerCourse.DELETE_BY_LECTURER_ID_COURSE_ID, query = "delete from LecturerCourse lc where lc.lecturer.personalNr = :lecturerId and lc.course.id = :courseId"),
        @NamedQuery(name = LecturerCourse.DELETE_ALL_LC_BOOKING_TOO_FAR_IN_THE_PAST, query = "delete from LecturerCourse lc where lc.dateOfImport < :nYearsInThePast"),
		@NamedQuery(name = LecturerCourse.DELETE_ALL_LC_BOOKING_TOO_FAR_IN_THE_PAST_EXCEPT_FOR_COURSES_TO_BE_EXCLUDED, query = "delete from LecturerCourse lc where lc.dateOfImport < :nYearsInThePast and lc.course.id not in :courseIdsToBeExcluded")
})
public class LecturerCourse {
    public static final String GET_ALL_NOT_UPDATED_LC_BOOKING_OF_CURRENT_IMPORT_PROCESS = "getAllNotUpdatedLCBookingOfCurrentImportProcess";
    public static final String DELETE_BY_LECTURER_IDS = "deleteLecturerCourseByLecturerIds";
    public static final String DELETE_BY_COURSE_IDS = "deleteLecturerCourseByCourseIds";
    public static final String DELETE_BY_LECTURER_ID_COURSE_ID = "deleteLecturerCourseByLecturerIdCourseId";
    public static final String DELETE_ALL_LC_BOOKING_TOO_FAR_IN_THE_PAST = "deleteAllLCBookingTooFarInThePast";
	public static final String DELETE_ALL_LC_BOOKING_TOO_FAR_IN_THE_PAST_EXCEPT_FOR_COURSES_TO_BE_EXCLUDED = "deleteAllLCBookingTooFarInThePastExceptForCoursesToBeExcluded";

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_lecturer")
    private Lecturer lecturer;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_course")
    private Course course;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_import", nullable = false)
    private Date dateOfImport;

    public LecturerCourse() {
    }

    public LecturerCourse(Lecturer lecturer, Course course, Date dateOfImport) {
        this.lecturer = lecturer;
        this.course = course;
        this.dateOfImport = dateOfImport;
    }

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
