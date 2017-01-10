package ch.uzh.campus.data;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseException;
import ch.uzh.campus.CampusCourseTestCase;
import ch.uzh.campus.service.data.CampusGroups;
import org.junit.Before;
import org.junit.Test;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Initial Date: Oct 28, 2014<br />
 *
 * @author aabouc
 * @author Martin Schraner
 * @author lavinia
 */
@Component
public class CourseDaoTest extends CampusCourseTestCase {

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    private SemesterDao semesterDao;

    @Autowired
    private OrgDao orgDao;

    @Autowired
    private LecturerDao lecturerDao;

    @Autowired
    private LecturerCourseDao lecturerCourseDao;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private StudentCourseDao studentCourseDao;

    @Autowired
    private TextDao textDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private BusinessGroupDAO businessGroupDao;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    private CourseDao courseDao;

    private RepositoryEntry repositoryEntry1;
    private RepositoryEntry repositoryEntry2;
    private RepositoryEntry repositoryEntry4;
    private RepositoryEntry repositoryEntry5;
    private RepositoryEntry repositoryEntry6;

    @Before
    public void before() {
        campusCourseConfiguration.setMaxYearsToKeepCkData(1);
        courseDao = new CourseDao(dbInstance, semesterDao);
    }

    @Test
    public void testSaveCourseOrgId() throws CampusCourseException {
        // Insert some orgs
        List<Org> orgs = mockDataGeneratorProvider.get().getOrgs();
        orgDao.save(orgs);
        dbInstance.flush();

        assertNull(courseDao.getCourseById(100L));
        assertNull(courseDao.getCourseById(300L));

        // Insert some courseOrgIds
        List<CourseSemesterOrgId> courseSemesterOrgIds = mockDataGeneratorProvider.get().getCourseSemesterOrgIds();
        courseDao.save(courseSemesterOrgIds);

        // Check before calling flush
        assertSave();

        dbInstance.flush();
        dbInstance.clear();

        assertSave();
    }

    private void assertSave() {
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(1, course.getOrgs().size());

        course = courseDao.getCourseById(300L);
        assertNotNull(course);
        assertEquals(9, course.getOrgs().size());

        Set<Long> orgIds = course.getOrgs().stream().map(Org::getId).collect(Collectors.toSet());
        assertTrue(orgIds.contains(9100L));
        assertTrue(orgIds.contains(9200L));
        assertTrue(orgIds.contains(9300L));
        assertTrue(orgIds.contains(9400L));
        assertTrue(orgIds.contains(9500L));
        assertTrue(orgIds.contains(9600L));
        assertTrue(orgIds.contains(9700L));
        assertTrue(orgIds.contains(9800L));
        assertTrue(orgIds.contains(9900L));

        Org org = orgDao.getOrgById(9100L);
        assertNotNull(org);
        assertEquals(2, org.getCourses().size());

        Set<Long> courseIds = org.getCourses().stream().map(Course::getId).collect(Collectors.toSet());
        assertTrue(courseIds.contains(100L));
        assertTrue(courseIds.contains(300L));
    }

    @Test
    public void testSaveOrUpdateCourseOrgId() throws CampusCourseException {
        insertTestData();

        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(1, course.getOrgs().size());

        // Modify orgs of course with id 100L
        CourseSemesterOrgId courseSemesterOrgIdUpdated = mockDataGeneratorProvider.get().getCourseSemesterOrgIds().get(0);
        assertEquals(100L, courseSemesterOrgIdUpdated.getId().longValue());
        courseSemesterOrgIdUpdated.setOrg1(null);
        courseSemesterOrgIdUpdated.setOrg2(9200L);
        courseSemesterOrgIdUpdated.setOrg3(9300L);
        courseSemesterOrgIdUpdated.setOrg4(9400L);
        courseSemesterOrgIdUpdated.setOrg5(9500L);
        courseSemesterOrgIdUpdated.setOrg6(9600L);
        courseSemesterOrgIdUpdated.setOrg7(9700L);
        courseSemesterOrgIdUpdated.setOrg8(9800L);
        courseSemesterOrgIdUpdated.setOrg9(9900L);

        courseDao.saveOrUpdate(courseSemesterOrgIdUpdated);

        dbInstance.flush();
        dbInstance.clear();

        courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(8, course.getOrgs().size());

        Set<Long> orgIds = course.getOrgs().stream().map(Org::getId).collect(Collectors.toSet());
        assertTrue(orgIds.contains(9200L));
        assertTrue(orgIds.contains(9300L));
        assertTrue(orgIds.contains(9400L));
        assertTrue(orgIds.contains(9500L));
        assertTrue(orgIds.contains(9600L));
        assertTrue(orgIds.contains(9700L));
        assertTrue(orgIds.contains(9800L));
        assertTrue(orgIds.contains(9900L));
    }

