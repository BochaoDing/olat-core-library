package ch.uzh.campus.service.core.impl.syncer;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.CampusCourseJunitTestHelper;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedGroupStatistic;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class CampusCourseGroupSynchronizerTest extends OlatTestCase {
	
	private static final String TEST_COURSE_GROUP_A_NAME = "Campusgroup A SynchronizeTest";
    private static final String TEST_COURSE_GROUP_B_NAME = "Campusgroup B SynchronizeTest";
    private static final Long TEST_RESOURCEABLE_ID = 1234L;

    @Autowired
    CampusCourseGroupSynchronizer courseGroupSynchronizerTestObject;

    @Autowired
    private BusinessGroupService businessGroupService;
    
    @Autowired
    RepositoryService repositoryService;

    @Autowired
    CampusCourseGroupFinder campusCourseGroupFinder;

    @Autowired
    private DB dbInstance;

    private Identity ownerIdentity;
    private Identity secondOwnerIdentity;
    private Identity firstTestIdentity;
    private Identity secondTestIdentity;
    private Identity thirdTestIdentity;
    private ICourse course;
    private BusinessGroup campusCourseGroup; //GroupA
    private Identity firstCoOwnerIdentity;
    private Identity secondCoOwnerIdentity;
    private CampusCourse campusCourseMock;
    private CampusCourseConfiguration campusCourseConfigurationMock;
    private RepositoryEntry sourceRepositoryEntry;

    @Before
    public void setup() {
    	 // Setup Test Identities
        ownerIdentity = JunitTestHelper.createAndPersistIdentityAsUser("TestOwner"); //course owner
        secondOwnerIdentity = JunitTestHelper.createAndPersistIdentityAsUser("SecondTestOwner");

        firstTestIdentity = JunitTestHelper.createAndPersistIdentityAsUser("FirstTestUser");
        secondTestIdentity = JunitTestHelper.createAndPersistIdentityAsUser("SecondTestUser");
        thirdTestIdentity = JunitTestHelper.createAndPersistIdentityAsUser("ThirdTestUser");

        firstCoOwnerIdentity = JunitTestHelper.createAndPersistIdentityAsUser("co_owner1");
        secondCoOwnerIdentity = JunitTestHelper.createAndPersistIdentityAsUser("co_owner2");
        List<Identity> coOwnerList = new ArrayList<>();
        coOwnerList.add(firstCoOwnerIdentity);
        coOwnerList.add(secondCoOwnerIdentity);
        
        // Setup Test Configuration
        campusCourseConfigurationMock = mock(CampusCourseConfiguration.class);
        when(campusCourseConfigurationMock.getCourseGroupAName()).thenReturn(TEST_COURSE_GROUP_A_NAME);
        when(campusCourseConfigurationMock.getCourseGroupBName()).thenReturn(TEST_COURSE_GROUP_B_NAME);

        courseGroupSynchronizerTestObject = new CampusCourseGroupSynchronizer(campusCourseConfigurationMock, mock(CampusCourseCoOwners.class), repositoryService, businessGroupService, campusCourseGroupFinder);
        when(courseGroupSynchronizerTestObject.getCampusCourseCoOwners().getDefaultCoOwners()).thenReturn(coOwnerList);

        // Setup test-course
        sourceRepositoryEntry = JunitTestHelper.deployDemoCourse(ownerIdentity);
        course = CourseFactory.loadCourse(sourceRepositoryEntry.getOlatResource().getResourceableId());

        // Setup campus-groups
        CampusCourseJunitTestHelper.setupCampusCourseGroupForTest(sourceRepositoryEntry, TEST_COURSE_GROUP_A_NAME, businessGroupService);
        CampusCourseJunitTestHelper.setupCampusCourseGroupForTest(sourceRepositoryEntry, TEST_COURSE_GROUP_B_NAME, businessGroupService);
        dbInstance.flush();
        campusCourseGroup = campusCourseGroupFinder.lookupCampusGroup(course, campusCourseConfigurationMock.getCourseGroupAName());
        
        campusCourseMock = mock(CampusCourse.class);       
        when(campusCourseMock.getRepositoryEntry()).thenReturn(sourceRepositoryEntry);
        when(campusCourseMock.getCourse()).thenReturn(course);
    }

    @After
    public void teardown() {    	
    	dbInstance.rollback();
    }

    @Test
    public void addAllLecturesAsOwner() {
        // Exercise
        courseGroupSynchronizerTestObject.addAllLecturesAsOwner(campusCourseMock, getTestLecturersWithDuplicateEntry());
        List<Identity> ownerIdentities =  repositoryService.getMembers(sourceRepositoryEntry, GroupRoles.owner.name());        
        assertEquals("Wrong number of owners", 2, ownerIdentities.size());
       
        assertTrue("Missing identity (" + ownerIdentity + ") in owner-group", ownerIdentities.contains(ownerIdentity));
        assertTrue("Missing identity (" + secondOwnerIdentity + ")in owner-group", ownerIdentities.contains(secondOwnerIdentity));
    }

    @Test
    public void addDefaultCoOwnersAsOwner() {
        // Exercise
        courseGroupSynchronizerTestObject.addDefaultCoOwnersAsOwner(campusCourseMock);
        
        List<Identity> ownerIdentities =  repositoryService.getMembers(sourceRepositoryEntry, GroupRoles.owner.name());        
        assertEquals("Wrong number of owners", 3, ownerIdentities.size()); //the third owner is the one that created the course
        
        assertTrue("Missing identity (" + ownerIdentity + ") in owner-group", ownerIdentities.contains(firstCoOwnerIdentity));
        assertTrue("Missing identity (" + secondOwnerIdentity + ")in owner-group", ownerIdentities.contains(secondCoOwnerIdentity));
    }

    /**
     * Add two lecturers (includes an identity twice to check duplicate handling) and no participants. This is the initialize process when owner-list is empty at the
     * beginning.
     */
    @Test
    public void synchronizeCourseGroups_AddLectures_CheckAddedStatisticAndMembers() {
        CampusCourseImportTO campusCourseImportData = new CampusCourseImportTO("Group_Test", "HS-2012", getTestLecturersWithDuplicateEntry(), null, new ArrayList<>(),
                "Group_Test", TEST_RESOURCEABLE_ID, null, null, null);

        SynchronizedGroupStatistic statistic = courseGroupSynchronizerTestObject.synchronizeCourseGroups(campusCourseMock, campusCourseImportData);
        
        // 1. assert statistic
        assertEquals("Wrong number of added identity in statistic", 2, statistic.getOwnerGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getOwnerGroupStatistic().getRemovedStatistic());
        assertEquals("Wrong number of added identity in statistic", 0, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getParticipantGroupStatistic().getRemovedStatistic());
        
        BusinessGroup campusCourseGroup = campusCourseGroupFinder.lookupCampusGroup(course, campusCourseConfigurationMock.getCourseGroupAName());
        List<Identity> groupCoaches = businessGroupService.getMembers(campusCourseGroup, GroupRoles.coach.name());
        // 2. assert members
        assertEquals("Wrong number of owners", 2, groupCoaches.size());
        assertTrue("Missing identity (" + ownerIdentity + ") as coach of course-group", groupCoaches.contains(ownerIdentity));
        assertTrue("Missing identity (" + secondOwnerIdentity + ") as coach of course-group", groupCoaches.contains(secondOwnerIdentity));
                
    }

    /**
     * Add two participants (includes an identity twice to check duplicate handling) and no lecturer. This is the initialize process when participant-list is empty at the
     * beginning.
     */
    @Test
    public void synchronizeCourseGroups_AddParticipants_CheckAddedStatisticAndMembers() {
        CampusCourseImportTO campusCourseImportData = new CampusCourseImportTO("Group_Test", "HS-2012", new ArrayList<>(), null,
                getTestParticipantsWithDuplicateEntry(), "Group_Test", TEST_RESOURCEABLE_ID, null, null, null);

        // no owner-identities, two participants (testIdentity, secondTestIdentity)
        SynchronizedGroupStatistic statistic = courseGroupSynchronizerTestObject.synchronizeCourseGroups(campusCourseMock, campusCourseImportData);

        // 1. assert statistic
        assertEquals("Wrong number of added identity in statistic", 0, statistic.getOwnerGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getOwnerGroupStatistic().getRemovedStatistic());
                
        assertEquals("Wrong number of added identity in statistic", 2, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getParticipantGroupStatistic().getRemovedStatistic());
                
        //BusinessGroup campusCourseGroup = CampusGroupHelper.lookupCampusGroup(course, campusConfigurationMock.getCourseGroupAName());
        List<Identity> groupParticipants = businessGroupService.getMembers(campusCourseGroup, GroupRoles.participant.name());
        // 2. assert members
        assertEquals("Wrong number of owners", 0, businessGroupService.getMembers(campusCourseGroup, GroupRoles.coach.name()).size());
        assertEquals("Wrong number of participants", 2, groupParticipants.size());
        assertTrue("Missing identity (" + firstTestIdentity + ") as participant of course-group", groupParticipants.contains(firstTestIdentity));
        assertTrue("Missing identity (" + secondTestIdentity + ") as participant of course-group", groupParticipants.contains(secondTestIdentity));
    }

    private List<Identity> getTestParticipantsWithDuplicateEntry() {
        List<Identity> participants = new ArrayList<>();
        participants.add(firstTestIdentity);
        participants.add(secondTestIdentity);
        participants.add(firstTestIdentity);
        return participants;
    }

    private List<Identity> getTestLecturersWithDuplicateEntry() {
        List<Identity> lecturers = new ArrayList<>();
        lecturers.add(ownerIdentity);
        lecturers.add(secondOwnerIdentity);
        lecturers.add(ownerIdentity);
        return lecturers;
    }

    /**
     * 1. Setup Campus-Group with two owners (ownerIdentity, secondOwnerIdentity) <br>
     * 2. Synchronize Campus-Group, remove one owner (secondOwnerIdentity), add a new owner (thirdTestIdentity) <br>
     * The owner group is not synchronized, that is the owners are never removed.
     */
    @Test
    public void synchronizeCourseGroups_AddRemoveLectures_CheckRemovedStatisticAndMembers() {
        CampusCourseImportTO campusCourseImportData = new CampusCourseImportTO("Group_Test", "HS-2012", getTestLecturersWithDuplicateEntry(), null, new ArrayList<>(),
                "Group_Test", TEST_RESOURCEABLE_ID, null, null, null);

        // 1. Setup Campus-Group with owners (ownerIdentity, secondOwnerIdentity)        
        courseGroupSynchronizerTestObject.synchronizeCourseGroups(campusCourseMock, campusCourseImportData);
        assertEquals("Wrong number of owners after init", 2, businessGroupService.getMembers(campusCourseGroup, GroupRoles.coach.name()).size());
        assertEquals("Wrong number of participants after init", 0, businessGroupService.getMembers(campusCourseGroup, GroupRoles.participant.name()).size());

        // 2. Synchronize Campus-Group, remove one owner (secondOwnerIdentity), add a new owner (thirdTestIdentity)
        List<Identity> newOwnerIdentites = new ArrayList<>();
        newOwnerIdentites.add(ownerIdentity);
        newOwnerIdentites.add(thirdTestIdentity);
        CampusCourseImportTO campusCourseImportDataToSyncronize = new CampusCourseImportTO("Group_Test", "HS-2012", newOwnerIdentites, null, new ArrayList<>(),
                "Group_Test", TEST_RESOURCEABLE_ID, null, null, null);
        SynchronizedGroupStatistic statistic = courseGroupSynchronizerTestObject.synchronizeCourseGroups(campusCourseMock, campusCourseImportDataToSyncronize);

        // 1. assert statistic
        assertEquals("Wrong number of added identity in statistic", 1, statistic.getOwnerGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getOwnerGroupStatistic().getRemovedStatistic());
        assertEquals("Wrong number of added identity in statistic", 0, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getParticipantGroupStatistic().getRemovedStatistic());
        
        List<Identity> coaches = businessGroupService.getMembers(campusCourseGroup, GroupRoles.coach.name());
        List<Identity> particiants = businessGroupService.getMembers(campusCourseGroup, GroupRoles.participant.name());
        // 2. assert members
        assertEquals("Wrong number of coaches after synchronize", 3, coaches.size());
        assertEquals("Wrong number of participants after synchronize", 0, particiants.size());
        assertTrue("Missing identity (" + ownerIdentity + ") as coach of course-group", coaches.contains(ownerIdentity));
        assertTrue("Missing identity (" + thirdTestIdentity + ") as coach of course-group", coaches.contains(thirdTestIdentity));
        assertTrue("Missing identity (" + secondOwnerIdentity + ") as coach of course-group", coaches.contains(secondOwnerIdentity));

    }

    /**
     * Test 1. Setup Campus-Group with two participants (testIdentity, secondTestIdentity) <br>
     * 2. Synchronize Campus-Group, remove one participant (secondTestIdentity), add a new participant (thirdTestIdentity)
     */
    @Test
    public void synchronizeCourseGroups_AddRemoveParticipants_CheckRemovedStatisticAndMembers() {
        CampusCourseImportTO campusCourseImportData = new CampusCourseImportTO("Group_Test", "HS-2012", new ArrayList<>(), null,
                getTestParticipantsWithDuplicateEntry(), "Group_Test", TEST_RESOURCEABLE_ID, null, null, null);

        // 1. Setup Campus-Group with participants (testIdentity, secondTestIdentity)
        courseGroupSynchronizerTestObject.synchronizeCourseGroups(campusCourseMock, campusCourseImportData);
        assertEquals("Wrong number of owners after init", 0, businessGroupService.getMembers(campusCourseGroup, GroupRoles.coach.name()).size());
        assertEquals("Wrong number of participants after init", 2, businessGroupService.getMembers(campusCourseGroup, GroupRoles.participant.name()).size());

        // 2. Synchronize Campus-Group, remove one participant (secondTestIdentity), add a new participant (thirdTestIdentity)
        List<Identity> newParticipantsIdentites = new ArrayList<>();
        newParticipantsIdentites.add(firstTestIdentity);
        newParticipantsIdentites.add(thirdTestIdentity);
        CampusCourseImportTO campusCourseImportDataToSyncronize = new CampusCourseImportTO("Group_Test", "HS-2012", new ArrayList<>(), null, newParticipantsIdentites,
                "Group_Test", TEST_RESOURCEABLE_ID, null, null, null);
        SynchronizedGroupStatistic statistic = courseGroupSynchronizerTestObject.synchronizeCourseGroups(campusCourseMock, campusCourseImportDataToSyncronize);

        // 1. assert statistic
        assertEquals("Wrong number of added identity in statistic", 0, statistic.getOwnerGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getOwnerGroupStatistic().getRemovedStatistic());
        assertEquals("Wrong number of added identity in statistic", 1, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 1, statistic.getParticipantGroupStatistic().getRemovedStatistic());
        
        List<Identity> coaches = businessGroupService.getMembers(campusCourseGroup, GroupRoles.coach.name());
        List<Identity> particiants = businessGroupService.getMembers(campusCourseGroup, GroupRoles.participant.name());
        
        // 2. assert members
        assertEquals("Wrong number of coaches after synchronize", 0, coaches.size());
        assertEquals("Wrong number of participants after synchronize", 2, particiants.size());
        assertTrue("Missing identity (" + firstTestIdentity + ") in participant-group of course-group", particiants.contains(firstTestIdentity));
        assertTrue("Missing identity (" + thirdTestIdentity + ")in participant-group of course-group", particiants.contains(thirdTestIdentity));
        assertFalse("Identity (" + secondTestIdentity + ")is no longer member of participant-group of course-group", particiants.contains(secondTestIdentity));
    }

}
