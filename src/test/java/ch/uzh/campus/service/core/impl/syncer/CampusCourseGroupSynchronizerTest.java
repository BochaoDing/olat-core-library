package ch.uzh.campus.service.core.impl.syncer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.test.JunitTestHelper;

import ch.uzh.campus.CampusConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.creator.ObjectMother;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedGroupStatistic;

public class CampusCourseGroupSynchronizerTest extends OlatTestCase {
	private static final String TEST_COURSE_GROUP_A_NAME = "Campusgroup A SynchronizeTest";
    private static final String TEST_COURSE_GROUP_B_NAME = "Campusgroup B SynchronizeTest";

    private String firstCoOwnerName = "co_owner1";
    private String secondCoOwnerName = "co_owner2";
    private String TEST_CO_OWNER_NAMES = firstCoOwnerName + "," + secondCoOwnerName;

    private final Long TEST_RESOURCEABLE_ID = 1234L;

    @Autowired
    CampusCourseGroupSynchronizer courseGroupSynchronizerTestObject;
    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private BusinessGroupService businessGroupService;
    
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    private DB dbInstance;

    private String ownerName = "TestOwner";
    private Identity ownerIdentity;
    private String ownerNameSecond = "SecondTestOwner";
    private Identity secondOwnerIdentity;
    private String firstTestUserName = "FirstTestUser";
    private Identity firstTestIdentity;
    private String secondTestUserName = "SecondTestUser";
    private Identity secondTestIdentity;
    private String thirdTestUserName = "ThirdTestUser";
    private Identity thirdTestIdentity;

    private ICourse course;
    //private BusinessGroup campusCourseGroup; //GroupA

    private Identity firstCoOwnerIdentity;
    private Identity secondCoOwnerIdentity;
    
    private CampusCourse campusCourseMock;
    private CampusConfiguration campusConfigurationMock;
    
    RepositoryEntry sourceRepositoryEntry;

    @Before
    public void setup() {
        // Setup Test Configuration
        campusConfigurationMock = mock(CampusConfiguration.class);
        when(campusConfigurationMock.getCourseGroupAName()).thenReturn(TEST_COURSE_GROUP_A_NAME);
        when(campusConfigurationMock.getCourseGroupBName()).thenReturn(TEST_COURSE_GROUP_B_NAME);
        when(campusConfigurationMock.getDefaultCoOwnerUserNames()).thenReturn(TEST_CO_OWNER_NAMES);
        courseGroupSynchronizerTestObject.campusConfiguration = campusConfigurationMock;
        courseGroupSynchronizerTestObject.campuskursCoOwners.campusConfiguration = campusConfigurationMock;

        // Setup Test Identities
        ownerIdentity = JunitTestHelper.createAndPersistIdentityAsUser(ownerName); //course owner
        secondOwnerIdentity = JunitTestHelper.createAndPersistIdentityAsUser(ownerNameSecond);

        firstTestIdentity = JunitTestHelper.createAndPersistIdentityAsUser(firstTestUserName);
        secondTestIdentity = JunitTestHelper.createAndPersistIdentityAsUser(secondTestUserName);
        thirdTestIdentity = JunitTestHelper.createAndPersistIdentityAsUser(thirdTestUserName);

        firstCoOwnerIdentity = JunitTestHelper.createAndPersistIdentityAsUser(firstCoOwnerName);
        secondCoOwnerIdentity = JunitTestHelper.createAndPersistIdentityAsUser(secondCoOwnerName);

        // Setup test-course and campus-group
        sourceRepositoryEntry = JunitTestHelper.deployDemoCourse(ownerIdentity);
        course = CourseFactory.loadCourse(sourceRepositoryEntry.getOlatResource().getResourceableId());
        ObjectMother.setupCampusCourseGroupForTest(sourceRepositoryEntry, TEST_COURSE_GROUP_A_NAME, businessGroupService);
        dbInstance.commitAndCloseSession();
        ObjectMother.setupCampusCourseGroupForTest(sourceRepositoryEntry, TEST_COURSE_GROUP_B_NAME, businessGroupService);
        dbInstance.commitAndCloseSession();
        

        //TODO: olatng
        // Setup RepositoryEntry-mock to have access to the owner-group
        /*testSecurityGroup = baseSecurity.createAndPersistSecurityGroup();
        RepositoryEntry repositoryEntryMock = mock(RepositoryEntry.class);
        when(repositoryEntryMock.getOwnerGroup()).thenReturn(testSecurityGroup);
        */
        campusCourseMock = mock(CampusCourse.class);
        //when(campusCourseMock.getRepositoryEntry()).thenReturn(repositoryEntryMock);
        when(campusCourseMock.getRepositoryEntry()).thenReturn(sourceRepositoryEntry);
        when(campusCourseMock.getCourse()).thenReturn(course);
    }
        

