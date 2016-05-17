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

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: 03.05.2013 <br>
 * 
 * @author aabouc
 */
@Repository
public class DelegationDao implements CampusDao<Delegation> {

    @Autowired
    private DB dbInstance;

    @Override
    public void save(List<Delegation> delegations) {
        for(Delegation delegation: delegations) {
            dbInstance.saveObject(delegation);
        }
    }

    public void save(Delegation delegation) {
        dbInstance.saveObject(delegation);
    }

    public void deleteByDelegatorAndDelegatee(String delegator, String delegatee) {
        dbInstance.getCurrentEntityManager()
                .createNamedQuery(Delegation.DELETE_BY_DELEGATOR_AND_DELEGATEE)
                .setParameter("delegator", delegator)
                .setParameter("delegatee", delegatee)
                .executeUpdate();
    }

    /** TODO Check if we really want to consume the list and not a single entry */
    public List<Delegation> getDelegationByDelegator(String delegator) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Delegation.GET_BY_DELEGATOR, Delegation.class)
                .setParameter("delegator", delegator)
                .getResultList();
    }

    /** TODO Check if we really want to consume the list and not a single entry */
    public List<Delegation> getDelegationByDelegatee(String delegatee) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Delegation.GET_BY_DELEGATEE, Delegation.class)
                .setParameter("delegatee", delegatee)
                .getResultList();
    }

    public boolean existDelegation(String delegator, String delegatee) {
        boolean isEmpty = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Delegation.GET_BY_DELEGATOR_AND_DELEGATEE)
                .setParameter("delegator", delegator)
                .setParameter("delegatee", delegatee)
                .getResultList()
                .isEmpty();

        return !isEmpty;
    }

}
