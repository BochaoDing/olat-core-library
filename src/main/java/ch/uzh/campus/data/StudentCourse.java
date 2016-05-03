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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_student_course")
@NamedQueries({
        @NamedQuery(name = StudentCourse.DELETE_STUDENT_BY_COURSE_ID, query = "delete from StudentCourse sc where sc.pk.courseId = :courseId "),
        @NamedQuery(name = StudentCourse.DELETE_STUDENTS_BY_COURSE_IDS, query = "delete from StudentCourse sc where sc.pk.courseId in ( :courseIds) "),
        @NamedQuery(name = StudentCourse.DELETE_STUDENT_BY_STUDENT_ID, query = "delete from StudentCourse sc where sc.pk.studentId = :studentId "),
        @NamedQuery(name = StudentCourse.DELETE_STUDENTS_BY_STUDENT_IDS, query = "delete from StudentCourse sc where sc.pk.studentId in ( :studentIds) "),
        @NamedQuery(name = StudentCourse.DELETE_ALL_NOT_UPDATED_SC_BOOKING, query = "delete from StudentCourse sc where sc.modifiedDate is not null and sc.modifiedDate < :lastImportDate") })
public class StudentCourse {
    @EmbeddedId
    private StudentCoursePK pk;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;
    
    public static final String DELETE_STUDENT_BY_COURSE_ID = "deleteStudentByCourseId";
    public static final String DELETE_STUDENTS_BY_COURSE_IDS = "deleteStudentsByCourseIds";

    public static final String DELETE_STUDENT_BY_STUDENT_ID = "deleteStudentByStudentId";
    public static final String DELETE_STUDENTS_BY_STUDENT_IDS = "deleteStudentsByStudentIds";
    public static final String DELETE_ALL_NOT_UPDATED_SC_BOOKING = "deleteAllNotUpdatedSCBooking";
    
    public StudentCourse() {
    }

    public StudentCourse(StudentCoursePK pk) {
        this.pk = pk;
    }

    public StudentCoursePK getPk() {
        return pk;
    }

    public void setPk(StudentCoursePK pk) {
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
        builder.append("studentId", getPk().getStudentId());
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
        if (!(obj instanceof StudentCourse))
            return false;
        StudentCourse theOther = (StudentCourse) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.pk, theOther.pk);
        builder.append(this.modifiedDate, theOther.modifiedDate);

        return builder.isEquals();
    }

}
