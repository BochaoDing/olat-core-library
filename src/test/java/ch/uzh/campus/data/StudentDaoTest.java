package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;
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
    public void testGetAllOrphanedStudents() throws InterruptedException {
        int numberOfStudentsFoundBeforeInsertingTestData = studentDao.getAllOrphanedStudents().size();
        insertTestData();

        // Student 2300 has no courses. i.e. it is orphaned
        Student student = studentDao.getStudentById(2300L);
        assertEquals(0, student.getStudentCourses().size());

        assertEquals(numberOfStudentsFoundBeforeInsertingTestData + 1, studentDao.getAllOrphanedStudents().size());

        student = studentDao.getStudentById(2100L);
        assertEquals(3, student.getStudentCourses().size());

        // Remove all courses of students, i.e. make it orphaned
        List<StudentIdCourseId> studentIdCourseIds = new LinkedList<>();
        studentIdCourseIds.add(new StudentIdCourseId(2100L, 100L));
        studentIdCourseIds.add(new StudentIdCourseId(2100L, 200L));
        studentIdCourseIds.add(new StudentIdCourseId(2100L, 300L));

        studentCourseDao.deleteByStudentIdCourseIdsAsBulkDelete(studentIdCourseIds);
        dbInstance.flush();

        List<Long> studentIdsFound = studentDao.getAllOrphanedStudents();
        assertEquals(numberOfStudentsFoundBeforeInsertingTestData + 2, studentIdsFound.size());
        assertTrue(studentIdsFound.contains(2100L));
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
        List<CourseOrgId> courseOrgIds = mockDataGeneratorProvider.get().getCourseOrgIds();
        courseDao.save(courseOrgIds);
        dbInstance.flush();

        // Insert some studentIdCourseIds
        List<StudentIdCourseIdModifiedDate> studentIdCourseIdModifiedDates = mockDataGeneratorProvider.get().getStudentIdCourseIdModifiedDates();
        studentCourseDao.save(studentIdCourseIdModifiedDates);
        dbInstance.flush();
    }
}
