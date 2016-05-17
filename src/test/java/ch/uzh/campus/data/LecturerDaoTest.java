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
 * @author Martin Schraner
 */

@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class LecturerDaoTest extends OlatTestCase {

    @Autowired
    private DB dbInstance;

    @Autowired
    private LecturerDao lecturerDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    private List<Lecturer> lecturers;

    @Before
    public void setup() {
        // Insert some lecturers
        lecturers = mockDataGeneratorProvider.get().getLecturers();
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
    public void testAddLecturerToCourse() {
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(0, lecturer.getCourses().size());
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(0, course.getLecturers().size());

        // Add a lecturer
        lecturerDao.addLecturerToCourse(1100L, 100L);

        // Check before flush
        assertEquals(1, lecturer.getCourses().size());
        assertEquals(1, course.getLecturers().size());

        dbInstance.flush();
        dbInstance.clear();

        lecturer = lecturerDao.getLecturerById(1100L);
        assertEquals(1, lecturer.getCourses().size());
        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getLecturers().size());
    }

    @Test
    public void testAddLecturersToCourse() {
        Course course = courseDao.getCourseById(200L);
        assertNotNull(course);
        assertEquals(0, course.getLecturers().size());

        addLecturersToCourses();
        dbInstance.clear();

        course = courseDao.getCourseById(200L);
        assertEquals(2, course.getLecturers().size());
    }

    @Test
    public void testNullGetLecturerById() {
        assertNull(lecturerDao.getLecturerById(1999L));
    }

    @Test
    public void testNotNullGetLecturerById() {
        assertNotNull(lecturerDao.getLecturerById(1100L));
    }

    @Test
    public void testNullGetLecturerByEmail() {
        assertNull(lecturerDao.getLecturerByEmail("wrongEmail"));
    }

    @Test
    public void testNotNullGetLecturerByEmail() {
        assertNotNull(lecturerDao.getLecturerByEmail("email1"));
    }

    @Test
    public void testGetAllNotUpdatedLecturers_foundTwoLecturers() {
        assertEquals(2, lecturerDao.getAllNotUpdatedLecturers(new Date()).size());
    }

    @Test
    public void testGetAllNotUpdatedLecturers_foundOneLecturer() throws InterruptedException {
        Calendar now = new GregorianCalendar();
        // To avoid rounding problems
        Calendar nowMinusOneSecond = (Calendar) now.clone();
        nowMinusOneSecond.add(Calendar.SECOND, -1);

        lecturers.get(0).setModifiedDate(now.getTime());
        dbInstance.flush();

        assertEquals(1, lecturerDao.getAllNotUpdatedLecturers(nowMinusOneSecond.getTime()).size());
    }

    @Test
    public void testDelete() {
        assertNotNull(lecturerDao.getLecturerById(1100L));

        // Check bidirectional deletion
        addLecturersToCourses();
        Course course = courseDao.getCourseById(200L);
        assertNotNull(course);
        assertEquals(2, course.getLecturers().size());

        lecturerDao.delete(lecturers.get(0));

        // Check before flush
        assertEquals(1, course.getLecturers().size());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(lecturerDao.getLecturerById(1100L));
        assertEquals(1, course.getLecturers().size());
    }

    @Test
    public void testDeleteByLecturerIds() {
        assertNotNull(lecturerDao.getLecturerById(1100L));
        assertNotNull(lecturerDao.getLecturerById(1200L));

        // Check bidirectional deletion
        addLecturersToCourses();
        Course course = courseDao.getCourseById(200L);
        assertNotNull(course);
        assertEquals(2, course.getLecturers().size());

        List<Long> lecturerIds = new LinkedList<>();
        lecturerIds.add(1100L);
        lecturerIds.add(1200L);

        lecturerDao.deleteByLecturerIds(lecturerIds);

        // Check before flush
        assertEquals(0, course.getLecturers().size());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(lecturerDao.getLecturerById(1100L));
        assertNull(lecturerDao.getLecturerById(1200L));
        assertEquals(0, course.getLecturers().size());
    }

    @Test
    public void testGetAllPilotLecturers() {
        addLecturersToCourses();
        assertEquals(2, lecturerDao.getAllPilotLecturers().size());
    }

    private void addLecturersToCourses() {
        List<LecturerIdCourseId> lecturerIdCourseIds = mockDataGeneratorProvider.get().getLecturerIdCourseIds();
        lecturerDao.addLecturersToCourse(lecturerIdCourseIds);
        dbInstance.flush();
    }

}