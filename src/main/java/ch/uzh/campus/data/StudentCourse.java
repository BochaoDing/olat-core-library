package ch.uzh.campus.data;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_student_course")
@IdClass(StudentCoursePK.class)
@NamedQueries({
        //@NamedQuery(name = StudentCourse.DELETE_STUDENT_BY_COURSE_ID, query = "delete from StudentCourse sc where sc.pk.courseId = :courseId "),
	//@NamedQuery(name = StudentCourse.DELETE_STUDENTS_BY_COURSE_IDS, query = "delete from StudentCourse sc where sc.pk.courseId in ( :courseIds) "),
	//@NamedQuery(name = StudentCourse.DELETE_STUDENT_BY_STUDENT_ID, query = "delete from StudentCourse sc where sc.pk.studentId = :studentId "),
	//@NamedQuery(name = StudentCourse.DELETE_STUDENTS_BY_STUDENT_IDS, query = "delete from StudentCourse sc where sc.pk.studentId in ( :studentIds) "),
	@NamedQuery(name = StudentCourse.DELETE_ALL_NOT_UPDATED_SC_BOOKING, query = "delete from StudentCourse sc where sc.modifiedDate is not null and sc.modifiedDate < :lastImportDate") })
public class StudentCourse {
	
	@Id
	private long studentId;
	
	@Id
	private long courseId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;
    
    @ManyToOne
    @PrimaryKeyJoinColumn(name="student_id", referencedColumnName="id")    
    private Student student;
    
    @ManyToOne
    @PrimaryKeyJoinColumn(name="course_id", referencedColumnName="id")    
    private Course course;
    
    public static final String DELETE_STUDENT_BY_COURSE_ID = "deleteStudentByCourseId";
    public static final String DELETE_STUDENTS_BY_COURSE_IDS = "deleteStudentsByCourseIds";

    public static final String DELETE_STUDENT_BY_STUDENT_ID = "deleteStudentByStudentId";
    public static final String DELETE_STUDENTS_BY_STUDENT_IDS = "deleteStudentsByStudentIds";
    public static final String DELETE_ALL_NOT_UPDATED_SC_BOOKING = "deleteAllNotUpdatedSCBooking";
    
    public StudentCourse(long studentId, long courseId) {
    	this.studentId = studentId;
    	this.courseId = courseId;
    }
    
    public StudentCourse() {    	
    }
    
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
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
        builder.append(this.studentId, theOther.studentId);
        builder.append(this.courseId, theOther.courseId);
        builder.append(this.modifiedDate, theOther.modifiedDate);

        return builder.isEquals();
    }

}
