package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;
import java.util.Date;
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
    
    @After
    public void after() {
    	dbInstance.rollback();
    }

    @Test
    public void testGetIdsOfAllEnabledOrgsFoundTwoOrgs() {
        int numberOfOrgsFoundBeforeInsertingTestData = orgDao.getIdsOfAllEnabledOrgs().size();
        insertTestData();
        assertEquals(numberOfOrgsFoundBeforeInsertingTestData + 2, orgDao.getIdsOfAllEnabledOrgs().size());
    }

    @Test
    public void testGetAllNotUpdatedOrgsFoundOneOrg() {
        Date now = new Date();
        int numberOfOrgsFoundBeforeInsertingTestData = orgDao.getAllNotUpdatedOrgs(now).size();
        insertTestData();
        assertEquals(numberOfOrgsFoundBeforeInsertingTestData + 2, orgDao.getAllNotUpdatedOrgs(now).size());

        orgs.get(0).setModifiedDate(now);

        dbInstance.flush();

        assertEquals(numberOfOrgsFoundBeforeInsertingTestData + 1, orgDao.getAllNotUpdatedOrgs(now).size());
    }

    @Test
    public void testDeleteByOrgIdsAsBulkDelete() {
        int numberOfOrgsFoundBeforeInsertingTestData = orgDao.getIdsOfAllEnabledOrgs().size();
        insertTestData();
        assertEquals(numberOfOrgsFoundBeforeInsertingTestData + 2, orgDao.getIdsOfAllEnabledOrgs().size());

        List<Long> orgIds = new LinkedList<>();
        orgIds.add(100L);
        orgIds.add(200L);

        orgDao.deleteByOrgIdsAsBulkDelete(orgIds);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(numberOfOrgsFoundBeforeInsertingTestData, orgDao.getIdsOfAllEnabledOrgs().size());
    }
    
    private void insertTestData() {
        // Insert some orgs
        orgs = mockDataGeneratorProvider.get().getOrgs();
        orgDao.save(orgs);
        dbInstance.flush();
    }
}