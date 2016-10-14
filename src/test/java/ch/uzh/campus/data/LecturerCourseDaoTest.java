package ch.uzh.campus.data;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseException;
import ch.uzh.campus.utils.DateUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;
import javax.persistence.EntityNotFoundException;
import java.util.*;

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
    private CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    private DB dbInstance;

    @Autowired
    private LecturerDao lecturerDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private SemesterDao semesterDao;

    @Autowired
    private LecturerCourseDao lecturerCourseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    @Before
    public void setup() throws CampusCourseException {
        // Insert some lecturers
        List<Lecturer> lecturers = mockDataGeneratorProvider.get().getLecturers();
        lecturerDao.save(lecturers);
        dbInstance.flush();

        // Insert some courses
        List<CourseSemesterOrgId> courseSemesterOrgIds = mockDataGeneratorProvider.get().getCourseSemesterOrgIds();
        courseDao.save(courseSemesterOrgIds);
        dbInstance.flush();

        // Set current semester
        Course course = courseDao.getCourseById(100L);
        semesterDao.setCurrentSemester(course.getSemester().getId());
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
    public void testSaveLecturerCourse_NotExistingCourse() {
        LecturerIdCourseIdDateOfImport lecturerIdCourseIdDateOfImport = new LecturerIdCourseIdDateOfImport(1100L, 999L, new Date());
        try {
            lecturerCourseDao.save(lecturerIdCourseIdDateOfImport);
            fail("Expected exception has not occurred.");
        } catch(EntityNotFoundException e) {
            // All good, that's exactly what we expect
        } catch(Exception e) {
            fail("Unexpected exception has occurred: " + e.getMessage());
        }

        dbInstance.flush();
        dbInstance.clear();

        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 999L));
    }

    @Test
    public void testSaveLecturerCourse_NotExistingLecturer() {
        LecturerIdCourseIdDateOfImport lecturerIdCourseIdDateOfImport = new LecturerIdCourseIdDateOfImport(999L, 100L, new Date());
        try {
            lecturerCourseDao.save(lecturerIdCourseIdDateOfImport);
            fail("Expected exception has not occurred.");
        } catch(EntityNotFoundException e) {
            // All good, that's exactly what we expect
        } catch(Exception e) {
            fail("Unexpected exception has occurred: " + e.getMessage());
        }

        dbInstance.flush();
        dbInstance.clear();

        assertNull(lecturerCourseDao.getLecturerCourseById(999L, 100L));
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

        // Insert the same lecturer a second time to the same course
        LecturerCourse lecturerCourse2 = new LecturerCourse(lecturer, course, new Date());
        lecturerCourseDao.saveOrUpdate(lecturerCourse2);

        dbInstance.flush();
        dbInstance.clear();
    }

    @Test
    public void testSaveOrUpdateLecturerCourseWithoutBidirctionalUpdate() {
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        Course course = courseDao.getCourseById(100L);
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));

        // Insert lecturer to course
        LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, new Date());
        lecturerCourseDao.saveOrUpdateWithoutBidirectionalUpdate(lecturerCourse);

		// Check before flush
		assertTrue(lecturer.getLecturerCourses().isEmpty());
		assertTrue(course.getLecturerCourses().isEmpty());

        dbInstance.flush();
        dbInstance.clear();

        lecturer = lecturerDao.getLecturerById(1100L);
        assertEquals(1, lecturer.getLecturerCourses().size());
        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getLecturerCourses().size());
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));

        // Insert the same lecturer a second time to the same course
        LecturerCourse lecturerCourse2 = new LecturerCourse(lecturer, course, new Date());
        lecturerCourseDao.saveOrUpdateWithoutBidirectionalUpdate(lecturerCourse2);

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
    public void testGetAllNotUpdatedLCBookingOfCurrentSemester() {
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        Course course1CurrentSemester = courseDao.getCourseById(100L);
        Course course2CurrentSemester = courseDao.getCourseById(200L);
        Course course3FormerSemester = courseDao.getCourseById(400L);
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 400L));

        Date referenceDateOfImport = new Date();

        // Insert lecturer to course of current semester with date of import in the past (-> should be returned by method)
        LecturerCourse lecturerCourse1 = new LecturerCourse(lecturer, course1CurrentSemester, DateUtil.addHoursToDate(referenceDateOfImport, -1));
        lecturerCourseDao.saveOrUpdate(lecturerCourse1);

        // Insert lecturer to course of current semester with date of import in the future (-> should not be returned by method)
        LecturerCourse lecturerCourse2 = new LecturerCourse(lecturer, course2CurrentSemester, DateUtil.addHoursToDate(referenceDateOfImport, 1));
        lecturerCourseDao.saveOrUpdate(lecturerCourse2);

        // Insert lecturer to course from former semester with date of import in the past (-> should not be returned by method)
        LecturerCourse lecturerCourse3 = new LecturerCourse(lecturer, course3FormerSemester, DateUtil.addHoursToDate(referenceDateOfImport, -1));
        lecturerCourseDao.saveOrUpdate(lecturerCourse3);

        dbInstance.flush();

        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 400L));

        List<LecturerIdCourseId> lecturerIdCourseIds = lecturerCourseDao.getAllNotUpdatedLCBookingOfCurrentSemester(referenceDateOfImport);

        dbInstance.flush();

        assertEquals(1, lecturerIdCourseIds.size());
        LecturerIdCourseId lecturerIdCourseIdExpected = new LecturerIdCourseId(1100L, 100L);
        assertTrue(lecturerIdCourseIds.contains(lecturerIdCourseIdExpected));
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
    public void testDeleteAllLCBookingTooFarInThePastAsBulkDelete() {
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        Course course1 = courseDao.getCourseById(100L);
        Course course2 = courseDao.getCourseById(200L);
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));

        Date referenceDateOfImport = new Date();

        // Insert lecturer to course with date too far in the past (-> should be deleted)
        LecturerCourse lecturerCourse1 = new LecturerCourse(lecturer, course1, DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1));
        lecturerCourseDao.saveOrUpdate(lecturerCourse1);

        // Insert lecturer to course with date not too far in the past (-> should not be deleted)
        LecturerCourse lecturerCourse2 = new LecturerCourse(lecturer, course2, DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() + 1));
        lecturerCourseDao.saveOrUpdate(lecturerCourse2);

        dbInstance.flush();

        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));

        lecturerCourseDao.deleteAllLCBookingTooFarInThePastAsBulkDelete(new Date());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
    }

    @Test
    public void testDeleteByLecturerIdsAsBulkDelete() {
        insertLecturerIdCourseIds();

        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1200L, 200L));

        List<Long> lecturerIds = new LinkedList<>();
        lecturerIds.add(1100L);
        lecturerIds.add(1200L);

        lecturerCourseDao.deleteByLecturerIdsAsBulkDelete(lecturerIds);

        dbInstance.flush();
        dbInstance.clear();

        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNull(lecturerCourseDao.getLecturerCourseById(1200L, 200L));
    }

    @Test
    public void testDeleteByCourseIdsAsBulkDelete() {
        insertLecturerIdCourseIds();

        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));

        List<Long> courseIds = new LinkedList<>();
        courseIds.add(100L);
        courseIds.add(200L);

        lecturerCourseDao.deleteByCourseIdsAsBulkDelete(courseIds);

        dbInstance.flush();
        dbInstance.clear();

        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
    }

    @Test
    public void testDeleteByLecturerIdCourseIdsAsBulkDelete() {
        insertLecturerIdCourseIds();

        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));

        List<LecturerIdCourseId> lecturerIdCourseIds = new LinkedList<>();
        lecturerIdCourseIds.add(new LecturerIdCourseId(1100L, 100L));
        lecturerIdCourseIds.add(new LecturerIdCourseId(1100L, 200L));

        int numberOfDeletedEntities = lecturerCourseDao.deleteByLecturerIdCourseIdsAsBulkDelete(lecturerIdCourseIds);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(2, numberOfDeletedEntities);
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
    }

    private void insertLecturerIdCourseIds() {
        List<LecturerIdCourseIdDateOfImport> lecturerIdCourseIdDateOfImports = mockDataGeneratorProvider.get().getLecturerIdCourseIdDateOfImports();
        lecturerCourseDao.save(lecturerIdCourseIdDateOfImports);
        dbInstance.flush();
    }
}
