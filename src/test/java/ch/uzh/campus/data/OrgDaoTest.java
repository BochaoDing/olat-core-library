package ch.uzh.campus.data;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Martin Schraner
 */

@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml" })
public class OrgDaoTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;

    @Autowired
    private OrgDao orgDao;

    @Autowired
    private MockDataGenerator mockDataGenerator;

    private List<Org> orgs;

    @Before
    public void setup() {
    	orgs = mockDataGenerator.getOrgs();
    	assertEquals(2, orgs.size());
    }

    
    @Test
    public void testGetIdsOfAllEnabledOrgs_notFound() {
        assertTrue(orgDao.getIdsOfAllEnabledOrgs().isEmpty());
    }

    
    @Test
    public void testGetIdsOfAllEnabledOrgs_foundTwoOrgs() {
        orgs = mockDataGenerator.getOrgs();
        orgDao.save(orgs);

        assertEquals(orgDao.getIdsOfAllEnabledOrgs().size(), 2);
    }

    
    @Test
    public void testGetAllNotUpdatedOrgs_notFound() {
        assertTrue(orgDao.getAllNotUpdatedOrgs(new Date()).isEmpty());
    }

    @Ignore
    @Test
    public void testGetAllNotUpdatedOrgs_foundOneOrg() {
//        orgs = mockDataGenerator.getOrgs();
//        orgDao.saveOrUpdate(orgs);
//
//        Date now = new Date();
//
//        orgs.get(0).setModifiedDate(now);
//        orgDao.saveOrUpdate(orgs);
//
//        assertEquals(orgDao.getAllNotUpdatedOrgs(now).size(), 1);
    }

    @Ignore
    @Test
    public void testDeleteByOrgIds() {
//        orgs = mockDataGenerator.getOrgs();
//        orgDao.saveOrUpdate(orgs);
//
//        assertEquals(orgDao.getIdsOfAllEnabledOrgs().size(), 2);
//
//        List<Long> orgIds = new LinkedList<Long>();
//        orgIds.add(100L);
//        orgIds.add(200L);
//
//        orgDao.deleteByOrgIds(orgIds);
//
//        assertEquals(orgDao.getIdsOfAllEnabledOrgs().size(), 0);

    }


}