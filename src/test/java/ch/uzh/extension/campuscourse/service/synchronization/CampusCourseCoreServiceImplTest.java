package ch.uzh.extension.campuscourse.service.synchronization;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.CampusCourseJunitTestHelper;
import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import ch.uzh.extension.campuscourse.model.SemesterName;
import ch.uzh.extension.campuscourse.data.entity.Course;
import ch.uzh.extension.campuscourse.data.entity.Semester;
import ch.uzh.extension.campuscourse.service.CampusCourseCoreService;
import ch.uzh.extension.campuscourse.service.CampusCourseCoreServiceImpl;
import ch.uzh.extension.campuscourse.service.coursecreation.CampusCoursePublisher;
import ch.uzh.extension.campuscourse.service.coursecreation.CampusGroupsCreator;
import ch.uzh.extension.campuscourse.service.coursecreation.OlatCampusCourseCreator;
import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import ch.uzh.extension.campuscourse.service.dao.DataConverter;
import ch.uzh.extension.campuscourse.service.synchronization.CampusCourseDefaultCoOwners;
import ch.uzh.extension.campuscourse.service.synchronization.CampusCourseRepositoryEntryDescriptionBuilder;
import ch.uzh.extension.campuscourse.service.synchronization.CampusCourseRepositoryEntrySynchronizer;
import ch.uzh.extension.campuscourse.service.synchronization.CampusGroupsSynchronizer;
import ch.uzh.extension.campuscourse.model.CampusCourseTO;
import ch.uzh.extension.campuscourse.data.dao.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.course.CourseFactory;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author cg
 * @author Martin Schraner
 */
@Component
public class CampusCourseCoreServiceImplTest extends CampusCourseTestCase {

    private static final String TEST_EVENT_DESCRIPTION_TEXT = "Event description";
    private static final String CAMPUS_LEARNING_AREA_NAME = "Campuslernbereich";
    private static final String CAMPUS_GROUP_A_DEFAULT_NAME = "Campusgruppe A";
    private static final String CAMPUS_GROUP_B_DEFAULT_NAME = "Campusgruppe B";

    @Autowired
    private SemesterDao semesterDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private LecturerDao lecturerDao;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private LecturerCourseDao lecturerCourseDao;

    @Autowired
    private StudentCourseDao studentCourseDao;

    @Autowired
    private DelegationDao delegationDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private OrgDao orgDao;

    @Autowired
    private ImportStatisticDao importStatisticDao;

    @Autowired
    private BusinessGroupService businessGroupService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RepositoryEntryDAO repositoryEntryDAO;

    @Autowired
    private GroupDAO groupDAO;

    @Autowired
    private BGAreaManager bgAreaManager;
    
    @Autowired
    private DB dbInstance;

    @Autowired
    private CampusCourseDefaultCoOwners campusCourseDefaultCoOwners;

    @Autowired
    private OLATResourceManager olatResourceManager;

    @Autowired
    private OlatCampusCourseCreator olatCampusCourseCreator;

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    private CampusGroupsSynchronizer campusGroupsSynchronizer;

    @Autowired
    private CampusCourseRepositoryEntrySynchronizer campusCourseRepositoryEntrySynchronizer;

    @Autowired
    private CampusCourseRepositoryEntryDescriptionBuilder campusCourseRepositoryEntryDescriptionBuilder;

    @Autowired
    private CampusGroupsCreator campusGroupsCreator;

    private DataConverter dataConverterMock;
    private DaoManager daoManagerMock;
    private CampusCourseCoreService campusCourseCoreServiceMock;
    private Course course;
    private Course childCourse;
    private Identity firstLecturerIdentity;
    private Identity secondLecturerIdentity;
    private Identity thirdLecturerIdentity;
    private Identity firstParticipantIdentity;
    private Identity secondParticipantIdentity;
    private Identity thirdParticipantIdentity;

