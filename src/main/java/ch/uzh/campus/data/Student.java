package ch.uzh.campus.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author Martin Schraner
 * 
 */
@SuppressWarnings("JpaQlInspection")  // Required to suppress warnings in named query GET_STUDENTS_MAPPED_TO_OLAT_USER_NAME
@Entity
@NamedQueries({
        @NamedQuery(name = Student.GET_ALL_STUDENTS_WITH_CREATED_OR_NOT_CREATED_CREATABLE_COURSES, query = "select distinct s from Student s join s.studentCourses sc where " +
                "sc.course.resourceableId is not null or " +
                "(sc.course.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = sc.course.id and o.enabled = true))"),
        @NamedQuery(name = Student.GET_ALL_NOT_MANUALLY_MAPPED_OR_TOO_OLD_ORPHANED_STUDENTS, query = "select s.id from Student s where " +
                "(s.kindOfMapping is null or s.kindOfMapping <> 'MANUAL' or s.dateOfImport < :nYearsInThePast) " +
                "and s.id not in (select sc.student.id from StudentCourse sc)"),
        @NamedQuery(name = Student.GET_STUDENTS_BY_EMAIL, query = "select s from Student s where s.email = :email"),
        @NamedQuery(name = Student.GET_STUDENTS_WITH_REGISTRATION_NUMBER, query = "select s from Student s where s.registrationNr = :registrationNr"),
        @NamedQuery(name = Student.GET_STUDENTS_MAPPED_TO_OLAT_USER_NAME, query = "select s from Student s where s.mappedIdentity.name = :olatUserName"),
        @NamedQuery(name = Student.DELETE_BY_STUDENT_IDS, query = "delete from Student s where s.id in :studentIds")
})
@Table(name = "ck_student")
public class Student {
	
    @Id    
    private Long id;

    @Column(name = "registration_nr", nullable = false)
    private String registrationNr;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "email", nullable = false)
    private String email;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_import", nullable = false)
    private Date dateOfImport;

    @Column(name = "kind_of_mapping")
    private String kindOfMapping;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_mapping")
    private Date dateOfMapping;

    @OneToMany(mappedBy = "student")
    private Set<StudentCourse> studentCourses = new HashSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @ManyToOne(targetEntity=IdentityImpl.class)
    @JoinColumn(name = "fk_mapped_identity")
    private Identity mappedIdentity;

    static final String GET_ALL_STUDENTS_WITH_CREATED_OR_NOT_CREATED_CREATABLE_COURSES = "getAllStudentsWithCreatedOrNotCreatedCreatableCourses";
    static final String GET_ALL_NOT_MANUALLY_MAPPED_OR_TOO_OLD_ORPHANED_STUDENTS = "getAllNotManuallyMappedOrTooOldOrphanedStudents";
    static final String GET_STUDENTS_BY_EMAIL = "getStudentsWithEmail";
    static final String GET_STUDENTS_WITH_REGISTRATION_NUMBER = "getStudentsWithRegistrationNr";
    static final String GET_STUDENTS_MAPPED_TO_OLAT_USER_NAME = "getStudentsMappedToOlatUserName";
    static final String DELETE_BY_STUDENT_IDS = "deleteStudentByStudentIds";

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

    public Date getDateOfImport() {
        return dateOfImport;
    }

    public void setDateOfImport(Date modifiedDate) {
        this.dateOfImport = modifiedDate;
    }

    public String getKindOfMapping() {
        return kindOfMapping;
    }

    public void setKindOfMapping(String kindOfMapping) {
        this.kindOfMapping = kindOfMapping;
    }

    public Date getDateOfMapping() {
        return dateOfMapping;
    }

    public void setDateOfMapping(Date dateOfMapping) {
        this.dateOfMapping = dateOfMapping;
    }

    public Identity getMappedIdentity() {
        return mappedIdentity;
    }

    public void setMappedIdentity(Identity mappedIdentity) {
        this.mappedIdentity = mappedIdentity;
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
        builder.append("mappedUserName", mappedIdentity == null ? "-" : mappedIdentity.getName());

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
        builder.append(this.dateOfImport);

        return builder.toHashCode();
    }

}
