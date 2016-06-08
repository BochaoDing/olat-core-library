package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
        // Insert some orgs
    	orgs = mockDataGeneratorProvider.get().getOrgs();
        orgDao.save(orgs);
        dbInstance.flush();
    }
    
    @After
    public void after() {
    	dbInstance.rollback();
    }

    @Test
    public void testGetIdsOfAllEnabledOrgs_foundTwoOrgs() {
        assertEquals(2, orgDao.getIdsOfAllEnabledOrgs().size());
    }

    @Test
    public void testGetAllNotUpdatedOrgs_foundOneOrg() {
        Calendar now = new GregorianCalendar();
        // To avoid rounding problems
        Calendar nowMinusOneSecond = (Calendar) now.clone();
        nowMinusOneSecond.add(Calendar.SECOND, -1);

        assertEquals(2, orgDao.getAllNotUpdatedOrgs(nowMinusOneSecond.getTime()).size());

        orgs.get(0).setModifiedDate(now.getTime());
        dbInstance.flush();
        dbInstance.clear();

        assertEquals(1, orgDao.getAllNotUpdatedOrgs(nowMinusOneSecond.getTime()).size());
    }

    @Test
    public void testDeleteByOrgIdsAsBulkDelete() {
        assertEquals(2, orgDao.getIdsOfAllEnabledOrgs().size());

        List<Long> orgIds = new LinkedList<>();
        orgIds.add(100L);
        orgIds.add(200L);

        orgDao.deleteByOrgIdsAsBulkDelete(orgIds);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, orgDao.getIdsOfAllEnabledOrgs().size());
    }
    
    


}