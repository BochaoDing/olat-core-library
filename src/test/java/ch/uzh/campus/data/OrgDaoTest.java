package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Schraner
 */

@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class OrgDaoTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;

    @Autowired
    private OrgDao orgDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    private List<Org> orgs;

    @Before
    public void setup() {
    	orgs = mockDataGeneratorProvider.get().getOrgs();
    	assertTrue(orgs.size() >= 2);
    	assertEquals(0, orgs.size()%2);
    }
    
    @After
    public void after() {
    	dbInstance.rollback();
    }

    @Test
    public void testGetIdsOfAllEnabledOrgs_notFound() {
    	assertEquals(0, orgDao.getIdsOfAllEnabledOrgs().size());
    }

    @Test
    public void testGetIdsOfAllEnabledOrgs_foundTwoOrgs() {
        orgDao.save(orgs);
        dbInstance.flush();
        assertEquals(2, orgDao.getIdsOfAllEnabledOrgs().size());
    }

    @Test
    public void testGetAllNotUpdatedOrgs_notFound() {
    	assertEquals(0, orgDao.getAllNotUpdatedOrgs(new Date()).size());
    }

    @Test
    public void testGetAllNotUpdatedOrgs_foundOneOrg() {
        orgDao.save(orgs);
        dbInstance.flush();

        Calendar now = new GregorianCalendar();
        // To avoid rounding problems
        Calendar nowMinusOneSecond = (Calendar) now.clone();
        nowMinusOneSecond.add(Calendar.SECOND, -1);

        orgs.get(0).setModifiedDate(now.getTime());
        dbInstance.flush();
        assertEquals(1, orgDao.getAllNotUpdatedOrgs(nowMinusOneSecond.getTime()).size());
    }

    @Test
    public void testDeleteByOrgIds() {
        orgDao.save(orgs);
        dbInstance.flush();
        assertEquals(2, orgDao.getIdsOfAllEnabledOrgs().size());

        List<Long> orgIds = new LinkedList<>();
        orgIds.add(100L);
        orgIds.add(200L);

        orgDao.deleteByOrgIds(orgIds);
        dbInstance.flush();
        assertEquals(0, orgDao.getIdsOfAllEnabledOrgs().size());
    }
    
    


}