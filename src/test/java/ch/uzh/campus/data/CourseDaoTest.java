package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
    private StudentDao studentDao;

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
        lecturerDao.addLecturersToCourse(lecturerIdCourseIds);
        dbInstance.flush();

        // Insert some students
        List<Student> students = mockDataGeneratorProvider.get().getStudents();
        studentDao.save(students);
        dbInstance.flush();

        // Add students to courses
        List<StudentIdCourseId> studentIdCourseIds = mockDataGeneratorProvider.get().getStudentIdCourseIds();
        studentDao.addStudentsToCourse(studentIdCourseIds);
        dbInstance.flush();
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
    public void testGetPilotCoursesByLecturerId() {
        List<Course> courses = courseDao.getPilotCoursesByLecturerId(1100L);

        assertNotNull(courses);
        assertEquals(3, courses.size());
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

        courseDao.delete(courses.get(0));
        dbInstance.flush();
        dbInstance.getCurrentEntityManager().clear();

        assertNull(courseDao.getCourseById(100L));
    }

    @Test
    public void testDeleteByCourseId() {
        assertNotNull(courseDao.getCourseById(100L));

        // Check bidirectional deletion
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(3, lecturer.getCourses().size());

        courseDao.deleteByCourseId(100L);
        dbInstance.flush();
        dbInstance.getCurrentEntityManager().clear();

        assertNull(courseDao.getCourseById(100L));
        lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(2, lecturer.getCourses().size());
    }


    @Test
    public void testSaveResourceableId() {
        Course course = courseDao.getCourseById(100L);
        assertEquals(course.getResourceableId().longValue(), 100L);
        
        courseDao.saveResourceableId(100L, 1000L);        
        dbInstance.getCurrentEntityManager().clear();
        
        Course updatedCourse = courseDao.getCourseById(100L);
        assertEquals(updatedCourse.getResourceableId().longValue(), 1000L);
    }

    @Test
    public void testDisableSynchronization() {
        Course course = courseDao.getCourseById(100L);
        assertTrue(course.isSynchronizable());
        
        courseDao.disableSynchronization(100L);
        dbInstance.getCurrentEntityManager().clear();
        
        Course updatedCourse = courseDao.getCourseById(100L);
        assertFalse(updatedCourse.isSynchronizable());
    }

    @Test
    public void testDeleteResourceableId() {
        Course course = courseDao.getCourseById(100L);
        assertEquals(course.getResourceableId().longValue(), 100L);
        
        courseDao.deleteResourceableId(100L);
        dbInstance.getCurrentEntityManager().clear();
        
        Course updatedCourse = courseDao.getCourseById(100L);
        assertNull(updatedCourse.getResourceableId());
    }

    @Test
    public void testDeleteByCourseIds() {
        assertEquals(courseDao.getAllCreatedCourses().size(), 2);

        List<Long> courseIds = new LinkedList<>();
        courseIds.add(100L);
        courseIds.add(200L);
        courseDao.deleteByCourseIds(courseIds);
        dbInstance.flush();
        dbInstance.getCurrentEntityManager().clear();

        assertEquals(courseDao.getAllCreatedCourses().size(), 0);
    }

    @Test
    public void testGetResourceableIdsOfAllCreatedCourses() {
        List<Long> resourceableIdsOfAllCreatedCourses = courseDao.getResourceableIdsOfAllCreatedCourses();
        assertTrue(resourceableIdsOfAllCreatedCourses.contains(100L));
        assertFalse(resourceableIdsOfAllCreatedCourses.contains(999L));
    }

    @Test
    public void testGetIdsOfAllCreatedCourses() {
        assertEquals(courseDao.getAllCreatedCourses().size(), 2);
        courseDao.deleteResourceableId(100L);
        
        dbInstance.getCurrentEntityManager().clear();
        
        assertEquals(courseDao.getAllCreatedCourses().size(), 1);
    }

    @Test
    public void testGetIdsOfAllNotCreatedCourses() {
        assertEquals(courseDao.getIdsOfAllNotCreatedCourses().size(), 1);
        
        courseDao.deleteResourceableId(100L);
        dbInstance.getCurrentEntityManager().clear();
        
        assertEquals(courseDao.getIdsOfAllNotCreatedCourses().size(), 2);
    }

    @Test
    public void testGetAllCreatedCourses() {
        assertEquals(courseDao.getAllCreatedCourses().size(), 2);
        courseDao.deleteByCourseId(100L);
        dbInstance.flush();
        dbInstance.getCurrentEntityManager().clear();
        assertEquals(courseDao.getAllCreatedCourses().size(), 1);
    }

    @Ignore
    @Test
    public void tesGetAllNotUpdatedCourses() {
    	List<Long> courseIds = courseDao.getAllNotUpdatedCourses(new Date());
    	assertEquals(1, courseIds.size());
    	
        courseDao.deleteResourceableId(100L);
        courseDao.deleteResourceableId(200L);
        dbInstance.getCurrentEntityManager().clear();
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        courseIds = courseDao.getAllNotUpdatedCourses(new Date());
        
        assertEquals( 3, courseIds.size());

        Date now = new Date();

        courses.get(0).setModifiedDate(now);
        courseDao.save(courses);
        dbInstance.getCurrentEntityManager().clear();
        
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        assertEquals(2, courseDao.getAllNotUpdatedCourses(new Date()).size());
    }

//    @Test
//    public void testExistResourceableId() {
//        assertTrue(courseDao.existResourceableId(100L));
//
//        courseDao.deleteResourceableId(100L);
//        transactionManager.getSessionFactory().getCurrentSession().clear();
//
//        assertFalse(courseDao.existResourceableId(100L));
//
//    }


//TODO Martin fixen!
//    @Test
//    public void testGetCreatedCoursesByStudentId_noneFound() {
//    	List<Course> courses = courseDao.getCreatedCoursesByStudentId(100L);
//    	assertEquals(0, courses.size());
//    }
    
    @Test
    public void testGetCreatedCoursesByStudentId_twoFound() {
    	List<Course> courses = courseDao.getCreatedCoursesByStudentId(100L);
    	assertEquals(2, courses.size());
    }
    
    @Test
    public void testGetNotCreatedCoursesByStudentId_noneFound() {
    	List<Course> courses = courseDao.getNotCreatedCoursesByStudentId(100L);
    	assertEquals(1, courses.size());
    }
}