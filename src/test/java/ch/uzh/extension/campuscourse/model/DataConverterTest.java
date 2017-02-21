package ch.uzh.extension.campuscourse.model;

import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import ch.uzh.extension.campuscourse.CampusCourseTestDataGenerator;
import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.data.dao.*;
import ch.uzh.extension.campuscourse.data.entity.*;
import ch.uzh.extension.campuscourse.service.dao.DataConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 *
 * @author Martin Schraner
 */
@Component
public class DataConverterTest extends CampusCourseTestCase {

    @Autowired
    private DataConverter dataConverter;

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
    private CampusCourseTestDataGenerator campusCourseTestDataGenerator;

    @Autowired
    private UserManager userManager;

    @Before
    public void setup() throws Exception {
        insertTestData();
    }

    @Test
    public void testConvertStudentsToIdentities() {

        // Select course from test data with participating students
        Course course = courseDao.getCourseById(100L);
        Assert.assertNotNull(course);

        Student student1 = studentDao.getStudentById(2100L);
        Assert.assertNotNull(student1);

        Student student2 = studentDao.getStudentById(2200L);
        Assert.assertNotNull(student2);

        // Check students of course
        List<Student> studentsOfCourse = course.getStudentCourses().stream().map(StudentCourse::getStudent).collect(Collectors.toList());
        Assert.assertEquals(2, studentsOfCourse.size());
        Assert.assertTrue(studentsOfCourse.contains(student1));
        Assert.assertTrue(studentsOfCourse.contains(student2));

        Assert.assertTrue(dataConverter.convertStudentsToIdentities(course.getStudentCourses()).isEmpty());

        // Add mapping for student 2
        String student1UserName = "dataConverterTestStudent1";
        Identity mappedIdentityOfStudent1 = insertTestUser(student1UserName, Identity.STATUS_ACTIV);
        studentDao.addMapping(student1.getId(), mappedIdentityOfStudent1);

        String student2UserName = "dataConverterTestStudent2";
        Identity mappedIdentityOfStudent2 = insertTestUser(student2UserName, Identity.STATUS_ACTIV);
        studentDao.addMapping(student2.getId(), mappedIdentityOfStudent2);
        dbInstance.flush();

        Set<Identity> identitiesOfStudents = dataConverter.convertStudentsToIdentities(course.getStudentCourses());
        Assert.assertEquals(2, identitiesOfStudents.size());
        Assert.assertTrue(identitiesOfStudents.contains(mappedIdentityOfStudent1));
        Assert.assertTrue(identitiesOfStudents.contains(mappedIdentityOfStudent2));

        // Change status of mapped student1 to deleted
        mappedIdentityOfStudent1.setStatus(Identity.STATUS_DELETED);

        identitiesOfStudents = dataConverter.convertStudentsToIdentities(course.getStudentCourses());
        Assert.assertEquals(1, identitiesOfStudents.size());
        Assert.assertTrue(identitiesOfStudents.contains(mappedIdentityOfStudent2));

        // Remove mapping of student 2
        studentDao.removeMapping(student2.getId());

        Assert.assertTrue(dataConverter.convertStudentsToIdentities(course.getStudentCourses()).isEmpty());
    }
    
    @Test
    public void testConvertLecturersToIdentities() {

        // Select course from test data with associated lecturer
        Course course = courseDao.getCourseById(100L);
        Assert.assertNotNull(course);

        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        Assert.assertNotNull(lecturer);

        // Check lecturer of course
        List<Lecturer> lecturersOfCourse = course.getLecturerCourses().stream().map(LecturerCourse::getLecturer).collect(Collectors.toList());
        Assert.assertEquals(1, lecturersOfCourse.size());
        Assert.assertTrue(lecturersOfCourse.contains(lecturer));

        Assert.assertTrue(dataConverter.convertLecturersToIdentities(course.getLecturerCourses()).isEmpty());

        // Add mapping for lecturer
        Identity mappedIdentityOfLecturer = insertTestUser("dataConverterTestLecturer", Identity.STATUS_ACTIV);
        lecturerDao.addMapping(lecturer.getPersonalNr(), mappedIdentityOfLecturer);
        dbInstance.flush();

        Set<Identity> identitiesOfLecturers = dataConverter.convertLecturersToIdentities(course.getLecturerCourses());
        Assert.assertEquals(1, identitiesOfLecturers.size());
        Assert.assertTrue(identitiesOfLecturers.contains(mappedIdentityOfLecturer));

        // Change status of mapped lecturer to deleted
        mappedIdentityOfLecturer.setStatus(Identity.STATUS_DELETED);

        Assert.assertTrue(dataConverter.convertLecturersToIdentities(course.getLecturerCourses()).isEmpty());

        // Remove mapping of lecturer
        lecturerDao.removeMapping(lecturer.getPersonalNr());

        Assert.assertTrue(dataConverter.convertLecturersToIdentities(course.getLecturerCourses()).isEmpty());
    }

