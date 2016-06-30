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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Initial Date: Oct 28, 2014 <br>
 * 
 * @author aabouc
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class DelegationDaoTest extends OlatTestCase {

    @Autowired
    private DB dbInstance;

    @Autowired
    private DelegationDao delegationDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    private List<Delegation> delegations;

    @Before
    public void setup() {
        delegations = mockDataGeneratorProvider.get().getDelegations();
    }

    @After
    public void after() {
        dbInstance.rollback();
    }

    @Test
    public void testGetDelegationByDelegator_notFound() {
        assertTrue(delegationDao.getDelegationsByDelegator("delegatorX").isEmpty());
    }

    @Test
    public void testGetDelegationsByDelegator_foundTwoDelegations() {
        delegationDao.save(delegations.subList(0, 2));
        dbInstance.flush();

        List<Delegation> foundDelegations = delegationDao.getDelegationsByDelegator("delegator1");

        assertEquals(2, foundDelegations.size(), 2);
        assertEquals("delegatee11", foundDelegations.get(0).getDelegatee());
        assertEquals("delegatee12", foundDelegations.get(1).getDelegatee());
    }

    @Test
    public void testGetDelegationsByDelegatee_notFound() {
        assertTrue(delegationDao.getDelegationsByDelegatee("delegateeX").isEmpty());
    }

    @Test
    public void testGetDelegationsByDelegatee_foundTwoDelegations() {
        delegationDao.save(delegations.subList(2, 4));
        dbInstance.flush();

        List<Delegation> foundDelegations = delegationDao.getDelegationsByDelegatee("delegatee20");

        assertEquals(2, foundDelegations.size());
        assertEquals("delegator2", foundDelegations.get(0).getDelegator());
        assertEquals("delegator3", foundDelegations.get(1).getDelegator());
    }

    @Test
    public void testNotExistDelegation() {
        assertFalse(delegationDao.existDelegation("delegatorX", "delegateeX"));
    }

    @Test
    public void testExistDelegation() {
        delegationDao.save(delegations.get(4));
        dbInstance.flush();

        assertTrue(delegationDao.existDelegation("delegator4", "delegatee40"));
    }

    @Test
    public void testDeleteByDelegatorAndDelegatee() {
        delegationDao.save(delegations.subList(5, 6));
        dbInstance.flush();

        assertTrue(delegationDao.existDelegation("delegator5", "delegatee50"));

        delegationDao.deleteByDelegatorAndDelegatee("delegator5", "delegatee50");

        // Check before flush
        assertFalse(delegationDao.existDelegation("delegator5", "delegatee50"));

        dbInstance.flush();
        dbInstance.clear();

        assertFalse(delegationDao.existDelegation("delegator5", "delegatee50"));
    }

}
