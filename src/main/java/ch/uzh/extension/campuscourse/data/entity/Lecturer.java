package ch.uzh.extension.campuscourse.data.entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

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
@Repository
@Scope("prototype")
@SuppressWarnings("JpaQlInspection")  // Required to suppress warnings in named query GET_LECTURERS_BY_MAPPED_IDENTITY_KEY
@NamedQueries({
        @NamedQuery(name = Lecturer.GET_LECTURER_BY_EMAIL, query = "select l from Lecturer l where l.email = :email"),
        @NamedQuery(name = Lecturer.GET_ALL_LECTURERS_WITH_CREATED_OR_NOT_CREATED_CREATABLE_COURSES, query = "select distinct l from Lecturer l join l.lecturerCourses lc where " +
                "lc.course.repositoryEntry is not null or lc.course.parentCourse is not null or " +
                "(lc.course.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = lc.course.id and o.enabled = true))"),
        @NamedQuery(name = Lecturer.GET_ALL_NOT_MANUALLY_MAPPED_OR_TOO_OLD_ORPHANED_LECTURERS, query = "select l.personalNr from Lecturer l where " +
                "(l.kindOfMapping is null or l.kindOfMapping <> 'MANUAL' or l.dateOfLatestImport < :nYearsInThePast) " +
                "and l.personalNr not in (select lc.lecturer.personalNr from LecturerCourse lc)"),
        @NamedQuery(name = Lecturer.GET_LECTURERS_BY_MAPPED_IDENTITY_KEY, query = "select l from Lecturer l where l.mappedIdentity.key = :mappedIdentityKey"),
        @NamedQuery(name = Lecturer.DELETE_BY_LECTURER_IDS, query = "delete from Lecturer l where l.personalNr in :lecturerIds")
})
public class Lecturer {

    public static final String GET_LECTURER_BY_EMAIL = "getLecturerByEmail";
    public static final String GET_ALL_LECTURERS_WITH_CREATED_OR_NOT_CREATED_CREATABLE_COURSES = "getAllLecturersWithCreatedOrNotCreatedCreatableCourses";
    public static final String GET_ALL_NOT_MANUALLY_MAPPED_OR_TOO_OLD_ORPHANED_LECTURERS = "getAllNotManuallyMappedOrTooOldOrphanedLecturers";
    public static final String GET_LECTURERS_BY_MAPPED_IDENTITY_KEY = "getLecturersByMappedIdentityKey";
    public static final String DELETE_BY_LECTURER_IDS = "deleteLecturerByLecturerIds";

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

    @Column(name = "kind_of_mapping")
    private String kindOfMapping;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_mapping")
    private Date dateOfMapping;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_of_first_import", nullable = false)
	private Date dateOfFirstImport;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_of_latest_import", nullable = false)
	private Date dateOfLatestImport;

    @OneToMany(mappedBy = "lecturer")
    private Set<LecturerCourse> lecturerCourses = new HashSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @ManyToOne(targetEntity=IdentityImpl.class)
    @JoinColumn(name = "fk_mapped_identity")
    private Identity mappedIdentity;

    public Lecturer() {
    }

    public Lecturer(Long personalNr) {
        this.personalNr = personalNr;
    }

    public Lecturer(Long personalNr, String firstName, String lastName, String email, Date dateOfLatestImport) {
        this.personalNr = personalNr;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;


		this.dateOfLatestImport = dateOfLatestImport;
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

	public Date getDateOfFirstImport() {
		return dateOfFirstImport;
	}

	public void setDateOfFirstImport(Date dateOfFirstImport) {
		this.dateOfFirstImport = dateOfFirstImport;
	}

	public Date getDateOfLatestImport() {
		return dateOfLatestImport;
	}

	public void setDateOfLatestImport(Date dateOfImport) {
		this.dateOfLatestImport = dateOfImport;
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

    public void mergeImportedAttributesInto(Lecturer lecturerToBeUpdated) {
		// all imported attributes, except id and date of first import
        lecturerToBeUpdated.setFirstName(getFirstName());
        lecturerToBeUpdated.setLastName(getLastName());
        lecturerToBeUpdated.setEmail(getEmail());
        lecturerToBeUpdated.setAdditionalPersonalNrs(getAdditionalPersonalNrs());
		lecturerToBeUpdated.setDateOfLatestImport(getDateOfLatestImport());
    }

}
