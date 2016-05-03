package ch.uzh.campus.data;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@SuppressWarnings("serial")
public class StudentCoursePK implements Serializable {
    
    private long studentId;
    
    private long courseId;

    public StudentCoursePK() {
    }

    public StudentCoursePK(long studentId, long courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("studentId", getStudentId());
        builder.append("courseId", getCourseId());
        return builder.toString();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(studentId);
        builder.append(courseId);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StudentCoursePK))
            return false;
        StudentCoursePK theOther = (StudentCoursePK) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.studentId, theOther.studentId);
        builder.append(this.courseId, theOther.courseId);

        return builder.isEquals();
    }

}
