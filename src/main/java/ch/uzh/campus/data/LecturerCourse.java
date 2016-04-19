package ch.uzh.campus.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_lecturer_course")
@NamedQueries({ @NamedQuery(name = LecturerCourse.DELETE_LECTURER_BY_COURSE_ID, query = "delete from LecturerCourse lc where lc.pk.courseId = :courseId "),
        @NamedQuery(name = LecturerCourse.DELETE_LECTURERS_BY_COURSE_IDS, query = "delete from LecturerCourse lc where lc.pk.courseId in ( :courseIds) "),
        @NamedQuery(name = LecturerCourse.DELETE_LECTURER_BY_LECTURER_ID, query = "delete from LecturerCourse lc where lc.pk.lecturerId = :lecturerId "),
        @NamedQuery(name = LecturerCourse.DELETE_LECTURERS_BY_LECTURER_IDS, query = "delete from LecturerCourse lc where lc.pk.lecturerId in( :lecturerIds) "),
        @NamedQuery(name = LecturerCourse.DELETE_ALL_NOT_UPDATED_LC_BOOKING, query = "delete from LecturerCourse lc where lc.modifiedDate < :lastImportDate") })
public class LecturerCourse {
    @EmbeddedId
    private LecturerCoursePK pk;

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

    public LecturerCourse(LecturerCoursePK pk) {
        super();
        this.pk = pk;
    }

    public LecturerCoursePK getPk() {
        return pk;
    }

    public void setPk(LecturerCoursePK pk) {
        this.pk = pk;
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
        builder.append("dozentId", getPk().getLecturerId());
        builder.append("courseId", getPk().getCourseId());
        return builder.toString();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(pk);
        builder.append(modifiedDate);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LecturerCourse))
            return false;
        LecturerCourse theOther = (LecturerCourse) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.pk, theOther.pk);
        builder.append(this.modifiedDate, theOther.modifiedDate);

        return builder.isEquals();
    }

}