    @Test
    public void testNullGetCourseById() throws CampusCourseException {
        insertTestData();
        assertNull(courseDao.getCourseById(999L));
    }

    @Test
    public void testNotNullGetCourseById() throws CampusCourseException {
        insertTestData();
        assertNotNull(courseDao.getCourseById(100L));
    }

    @Test
    public void testGetCreatedCoursesOfCurrentSemesterByLecturerId() throws CampusCourseException {
        insertTestData();
        List<Course> courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1200L, null);

        assertEquals(1, courses.size());
        assertEquals(200L, courses.get(0).getId().longValue());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1400L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1500L, null);
        assertEquals(0, courses.size());
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(1600L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId() throws CampusCourseException {
        insertTestData();
        List<Course> courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1300L, null);

        assertNotNull(courses);
        assertEquals(1, courses.size());

        // Course already created
        courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1200L, null);
        assertTrue(courses.isEmpty());

        // Course with disabled org
        courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1800L, null);
        assertTrue(courses.isEmpty());

        // Excluded ourse
        courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1900L, null);
        assertTrue(courses.isEmpty());
    }

    @Test
    public void testLatestCourseByRepositoryEntry() throws Exception {
        insertTestData();

        // Make course 400 to be the parent course of 300, 500 the parent of 400 and 600 the parent of 500 and set for courses 300, 400 and 500 the repository entry of course 600
        courseDao.saveParentCourseId(300L, 400L);
        courseDao.saveParentCourseId(400L, 500L);
        courseDao.saveParentCourseId(500L, 600L);
        Course course = courseDao.getCourseById(600L);
        courseDao.saveRepositoryEntry(300L, course.getRepositoryEntry().getKey());
        courseDao.saveRepositoryEntry(400L, course.getRepositoryEntry().getKey());
        courseDao.saveRepositoryEntry(500L, course.getRepositoryEntry().getKey());

        course = courseDao.getLatestCourseByRepositoryEntry(course.getRepositoryEntry().getKey());
        assertNotNull(course);
        assertEquals(300L, course.getId().longValue());
    }

    @Test
    public void testDelete() throws CampusCourseException {
        insertTestData();
        assertNotNull(courseDao.getCourseById(100L));
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(2, lecturer.getLecturerCourses().size());
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        Student student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertEquals(3, student.getStudentCourses().size());
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        Org org = orgDao.getOrgById(9100L);
        assertNotNull(org);
        assertEquals(2, org.getCourses().size());

        Course courseToBeDeleted = courseDao.getCourseById(100L);
        courseDao.delete(courseToBeDeleted);

        // Check before flush
        assertEquals(1, lecturer.getLecturerCourses().size());
        assertEquals(2, student.getStudentCourses().size());
        assertEquals(1, org.getCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(courseDao.getCourseById(100L));

        lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(1, lecturer.getLecturerCourses().size());
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertEquals(2, student.getStudentCourses().size());
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        org = orgDao.getOrgById(9100L);
        assertNotNull(org);
        assertEquals(1, org.getCourses().size());
    }

    @Test
    public void testDeleteByCourseId() throws CampusCourseException {
        insertTestData();
        assertNotNull(courseDao.getCourseById(100L));
        Lecturer lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(2, lecturer.getLecturerCourses().size());
        assertNotNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        Student student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertEquals(3, student.getStudentCourses().size());
        assertNotNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        Org org = orgDao.getOrgById(9100L);
        assertNotNull(org);
        assertEquals(2, org.getCourses().size());
        assertFalse(textDao.getTextsByCourseId(100L).isEmpty());
        assertFalse(eventDao.getEventsByCourseId(100L).isEmpty());

        courseDao.deleteByCourseId(100L);

        // Check before flush
        assertEquals(1, lecturer.getLecturerCourses().size());
        assertEquals(2, student.getStudentCourses().size());
        assertEquals(1, org.getCourses().size());

        dbInstance.flush();
        dbInstance.clear();

        assertNull(courseDao.getCourseById(100L));

        lecturer = lecturerDao.getLecturerById(1100L);
        assertNotNull(lecturer);
        assertEquals(1, lecturer.getLecturerCourses().size());
        assertNull(lecturerCourseDao.getLecturerCourseById(1100L, 100L));
        student = studentDao.getStudentById(2100L);
        assertNotNull(student);
        assertEquals(2, student.getStudentCourses().size());
        assertNull(studentCourseDao.getStudentCourseById(2100L, 100L));
        org = orgDao.getOrgById(9100L);
        assertNotNull(org);
        assertEquals(1, org.getCourses().size());
        assertTrue(textDao.getTextsByCourseId(100L).isEmpty());
        assertTrue(eventDao.getEventsByCourseId(100L).isEmpty());
    }

    @Test
    public void testDeleteByCourseIds() throws CampusCourseException {
        insertTestData();
        assertEquals(2, courseDao.getAllCreatedCoursesOfCurrentSemester().size());

        List<Long> courseIds = new LinkedList<>();
        courseIds.add(100L);
        courseIds.add(200L);

        courseDao.deleteByCourseIds(courseIds);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, courseDao.getAllCreatedCoursesOfCurrentSemester().size());
    }

    @Test
    public void testGetCampusGroupsByRepositoryEntry() throws CampusCourseException {
        insertTestData();

        Course course1 = courseDao.getCourseById(200L);
        RepositoryEntry repositoryEntry1 = course1.getRepositoryEntry();
        assertNotNull(repositoryEntry1);

        Course course2 = courseDao.getCourseById(300L);
        // Add the repository entry of course 1 to course 2
        courseDao.saveRepositoryEntry(course2.getId(), repositoryEntry1.getKey());
        dbInstance.flush();

        // Add different campus groups
        BusinessGroup campusGroupA1 = insertCampusGroup("Campusgruppe A", "Description Campusgruppe A");
        BusinessGroup campusGroupB1 = insertCampusGroup("Campusgruppe B", "Description Campusgruppe B");
        BusinessGroup campusGroupA2 = insertCampusGroup("Campusgruppe A", "Description Campusgruppe A");
        BusinessGroup campusGroupB2 = insertCampusGroup("Campusgruppe B", "Description Campusgruppe B");
        dbInstance.flush();

        courseDao.saveCampusGroupA(course1.getId(), campusGroupA1.getKey());
        courseDao.saveCampusGroupB(course1.getId(), campusGroupB1.getKey());
        courseDao.saveCampusGroupA(course2.getId(), campusGroupA2.getKey());
        courseDao.saveCampusGroupB(course2.getId(), campusGroupB2.getKey());
        dbInstance.flush();

        Set<CampusGroups> setOfCampusGroups = courseDao.getCampusGroupsByRepositoryEntry(repositoryEntry1.getKey());

        assertNotNull(setOfCampusGroups);
        assertEquals(2, setOfCampusGroups.size());

        Set<Long> campusGroupAKeys = setOfCampusGroups.stream().map(campusGroups -> campusGroups.getCampusGroupA().getKey()).collect(Collectors.toSet());
        Set<Long> campusGroupBKeys = setOfCampusGroups.stream().map(campusGroups -> campusGroups.getCampusGroupB().getKey()).collect(Collectors.toSet());
        assertTrue(campusGroupAKeys.contains(campusGroupA1.getKey()));
        assertTrue(campusGroupAKeys.contains(campusGroupA2.getKey()));
        assertTrue(campusGroupBKeys.contains(campusGroupB1.getKey()));
        assertTrue(campusGroupBKeys.contains(campusGroupB2.getKey()));

        // Campus course without campus groups
        Course course3 = courseDao.getCourseById(100L);
        RepositoryEntry repositoryEntry3 = course3.getRepositoryEntry();
        assertNotNull(repositoryEntry3);
        assertNull(course3.getCampusGroupA());
        assertNull(course3.getCampusGroupB());

        setOfCampusGroups = courseDao.getCampusGroupsByRepositoryEntry(repositoryEntry3.getKey());

        assertNotNull(setOfCampusGroups);
        assertTrue(setOfCampusGroups.isEmpty());
    }

    @Test
    public void testSaveRepositoryEntry() throws CampusCourseException {
        insertTestData();
        Course course = courseDao.getCourseById(300L);
        assertNull(course.getRepositoryEntry());

        RepositoryEntry repositoryEntry3 = repositoryService.create("Rei Ayanami", "-", "Repository entry 3 CourseDaoTest", "", null);
        courseDao.saveRepositoryEntry(course.getId(), repositoryEntry3.getKey());

        assertEquals(repositoryEntry3, course.getRepositoryEntry());

        dbInstance.flush();
        dbInstance.clear();

        Course updatedCourse = courseDao.getCourseById(course.getId());
        assertEquals(repositoryEntry3, updatedCourse.getRepositoryEntry());
    }

    @Test
    public void testSaveCampusGroupA() throws CampusCourseException {
        insertTestData();
        Course course = courseDao.getCourseById(300L);
        assertNull(course.getCampusGroupA());

        BusinessGroup campusGroupA = insertCampusGroup("Campusgruppe A", "Description Campusgruppe A");
        dbInstance.flush();

        courseDao.saveCampusGroupA(course.getId(), campusGroupA.getKey());

        assertEquals(campusGroupA, course.getCampusGroupA());

        dbInstance.flush();
        dbInstance.clear();

        Course updatedCourse = courseDao.getCourseById(course.getId());
        assertEquals(campusGroupA, updatedCourse.getCampusGroupA());
    }

    @Test
    public void testSaveCampusGroupB() throws CampusCourseException {
        insertTestData();
        Course course = courseDao.getCourseById(300L);
        assertNull(course.getCampusGroupB());

        BusinessGroup campusGroupB = insertCampusGroup("Campusgruppe B", "Description Campusgruppe B");
        dbInstance.flush();

        courseDao.saveCampusGroupB(course.getId(), campusGroupB.getKey());

        assertEquals(campusGroupB, course.getCampusGroupB());

        dbInstance.flush();
        dbInstance.clear();

        Course updatedCourse = courseDao.getCourseById(course.getId());
        assertEquals(campusGroupB, updatedCourse.getCampusGroupB());
    }

    @Test
    public void testDisableSynchronization() throws CampusCourseException {
        insertTestData();
        Course course = courseDao.getCourseById(100L);
        assertTrue(course.isSynchronizable());

        courseDao.disableSynchronization(100L);

        assertFalse(course.isSynchronizable());

        dbInstance.flush();
        dbInstance.clear();

        Course updatedCourse = courseDao.getCourseById(100L);
        assertFalse(updatedCourse.isSynchronizable());
    }

    @Test
    public void testResetRepositoryEntryAndParentCourse() throws CampusCourseException {
        insertTestData();
        Course courseWithoutParentCourse = courseDao.getCourseById(100L);
        Course courseWithParentCourse = courseDao.getCourseById(200L);
        dbInstance.flush();

        // Make course 400 to be the parent course of 200
        courseDao.saveParentCourseId(200L, 400L);
        assertEquals(repositoryEntry1, courseWithoutParentCourse.getRepositoryEntry());
        assertNull(courseWithoutParentCourse.getParentCourse());
        assertEquals(repositoryEntry2, courseWithParentCourse.getRepositoryEntry());
        assertNotNull(courseWithParentCourse.getParentCourse());

        courseDao.resetRepositoryEntryAndParentCourse(repositoryEntry1.getKey());
        courseDao.resetRepositoryEntryAndParentCourse(repositoryEntry2.getKey());

        assertNull(courseWithoutParentCourse.getRepositoryEntry());
        assertNull(courseWithoutParentCourse.getParentCourse());
        assertNull(courseWithParentCourse.getRepositoryEntry());
        assertNull(courseWithParentCourse.getParentCourse());

        dbInstance.flush();
        dbInstance.clear();

        Course updatedCourseWithoutParentCourse = courseDao.getCourseById(100L);
        assertNull(updatedCourseWithoutParentCourse.getRepositoryEntry());
        assertNull(updatedCourseWithoutParentCourse.getParentCourse());
        Course updatedCourseWithParentcourse = courseDao.getCourseById(200L);
        assertNull(updatedCourseWithParentcourse.getRepositoryEntry());
        assertNull(updatedCourseWithParentcourse.getParentCourse());
    }

    @Test
    public void testResetCampusGroup() throws CampusCourseException {
        insertTestData();
        BusinessGroup campusGroupA = insertCampusGroup("Campusgruppe A", "Description Campusgruppe A");
        BusinessGroup campusGroupB = insertCampusGroup("Campusgruppe B", "Description Campusgruppe B");
        dbInstance.flush();

        Course course = courseDao.getCourseById(100L);
        courseDao.saveCampusGroupA(course.getId(), campusGroupA.getKey());
        courseDao.saveCampusGroupB(course.getId(), campusGroupB.getKey());
        dbInstance.flush();

        assertEquals(campusGroupA, course.getCampusGroupA());
        assertEquals(campusGroupB, course.getCampusGroupB());

        // Reset campus group A
        courseDao.resetCampusGroup(campusGroupA.getKey());
        dbInstance.flush();

        course = courseDao.getCourseById(course.getId());
        assertNull(course.getCampusGroupA());
        assertEquals(campusGroupB, course.getCampusGroupB());

        // Add campus group A again
        courseDao.saveCampusGroupA(course.getId(), campusGroupA.getKey());
        dbInstance.flush();

        course = courseDao.getCourseById(course.getId());
        assertEquals(campusGroupA, course.getCampusGroupA());
        assertEquals(campusGroupB, course.getCampusGroupB());

        // Reset campus group B
        courseDao.resetCampusGroup(campusGroupB.getKey());
        dbInstance.flush();

        course = courseDao.getCourseById(course.getId());
        assertEquals(campusGroupA, course.getCampusGroupA());
        assertNull(course.getCampusGroupB());
    }

    @Test
    public void testSaveParentCourseId() throws CampusCourseException {
        insertTestData();
        Course parentCourse = courseDao.getCourseById(100L);
        Course course = courseDao.getCourseById(200L);
        assertNotNull(parentCourse);
        assertNotNull(course);
        assertNull(course.getParentCourse());
        assertNull(parentCourse.getChildCourse());

        courseDao.saveParentCourseId(200L, 100L);

        assertParentCourse(course, parentCourse);

        dbInstance.flush();
        dbInstance.clear();

        Course updatedParentCourse = courseDao.getCourseById(100L);
        Course updatedCourse = courseDao.getCourseById(200L);
        assertParentCourse(updatedCourse, updatedParentCourse);
    }

    private void assertParentCourse(Course course, Course parentCourse) {
        assertNotNull(course.getParentCourse());
        assertEquals(100L, course.getParentCourse().getId().longValue());
        assertNotNull(parentCourse.getChildCourse());
        assertEquals(200L, parentCourse.getChildCourse().getId().longValue());
    }

    @Test
    public void testGetRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters() throws CampusCourseException {
        insertTestData();

        Semester semester1 = semesterDao.getSemesterBySemesterNameAndYear(SemesterName.FRUEHJAHRSSEMESTER, 2099);
        assertNotNull(semester1);
        Semester semester2 = semesterDao.getSemesterBySemesterNameAndYear(SemesterName.HERBSTSEMESTER, 2098);
        assertNotNull(semester2);
        Semester semester3 = semesterDao.getSemesterBySemesterNameAndYear(SemesterName.FRUEHJAHRSSEMESTER, 2098);
        assertNotNull(semester3);

        List<Long> semesterIds = new ArrayList<>();
        semesterIds.add(semester1.getId());
        semesterIds.add(semester2.getId());
        semesterIds.add(semester3.getId());

        List<Long> repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters = courseDao.getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters(semesterIds);
        assertEquals(3, repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters.size());
        assertTrue(repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters.contains(repositoryEntry4.getKey()));
        assertTrue(repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters.contains(repositoryEntry5.getKey()));
        assertTrue(repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters.contains(repositoryEntry6.getKey()));

        // Make course 400 to be the parent course of 300 and set set the same repository entry for courses 300 and 400
        // -> Should not contain course 400 any more, because it is continued, i.e. it is the parent of course 300
        dbInstance.clear();
        courseDao.saveParentCourseId(300L, 400L);

        RepositoryEntry repositoryEntry = repositoryService.create("Rei Ayanami", "-", "Repository entry CourseDaoTest", "", null);
        courseDao.saveRepositoryEntry(300L, repositoryEntry.getKey());
        courseDao.saveRepositoryEntry(400L, repositoryEntry.getKey());
        dbInstance.flush();

        repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters = courseDao.getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters(semesterIds);
        assertEquals(2, repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters.size());
        assertTrue(repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters.contains(repositoryEntry5.getKey()));
        assertTrue(repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters.contains(repositoryEntry6.getKey()));

        // If we look for courses in 99HS, we should get course 300, which has a parent, but is not continued itself
        dbInstance.clear();

        Semester semester4 = semesterDao.getSemesterBySemesterNameAndYear(SemesterName.HERBSTSEMESTER, 2099);
        assertNotNull(semester4);

        semesterIds = new ArrayList<>();
        semesterIds.add(semester4.getId());

        repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters = courseDao.getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters(semesterIds);
        assertTrue(repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters.contains(repositoryEntry.getKey()));
    }

    @Test
    public void testGetRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfPreviousSemestersNotTooFarInThePast() throws CampusCourseException {
        insertTestData();
        List<Long> repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfOldSemestersNotTooFarInThePast = courseDao.getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfPreviousSemestersNotTooFarInThePast();
        assertEquals(2, repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfOldSemestersNotTooFarInThePast.size());
        assertTrue(repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfOldSemestersNotTooFarInThePast.contains(repositoryEntry4.getKey()));
        assertTrue(repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfOldSemestersNotTooFarInThePast.contains(repositoryEntry5.getKey()));

        // Make course 400 to be the parent course of 300 and set set the same repository entry for courses 300 and 400
        // -> Should not contain course 400 any more, because it is continued, i.e. it is the parent of course 300
        dbInstance.clear();
        courseDao.saveParentCourseId(300L, 400L);

        RepositoryEntry repositoryEntry = repositoryService.create("Rei Ayanami", "-", "Repository entry CourseDaoTest", "", null);
        courseDao.saveRepositoryEntry(300L, repositoryEntry.getKey());
        courseDao.saveRepositoryEntry(400L, repositoryEntry.getKey());
        dbInstance.flush();

        repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfOldSemestersNotTooFarInThePast = courseDao.getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfPreviousSemestersNotTooFarInThePast();
        assertEquals(1, repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfOldSemestersNotTooFarInThePast.size());
        assertTrue(repositoryEntryKeysOfAllCreatedNotContinuedCoursesOfOldSemestersNotTooFarInThePast.contains(repositoryEntry5.getKey()));
    }

    @Test
    public void testGetIdsOfAllCreatedSynchronizableCoursesOfCurrentSemesterAndMostRecentImport() throws CampusCourseException {
        insertTestData();
        Calendar startTimeOfMostRecentCourseImportAsCalendar = new GregorianCalendar(2099, Calendar.OCTOBER, 11);
        startTimeOfMostRecentCourseImportAsCalendar.set(Calendar.HOUR_OF_DAY, 10);
		startTimeOfMostRecentCourseImportAsCalendar.set(Calendar.MINUTE, 13);
		startTimeOfMostRecentCourseImportAsCalendar.set(Calendar.SECOND, 0);
        assertEquals(2, courseDao.getIdsOfAllCreatedSynchronizableCoursesOfCurrentSemesterAndMostRecentImport(startTimeOfMostRecentCourseImportAsCalendar.getTime()).size());

        // Remove repository entry from course 100
        Course course = courseDao.getCourseById(100L);
        course.setRepositoryEntry(null);
        dbInstance.flush();
        assertEquals(1, courseDao.getIdsOfAllCreatedSynchronizableCoursesOfCurrentSemesterAndMostRecentImport(startTimeOfMostRecentCourseImportAsCalendar.getTime()).size());

        // Add 1 second to start time of import -> should still be found
		startTimeOfMostRecentCourseImportAsCalendar.add(Calendar.SECOND, 1);
		assertEquals(1, courseDao.getIdsOfAllCreatedSynchronizableCoursesOfCurrentSemesterAndMostRecentImport(startTimeOfMostRecentCourseImportAsCalendar.getTime()).size());

		// Add another second -> should not be found any more
		startTimeOfMostRecentCourseImportAsCalendar.add(Calendar.SECOND, 1);
		assertTrue(courseDao.getIdsOfAllCreatedSynchronizableCoursesOfCurrentSemesterAndMostRecentImport(startTimeOfMostRecentCourseImportAsCalendar.getTime()).isEmpty());
    }

    @Test
    public void testGetIdsOfAllNotCreatedCreatableCoursesOfCurrentSemester() throws CampusCourseException {
        insertTestData();
        List<Long> idsFound = courseDao.getIdsOfAllNotCreatedCreatableCoursesOfCurrentSemester();
        assertEquals(1, idsFound.size());
        assertTrue(idsFound.contains(300L));

        // Remove repository entry from course 100
        Course course = courseDao.getCourseById(100L);
        course.setRepositoryEntry(null);
        dbInstance.flush();

        idsFound = courseDao.getIdsOfAllNotCreatedCreatableCoursesOfCurrentSemester();
        assertEquals(2, idsFound.size());
        assertTrue(idsFound.contains(100L));
        assertTrue(idsFound.contains(300L));
    }

    @Test
    public void testGetAllCreatedCoursesOfCurrentSemester() throws CampusCourseException {
        insertTestData();
        assertEquals(2, courseDao.getAllCreatedCoursesOfCurrentSemester().size());

        courseDao.deleteByCourseId(100L);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(1, courseDao.getAllCreatedCoursesOfCurrentSemester().size());
    }

    @Test
    public void tesGetAllNotCreatedOrphanedCourses() throws CampusCourseException {
        insertTestData();

        assertEquals(0, courseDao.getAllNotCreatedOrphanedCourses().size());

        // Remove lecturerCourse and studentCourse entries of course 100 (not created course) and 300 (created course)-> courses 100 and 300 are orphaned
        List<Long> courseIds = new ArrayList<>();
        courseIds.add(100L);
        courseIds.add(300L);
        lecturerCourseDao.deleteByCourseIdsAsBulkDelete(courseIds);
        studentCourseDao.deleteByCourseIdsAsBulkDelete(courseIds);
        dbInstance.flush();

        List<Long> courseIdsFound = courseDao.getAllNotCreatedOrphanedCourses();

        assertEquals(1, courseIdsFound.size());
        assertFalse(courseIdsFound.contains(100L));   // created course
        assertTrue(courseIdsFound.contains(300L));    // not created course
    }

    @Test
    public void testExistCoursesForRepositoryEntry() throws CampusCourseException {
        insertTestData();

        RepositoryEntry repositoryEntry = repositoryService.create("Rei Ayanami", "-", "Repository entry CourseDaoTest", "", null);

        assertFalse(courseDao.existCoursesForRepositoryEntry(repositoryEntry.getKey()));

        Course course = courseDao.getCourseById(300L);
        assertNotNull(course);
        courseDao.saveRepositoryEntry(course.getId(), repositoryEntry.getKey());
        dbInstance.flush();

        assertTrue(courseDao.existCoursesForRepositoryEntry(repositoryEntry.getKey()));
    }

    @Test
    public void testGetCreatedCoursesOfCurrentSemesterByStudentId() throws CampusCourseException {
        insertTestData();

    	List<Course> courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2100L, null);
        assertNotNull(courses);
    	assertEquals(2, courses.size());
        List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
        assertTrue(courseIds.contains(100L));
        assertTrue(courseIds.contains(200L));

        // Student without any created courses
        courses = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(2400L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse() throws CampusCourseException {
        insertTestData();

        Student student1 = studentDao.getStudentById(2100L);
        Student student2 = studentDao.getStudentById(2400L);

        Course course1 = courseDao.getCourseById(300L);
        Course course2 = courseDao.getCourseById(400L);

        // Make course 2 to be the parent course of course 1 and set set the same repository entry for courses 1 and 2
        courseDao.saveParentCourseId(course1.getId(), course2.getId());

        RepositoryEntry repositoryEntry = repositoryService.create("Rei Ayanami", "-", "Repository entry CourseDaoTest", "", null);
        course1.setRepositoryEntry(repositoryEntry);
        course2.setRepositoryEntry(repositoryEntry);
        dbInstance.flush();

        // Student 1 has only a booking for course course 1, but not for it's parent course
        assertTrue(courseDao.getCreatedCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(student1.getId(), null).isEmpty());

        // Student 2 has a booking for the parent course of course 1, but not for course 1
        List<Course> coursesFound = courseDao.getCreatedCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(student2.getId(), null);
        assertEquals(1, coursesFound.size());
        List<Long> courseIds = coursesFound.stream().map(Course::getId).collect(Collectors.toList());
        assertTrue(courseIds.contains(course1.getId()));

        // Add booking for student 1 for course 2, i.e. the parent course of course 1
        studentCourseDao.save(new StudentCourse(student1, course2, new Date()));
        dbInstance.flush();

        // Student 1 has also booking for current semester, so we should not find it
        assertTrue(courseDao.getCreatedCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(student1.getId(), null).isEmpty());

        // Remove booking of student 1 for course 1
        studentCourseDao.delete(new StudentCourse(student1, course1, new Date()));
        dbInstance.flush();

        // Student 1 should now be found, since it has only a booking to the parent course
        coursesFound = courseDao.getCreatedCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(student1.getId(), null);
        assertEquals(1, coursesFound.size());
        courseIds = coursesFound.stream().map(Course::getId).collect(Collectors.toList());
        assertTrue(courseIds.contains(course1.getId()));
    }

    @Test
    public void testGetNotCreatedCreatableCoursesOfCurrentSemesterByStudentId() throws CampusCourseException {
        insertTestData();

        List<Course> courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2100L, null);
        assertEquals(1, courses.size());

        // Course with disabled org
        courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2700L, null);
        assertEquals(0, courses.size());

        // Excluded course
        courses = courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2800L, null);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId() throws CampusCourseException {
        insertTestData();

        List<Course> courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1200L);
        assertNotNull(courses);
        assertEquals(1, courses.size());
        assertEquals(200L, courses.get(0).getId().longValue());

        // Course already created
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1400L);
        assertTrue(courses.isEmpty());

        // Courses of old semester
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1500L);
        assertTrue(courses.isEmpty());
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1600L);
        assertTrue(courses.isEmpty());

        // Course with disabled org
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1800L);
        assertTrue(courses.isEmpty());

        // Excluded course
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(1900L);
        assertTrue(courses.isEmpty());
    }

    @Test
    public void testGetCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId() throws CampusCourseException {
        insertTestData();
        List<Course> courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2100L);

        assertNotNull(courses);
        assertEquals(3, courses.size());
        List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
        assertTrue(courseIds.contains(100L));
        assertTrue(courseIds.contains(200L));
        assertTrue(courseIds.contains(300L));

        // Student only with course with disabled org
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2700L);
        assertEquals(0, courses.size());

        // Student only with excluded course
        courses = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(2800L);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse() throws CampusCourseException {
        insertTestData();

        Student student1 = studentDao.getStudentById(2100L);
        Student student2 = studentDao.getStudentById(2400L);

        Course course1 = courseDao.getCourseById(300L);
        Course course2 = courseDao.getCourseById(400L);
        Course course3 = courseDao.getCourseById(700L);
        Course course4 = courseDao.getCourseById(800L);

        // Make course 2 to be the parent course of course 1 (course 1 is creatable)
        courseDao.saveParentCourseId(course1.getId(), course2.getId());
        dbInstance.flush();

        // Student 1 has only a booking for course course 1, but not for it's parent course
        assertTrue(courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(student1.getId()).isEmpty());

        // Student 2 has a booking for course 2, but not for course 1
        List<Course> coursesFound = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(student2.getId());
        assertEquals(1, coursesFound.size());
        List<Long> courseIds = coursesFound.stream().map(Course::getId).collect(Collectors.toList());
        assertTrue(courseIds.contains(course1.getId()));

        // Add repository entry to courses 1 and 2, i.e. make it created
        RepositoryEntry repositoryEntry = repositoryService.create("Rei Ayanami", "-", "Repository entry CourseDaoTest", "", null);
        courseDao.saveRepositoryEntry(course1.getId(), repositoryEntry.getKey());
        courseDao.saveRepositoryEntry(course2.getId(), repositoryEntry.getKey());
        dbInstance.flush();

        // Should behave as before
        assertTrue(courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(student1.getId()).isEmpty());
        coursesFound = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(student2.getId());
        assertEquals(1, coursesFound.size());
        courseIds = coursesFound.stream().map(Course::getId).collect(Collectors.toList());
        assertTrue(courseIds.contains(course1.getId()));

        // Add booking for student 1 for course 2, i.e. the parent course of course 1
        studentCourseDao.save(new StudentCourse(student1, course2, new Date()));
        dbInstance.flush();

        // Student 1 has also booking for current semester, so we should not find it
        assertTrue(courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(student1.getId()).isEmpty());

        // Remove booking of student 1 for course 1
        studentCourseDao.delete(new StudentCourse(student1, course1, new Date()));
        dbInstance.flush();

        // Make course 2 to be the parent course of course 3 (course with disabled org)
        courseDao.resetRepositoryEntryAndParentCourse(course1.getRepositoryEntry().getKey());
        courseDao.saveParentCourseId(course3.getId(), course2.getId());
        courseDao.saveRepositoryEntry(course3.getId(), repositoryEntry.getKey());
        courseDao.saveRepositoryEntry(course2.getId(), repositoryEntry.getKey());
        dbInstance.flush();

        // Course 3 should not be found, since it has a disabled org
        assertTrue(courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(student2.getId()).isEmpty());

        // Make course 2 to be the parent course of course 4 (excluded course)
        courseDao.resetRepositoryEntryAndParentCourse(course3.getRepositoryEntry().getKey());
        courseDao.saveParentCourseId(course4.getId(), course2.getId());
        courseDao.saveRepositoryEntry(course4.getId(), repositoryEntry.getKey());
        courseDao.saveRepositoryEntry(course2.getId(), repositoryEntry.getKey());
        dbInstance.flush();

        // Course 4 should not be found, since it has a disabled org
        assertTrue(courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(student2.getId()).isEmpty());
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

        // Add some texts
        List<TextCourseId> textCourseIds = mockDataGeneratorProvider.get().getTextCourseIds();
        textDao.addTextsToCourse(textCourseIds);
        dbInstance.flush();

        // Add some events
        List<EventCourseId> eventCourseIds = mockDataGeneratorProvider.get().getEventCourseIds();
        eventDao.addEventsToCourse(eventCourseIds);
        dbInstance.flush();

        // Set current semester
        Course course = courseDao.getCourseById(100L);
        semesterDao.setCurrentSemester(course.getSemester().getId());
        dbInstance.flush();

        addOlatRepositoryEntryToCourses_100_200_400_500_600();
    }

    private void addOlatRepositoryEntryToCourses_100_200_400_500_600() {
        repositoryEntry1 = repositoryService.create("Rei Ayanami", "-", "Repository entry 1 CourseDaoTest", "", null);
        repositoryEntry2 = repositoryService.create("Rei Ayanami", "-", "Repository entry 2 CourseDaoTest", "", null);
        repositoryEntry4 = repositoryService.create("Rei Ayanami", "-", "Repository entry 4 CourseDaoTest", "", null);
        repositoryEntry5 = repositoryService.create("Rei Ayanami", "-", "Repository entry 5 CourseDaoTest", "", null);
        repositoryEntry6 = repositoryService.create("Rei Ayanami", "-", "Repository entry 6 CourseDaoTest", "", null);
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

    private BusinessGroup insertCampusGroup(String groupName, String groupDescription) {
        return businessGroupDao.createAndPersist(null, groupName, groupDescription, -1, -1, false, false, false, false, false);
    }
}
