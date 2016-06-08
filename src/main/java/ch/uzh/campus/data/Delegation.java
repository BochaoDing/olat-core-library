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

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Date;

/**
 * Initial Date: 03.05.2013 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_delegation")
@NamedQueries({
        @NamedQuery(name = Delegation.DELETE_BY_DELEGATOR_AND_DELEGATEE, query = "delete from Delegation d where d.delegator = :delegator and d.delegatee = :delegatee"),
        @NamedQuery(name = Delegation.GET_BY_DELEGATOR_AND_DELEGATEE, query = "select d from Delegation d where d.delegator = :delegator and d.delegatee = :delegatee"),
        @NamedQuery(name = Delegation.GET_BY_DELEGATOR, query = "select d from Delegation d where d.delegator = :delegator"),
        @NamedQuery(name = Delegation.GET_BY_DELEGATEE, query = "select d from Delegation d where d.delegatee = :delegatee")
})
public class Delegation {

    @Id
    @GeneratedValue
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;

    @Column(name = "delegator", nullable = false)
    private String delegator;

    @Column(name = "delegatee", nullable = false)
    private String delegatee;

    static final String DELETE_BY_DELEGATOR_AND_DELEGATEE = "deleteByDelegatorAndDelegatee";
    static final String GET_BY_DELEGATOR_AND_DELEGATEE = "getByDelegatorAndDelegatee";
    static final String GET_BY_DELEGATOR = "getByDelegator";
    static final String GET_BY_DELEGATEE = "getByDelegatee";

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Delegation that = (Delegation) o;

        if (!delegator.equals(that.delegator)) return false;
        return delegatee.equals(that.delegatee);

    }

    @Override
    public int hashCode() {
        int result = delegator.hashCode();
        result = 31 * result + delegatee.hashCode();
        return result;
    }
}
