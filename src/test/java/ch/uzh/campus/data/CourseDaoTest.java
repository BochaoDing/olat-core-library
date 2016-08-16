package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Initial Date: Oct 28, 2014<br />
 *
 * @author aabouc
 * @author Martin Schraner
 * @author lavinia
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class CourseDaoTest extends OlatTestCase {

    @Autowired
    private DB dbInstance;

    @Autowired
    private OrgDao orgDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private LecturerDao lecturerDao;

    @Autowired
    private LecturerCourseDao lecturerCourseDao;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private StudentCourseDao studentCourseDao;

    @Autowired
    private TextDao textDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    @After
    public void after() {
        dbInstance.rollback();
    }

    @Test
    public void testSaveCourseOrgId() {
        // Insert some orgs
        List<Org> orgs = mockDataGeneratorProvider.get().getOrgs();
        orgDao.save(orgs);
        dbInstance.flush();

        assertNull(courseDao.getCourseById(100L));
        assertNull(courseDao.getCourseById(300L));

        // Insert some courseOrgIds
        List<CourseOrgId> courseOrgIds = mockDataGeneratorProvider.get().getCourseOrgIds();
        courseDao.save(courseOrgIds);

        // Check before calling flush
        assertSave();

        dbInstance.flush();
        dbInstance.clear();

        assertSave();
    }

    private void assertSave() {
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(1, course.getOrgs().size());

        course = courseDao.getCourseById(300L);
        assertNotNull(course);
        assertEquals(9, course.getOrgs().size());

        Set<Long> orgIds = course.getOrgs().stream().map(Org::getId).collect(Collectors.toSet());
        assertTrue(orgIds.contains(9100L));
        assertTrue(orgIds.contains(9200L));
        assertTrue(orgIds.contains(9300L));
        assertTrue(orgIds.contains(9400L));
        assertTrue(orgIds.contains(9500L));
        assertTrue(orgIds.contains(9600L));
        assertTrue(orgIds.contains(9700L));
        assertTrue(orgIds.contains(9800L));
        assertTrue(orgIds.contains(9900L));

        Org org = orgDao.getOrgById(9100L);
        assertNotNull(org);
        assertEquals(2, org.getCourses().size());

        Set<Long> courseIds = org.getCourses().stream().map(Course::getId).collect(Collectors.toSet());
        assertTrue(courseIds.contains(100L));
        assertTrue(courseIds.contains(300L));
    }

    @Test
    public void testSaveOrUpdateCourseOrgId() {
        insertTestData();

        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(1, course.getOrgs().size());

        // Modify orgs of course with id 100L
        CourseOrgId courseOrgIdUpdated = mockDataGeneratorProvider.get().getCourseOrgIds().get(0);
        assertEquals(100L, courseOrgIdUpdated.getId().longValue());
        courseOrgIdUpdated.setOrg1(null);
        courseOrgIdUpdated.setOrg2(9200L);
        courseOrgIdUpdated.setOrg3(9300L);
        courseOrgIdUpdated.setOrg4(9400L);
        courseOrgIdUpdated.setOrg5(9500L);
        courseOrgIdUpdated.setOrg6(9600L);
        courseOrgIdUpdated.setOrg7(9700L);
        courseOrgIdUpdated.setOrg8(9800L);
        courseOrgIdUpdated.setOrg9(9900L);

        courseDao.saveOrUpdate(courseOrgIdUpdated);

        dbInstance.flush();
        dbInstance.clear();

        courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(8, course.getOrgs().size());

        Set<Long> orgIds = course.getOrgs().stream().map(Org::getId).collect(Collectors.toSet());
        assertTrue(orgIds.contains(9200L));
        assertTrue(orgIds.contains(9300L));
        assertTrue(orgIds.contains(9400L));
        assertTrue(orgIds.contains(9500L));
        assertTrue(orgIds.contains(9600L));
        assertTrue(orgIds.contains(9700L));
        assertTrue(orgIds.contains(9800L));
        assertTrue(orgIds.contains(9900L));
    }

    @Test
    public void testNullGetCourseById() {
        insertTestData();
        assertNull(courseDao.getCourseById(999L));
    }

    @Test
    public void testNotNullGetCourseById() {
        insertTestData();
        assertNotNull(courseDao.getCourseById(100L));
    }

    @Test
    public void testGetCreatedCoursesOfCurrentSemesterByLecturerId() {
        insertTestData();
        List<Course> courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1200L, null);

        assertEquals(1, courses.size());
        assertEquals(200L, courses.get(0).getId().longValue());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1400L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1500L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1600L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesOfCurrentSemesterByLecturerId_TwoSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300 and set a reourceableId for course 400 and set the same reourceableId for courses 300 and 400
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveResourceableId(300L, 1L);
        courseDao.saveResourceableId(400L, 1L);

        // Check that the students of both semesters can be found
        List<Course> courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1300L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1400L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1500L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1600L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesOfCurrentSemesterByLecturerId_ThreeSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and set the same reourceableId for courses 300, 400 and 500
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveResourceableId(300L, 1L);
        courseDao.saveResourceableId(400L, 1L);
        courseDao.saveResourceableId(500L, 1L);

        // Check that the students of all 3 semesters can be found
        List<Course> courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1300L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1400L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1500L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1600L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesOfCurrentSemesterByLecturerId_FourSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and 600 the parent of 500 and set for courses 300, 400 and 500 the recourceableId of course 600
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveParentCourseId(500L, 600L);
        Course course = courseDao.getCourseById(600L);
        courseDao.saveResourceableId(300L, course.getResourceableId());
        courseDao.saveResourceableId(400L, course.getResourceableId());
        courseDao.saveResourceableId(500L, course.getResourceableId());

        // Check that the students of all 4 semesters can be found
        List<Course> courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1300L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1400L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1500L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1600L, null);
        assertEquals(1, courses.size());
    }

    @Test
    public void testGetNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId() {
        insertTestData();
        List<Course> courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1300L, null);

        assertNotNull(courses);
        assertEquals(1, courses.size());

        // Course already created
        courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1200L, null);
        assertTrue(courses.isEmpty());

        // Course with disabled org
        courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1800L, null);
        assertTrue(courses.isEmpty());

        // Excluded ourse
        courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1900L, null);
        assertTrue(courses.isEmpty());
    }

    @Test
    public void testLatestCourseByResourceable() throws Exception {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and 600 the parent of 500 and set for courses 300, 400 and 500 the recourceableId of course 600
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveParentCourseId(500L, 600L);
        Course course = courseDao.getCourseById(600L);
        courseDao.saveResourceableId(300L, course.getResourceableId());
        courseDao.saveResourceableId(400L, course.getResourceableId());
        courseDao.saveResourceableId(500L, course.getResourceableId());

        course = courseDao.getLatestCourseByResourceable(course.getResourceableId());
        assertNotNull(course);
        assertEquals(300L, course.getId().longValue());
    }

    @Test
    public void testDelete() {
        insertTestData();
        assertNotNull(courseDao.getCourseById(100L));
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(2, lecturer.getLecturerCourses().size());
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        Student student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertEquals(3, student.getStudentCourses().size());
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        Org org = orgDao.getOrgById(9100L);
        assertNotNull(org);
        assertEquals(2, org.getCourses().size());

        Course courseToBeDeleted = courseDao.getCourseById(100L);
        courseDao.delete(courseToBeDeleted);

        // Check before flush
        assertEquals(1, lecturer.getLecturerCourses().size());
        assertEquals(2, student.getStudentCourses().size());
        assertEquals(1, org.getCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(courseDao.getCourseById(100L));

        lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(1, lecturer.getLecturerCourses().size());
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertEquals(2, student.getStudentCourses().size());
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        org = orgDao.getOrgById(9100L);
        assertNotNull(org);
        assertEquals(1, org.getCourses().size());
    }

    @Test
    public void testDeleteByCourseId() {
        insertTestData();
        assertNotNull(courseDao.getCourseById(100L));
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(2, lecturer.getLecturerCourses().size());
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        Student student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertEquals(3, student.getStudentCourses().size());
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        Org org = orgDao.getOrgById(9100L);
        assertNotNull(org);
        assertEquals(2, org.getCourses().size());
        assertFalse(textDao.getTextsByCourseId(100L).isEmpty());
        assertFalse(eventDao.getEventsByCourseId(100L).isEmpty());

        courseDao.deleteByCourseId(100L);

        // Check before flush
        assertEquals(1, lecturer.getLecturerCourses().size());
        assertEquals(2, student.getStudentCourses().size());
        assertEquals(1, org.getCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(courseDao.getCourseById(100L));

        lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(1, lecturer.getLecturerCourses().size());
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertEquals(2, student.getStudentCourses().size());
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        org = orgDao.getOrgById(9100L);
        assertNotNull(org);
        assertEquals(1, org.getCourses().size());
        assertTrue(textDao.getTextsByCourseId(100L).isEmpty());
        assertTrue(eventDao.getEventsByCourseId(100L).isEmpty());
    }

    @Test
    public void testDeleteByCourseIds() {
        insertTestData();
        assertEquals(2, courseDao.getAllCreatedCoursesOfCurrentSemester().size());

        List<Long> courseIds = new LinkedList<>();
        courseIds.add(100L);
        courseIds.add(200L);

        courseDao.deleteByCourseIds(courseIds);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, courseDao.getAllCreatedCoursesOfCurrentSemester().size());
    }

    @Test
    public void testSaveResourceableId() {
        insertTestData();
        Course course = courseDao.getCourseById(100L);
        assertEquals(101L, course.getResourceableId().longValue());

        courseDao.saveResourceableId(100L, 1000L);

        assertEquals(1000L, course.getResourceableId().longValue());

        dbInstance.flush();
        dbInstance.clear();

        Course updatedCourse = courseDao.getCourseById(100L);
        assertEquals(1000L, updatedCourse.getResourceableId().longValue());
    }

    @Test
    public void testDisableSynchronization() {
        insertTestData();
        Course course = courseDao.getCourseById(100L);
        assertTrue(course.isSynchronizable());

        courseDao.disableSynchronization(100L);

        assertFalse(course.isSynchronizable());

        dbInstance.flush();
        dbInstance.clear();

        Course updatedCourse = courseDao.getCourseById(100L);
        assertFalse(updatedCourse.isSynchronizable());
    }

    @Test
    public void testDeleteResourceableIdAndParentCourse() {
        insertTestData();
        Course courseWithoutParentCourse = courseDao.getCourseById(100L);
        Course courseWithParentCourse = courseDao.getCourseById(200L);
        // Make course 400 to be the parent course of 200
        courseDao.saveParentCourseId(200L, 400L);
        assertEquals(101L, courseWithoutParentCourse.getResourceableId().longValue());
        assertNull(courseWithoutParentCourse.getParentCourse());
        assertEquals(201L, courseWithParentCourse.getResourceableId().longValue());
        assertNotNull(courseWithParentCourse.getParentCourse());

        courseDao.resetResourceableIdAndParentCourse(101L);
        courseDao.resetResourceableIdAndParentCourse(201L);

        assertNull(courseWithoutParentCourse.getResourceableId());
        assertNull(courseWithoutParentCourse.getParentCourse());
        assertNull(courseWithParentCourse.getResourceableId());
        assertNull(courseWithParentCourse.getParentCourse());

        dbInstance.flush();
        dbInstance.clear();

        Course updatedCourseWithoutParentCourse = courseDao.getCourseById(100L);
        assertNull(updatedCourseWithoutParentCourse.getResourceableId());
        assertNull(updatedCourseWithoutParentCourse.getParentCourse());
        Course updatedCourseWithParentCourse = courseDao.getCourseById(200L);
        assertNull(updatedCourseWithParentCourse.getResourceableId());
        assertNull(updatedCourseWithParentCourse.getParentCourse());
    }

    @Test
    public void testSaveParentCourseId() {
        insertTestData();
        Course parentCourse = courseDao.getCourseById(100L);
        Course course = courseDao.getCourseById(200L);
        assertNotNull(parentCourse);
        assertNotNull(course);
        assertNull(course.getParentCourse());
        assertNull(parentCourse.getChildCourse());

        courseDao.saveParentCourseId(200L, 100L);

        assertParentCourse(course, parentCourse);

        dbInstance.flush();
        dbInstance.clear();

        Course updatedParentCourse = courseDao.getCourseById(100L);
        Course updatedCourse = courseDao.getCourseById(200L);
        assertParentCourse(updatedCourse, updatedParentCourse);
    }

    private void assertParentCourse(Course course, Course parentCourse) {
        assertNotNull(course.getParentCourse());
        assertEquals(100L, course.getParentCourse().getId().longValue());
        assertNotNull(parentCourse.getChildCourse());
        assertEquals(200L, parentCourse.getChildCourse().getId().longValue());
    }

    @Test
    public void testGetResourceableIdsOfAllCreatedCoursesOfSpecificSemester() {
        insertTestData();
        List<Long> resourceableIdsOfAllCreatedCourses = courseDao.getResourceableIdsOfAllCreatedCoursesOfSpecificSemester("99HS");
        assertEquals(2, resourceableIdsOfAllCreatedCourses.size());
        assertTrue(resourceableIdsOfAllCreatedCourses.contains(101L));
        assertTrue(resourceableIdsOfAllCreatedCourses.contains(201L));
    }

    @Test
    public void testGetResourceableIdsOfAllCreatedCoursesOfPreviousSemester() {
        insertTestData();
        List<Long> resourceableIdsOfAllCreatedCourses = courseDao.getResourceableIdsOfAllCreatedCoursesOfPreviousSemester();
        assertEquals(1, resourceableIdsOfAllCreatedCourses.size());
        assertTrue(resourceableIdsOfAllCreatedCourses.contains(401L));
    }

    @Test
    public void testGetIdsOfAllCreatedSynchronizableCoursesOfCurrentSemester() {
        insertTestData();
        assertEquals(2, courseDao.getIdsOfAllCreatedSynchronizableCoursesOfCurrentSemester().size());

        courseDao.resetResourceableIdAndParentCourse(101L);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(1, courseDao.getIdsOfAllCreatedSynchronizableCoursesOfCurrentSemester().size());
    }

    @Test
    public void testGetIdsOfAllNotCreatedCreatableCoursesOfCurrentSemester() {
        insertTestData();
        List<Long> idsFound = courseDao.getIdsOfAllNotCreatedCreatableCoursesOfCurrentSemester();
        assertEquals(1, idsFound.size());
        assertTrue(idsFound.contains(300L));

        // Reset resourceable, i.e. undo create course
        courseDao.resetResourceableIdAndParentCourse(101L);

        dbInstance.flush();
        dbInstance.clear();

        idsFound = courseDao.getIdsOfAllNotCreatedCreatableCoursesOfCurrentSemester();
        assertEquals(2, idsFound.size());
        assertTrue(idsFound.contains(100L));
        assertTrue(idsFound.contains(300L));
    }

    @Test
    public void testGetAllCreatedCoursesOfCurrentSemester() {
        insertTestData();
        assertEquals(2, courseDao.getAllCreatedCoursesOfCurrentSemester().size());

        courseDao.deleteByCourseId(100L);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(1, courseDao.getAllCreatedCoursesOfCurrentSemester().size());
    }

    @Test
    public void tesGetAllNotCreatedOrphanedCourses() {
        int numberOfCoursesFoundBeforeInsertingTestData = courseDao.getAllNotCreatedOrphanedCourses().size();
        insertTestData();

        assertEquals(numberOfCoursesFoundBeforeInsertingTestData, courseDao.getAllNotCreatedOrphanedCourses().size());

        // Remove lecturerCourse and studentCourse entries of course 100 (not created course) and 300 (created course)-> courses 100 and 300 are orphaned
        List<Long> courseIds = new ArrayList<>();
        courseIds.add(100L);
        courseIds.add(300L);
        lecturerCourseDao.deleteByCourseIdsAsBulkDelete(courseIds);
        studentCourseDao.deleteByCourseIdsAsBulkDelete(courseIds);
        dbInstance.flush();

        List<Long> courseIdsFound = courseDao.getAllNotCreatedOrphanedCourses();

        assertEquals(numberOfCoursesFoundBeforeInsertingTestData + 1, courseIdsFound.size());
        assertFalse(courseIdsFound.contains(100L));   // created course
        assertTrue(courseIdsFound.contains(300L));    // not created course
    }

    @Test
    public void testExistResourceableId() {
        insertTestData();
        assertTrue(courseDao.existResourceableId(101L));

        courseDao.resetResourceableIdAndParentCourse(100L);

        dbInstance.flush();
        dbInstance.clear();

        assertFalse(courseDao.existResourceableId(100L));
    }

    @Test
    public void testGetCreatedCoursesOfCurrentSemesterByStudentId() {
        insertTestData();

    	List<Course> courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2100L, null);
        assertNotNull(courses);
    	assertEquals(2, courses.size());

        // Student has no courses
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2400L, null);
        assertEquals(0, courses.size());

        // Old courses
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2400L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2500L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2600L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesOfCurrentSemesterByStudentId_TwoSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300 and set set the same reourceableId for courses 300 and 400
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveResourceableId(300L, 1L);
        courseDao.saveResourceableId(400L, 1L);

        // Check that the students of both semesters can be found
        List<Course> courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2100L, null);
        assertEquals(3, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2400L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2500L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2600L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesOfCurrentSemesterByStudentId_ThreeSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and set the same reourceableId for courses 300, 400 and 500
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveResourceableId(300L, 1L);
        courseDao.saveResourceableId(400L, 1L);
        courseDao.saveResourceableId(500L, 1L);

        // Check that the students of all 3 semesters can be found
        List<Course> courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2100L, null);
        assertEquals(3, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2400L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2500L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2600L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesOfCurrentSemesterByStudentId_FourSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and 600 the parent of 500 and set for courses 300, 400 and 500 the recourceableId of course 600
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveParentCourseId(500L, 600L);
        Course course = courseDao.getCourseById(600L);
        courseDao.saveResourceableId(300L, course.getResourceableId());
        courseDao.saveResourceableId(400L, course.getResourceableId());
        courseDao.saveResourceableId(500L, course.getResourceableId());

        // Check that the students of all 4 semesters can be found
        List<Course> courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2100L, null);
        assertEquals(3, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2400L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2500L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2600L, null);
        assertEquals(1, courses.size());
    }

    @Test
    public void testGetNotCreatedCreatableCoursesOfCurrentSemesterByStudentId() {
        insertTestData();

        List<Course> courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2100L, null);
        assertEquals(1, courses.size());

        // Course with disabled org
        courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2700L, null);
        assertEquals(0, courses.size());

        // Excluded course
        courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2800L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId() {
        insertTestData();

        List<Course> courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1200L);
        assertNotNull(courses);
        assertEquals(1, courses.size());
        assertEquals(200L, courses.get(0).getId().longValue());

        // Course already created
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1400L);
        assertTrue(courses.isEmpty());

        // Courses of old semester
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1500L);
        assertTrue(courses.isEmpty());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1600L);
        assertTrue(courses.isEmpty());

        // Course with disabled org
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1800L);
        assertTrue(courses.isEmpty());

        // Excluded course
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1900L);
        assertTrue(courses.isEmpty());
    }

    @Test
    public void testGetCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId_TwoSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300 and set a reourceableId for courses 300 and 400
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveResourceableId(300L, 1L);
        courseDao.saveResourceableId(400L, 1L);

        // Check that the students of both semesters can be found
        List<Course> courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1300L);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1400L);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1500L);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1600L);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId_ThreeSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and set a reourceableId for courses 300, 400 and 500
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveResourceableId(300L, 1L);
        courseDao.saveResourceableId(400L, 1L);
        courseDao.saveResourceableId(500L, 1L);

        // Check that the students of all 3 semesters can be found
        List<Course> courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1300L);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1400L);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1500L);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1600L);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId_FourSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and 600 the parent of 500 and set for courses 300, 400 and 500 the recourceableId of course 600
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveParentCourseId(500L, 600L);
        Course course = courseDao.getCourseById(600L);
        courseDao.saveResourceableId(300L, course.getResourceableId());
        courseDao.saveResourceableId(400L, course.getResourceableId());
        courseDao.saveResourceableId(500L, course.getResourceableId());

        // Check that the students of all 4 semesters can be found
        List<Course> courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1300L);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1400L);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1500L);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1600L);
        assertEquals(1, courses.size());
    }

    @Test
    public void testGetCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId() {
        insertTestData();
        List<Course> courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2100L);

        assertNotNull(courses);
        assertEquals(3, courses.size());

        // Old courses
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2400L);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2500L);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2600L);
        assertEquals(0, courses.size());

        // Course with disabled org
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2700L);
        assertEquals(0, courses.size());

        // Excluded course
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2800L);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId_TwoSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300 and set a reourceableId for courses 300 and 400
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveResourceableId(300L, 1L);
        courseDao.saveResourceableId(400L, 1L);

        // Check that the students of both semesters can be found
        List<Course> courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2100L);
        assertEquals(3, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2400L);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2500L);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2600L);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId_ThreeSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and set a reourceableId for courses 300, 400 and 500
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveResourceableId(300L, 1L);
        courseDao.saveResourceableId(400L, 1L);
        courseDao.saveResourceableId(500L, 1L);

        // Check that the students of all 3 semesters can be found
        List<Course> courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2100L);
        assertEquals(3, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2400L);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2500L);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2600L);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId_FourSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and 600 the parent of 500 and set for courses 300, 400 and 500 the recourceableId of course 600
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveParentCourseId(500L, 600L);
        Course course = courseDao.getCourseById(600L);
        courseDao.saveResourceableId(300L, course.getResourceableId());
        courseDao.saveResourceableId(400L, course.getResourceableId());
        courseDao.saveResourceableId(500L, course.getResourceableId());

        // Check that the students of all 4 semesters can be found
        List<Course> courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2100L);
        assertEquals(3, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2400L);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2500L);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2600L);
        assertEquals(1, courses.size());
    }

    @Test
    public void testGetPreviousShortSemester() {
        insertTestData();
        assertEquals("99FS", courseDao.getPreviousShortSemester());
    }

    private void insertTestData() {
        // Insert some orgs
        List<Org> orgs = mockDataGeneratorProvider.get().getOrgs();
        orgDao.save(orgs);
        dbInstance.flush();

        // Insert some courseOrgIds
        List<CourseOrgId> courseOrgIds = mockDataGeneratorProvider.get().getCourseOrgIds();
        courseDao.save(courseOrgIds);
        dbInstance.flush();

        // Insert some lecturers
        List<Lecturer> lecturers = mockDataGeneratorProvider.get().getLecturers();
        lecturerDao.save(lecturers);
        dbInstance.flush();

        // Add lecturers to courseOrgIds
        List<LecturerIdCourseIdDateOfImport> lecturerIdCourseIdDateOfImports = mockDataGeneratorProvider.get().getLecturerIdCourseIdDateOfImports();
        lecturerCourseDao.save(lecturerIdCourseIdDateOfImports);
        dbInstance.flush();

        // Insert some students
        List<Student> students = mockDataGeneratorProvider.get().getStudents();
        studentDao.save(students);
        dbInstance.flush();

        // Add students to courseOrgIds
        List<StudentIdCourseIdDateOfImport> studentIdCourseIdDateOfImports = mockDataGeneratorProvider.get().getStudentIdCourseIdDateOfImports();
        studentCourseDao.save(studentIdCourseIdDateOfImports);
        dbInstance.flush();

        // Add some texts
        List<TextCourseId> textCourseIds = mockDataGeneratorProvider.get().getTextCourseIds();
        textDao.addTextsToCourse(textCourseIds);
        dbInstance.flush();

        // Add some events
        List<EventCourseId> eventCourseIds = mockDataGeneratorProvider.get().getEventCourseIds();
        eventDao.addEventsToCourse(eventCourseIds);
        dbInstance.flush();
    }
}
