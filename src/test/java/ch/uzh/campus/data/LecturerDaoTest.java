package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;
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

@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class LecturerDaoTest extends OlatTestCase {

    @Autowired
    private DB dbInstance;

    @Autowired
    private LecturerDao lecturerDao;

    @Autowired
    private OrgDao orgDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private LecturerCourseDao lecturerCourseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    private List<Lecturer> lecturers;

    @After
    public void after() {
       dbInstance.rollback();
    }

    @Test
    public void testNullGetLecturerById() {
        insertTestData();
        assertNull(lecturerDao.getLecturerById(1999L));
    }

    @Test
    public void testNotNullGetLecturerById() {
        insertTestData();
        assertNotNull(lecturerDao.getLecturerById(1100L));
    }

    @Test
    public void testNullGetLecturerByEmail() {
        insertTestData();
        assertNull(lecturerDao.getLecturerByEmail("wrongEmail"));
    }

    @Test
    public void testNotNullGetLecturerByEmail() {
        insertTestData();
        assertNotNull(lecturerDao.getLecturerByEmail("email1"));
    }

    @Test
    public void testGetAllOrphanedLecturers() throws InterruptedException {
        int numberOfLecturersFoundBeforeInsertingTestData = lecturerDao.getAllOrphanedLecturers().size();
        insertTestData();

        // Lecturer 1700 has no courses. i.e. it is orphaned
        Lecturer lecturer = lecturerDao.getLecturerById(1700L);
        assertEquals(0, lecturer.getLecturerCourses().size());

        assertEquals(numberOfLecturersFoundBeforeInsertingTestData + 1, lecturerDao.getAllOrphanedLecturers().size());

        lecturer = lecturerDao.getLecturerById(1100L);
        assertEquals(2, lecturer.getLecturerCourses().size());

        // Remove all courses of lecturers, i.e. make it orphaned
        List<LecturerIdCourseId> lecturerIdCourseIds = new LinkedList<>();
        lecturerIdCourseIds.add(new LecturerIdCourseId(1100L, 100L));
        lecturerIdCourseIds.add(new LecturerIdCourseId(1100L, 200L));

        lecturerCourseDao.deleteByLecturerIdCourseIdsAsBulkDelete(lecturerIdCourseIds);
        dbInstance.flush();

        List<Long> lecturerIdsFound = lecturerDao.getAllOrphanedLecturers();
        assertEquals(numberOfLecturersFoundBeforeInsertingTestData + 2, lecturerIdsFound.size());
        assertTrue(lecturerIdsFound.contains(1100L));
    }

    @Test
    public void testDelete() {
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
    public void testDeleteByLecturerIds() {
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
    public void testDeleteByLecturerIdsAsBulkDelete() {
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
    public void testGetAllLecturersWithCreatedOrNotCreatedCreatableCourses() {
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

    private void insertTestData() {
        // Insert some orgs
        List<Org> orgs = mockDataGeneratorProvider.get().getOrgs();
        orgDao.save(orgs);
        dbInstance.flush();

        // Insert some lecturers
        lecturers = mockDataGeneratorProvider.get().getLecturers();
        lecturerDao.save(lecturers);
        dbInstance.flush();

        // Insert some courses
        List<CourseOrgId> courseOrgIds = mockDataGeneratorProvider.get().getCourseOrgIds();
        courseDao.save(courseOrgIds);
        dbInstance.flush();

        // Insert some lecturerIdCourseIds
        List<LecturerIdCourseIdModifiedDate> lecturerIdCourseIdModifiedDates = mockDataGeneratorProvider.get().getLecturerIdCourseIdModifiedDates();
        lecturerCourseDao.save(lecturerIdCourseIdModifiedDates);
        dbInstance.flush();
    }
}