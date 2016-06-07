package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Initial Date: Oct 28, 2014 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */

@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml" })
public class StudentCourseDaoTest extends OlatTestCase {

    @Autowired
    private DB dbInstance;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private StudentCourseDao studentCourseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    @Before
    public void setup() {
        // Insert some students
        List<Student> students = mockDataGeneratorProvider.get().getStudents();
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
    public void testSaveStudentCourse() {
        Student student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertEquals(0, student.getStudentCourses().size());
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(0, course.getStudentCourses().size());
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));

        // Add student to course
        StudentCourse studentCourse = new StudentCourse(student, course, new Date());
        studentCourseDao.save(studentCourse);

        // Check before flush
        assertEquals(1, student.getStudentCourses().size());
        assertEquals(1, course.getStudentCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        student = studentDao.getStudentById(2100L);
        assertEquals(1, student.getStudentCourses().size());
        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getStudentCourses().size());
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
    }

    @Test
    public void testSaveStudentCourse_NotExistingCourse() {
        StudentIdCourseId studentIdCourseId = new StudentIdCourseId();
        studentIdCourseId.setStudentId(2100L);
        studentIdCourseId.setCourseId(999L);
        studentIdCourseId.setModifiedDate(new Date());

        studentCourseDao.save(studentIdCourseId);

        dbInstance.flush();
        dbInstance.clear();

        assertNull(studentCourseDao.getStudentCourseById(2100L, 999L));
    }

    @Test
    public void testSaveStudentCourse_NotExistingStudent() {
        StudentIdCourseId studentIdCourseId = new StudentIdCourseId();
        studentIdCourseId.setStudentId(999L);
        studentIdCourseId.setCourseId(100L);
        studentIdCourseId.setModifiedDate(new Date());

        studentCourseDao.save(studentIdCourseId);

        dbInstance.flush();
        dbInstance.clear();

        assertNull(studentCourseDao.getStudentCourseById(999L, 100L));
    }

    @Test
    public void testSaveStudentCourses() {
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNull(studentCourseDao.getStudentCourseById(2200L, 100L));

        insertStudentIdCourseIds();
        dbInstance.clear();

        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNotNull(studentCourseDao.getStudentCourseById(2200L, 100L));
    }

    @Test
    public void testSaveOrUpdateStudentCourse() {
        Student student = studentDao.getStudentById(2100L);
        Course course = courseDao.getCourseById(100L);
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));

        // Insert student to course
        StudentCourse studentCourse = new StudentCourse(student, course, new Date());
        studentCourseDao.saveOrUpdate(studentCourse);

        dbInstance.flush();
        dbInstance.clear();

        student = studentDao.getStudentById(2100L);
        assertEquals(1, student.getStudentCourses().size());
        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getStudentCourses().size());
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));

        // Insert the same student a second time to the same course
        StudentCourse studentCourse2 = new StudentCourse(student, course, new Date());
        studentCourseDao.saveOrUpdate(studentCourse2);

        dbInstance.flush();
        dbInstance.clear();
    }

    @Test
    public void testGetStudentCourseById_Null() {
        insertStudentIdCourseIds();
        assertNull(studentCourseDao.getStudentCourseById(999L, 999L));
    }

    @Test
    public void testGetStudentCourseById_NotNull() {
        insertStudentIdCourseIds();
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
    }

    @Test
    public void testDeleteStudentCourse() {
        // Insert student to course
        Student student = studentDao.getStudentById(2100L);
        Course course = courseDao.getCourseById(100L);
        StudentCourse studentCourse = new StudentCourse(student, course, new Date());
        studentCourseDao.save(studentCourse);

        dbInstance.flush();

        assertEquals(1, student.getStudentCourses().size());
        assertEquals(1, course.getStudentCourses().size());

        // Delete
        studentCourseDao.delete(studentCourse);

        // Check before flush
        assertEquals(0, student.getStudentCourses().size());
        assertEquals(0, course.getStudentCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        student = studentDao.getStudentById(2100L);
        assertEquals(0, student.getStudentCourses().size());
        course = courseDao.getCourseById(100L);
        assertEquals(0, course.getStudentCourses().size());
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
    }

    @Test
    public void testDeleteAllNotUpdatedSCBooking() {
        Student student = studentDao.getStudentById(2100L);
        Course course1 = courseDao.getCourseById(100L);
        Course course2 = courseDao.getCourseById(200L);
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNull(studentCourseDao.getStudentCourseById(2100L, 200L));

        // Insert student to course with date in the past
        StudentCourse studentCourse1 = new StudentCourse(student, course1, new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime());
        studentCourseDao.saveOrUpdate(studentCourse1);

        // Insert student to course with date in the future
        StudentCourse studentCourse2 = new StudentCourse(student, course2, new GregorianCalendar(2035, Calendar.JANUARY, 1).getTime());
        studentCourseDao.saveOrUpdate(studentCourse2);

        dbInstance.flush();

        assertEquals(2, student.getStudentCourses().size());
        assertEquals(1, course1.getStudentCourses().size());
        assertEquals(1, course2.getStudentCourses().size());

        studentCourseDao.deleteAllNotUpdatedSCBooking(new Date());

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(1, student.getStudentCourses().size());
        assertEquals(0, course1.getStudentCourses().size());
        assertEquals(1, course2.getStudentCourses().size());
    }


    private void insertStudentIdCourseIds() {
        List<StudentIdCourseId> studentIdCourseIds = mockDataGeneratorProvider.get().getStudentIdCourseIds();
        studentCourseDao.save(studentIdCourseIds);
        dbInstance.flush();
    }

}
