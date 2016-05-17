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
import org.junit.runner.RunWith;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    private MockDataGenerator mockDataGenerator;

    private List<Delegation> delegations;

    @Before
    public void setup() {
        delegations = mockDataGenerator.getDelegations();
    }

    @After
    public void after() {
        dbInstance.rollback();
    }

    @Test
    public void testGetDelegationByDelegator_notFound() {
        assertTrue(delegationDao.getDelegationByDelegator("delegatorX").isEmpty());
    }

    @Test
    public void testGetDelegationByDelegator_foundTwoDelegations() {
        delegationDao.save(delegations.subList(0, 2));
        dbInstance.flush();
        List<Delegation> foundDelegations = delegationDao.getDelegationByDelegator("delegator1");
        assertEquals(foundDelegations.size(), 2);
        assertEquals(foundDelegations.get(0).getDelegatee(), "delegatee11");
        assertEquals(foundDelegations.get(1).getDelegatee(), "delegatee12");
    }

    @Test
    public void testGetDelegationByDelegatee_notFound() {
        assertTrue(delegationDao.getDelegationByDelegatee("delegateeX").isEmpty());
    }

    @Test
    public void testGetDelegationByDelegatee_foundTwoDelegations() {
        delegationDao.save(delegations.subList(2, 4));
        dbInstance.flush();
        List<Delegation> foundDelegations = delegationDao.getDelegationByDelegatee("delegatee20");
        assertEquals(foundDelegations.size(), 2);
        assertEquals(foundDelegations.get(0).getDelegator(), "delegator2");
        assertEquals(foundDelegations.get(1).getDelegator(), "delegator3");
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
        assertFalse(delegationDao.existDelegation("delegator5", "delegatee50"));
    }

}
