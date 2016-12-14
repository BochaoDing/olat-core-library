package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseException;
import ch.uzh.campus.CampusCourseJunitTestHelper;
import ch.uzh.campus.CampusCourseTestCase;
import ch.uzh.campus.data.*;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import ch.uzh.campus.service.core.impl.CampusCourseCoreServiceImpl;
import ch.uzh.campus.service.core.impl.OlatCampusCourseProvider;
import ch.uzh.campus.service.core.impl.syncer.CampusCourseDefaultCoOwners;
import ch.uzh.campus.service.core.impl.syncer.CampusCourseRepositoryEntrySynchronizer;
import ch.uzh.campus.service.core.impl.syncer.CampusGroupsSynchronizer;
import ch.uzh.campus.service.data.OlatCampusCourse;
import ch.uzh.campus.service.data.SapCampusCourseTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
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
    private OlatCampusCourseProvider olatCampusCourseProvider;

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
                daoManagerMock, repositoryService, olatCampusCourseProvider,
                olatCampusCourseCreator,
                campusCoursePublisherMock, campusGroupsCreator, campusCourseConfigurationMock,
                campusGroupsSynchronizer, campusCourseRepositoryEntrySynchronizer, olatResourceManager, businessGroupService, campusCourseDefaultCoOwners);
    }

	@After
    public void tearDown() throws Exception {
		dbInstance.rollback();
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkRepositoryEntryNotNullCourseNotNull() throws Exception {
        OlatCampusCourse createdOlatCampusCourse = createOlatCampusCourseFromStandardTemplate();
        assertNotNull("Missing repositoryEntry in CampusCourse return-object", createdOlatCampusCourse.getRepositoryEntry());
        assertNotNull("Missing Course in CampusCourse return-object", createdOlatCampusCourse.getCourse());
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkRepositoryEntryAccess() throws Exception {
        OlatCampusCourse createdOlatCampusCourse = createOlatCampusCourseFromStandardTemplate();
        assertAccess(createdOlatCampusCourse.getRepositoryEntry());
        // Ditto, but read from DB
        assertAccess(loadRepositoryEntryFromDatabase(createdOlatCampusCourse.getRepositoryEntry()));
    }

    private void assertAccess(RepositoryEntry repositoryEntry) {
        assertEquals("CampusCourse Access must be 'BARG'", RepositoryEntry.ACC_USERS_GUESTS, repositoryEntry.getAccess());
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkRepositoryEntryDisplayname() throws Exception {
        OlatCampusCourse createdOlatCampusCourse = createOlatCampusCourseFromStandardTemplate();
        assertRepositoryEntryDisplaynameCreatedCampusCourse(createdOlatCampusCourse.getRepositoryEntry());
        // Ditto, but read from DB
        assertRepositoryEntryDisplaynameCreatedCampusCourse(loadRepositoryEntryFromDatabase(createdOlatCampusCourse.getRepositoryEntry()));
    }

    private void assertRepositoryEntryDisplaynameCreatedCampusCourse(RepositoryEntry repositoryEntry) {
        assertEquals("Wrong title in RepositoryEntry", course.getTitleToBeDisplayed(), repositoryEntry.getDisplayname());
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkRepositoryEntryDescription() throws Exception {
        OlatCampusCourse createdOlatCampusCourse = createOlatCampusCourseFromStandardTemplate();
        assertRepositoryEntryDescription(createdOlatCampusCourse, course.getSemester().getSemesterNameYear());
        assertRepositoryEntryDescription(createdOlatCampusCourse, firstLecturerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertRepositoryEntryDescription(createdOlatCampusCourse, secondLecturerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertRepositoryEntryDescription(createdOlatCampusCourse, firstLecturerIdentity.getUser().getProperty(UserConstants.LASTNAME, null));
        assertRepositoryEntryDescription(createdOlatCampusCourse, secondLecturerIdentity.getUser().getProperty(UserConstants.LASTNAME, null));
        assertRepositoryEntryDescription(createdOlatCampusCourse, TEST_EVENT_DESCRIPTION_TEXT);
    }

    private void assertRepositoryEntryDescription(OlatCampusCourse createdOlatCampusCourse, String attribute) {
        assertRepositoryEntryDescriptionContainsAttribute(createdOlatCampusCourse.getRepositoryEntry(), attribute);
        // Ditto, but read from DB
        assertRepositoryEntryDescriptionContainsAttribute(loadRepositoryEntryFromDatabase(createdOlatCampusCourse.getRepositoryEntry()), attribute);
    }

    private void assertRepositoryEntryDescriptionContainsAttribute(RepositoryEntry repositoryEntry, String attribute) {
        assertTrue("Missing attribute of test-identity (" + attribute + ") in RepositoryEntry-Description", repositoryEntry.getDescription().contains(attribute));
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkCourseOwners() throws Exception {
        OlatCampusCourse createdOlatCampusCourse = createOlatCampusCourseFromStandardTemplate();

        Group defaultGroup = repositoryService.getDefaultGroup(createdOlatCampusCourse.getRepositoryEntry());
        List<Identity> ownerIdentities = groupDAO.getMembers(defaultGroup, "owner");

        assertTrue("Missing identity (" + firstLecturerIdentity + ") in owner-group", ownerIdentities.contains(firstLecturerIdentity));
        assertTrue("Missing identity (" + secondLecturerIdentity + ") in owner-group", ownerIdentities.contains(secondLecturerIdentity));
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkCourseOlatResource() throws Exception {
        OlatCampusCourse createdOlatCampusCourse = createOlatCampusCourseFromStandardTemplate();
        assertNotNull(course.getOlatResource());
        assertEquals(createdOlatCampusCourse.getRepositoryEntry().getOlatResource(), course.getOlatResource());
    }

    @Test
    public void testCreateOlatCampusCourseFromStandardTemplate_checkCampusGroups() throws Exception {
        assertNull(course.getCampusGroupA());
        assertNull(course.getCampusGroupB());

        OlatCampusCourse createdOlatCampusCourse = createOlatCampusCourseFromStandardTemplate();

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
        BGArea campusLearningArea = bgAreaManager.findBGArea(CAMPUS_LEARNING_AREA_NAME, createdOlatCampusCourse.getRepositoryEntry().getOlatResource());
        assertNotNull(campusLearningArea);

        List<BusinessGroup> businessGroupsOfLearningArea = bgAreaManager.findBusinessGroupsOfArea(campusLearningArea);
        List<Long> keysOfBusinessGroupsOfLearningArea = businessGroupsOfLearningArea.stream().map(BusinessGroupRef::getKey).collect(Collectors.toList());
        assertTrue(keysOfBusinessGroupsOfLearningArea.contains(campusGroupA.getKey()));
        assertTrue(keysOfBusinessGroupsOfLearningArea.contains(campusGroupB.getKey()));
    }

	@Test
	public void testContinueCampusCourse_checkRepositoryEntryNotNullCourseNotNull() throws Exception {
        createOlatCampusCourseFromStandardTemplate();
        OlatCampusCourse continuedOlatCampusCourse = continueOlatCampusCourse();

        assertNotNull("Missing repositoryEntry in CampusCourse return-object", continuedOlatCampusCourse.getRepositoryEntry());
        assertNotNull("Missing Course in CampusCourse return-object", continuedOlatCampusCourse.getCourse());
    }

    @Test
    public void testContinueCampusCourse_checkRepositoryEntryKeyOlatResourceIdenticalToParentOlatCourse() throws Exception {
        OlatCampusCourse parentOlatCampusCourse = createOlatCampusCourseFromStandardTemplate();
        OlatCampusCourse continuedOlatCampusCourse = continueOlatCampusCourse();

        assertEquals(parentOlatCampusCourse.getRepositoryEntry().getKey(), continuedOlatCampusCourse.getRepositoryEntry().getKey());
        assertEquals(parentOlatCampusCourse.getRepositoryEntry().getResourceableId(), continuedOlatCampusCourse.getRepositoryEntry().getResourceableId());
    }

    @Test
    public void testContinueCampusCourse_checkParentCourse() throws Exception {
        createOlatCampusCourseFromStandardTemplate();
        assertNull(childCourse.getParentCourse());

        continueOlatCampusCourse();
        assertNotNull(childCourse.getParentCourse());
        assertEquals(course, childCourse.getParentCourse());
    }

    @Test
    public void testContinueOlatCampusCourse_checkChildCourseOlatResource() throws Exception {
        OlatCampusCourse createdOlatCampusCourse = createOlatCampusCourseFromStandardTemplate();
        continueOlatCampusCourse();

        assertNotNull(childCourse.getOlatResource());
        assertEquals(course.getOlatResource(), childCourse.getOlatResource());
        assertEquals(createdOlatCampusCourse.getRepositoryEntry().getOlatResource(), childCourse.getOlatResource());
    }

    @Test
    public void testContinueOlatCampusCourse_checkRepositoryEntryAccess() throws Exception {
        createOlatCampusCourseFromStandardTemplate();
        OlatCampusCourse continuedOlatCampusCourse = continueOlatCampusCourse();

        assertAccess(continuedOlatCampusCourse.getRepositoryEntry());
        // Ditto, but read from DB
        assertAccess(loadRepositoryEntryFromDatabase(continuedOlatCampusCourse.getRepositoryEntry()));
    }

    @Test
    public void testContinueOlatCampusCourse_checkRepositoryEntryDisplayname() throws Exception {
        createOlatCampusCourseFromStandardTemplate();
        OlatCampusCourse continuedOlatCampusCourse = continueOlatCampusCourse();

        SapCampusCourseTO childSapCampusCourseTO = daoManagerMock.loadSapCampusCourseTO(childCourse.getId());
        assertRepositoryEntryDisplaynameContinuedCampusCourse(childSapCampusCourseTO, continuedOlatCampusCourse.getRepositoryEntry());
        // Ditto, but read from DB
        assertRepositoryEntryDisplaynameContinuedCampusCourse(childSapCampusCourseTO, continuedOlatCampusCourse.getRepositoryEntry());
    }

    private void assertRepositoryEntryDisplaynameContinuedCampusCourse(SapCampusCourseTO childSapCampusCourseTO, RepositoryEntry childRepositoryEntry) {
        assertEquals("Wrong title in RepositoryEntry", childSapCampusCourseTO.getTitleToBeDisplayed(), childRepositoryEntry.getDisplayname());
    }

    @Test
    public void testContinueOlatCampusCourse_checkRepositoryEntryDescription() throws Exception {
        createOlatCampusCourseFromStandardTemplate();
        OlatCampusCourse continuedOlatCampusCourse = continueOlatCampusCourse();

        SapCampusCourseTO childSapCampusCourseTO = daoManagerMock.loadSapCampusCourseTO(childCourse.getId());
        assertRepositoryEntryDescription(continuedOlatCampusCourse, campusCourseRepositoryEntryDescriptionBuilder.createMultiSemesterTitle(childSapCampusCourseTO.getTitlesOfCourseAndParentCourses()));
        assertRepositoryEntryDescription(continuedOlatCampusCourse, firstLecturerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertRepositoryEntryDescription(continuedOlatCampusCourse, thirdLecturerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertRepositoryEntryDescription(continuedOlatCampusCourse, firstLecturerIdentity.getUser().getProperty(UserConstants.LASTNAME, null));
        assertRepositoryEntryDescription(continuedOlatCampusCourse, thirdLecturerIdentity.getUser().getProperty(UserConstants.LASTNAME, null));
        assertRepositoryEntryDescription(continuedOlatCampusCourse, TEST_EVENT_DESCRIPTION_TEXT);
    }

    @Test
    public void testContinueOlatCampusCourse_checkCourseOwners() throws Exception {
        createOlatCampusCourseFromStandardTemplate();
        OlatCampusCourse continuedOlatCampusCourse = continueOlatCampusCourse();

        Group defaultGroup = repositoryService.getDefaultGroup(continuedOlatCampusCourse.getRepositoryEntry());
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

    private OlatCampusCourse createOlatCampusCourseFromStandardTemplate() throws Exception {
        OlatCampusCourse createdOlatCampusCourse = campusCourseCoreServiceMock.createOlatCampusCourseFromStandardTemplate(course.getId(), firstLecturerIdentity);
        dbInstance.flush();
        return createdOlatCampusCourse;
    }

    private OlatCampusCourse continueOlatCampusCourse() throws Exception {
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

        return campusCourseCoreServiceMock.continueOlatCampusCourse(childCourse.getId(), course.getId(), firstLecturerIdentity);
    }

    private RepositoryEntry loadRepositoryEntryFromDatabase(RepositoryEntry repositoryEntry) {
        dbInstance.clear();
        return repositoryEntryDAO.loadByKey(repositoryEntry.getKey());
    }
}
