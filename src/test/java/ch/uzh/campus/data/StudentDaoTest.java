package ch.uzh.campus.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
    private StudentDao studentDao;
    
    @Autowired
	private DB dbInstance;

    //@Autowired
    //private CourseDao courseDao;

    //@Autowired
    //private StudentCourseDao studentCourseDao;

    @Autowired
    private MockDataGenerator mockDataGenerator;

    private List<Student> students;
    

    @Before
    public void setup() {
        students = mockDataGenerator.getStudents();
        studentDao.save(students);
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
    public void testlGetStudentByEmail_NotNul() {
        assertNotNull(studentDao.getStudentByEmail("email1"));
    }

    @Test
    public void testGetStudentByRegistrationNr_Null() {
        //assertNull(studentDao.getStudentByEmail("999L"));
    }

    /*@Test
    public void testNotNullGetStudentByRegistrationNr() {
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
        studentDao.saveOrUpdate(students);

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
        transactionManager.getSessionFactory().getCurrentSession().clear();

        assertNull(studentDao.getStudentById(100L));
        assertNull(studentDao.getStudentById(200L));
    }

    @Test
    public void testGetAllPilotStudents() {
        courseDao.saveOrUpdate(mockDataGenerator.getCourses());
        studentCourseDao.saveOrUpdate(mockDataGenerator.getStudentCourses());
        assertEquals(studentDao.getAllPilotStudents().size(), 2);
    }*/
}
