package ch.uzh.campus.data;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToMany;
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
 * Initial Date: 04.06.2012 <p>
 * 
 * The Student and Course have a ManyToMany relationship, the relational join table (ck_student_course) has additional data (modifiedDate).
 * @link https://en.wikibooks.org/wiki/Java_Persistence/ManyToMany#Example_of_a_ManyToMany_relationship_database 
 * 
 * @author aabouc
 * @author lavinia
 */

//@Entity
//@Table(name = "ck_student_course")
//@IdClass(StudentCoursePK.class)
//@NamedQueries({
        //@NamedQuery(name = StudentCourse.DELETE_STUDENT_BY_COURSE_ID, query = "delete from StudentCourse sc where sc.pk.courseId = :courseId "),
	//@NamedQuery(name = StudentCourse.DELETE_STUDENTS_BY_COURSE_IDS, query = "delete from StudentCourse sc where sc.pk.courseId in ( :courseIds) "),
	//@NamedQuery(name = StudentCourse.DELETE_STUDENT_BY_STUDENT_ID, query = "delete from StudentCourse sc where sc.pk.studentId = :studentId "),
	//@NamedQuery(name = StudentCourse.DELETE_STUDENTS_BY_STUDENT_IDS, query = "delete from StudentCourse sc where sc.pk.studentId in ( :studentIds) "),
//	@NamedQuery(name = StudentCourse.DELETE_ALL_NOT_UPDATED_SC_BOOKING, query = "delete from StudentCourse sc where sc.modifiedDate is not null and sc.modifiedDate < :lastImportDate") })
public class StudentCourse {
	
	//@Id //the @Id could be either on this or on student field.
	private Long studentId;
	
	//@Id
	private Long courseId;

	//@Temporal(TemporalType.TIMESTAMP)
	//@Column(name = "modified_date")
	//private Date modifiedDate;
    
	//@Id
	//@ManyToOne
	//@PrimaryKeyJoinColumn(name="student_id", referencedColumnName="id")    
	//private Student student;
    
	//@Id
	//@ManyToOne
	//@PrimaryKeyJoinColumn(name="course_id", referencedColumnName="id")    
	//private Course course;
    
	//public static final String DELETE_STUDENT_BY_COURSE_ID = "deleteStudentByCourseId";
	//public static final String DELETE_STUDENTS_BY_COURSE_IDS = "deleteStudentsByCourseIds";

	//public static final String DELETE_STUDENT_BY_STUDENT_ID = "deleteStudentByStudentId";
	//public static final String DELETE_STUDENTS_BY_STUDENT_IDS = "deleteStudentsByStudentIds";
	//public static final String DELETE_ALL_NOT_UPDATED_SC_BOOKING = "deleteAllNotUpdatedSCBooking";
    
        
    public StudentCourse(Long studentId, Long courseId) {
    	this.studentId = studentId;
    	this.courseId = courseId;
    }
    
    public StudentCourse() {    	
    }
    
    
	public Long getStudentId() {
		return studentId;
	}

	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}

	public Long getCourseId() {
		return courseId;
	}

	public void setCourseId(Long courseId) {
		this.courseId = courseId;
	}

	@Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        
        builder.append("studentId", studentId);
        builder.append("courseId", courseId);
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
        if (!(obj instanceof StudentCourse))
            return false;
        StudentCourse theOther = (StudentCourse) obj;
        EqualsBuilder builder = new EqualsBuilder();
                
        builder.append(this.studentId, theOther.studentId);
        builder.append(this.courseId, theOther.courseId);
       
        return builder.isEquals();
    }

}
