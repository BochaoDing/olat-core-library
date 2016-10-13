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
public class StudentCourseDaoTest extends OlatTestCase {

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    private DB dbInstance;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private SemesterDao semesterDao;

    @Autowired
    private StudentCourseDao studentCourseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    @Before
    public void setup() throws CampusCourseException {
        // Insert some students
        List<Student> students = mockDataGeneratorProvider.get().getStudents();
        studentDao.save(students);
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
    public void testSaveStudentCourse() {
        Student student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertEquals(0, student.getStudentCourses().size());
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(0, course.getStudentCourses().size());
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));

        // Add student to course
        StudentCourse studentCourse = new StudentCourse(student, course, new Date());
        studentCourseDao.save(studentCourse);

        // Check before flush
        assertEquals(1, student.getStudentCourses().size());
        assertEquals(1, course.getStudentCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        student = studentDao.getStudentById(2100L);
        assertEquals(1, student.getStudentCourses().size());
        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getStudentCourses().size());
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
    }

    @Test
    public void testSaveStudentCourse_NotExistingCourse() {
        StudentIdCourseIdDateOfImport studentIdCourseIdDateOfImport = new StudentIdCourseIdDateOfImport();
        studentIdCourseIdDateOfImport.setStudentId(2100L);
        studentIdCourseIdDateOfImport.setCourseId(999L);
        studentIdCourseIdDateOfImport.setDateOfImport(new Date());

        try {
            studentCourseDao.save(studentIdCourseIdDateOfImport);
            fail("Expected exception has not occurred.");
        } catch(EntityNotFoundException e) {
            // All good, that's exactly what we expect
        } catch(Exception e) {
            fail("Unexpected exception has occurred: " + e.getMessage());
        }

        dbInstance.flush();
        dbInstance.clear();

        assertNull(studentCourseDao.getStudentCourseById(2100L, 999L));
    }

    @Test
    public void testSaveStudentCourse_NotExistingStudent() {
        StudentIdCourseIdDateOfImport studentIdCourseIdDateOfImport = new StudentIdCourseIdDateOfImport();
        studentIdCourseIdDateOfImport.setStudentId(999L);
        studentIdCourseIdDateOfImport.setCourseId(100L);
        studentIdCourseIdDateOfImport.setDateOfImport(new Date());

        try {
            studentCourseDao.save(studentIdCourseIdDateOfImport);
            fail("Expected exception has not occurred.");
        } catch(EntityNotFoundException e) {
            // All good, that's exactly what we expect
        } catch(Exception e) {
            fail("Unexpected exception has occurred: " + e.getMessage());
        }

        dbInstance.flush();
        dbInstance.clear();

        assertNull(studentCourseDao.getStudentCourseById(999L, 100L));
    }

    @Test
    public void testSaveStudentCourses() {
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNull(studentCourseDao.getStudentCourseById(2200L, 100L));

        insertStudentIdCourseIds();
        dbInstance.clear();

        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNotNull(studentCourseDao.getStudentCourseById(2200L, 100L));
    }

    @Test
    public void testSaveOrUpdateStudentCourse() {
        Student student = studentDao.getStudentById(2100L);
        Course course = courseDao.getCourseById(100L);
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));

        // Insert student to course
        StudentCourse studentCourse = new StudentCourse(student, course, new Date());
        studentCourseDao.saveOrUpdate(studentCourse);

        // Check before flush
        assertEquals(1, student.getStudentCourses().size());
        assertEquals(1, course.getStudentCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        student = studentDao.getStudentById(2100L);
        assertEquals(1, student.getStudentCourses().size());
        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getStudentCourses().size());
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));

        // Insert the same student a second time to the same course
        StudentCourse studentCourse2 = new StudentCourse(student, course, new Date());
        studentCourseDao.saveOrUpdate(studentCourse2);

        dbInstance.flush();
        dbInstance.clear();
    }

    @Test
    public void testSaveOrUpdateStudentCourseWithoutBidirectionalUpdate() {
        Student student = studentDao.getStudentById(2100L);
        Course course = courseDao.getCourseById(100L);
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));

        // Insert student to course
        StudentCourse studentCourse = new StudentCourse(student, course, new Date());
        studentCourseDao.saveOrUpdateWithoutBidirectionalUpdate(studentCourse);

        dbInstance.flush();
        dbInstance.clear();

        student = studentDao.getStudentById(2100L);
        assertEquals(1, student.getStudentCourses().size());
        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getStudentCourses().size());
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));

        // Insert the same student a second time to the same course
        StudentCourse studentCourse2 = new StudentCourse(student, course, new Date());
        studentCourseDao.saveOrUpdateWithoutBidirectionalUpdate(studentCourse2);

        dbInstance.flush();
        dbInstance.clear();
    }

    @Test
    public void testGetStudentCourseById_Null() {
        insertStudentIdCourseIds();
        assertNull(studentCourseDao.getStudentCourseById(999L, 999L));
    }

    @Test
    public void testGetStudentCourseById_NotNull() {
        insertStudentIdCourseIds();
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
    }

    @Test
    public void testGetAllNotUpdatedSCBookingOfCurrentSemester() {
        Student student = studentDao.getStudentById(2100L);
        Course course1CurrentSemester = courseDao.getCourseById(100L);
        Course course2CurrentSemester = courseDao.getCourseById(200L);
        Course course3FormerSemester = courseDao.getCourseById(400L);
        assertNull(studentCourseDao.getStudentCourseById(1100L, 100L));
        assertNull(studentCourseDao.getStudentCourseById(1100L, 200L));
        assertNull(studentCourseDao.getStudentCourseById(1100L, 400L));

        Date referenceDateOfImport = new Date();

        // Insert student to course of current semester with date of import in the past (-> should be returned by method)
        StudentCourse studentCourse1 = new StudentCourse(student, course1CurrentSemester, DateUtil.addHoursToDate(referenceDateOfImport, -1));
        studentCourseDao.saveOrUpdate(studentCourse1);

        // Insert student to course of current semester with date of import in the future (-> should not be returned by method)
        StudentCourse studentCourse2 = new StudentCourse(student, course2CurrentSemester, DateUtil.addHoursToDate(referenceDateOfImport, 1));
        studentCourseDao.saveOrUpdate(studentCourse2);

        // Insert student to course from former semester with date of importin the past (-> should not be returned by method)
        StudentCourse studentCourse3 = new StudentCourse(student, course3FormerSemester, DateUtil.addHoursToDate(referenceDateOfImport, -1));
        studentCourseDao.saveOrUpdate(studentCourse3);

        dbInstance.flush();

        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 200L));
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 400L));

        List<StudentIdCourseId> studentIdCourseIds = studentCourseDao.getAllNotUpdatedSCBookingOfCurrentSemester(referenceDateOfImport);

        dbInstance.flush();

        assertEquals(1, studentIdCourseIds.size());
        StudentIdCourseId studentIdCourseIdExpected = new StudentIdCourseId(2100L, 100L);
        assertTrue(studentIdCourseIds.contains(studentIdCourseIdExpected));
    }

    @Test
    public void testDeleteStudentCourse() {
        // Insert student to course
        Student student = studentDao.getStudentById(2100L);
        Course course = courseDao.getCourseById(100L);
        StudentCourse studentCourse = new StudentCourse(student, course, new Date());
        studentCourseDao.save(studentCourse);

        dbInstance.flush();

        assertEquals(1, student.getStudentCourses().size());
        assertEquals(1, course.getStudentCourses().size());

        // Delete
        studentCourseDao.delete(studentCourse);

        // Check before flush
        assertEquals(0, student.getStudentCourses().size());
        assertEquals(0, course.getStudentCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        student = studentDao.getStudentById(2100L);
        assertEquals(0, student.getStudentCourses().size());
        course = courseDao.getCourseById(100L);
        assertEquals(0, course.getStudentCourses().size());
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
    }

    @Test
    public void testDeleteAllSCBookingTooFarInThePastAsBulkDelete() {
        Student student = studentDao.getStudentById(2100L);
        Course course1 = courseDao.getCourseById(100L);
        Course course2 = courseDao.getCourseById(200L);
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNull(studentCourseDao.getStudentCourseById(2100L, 200L));

        Date referenceDateOfImport = new Date();

        // Insert student to course with date too far in the past (-> should be deleted)
        StudentCourse studentCourse1 = new StudentCourse(student, course1, DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1));
        studentCourseDao.saveOrUpdate(studentCourse1);

        // Insert student to course with date not too far in the past (-> should not be deleted)
        StudentCourse studentCourse2 = new StudentCourse(student, course2, DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() + 1));
        studentCourseDao.saveOrUpdate(studentCourse2);

        dbInstance.flush();

        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 200L));

        studentCourseDao.deleteAllSCBookingTooFarInThePastAsBulkDelete(new Date());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 200L));
    }

    @Test
    public void testDeleteByStudentIdsAsBulkDelete() {
        insertStudentIdCourseIds();

        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNotNull(studentCourseDao.getStudentCourseById(2200L, 100L));

        List<Long> studentIds = new LinkedList<>();
        studentIds.add(2100L);
        studentIds.add(2200L);

        studentCourseDao.deleteByStudentIdsAsBulkDelete(studentIds);

        dbInstance.flush();
        dbInstance.clear();

        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNull(studentCourseDao.getStudentCourseById(2200L, 100L));
    }

    @Test
    public void testDeleteByCourseIdsAsBulkDelete() {
        insertStudentIdCourseIds();

        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 200L));

        List<Long> courseIds = new LinkedList<>();
        courseIds.add(100L);
        courseIds.add(200L);

        studentCourseDao.deleteByCourseIdsAsBulkDelete(courseIds);

        dbInstance.flush();
        dbInstance.clear();

        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNull(studentCourseDao.getStudentCourseById(2100L, 200L));
    }

    @Test
    public void testDeleteByStudentIdCourseIdsAsBulkDelete() {
        insertStudentIdCourseIds();

        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 200L));

        List<StudentIdCourseId> studentIdCourseIds = new LinkedList<>();
        studentIdCourseIds.add(new StudentIdCourseId(2100L, 100L));
        studentIdCourseIds.add(new StudentIdCourseId(2100L, 200L));

        int numberOfDeletedEntities = studentCourseDao.deleteByStudentIdCourseIdsAsBulkDelete(studentIdCourseIds);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(2, numberOfDeletedEntities);
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        assertNull(studentCourseDao.getStudentCourseById(2100L, 200L));
    }

    private void insertStudentIdCourseIds() {
        List<StudentIdCourseIdDateOfImport> studentIdCourseIdDateOfImports = mockDataGeneratorProvider.get().getStudentIdCourseIdDateOfImports();
        studentCourseDao.save(studentIdCourseIdDateOfImports);
        dbInstance.flush();
    }

}
