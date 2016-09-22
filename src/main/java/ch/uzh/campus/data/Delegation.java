package ch.uzh.campus.data;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;

import javax.persistence.*;
import java.util.Date;

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
 *
 * Initial Date: 03.05.2013 <br>
 * 
 * @author aabouc
 */
@SuppressWarnings("JpaQlInspection")
@Entity
@Table(name = "ck_delegation")
@IdClass(DelegatorDelegateeId.class)
@NamedQueries({
        @NamedQuery(name = Delegation.GET_BY_DELEGATOR, query = "select d from Delegation d where d.delegator.key = :delegatorKey"),
        @NamedQuery(name = Delegation.GET_BY_DELEGATEE, query = "select d from Delegation d where d.delegatee.key = :delegateeKey")
})
public class Delegation {

    @Id
    @SuppressWarnings("JpaAttributeTypeInspection")
    @ManyToOne(targetEntity = IdentityImpl.class, optional = false)
    @JoinColumn(name = "fk_delegator_identity")
    private Identity delegator;

    @Id
    @SuppressWarnings("JpaAttributeTypeInspection")
    @ManyToOne(targetEntity = IdentityImpl.class, optional = false)
    @JoinColumn(name = "fk_delegatee_identity")
    private Identity delegatee;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creationdate", nullable = false)
    private Date creationDate;

    static final String GET_BY_DELEGATOR = "getByDelegator";
    static final String GET_BY_DELEGATEE = "getByDelegatee";

    public Delegation() {
    }

    public Delegation(Identity delegator, Identity delegatee, Date creationDate) {
        this.delegator = delegator;
        this.delegatee = delegatee;
        this.creationDate = creationDate;
    }

    public Identity getDelegator() {
        return delegator;
    }

    public void setDelegator(Identity delegator) {
        this.delegator = delegator;
    }

    public Identity getDelegatee() {
        return delegatee;
    }

    public void setDelegatee(Identity delegatee) {
        this.delegatee = delegatee;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date modifiedDate) {
        this.creationDate = modifiedDate;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("delegator key", getDelegator().getKey());
        builder.append("delegatee key", getDelegatee().getKey());
        builder.append("creationDate", getCreationDate());

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
