package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;
import java.util.*;

import static org.junit.Assert.*;
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
    private CourseDao courseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    private List<Org> orgs;
    
    @After
    public void after() {
    	dbInstance.rollback();
    }

    @Test
    public void getOrgById() {
        insertTestData();
        assertNull(orgDao.getOrgById(999L));
        assertNotNull(orgDao.getOrgById(9100L));
    }

    @Test
    public void testGetIdsOfAllEnabledOrgsFoundEightOrgs() {
        int numberOfOrgsFoundBeforeInsertingTestData = orgDao.getIdsOfAllEnabledOrgs().size();
        insertTestData();
        assertEquals(numberOfOrgsFoundBeforeInsertingTestData + 9, orgDao.getIdsOfAllEnabledOrgs().size());
    }

    @Test
    public void testGetAllNotUpdatedOrgsFoundSevenOrg() {
        Date now = new Date();
        int numberOfOrgsFoundBeforeInsertingTestData = orgDao.getAllNotUpdatedOrgs(now).size();
        insertTestData();
        assertEquals(numberOfOrgsFoundBeforeInsertingTestData + 9, orgDao.getAllNotUpdatedOrgs(now).size());

        orgs.get(0).setModifiedDate(now);

        dbInstance.flush();

        assertEquals(numberOfOrgsFoundBeforeInsertingTestData + 8, orgDao.getAllNotUpdatedOrgs(now).size());
    }

    @Test
    public void testDeleteByOrgIds() {
        insertTestData();
        Org org = orgDao.getOrgById(9100L);
        assertNotNull(org);
        assertEquals(2, org.getCourses().size());
        Set<Long> courseIds = new HashSet<>();
        for (Course course : org.getCourses()) {
            courseIds.add(course.getId());
        }
        assertTrue(courseIds.contains(100L));
        assertTrue(courseIds.contains(300L));
        Course course1 = courseDao.getCourseById(100L);
        assertNotNull(course1);
        assertEquals(1, course1.getOrgs().size());
        Course course2 = courseDao.getCourseById(300L);
        assertNotNull(course2);
        assertEquals(9, course2.getOrgs().size());

        orgDao.deleteByOrgId(9100L);

        // Check before flush
        assertEquals(0, course1.getOrgs().size());
        assertEquals(8, course2.getOrgs().size());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(orgDao.getOrgById(9100L));

        course1 = courseDao.getCourseById(100L);
        assertNotNull(course1);
        assertEquals(0, course1.getOrgs().size());
        course2 = courseDao.getCourseById(300L);
        assertNotNull(course2);
        assertEquals(8, course2.getOrgs().size());
    }
    
    private void insertTestData() {
        // Insert some orgs
        orgs = mockDataGeneratorProvider.get().getOrgs();
        orgDao.save(orgs);
        dbInstance.flush();

        // Insert some courses
        List<CourseOrgId> courseOrgIds = mockDataGeneratorProvider.get().getCourseOrgIds();
        courseDao.save(courseOrgIds);
        dbInstance.flush();
    }
}