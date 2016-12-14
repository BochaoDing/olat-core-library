package ch.uzh.campus.data;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseException;
import ch.uzh.campus.CampusCourseTestCase;
import ch.uzh.campus.utils.DateUtil;
import org.junit.Test;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Initial Date: Oct 28, 2014 <br>
 *
 * @author aabouc
 * @author Martin Schraner
 */

@Component
public class LecturerDaoTest extends CampusCourseTestCase {

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    private LecturerDao lecturerDao;

    @Autowired
    private OrgDao orgDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private SemesterDao semesterDao;

    @Autowired
    private LecturerCourseDao lecturerCourseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    @Autowired
    private UserManager userManager;

    @Autowired
    private OLATResourceManager olatResourceManager;

    private List<Lecturer> lecturers;

    @Test
    public void testAddMapping() throws CampusCourseException {
        insertTestData();
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertNull(lecturer.getMappedIdentity());
        assertNull(lecturer.getKindOfMapping());
        assertNull(lecturer.getDateOfMapping());

        Identity identity = insertTestUser("lecturerDaoTestUser");

        lecturerDao.addMapping(lecturer.getPersonalNr(), identity);

        // Check before flush
        assertEquals(identity, lecturer.getMappedIdentity());
        assertEquals("AUTO", lecturer.getKindOfMapping());
        assertNotNull(lecturer.getDateOfMapping());

        dbInstance.flush();
        dbInstance.clear();

        lecturer = lecturerDao.getLecturerById(1100L);
        assertEquals(identity, lecturer.getMappedIdentity());
        assertEquals("AUTO", lecturer.getKindOfMapping());
        assertNotNull(lecturer.getDateOfMapping());
    }

    @Test
    public void testRemoveMapping() throws CampusCourseException {
        insertTestData();
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);

        // Add mapping
        Identity identity = insertTestUser("lecturerDaoTestUser");
        lecturerDao.addMapping(lecturer.getPersonalNr(), identity);
        dbInstance.flush();
        dbInstance.clear();

        lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer.getMappedIdentity());
        assertNotNull(lecturer.getKindOfMapping());
        assertNotNull(lecturer.getDateOfMapping());

        // Remove mapping
        lecturerDao.removeMapping(lecturer.getPersonalNr());

        // Check before flush
        assertNull(lecturer.getMappedIdentity());
        assertNull(lecturer.getKindOfMapping());
        assertNull(lecturer.getDateOfMapping());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(lecturer.getMappedIdentity());
        assertNull(lecturer.getKindOfMapping());
        assertNull(lecturer.getDateOfMapping());
    }

    @Test
    public void testNullGetLecturerById() throws CampusCourseException {
        insertTestData();
        assertNull(lecturerDao.getLecturerById(1999L));
    }

    @Test
    public void testNotNullGetLecturerById() throws CampusCourseException {
        insertTestData();
        assertNotNull(lecturerDao.getLecturerById(1100L));
    }

    @Test
    public void testNullGetLecturerByEmail() throws CampusCourseException {
        insertTestData();
        assertNull(lecturerDao.getLecturerByEmail("wrongEmail"));
    }

    @Test
    public void testNotNullGetLecturerByEmail() throws CampusCourseException {
        insertTestData();
        assertNotNull(lecturerDao.getLecturerByEmail("email1"));
    }

    @Test
    public void testGetAllNotManuallyMappedOrTooOldOrphanedLecturers() throws InterruptedException, CampusCourseException {
        Date referenceDateOfImport = new Date();
        int numberOfLecturersFoundBeforeInsertingTestData = lecturerDao.getAllNotManuallyMappedOrTooOldOrphanedLecturers(referenceDateOfImport).size();
        insertTestData();

        // Lecturer 1700 has no courses. i.e. it is orphaned and not mapped (-> should be selected)
        Lecturer lecturer = lecturerDao.getLecturerById(1700L);
        assertTrue(lecturer.getLecturerCourses().isEmpty());
        assertNull(lecturer.getKindOfMapping());
        assertEquals(numberOfLecturersFoundBeforeInsertingTestData + 1, lecturerDao.getAllNotManuallyMappedOrTooOldOrphanedLecturers(referenceDateOfImport).size());

        // Map lecturer auto and set time of import not too far in the past (-> should be selected)
        lecturer.setKindOfMapping("AUTO");
        lecturer.setDateOfImport(DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() + 1));
        dbInstance.flush();
        assertEquals(numberOfLecturersFoundBeforeInsertingTestData + 1, lecturerDao.getAllNotManuallyMappedOrTooOldOrphanedLecturers(referenceDateOfImport).size());

        // Map lecturer auto and set time of import too far in the past (-> should be selected)
        lecturer.setDateOfImport(DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1));
        dbInstance.flush();
        assertEquals(numberOfLecturersFoundBeforeInsertingTestData + 1, lecturerDao.getAllNotManuallyMappedOrTooOldOrphanedLecturers(referenceDateOfImport).size());

        // Map lecturer manually and set time of import not too far in the past (-> should not be selected)
        lecturer.setKindOfMapping("MANUAL");
        lecturer.setDateOfImport(DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() + 1));
        dbInstance.flush();
        assertEquals(numberOfLecturersFoundBeforeInsertingTestData, lecturerDao.getAllNotManuallyMappedOrTooOldOrphanedLecturers(referenceDateOfImport).size());

        // Map lecturer manually and set time of import too far in the past (-> should be selected)
        lecturer.setDateOfImport(DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1));
        dbInstance.flush();
        assertEquals(numberOfLecturersFoundBeforeInsertingTestData + 1, lecturerDao.getAllNotManuallyMappedOrTooOldOrphanedLecturers(referenceDateOfImport).size());

        lecturer = lecturerDao.getLecturerById(1100L);
        assertNull(lecturer.getKindOfMapping());
        assertEquals(2, lecturer.getLecturerCourses().size());

        // Remove all courses of lecturer, i.e. make it orphaned
        List<LecturerIdCourseId> lecturerIdCourseIds = new LinkedList<>();
        lecturerIdCourseIds.add(new LecturerIdCourseId(1100L, 100L));
        lecturerIdCourseIds.add(new LecturerIdCourseId(1100L, 200L));

        lecturerCourseDao.deleteByLecturerIdCourseIdsAsBulkDelete(lecturerIdCourseIds);
        dbInstance.flush();

        List<Long> lecturerIdsFound = lecturerDao.getAllNotManuallyMappedOrTooOldOrphanedLecturers(referenceDateOfImport);
        assertEquals(numberOfLecturersFoundBeforeInsertingTestData + 2, lecturerIdsFound.size());
        assertTrue(lecturerIdsFound.contains(1100L));
    }

    @Test
    public void testGetLecturersByMappedIdentityKey() throws CampusCourseException {
        insertTestData();

        Lecturer lecturer1 = lecturerDao.getLecturerById(1100L);
        Lecturer lecturer2 = lecturerDao.getLecturerById(1200L);
        assertNotNull(lecturer1);
        assertNotNull(lecturer2);

        Identity identity = insertTestUser("lecturerDaoTestUser");

        assertTrue(lecturerDao.getLecturersByMappedIdentityKey(identity.getKey()).isEmpty());

        // Add mapping forlecturer 1
        lecturerDao.addMapping(lecturer1.getPersonalNr(), identity);
        dbInstance.flush();

        List<Lecturer> lecturersFound = lecturerDao.getLecturersByMappedIdentityKey(identity.getKey());
        assertEquals(1, lecturersFound.size());
        assertTrue(lecturersFound.contains(lecturer1));

        // Also add mapping for lecturer 2
        lecturerDao.addMapping(lecturer2.getPersonalNr(), identity);
        dbInstance.flush();

        lecturersFound = lecturerDao.getLecturersByMappedIdentityKey(identity.getKey());
        assertEquals(2, lecturersFound.size());
        assertTrue(lecturersFound.contains(lecturer1));
        assertTrue(lecturersFound.contains(lecturer2));

        // Remove mapping for lecturer 1
        lecturerDao.removeMapping(lecturer1.getPersonalNr());
        dbInstance.flush();

        lecturersFound = lecturerDao.getLecturersByMappedIdentityKey(identity.getKey());
        assertEquals(1, lecturersFound.size());
        assertTrue(lecturersFound.contains(lecturer2));

        // Remove also mapping for lecturer 2
        lecturerDao.removeMapping(lecturer2.getPersonalNr());
        dbInstance.flush();

        assertTrue(lecturerDao.getLecturersByMappedIdentityKey(identity.getKey()).isEmpty());
    }

    @Test
    public void testDelete() throws CampusCourseException {
        insertTestData();
        assertNotNull(lecturerDao.getLecturerById(1100L));
        Course course = courseDao.getCourseById(200L);
        assertNotNull(course);
        assertEquals(2, course.getLecturerCourses().size());
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));

        lecturerDao.delete(lecturers.get(0));

        // Check before flush
        assertEquals(1, course.getLecturerCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(lecturerDao.getLecturerById(1100L));
        assertEquals(1, course.getLecturerCourses().size());
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
    }

    @Test
    public void testDeleteByLecturerIds() throws CampusCourseException {
        insertTestData();
        assertNotNull(lecturerDao.getLecturerById(1100L));
        assertNotNull(lecturerDao.getLecturerById(1200L));
        Course course = courseDao.getCourseById(200L);
        assertNotNull(course);
        assertEquals(2, course.getLecturerCourses().size());
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1200L, 200L));

        List<Long> lecturerIds = new LinkedList<>();
        lecturerIds.add(1100L);
        lecturerIds.add(1200L);

        lecturerDao.deleteByLecturerIds(lecturerIds);

        // Check before flush
        assertEquals(0, course.getLecturerCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(lecturerDao.getLecturerById(1100L));
        assertNull(lecturerDao.getLecturerById(1200L));
        assertEquals(0, course.getLecturerCourses().size());
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
        assertNull(lecturerCourseDao.getLecturerCourseById(1200L, 200L));
    }

    @Test
    public void testDeleteByLecturerIdsAsBulkDelete() throws CampusCourseException {
        insertTestData();
        assertNotNull(lecturerDao.getLecturerById(1100L));
        assertNotNull(lecturerDao.getLecturerById(1200L));
        Course course = courseDao.getCourseById(200L);
        assertNotNull(course);
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1200L, 200L));

        List<Long> lecturerIds = new LinkedList<>();
        lecturerIds.add(2100L);
        lecturerIds.add(2200L);

        lecturerCourseDao.deleteByLecturerIdsAsBulkDelete(lecturerIds);
        lecturerDao.deleteByLecturerIdsAsBulkDelete(lecturerIds);

        dbInstance.flush();
        dbInstance.clear();

        assertNull(lecturerDao.getLecturerById(2100L));
        assertNull(lecturerDao.getLecturerById(2200L));
        assertNull(lecturerCourseDao.getLecturerCourseById(2100L, 200L));
        assertNull(lecturerCourseDao.getLecturerCourseById(2200L, 200L));
    }

    @Test
    public void testGetAllLecturersWithCreatedOrNotCreatedCreatableCourses() throws CampusCourseException {
        int numberOfLecturersFoundBeforeInsertingTestData = lecturerDao.getAllLecturersWithCreatedOrNotCreatedCreatableCourses().size();
        insertTestData();
        List<Lecturer> lecturersFound = lecturerDao.getAllLecturersWithCreatedOrNotCreatedCreatableCourses();
        assertEquals(numberOfLecturersFoundBeforeInsertingTestData + 6, lecturersFound.size());
        List<Long> idsFound = lecturersFound.stream().map(Lecturer::getPersonalNr).collect(Collectors.toList());
        assertTrue(idsFound.contains(1100L));
        assertTrue(idsFound.contains(1200L));
        assertTrue(idsFound.contains(1300L));
        assertTrue(idsFound.contains(1400L));
        assertTrue(idsFound.contains(1500L));
        assertTrue(idsFound.contains(1100L));
    }

    private void insertTestData() throws CampusCourseException {
        // Insert some orgs
        List<Org> orgs = mockDataGeneratorProvider.get().getOrgs();
        orgDao.save(orgs);
        dbInstance.flush();

        // Insert some lecturers
        lecturers = mockDataGeneratorProvider.get().getLecturers();
        lecturerDao.save(lecturers);
        dbInstance.flush();

        // Insert some courses
        List<CourseSemesterOrgId> courseSemesterOrgIds = mockDataGeneratorProvider.get().getCourseSemesterOrgIds();
        courseDao.save(courseSemesterOrgIds);
        dbInstance.flush();

        // Insert some lecturerIdCourseIds
        List<LecturerIdCourseIdDateOfImport> lecturerIdCourseIdDateOfImports = mockDataGeneratorProvider.get().getLecturerIdCourseIdDateOfImports();
        lecturerCourseDao.save(lecturerIdCourseIdDateOfImports);
        dbInstance.flush();

        // Set current semester
        Course course = courseDao.getCourseById(100L);
        semesterDao.setCurrentSemester(course.getSemester().getId());
        dbInstance.flush();

        addOlatResourceToCourses_100_200_400_500_600();
    }

    private Identity insertTestUser(String userName) {
        User user = userManager.createUser("lecturerDaoFirstName" + userName, "lecturerDaoLastName" + userName, userName + "@uzh.ch");
        dbInstance.saveObject(user);
        Identity identity = new IdentityImpl(userName, user);
        dbInstance.saveObject(identity);
        dbInstance.flush();
        return identity;
    }

    private void addOlatResourceToCourses_100_200_400_500_600() {
        Course course1 = courseDao.getCourseById(100L);
        Course course2 = courseDao.getCourseById(200L);
        Course course4 = courseDao.getCourseById(400L);
        Course course5 = courseDao.getCourseById(500L);
        Course course6 = courseDao.getCourseById(600L);
        OLATResource olatResource1 = insertOlatResource("resourceLecturerDaoTestData1");
        OLATResource olatResource2 = insertOlatResource("resourceLecturerDaoTestData2");
        OLATResource olatResource4 = insertOlatResource("resourceLecturerDaoTestData4");
        OLATResource olatResource5 = insertOlatResource("resourceLecturerDaoTestData5");
        OLATResource olatResource6 = insertOlatResource("resourceLecturerDaoTestData6");
        course1.setOlatResource(olatResource1);
        course2.setOlatResource(olatResource2);
        course4.setOlatResource(olatResource4);
        course5.setOlatResource(olatResource5);
        course6.setOlatResource(olatResource6);
        dbInstance.flush();
    }

    private OLATResource insertOlatResource(String olatResourceName) {
        olatResourceName = "lecturerDaoTest_" + olatResourceName;
        TestResourceable resourceable = new TestResourceable(8213649L, olatResourceName);
        OLATResource olatResource = olatResourceManager.createOLATResourceInstance(resourceable);
        olatResourceManager.saveOLATResource(olatResource);
        return olatResource;
    }

    private static class TestResourceable implements OLATResourceable {
        private final Long resId;
        private final String resName;

        TestResourceable(Long resId, String resourceName) {
            this.resId = resId;
            this.resName = resourceName;
        }

        @Override
        public Long getResourceableId() {
            return resId;
        }

        @Override
        public String getResourceableTypeName() {
            return resName;
        }
    }
}