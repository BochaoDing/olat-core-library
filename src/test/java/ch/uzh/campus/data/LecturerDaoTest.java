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

        // Add the lecturers to these courses
        List<LecturerIdCourseId> lecturerIdCourseIds = mockDataGeneratorProvider.get().getLecturerIdCourseIds();
        courseDao.addLecturersById(lecturerIdCourseIds);
        dbInstance.flush();
    }

    @After
    public void after() {
        dbInstance.rollback();
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

        lecturerDao.delete(lecturers.get(0));
        dbInstance.flush();
        dbInstance.getCurrentEntityManager().clear();

        assertNull(lecturerDao.getLecturerById(1100L));

        // Check bidirectional deletion
        Course course = courseDao.getCourseById(200L);
        assertNotNull(course);
        assertEquals(1, course.getLecturers().size());
    }

    @Test
    public void testDeleteByLecturerIds() {
        assertNotNull(lecturerDao.getLecturerById(1100L));
        assertNotNull(lecturerDao.getLecturerById(1200L));

        Course course = courseDao.getCourseById(200L);
        assertNotNull(course);
        assertEquals(2, course.getLecturers().size());

        List<Long> lecturerIds = new LinkedList<>();
        lecturerIds.add(1100L);
        lecturerIds.add(1200L);

        lecturerDao.deleteByLecturerIds(lecturerIds);
        dbInstance.flush();
        dbInstance.getCurrentEntityManager().clear();

        assertNull(lecturerDao.getLecturerById(1100L));
        assertNull(lecturerDao.getLecturerById(1200L));

        // Check bidirectional deletion
        assertEquals(0, course.getLecturers().size());
    }

    @Test
    public void testGetAllPilotLecturers() {
        assertEquals(2, lecturerDao.getAllPilotLecturers().size());
    }

}