    @Before
    public void setup() throws CampusCourseException {

        // Setup test users
    	firstLecturerIdentity = JunitTestHelper.createAndPersistIdentityAsUser("FirstLecturer");  // course owner
        secondLecturerIdentity = JunitTestHelper.createAndPersistIdentityAsUser("SecondLecturer");
        firstParticipantIdentity = JunitTestHelper.createAndPersistIdentityAsUser("FirstParticipant");
        secondParticipantIdentity = JunitTestHelper.createAndPersistIdentityAsUser("SecondParticipant");
        thirdLecturerIdentity = JunitTestHelper.createAndPersistIdentityAsUser("ThirdLecturer");
        thirdParticipantIdentity = JunitTestHelper.createAndPersistIdentityAsUser("ThirdParticipant");
        Identity templateOwnerIdentity = JunitTestHelper.createAndPersistIdentityAsUser("TemplateOwner");

        Set<Identity> lecturerIdentities = new HashSet<>();
        lecturerIdentities.add(firstLecturerIdentity);
        lecturerIdentities.add(secondLecturerIdentity);

        Set<Identity> participantIdentities = new HashSet<>();
        participantIdentities.add(firstParticipantIdentity);
        participantIdentities.add(secondParticipantIdentity);

        // Create sap campus course
        Semester semester = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2099, false);
        course = CampusCourseJunitTestHelper.createCourseForTest(100L, semester, semesterDao, courseDao, dbInstance);

        // Create child course
        Semester semesterChildCourse = new Semester(SemesterName.HERBSTSEMESTER, 2099, false);
        childCourse = CampusCourseJunitTestHelper.createCourseForTest(101L, semesterChildCourse, semesterDao, courseDao, dbInstance);

        // Create olat course for template
        RepositoryEntry templateRepositoryEntry = JunitTestHelper.deployDemoCourse(templateOwnerIdentity);

        // Create daoManagerMock
        dataConverterMock = mock(DataConverter.class);
        when(dataConverterMock.convertLecturersToIdentities(any())).thenReturn(lecturerIdentities);
        when(dataConverterMock.convertDelegateesToIdentities(any())).thenReturn(Collections.emptySet());
        when(dataConverterMock.convertStudentsToIdentities(any())).thenReturn(participantIdentities);

        TextDao textDaoMock = mock(TextDao.class);
        when(textDaoMock.getContentsByCourseId(anyLong())).thenReturn(TEST_EVENT_DESCRIPTION_TEXT);

        daoManagerMock = new DaoManager(
                courseDao,
                semesterDao,
                studentDao,
                lecturerCourseDao,
                studentCourseDao,
                lecturerDao,
                delegationDao,
                textDaoMock,
                eventDao,
                orgDao,
                importStatisticDao,
                dataConverterMock,
                campusCourseConfiguration);

        // Create campusCourseCoreServiceMock
        CampusCoursePublisher campusCoursePublisherMock = mock(CampusCoursePublisher.class);

        CampusCourseConfiguration campusCourseConfigurationMock = mock(CampusCourseConfiguration.class);
        when(campusCourseConfigurationMock.getTemplateRepositoryEntryId(anyString())).thenReturn(templateRepositoryEntry.getKey());
        when(campusCourseConfigurationMock.getSupportedTemplateLanguage(anyString())).thenReturn("DE");
        when(campusCourseConfigurationMock.getCampusGroupsLearningAreaName()).thenReturn(CAMPUS_LEARNING_AREA_NAME);
        when(campusCourseConfigurationMock.getCampusGroupADefaultName()).thenReturn(CAMPUS_GROUP_A_DEFAULT_NAME);
        when(campusCourseConfigurationMock.getCampusGroupBDefaultName()).thenReturn(CAMPUS_GROUP_B_DEFAULT_NAME);

