package ch.uzh.campus.data;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author Martin Schraner
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = Student.GET_ALL_PILOT_STUDENTS, query = "select distinct s from Student s left join s.studentCourses sc where sc.course.enabled = '1' "),
        @NamedQuery(name = Student.GET_ALL_NOT_UPDATED_STUDENTS, query = "select s.id from Student s where s.modifiedDate < :lastImportDate"),
        @NamedQuery(name = Student.GET_STUDENTS_BY_EMAIL, query = "select s from Student s where s.email = :email"),
        @NamedQuery(name = Student.GET_STUDENTS_WITH_REGISTRATION_NUMBER, query = "select s from Student s where s.registrationNr = :registrationNr")
})
@Table(name = "ck_student")
public class Student {
	
    @Id    
    private Long id;

    @Column(name = "registration_nr")
    private String registrationNr;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "email")
    private String email;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;

    @OneToMany(mappedBy = "student")
    private Set<StudentCourse> studentCourses = new HashSet<>();

    public static final String GET_ALL_PILOT_STUDENTS = "getAllPilotStudents";
    public static final String GET_ALL_NOT_UPDATED_STUDENTS = "getAllNotUpdatedStudents";
    public static final String GET_STUDENTS_BY_EMAIL = "getStudentsWithEmail";
    public static final String GET_STUDENTS_WITH_REGISTRATION_NUMBER = "getStudentsWithRegistrationNr";

    public Student() {
    }

    public Student(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRegistrationNr() {
        return registrationNr;
    }

    public void setRegistrationNr(String registrationNr) {
        this.registrationNr = registrationNr;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Set<StudentCourse> getStudentCourses() {
        return studentCourses;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("registrationNr", getRegistrationNr());
        builder.append("firstName", getFirstName());
        builder.append("lastName", getLastName());
        builder.append("email", getEmail());

        return builder.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Student))
            return false;
        Student theOther = (Student) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.firstName, theOther.firstName);
        builder.append(this.lastName, theOther.lastName);
        builder.append(this.email, theOther.email);

        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(this.firstName);
        builder.append(this.lastName);
        builder.append(this.email);
        builder.append(this.modifiedDate);

        return builder.toHashCode();
    }

}
