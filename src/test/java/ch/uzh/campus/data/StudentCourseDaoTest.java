package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Initial Date: Oct 27, 2014 <br>
 * 
 * @author aabouc
 * @author lavinia
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class StudentCourseDaoTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	
	@Autowired
    private StudentCourseDao studentCourseDao;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private MockDataGenerator mockDataGenerator;

    private List<StudentCourse> studentCourses;

    @Before
    public void setup() {
        courseDao.save(mockDataGenerator.getCourses());
        studentDao.save(mockDataGenerator.getStudents());

        studentCourses = mockDataGenerator.getStudentCourses();
        studentCourseDao.save(studentCourses);
    }
    
    @After
    public void after() {
    	dbInstance.rollback();
    }

    @Test
    public void testGetAllSudentCourses() {
        assertEquals( 4, studentCourseDao.getAllSudentCourses().size());
    }
    
    @Test
    public void testDelete() {
        List<StudentCourse> studentCourses = studentCourseDao.getAllSudentCourses();
        assertEquals(studentCourses.size(), 4);
        studentCourseDao.delete(studentCourses.get(0));
        assertEquals(studentCourseDao.getAllSudentCourses().size(), 3);
    }
    /*
    @Test
    public void testDeleteByCourseId() {
        assertEquals(studentCourseDao.getAllSudentCourses().size(), 4);
        studentCourseDao.deleteByCourseId(100L);
        assertEquals(studentCourseDao.getAllSudentCourses().size(), 2);
    }

    @Test
    public void testDeleteByCourseIds() {
        assertEquals(studentCourseDao.getAllSudentCourses().size(), 4);
        List<Long> courseIds = new LinkedList<Long>();
        courseIds.add(100L);
        courseIds.add(200L);
        studentCourseDao.deleteByCourseIds(courseIds);
        assertEquals(studentCourseDao.getAllSudentCourses().size(), 0);
    }

    @Test
    public void testDeleteByStudentId() {
        assertEquals(studentCourseDao.getAllSudentCourses().size(), 4);
        studentCourseDao.deleteByStudentId(100L);
        assertEquals(studentCourseDao.getAllSudentCourses().size(), 2);
    }

    @Test
    public void testDeleteByStudentIds() {
        assertEquals(studentCourseDao.getAllSudentCourses().size(), 4);
        List<Long> studentIds = new LinkedList<Long>();
        studentIds.add(100L);
        studentIds.add(200L);
        studentCourseDao.deleteByStudentIds(studentIds);
        assertTrue(studentCourseDao.getAllSudentCourses().isEmpty());
    }

    @Test
    public void testDeleteAllNotUpdatedSCBooking() {
        Date now = new Date();
        studentCourses.get(0).setModifiedDate(now);
        studentCourseDao.saveOrUpdate(studentCourses);
        assertEquals(studentCourseDao.getAllSudentCourses().size(), 4);
        studentCourseDao.deleteAllNotUpdatedSCBooking(now);
        assertEquals(studentCourseDao.getAllSudentCourses().size(), 1);
    }*/

}