        campusCourseCoreServiceMock = new CampusCourseCoreServiceImpl(dbInstance,
                daoManagerMock, repositoryService, olatCampusCourseCreator,
                campusCoursePublisherMock, campusGroupsCreator, campusCourseConfigurationMock,
                campusGroupsSynchronizer, campusCourseRepositoryEntrySynchronizer, olatResourceManager, businessGroupService, campusCourseDefaultCoOwners);
    }

	@After
    public void tearDown() throws Exception {
		dbInstance.rollback();
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkRepositoryEntryNotNullCourseNotNull() throws Exception {
        RepositoryEntry createdRepositoryEntry = createOlatCampusCourseFromStandardTemplate();
        assertNotNull("Missing repositoryEntry in CampusCourse return-object", createdRepositoryEntry);
        assertNotNull("Missing Course in CampusCourse return-object", CourseFactory.loadCourse(createdRepositoryEntry));
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkRepositoryEntryAccess() throws Exception {
        RepositoryEntry createdRepositoryEntry = createOlatCampusCourseFromStandardTemplate();
        assertAccess(createdRepositoryEntry);
        // Ditto, but read from DB
        assertAccess(loadRepositoryEntryFromDatabase(createdRepositoryEntry));
    }

    private void assertAccess(RepositoryEntry repositoryEntry) {
        assertEquals("CampusCourse Access must be 'BARG'", RepositoryEntry.ACC_USERS_GUESTS, repositoryEntry.getAccess());
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkRepositoryEntryDisplayname() throws Exception {
        RepositoryEntry createdRepositoryEntry = createOlatCampusCourseFromStandardTemplate();
        assertRepositoryEntryDisplaynameCreatedCampusCourse(createdRepositoryEntry);
        // Ditto, but read from DB
        assertRepositoryEntryDisplaynameCreatedCampusCourse(loadRepositoryEntryFromDatabase(createdRepositoryEntry));
    }

    private void assertRepositoryEntryDisplaynameCreatedCampusCourse(RepositoryEntry repositoryEntry) {
        assertEquals("Wrong title in RepositoryEntry", course.getTitleToBeDisplayed(), repositoryEntry.getDisplayname());
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkRepositoryEntryDescription() throws Exception {
        RepositoryEntry createdRepositoryEntry = createOlatCampusCourseFromStandardTemplate();
        assertRepositoryEntryDescription(createdRepositoryEntry, course.getSemester().getSemesterNameYear());
        assertRepositoryEntryDescription(createdRepositoryEntry, firstLecturerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertRepositoryEntryDescription(createdRepositoryEntry, secondLecturerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertRepositoryEntryDescription(createdRepositoryEntry, firstLecturerIdentity.getUser().getProperty(UserConstants.LASTNAME, null));
        assertRepositoryEntryDescription(createdRepositoryEntry, secondLecturerIdentity.getUser().getProperty(UserConstants.LASTNAME, null));
        assertRepositoryEntryDescription(createdRepositoryEntry, TEST_EVENT_DESCRIPTION_TEXT);
    }

    private void assertRepositoryEntryDescription(RepositoryEntry createdRepositoryEntry, String attribute) {
        assertRepositoryEntryDescriptionContainsAttribute(createdRepositoryEntry, attribute);
        // Ditto, but read from DB
        assertRepositoryEntryDescriptionContainsAttribute(loadRepositoryEntryFromDatabase(createdRepositoryEntry), attribute);
    }

    private void assertRepositoryEntryDescriptionContainsAttribute(RepositoryEntry repositoryEntry, String attribute) {
        assertTrue("Missing attribute of test-identity (" + attribute + ") in RepositoryEntry-Description", repositoryEntry.getDescription().contains(attribute));
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkCourseOwners() throws Exception {
        RepositoryEntry createdRepositoryEntry = createOlatCampusCourseFromStandardTemplate();

        Group defaultGroup = repositoryService.getDefaultGroup(createdRepositoryEntry);
        List<Identity> ownerIdentities = groupDAO.getMembers(defaultGroup, "owner");

        assertTrue("Missing identity (" + firstLecturerIdentity + ") in owner-group", ownerIdentities.contains(firstLecturerIdentity));
        assertTrue("Missing identity (" + secondLecturerIdentity + ") in owner-group", ownerIdentities.contains(secondLecturerIdentity));
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkCourseRepositoryEntry() throws Exception {
        RepositoryEntry createdRepositoryEntry = createOlatCampusCourseFromStandardTemplate();
        assertNotNull(course.getRepositoryEntry());
        assertEquals(createdRepositoryEntry, course.getRepositoryEntry());
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkCampusGroups() throws Exception {
        assertNull(course.getCampusGroupA());
        assertNull(course.getCampusGroupB());

        RepositoryEntry createdRepositoryEntry = createOlatCampusCourseFromStandardTemplate();

        // Check if campus groups have been created
        BusinessGroup campusGroupA = course.getCampusGroupA();
        BusinessGroup campusGroupB = course.getCampusGroupB();
        assertNotNull(campusGroupA);
        assertNotNull(campusGroupB);

        assertEquals("Wrong name for campus group A", CAMPUS_GROUP_A_DEFAULT_NAME, campusGroupA.getName());
        assertEquals("Wrong name for campus group B", CAMPUS_GROUP_B_DEFAULT_NAME, campusGroupB.getName());

        // Check first synchronization
        List<Identity> coaches = businessGroupService.getMembers(campusGroupA, GroupRoles.coach.name());
        assertEquals(2, coaches.size());
        assertTrue("Missing identity (" + firstLecturerIdentity + ") in coach-group of course-group", coaches.contains(firstLecturerIdentity));
        assertTrue("Missing identity (" + secondLecturerIdentity + ") in coach-group of course-group", coaches.contains(secondLecturerIdentity));

        List<Identity> participants = businessGroupService.getMembers(campusGroupA, GroupRoles.participant.name());
        assertEquals(2, participants.size());
        assertTrue("Missing identity (" + firstParticipantIdentity + ") in participant-group of course-group", participants.contains(firstParticipantIdentity));
        assertTrue("Missing identity (" + secondParticipantIdentity + ") in participant-group of course-group", participants.contains(secondParticipantIdentity));

        coaches = businessGroupService.getMembers(campusGroupB, GroupRoles.coach.name());
        assertEquals(2, coaches.size());
        assertTrue("Missing identity (" + firstLecturerIdentity + ") in coach-group of course-group", coaches.contains(firstLecturerIdentity));
        assertTrue("Missing identity (" + secondLecturerIdentity + ") in coach-group of course-group", coaches.contains(secondLecturerIdentity));

        participants = businessGroupService.getMembers(campusGroupB, GroupRoles.participant.name());
        assertTrue(participants.isEmpty());

        // Check campus learning area
        BGArea campusLearningArea = bgAreaManager.findBGArea(CAMPUS_LEARNING_AREA_NAME, createdRepositoryEntry.getOlatResource());
        assertNotNull(campusLearningArea);

        List<BusinessGroup> businessGroupsOfLearningArea = bgAreaManager.findBusinessGroupsOfArea(campusLearningArea);
        List<Long> keysOfBusinessGroupsOfLearningArea = businessGroupsOfLearningArea.stream().map(BusinessGroupRef::getKey).collect(Collectors.toList());
        assertTrue(keysOfBusinessGroupsOfLearningArea.contains(campusGroupA.getKey()));
        assertTrue(keysOfBusinessGroupsOfLearningArea.contains(campusGroupB.getKey()));
    }

	@Test
	public void testContinueCampusCourse_checkRepositoryEntryNotNullCourseNotNull() throws Exception {
        createOlatCampusCourseFromStandardTemplate();
        RepositoryEntry repositoryEntry = continueOlatCampusCourse();

        assertNotNull("Missing repositoryEntry in CampusCourse return-object", repositoryEntry);
        assertNotNull("Missing Course in CampusCourse return-object", CourseFactory.loadCourse(repositoryEntry));
    }

    @Test
    public void testContinueCampusCourse_checkRepositoryEntryKeyIdenticalToParentOlatCourse() throws Exception {
        RepositoryEntry parentCourseRepositoryEntry = createOlatCampusCourseFromStandardTemplate();
        RepositoryEntry childCourseRepositoryEntry = continueOlatCampusCourse();

        assertEquals(parentCourseRepositoryEntry.getKey(), childCourseRepositoryEntry.getKey());
    }

    @Test
    public void testContinueCampusCourse_checkParentCourse() throws Exception {
        createOlatCampusCourseFromStandardTemplate();
        assertNull(childCourse.getParentCourse());

        continueOlatCampusCourse();
        assertNotNull(childCourse.getParentCourse());
        assertEquals(course.getId(), childCourse.getParentCourse().getId());
    }

    @Test
    public void testContinueOlatCampusCourse_checkChildCourseRepositoryEntry() throws Exception {
        RepositoryEntry repositoryEntry = createOlatCampusCourseFromStandardTemplate();
        continueOlatCampusCourse();

        assertNotNull(childCourse.getRepositoryEntry());
        assertEquals(course.getRepositoryEntry(), childCourse.getRepositoryEntry());
        assertEquals(repositoryEntry, childCourse.getRepositoryEntry());
    }

    @Test
    public void testContinueOlatCampusCourse_checkRepositoryEntryAccess() throws Exception {
        createOlatCampusCourseFromStandardTemplate();
        RepositoryEntry repositoryEntry = continueOlatCampusCourse();

        assertAccess(repositoryEntry);
        // Ditto, but read from DB
        assertAccess(loadRepositoryEntryFromDatabase(repositoryEntry));
    }

    @Test
    public void testContinueOlatCampusCourse_checkRepositoryEntryDisplayname() throws Exception {
        createOlatCampusCourseFromStandardTemplate();
        RepositoryEntry repositoryEntry = continueOlatCampusCourse();

        CampusCourseTO childCampusCourseTO = daoManagerMock.loadCampusCourseTO(childCourse.getId());
        assertRepositoryEntryDisplaynameContinuedCampusCourse(childCampusCourseTO, repositoryEntry);
        // Ditto, but read from DB
        assertRepositoryEntryDisplaynameContinuedCampusCourse(childCampusCourseTO, loadRepositoryEntryFromDatabase(repositoryEntry));
    }

    private void assertRepositoryEntryDisplaynameContinuedCampusCourse(CampusCourseTO parentCampusCourseTO, RepositoryEntry repositoryEntry) {
        assertEquals("Wrong title in RepositoryEntry", parentCampusCourseTO.getTitleToBeDisplayed(), repositoryEntry.getDisplayname());
    }

    @Test
    public void testContinueOlatCampusCourse_checkRepositoryEntryDescription() throws Exception {
        createOlatCampusCourseFromStandardTemplate();
        RepositoryEntry repositoryEntry = continueOlatCampusCourse();

        CampusCourseTO childCampusCourseTO = daoManagerMock.loadCampusCourseTO(childCourse.getId());
        assertRepositoryEntryDescription(repositoryEntry, campusCourseRepositoryEntryDescriptionBuilder.createMultiSemesterTitle(childCampusCourseTO.getTitlesOfCourseAndParentCourses()));
        assertRepositoryEntryDescription(repositoryEntry, firstLecturerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertRepositoryEntryDescription(repositoryEntry, thirdLecturerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertRepositoryEntryDescription(repositoryEntry, firstLecturerIdentity.getUser().getProperty(UserConstants.LASTNAME, null));
        assertRepositoryEntryDescription(repositoryEntry, thirdLecturerIdentity.getUser().getProperty(UserConstants.LASTNAME, null));
        assertRepositoryEntryDescription(repositoryEntry, TEST_EVENT_DESCRIPTION_TEXT);
    }

    @Test
    public void testContinueOlatCampusCourse_checkCourseOwners() throws Exception {
        createOlatCampusCourseFromStandardTemplate();
        RepositoryEntry repositoryEntry = continueOlatCampusCourse();

        Group defaultGroup = repositoryService.getDefaultGroup(repositoryEntry);
        List<Identity> ownerIdentities = groupDAO.getMembers(defaultGroup, "owner");

        assertTrue("Missing identity (" + firstLecturerIdentity + ") in owner-group", ownerIdentities.contains(firstLecturerIdentity));
        assertTrue("Missing identity (" + secondLecturerIdentity + ") in owner-group", ownerIdentities.contains(secondLecturerIdentity));
        assertTrue("Missing identity (" + thirdLecturerIdentity + ") in owner-group", ownerIdentities.contains(thirdLecturerIdentity));
    }

    @Test
    public void testContinueOlatCampusCourse_checkCampusGroups() throws Exception {
        createOlatCampusCourseFromStandardTemplate();
        continueOlatCampusCourse();

        BusinessGroup campusGroupA = childCourse.getCampusGroupA();
        BusinessGroup campusGroupB = childCourse.getCampusGroupB();
        assertNotNull(campusGroupA);
        assertEquals(course.getCampusGroupA(), campusGroupA);
        assertNotNull(campusGroupB);
        assertEquals(course.getCampusGroupB(), campusGroupB);

        // Check synchronization
        List<Identity> coaches = businessGroupService.getMembers(campusGroupA, GroupRoles.coach.name());
        // Old lecturers are not removed
        assertEquals(3, coaches.size());
        assertTrue("Missing identity (" + firstLecturerIdentity + ") in coach-group of course-group", coaches.contains(firstLecturerIdentity));
        assertTrue("Missing identity (" + secondLecturerIdentity + ") in coach-group of course-group", coaches.contains(secondLecturerIdentity));
        assertTrue("Missing identity (" + thirdLecturerIdentity + ") in coach-group of course-group", coaches.contains(thirdLecturerIdentity));

        List<Identity> participants = businessGroupService.getMembers(campusGroupA, GroupRoles.participant.name());
        assertEquals(2, participants.size());
        assertTrue("Missing identity (" + secondParticipantIdentity + ") in participant-group of course-group", participants.contains(secondParticipantIdentity));
        assertTrue("Missing identity (" + thirdParticipantIdentity + ") in participant-group of course-group", participants.contains(thirdParticipantIdentity));

        coaches = businessGroupService.getMembers(campusGroupB, GroupRoles.coach.name());
        assertEquals(3, coaches.size());
        assertTrue("Missing identity (" + firstLecturerIdentity + ") in coach-group of course-group", coaches.contains(firstLecturerIdentity));
        assertTrue("Missing identity (" + secondLecturerIdentity + ") in coach-group of course-group", coaches.contains(secondLecturerIdentity));
        assertTrue("Missing identity (" + thirdLecturerIdentity + ") in coach-group of course-group", coaches.contains(thirdLecturerIdentity));

        participants = businessGroupService.getMembers(campusGroupB, GroupRoles.participant.name());
        assertTrue(participants.isEmpty());
    }

    private RepositoryEntry createOlatCampusCourseFromStandardTemplate() throws Exception {
        RepositoryEntry createdRepositoryEntry = campusCourseCoreServiceMock.createOlatCampusCourseFromStandardTemplate(course.getId(), firstLecturerIdentity);
         dbInstance.flush();
        // Reload courses because of commit and close session (implemented as clear() in databaseTest)
        reloadCourse();
        reloadChildCourse();
        return createdRepositoryEntry;
    }

    private RepositoryEntry continueOlatCampusCourse() throws Exception {
        // Lecturers and students of child course
        Set<Identity> lecturerIdentitiesChildCourse = new HashSet<>();
        lecturerIdentitiesChildCourse.add(firstLecturerIdentity);
        lecturerIdentitiesChildCourse.add(thirdLecturerIdentity);

        Set<Identity> participantIdentitiesChildCourse = new HashSet<>();
        participantIdentitiesChildCourse.add(secondParticipantIdentity);
        participantIdentitiesChildCourse.add(thirdParticipantIdentity);

        // Change dataConverterMock to add changed lecturers and participants
        when(dataConverterMock.convertLecturersToIdentities(any())).thenReturn(lecturerIdentitiesChildCourse);
        when(dataConverterMock.convertDelegateesToIdentities(any())).thenReturn(Collections.emptySet());
        when(dataConverterMock.convertStudentsToIdentities(any())).thenReturn(participantIdentitiesChildCourse);

        RepositoryEntry repositoryEntry = campusCourseCoreServiceMock.continueOlatCampusCourse(childCourse.getId(), course.getId(), firstLecturerIdentity);
        // Reload courses because of commit and close session (implemented as clear() in databaseTest)
        reloadCourse();
        reloadChildCourse();
        return repositoryEntry;
    }

    private RepositoryEntry loadRepositoryEntryFromDatabase(RepositoryEntry repositoryEntry) {
        dbInstance.clear();
        return repositoryEntryDAO.loadByKey(repositoryEntry.getKey());
    }

    private void reloadCourse() {
        course = courseDao.getCourseById(course.getId());
    }

    private void reloadChildCourse() {
        childCourse = courseDao.getCourseById(childCourse.getId());
    }
}
