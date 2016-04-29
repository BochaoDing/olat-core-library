package ch.uzh.campus.data;

import java.util.Date;

import javax.persistence.*;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_lecturer_course")
@IdClass(LecturerCourseId.class)
//@NamedQueries({ @NamedQuery(name = LecturerCourse.DELETE_LECTURER_BY_COURSE_ID, query = "delete from LecturerCourse lc where lc.pk.courseId = :courseId "),
//        @NamedQuery(name = LecturerCourse.DELETE_LECTURERS_BY_COURSE_IDS, query = "delete from LecturerCourse lc where lc.pk.courseId in :courseIds"),
//        @NamedQuery(name = LecturerCourse.DELETE_LECTURER_BY_LECTURER_ID, query = "delete from LecturerCourse lc where lc.pk.lecturerId = :lecturerId "),
//        @NamedQuery(name = LecturerCourse.DELETE_LECTURERS_BY_LECTURER_IDS, query = "delete from LecturerCourse lc where lc.pk.lecturerId in :lecturerIds"),
//        @NamedQuery(name = LecturerCourse.DELETE_ALL_NOT_UPDATED_LC_BOOKING, query = "delete from LecturerCourse lc where lc.modifiedDate < :lastImportDate") })
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
    @Column(name = "modified_date")
    private Date modifiedDate;

    public static final String DELETE_LECTURER_BY_COURSE_ID = "deleteLecturerByCourseId";
    public static final String DELETE_LECTURERS_BY_COURSE_IDS = "deleteLecturersByCourseIds";

    public static final String DELETE_LECTURER_BY_LECTURER_ID = "deleteLecturerByLecturerId";
    public static final String DELETE_LECTURERS_BY_LECTURER_IDS = "deleteLecturersByLecturerIds";
    public static final String DELETE_ALL_NOT_UPDATED_LC_BOOKING = "deleteAllNotUpdatedLCBooking";

    public LecturerCourse() {
    }

    public LecturerCourse(Lecturer lecturer, Course course) {
        this.lecturer = lecturer;
        this.course = course;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("dozentId", lecturer.getPersonalNr());
        builder.append("courseId", course.getId());
        return builder.toString();
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
