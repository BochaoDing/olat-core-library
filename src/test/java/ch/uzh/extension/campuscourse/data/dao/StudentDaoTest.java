package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import ch.uzh.extension.campuscourse.model.CourseSemesterOrgId;
import ch.uzh.extension.campuscourse.model.StudentIdCourseId;
import ch.uzh.extension.campuscourse.model.StudentIdCourseIdDateOfImport;
import ch.uzh.extension.campuscourse.data.MockDataGenerator;
import ch.uzh.extension.campuscourse.util.DateUtil;
import ch.uzh.extension.campuscourse.data.entity.Course;
import ch.uzh.extension.campuscourse.data.entity.Org;
import ch.uzh.extension.campuscourse.data.entity.Student;
import ch.uzh.extension.campuscourse.data.entity.StudentCourse;
import org.junit.Test;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Initial Date: Oct 28, 2014 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */

@Component
public class StudentDaoTest extends CampusCourseTestCase {

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private OrgDao orgDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private SemesterDao semesterDao;

    @Autowired
    private StudentCourseDao studentCourseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    @Autowired
    private UserManager userManager;

    @Autowired
    private RepositoryService repositoryService;

    private List<Student> students;

    @Test
    public void testAddMapping() throws CampusCourseException {
        insertTestData();
        Student student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertNull(student.getMappedIdentity());
        assertNull(student.getKindOfMapping());
        assertNull(student.getDateOfMapping());

        Identity identity = insertTestUser("studentDaoTestUser");

        studentDao.addMapping(student.getId(), identity);

        // Check before flush
        assertEquals(identity, student.getMappedIdentity());
        assertEquals("AUTO", student.getKindOfMapping());
        assertNotNull(student.getDateOfMapping());

        dbInstance.flush();
        dbInstance.clear();

        student = studentDao.getStudentById(2100L);
        assertEquals(identity, student.getMappedIdentity());
        assertEquals("AUTO", student.getKindOfMapping());
        assertNotNull(student.getDateOfMapping());
    }

    @Test
    public void testRemoveMapping() throws CampusCourseException {
        insertTestData();
        Student student = studentDao.getStudentById(2100L);
        assertNotNull(student);

        // Add mapping
        Identity identity = insertTestUser("studentDaoTestUser");
        studentDao.addMapping(student.getId(), identity);
        dbInstance.flush();
        dbInstance.clear();

        student = studentDao.getStudentById(2100L);
        assertNotNull(student.getMappedIdentity());
        assertNotNull(student.getKindOfMapping());
        assertNotNull(student.getDateOfMapping());

        // Remove mapping
        studentDao.removeMapping(student.getId());

        // Check before flush
        assertNull(student.getMappedIdentity());
        assertNull(student.getKindOfMapping());
        assertNull(student.getDateOfMapping());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(student.getMappedIdentity());
        assertNull(student.getKindOfMapping());
        assertNull(student.getDateOfMapping());
    }

    @Test
    public void testGetStudentById_Null() throws CampusCourseException {
        insertTestData();
        assertNull(studentDao.getStudentById(2999L));
    }

    @Test
    public void testGetStudentById_NotNull() throws CampusCourseException {
        insertTestData();
        assertNotNull(studentDao.getStudentById(2100L));
    }

    @Test
    public void testGetStudentByEmail_Null() throws CampusCourseException {
        insertTestData();
        assertNull(studentDao.getStudentByEmail("wrongEmail"));
    }

    @Test
    public void testlGetStudentByEmail_NotNull() throws CampusCourseException {
        insertTestData();
        assertNotNull(studentDao.getStudentByEmail("email1"));
    }

    @Test
    public void testGetStudentByRegistrationNr_Null() throws CampusCourseException {
        insertTestData();
        assertNull(studentDao.getStudentByEmail("999L"));
    }

    @Test
    public void testGetStudentByRegistrationNr_NotNull() throws CampusCourseException {
        insertTestData();
        assertNotNull(studentDao.getStudentByRegistrationNr("1000"));
    }

