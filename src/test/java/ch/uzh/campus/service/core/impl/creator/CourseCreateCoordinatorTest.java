package ch.uzh.campus.service.core.impl.creator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;

import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import ch.uzh.campus.CampusConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.service.CampusCourse;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author cg
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class CourseCreateCoordinatorTest extends OlatTestCase {
   
	private static final OLog log = Tracing.createLoggerFor(CourseCreateCoordinatorTest.class);

    private static final String TEST_TITLE_TEXT = "Test Title";
    private static final String TEST_SEMESTER_TEXT = "Herbstsemester 2012";
    private static final String TEST_EVENT_DESCRIPTION_TEXT = "Event description";
    private static final String TEST_COURSE_GROUP_A_NAME = "Campusgroup A";
    private static final String TEST_COURSE_GROUP_B_NAME = "Campusgroup B";
    private static final Long TEST_RESOURCEABLE_ID = 1234L;

    @Autowired
    private CourseCreateCoordinator courseCreateCoordinator;
    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private BusinessGroupService businessGroupService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private GroupDAO groupDAO;

    private Long sourceResourceableId;
    private ICourse sourceCourse;
    private RepositoryEntry sourceRepositoryEntry;
    private String ownerName = "TestOwner";
    private Identity ownerIdentity;
    private String ownerNameSecond = "SecondTestOwner";
    private Identity secondOwnerIdentity;
    private String testUserName = "TestUser";
    private Identity testIdentity;
    private String secondTestUserName = "SecondTestUser";
    private Identity secondTestIdentity;

    private CampusConfiguration campusConfigurationMock;
    
    @Before
    public void setup() {
    	ownerIdentity = JunitTestHelper.createAndPersistIdentityAsUser(ownerName);
    	
        sourceRepositoryEntry = JunitTestHelper.deployDemoCourse(ownerIdentity);
        sourceResourceableId = sourceRepositoryEntry.getOlatResource().getResourceableId();
                
        sourceCourse = CourseFactory.loadCourse(sourceResourceableId);
        DBFactory.getInstance().closeSession();   
        
        setupCampusCourseGroupForTest(sourceRepositoryEntry, TEST_COURSE_GROUP_A_NAME);
        setupCampusCourseGroupForTest(sourceRepositoryEntry, TEST_COURSE_GROUP_B_NAME);
        // DBFactory.getInstance().closeSession();

        campusConfigurationMock = mock(CampusConfiguration.class);
        when(campusConfigurationMock.getTemplateCourseResourcableId(null)).thenReturn(sourceResourceableId);
        when(campusConfigurationMock.getCourseGroupAName()).thenReturn(TEST_COURSE_GROUP_A_NAME);
        when(campusConfigurationMock.getCourseGroupBName()).thenReturn(TEST_COURSE_GROUP_B_NAME);
        when(campusConfigurationMock.getTemplateLanguage(null)).thenReturn("DE");
        courseCreateCoordinator.campusConfiguration = campusConfigurationMock;
        //TODO: olatng
        //courseCreateCoordinator.campusCourseGroupSynchronizer.setCampusConfiguration(campusConfigurationMock);

        CoursePublisher coursePublisherMock = mock(CoursePublisher.class);
        courseCreateCoordinator.coursePublisher = coursePublisherMock;

        
        secondOwnerIdentity = JunitTestHelper.createAndPersistIdentityAsUser(ownerNameSecond);
        testIdentity = JunitTestHelper.createAndPersistIdentityAsUser(testUserName);
        secondTestIdentity = JunitTestHelper.createAndPersistIdentityAsUser(secondTestUserName);
    }

    private void setupCampusCourseGroupForTest(RepositoryEntry repositoryEntry, String testCourseGroupAName) {		
    	businessGroupService.createBusinessGroup(null, testCourseGroupAName, null, 0, 10,false, false, repositoryEntry);
	}

	@After
    public void tearDown() throws Exception {
        // TODO: Does not cleanup Demo-course because other Test which use Demo-Course too, will be have failures
        // remove demo course on file system
        // CourseFactory.deleteCourse(course);
        try {
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("tearDown failed: ", e);
        }
    }

    private CampusCourse createCampusCourseTestObject() {
        String semester = "Herbstsemester 2012";
        List<Identity> lecturers = new ArrayList<Identity>();
        lecturers.add(ownerIdentity);
        lecturers.add(secondOwnerIdentity);
        List<Identity> participants = new ArrayList<Identity>();
        participants.add(testIdentity);
        participants.add(secondTestIdentity);
        CampusCourseImportTO campusCourseImportData = new CampusCourseImportTO(TEST_TITLE_TEXT, semester, lecturers, participants, TEST_EVENT_DESCRIPTION_TEXT,
                TEST_RESOURCEABLE_ID, null);
        CampusCourse campusCourse = courseCreateCoordinator.createCampusCourse(null, campusCourseImportData, ownerIdentity);
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
        assertTrue("CampusCourse Access must be 'BARG'", createdCampusCourseTestObject.getRepositoryEntry().getAccess() == RepositoryEntry.ACC_USERS_GUESTS);
    }

    /*
    @Test
    public void createCampusCourse_CheckTitle() {
        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
        assertEquals("Wrong title in RepositoryEntry", TEST_TITLE_TEXT, createdCampusCourseTestObject.getRepositoryEntry().getDisplayname());
        assertEquals("Wrong title in Course", TEST_TITLE_TEXT, createdCampusCourseTestObject.getCourse().getCourseTitle());
    }*/
    
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
        assertTrue("Missing attribute of test-identity (" + attribute + ") in RepositoryEntry-Description", createdCampusCourseTestObject.getRepositoryEntry()
                .getDescription().indexOf(attribute) != -1);
    }
    
    /*
    @Test
    public void createCampusCourse_CheckOwners() {
        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
        
        Group defaultGroup = repositoryService.getDefaultGroup( createdCampusCourseTestObject.getRepositoryEntry());
        List<Identity> ownerIdentities = groupDAO.getMembers(defaultGroup, "owner");
        
        assertTrue("Missing identity (" + ownerIdentity + ") in owner-group", ownerIdentities.contains(ownerIdentity));
        assertTrue("Missing identity (" + secondOwnerIdentity + ")in owner-group", ownerIdentities.contains(secondOwnerIdentity));
    }
    */
    /*
    @Test
    public void createCampusCourse_CheckCourseGroup() {
        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
        BusinessGroup campusCourseGroup = CampusGroupHelper.lookupCampusGroup(createdCampusCourseTestObject.getCourse(), campusConfigurationMock.getCourseGroupAName());

        assertTrue("Missing identity (" + ownerIdentity + ") in owner-group of course-group",
                baseSecurity.isIdentityInSecurityGroup(ownerIdentity, campusCourseGroup.getOwnerGroup()));
        assertTrue("Missing identity (" + secondOwnerIdentity + ")in owner-group of course-group",
                baseSecurity.isIdentityInSecurityGroup(secondOwnerIdentity, campusCourseGroup.getOwnerGroup()));

        assertTrue("Missing identity (" + testIdentity + ") in participant-group of course-group",
                baseSecurity.isIdentityInSecurityGroup(testIdentity, campusCourseGroup.getPartipiciantGroup()));
        assertTrue("Missing identity (" + secondTestIdentity + ")in participant-group of course-group",
                baseSecurity.isIdentityInSecurityGroup(secondTestIdentity, campusCourseGroup.getPartipiciantGroup()));

    }*/

}
