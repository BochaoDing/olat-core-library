package ch.uzh.extension.campuscourse.data.entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Initial Date: 07.12.2012 <br>
 *
 * @author aabouc
 * @author Martin Schraner
 */
@Entity
@Table(name = "ck_org")
@Repository
@Scope("prototype")
@NamedQueries({ @NamedQuery(name = Org.GET_IDS_OF_ALL_ENABLED_ORGS, query = "select id from Org"),
        @NamedQuery(name = Org.GET_ALL_ORPHANED_ORGS, query = "select o.id from Org o where o.id not in (select o1.id from Course c join c.orgs o1)")})
public class Org {

    public static final String GET_IDS_OF_ALL_ENABLED_ORGS = "getIdsOfAllEnabledOrgs";
    public static final String GET_ALL_ORPHANED_ORGS = "getAllOrphanedOrgs";

    public Org() {
    }

    public Org(Long id, String shortName, String name, boolean enabled, Date dateOfImport) {
        this.id = id;
        this.shortName = shortName;
        this.name = name;
        this.enabled = enabled;
        this.dateOfImport = dateOfImport;
    }

    @Id
    private Long id;

    @Column(name = "short_name", nullable = false)
    private String shortName;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_import")
    private Date dateOfImport;

    @ManyToMany(mappedBy = "orgs")
    private Set<Course> courses = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getDateOfImport() {
        return dateOfImport;
    }

    public void setDateOfImport(Date dateOfImport) {
        this.dateOfImport = dateOfImport;
    }

    public Set<Course> getCourses() {
        return courses;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("shortName", getShortName());
        builder.append("name", getName());
        builder.append("enabled", isEnabled());
        builder.append("modifiedDate", getDateOfImport());

        return builder.toString();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(shortName);
        builder.append(name);
        builder.append(dateOfImport);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Org))
            return false;
        Org theOther = (Org) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.shortName, theOther.shortName);
        builder.append(this.name, theOther.name);
        builder.append(this.dateOfImport, theOther.dateOfImport);

        return builder.isEquals();
    }

}

