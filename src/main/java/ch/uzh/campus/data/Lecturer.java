package ch.uzh.campus.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author Martin Schraner
 */
@Entity
@Table(name = "ck_lecturer")
@NamedQueries({
        @NamedQuery(name = Lecturer.GET_LECTURER_BY_EMAIL, query = "select l from Lecturer l where l.email = :email"),
        @NamedQuery(name = Lecturer.GET_ALL_PILOT_LECTURERS, query = "select distinct l from Lecturer l left join l.lecturerCourses lc where lc.course.enabled = '1' "),
        @NamedQuery(name = Lecturer.GET_ALL_NOT_UPDATED_LECTURERS, query = "select l.personalNr from Lecturer l where l.modifiedDate < :lastImportDate")
})
public class Lecturer {
    @Id
    @Column(name = "ID")
    private Long personalNr;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "additionalPersonalNrs")
    private String additionalPersonalNrs;

    @Transient
    private String privateEmail;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;

    @OneToMany(mappedBy = "lecturer")
    private Set<LecturerCourse> lecturerCourses = new HashSet<>();

    public static final String GET_LECTURER_BY_EMAIL = "getLecturerByEmail";
    public static final String GET_ALL_PILOT_LECTURERS = "getAllPilotLecturers";
    public static final String GET_ALL_NOT_UPDATED_LECTURERS = "getAllNotUpdatedLecturers";

    public Lecturer() {
    }

    public Lecturer(Long personalNr) {
        this.personalNr = personalNr;
    }

    public Long getPersonalNr() {
        return personalNr;
    }

    public void setPersonalNr(Long personalNr) {
        this.personalNr = personalNr;
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

    public String getPrivateEmail() {
        return privateEmail;
    }

    public void setPrivateEmail(String privateEmail) {
        this.privateEmail = privateEmail;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getAdditionalPersonalNrs() {
        return additionalPersonalNrs;
    }

    public void setAdditionalPersonalNrs(String additionalPersonalNrs) {
        this.additionalPersonalNrs = additionalPersonalNrs;
    }

    public Set<LecturerCourse> getLecturerCourses() {
        return lecturerCourses;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("personalNr", getPersonalNr());
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
        if (!(obj instanceof Lecturer))
            return false;
        Lecturer theOther = (Lecturer) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.personalNr, theOther.personalNr);
        builder.append(this.firstName, theOther.firstName);
        builder.append(this.lastName, theOther.lastName);
        builder.append(this.email, theOther.email);

        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(this.personalNr);
        builder.append(this.firstName);
        builder.append(this.lastName);
        builder.append(this.email);

        return builder.toHashCode();
    }

}
