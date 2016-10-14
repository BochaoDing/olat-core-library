package ch.uzh.campus.data;

import ch.uzh.campus.CampusCourseException;
import org.junit.After;
import org.junit.Before;
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
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;


/**
 *
 * @author Martin Schraner
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class DataConverterTest extends OlatTestCase {

    @Autowired
    private DataConverter dataConverter;

    @Autowired
    private DB dbInstance;

    @Autowired
    private OrgDao orgDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private LecturerDao lecturerDao;

    @Autowired
    private LecturerCourseDao lecturerCourseDao;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private StudentCourseDao studentCourseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    @Autowired
    private UserManager userManager;

    @Before
    public void setup() throws Exception {
        insertTestData();
    }

    @After
    public void after() {
        dbInstance.rollback();
    }

    @Test
    public void testConvertStudentsToIdentities() {

        // Select course from test data with participating students
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);

        Student student1 = studentDao.getStudentById(2100L);
        assertNotNull(student1);

        Student student2 = studentDao.getStudentById(2200L);
        assertNotNull(student2);

        // Check students of course
        List<Student> studentsOfCourse = course.getStudentCourses().stream().map(StudentCourse::getStudent).collect(Collectors.toList());
        assertEquals(2, studentsOfCourse.size());
        assertTrue(studentsOfCourse.contains(student1));
        assertTrue(studentsOfCourse.contains(student2));

        assertTrue(dataConverter.convertStudentsToIdentities(course.getStudentCourses()).isEmpty());

        // Add mapping for student 2
        String student1UserName = "dataConverterTestStudent1";
        Identity mappedIdentityOfStudent1 = insertTestUser(student1UserName, Identity.STATUS_ACTIV);
        studentDao.addMapping(student1.getId(), mappedIdentityOfStudent1);

        String student2UserName = "dataConverterTestStudent2";
        Identity mappedIdentityOfStudent2 = insertTestUser(student2UserName, Identity.STATUS_ACTIV);
        studentDao.addMapping(student2.getId(), mappedIdentityOfStudent2);
        dbInstance.flush();

        List<Identity> identitiesOfStudents = dataConverter.convertStudentsToIdentities(course.getStudentCourses());
        assertEquals(2, identitiesOfStudents.size());
        assertTrue(identitiesOfStudents.contains(mappedIdentityOfStudent1));
        assertTrue(identitiesOfStudents.contains(mappedIdentityOfStudent2));

        // Change status of mapped student1 to deleted
        mappedIdentityOfStudent1.setStatus(Identity.STATUS_DELETED);

        identitiesOfStudents = dataConverter.convertStudentsToIdentities(course.getStudentCourses());
        assertEquals(1, identitiesOfStudents.size());
        assertTrue(identitiesOfStudents.contains(mappedIdentityOfStudent2));

        // Remove mapping of student 2
        studentDao.removeMapping(student2.getId());

        assertTrue(dataConverter.convertStudentsToIdentities(course.getStudentCourses()).isEmpty());
    }
    
    @Test
    public void testConvertLecturersToIdentities() {

        // Select course from test data with associated lecturer
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);

        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);

        // Check lecturer of course
        List<Lecturer> lecturersOfCourse = course.getLecturerCourses().stream().map(LecturerCourse::getLecturer).collect(Collectors.toList());
        assertEquals(1, lecturersOfCourse.size());
        assertTrue(lecturersOfCourse.contains(lecturer));

        assertTrue(dataConverter.convertLecturersToIdentities(course.getLecturerCourses()).isEmpty());

        // Add mapping for lecturer
        String lecturerUserName = "dataConverterTestLecturer";
        Identity mappedIdentityOfLecturer = insertTestUser(lecturerUserName, Identity.STATUS_ACTIV);
        lecturerDao.addMapping(lecturer.getPersonalNr(), mappedIdentityOfLecturer);
        dbInstance.flush();

        List<Identity> identitiesOfLecturers = dataConverter.convertLecturersToIdentities(course.getLecturerCourses());
        assertEquals(1, identitiesOfLecturers.size());
        assertEquals(mappedIdentityOfLecturer, identitiesOfLecturers.get(0));

        // Create delegation (i.e. delegator + delegatee) with lecturer as delegator
        Identity identityOfDelegatee = insertTestUser("dataConverterTestDelegateeOfLecturer", Identity.STATUS_ACTIV);
        Delegation delegation = new Delegation(mappedIdentityOfLecturer, identityOfDelegatee, new Date());
        dbInstance.saveObject(delegation);
        dbInstance.flush();

        identitiesOfLecturers = dataConverter.convertLecturersToIdentities(course.getLecturerCourses());
        assertEquals(2, identitiesOfLecturers.size());
        assertTrue(identitiesOfLecturers.contains(mappedIdentityOfLecturer));
        assertTrue(identitiesOfLecturers.contains(identityOfDelegatee));

        // Change status of delegatee to deleted
        identityOfDelegatee.setStatus(Identity.STATUS_DELETED);

        identitiesOfLecturers = dataConverter.convertLecturersToIdentities(course.getLecturerCourses());
        assertEquals(1, identitiesOfLecturers.size());
        assertEquals(lecturerUserName, identitiesOfLecturers.get(0).getName());

        // Change status of mapped lecturer to deleted
        mappedIdentityOfLecturer.setStatus(Identity.STATUS_DELETED);

        assertTrue(dataConverter.convertLecturersToIdentities(course.getLecturerCourses()).isEmpty());

        // Remove mapping of lecturer
        lecturerDao.removeMapping(lecturer.getPersonalNr());

        assertTrue(dataConverter.convertLecturersToIdentities(course.getLecturerCourses()).isEmpty());
    }

    @Test
    public void testConvertDelegateesToIdentities() {

        // Select course from test data with associated lecturer
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);

        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);

        // Check lecturer of course
        List<Lecturer> lecturersOfCourse = course.getLecturerCourses().stream().map(LecturerCourse::getLecturer).collect(Collectors.toList());
        assertEquals(1, lecturersOfCourse.size());
        assertTrue(lecturersOfCourse.contains(lecturer));

        // Add mapping for lecturer
        String lecturerUserName = "dataConverterTestLecturer";
        Identity mappedIdentityOfLecturer = insertTestUser(lecturerUserName, Identity.STATUS_ACTIV);
        lecturerDao.addMapping(lecturer.getPersonalNr(), mappedIdentityOfLecturer);
        dbInstance.flush();

        assertTrue(dataConverter.convertDelegateesToIdentities(course.getLecturerCourses()).isEmpty());

        // Create delegation (i.e. delegator + delegatee) with lecturer as delegator
        Identity identityOfDelegatee = insertTestUser("dataConverterTestDelegateeOfLecturer", Identity.STATUS_ACTIV);
        Delegation delegation = new Delegation(mappedIdentityOfLecturer, identityOfDelegatee, new Date());
        dbInstance.saveObject(delegation);
        dbInstance.flush();

        List<Identity> identitiesOfLecturers = dataConverter.convertDelegateesToIdentities(course.getLecturerCourses());
        assertEquals(1, identitiesOfLecturers.size());
        assertTrue(identitiesOfLecturers.contains(identityOfDelegatee));

        // Change status of delegatee to deleted
        identityOfDelegatee.setStatus(Identity.STATUS_DELETED);

        assertTrue(dataConverter.convertDelegateesToIdentities(course.getLecturerCourses()).isEmpty());
    }

    @Test
    public void testGetDelegatees() {

        String delegatorUserName = "dataConverterTestDelegator";
        Identity identityOfDelegator = insertTestUser(delegatorUserName, Identity.STATUS_ACTIV);

        assertTrue(dataConverter.getDelegatees(identityOfDelegator).isEmpty());

        // Create two delegations for the same delegator
        Identity identityOfDelegatee1 = insertTestUser("dataConverterTestDelegatee1", Identity.STATUS_ACTIV);
        Delegation delegation1 = new Delegation(identityOfDelegator, identityOfDelegatee1, new Date());
        dbInstance.saveObject(delegation1);

        Identity identityOfDelegatee2 = insertTestUser("dataConverterTestDelegatee2", Identity.STATUS_ACTIV);
        Delegation delegation2 = new Delegation(identityOfDelegator, identityOfDelegatee2, new Date());
        dbInstance.saveObject(delegation2);
        dbInstance.flush();

        List<Object[]> delegateesAndModifiedDate = dataConverter.getDelegatees(identityOfDelegator);

        assertEquals(2, delegateesAndModifiedDate.size());

        List<Identity> identitiesOfDelegatees = delegateesAndModifiedDate.stream().map(delegateeAndModifiedDate -> (Identity) delegateeAndModifiedDate[0]).collect(Collectors.toList());

        assertTrue(identitiesOfDelegatees.contains(identityOfDelegatee1));
        assertTrue(identitiesOfDelegatees.contains(identityOfDelegatee2));
    }

    private void insertTestData() throws CampusCourseException {
        // Insert some orgs
        List<Org> orgs = mockDataGeneratorProvider.get().getOrgs();
        orgDao.save(orgs);
        dbInstance.flush();

        // Insert some courseOrgIds
        List<CourseSemesterOrgId> courseSemesterOrgIds = mockDataGeneratorProvider.get().getCourseSemesterOrgIds();
        courseDao.save(courseSemesterOrgIds);
        dbInstance.flush();

        // Insert some lecturers
        List<Lecturer> lecturers = mockDataGeneratorProvider.get().getLecturers();
        lecturerDao.save(lecturers);
        dbInstance.flush();

        // Add lecturers to courseOrgIds
        List<LecturerIdCourseIdDateOfImport> lecturerIdCourseIdDateOfImports = mockDataGeneratorProvider.get().getLecturerIdCourseIdDateOfImports();
        lecturerCourseDao.save(lecturerIdCourseIdDateOfImports);
        dbInstance.flush();

        // Insert some students
        List<Student> students = mockDataGeneratorProvider.get().getStudents();
        studentDao.save(students);
        dbInstance.flush();

        // Add students to courseOrgIds
        List<StudentIdCourseIdDateOfImport> studentIdCourseIdDateOfImports = mockDataGeneratorProvider.get().getStudentIdCourseIdDateOfImports();
        studentCourseDao.save(studentIdCourseIdDateOfImports);
        dbInstance.flush();
    }

    private Identity insertTestUser(String userName, Integer status) {
        User user = userManager.createUser("dataConverterTestFirstName" + userName, "dataConverterTestLastName" + userName, userName + "@uzh.ch");
        dbInstance.saveObject(user);
        Identity identity = new IdentityImpl(userName, user);
        identity.setStatus(status);
        dbInstance.saveObject(identity);
        dbInstance.flush();
        return identity;
    }
}

