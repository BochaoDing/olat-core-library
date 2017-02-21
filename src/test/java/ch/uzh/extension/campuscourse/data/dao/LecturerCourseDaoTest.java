package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import ch.uzh.extension.campuscourse.CampusCourseTestDataGenerator;
import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.data.entity.Course;
import ch.uzh.extension.campuscourse.data.entity.Lecturer;
import ch.uzh.extension.campuscourse.data.entity.LecturerCourse;
import ch.uzh.extension.campuscourse.model.CourseSemesterOrgId;
import ch.uzh.extension.campuscourse.model.LecturerIdCourseId;
import ch.uzh.extension.campuscourse.model.LecturerIdCourseIdDateOfLatestImport;
import ch.uzh.extension.campuscourse.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

@Component
public class LecturerCourseDaoTest extends CampusCourseTestCase {

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    private LecturerDao lecturerDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private SemesterDao semesterDao;

    @Autowired
    private LecturerCourseDao lecturerCourseDao;

    @Autowired
    private CampusCourseTestDataGenerator campusCourseTestDataGenerator;

    @Before
    public void setup() throws CampusCourseException {
        // Insert some lecturers
        List<Lecturer> lecturers = campusCourseTestDataGenerator.createLecturers();
        lecturerDao.save(lecturers);
        dbInstance.flush();

        // Insert some courses
        List<CourseSemesterOrgId> courseSemesterOrgIds = campusCourseTestDataGenerator.createCourseSemesterOrgIds();
        courseDao.save(courseSemesterOrgIds);
        dbInstance.flush();

        // Set current semester
        Course course = courseDao.getCourseById(100L);
        semesterDao.setCurrentSemester(course.getSemester().getId());
        dbInstance.flush();
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
        LecturerIdCourseIdDateOfLatestImport lecturerIdCourseIdDateOfLatestImport = new LecturerIdCourseIdDateOfLatestImport(1100L, 999L, new Date());
        try {
            lecturerCourseDao.save(lecturerIdCourseIdDateOfLatestImport);
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
        LecturerIdCourseIdDateOfLatestImport lecturerIdCourseIdDateOfLatestImport = new LecturerIdCourseIdDateOfLatestImport(999L, 100L, new Date());
        try {
            lecturerCourseDao.save(lecturerIdCourseIdDateOfLatestImport);
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
    public void testSaveOrUpdateLecturerCourseWithoutBidirectionalUpdate() {
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        Course course = courseDao.getCourseById(100L);
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));

        // Insert lecturer to course
        LecturerIdCourseIdDateOfLatestImport lecturerIdCourseIdDateOfLatestImport = new LecturerIdCourseIdDateOfLatestImport(lecturer.getPersonalNr(), course.getId(), new Date());
        lecturerCourseDao.saveOrUpdateWithoutBidirectionalUpdate(lecturerIdCourseIdDateOfLatestImport);

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
		LecturerIdCourseIdDateOfLatestImport lecturerIdCourseIdDateOfLatestImport2 = new LecturerIdCourseIdDateOfLatestImport(lecturer.getPersonalNr(), course.getId(), new Date());
        lecturerCourseDao.saveOrUpdateWithoutBidirectionalUpdate(lecturerIdCourseIdDateOfLatestImport2);

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
    public void testGetAllNotUpdatedLCBookingOfCurrentImportProcess() {
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        Course course1CurrentSemester = courseDao.getCourseById(100L);
        Course course2CurrentSemester = courseDao.getCourseById(200L);
        Course course3FormerSemester = courseDao.getCourseById(400L);
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 400L));

        Date referenceDateOfImport = new Date();

        // Insert lecturer to course of current semester with date of import in the past (-> should be returned by method)
        Date dateOfImport1 = DateUtil.addHoursToDate(referenceDateOfImport, -1);
        LecturerCourse lecturerCourse1 = new LecturerCourse(lecturer, course1CurrentSemester, dateOfImport1);
        lecturerCourseDao.saveOrUpdate(lecturerCourse1);

        // Insert lecturer to course of current semester with date of import in the future (-> should not be returned by method)
		Date dateOfImport2 = DateUtil.addHoursToDate(referenceDateOfImport, 1);
        LecturerCourse lecturerCourse2 = new LecturerCourse(lecturer, course2CurrentSemester, dateOfImport2);
        lecturerCourseDao.saveOrUpdate(lecturerCourse2);

        // Insert lecturer to course from former semester with date of import in the past (-> should not be returned by method)
		Date dateOfImport3 = DateUtil.addHoursToDate(referenceDateOfImport, -1);
        LecturerCourse lecturerCourse3 = new LecturerCourse(lecturer, course3FormerSemester, dateOfImport3);
        lecturerCourseDao.saveOrUpdate(lecturerCourse3);

        dbInstance.flush();

        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 400L));

        List<LecturerIdCourseId> lecturerIdCourseIds = lecturerCourseDao.getAllNotUpdatedLCBookingOfCurrentImportProcess(referenceDateOfImport, course1CurrentSemester.getSemester());

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
    public void testDeleteAllLCBookingOfNotContinuedCoursesTooFarInThePastAsBulkDelete() {
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        Course course1 = courseDao.getCourseById(100L);
        Course course2 = courseDao.getCourseById(200L);
        Course course3 = courseDao.getCourseById(300L);
        Course course4 = courseDao.getCourseById(400L);
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
		assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 300L));
		assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 400L));

		// Make course 400 to be the parent course of 300
		// -> course 3 has a parent course, course 4 has a child course
		courseDao.saveParentCourseIdAndDateOfOlatCourseCreation(300L, 400L);

        Date referenceDateOfImport = new GregorianCalendar(1800, Calendar.JANUARY, 1).getTime();

        // Insert lecturer to course with date too far in the past (-> should be deleted)
		Date dateOfImport1 = DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1);
		course1.setDateOfLatestImport(dateOfImport1);
		LecturerCourse lecturerCourse1 = new LecturerCourse(lecturer, course1, dateOfImport1);
        lecturerCourseDao.saveOrUpdate(lecturerCourse1);

        // Insert lecturer to course with date not too far in the past (-> should not be deleted)
		Date dateOfImport2 = DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() + 1);
		course2.setDateOfLatestImport(dateOfImport2);
        LecturerCourse lecturerCourse2 = new LecturerCourse(lecturer, course2, dateOfImport2);
        lecturerCourseDao.saveOrUpdate(lecturerCourse2);

		// Insert lecturer to course with date too far in the past (-> should be deleted)
		Date dateOfImport3 = DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1);
		course3.setDateOfLatestImport(dateOfImport3);
		LecturerCourse lecturerCourse3 = new LecturerCourse(lecturer, course3, dateOfImport3);
		lecturerCourseDao.saveOrUpdate(lecturerCourse3);

		// Insert lecturer to course with date too far in the past (-> should not be deleted, because course has a child course)
		Date dateOfImport4 = DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1);
		course4.setDateOfLatestImport(dateOfImport4);
		LecturerCourse lecturerCourse4 = new LecturerCourse(lecturer, course4, dateOfImport4);
		lecturerCourseDao.saveOrUpdate(lecturerCourse4);

        dbInstance.flush();

        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
		assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 300L));
		assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 400L));

        lecturerCourseDao.deleteAllLCBookingOfNotContinuedCoursesTooFarInThePastAsBulkDelete(referenceDateOfImport);

        dbInstance.flush();
        dbInstance.clear();

        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 200L));
		assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 300L));
		assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 400L));

		// Remove parent course of course 3 (-> course 4 doesn't have a child course any more and should also be deleted)
		course3 = courseDao.getCourseById(300L);
		course3.removeParentCourse();
		dbInstance.flush();

		lecturerCourseDao.deleteAllLCBookingOfNotContinuedCoursesTooFarInThePastAsBulkDelete(referenceDateOfImport);

		dbInstance.flush();
		dbInstance.clear();

		assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 400L));
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

        // Test with empty list
		assertEquals(0, lecturerCourseDao.deleteByLecturerIdsAsBulkDelete(new ArrayList<>()));
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

		// Test with empty list
		assertEquals(0, lecturerCourseDao.deleteByCourseIdsAsBulkDelete(new ArrayList<>()));
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

		// Test with empty list
		assertEquals(0, lecturerCourseDao.deleteByLecturerIdCourseIdsAsBulkDelete(new ArrayList<>()));
    }

    private void insertLecturerIdCourseIds() {
        List<LecturerIdCourseIdDateOfLatestImport> lecturerIdCourseIdDateOfLatestImports = campusCourseTestDataGenerator.createLecturerIdCourseIdDateOfImports();
        lecturerCourseDao.save(lecturerIdCourseIdDateOfLatestImports);
        dbInstance.flush();
    }
}
