package ch.uzh.campus.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Date;

/**
 * Initial Date: 07.12.2012 <br>
 *
 * @author aabouc
 * @author Martin Schraner
 */
@Entity
@Table(name = "ck_org")
@NamedQueries({ @NamedQuery(name = Org.GET_IDS_OF_ALL_ENABLED_ORGS, query = "select id from Org"),
        @NamedQuery(name = Org.GET_ALL_NOT_UPDATED_ORGS, query = "select o.id from Org o where o.modifiedDate < :lastImportDate"),
        @NamedQuery(name = Org.DELETE_ALL_NOT_UPDATED_ORGS, query = "delete from Org o where o.modifiedDate < :lastImportDate"),
        @NamedQuery(name = Org.DELETE_BY_ORG_IDS, query = "delete from Org o where o.id in ( :orgIds)") })
public class Org {
    @Id
    private Long id;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "name")
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;

    public static final String GET_IDS_OF_ALL_ENABLED_ORGS = "getIdsOfAllEnabledOrgs";
    public static final String GET_ALL_NOT_UPDATED_ORGS = "getAllNotUpdatedOrgs";
    public static final String DELETE_ALL_NOT_UPDATED_ORGS = "deleteAllNotUpdatedOrgs";
    public static final String DELETE_BY_ORG_IDS = "deleteByOrgIds";

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

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("shortName", getShortName());
        builder.append("name", getName());
        builder.append("modifiedDate", getModifiedDate());

        return builder.toString();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(shortName);
        builder.append(name);
        builder.append(modifiedDate);

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
        builder.append(this.modifiedDate, theOther.modifiedDate);

        return builder.isEquals();
    }

}

