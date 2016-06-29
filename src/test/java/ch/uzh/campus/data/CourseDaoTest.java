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

    private List<Course> courses;

    @After
    public void after() {
        dbInstance.rollback();
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
    public void testGetCreatedCoursesByLecturerId() {
        insertTestData();
        List<Course> courses = courseDao.getCreatedCoursesByLecturerId(1200L, null);

        assertEquals(1, courses.size());
        assertEquals(200L, courses.get(0).getId().longValue());
        courses = courseDao.getCreatedCoursesByLecturerId(1400L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesByLecturerId(1500L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesByLecturerId(1600L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesByLecturerId_TwoSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300 and set a reourceableId for course 400
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveResourceableId(400L, 1L);

        // Check that the students of both semesters can be found
        List<Course> courses = courseDao.getCreatedCoursesByLecturerId(1300L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesByLecturerId(1400L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesByLecturerId(1500L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesByLecturerId(1600L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesByLecturerId_ThreeSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and set a reourceableId for course 500
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveResourceableId(500L, 1L);

        // Check that the students of all 3 semesters can be found
        List<Course> courses = courseDao.getCreatedCoursesByLecturerId(1300L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesByLecturerId(1400L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesByLecturerId(1500L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesByLecturerId(1600L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesByLecturerId_FourSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and 600 the parent of 500
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveParentCourseId(500L, 600L);

        // Check that the students of all 4 semesters can be found
        List<Course> courses = courseDao.getCreatedCoursesByLecturerId(1300L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesByLecturerId(1400L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesByLecturerId(1500L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesByLecturerId(1600L, null);
        assertEquals(1, courses.size());
    }

    @Test
    public void testGetNotCreatedCoursesByLecturerId() {
        insertTestData();
        List<Course> courses = courseDao.getNotCreatedCoursesByLecturerId(1300L, null);

        assertNotNull(courses);
        assertEquals(1, courses.size());

        // Ditto for lecturer 1200
        courses = courseDao.getNotCreatedCoursesByLecturerId(1200L, null);
        assertTrue(courses.isEmpty());
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

        courseDao.delete(courses.get(0));

        // Check before flush
        assertEquals(1, lecturer.getLecturerCourses().size());
        assertEquals(2, student.getStudentCourses().size());

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
        assertFalse(textDao.getTextsByCourseId(100L).isEmpty());
        assertFalse(eventDao.getEventsByCourseId(100L).isEmpty());

        courseDao.deleteByCourseId(100L);

        // Check before flush
        assertEquals(1, lecturer.getLecturerCourses().size());
        assertEquals(2, student.getStudentCourses().size());

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
        assertTrue(textDao.getTextsByCourseId(100L).isEmpty());
        assertTrue(eventDao.getEventsByCourseId(100L).isEmpty());
    }

    @Test
    public void testDeleteByCourseIds() {
        insertTestData();
        assertEquals(2, courseDao.getAllCreatedCourses().size());

        List<Long> courseIds = new LinkedList<>();
        courseIds.add(100L);
        courseIds.add(200L);

        courseDao.deleteByCourseIds(courseIds);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, courseDao.getAllCreatedCourses().size());
    }

    @Test
    public void testDeleteByCourseIdsAsBulkDelete() {
        insertTestData();
        assertEquals(2, courseDao.getAllCreatedCourses().size());
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertFalse(textDao.getTextsByCourseId(100L).isEmpty());
        assertFalse(eventDao.getEventsByCourseId(100L).isEmpty());

        List<Long> courseIds = new LinkedList<>();
        courseIds.add(100L);
        courseIds.add(200L);

        studentCourseDao.deleteByCourseIdsAsBulkDelete(courseIds);
        lecturerCourseDao.deleteByCourseIdsAsBulkDelete(courseIds);
        textDao.deleteTextsByCourseIdsAsBulkDelete(courseIds);
        eventDao.deleteEventsByCourseIdsAsBulkDelete(courseIds);
        courseDao.deleteByCourseIdsAsBulkDelete(courseIds);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, courseDao.getAllCreatedCourses().size());
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertTrue(textDao.getTextsByCourseId(100L).isEmpty());
        assertTrue(eventDao.getEventsByCourseId(100L).isEmpty());
    }

    @Test
    public void testSaveResourceableId() {
        insertTestData();
        Course course = courseDao.getCourseById(100L);
        assertEquals(100L, course.getResourceableId().longValue());

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
    public void testDeleteResourceableId() {
        insertTestData();
        Course course = courseDao.getCourseById(100L);
        assertEquals(100L, course.getResourceableId().longValue());

        courseDao.resetResourceable(100L);

        assertNull(course.getResourceableId());

        dbInstance.flush();
        dbInstance.clear();

        Course updatedCourse = courseDao.getCourseById(100L);
        assertNull(updatedCourse.getResourceableId());
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
    public void testGetResourceableIdsOfAllCreatedCourses() {
        insertTestData();
        List<Long> resourceableIdsOfAllCreatedCourses = courseDao.getResourceableIdsOfAllCreatedCourses();
        assertTrue(resourceableIdsOfAllCreatedCourses.contains(100L));
        assertFalse(resourceableIdsOfAllCreatedCourses.contains(999L));
    }

    @Test
    public void testGetIdsOfAllCreatedCourses() {
        insertTestData();
        assertEquals(2, courseDao.getIdsOfAllCreatedCourses().size());

        courseDao.resetResourceable(100L);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(1, courseDao.getIdsOfAllCreatedCourses().size());
    }

    @Test
    public void testGetIdsOfAllNotCreatedCourses() {
        insertTestData();
        assertEquals(1, courseDao.getIdsOfAllNotCreatedCourses().size());

        courseDao.resetResourceable(100L);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(2, courseDao.getIdsOfAllNotCreatedCourses().size());
    }

    @Test
    public void testGetAllCreatedCourses() {
        insertTestData();
        assertEquals(2, courseDao.getAllCreatedCourses().size());

        courseDao.deleteByCourseId(100L);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(1, courseDao.getAllCreatedCourses().size());
    }

    @Test
    public void tesGetAllNotUpdatedCourses() {
        Date now = new Date();
        int numberOfCoursesFoundBeforeInsertingTestData = courseDao.getAllNotUpdatedCourses(now).size();
        insertTestData();
    	List<Long> courseIds = courseDao.getAllNotUpdatedCourses(now);
        // Only courses with resourceableId = null
    	assertEquals(numberOfCoursesFoundBeforeInsertingTestData + 3, courseIds.size());

        assertNull(courses.get(2).getResourceableId());
        courses.get(2).setModifiedDate(now);

        dbInstance.flush();
        dbInstance.clear();

        courseIds = courseDao.getAllNotUpdatedCourses(now);
        assertEquals(numberOfCoursesFoundBeforeInsertingTestData + 2, courseIds.size());
    }

    @Test
    public void testExistResourceableId() {
        insertTestData();
        assertTrue(courseDao.existResourceableId(100L));

        courseDao.resetResourceable(100L);

        dbInstance.flush();
        dbInstance.clear();

        assertFalse(courseDao.existResourceableId(100L));
    }

    @Test
    public void testGetCreatedCoursesByStudentId_noneFound() {
        insertTestData();
    	List<Course> courses = courseDao.getCreatedCoursesByStudentId(2300L, null);

        assertNotNull(courses);
    	assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesByStudentId() {
        insertTestData();
    	List<Course> courses = courseDao.getCreatedCoursesByStudentId(2100L, null);

        assertNotNull(courses);
    	assertEquals(2, courses.size());
        courses = courseDao.getCreatedCoursesByStudentId(2400L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesByStudentId(2500L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesByStudentId(2600L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesByStudentId_TwoSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300 and set a reourceableId for course 400
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveResourceableId(400L, 1L);

        // Check that the students of both semesters can be found
        List<Course> courses = courseDao.getCreatedCoursesByStudentId(2100L, null);
        assertEquals(3, courses.size());
        courses = courseDao.getCreatedCoursesByStudentId(2400L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesByStudentId(2500L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesByStudentId(2600L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesByStudentId_ThreeSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and set a reourceableId for course 500
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveResourceableId(500L, 1L);

        // Check that the students of all 3 semesters can be found
        List<Course> courses = courseDao.getCreatedCoursesByStudentId(2100L, null);
        assertEquals(3, courses.size());
        courses = courseDao.getCreatedCoursesByStudentId(2400L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesByStudentId(2500L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesByStudentId(2600L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesByStudentId_FourSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and 600 the parent of 500
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveParentCourseId(500L, 600L);

        // Check that the students of all 4 semesters can be found
        List<Course> courses = courseDao.getCreatedCoursesByStudentId(2100L, null);
        assertEquals(3, courses.size());
        courses = courseDao.getCreatedCoursesByStudentId(2400L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesByStudentId(2500L, null);
        assertEquals(1, courses.size());
        courses = courseDao.getCreatedCoursesByStudentId(2600L, null);
        assertEquals(1, courses.size());
    }

    @Test
    public void testGetPilotCoursesByLecturerId() {
        insertTestData();
        List<Course> courses = courseDao.getPilotCoursesByLecturerId(1200L);

        assertNotNull(courses);
        assertEquals(1, courses.size());
        assertEquals(200L, courses.get(0).getId().longValue());
        courses = courseDao.getPilotCoursesByLecturerId(1400L);
        assertEquals(0, courses.size());
        courses = courseDao.getPilotCoursesByLecturerId(1500L);
        assertEquals(0, courses.size());
        courses = courseDao.getPilotCoursesByLecturerId(1600L);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetPilotCoursesByLecturerId_TwoSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300
        courseDao.saveParentCourseId(300L, 400L);

        // Check that the students of both semesters can be found
        List<Course> courses = courseDao.getPilotCoursesByLecturerId(1300L);
        assertEquals(1, courses.size());
        courses = courseDao.getPilotCoursesByLecturerId(1400L);
        assertEquals(1, courses.size());
        courses = courseDao.getPilotCoursesByLecturerId(1500L);
        assertEquals(0, courses.size());
        courses = courseDao.getPilotCoursesByLecturerId(1600L);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetPilotCoursesByLecturerId_ThreeSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);

        // Check that the students of all 3 semesters can be found
        List<Course> courses = courseDao.getPilotCoursesByLecturerId(1300L);
        assertEquals(1, courses.size());
        courses = courseDao.getPilotCoursesByLecturerId(1400L);
        assertEquals(1, courses.size());
        courses = courseDao.getPilotCoursesByLecturerId(1500L);
        assertEquals(1, courses.size());
        courses = courseDao.getPilotCoursesByLecturerId(1600L);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetPilotCoursesByLecturerId_FourSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and 600 the parent of 500
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveParentCourseId(500L, 600L);

        // Check that the students of all 4 semesters can be found
        List<Course> courses = courseDao.getPilotCoursesByLecturerId(1300L);
        assertEquals(1, courses.size());
        courses = courseDao.getPilotCoursesByLecturerId(1400L);
        assertEquals(1, courses.size());
        courses = courseDao.getPilotCoursesByLecturerId(1500L);
        assertEquals(1, courses.size());
        courses = courseDao.getPilotCoursesByLecturerId(1600L);
        assertEquals(1, courses.size());
    }

    @Test
    public void testGetPilotCoursesByStudentId() {
        insertTestData();
        List<Course> courses = courseDao.getPilotCoursesByStudentId(2100L);

        assertNotNull(courses);
        assertEquals(3, courses.size());
        courses = courseDao.getPilotCoursesByStudentId(2400L);
        assertEquals(0, courses.size());
        courses = courseDao.getPilotCoursesByStudentId(2500L);
        assertEquals(0, courses.size());
        courses = courseDao.getPilotCoursesByStudentId(2600L);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetPilotCoursesByStudentId_TwoSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300
        courseDao.saveParentCourseId(300L, 400L);

        // Check that the students of both semesters can be found
        List<Course> courses = courseDao.getPilotCoursesByStudentId(2100L);
        assertEquals(3, courses.size());
        courses = courseDao.getPilotCoursesByStudentId(2400L);
        assertEquals(1, courses.size());
        courses = courseDao.getPilotCoursesByStudentId(2500L);
        assertEquals(0, courses.size());
        courses = courseDao.getPilotCoursesByStudentId(2600L);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetPilotCoursesByStudentId_ThreeSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);

        // Check that the students of all 3 semesters can be found
        List<Course> courses = courseDao.getPilotCoursesByStudentId(2100L);
        assertEquals(3, courses.size());
        courses = courseDao.getPilotCoursesByStudentId(2400L);
        assertEquals(1, courses.size());
        courses = courseDao.getPilotCoursesByStudentId(2500L);
        assertEquals(1, courses.size());
        courses = courseDao.getPilotCoursesByStudentId(2600L);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetPilotCoursesByStudentId_FourSemestersCampusCourse() {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and 600 the parent of 500
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveParentCourseId(500L, 600L);

        // Check that the students of all 4 semesters can be found
        List<Course> courses = courseDao.getPilotCoursesByStudentId(2100L);
        assertEquals(3, courses.size());
        courses = courseDao.getPilotCoursesByStudentId(2400L);
        assertEquals(1, courses.size());
        courses = courseDao.getPilotCoursesByStudentId(2500L);
        assertEquals(1, courses.size());
        courses = courseDao.getPilotCoursesByStudentId(2600L);
        assertEquals(1, courses.size());
    }

    private void insertTestData() {
        // Insert some courses
        courses = mockDataGeneratorProvider.get().getCourses();
        courseDao.save(courses);
        dbInstance.flush();

        // Insert some lecturers
        List<Lecturer> lecturers = mockDataGeneratorProvider.get().getLecturers();
        lecturerDao.save(lecturers);
        dbInstance.flush();

        // Add lecturers to courses
        List<LecturerIdCourseId> lecturerIdCourseIds = mockDataGeneratorProvider.get().getLecturerIdCourseIds();
        lecturerCourseDao.save(lecturerIdCourseIds);
        dbInstance.flush();

        // Insert some students
        List<Student> students = mockDataGeneratorProvider.get().getStudents();
        studentDao.save(students);
        dbInstance.flush();

        // Add students to courses
        List<StudentIdCourseId> studentIdCourseIds = mockDataGeneratorProvider.get().getStudentIdCourseIds();
        studentCourseDao.save(studentIdCourseIds);
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
