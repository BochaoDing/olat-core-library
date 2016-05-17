/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package ch.uzh.campus.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

/**
 * Initial Date: 03.05.2013 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_delegation")
@NamedQueries({
        @NamedQuery(name = Delegation.DELETE_BY_DELEGATOR_AND_DELEGATEE, query = "delete from Delegation d where d.delegator = :delegator and d.delegatee = :delegatee"),
        @NamedQuery(name = Delegation.GET_BY_DELEGATOR_AND_DELEGATEE, query = "select Delegation d where d.delegator = :delegator and d.delegatee = :delegatee"),
        @NamedQuery(name = Delegation.GET_BY_DELEGATOR, query = "select Delegation d where d.delegator = :delegator"),
        @NamedQuery(name = Delegation.GET_BY_DELEGATEE, query = "select Delegation d where d.delegatee = :delegatee")
})
public class Delegation {

    @Id
    @GeneratedValue
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;

    @Column(name = "delegator")
    private String delegator;

    @Column(name = "delegatee")
    private String delegatee;

    public static final String DELETE_BY_DELEGATOR_AND_DELEGATEE = "deleteByDelegatorAndDelegatee";
    public static final String GET_BY_DELEGATOR_AND_DELEGATEE = "getByDelegatorAndDelegatee";
    public static final String GET_BY_DELEGATOR = "deleteByDelegator";
    public static final String GET_BY_DELEGATEE = "deleteByDelegatee";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getDelegator() {
        return delegator;
    }

    public void setDelegator(String delegator) {
        this.delegator = delegator;
    }

    public String getDelegatee() {
        return delegatee;
    }

    public void setDelegatee(String delegatee) {
        this.delegatee = delegatee;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("delegator", getDelegator());
        builder.append("delegatee", getDelegatee());
        builder.append("modifiedDate", getModifiedDate());

        return builder.toString();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(delegator);
        builder.append(delegatee);
        builder.append(modifiedDate);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Delegation))
            return false;
        Delegation theOther = (Delegation) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.delegator, theOther.delegator);
        builder.append(this.delegatee, theOther.delegatee);
        builder.append(this.modifiedDate, theOther.modifiedDate);

        return builder.isEquals();
    }

}
