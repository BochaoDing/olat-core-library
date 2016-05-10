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
 * The Student and Course have a ManyToMany relationship, this is the composite primary key.
 * 
 * @link https://en.wikibooks.org/wiki/Java_Persistence/ManyToMany#Example_of_a_ManyToMany_relationship_database 
 *  
 * @author aabouc
 * @author lavinia
 */
@Deprecated//this no more needed since the Course and Student have a @ManyToMany relation
@SuppressWarnings("serial")
public class StudentCoursePK implements Serializable {
    
    private Long student;    
    private Long course;

    public StudentCoursePK() {
    }

    public StudentCoursePK(Long student, Long course) {
        this.student = student;
        this.course = course;
    }

    public Long getStudent() {
		return student;
	}

	public void setStudent(Long student) {
		this.student = student;
	}

	public Long getCourse() {
		return course;
	}

	public void setCourse(Long course) {
		this.course = course;
	}

	@Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("student", getStudent());
        builder.append("course", getCourse());
        return builder.toString();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(student);
        builder.append(course);

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
        builder.append(this.student, theOther.student);
        builder.append(this.course, theOther.course);

        return builder.isEquals();
    }

}