    @After
    public void teardown() {    	
    	dbInstance.rollback();
        //cleanupCampusCourseGroup(campusCourseGroup);
    }

    @Test
    public void addAllLecturesAsOwner() {
        // Exercise
        courseGroupSynchronizerTestObject.addAllLecturesAsOwner(campusCourseMock, getTestLecturersWithDublicateEntry());
        List<Identity> ownerIdentities =  repositoryService.getMembers(sourceRepositoryEntry, GroupRoles.owner.name());
        //assertEquals("Wrong number of owners", 2, baseSecurity.countIdentitiesOfSecurityGroup(testSecurityGroup));
        assertEquals("Wrong number of owners", 2, ownerIdentities.size());
        //assertTrue("Missing identity (" + ownerIdentity + ") in owner-group", baseSecurity.isIdentityInSecurityGroup(ownerIdentity, testSecurityGroup));
        //assertTrue("Missing identity (" + secondOwnerIdentity + ")in owner-group", baseSecurity.isIdentityInSecurityGroup(secondOwnerIdentity, testSecurityGroup));
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
        CampusCourseImportTO campusCourseImportData = new CampusCourseImportTO("Group_Test", "HS-2012", getTestLecturersWithDublicateEntry(), new ArrayList<Identity>(),
                "Group_Test", TEST_RESOURCEABLE_ID);

        SynchronizedGroupStatistic statistic = courseGroupSynchronizerTestObject.synchronizeCourseGroups(campusCourseMock, campusCourseImportData);
        
        // 1. assert statistic
        assertEquals("Wrong number of added identity in statistic", 2, statistic.getOwnerGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getOwnerGroupStatistic().getRemovedStatistic());
        assertEquals("Wrong number of added identity in statistic", 0, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getParticipantGroupStatistic().getRemovedStatistic());
        
        BusinessGroup campusCourseGroup = CampusGroupHelper.lookupCampusGroup(course, campusConfigurationMock.getCourseGroupAName());
        List<Identity> groupCoaches = businessGroupService.getMembers(campusCourseGroup, GroupRoles.coach.name());
        // 2. assert members
        assertEquals("Wrong number of owners", 2, groupCoaches.size());
        assertTrue("Missing identity (" + ownerIdentity + ") as coach of course-group", groupCoaches.contains(ownerIdentity));
        assertTrue("Missing identity (" + secondOwnerIdentity + ") as coach of course-group", groupCoaches.contains(secondOwnerIdentity));
                
    }

/*    private void cleanupCampusCourseGroup(BusinessGroup campusCourseGroup) {
        removAllIdentitesFromSecurityGroup(campusCourseGroup.getOwnerGroup());
        removAllIdentitesFromSecurityGroup(campusCourseGroup.getPartipiciantGroup());
    }

    private void removAllIdentitesFromSecurityGroup(SecurityGroup securityGroup) {
        List<Identity> identities = baseSecurity.getIdentitiesOfSecurityGroup(securityGroup);
        for (Identity identity : identities) {
            baseSecurity.removeIdentityFromSecurityGroup(identity, securityGroup);
        }
    }*/

