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
 */
@SuppressWarnings("JpaQlInspection")  // Required to suppress warnings in named query GET_LECTURERS_MAPPED_TO_OLAT_USER_NAME
@Entity
@Table(name = "ck_lecturer")
@NamedQueries({
        @NamedQuery(name = Lecturer.GET_LECTURER_BY_EMAIL, query = "select l from Lecturer l where l.email = :email"),
        @NamedQuery(name = Lecturer.GET_ALL_LECTURERS_WITH_CREATED_OR_NOT_CREATED_CREATABLE_COURSES, query = "select distinct l from Lecturer l join l.lecturerCourses lc where " +
                "lc.course.resourceableId is not null or " +
                "(lc.course.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = lc.course.id and o.enabled = true))"),
        @NamedQuery(name = Lecturer.GET_ALL_ORPHANED_LECTURERS, query = "select l.personalNr from Lecturer l where l.personalNr not in (select lc.lecturer.personalNr from LecturerCourse lc)"),
        @NamedQuery(name = Lecturer.GET_LECTURERS_MAPPED_TO_OLAT_USER_NAME, query = "select l from Lecturer l where l.mappedIdentity.name = :olatUserName"),
        @NamedQuery(name = Lecturer.DELETE_BY_LECTURER_IDS, query = "delete from Lecturer l where l.personalNr in :lecturerIds")
})
public class Lecturer {
    @Id
    @Column(name = "ID")
    private Long personalNr;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "additionalPersonalNrs")
    private String additionalPersonalNrs;

    @Transient
    private String privateEmail;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_import", nullable = false)
    private Date dateOfImport;

    @Column(name = "kind_of_mapping")
    private String kindOfMapping;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_mapping")
    private Date dateOfMapping;

    @OneToMany(mappedBy = "lecturer")
    private Set<LecturerCourse> lecturerCourses = new HashSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @ManyToOne(targetEntity=IdentityImpl.class)
    @JoinColumn(name = "fk_mapped_identity")
    private Identity mappedIdentity;

    static final String GET_LECTURER_BY_EMAIL = "getLecturerByEmail";
    static final String GET_ALL_LECTURERS_WITH_CREATED_OR_NOT_CREATED_CREATABLE_COURSES = "getAllLecturersWithCreatedOrNotCreatedCreatableCourses";
    static final String GET_ALL_ORPHANED_LECTURERS = "getAllOrphanedLecturers";
    static final String GET_LECTURERS_MAPPED_TO_OLAT_USER_NAME = "getLecturersMappedToOlatUserName";
    static final String DELETE_BY_LECTURER_IDS = "deleteLecturerByLecturerIds";

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

    public Date getDateOfImport() {
        return dateOfImport;
    }

    public void setDateOfImport(Date dateOfImport) {
        this.dateOfImport = dateOfImport;
    }

    public String getAdditionalPersonalNrs() {
        return additionalPersonalNrs;
    }

    public void setAdditionalPersonalNrs(String additionalPersonalNrs) {
        this.additionalPersonalNrs = additionalPersonalNrs;
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
        builder.append("mappedUserName", mappedIdentity == null ? "-" : mappedIdentity.getName());
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
