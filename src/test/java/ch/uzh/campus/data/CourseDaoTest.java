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

import static org.junit.Assert.*;

/**
 * Initial Date: Oct 28, 2014 <br>
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

    @Before
    public void setup() {
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

        // Add some events
        List<EventCourseId> eventCourseIds = mockDataGeneratorProvider.get().getEventCourseIds();
        eventDao.addEventsToCourse(eventCourseIds);
    }

    @After
    public void after() {
        dbInstance.rollback();
    }

    @Test
    public void testNullGetCourseById() {
        assertNull(courseDao.getCourseById(999L));
    }

    @Test
    public void testNotNullGetCourseById() {
        assertNotNull(courseDao.getCourseById(100L));
    }

    @Test
    public void testGetCreatedCoursesByLecturerIds() {
        List<Long> lecturerIds = new LinkedList<>();
        lecturerIds.add(1100L);
        lecturerIds.add(1200L);
        List<Course> courses = courseDao.getCreatedCoursesByLecturerIds(lecturerIds);

        assertNotNull(courses);
        assertEquals(2, courses.size());
    }

    @Test
    public void testGetNotCreatedCoursesByLecturerIds() {
        List<Long> lecturerIds = new LinkedList<>();
        lecturerIds.add(1100L);
        List<Course> courses = courseDao.getNotCreatedCoursesByLecturerIds(lecturerIds);

        assertNotNull(courses);
        assertEquals(1, courses.size());

        // Ditto for lecturer 1200
        lecturerIds = new LinkedList<>();
        lecturerIds.add(1200L);
        courses = courseDao.getNotCreatedCoursesByLecturerIds(lecturerIds);

        assertTrue(courses.isEmpty());
    }

    @Test
    public void testDelete() {
        assertNotNull(courseDao.getCourseById(100L));
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(3, lecturer.getLecturerCourses().size());
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        Student student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertEquals(3, student.getStudentCourses().size());
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));

        courseDao.delete(courses.get(0));

        // Check before flush
        assertEquals(2, lecturer.getLecturerCourses().size());
        assertEquals(2, student.getStudentCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(courseDao.getCourseById(100L));

        lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(2, lecturer.getLecturerCourses().size());
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertEquals(2, student.getStudentCourses().size());
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
    }

    @Test
    public void testDeleteByCourseId() {
        assertNotNull(courseDao.getCourseById(100L));
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(3, lecturer.getLecturerCourses().size());
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        Student student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertEquals(3, student.getStudentCourses().size());
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertFalse(textDao.getTextsByCourseId(100L).isEmpty());
        assertFalse(eventDao.getEventsByCourseId(100L).isEmpty());

        courseDao.deleteByCourseId(100L);

        // Check before flush
        assertEquals(2, lecturer.getLecturerCourses().size());
        assertEquals(2, student.getStudentCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(courseDao.getCourseById(100L));

        lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(2, lecturer.getLecturerCourses().size());
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
    public void testSaveResourceableIdAsBulkUpdate() {
        Course course = courseDao.getCourseById(100L);
        assertEquals(100L, course.getResourceableId().longValue());
        
        courseDao.saveResourceableIdAsBulkUpdate(100L, 1000L);

        dbInstance.flush();
        dbInstance.clear();
        
        Course updatedCourse = courseDao.getCourseById(100L);
        assertEquals(1000L, updatedCourse.getResourceableId().longValue());
    }

    @Test
    public void testDisableSynchronizationAsBulkUpdate() {
        Course course = courseDao.getCourseById(100L);
        assertTrue(course.isSynchronizable());
        
        courseDao.disableSynchronizationAsBulkUpdate(100L);

        dbInstance.flush();
        dbInstance.clear();
        
        Course updatedCourse = courseDao.getCourseById(100L);
        assertFalse(updatedCourse.isSynchronizable());
    }

    @Test
    public void testDeleteResourceableIdAsBulkUpdate() {
        Course course = courseDao.getCourseById(100L);
        assertEquals(100L, course.getResourceableId().longValue());
        
        courseDao.deleteResourceableIdAsBulkUpdate(100L);

        dbInstance.flush();
        dbInstance.clear();
        
        Course updatedCourse = courseDao.getCourseById(100L);
        assertNull(updatedCourse.getResourceableId());
    }

    @Test
    public void testGetResourceableIdsOfAllCreatedCourses() {
        List<Long> resourceableIdsOfAllCreatedCourses = courseDao.getResourceableIdsOfAllCreatedCourses();
        assertTrue(resourceableIdsOfAllCreatedCourses.contains(100L));
        assertFalse(resourceableIdsOfAllCreatedCourses.contains(999L));
    }

    @Test
    public void testGetIdsOfAllCreatedCourses() {
        assertEquals(2, courseDao.getIdsOfAllCreatedCourses().size());

        courseDao.deleteResourceableIdAsBulkUpdate(100L);

        dbInstance.flush();
        dbInstance.clear();
        
        assertEquals(1, courseDao.getIdsOfAllCreatedCourses().size());
    }

    @Test
    public void testGetIdsOfAllNotCreatedCourses() {
        assertEquals(1, courseDao.getIdsOfAllNotCreatedCourses().size());
        
        courseDao.deleteResourceableIdAsBulkUpdate(100L);

        dbInstance.flush();
        dbInstance.clear();
        
        assertEquals(2, courseDao.getIdsOfAllNotCreatedCourses().size());
    }

    @Test
    public void testGetAllCreatedCourses() {
        assertEquals(2, courseDao.getAllCreatedCourses().size());

        courseDao.deleteByCourseId(100L);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(1, courseDao.getAllCreatedCourses().size());
    }

    @Test
    public void tesGetAllNotUpdatedCourses() {
        Calendar now = new GregorianCalendar();
        // To avoid rounding problems
        Calendar nowMinusOneSecond = (Calendar) now.clone();
        nowMinusOneSecond.add(Calendar.SECOND, -1);

    	List<Long> courseIds = courseDao.getAllNotUpdatedCourses(nowMinusOneSecond.getTime());
        // Only courses with resourceableId = null
    	assertEquals(1, courseIds.size());

        assertNull(courses.get(2).getResourceableId());
        courses.get(2).setModifiedDate(now.getTime());

        dbInstance.flush();
        dbInstance.clear();

        courseIds = courseDao.getAllNotUpdatedCourses(nowMinusOneSecond.getTime());
        assertEquals(0, courseIds.size());
    }

    @Test
    public void testExistResourceableId() {
        assertTrue(courseDao.existResourceableId(100L));

        courseDao.deleteResourceableIdAsBulkUpdate(100L);

        dbInstance.flush();
        dbInstance.clear();

        assertFalse(courseDao.existResourceableId(100L));
    }

    @Test
    public void testGetCreatedCoursesByStudentId_noneFound() {
    	List<Course> courses = courseDao.getCreatedCoursesByStudentId(2300L);
    	assertEquals(0, courses.size());
    }
    
    @Test
    public void testGetCreatedCoursesByStudentId_twoFound() {
    	List<Course> courses = courseDao.getCreatedCoursesByStudentId(2100L);
    	assertEquals(2, courses.size());
    }
    
    @Test
    public void testGetNotCreatedCoursesByStudentId_noneFound() {
    	List<Course> courses = courseDao.getNotCreatedCoursesByStudentId(2100L);
    	assertEquals(1, courses.size());
    }
}