    /**
     * Add two participants (includes an identity twice to check duplicate handling) and no lecturer. This is the initialize process when participant-list is empty at the
     * beginning.
     */
    @Test
    public void synchronizeCourseGroups_AddParticipants_CheckAddedStatisticAndMembers() {
        CampusCourseImportTO campusCourseImportData = new CampusCourseImportTO("Group_Test", "HS-2012", new ArrayList<Identity>(),
                getTestParticipantsWithDublicateEntry(), "Group_Test", TEST_RESOURCEABLE_ID);

        // no owner-identities, two participants (testIdentity, secondTestIdentity)
        SynchronizedGroupStatistic statistic = courseGroupSynchronizerTestObject.synchronizeCourseGroups(campusCourseMock, campusCourseImportData);

        // 1. assert statistic
        assertEquals("Wrong number of added identity in statistic", 0, statistic.getOwnerGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getOwnerGroupStatistic().getRemovedStatistic());
        
        //TODO: olatng
        //assertEquals("Wrong number of added identity in statistic", 2, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getParticipantGroupStatistic().getRemovedStatistic());
                
        BusinessGroup campusCourseGroup = CampusGroupHelper.lookupCampusGroup(course, campusConfigurationMock.getCourseGroupAName());
        List<Identity> groupParticipants = businessGroupService.getMembers(campusCourseGroup, GroupRoles.participant.name());
        // 2. assert members
        //assertEquals("Wrong number of owners", 0, baseSecurity.countIdentitiesOfSecurityGroup(campusCourseGroup.getOwnerGroup()));
        assertEquals("Wrong number of participants", 2, groupParticipants.size());
        assertTrue("Missing identity (" + firstTestIdentity + ") as participant of course-group", groupParticipants.contains(firstTestIdentity));
        assertTrue("Missing identity (" + secondTestIdentity + ") as participant of course-group", groupParticipants.contains(secondTestIdentity));
    }

    private List<Identity> getTestParticipantsWithDublicateEntry() {
        List<Identity> participants = new ArrayList<Identity>();
        participants.add(firstTestIdentity);
        participants.add(secondTestIdentity);
        participants.add(firstTestIdentity);
        return participants;
    }

