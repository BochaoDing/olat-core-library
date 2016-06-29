package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.CampusCourseJunitTestHelper;
import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import ch.uzh.campus.service.core.impl.CampusCourseCoreServiceImpl;
import ch.uzh.campus.service.core.impl.CampusCourseFactory;
import ch.uzh.campus.service.core.impl.syncer.CampusCourseCoOwners;
import ch.uzh.campus.service.core.impl.syncer.CampusCourseGroupFinder;
import ch.uzh.campus.service.core.impl.syncer.CampusCourseGroupSynchronizer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author cg
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class CampusCourseCoreServiceImplTest extends OlatTestCase {
   
	private static final OLog log = Tracing.createLoggerFor(CampusCourseCoreServiceImplTest.class);

    private static final String TEST_TITLE_TEXT = "Test Title";
    private static final String TEST_SEMESTER_TEXT = "Herbstsemester 2012";
    private static final String TEST_EVENT_DESCRIPTION_TEXT = "Event description";
    private static final String TEST_COURSE_GROUP_A_NAME = "Campusgroup A";
    private static final String TEST_COURSE_GROUP_B_NAME = "Campusgroup B";

    private CampusCourseCoreService campusCourseCoreService;

    @Autowired
    private BusinessGroupService businessGroupService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RepositoryEntryDAO repositoryEntryDAO;

    @Autowired
    private GroupDAO groupDAO;

    @Autowired
    private BGAreaManager areaManager;
    
    @Autowired
    private DB dbInstance;

    @Autowired
    private CampusCourseCoOwners campusCourseCoOwners;

    @Autowired
    private CampusCourseDescriptionBuilder campusCourseDescriptionBuilder;

    @Autowired
    private OLATResourceManager olatResourceManager;

    @Autowired
    private CampusCourseGroupFinder campusCourseGroupFinder;

    @Autowired
    private CampusCourseCreator campusCourseCreator;

    @Autowired
    private CampusCourseFactory campusCourseFactory;

    private Long sourceResourceableId;
    private Identity ownerIdentity;
    private Identity secondOwnerIdentity;
    private Identity testIdentity;
    private Identity secondTestIdentity;
    private CampusCourseConfiguration campusCourseConfigurationMock;
    private DaoManager daoManagerMock;

    @Before
    public void setup() {
    	ownerIdentity = JunitTestHelper.createAndPersistIdentityAsUser("TestOwner");

        RepositoryEntry sourceRepositoryEntry = JunitTestHelper.deployDemoCourse(ownerIdentity);
        sourceResourceableId = sourceRepositoryEntry.getOlatResource().getResourceableId();
        
        CampusCourseJunitTestHelper.setupCampusCourseGroupForTest(sourceRepositoryEntry, TEST_COURSE_GROUP_A_NAME, businessGroupService);
        dbInstance.commitAndCloseSession();
        CampusCourseJunitTestHelper.setupCampusCourseGroupForTest(sourceRepositoryEntry, TEST_COURSE_GROUP_B_NAME, businessGroupService);
        dbInstance.commitAndCloseSession();

        campusCourseConfigurationMock = mock(CampusCourseConfiguration.class);
        when(campusCourseConfigurationMock.getTemplateCourseResourcableId(null)).thenReturn(sourceResourceableId);
        when(campusCourseConfigurationMock.getCourseGroupAName()).thenReturn(TEST_COURSE_GROUP_A_NAME);
        when(campusCourseConfigurationMock.getCourseGroupBName()).thenReturn(TEST_COURSE_GROUP_B_NAME);
        when(campusCourseConfigurationMock.getTemplateLanguage(null)).thenReturn("DE");

        secondOwnerIdentity = JunitTestHelper.createAndPersistIdentityAsUser("SecondTestOwner");
        testIdentity = JunitTestHelper.createAndPersistIdentityAsUser("TestUser");
        secondTestIdentity = JunitTestHelper.createAndPersistIdentityAsUser("SecondTestUser");

        String semester = "Herbstsemester 2012";
        List<Identity> lecturers = new ArrayList<>();
        lecturers.add(ownerIdentity);
        lecturers.add(secondOwnerIdentity);
        List<Identity> participants = new ArrayList<>();
        participants.add(testIdentity);
        participants.add(secondTestIdentity);
        CampusCourseImportTO campusCourseImportData1 = new CampusCourseImportTO(TEST_TITLE_TEXT, semester, lecturers, participants, TEST_EVENT_DESCRIPTION_TEXT,
                null, null);

        daoManagerMock = mock(DaoManager.class);
        when(daoManagerMock.getSapCampusCourse(anyLong())).thenReturn(campusCourseImportData1);

        CampusCourseGroupSynchronizer campusCourseGroupSynchronizerMock = new CampusCourseGroupSynchronizer(campusCourseConfigurationMock, campusCourseCoOwners, repositoryService, businessGroupService, campusCourseGroupFinder);
        CampusCoursePublisher campusCoursePublisherMock = mock(CampusCoursePublisher.class);
        campusCourseCoreService = new CampusCourseCoreServiceImpl(dbInstance, daoManagerMock, repositoryService, campusCourseFactory, campusCourseDescriptionBuilder, campusCourseCreator, campusCoursePublisherMock, campusCourseConfigurationMock, campusCourseGroupSynchronizerMock, olatResourceManager);
    }

	@After
    public void tearDown() throws Exception {
		dbInstance.rollback();
        try {
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("tearDown failed: ", e);
        }
    }

    private CampusCourse createCampusCourseTestObject() {
        // Create campus course from a template
        CampusCourse campusCourse = campusCourseCoreService.createCampusCourseFromTemplate(null, 100L, ownerIdentity);
        dbInstance.flush();
        return campusCourse;
    }
    
    @Test
    public void createCampusCourse() {
        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
        assertNotNull("campusCourse is null, could not create course", createdCampusCourseTestObject);
        assertNotNull("Missing repositoryEntry in CampusCourse return-object", createdCampusCourseTestObject.getRepositoryEntry());
        assertNotNull("Missing Course in CampusCourse return-object", createdCampusCourseTestObject.getCourse());
    }
   
    @Test
    public void createCampusCourse_CheckAccess() {
        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
        assertAccess(createdCampusCourseTestObject.getRepositoryEntry());
        // Ditto, but read from DB
        assertAccess(loadRepositoryEntryFromDatabase(createdCampusCourseTestObject.getRepositoryEntry().getKey()));
    }

    private void assertAccess(RepositoryEntry repositoryEntry) {
        assertEquals("CampusCourse Access must be 'BARG'", RepositoryEntry.ACC_USERS_GUESTS, repositoryEntry.getAccess());
    }

    @Test
    public void createCampusCourse_CheckTitle() {
        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
        assertTitle(createdCampusCourseTestObject.getRepositoryEntry());
        // Ditto, but read from DB
        assertTitle(loadRepositoryEntryFromDatabase(createdCampusCourseTestObject.getRepositoryEntry().getKey()));
    }

    private void assertTitle(RepositoryEntry repositoryEntry) {
        assertEquals("Wrong title in RepositoryEntry", TEST_TITLE_TEXT, repositoryEntry.getDisplayname());
    }
    
    @Test
    public void createCampusCourse_CheckDescription() {
        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
        System.out.println("### TEST createdCampusCourseTestObject.getRepositoryEntry().getDescription()="
                + createdCampusCourseTestObject.getRepositoryEntry().getDescription());
        // Description Soll :
        // Campuskurs Herbstsemester 2012
        // Dozierende : Firstname Lastname, Firstname Lastname
        // Lehrveranstaltungsinhalt : Event description
        assertTextInDescription(createdCampusCourseTestObject, TEST_SEMESTER_TEXT);
        assertTextInDescription(createdCampusCourseTestObject, ownerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertTextInDescription(createdCampusCourseTestObject, secondOwnerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertTextInDescription(createdCampusCourseTestObject, ownerIdentity.getUser().getProperty(UserConstants.LASTNAME, null));
        assertTextInDescription(createdCampusCourseTestObject, secondOwnerIdentity.getUser().getProperty(UserConstants.LASTNAME, null));
        assertTextInDescription(createdCampusCourseTestObject, TEST_EVENT_DESCRIPTION_TEXT);
    }

    private void assertTextInDescription(CampusCourse createdCampusCourseTestObject, String attribute) {
        checkTextInDescription(createdCampusCourseTestObject.getRepositoryEntry(), attribute);
        // Ditto, but read from DB
        checkTextInDescription(loadRepositoryEntryFromDatabase(createdCampusCourseTestObject.getRepositoryEntry().getKey()), attribute);
    }

    private void checkTextInDescription(RepositoryEntry repositoryEntry, String attribute) {
        assertTrue("Missing attribute of test-identity (" + attribute + ") in RepositoryEntry-Description", repositoryEntry.getDescription().contains(attribute));
    }

    @Test
    public void createCampusCourse_CheckOwners() {
        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
        
        Group defaultGroup = repositoryService.getDefaultGroup( createdCampusCourseTestObject.getRepositoryEntry());
        List<Identity> ownerIdentities = groupDAO.getMembers(defaultGroup, "owner");
        
        assertTrue("Missing identity (" + ownerIdentity + ") in owner-group", ownerIdentities.contains(ownerIdentity));
        assertTrue("Missing identity (" + secondOwnerIdentity + ")in owner-group", ownerIdentities.contains(secondOwnerIdentity));
    }
    
    @Test
    public void createCampusCourse_CheckCourseGroup() {
        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
        BusinessGroup campusCourseGroup = campusCourseGroupFinder.lookupCampusGroup(createdCampusCourseTestObject.getCourse(), campusCourseConfigurationMock.getCourseGroupAName());

        List<Identity> coaches = businessGroupService.getMembers(campusCourseGroup, GroupRoles.coach.name());
        
        assertTrue("Missing identity (" + ownerIdentity + ") in owner-group of course-group", coaches.contains(ownerIdentity));
        assertTrue("Missing identity (" + secondOwnerIdentity + ")in owner-group of course-group", coaches.contains(secondOwnerIdentity));
        
        List<Identity> participants = businessGroupService.getMembers(campusCourseGroup, GroupRoles.participant.name());

        assertTrue("Missing identity (" + testIdentity + ") in participant-group of course-group", participants.contains(testIdentity));
        assertTrue("Missing identity (" + secondTestIdentity + ")in participant-group of course-group", participants.contains(secondTestIdentity));

    }
    
    @Test
    public void createCampusCourse_checkArea() {
    	//change the test setup: to do not use a template course
    	when(campusCourseConfigurationMock.getTemplateCourseResourcableId(null)).thenReturn(null);
    	
    	Identity testIdentity_2 = JunitTestHelper.createAndPersistIdentityAsUser("test_user_2");
    	Identity testIdentity_3 = JunitTestHelper.createAndPersistIdentityAsUser("test_user_3");
    	
    	String semester = "Herbstsemester 2016";
        List<Identity> lecturers = new ArrayList<>();
        lecturers.add(testIdentity_2);       
        List<Identity> participants = new ArrayList<>();
        participants.add(testIdentity_3);

        CampusCourseImportTO campusCourseImportData = new CampusCourseImportTO(TEST_TITLE_TEXT, semester, lecturers, participants, TEST_EVENT_DESCRIPTION_TEXT,
                null, null);

        when(daoManagerMock.getSapCampusCourse(100L)).thenReturn(campusCourseImportData);
        
        //uses an existing course
        CampusCourse campusCourse = campusCourseCoreService.createCampusCourseFromTemplate(sourceResourceableId, 100L, ownerIdentity);
        
        BusinessGroup campusCourseGroup = campusCourseGroupFinder.lookupCampusGroup(campusCourse.getCourse(), campusCourseConfigurationMock.getCourseGroupAName());
        
        List<Identity> coaches = businessGroupService.getMembers(campusCourseGroup, GroupRoles.coach.name());        
        assertTrue("Missing identity (" + ownerIdentity + ") in owner-group of course-group", coaches.contains(testIdentity_2));
        
        List<Identity> readParticipants = businessGroupService.getMembers(campusCourseGroup, GroupRoles.participant.name());
        assertTrue("Missing identity (" + secondOwnerIdentity + ")in owner-group of course-group", readParticipants.contains(testIdentity_3));
        
        List<BGArea> areas = areaManager.findBGAreasInContext(campusCourse.getRepositoryEntry().getOlatResource());
        assertEquals(1,areas.size());
    }
    
    //TODO: olatng
    @Ignore
    @Test
    public void continueCampusCourse() {
    	fail("Not yet implemented");
    }

    private RepositoryEntry loadRepositoryEntryFromDatabase(Long key) {
        dbInstance.clear();
        return repositoryEntryDAO.loadByKey(key);
    }

}
