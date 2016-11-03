package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Initial Date: Oct 28, 2014 <br>
 * 
 * @author Martin Schraner
 */
@ContextConfiguration(locations = {"classpath:/org/olat/_spring/mainContext.xml"})
public class DelegationDaoTest extends OlatTestCase {

    @Autowired
    private DB dbInstance;

    @Autowired
    private DelegationDao delegationDao;

    @Autowired
    private UserManager userManager;

    private Identity identity1;
    private Identity identity2;
    private Identity identity3;
    private Identity identity4;

    private Delegation delegation1;
    private Delegation delegation2;
    private Delegation delegation3;
    private Delegation delegation4;

    @Before
    public void setup() {
        insertTestData();
    }

    @After
    public void after() {
        dbInstance.rollback();
    }

    @Test
    public void testSave() {
        assertNull(dbInstance.getCurrentEntityManager().find(Delegation.class, new DelegatorDelegateeId(identity3.getKey(), identity4.getKey())));
        delegationDao.save(identity3.getKey(), identity4.getKey());
        assertNotNull(dbInstance.getCurrentEntityManager().find(Delegation.class, new DelegatorDelegateeId(identity3.getKey(), identity4.getKey())));
    }

    @Test
    public void testGetDelegationById() {
        Delegation delegationFound = delegationDao.getDelegationById(identity2.getKey(), identity1.getKey());
        assertNotNull(delegationFound);
        assertEquals(delegation3, delegationFound);

        // Look for not existing delegation
        assertNull(delegationDao.getDelegationById(identity1.getKey(), identity4.getKey()));
    }

    @Test
    public void testExistsDelegation() {
        assertTrue(delegationDao.existsDelegation(identity1.getKey(), identity2.getKey()));
        assertFalse(delegationDao.existsDelegation(identity1.getKey(), identity4.getKey()));
    }

    @Test
    public void testGetDelegationsByDelegator() {
        List<Delegation> delegationsFound = delegationDao.getDelegationsByDelegator(identity1.getKey());
        assertNotNull(delegationsFound);
        assertEquals(2, delegationsFound.size());
        assertTrue(delegationsFound.contains(delegation1));
        assertTrue(delegationsFound.contains(delegation2));
    }

    @Test
    public void testGetDelegationsByDelegatee() {
        List<Delegation> delegationsFound = delegationDao.getDelegationsByDelegatee(identity1.getKey());
        assertNotNull(delegationsFound);
        assertEquals(2, delegationsFound.size());
        assertTrue(delegationsFound.contains(delegation3));
        assertTrue(delegationsFound.contains(delegation4));
    }

    @Test
    public void testDeleteByDelegatorAndDelegatee() {
        assertNotNull(delegationDao.getDelegationById(identity1.getKey(), identity3.getKey()));
        delegationDao.deleteDelegationById(identity1.getKey(), identity3.getKey());
        dbInstance.flush();
        dbInstance.clear();
        assertNull(delegationDao.getDelegationById(identity1.getKey(), identity3.getKey()));
    }

    private void insertTestData() {
        Date now = new Date();
        identity1 = insertTestUser("user1");
        identity2 = insertTestUser("user2");
        identity3 = insertTestUser("user3");
        identity4 = insertTestUser("user4");
        delegation1 = new Delegation(identity1, identity2, now);
        delegation2 = new Delegation(identity1, identity3, now);
        delegation3 = new Delegation(identity2, identity1, now);
        delegation4 = new Delegation(identity4, identity1, now);
        delegationDao.save(delegation1);
        delegationDao.save(delegation2);
        delegationDao.save(delegation3);
        delegationDao.save(delegation4);
        dbInstance.flush();
    }

    private Identity insertTestUser(String userName) {
        User user = userManager.createUser("delegationDaoTestFirstName" + userName, "delegationDaoTestLastName" + userName, userName + "@uzh.ch");
        dbInstance.saveObject(user);
        Identity identity = new IdentityImpl(userName, user);
        dbInstance.saveObject(identity);
        dbInstance.flush();
        return identity;
    }

}