    private List<Identity> getTestLecturersWithDublicateEntry() {
        List<Identity> lecturers = new ArrayList<Identity>();
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
    /*@Test
    public void synchronizeCourseGroups_AddRemoveLectures_CheckRemovedStatisticAndMembers() {
        CampusCourseImportTO campusCourseImportData = new CampusCourseImportTO("Group_Test", "HS-2012", getTestLecturersWithDublicateEntry(), new ArrayList<Identity>(),
                "Group_Test", TEST_RESOURCEABLE_ID);

        // 1. Setup Campus-Group with owners (ownerIdentity, secondOwnerIdentity)
        courseGroupSynchronizerTestObject.synchronizeCourseGroups(course, campusCourseImportData);
        assertEquals("Wrong number of owners after init", 2, baseSecurity.countIdentitiesOfSecurityGroup(campusCourseGroup.getOwnerGroup()));
        assertEquals("Wrong number of participants after init", 0, baseSecurity.countIdentitiesOfSecurityGroup(campusCourseGroup.getPartipiciantGroup()));

        // 2. Synchronize Campus-Group, remove one owner (secondOwnerIdentity), add a new owner (thirdTestIdentity)
        List<Identity> newOwnerIdentites = new ArrayList<Identity>();
        newOwnerIdentites.add(ownerIdentity);
        newOwnerIdentites.add(thirdTestIdentity);
        CampusCourseImportTO campusCourseImportDataToSyncronize = new CampusCourseImportTO("Group_Test", "HS-2012", newOwnerIdentites, new ArrayList<Identity>(),
                "Group_Test", TEST_RESOURCEABLE_ID);
        SynchronizedGroupStatistic statistic = courseGroupSynchronizerTestObject.synchronizeCourseGroups(course, campusCourseImportDataToSyncronize);

        // 1. assert statistic
        assertEquals("Wrong number of added identity in statistic", 1, statistic.getOwnerGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getOwnerGroupStatistic().getRemovedStatistic());
        assertEquals("Wrong number of added identity in statistic", 0, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getParticipantGroupStatistic().getRemovedStatistic());
        // 2. assert members
        assertEquals("Wrong number of owners after synchronize", 3, baseSecurity.countIdentitiesOfSecurityGroup(campusCourseGroup.getOwnerGroup()));
        assertEquals("Wrong number of participants after synchronize", 0, baseSecurity.countIdentitiesOfSecurityGroup(campusCourseGroup.getPartipiciantGroup()));
        assertTrue("Missing identity (" + ownerIdentity + ") in owner-group of course-group",
                baseSecurity.isIdentityInSecurityGroup(ownerIdentity, campusCourseGroup.getOwnerGroup()));
        assertTrue("Missing identity (" + thirdTestIdentity + ")in owner-group of course-group",
                baseSecurity.isIdentityInSecurityGroup(thirdTestIdentity, campusCourseGroup.getOwnerGroup()));
        assertTrue("Missing identity (" + secondOwnerIdentity + ")in owner-group of course-group",
                baseSecurity.isIdentityInSecurityGroup(secondOwnerIdentity, campusCourseGroup.getOwnerGroup()));

    }*/

    /**
     * Test 1. Setup Campus-Group with two participants (testIdentity, secondTestIdentity) 2. Synchronize Campus-Group, remove one participant (secondTestIdentity), add a
     * new participant (thirdTestIdentity)
     */
    /*@Test
    public void synchronizeCourseGroups_AddRemoveParticipants_CheckRemovedStatisticAndMembers() {
        CampusCourseImportTO campusCourseImportData = new CampusCourseImportTO("Group_Test", "HS-2012", new ArrayList<Identity>(),
                getTestParticipantsWithDublicateEntry(), "Group_Test", TEST_RESOURCEABLE_ID);

        // 1. Setup Campus-Group with participants (testIdentity, secondTestIdentity)
        courseGroupSynchronizerTestObject.synchronizeCourseGroups(course, campusCourseImportData);
        assertEquals("Wrong number of owners after init", 0, baseSecurity.countIdentitiesOfSecurityGroup(campusCourseGroup.getOwnerGroup()));
        assertEquals("Wrong number of participants after init", 2, baseSecurity.countIdentitiesOfSecurityGroup(campusCourseGroup.getPartipiciantGroup()));

        // 2. Synchronize Campus-Group, remove one participant (secondTestIdentity), add a new participant (thirdTestIdentity)
        List<Identity> newParticipantsIdentites = new ArrayList<Identity>();
        newParticipantsIdentites.add(firstTestIdentity);
        newParticipantsIdentites.add(thirdTestIdentity);
        CampusCourseImportTO campusCourseImportDataToSyncronize = new CampusCourseImportTO("Group_Test", "HS-2012", new ArrayList<Identity>(), newParticipantsIdentites,
                "Group_Test", TEST_RESOURCEABLE_ID);
        SynchronizedGroupStatistic statistic = courseGroupSynchronizerTestObject.synchronizeCourseGroups(course, campusCourseImportDataToSyncronize);

        // 1. assert statistic
        assertEquals("Wrong number of added identity in statistic", 0, statistic.getOwnerGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getOwnerGroupStatistic().getRemovedStatistic());
        assertEquals("Wrong number of added identity in statistic", 1, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 1, statistic.getParticipantGroupStatistic().getRemovedStatistic());
        // 2. assert members
        assertEquals("Wrong number of owners after synchronize", 0, baseSecurity.countIdentitiesOfSecurityGroup(campusCourseGroup.getOwnerGroup()));
        assertEquals("Wrong number of participants after synchronize", 2, baseSecurity.countIdentitiesOfSecurityGroup(campusCourseGroup.getPartipiciantGroup()));
        assertTrue("Missing identity (" + firstTestIdentity + ") in participant-group of course-group",
                baseSecurity.isIdentityInSecurityGroup(firstTestIdentity, campusCourseGroup.getPartipiciantGroup()));
        assertTrue("Missing identity (" + thirdTestIdentity + ")in participant-group of course-group",
                baseSecurity.isIdentityInSecurityGroup(thirdTestIdentity, campusCourseGroup.getPartipiciantGroup()));
        assertFalse("Identity (" + secondTestIdentity + ")is no longer member of participant-group of course-group",
                baseSecurity.isIdentityInSecurityGroup(secondTestIdentity, campusCourseGroup.getPartipiciantGroup()));

    }*/

}
