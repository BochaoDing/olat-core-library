package ch.uzh.campus.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
    public void testGetStudentById_Null() {
        assertNull(studentDao.getStudentById(999L));
    }

    
    @Test
    public void testGetStudentById_NotNull() {
        assertNotNull(studentDao.getStudentById(100L));
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
        assertEquals(studentDao.getAllNotUpdatedStudents(new Date()).size(), 2);
    }

    
    @Test
    public void testGetAllNotUpdatedStudents_foundOneStudent() throws InterruptedException {
        Date now = new Date();

        students.get(0).setModifiedDate(now);
        studentDao.save(students);

        assertEquals(studentDao.getAllNotUpdatedStudents(now).size(), 1);
    }
    
    
    @Test
    public void testDelete() {
        assertNotNull(studentDao.getStudentById(100L));
        studentDao.delete(students.get(0));
        assertNull(studentDao.getStudentById(100L));
    }
   
    
    @Test
    public void testDeleteByStudentIds() {
        assertNotNull(studentDao.getStudentById(100L));
        assertNotNull(studentDao.getStudentById(200L));

        List<Long> studentIds = new LinkedList<Long>();
        studentIds.add(100L);
        studentIds.add(200L);

        studentDao.deleteByStudentIds(studentIds);
        //transactionManager.getSessionFactory().getCurrentSession().clear();
        dbInstance.getCurrentEntityManager().clear();
        
        assertNull(studentDao.getStudentById(100L));
        assertNull(studentDao.getStudentById(200L));
    }
      

    @Test
    public void testGetAllPilotStudents() {
        addStudentsToCourses();
        assertEquals(studentDao.getAllPilotStudents().size(), 2);
    }

    private void addStudentsToCourses() {
        List<StudentIdCourseId> studentIdCourseIds = mockDataGeneratorProvider.get().getStudentIdCourseIds();
        studentDao.addStudentsToCourse(studentIdCourseIds);
        dbInstance.flush();
    }
}
