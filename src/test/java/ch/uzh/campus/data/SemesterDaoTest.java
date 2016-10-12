package ch.uzh.campus.data;

import ch.uzh.campus.CampusCourseConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

/**
 * @author Martin Schraner
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class SemesterDaoTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    private SemesterDao semesterDao;

    private Semester semester1;
    private Semester semester2;
    private Semester semester3;
    private Semester semester4;
    private Semester semester5;
    private Semester semester6;
    private Semester semester7;
    private Semester semester8;
    private Semester semester9;
    private Semester semester10;

    @Before
    public void before() {
        campusCourseConfiguration.setMaxYearsToKeepCkData(3);
        semesterDao = new SemesterDao(dbInstance, campusCourseConfiguration);
        insertTestData();
    }
    
    @After
    public void after() {
    	dbInstance.rollback();
    }

    @Test
    public void testGetSemesterById() {
        Semester semester = semesterDao.getSemesterBySemesterNameAndYear(SemesterName.HERBSTSEMESTER, 2099);
        assertEquals(semester10, semester);
        Long id = semester.getId();
        dbInstance.clear();

        semester = semesterDao.getSemesterById(id);
        assertEquals(semester10, semester);
    }

    @Test
    public void testGetSemesterBySemesterNameAndYear() {
        assertEquals(semester1, semesterDao.getSemesterBySemesterNameAndYear(SemesterName.FRUEHJAHRSSEMESTER, 2095));
        assertNull(semesterDao.getSemesterBySemesterNameAndYear(SemesterName.HERBSTSEMESTER, 3096));
        assertNull(semesterDao.getSemesterBySemesterNameAndYear(SemesterName.HERBSTSEMESTER, null));
        assertNull(semesterDao.getSemesterBySemesterNameAndYear(null, 2096));
        assertNull(semesterDao.getSemesterBySemesterNameAndYear(null, null));
    }

    @Test
    public void testGetSemestersInAscendingOrder() {
        List<Semester> semesters = semesterDao.getSemestersInAscendingOrder();
        assertTrue(semesters.size() >= 10);
        assertEquals(semester10, semesters.get(semesters.size() - 1));
        assertEquals(semester9, semesters.get(semesters.size() - 2));
        assertEquals(semester8, semesters.get(semesters.size() - 3));
        assertEquals(semester7, semesters.get(semesters.size() - 4));
        assertEquals(semester6, semesters.get(semesters.size() - 5));
        assertEquals(semester5, semesters.get(semesters.size() - 6));
        assertEquals(semester4, semesters.get(semesters.size() - 7));
        assertEquals(semester3, semesters.get(semesters.size() - 8));
        assertEquals(semester2, semesters.get(semesters.size() - 9));
        assertEquals(semester1, semesters.get(semesters.size() - 10));
    }

    @Test
    public void testGetPreviousSemestersNotTooFarInThePastInDescendingOrder() {
        List<Long> ids = semesterDao.getPreviousSemestersNotTooFarInThePastInDescendingOrder();
        assertEquals(6, ids.size());
        assertEquals(semester7.getId(), ids.get(0));
        assertEquals(semester6.getId(), ids.get(1));
        assertEquals(semester5.getId(), ids.get(2));
        assertEquals(semester4.getId(), ids.get(3));
        assertEquals(semester3.getId(), ids.get(4));
        assertEquals(semester2.getId(), ids.get(5));
    }

    @Test
    public void testGetCurrentSemester() {
        semesterDao.unsetCurrentSemester();
        dbInstance.flush();

        semesterDao.setCurrentSemester(semester9.getId());
        dbInstance.flush();

        Semester semester = semesterDao.getCurrentSemester();
        assertEquals(semester9, semester);
    }

    @Test
    public void testSetCurrentSemester() {
        semesterDao.setCurrentSemester(semester9.getId());
        assertTrue(semester9.isCurrentSemester());

        // Check after flush
        dbInstance.flush();
        Semester semester = semesterDao.getSemesterBySemesterNameAndYear(SemesterName.FRUEHJAHRSSEMESTER, 2099);
        assertNotNull(semester);
        assertTrue(semester.isCurrentSemester());
    }

    @Test
    public void testUnsetCurrentSemester() {
        Semester semester = semesterDao.getSemesterBySemesterNameAndYear(SemesterName.HERBSTSEMESTER, 2098);
        assertEquals(semester8, semester);
        assertTrue(semester.isCurrentSemester());

        semesterDao.unsetCurrentSemester();
        assertFalse(semester.isCurrentSemester());
        dbInstance.flush();

        semester = semesterDao.getSemesterBySemesterNameAndYear(SemesterName.HERBSTSEMESTER, 2098);
        assertFalse(semester.isCurrentSemester());
    }
    
    private void insertTestData() {
        // Insert some semesters
        semester1 = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2095, false);
        semester2 = new Semester(SemesterName.HERBSTSEMESTER, 2095, false);
        semester3 = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2096, false);
        semester4 = new Semester(SemesterName.HERBSTSEMESTER, 2096, false);
        semester5 = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2097, false);
        semester6 = new Semester(SemesterName.HERBSTSEMESTER, 2097, false);
        semester7 = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2098, false);
        semester8 = new Semester(SemesterName.HERBSTSEMESTER, 2098, true);
        semester9 = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2099, false);
        semester10 = new Semester(SemesterName.HERBSTSEMESTER, 2099, false);

        // Save in random order
        semesterDao.save(semester9);
        dbInstance.flush();
        semesterDao.save(semester3);
        dbInstance.flush();
        semesterDao.save(semester6);
        dbInstance.flush();
        semesterDao.save(semester8);
        dbInstance.flush();
        semesterDao.save(semester5);
        dbInstance.flush();
        semesterDao.save(semester10);
        dbInstance.flush();
        semesterDao.save(semester7);
        dbInstance.flush();
        semesterDao.save(semester1);
        dbInstance.flush();
        semesterDao.save(semester2);
        dbInstance.flush();
        semesterDao.save(semester4);
        dbInstance.flush();
    }
}