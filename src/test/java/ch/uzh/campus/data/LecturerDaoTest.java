package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

    //TODO
//    @Autowired
//    private CourseDao courseDao;
//
//    @Autowired
//    private LecturerCourseDao lecturerCourseDao;

    @Autowired
    private MockDataGenerator mockDataGenerator;

    private List<Lecturer> lecturers;

    @Before
    public void setup() {
        lecturers = mockDataGenerator.getLecturers();
        lecturerDao.save(lecturers);
        dbInstance.flush();
    }

    @After
    public void after() {
        dbInstance.rollback();
    }

    @Test
    public void testNullGetLecturerById() {
        assertNull(lecturerDao.getLecturerById(999L));
    }

    @Test
    public void testNotNullGetLecturerById() {
        assertNotNull(lecturerDao.getLecturerById(100L));
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
        assertEquals(lecturerDao.getAllNotUpdatedLecturers(new Date()).size(), 2);
    }

//    @Test
//    public void testGetAllNotUpdatedLecturers_foundOneLecturer() throws InterruptedException {
//        Date now = new Date();
//
//        lecturers.get(0).setModifiedDate(now);
//        lecturerDao.saveOrUpdate(lecturers);
//
//        assertEquals(lecturerDao.getAllNotUpdatedLecturers(now).size(), 1);
//    }

    @Test
    public void testDelete() {
        assertNotNull(lecturerDao.getLecturerById(100L));
        lecturerDao.delete(lecturers.get(0));
        dbInstance.flush();
        assertNull(lecturerDao.getLecturerById(100L));
    }

//    @Test
//    public void testDeleteByLecturerIds() {
//        assertNotNull(lecturerDao.getLecturerById(100L));
//        assertNotNull(lecturerDao.getLecturerById(200L));
//
//        List<Long> lecturerIds = new LinkedList<Long>();
//        lecturerIds.add(100L);
//        lecturerIds.add(200L);
//
//        lecturerDao.deleteByLecturerIds(lecturerIds);
//        transactionManager.getSessionFactory().getCurrentSession().clear();
//
//        assertNull(lecturerDao.getLecturerById(100L));
//        assertNull(lecturerDao.getLecturerById(200L));
//    }
//
//    @Test
//    public void testGetAllPilotLecturers() {
//        courseDao.saveOrUpdate(mockDataGenerator.getCourses());
//        lecturerCourseDao.saveOrUpdate(mockDataGenerator.getLecturerCourses());
//        assertEquals(lecturerDao.getAllPilotLecturers().size(), 2);
//    }

}