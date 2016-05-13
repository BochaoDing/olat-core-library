package ch.uzh.campus.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.*;

import javax.inject.Provider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.context.ContextConfiguration;

/**
 * Initial Date: Oct 28, 2014 <br>
 * 
 * @author aabouc
 * @author lavinia
 */

@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml" })
public class StudentDaoTest extends OlatTestCase {

    @Autowired
    private DB dbInstance;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    private List<Student> students;

    @Before
    public void setup() {
        // Insert some students
        students = mockDataGeneratorProvider.get().getStudents();
        studentDao.save(students);
        dbInstance.flush();

        // Insert some courses
        List<Course> courses = mockDataGeneratorProvider.get().getCourses();
        courseDao.save(courses);
        dbInstance.flush();
    }
    
    @After
    public void after() {
    	dbInstance.rollback();
    }

    @Test
    public void testAddStudentToCourse() {
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(0, course.getStudents().size());
        assertNotNull(studentDao.getStudentById(2100L));

        // Add a student
        studentDao.addStudentToCourse(2100L, 100L);
        dbInstance.flush();
        dbInstance.getCurrentEntityManager().clear();

        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getStudents().size());
    }

    @Test
    public void testAddStudentsToCourse() {
        Course course = courseDao.getCourseById(200L);
        assertNotNull(course);
        assertEquals(0, course.getStudents().size());

        addStudentsToCourses();
        dbInstance.getCurrentEntityManager().clear();

        course = courseDao.getCourseById(200L);
        assertEquals(2, course.getStudents().size());
    }

    @Test
    public void testGetStudentById_Null() {
        assertNull(studentDao.getStudentById(2999L));
    }

    @Test
    public void testGetStudentById_NotNull() {
        assertNotNull(studentDao.getStudentById(2100L));
    }

    
    @Test
    public void testGetStudentByEmail_Null() {
        assertNull(studentDao.getStudentByEmail("wrongEmail"));
    }

    @Test
    public void testlGetStudentByEmail_NotNull() {
        assertNotNull(studentDao.getStudentByEmail("email1"));
    }

    @Test
    public void testGetStudentByRegistrationNr_Null() {
        assertNull(studentDao.getStudentByEmail("999L"));
    }

    
    @Test
    public void testGetStudentByRegistrationNr_NotNull() {
        assertNotNull(studentDao.getStudentByRegistrationNr("1000"));
    }

    @Test
    public void testGetAllNotUpdatedStudents_foundTwoStudents() {
        assertEquals(2, studentDao.getAllNotUpdatedStudents(new Date()).size());
    }
    
    @Test
    public void testGetAllNotUpdatedStudents_foundOneStudent() throws InterruptedException {
        Calendar now = new GregorianCalendar();
        // To avoid rounding problems
        Calendar nowMinusOneSecond = (Calendar) now.clone();
        nowMinusOneSecond.add(Calendar.SECOND, -1);

        students.get(0).setModifiedDate(now.getTime());
        dbInstance.flush();

        assertEquals(1, studentDao.getAllNotUpdatedStudents(nowMinusOneSecond.getTime()).size());
    }

    @Test
    public void testDelete() {
        assertNotNull(studentDao.getStudentById(2100L));

        // Check bidirectional deletion
        addStudentsToCourses();
        Course course = courseDao.getCourseById(200L);
        assertNotNull(course);
        assertEquals(2, course.getStudents().size());

        studentDao.delete(students.get(0));
        dbInstance.flush();
        dbInstance.getCurrentEntityManager().clear();

        assertNull(studentDao.getStudentById(2100L));
        assertEquals(1, course.getStudents().size());
    }

    @Test
    public void testDeleteByStudentIds() {
        assertNotNull(studentDao.getStudentById(2100L));
        assertNotNull(studentDao.getStudentById(2200L));

        // Check bidirectional deletion
        addStudentsToCourses();
        Course course = courseDao.getCourseById(200L);
        assertNotNull(course);
        assertEquals(2, course.getStudents().size());

        List<Long> studentIds = new LinkedList<>();
        studentIds.add(2100L);
        studentIds.add(2200L);

        studentDao.deleteByStudentIds(studentIds);
        dbInstance.flush();
        dbInstance.getCurrentEntityManager().clear();

        assertNull(studentDao.getStudentById(2100L));
        assertNull(studentDao.getStudentById(2200L));
        assertEquals(0, course.getStudents().size());
    }

    @Test
    public void testGetAllPilotStudents() {
        addStudentsToCourses();
        assertEquals(2, studentDao.getAllPilotStudents().size());
    }

    private void addStudentsToCourses() {
        List<StudentIdCourseId> studentIdCourseIds = mockDataGeneratorProvider.get().getStudentIdCourseIds();
        studentDao.addStudentsToCourse(studentIdCourseIds);
        dbInstance.flush();
    }
}