    @Test
    public void testConvertDelegateesToIdentities() {

        // Select course from test data with associated lecturer
        Course course = courseDao.getCourseById(100L);
        Assert.assertNotNull(course);

        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        Assert.assertNotNull(lecturer);

        // Check lecturer of course
        List<Lecturer> lecturersOfCourse = course.getLecturerCourses().stream().map(LecturerCourse::getLecturer).collect(Collectors.toList());
        Assert.assertEquals(1, lecturersOfCourse.size());
        Assert.assertTrue(lecturersOfCourse.contains(lecturer));

        // Add mapping for lecturer
        String lecturerUserName = "dataConverterTestLecturer";
        Identity mappedIdentityOfLecturer = insertTestUser(lecturerUserName, Identity.STATUS_ACTIV);
        lecturerDao.addMapping(lecturer.getPersonalNr(), mappedIdentityOfLecturer);
        dbInstance.flush();

        Assert.assertTrue(dataConverter.convertDelegateesToIdentities(course.getLecturerCourses()).isEmpty());

        // Create delegation (i.e. delegator + delegatee) with lecturer as delegator
        Identity identityOfDelegatee = insertTestUser("dataConverterTestDelegateeOfLecturer", Identity.STATUS_ACTIV);
        Delegation delegation = new Delegation(mappedIdentityOfLecturer, identityOfDelegatee, new Date());
        dbInstance.saveObject(delegation);
        dbInstance.flush();

        Set<Identity> identitiesOfDelegatees = dataConverter.convertDelegateesToIdentities(course.getLecturerCourses());
        Assert.assertEquals(1, identitiesOfDelegatees.size());
        Assert.assertTrue(identitiesOfDelegatees.contains(identityOfDelegatee));

        // Change status of delegatee to deleted
        identityOfDelegatee.setStatus(Identity.STATUS_DELETED);

        Assert.assertTrue(dataConverter.convertDelegateesToIdentities(course.getLecturerCourses()).isEmpty());
    }

    @Test
    public void testGetDelegateesAndCreationDateByDelegator() {

        Identity identityOfDelegator = insertTestUser("dataConverterTestDelegator", Identity.STATUS_ACTIV);

        Assert.assertTrue(dataConverter.getDelegateesAndCreationDateByDelegator(identityOfDelegator).isEmpty());

        // Create two delegations for the same delegator
        Identity identityOfDelegatee1 = insertTestUser("dataConverterTestDelegatee1", Identity.STATUS_ACTIV);
        Delegation delegation1 = new Delegation(identityOfDelegator, identityOfDelegatee1, new Date());
        dbInstance.saveObject(delegation1);

        Identity identityOfDelegatee2 = insertTestUser("dataConverterTestDelegatee2", Identity.STATUS_ACTIV);
        Delegation delegation2 = new Delegation(identityOfDelegator, identityOfDelegatee2, new Date());
        dbInstance.saveObject(delegation2);
        dbInstance.flush();

        List<IdentityDate> delegateesAndCreationDate = dataConverter.getDelegateesAndCreationDateByDelegator(identityOfDelegator);

        Assert.assertEquals(2, delegateesAndCreationDate.size());

        List<Identity> identitiesOfDelegatees = delegateesAndCreationDate.stream().map(IdentityDate::getIdentity).collect(Collectors.toList());

        Assert.assertTrue(identitiesOfDelegatees.contains(identityOfDelegatee1));
        Assert.assertTrue(identitiesOfDelegatees.contains(identityOfDelegatee2));
    }

	@Test
	public void testGetDelegatorsAndCreationDateByDelegatee() {

		Identity identityOfDelegatee = insertTestUser("dataConverterTestDelegatee", Identity.STATUS_ACTIV);

		Assert.assertTrue(dataConverter.getDelegateesAndCreationDateByDelegator(identityOfDelegatee).isEmpty());

		// Create two delegations for the same delegatee
		Identity identityOfDelegator1 = insertTestUser("dataConverterTestDelegator1", Identity.STATUS_ACTIV);
		Delegation delegation1 = new Delegation(identityOfDelegator1, identityOfDelegatee, new Date());
		dbInstance.saveObject(delegation1);

		Identity identityOfDelegator2 = insertTestUser("dataConverterTestDelegator2", Identity.STATUS_ACTIV);
		Delegation delegation2 = new Delegation(identityOfDelegator2, identityOfDelegatee, new Date());
		dbInstance.saveObject(delegation2);
		dbInstance.flush();

		List<IdentityDate> delegatorsAndCreationDate = dataConverter.getDelegatorsAndCreationDateByDelegatee(identityOfDelegatee);

		Assert.assertEquals(2, delegatorsAndCreationDate.size());

		List<Identity> identitiesOfDelegatees = delegatorsAndCreationDate.stream().map(IdentityDate::getIdentity).collect(Collectors.toList());

		Assert.assertTrue(identitiesOfDelegatees.contains(identityOfDelegator1));
		Assert.assertTrue(identitiesOfDelegatees.contains(identityOfDelegator2));
	}

    private void insertTestData() throws CampusCourseException {
        // Insert some orgs
        List<Org> orgs = campusCourseTestDataGenerator.createOrgs();
        orgDao.save(orgs);
        dbInstance.flush();

        // Insert some courseOrgIds
        List<CourseSemesterOrgId> courseSemesterOrgIds = campusCourseTestDataGenerator.createCourseSemesterOrgIds();
        courseDao.save(courseSemesterOrgIds);
        dbInstance.flush();

        // Insert some lecturers
        List<Lecturer> lecturers = campusCourseTestDataGenerator.createLecturers();
        lecturerDao.save(lecturers);
        dbInstance.flush();

        // Add lecturers to courseOrgIds
        List<LecturerIdCourseIdDateOfLatestImport> lecturerIdCourseIdDateOfLatestImports = campusCourseTestDataGenerator.createLecturerIdCourseIdDateOfImports();
        lecturerCourseDao.save(lecturerIdCourseIdDateOfLatestImports);
        dbInstance.flush();

        // Insert some students
        List<Student> students = campusCourseTestDataGenerator.createStudents();
        studentDao.save(students);
        dbInstance.flush();

        // Add students to courseOrgIds
        List<StudentIdCourseIdDateOfLatestImport> studentIdCourseIdDateOfLatestImports = campusCourseTestDataGenerator.createStudentIdCourseIdDateOfImports();
        studentCourseDao.save(studentIdCourseIdDateOfLatestImports);
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

