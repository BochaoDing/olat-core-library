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
@Embeddable
public class LecturerCoursePK implements Serializable {
    @Basic(optional = false)
    @Column(name = "lecturer_id")
    private long lecturerId;
    @Basic(optional = false)
    @Column(name = "course_id")
    private long courseId;

    public long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(long lecturerId) {
        this.lecturerId = lecturerId;
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
        builder.append("lecturerId", getLecturerId());
        builder.append("courseId", getCourseId());
        return builder.toString();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(lecturerId);
        builder.append(courseId);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LecturerCoursePK))
            return false;
        LecturerCoursePK theOther = (LecturerCoursePK) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.lecturerId, theOther.lecturerId);
        builder.append(this.courseId, theOther.courseId);

        return builder.isEquals();
    }

}
