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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Initial Date: Oct 28, 2014 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */

@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml" })
public class LecturerCourseDaoTest extends OlatTestCase {

    @Autowired
    private DB dbInstance;

    @Autowired
    private LecturerDao lecturerDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private LecturerCourseDao lecturerCourseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    @Before
    public void setup() {
        // Insert some lecturers
        List<Lecturer> lecturers = mockDataGeneratorProvider.get().getLecturers();
        lecturerDao.save(lecturers);
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
    public void testSaveLecturerCourse() {
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(0, lecturer.getLecturerCourses().size());
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(0, course.getLecturerCourses().size());
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));

        // Add lecturer to course
        LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, new Date());
        lecturerCourseDao.save(lecturerCourse);

        // Check before flush
        assertEquals(1, lecturer.getLecturerCourses().size());
        assertEquals(1, course.getLecturerCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        lecturer = lecturerDao.getLecturerById(1100L);
        assertEquals(1, lecturer.getLecturerCourses().size());
        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getLecturerCourses().size());
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
    }

    @Test
    public void testSaveLecturerCourses() {
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNull(lecturerCourseDao.getLecturerCourseById(1200L, 200L));

        insertLecturerIdCourseIds();
        dbInstance.clear();

        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1200L, 200L));
    }

    @Test
    public void testSaveOrUpdateLecturerCourse() {
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        Course course = courseDao.getCourseById(100L);
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));

        // Insert lecturer to course
        LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, new Date());
        lecturerCourseDao.saveOrUpdate(lecturerCourse);

        dbInstance.flush();
        dbInstance.clear();

        lecturer = lecturerDao.getLecturerById(1100L);
        assertEquals(1, lecturer.getLecturerCourses().size());
        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getLecturerCourses().size());
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));

        // Insert the same lecturer a second time to the same course
        LecturerCourse lecturerCourse2 = new LecturerCourse(lecturer, course, new Date());
        lecturerCourseDao.saveOrUpdate(lecturerCourse2);

        dbInstance.flush();
        dbInstance.clear();
    }

    @Test
    public void testGetLecturerCourseById_Null() {
        insertLecturerIdCourseIds();
        assertNull(lecturerCourseDao.getLecturerCourseById(999L, 999L));
    }

    @Test
    public void testGetLecturerCourseById_NotNull() {
        insertLecturerIdCourseIds();
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
    }

    @Test
    public void testDeleteLecturerCourse() {
        // Insert lecturer to course
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        Course course = courseDao.getCourseById(100L);
        LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, new Date());
        lecturerCourseDao.save(lecturerCourse);

        dbInstance.flush();

        assertEquals(1, lecturer.getLecturerCourses().size());
        assertEquals(1, course.getLecturerCourses().size());

        // Delete
        lecturerCourseDao.delete(lecturerCourse);

        // Check before flush
        assertEquals(0, lecturer.getLecturerCourses().size());
        assertEquals(0, course.getLecturerCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        lecturer = lecturerDao.getLecturerById(1100L);
        assertEquals(0, lecturer.getLecturerCourses().size());
        course = courseDao.getCourseById(100L);
        assertEquals(0, course.getLecturerCourses().size());
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
    }

    @Test
    public void testDeleteAllNotUpdatedSCBooking() {
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        Course course1 = courseDao.getCourseById(100L);
        Course course2 = courseDao.getCourseById(200L);
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));

        // Insert lecturer to course with date in the past
        LecturerCourse lecturerCourse1 = new LecturerCourse(lecturer, course1, new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime());
        lecturerCourseDao.saveOrUpdate(lecturerCourse1);

        // Insert lecturer to course with date in the future
        LecturerCourse lecturerCourse2 = new LecturerCourse(lecturer, course2, new GregorianCalendar(2035, Calendar.JANUARY, 1).getTime());
        lecturerCourseDao.saveOrUpdate(lecturerCourse2);

        dbInstance.flush();

        assertEquals(2, lecturer.getLecturerCourses().size());
        assertEquals(1, course1.getLecturerCourses().size());
        assertEquals(1, course2.getLecturerCourses().size());

        lecturerCourseDao.deleteAllNotUpdatedLCBooking(new Date());

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(1, lecturer.getLecturerCourses().size());
        assertEquals(0, course1.getLecturerCourses().size());
        assertEquals(1, course2.getLecturerCourses().size());
    }

    private void insertLecturerIdCourseIds() {
        List<LecturerIdCourseId> lecturerIdCourseIds = mockDataGeneratorProvider.get().getLecturerIdCourseIds();
        lecturerCourseDao.save(lecturerIdCourseIds);
        dbInstance.flush();
    }
}
