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
 * Initial Date: Oct 28, 2014 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
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
    private StudentCourseDao studentCourseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    private List<Student> students;
    
    @After
    public void after() {
    	dbInstance.rollback();
    }

    @Test
    public void testGetStudentById_Null() {
        insertTestData();
        assertNull(studentDao.getStudentById(2999L));
    }

    @Test
    public void testGetStudentById_NotNull() {
        insertTestData();
        assertNotNull(studentDao.getStudentById(2100L));
    }

    @Test
    public void testGetStudentByEmail_Null() {
        insertTestData();
        assertNull(studentDao.getStudentByEmail("wrongEmail"));
    }

    @Test
    public void testlGetStudentByEmail_NotNull() {
        insertTestData();
        assertNotNull(studentDao.getStudentByEmail("email1"));
    }

    @Test
    public void testGetStudentByRegistrationNr_Null() {
        insertTestData();
        assertNull(studentDao.getStudentByEmail("999L"));
    }

    @Test
    public void testGetStudentByRegistrationNr_NotNull() {
        insertTestData();
        assertNotNull(studentDao.getStudentByRegistrationNr("1000"));
    }

    @Test
    public void testGetAllNotUpdatedStudents_foundSixStudents() {
        int numberOfStudentsFoundBeforeInsertingTestData = studentDao.getAllNotUpdatedStudents(new Date()).size();
        insertTestData();
        assertEquals(numberOfStudentsFoundBeforeInsertingTestData + 6, studentDao.getAllNotUpdatedStudents(new Date()).size());
    }
    
    @Test
    public void testGetAllNotUpdatedStudents_foundFiveStudents() throws InterruptedException {
        Date now = new Date();
        int numberOfStudentsFoundBeforeInsertingTestData = studentDao.getAllNotUpdatedStudents(now).size();
        insertTestData();
        students.get(0).setModifiedDate(now);
        dbInstance.flush();
        assertEquals(numberOfStudentsFoundBeforeInsertingTestData + 5, studentDao.getAllNotUpdatedStudents(now).size());
    }

    @Test
    public void testDelete() {
        insertTestData();
        assertNotNull(studentDao.getStudentById(2100L));
        Course course = courseDao.getCourseById(200L);
        assertNotNull(course);
        assertEquals(2, course.getStudentCourses().size());
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));

        studentDao.delete(students.get(0));

        // Check before flush
        assertEquals(1, course.getStudentCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(studentDao.getStudentById(2100L));
        assertEquals(1, course.getStudentCourses().size());
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
    }

    @Test
    public void testDeleteByStudentIds() {
        insertTestData();
        assertNotNull(studentDao.getStudentById(2100L));
        assertNotNull(studentDao.getStudentById(2200L));
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(2, course.getStudentCourses().size());
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNotNull(studentCourseDao.getStudentCourseById(2200L, 100L));

        List<Long> studentIds = new LinkedList<>();
        studentIds.add(2100L);
        studentIds.add(2200L);

        studentDao.deleteByStudentIds(studentIds);

        // Check before flush
        assertEquals(0, course.getStudentCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(studentDao.getStudentById(2100L));
        assertNull(studentDao.getStudentById(2200L));
        assertEquals(0, course.getStudentCourses().size());
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNull(studentCourseDao.getStudentCourseById(2200L, 100L));
    }

    @Test
    public void testDeleteByStudentIdsAsBulkDelete() {
        insertTestData();
        assertNotNull(studentDao.getStudentById(2100L));
        assertNotNull(studentDao.getStudentById(2200L));
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNotNull(studentCourseDao.getStudentCourseById(2200L, 100L));

        List<Long> studentIds = new LinkedList<>();
        studentIds.add(2100L);
        studentIds.add(2200L);

        studentCourseDao.deleteByStudentIdsAsBulkDelete(studentIds);
        studentDao.deleteByStudentIdsAsBulkDelete(studentIds);

        dbInstance.flush();
        dbInstance.clear();

        assertNull(studentDao.getStudentById(2100L));
        assertNull(studentDao.getStudentById(2200L));
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNull(studentCourseDao.getStudentCourseById(2200L, 100L));
    }

    @Test
    public void testGetAllPilotStudents() {
        int numberOfStudentsFoundBeforeInsertingTestData = studentDao.getAllPilotStudents().size();
        insertTestData();
        assertEquals(numberOfStudentsFoundBeforeInsertingTestData + 5, studentDao.getAllPilotStudents().size());
    }

    private void insertTestData() {
        // Insert some students
        students = mockDataGeneratorProvider.get().getStudents();
        studentDao.save(students);
        dbInstance.flush();

        // Insert some courses
        List<Course> courses = mockDataGeneratorProvider.get().getCourses();
        courseDao.save(courses);
        dbInstance.flush();

        // Insert some studentIdCourseIds
        List<StudentIdCourseId> studentIdCourseIds = mockDataGeneratorProvider.get().getStudentIdCourseIds();
        studentCourseDao.save(studentIdCourseIds);
        dbInstance.flush();
    }
}