    @Test
    public void testGetAllNotManuallyMappedOrTooOldOrphanedStudents() throws InterruptedException, CampusCourseException {
        Date referenceDateOfImport = new Date();

        int numberOfStudentsFoundBeforeInsertingTestData = studentDao.getAllNotManuallyMappedOrTooOldOrphanedStudents(referenceDateOfImport).size();
        insertTestData();

        // Student 2300 has no courses. i.e. it is orphaned and not mapped (-> should be selected)
        Student student = studentDao.getStudentById(2300L);
        assertTrue(student.getStudentCourses().isEmpty());
        assertNull(student.getKindOfMapping());
        assertEquals(numberOfStudentsFoundBeforeInsertingTestData + 1, studentDao.getAllNotManuallyMappedOrTooOldOrphanedStudents(referenceDateOfImport).size());

        // Map student auto and set time of import not too far in the past (-> should be selected)
        student.setKindOfMapping("AUTO");
        student.setDateOfImport(DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() + 1));
        dbInstance.flush();
        assertEquals(numberOfStudentsFoundBeforeInsertingTestData + 1, studentDao.getAllNotManuallyMappedOrTooOldOrphanedStudents(referenceDateOfImport).size());

        // Map student auto and set time of import too far in the past (-> should be selected)
        student.setDateOfImport(DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1));
        dbInstance.flush();
        assertEquals(numberOfStudentsFoundBeforeInsertingTestData + 1, studentDao.getAllNotManuallyMappedOrTooOldOrphanedStudents(referenceDateOfImport).size());

        // Map student manually and set time of import not too far in the past (-> should not be selected)
        student.setKindOfMapping("MANUAL");
        student.setDateOfImport(DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() + 1));
        dbInstance.flush();
        assertEquals(numberOfStudentsFoundBeforeInsertingTestData, studentDao.getAllNotManuallyMappedOrTooOldOrphanedStudents(referenceDateOfImport).size());

        // Map student manually and set time of import too far in the past (-> should be selected)
        student.setDateOfImport(DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1));
        dbInstance.flush();
        assertEquals(numberOfStudentsFoundBeforeInsertingTestData + 1, studentDao.getAllNotManuallyMappedOrTooOldOrphanedStudents(referenceDateOfImport).size());

        student = studentDao.getStudentById(2100L);
        assertNull(student.getKindOfMapping());
        assertEquals(3, student.getStudentCourses().size());

        // Remove all courses of student, i.e. make it orphaned
        List<StudentIdCourseId> studentIdCourseIds = new LinkedList<>();
        studentIdCourseIds.add(new StudentIdCourseId(2100L, 100L));
        studentIdCourseIds.add(new StudentIdCourseId(2100L, 200L));
        studentIdCourseIds.add(new StudentIdCourseId(2100L, 300L));

        studentCourseDao.deleteByStudentIdCourseIdsAsBulkDelete(studentIdCourseIds);
        dbInstance.flush();

        List<Long> studentIdsFound = studentDao.getAllNotManuallyMappedOrTooOldOrphanedStudents(referenceDateOfImport);
        assertEquals(numberOfStudentsFoundBeforeInsertingTestData + 2, studentIdsFound.size());
        assertTrue(studentIdsFound.contains(2100L));
    }

    @Test
    public void testGetStudentsStudentsByMappedIdentityKey() throws CampusCourseException {
        insertTestData();

        Student student1 = studentDao.getStudentById(2100L);
        Student student2 = studentDao.getStudentById(2200L);
        assertNotNull(student1);
        assertNotNull(student2);

        Identity identity = insertTestUser("studentDaoTestUser");

        assertTrue(studentDao.getStudentsByMappedIdentityKey(identity.getKey()).isEmpty());

        // Map to student 1
        studentDao.addMapping(student1.getId(), identity);
        dbInstance.flush();

        List<Student> studentsFound = studentDao.getStudentsByMappedIdentityKey(identity.getKey());
        assertEquals(1, studentsFound.size());
        assertTrue(studentsFound.contains(student1));

        // Also map to student 2
        studentDao.addMapping(student2.getId(), identity);
        dbInstance.flush();

        studentsFound = studentDao.getStudentsByMappedIdentityKey(identity.getKey());
        assertEquals(2, studentsFound.size());
        assertTrue(studentsFound.contains(student1));
        assertTrue(studentsFound.contains(student2));

        // Remove mapping of student 1
        studentDao.removeMapping(student1.getId());
        dbInstance.flush();

        studentsFound = studentDao.getStudentsByMappedIdentityKey(identity.getKey());
        assertEquals(1, studentsFound.size());
        assertTrue(studentsFound.contains(student2));

        // Remove also mapping of student 2
        studentDao.removeMapping(student2.getId());
        dbInstance.flush();

        assertTrue(studentDao.getStudentsByMappedIdentityKey(identity.getKey()).isEmpty());
    }

    @Test
    public void testGetNumberOfStudentsOfSpecificCourse() throws CampusCourseException {
        insertTestData();

        Student student1 = studentDao.getStudentById(2100L);
        Student student2 = studentDao.getStudentById(2200L);
        assertNotNull(student1);
        assertNotNull(student2);

        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);

        assertEquals(2, studentDao.getNumberOfStudentsOfSpecificCourse(course.getId()));
    }

    @Test
    public void testGetNumberOfStudentsWithBookingForCourseAndParentCourse() throws CampusCourseException {
        insertTestData();

        Student student1 = studentDao.getStudentById(2100L);
        Student student2 = studentDao.getStudentById(2200L);
        assertNotNull(student1);
        assertNotNull(student2);

        Course course1 = courseDao.getCourseById(100L);
        Course course2 = courseDao.getCourseById(200L);
        Course course3 = courseDao.getCourseById(300L);

        // Check that student 1 is participant of both courses 1, 2 and 3
        assertNotNull(studentCourseDao.getStudentCourseById(student1.getId(), course1.getId()));
        assertNotNull(studentCourseDao.getStudentCourseById(student1.getId(), course2.getId()));
        assertNotNull(studentCourseDao.getStudentCourseById(student1.getId(), course3.getId()));

        // Check that student 2 is participant of course 1 and 2, but not of course 3
        assertNotNull(studentCourseDao.getStudentCourseById(student2.getId(), course1.getId()));
        assertNotNull(studentCourseDao.getStudentCourseById(student2.getId(), course2.getId()));
        assertNull(studentCourseDao.getStudentCourseById(student2.getId(), course3.getId()));

        // Course 1 is not parent of course 2, so we should not find anything
        assertEquals(0, studentDao.getNumberOfStudentsWithBookingForCourseAndParentCourse(course2.getId()));

        // Make course 1 to be the parent of course 2
        courseDao.saveParentCourseId(course2.getId(), course1.getId());
        dbInstance.flush();

        assertEquals(2, studentDao.getNumberOfStudentsWithBookingForCourseAndParentCourse(course2.getId()));

        // Course 2 is not parent of course 3, so we should not find anything
        assertEquals(0, studentDao.getNumberOfStudentsWithBookingForCourseAndParentCourse(course3.getId()));

        // Make course 2 to be the parent of course 3
        courseDao.saveParentCourseId(course3.getId(), course2.getId());
        dbInstance.flush();

        assertEquals(1, studentDao.getNumberOfStudentsWithBookingForCourseAndParentCourse(course3.getId()));
    }

    @Test
    public void testHasMoreThan50PercentOfStudentsOfSpecificCourseBothABookingOfCourseAndParentCourse() throws CampusCourseException {
        insertTestData();

        Student student1 = studentDao.getStudentById(2100L);
        Student student2 = studentDao.getStudentById(2200L);
        assertNotNull(student1);
        assertNotNull(student2);

        Course course1 = courseDao.getCourseById(100L);
        Course course2 = courseDao.getCourseById(200L);
        Course course3 = courseDao.getCourseById(300L);

        // Make course 1 to be the parent of course 2
        courseDao.saveParentCourseId(course2.getId(), course1.getId());
        dbInstance.flush();

        assertEquals(2, studentDao.getNumberOfStudentsWithBookingForCourseAndParentCourse(course2.getId()));
        assertEquals(2, studentDao.getNumberOfStudentsOfSpecificCourse(course2.getParentCourse().getId()));
        assertTrue(studentDao.hasMoreThan50PercentOfStudentsOfSpecificCourseBothABookingOfCourseAndParentCourse(course2));

        // Make course 1 to be the parent of course 3
        courseDao.saveParentCourseId(course3.getId(), course1.getId());
        dbInstance.flush();

        assertEquals(1, studentDao.getNumberOfStudentsWithBookingForCourseAndParentCourse(course3.getId()));
        assertEquals(2, studentDao.getNumberOfStudentsOfSpecificCourse(course3.getParentCourse().getId()));
        assertTrue(studentDao.hasMoreThan50PercentOfStudentsOfSpecificCourseBothABookingOfCourseAndParentCourse(course3));

        // Remove booking of course 3
        StudentCourse studentCourse = studentCourseDao.getStudentCourseById(student1.getId(), course3.getId());
        studentCourseDao.delete(studentCourse);
        dbInstance.flush();

        assertEquals(0, studentDao.getNumberOfStudentsWithBookingForCourseAndParentCourse(course3.getId()));
        assertEquals(2, studentDao.getNumberOfStudentsOfSpecificCourse(course3.getParentCourse().getId()));
        assertFalse(studentDao.hasMoreThan50PercentOfStudentsOfSpecificCourseBothABookingOfCourseAndParentCourse(course3));
    }

    @Test
    public void testDelete() throws CampusCourseException {
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
    public void testDeleteByStudentIds() throws CampusCourseException {
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
    public void testDeleteByStudentIdsAsBulkDelete() throws CampusCourseException {
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
    public void testGetAllStudentsWithCreatedOrNotCreatedCreatableCourses() throws CampusCourseException {
        int numberOfStudentsFoundBeforeInsertingTestData = studentDao.getAllStudentsWithCreatedOrNotCreatedCreatableCourses().size();
        insertTestData();
        List<Student> studentsFound = studentDao.getAllStudentsWithCreatedOrNotCreatedCreatableCourses();
        assertEquals(numberOfStudentsFoundBeforeInsertingTestData + 5, studentsFound.size());
        List<Long> idsFound = studentsFound.stream().map(Student::getId).collect(Collectors.toList());
        assertTrue(idsFound.contains(2100L));
        assertTrue(idsFound.contains(2200L));
        assertTrue(idsFound.contains(2400L));
        assertTrue(idsFound.contains(2500L));
        assertTrue(idsFound.contains(2600L));
    }

    private void insertTestData() throws CampusCourseException {
        // Insert some orgs
        List<Org> orgs = mockDataGeneratorProvider.get().getOrgs();
        orgDao.save(orgs);
        dbInstance.flush();

        // Insert some students
        students = mockDataGeneratorProvider.get().getStudents();
        studentDao.save(students);
        dbInstance.flush();

        // Insert some courses
        List<CourseSemesterOrgId> courseSemesterOrgIds = mockDataGeneratorProvider.get().getCourseSemesterOrgIds();
        courseDao.save(courseSemesterOrgIds);
        dbInstance.flush();

        // Insert some studentIdCourseIds
        List<StudentIdCourseIdDateOfImport> studentIdCourseIdDateOfImports = mockDataGeneratorProvider.get().getStudentIdCourseIdDateOfImports();
        studentCourseDao.save(studentIdCourseIdDateOfImports);
        dbInstance.flush();

        // Set current semester
        Course course = courseDao.getCourseById(100L);
        semesterDao.setCurrentSemester(course.getSemester().getId());
        dbInstance.flush();

        addOlatResourceToCourses_100_200_400_500_600();
    }

    private Identity insertTestUser(String userName) {
        User user = userManager.createUser("studentDaoFirstName" + userName, "studentDaoLastName" + userName, userName + "@uzh.ch");
        dbInstance.saveObject(user);
        Identity identity = new IdentityImpl(userName, user);
        dbInstance.saveObject(identity);
        dbInstance.flush();
        return identity;
    }

    private void addOlatResourceToCourses_100_200_400_500_600() {
        RepositoryEntry repositoryEntry1 = repositoryService.create("Rei Ayanami", "-", "Repository entry 1 StudentDaoTest", "", null);
        RepositoryEntry repositoryEntry2 = repositoryService.create("Rei Ayanami", "-", "Repository entry 2 StudentDaoTest", "", null);
        RepositoryEntry repositoryEntry4 = repositoryService.create("Rei Ayanami", "-", "Repository entry 4 StudentDaoTest", "", null);
        RepositoryEntry repositoryEntry5 = repositoryService.create("Rei Ayanami", "-", "Repository entry 5 StudentDaoTest", "", null);
        RepositoryEntry repositoryEntry6 = repositoryService.create("Rei Ayanami", "-", "Repository entry 6 StudentDaoTest", "", null);
        dbInstance.flush();
        Course course1 = courseDao.getCourseById(100L);
        Course course2 = courseDao.getCourseById(200L);
        Course course4 = courseDao.getCourseById(400L);
        Course course5 = courseDao.getCourseById(500L);
        Course course6 = courseDao.getCourseById(600L);
        course1.setRepositoryEntry(repositoryEntry1);
        course2.setRepositoryEntry(repositoryEntry2);
        course4.setRepositoryEntry(repositoryEntry4);
        course5.setRepositoryEntry(repositoryEntry5);
        course6.setRepositoryEntry(repositoryEntry6);
        dbInstance.flush();
    }
}
