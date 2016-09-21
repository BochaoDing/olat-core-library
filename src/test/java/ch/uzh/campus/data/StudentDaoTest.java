package ch.uzh.campus.data;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.utils.DateUtil;
import org.junit.After;
import org.junit.Test;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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

@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml" })
public class StudentDaoTest extends OlatTestCase {

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    private DB dbInstance;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private OrgDao orgDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private StudentCourseDao studentCourseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    @Autowired
    private UserManager userManager;

    private List<Student> students;
    
    @After
    public void after() {
    	dbInstance.rollback();
    }

    @Test
    public void testAddMapping() {
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
    public void testRemoveMapping() {
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
    public void testGetAllNotManuallyMappedOrTooOldOrphanedStudents() throws InterruptedException {
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
    public void testGetStudentsMappedToOlatUserName() {
        insertTestData();

        Student student1 = studentDao.getStudentById(2100L);
        Student student2 = studentDao.getStudentById(2200L);
        assertNotNull(student1);
        assertNotNull(student2);

        String olatUserName = "studentDaoTestUser";
        Identity identity = insertTestUser(olatUserName);

        assertTrue(studentDao.getStudentsMappedToOlatUserName(olatUserName).isEmpty());

        // Map to student 1
        studentDao.addMapping(student1.getId(), identity);
        dbInstance.flush();

        List<Student> studentsFound = studentDao.getStudentsMappedToOlatUserName(olatUserName);
        assertEquals(1, studentsFound.size());
        assertTrue(studentsFound.contains(student1));

        // Also map to student 2
        studentDao.addMapping(student2.getId(), identity);
        dbInstance.flush();

        studentsFound = studentDao.getStudentsMappedToOlatUserName(olatUserName);
        assertEquals(2, studentsFound.size());
        assertTrue(studentsFound.contains(student1));
        assertTrue(studentsFound.contains(student2));

        // Remove mapping of student 1
        studentDao.removeMapping(student1.getId());
        dbInstance.flush();

        studentsFound = studentDao.getStudentsMappedToOlatUserName(olatUserName);
        assertEquals(1, studentsFound.size());
        assertTrue(studentsFound.contains(student2));

        // Remove also mapping of student 2
        studentDao.removeMapping(student2.getId());
        dbInstance.flush();

        assertTrue(studentDao.getStudentsMappedToOlatUserName(olatUserName).isEmpty());
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
    public void testGetAllStudentsWithCreatedOrNotCreatedCreatableCourses() {
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

    private void insertTestData() {
        // Insert some orgs
        List<Org> orgs = mockDataGeneratorProvider.get().getOrgs();
        orgDao.save(orgs);
        dbInstance.flush();

        // Insert some students
        students = mockDataGeneratorProvider.get().getStudents();
        studentDao.save(students);
        dbInstance.flush();

        // Insert some courses
        List<CourseOrgId> courseOrgIds = mockDataGeneratorProvider.get().getCourseOrgIds();
        courseDao.save(courseOrgIds);
        dbInstance.flush();

        // Insert some studentIdCourseIds
        List<StudentIdCourseIdDateOfImport> studentIdCourseIdDateOfImports = mockDataGeneratorProvider.get().getStudentIdCourseIdDateOfImports();
        studentCourseDao.save(studentIdCourseIdDateOfImports);
        dbInstance.flush();
    }

    private Identity insertTestUser(String userName) {
        User user = userManager.createUser("studentDaoFirstName" + userName, "studentDaoLastName" + userName, userName + "@uzh.ch");
        dbInstance.saveObject(user);
        Identity identity = new IdentityImpl(userName, user);
        dbInstance.saveObject(identity);
        dbInstance.flush();
        return identity;
    }
}
