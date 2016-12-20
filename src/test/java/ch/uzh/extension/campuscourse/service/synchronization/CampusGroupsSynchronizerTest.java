package ch.uzh.extension.campuscourse.service.synchronization;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.CampusCourseJunitTestHelper;
import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import ch.uzh.extension.campuscourse.data.dao.CourseDao;
import ch.uzh.extension.campuscourse.service.synchronization.statistic.SynchronizedGroupStatistic;
import ch.uzh.extension.campuscourse.model.CampusGroups;
import ch.uzh.extension.campuscourse.model.CampusCourseTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@Component
public class CampusGroupsSynchronizerTest extends CampusCourseTestCase {

    @Autowired
    private UserManager userManager;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private CampusGroupsSynchronizer campusGroupsSynchronizer;

    @Autowired
    private BusinessGroupService businessGroupService;

    @Autowired
    private BGAreaManager bgAreaManager;
    
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    private DB dbInstance;

    private Identity firstLecturerIdentity;
    private Identity secondLecturerIdentity;
    private Identity thirdLecturerIdentity;
    private Identity forthLecturerIdentity;
    private Identity firstParicipantIdentity;
    private Identity secondParticipantIdentity;
    private Identity thirdParticipantIdentity;
    private Set<Identity> lecturers = new HashSet<>();
    private Set<Identity> delegatees = new HashSet<>();
    private Set<Identity> participants = new HashSet<>();
    private RepositoryEntry demoCourseRepositoryEntry;
	private CampusGroups campusGroups;

	@Before
    public void setup() {

    	 // Setup test users
        firstLecturerIdentity = CampusCourseJunitTestHelper.createTestUser(userManager, dbInstance, "FirsLecturer");  // course owner
        secondLecturerIdentity = CampusCourseJunitTestHelper.createTestUser(userManager, dbInstance, "SecondLecturer");
        thirdLecturerIdentity = CampusCourseJunitTestHelper.createTestUser(userManager, dbInstance, "ThirdLecturer");
        forthLecturerIdentity = CampusCourseJunitTestHelper.createTestUser(userManager, dbInstance, "ForthLecturer");

        firstParicipantIdentity = CampusCourseJunitTestHelper.createTestUser(userManager, dbInstance, "FirstParticipant");
        secondParticipantIdentity = CampusCourseJunitTestHelper.createTestUser(userManager, dbInstance, "SecondParticipant");
        thirdParticipantIdentity = CampusCourseJunitTestHelper.createTestUser(userManager, dbInstance, "ThirdParticipant");

        lecturers.add(firstLecturerIdentity);
        lecturers.add(secondLecturerIdentity);
        lecturers.add(thirdLecturerIdentity);

        delegatees.add(thirdLecturerIdentity);
        delegatees.add(forthLecturerIdentity);

        participants.add(firstParicipantIdentity);
        participants.add(secondParticipantIdentity);
        participants.add(thirdParticipantIdentity);

        // Create olat demo course with first lecturer as course owner
        demoCourseRepositoryEntry = JunitTestHelper.deployDemoCourse(firstLecturerIdentity);

        // Create campus groups
        campusGroups = CampusCourseJunitTestHelper.createCampusGroupsForTest(firstLecturerIdentity, demoCourseRepositoryEntry, campusCourseConfiguration, courseDao, bgAreaManager, businessGroupService, dbInstance);
    }

    @After
    public void teardown() {    	
    	dbInstance.rollback();
    }

    @Test
    public void testAddCourseOwnerRole() {

        campusGroupsSynchronizer.addCourseOwnerRole(demoCourseRepositoryEntry, lecturers);

        List<Identity> ownerIdentities =  repositoryService.getMembers(demoCourseRepositoryEntry, GroupRoles.owner.name());
        assertEquals("Wrong number of owners", 3, ownerIdentities.size());
        assertTrue("Missing identity (" + firstLecturerIdentity + ") in owner-group", ownerIdentities.contains(firstLecturerIdentity));
        assertTrue("Missing identity (" + secondLecturerIdentity + ") in owner-group", ownerIdentities.contains(secondLecturerIdentity));
        assertTrue("Missing identity (" + thirdLecturerIdentity + ") in owner-group", ownerIdentities.contains(thirdLecturerIdentity));
    }

    /**
     * Add two lecturers (includes an identity twice to check duplicate handling) and no participants.
     * This is the initialize process when owner-list is empty at the beginning.
     */
    @Test
    public void testSynchronizeCampusGroups_AddLectures_CheckAddedStatisticAndMembers() throws CampusCourseException {

        SynchronizedGroupStatistic statistic = campusGroupsSynchronizer.synchronizeCampusGroups(
				campusGroups,
				new CampusCourseTO("CampusCourseTitle", null, lecturers,
                        delegatees, Collections.emptySet(), false, null, null,
                        demoCourseRepositoryEntry, campusGroups, null, "DE", null), firstLecturerIdentity);



        // 1. Check members of campus group A
        List<Identity> groupCoaches = businessGroupService.getMembers(campusGroups.getCampusGroupA(), GroupRoles.coach.name());
        List<Identity> groupParticipants = businessGroupService.getMembers(campusGroups.getCampusGroupA(), GroupRoles.participant.name());
        assertEquals("Wrong number of coaches", 4, groupCoaches.size());
        assertTrue(groupCoaches.contains(firstLecturerIdentity));
        assertTrue(groupCoaches.contains(secondLecturerIdentity));
        assertTrue(groupCoaches.contains(thirdLecturerIdentity));
        assertTrue(groupCoaches.contains(forthLecturerIdentity));
        assertEquals("Wrong number of participants", 0, groupParticipants.size());

        // 2. Check members of campus group B
        groupCoaches = businessGroupService.getMembers(campusGroups.getCampusGroupB(), GroupRoles.coach.name());
        groupParticipants = businessGroupService.getMembers(campusGroups.getCampusGroupB(), GroupRoles.participant.name());
        assertEquals("Wrong number of coaches", 4, groupCoaches.size());
        assertTrue(groupCoaches.contains(firstLecturerIdentity));
        assertTrue(groupCoaches.contains(secondLecturerIdentity));
        assertTrue(groupCoaches.contains(thirdLecturerIdentity));
        assertTrue(groupCoaches.contains(forthLecturerIdentity));
        assertEquals("Wrong number of participants", 0, groupParticipants.size());

        // 3. Check statistic
        // firstLecturerIdentity is already in group (as owner of the course) and is not added a second time
        assertEquals("Wrong number of added identity in statistic", 3, statistic.getCoachGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getCoachGroupStatistic().getRemovedStatistic());
        assertEquals("Wrong number of added identity in statistic", 0, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getParticipantGroupStatistic().getRemovedStatistic());
    }

    /**
     * Add two participants (includes an identity twice to check duplicate handling) and no lecturer.
     * This is the initialize process when participant-list is empty at the beginning.
     */
    @Test
    public void testSynchronizeCampusGroups_AddParticipants_CheckAddedStatisticAndMembers() throws CampusCourseException {

        SynchronizedGroupStatistic statistic = campusGroupsSynchronizer.synchronizeCampusGroups(
                campusGroups,
				new CampusCourseTO("CampusCourseTitle", null, Collections.emptySet(),
						Collections.emptySet(), participants, false, null, null,
                        demoCourseRepositoryEntry, campusGroups, null, "DE", null),
                firstLecturerIdentity);

        // 1. Check members of campus group A
        List<Identity> groupCoaches =  businessGroupService.getMembers(campusGroups.getCampusGroupA(), GroupRoles.coach.name());
        List<Identity> groupParticipants = businessGroupService.getMembers(campusGroups.getCampusGroupA(), GroupRoles.participant.name());
        assertEquals("Wrong number of coaches", 1, groupCoaches.size());
        assertTrue(groupCoaches.contains(firstLecturerIdentity));
        assertEquals("Wrong number of participants", 3, groupParticipants.size());
        assertTrue(groupParticipants.contains(firstParicipantIdentity));
        assertTrue(groupParticipants.contains(secondParticipantIdentity));
        assertTrue(groupParticipants.contains(thirdParticipantIdentity));

        // 2. Check members of campus group B
        groupCoaches =  businessGroupService.getMembers(campusGroups.getCampusGroupB(), GroupRoles.coach.name());
        groupParticipants = businessGroupService.getMembers(campusGroups.getCampusGroupB(), GroupRoles.participant.name());
        assertEquals("Wrong number of coaches", 1, groupCoaches.size());
        assertTrue(groupCoaches.contains(firstLecturerIdentity));
        assertEquals("Wrong number of participants", 0, groupParticipants.size());

        // 3. Check statistic
        assertEquals("Wrong number of added identity in statistic", 0, statistic.getCoachGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getCoachGroupStatistic().getRemovedStatistic());

        assertEquals("Wrong number of added identity in statistic", 3, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getParticipantGroupStatistic().getRemovedStatistic());
    }

    /**
     * 1. Setup campus group with two lecturers (firstLecturerIdentity, secondLecturerIdentity) <br>
     * 2. Synchronize Campus-Group, remove one coach (secondLecturerIdentity), add a new coach (thirdLecturerIdentity) <br>
     * Coaches are only added, but never removed from the coache group.
     */
    @Test
    public void testSynchronizeCampusGroups_AddRemoveLectures_CheckRemovedStatisticAndMembers() throws CampusCourseException {

        Set<Identity> lecturers = new HashSet<>();
        lecturers.add(firstLecturerIdentity);
        lecturers.add(secondLecturerIdentity);

        campusGroupsSynchronizer.synchronizeCampusGroups(
                campusGroups,
				new CampusCourseTO("CampusCourseTitle", null, lecturers,
						Collections.emptySet(), Collections.emptySet(), false, null, null,
                        demoCourseRepositoryEntry, campusGroups, null, "DE", null),
                firstLecturerIdentity);
        assertEquals("Wrong number of coaches after init", 2, businessGroupService.getMembers(campusGroups.getCampusGroupA(), GroupRoles.coach.name()).size());
        assertEquals("Wrong number of participants after init", 0, businessGroupService.getMembers(campusGroups.getCampusGroupA(), GroupRoles.participant.name()).size());

        // Remove one owner (secondLecturerIdentity) and add a new owner (thirdParticipantIdentity)
        lecturers.remove(secondLecturerIdentity);
        lecturers.add(thirdLecturerIdentity);

        SynchronizedGroupStatistic statistic = campusGroupsSynchronizer.synchronizeCampusGroups(
                campusGroups,
				new CampusCourseTO("CampusCourseTitle", null, lecturers,
						Collections.emptySet(), Collections.emptySet(), false, null, null,
                        demoCourseRepositoryEntry, campusGroups, null, "DE", null),
                firstLecturerIdentity);

        // 1. Check members
        List<Identity> groupCoaches = businessGroupService.getMembers(campusGroups.getCampusGroupA(), GroupRoles.coach.name());
        List<Identity> groupParticipants = businessGroupService.getMembers(campusGroups.getCampusGroupA(), GroupRoles.participant.name());
        assertEquals("Wrong number of coaches after synchronize", 3, groupCoaches.size());
        assertEquals("Wrong number of participants after synchronize", 0, groupParticipants.size());
        assertTrue("Missing identity (" + firstLecturerIdentity + ") as coach of course-group", groupCoaches.contains(firstLecturerIdentity));
        assertTrue("Missing identity (" + thirdLecturerIdentity + ") as coach of course-group", groupCoaches.contains(thirdLecturerIdentity));
        assertTrue("Missing identity (" + secondLecturerIdentity + ") as coach of course-group", groupCoaches.contains(secondLecturerIdentity));

        // 2. Check statistic
        assertEquals("Wrong number of added identity in statistic", 1, statistic.getCoachGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getCoachGroupStatistic().getRemovedStatistic());
        assertEquals("Wrong number of added identity in statistic", 0, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getParticipantGroupStatistic().getRemovedStatistic());
    }

    /**
     * Test 1. Setup Campus-Group with two participants (firstParticipantIdentity, secondParticipantIdentity) <br>
     * 2. Synchronize Campus-Group, remove one participant (secondParticipantIdentity), add a new participant (thirdParticipantIdentity)
     */
    @Test
    public void testSynchronizeCampusGroups_AddRemoveParticipants_CheckRemovedStatisticAndMembers() throws CampusCourseException {

        Set<Identity> participants = new HashSet<>();
        participants.add(firstParicipantIdentity);
        participants.add(secondParticipantIdentity);

        campusGroupsSynchronizer.synchronizeCampusGroups(
                campusGroups,
				new CampusCourseTO("CampusCourseTitle", null, Collections.emptySet(),
						Collections.emptySet(), participants, false, null, null,
                        demoCourseRepositoryEntry, campusGroups, null, "DE", null),
                firstLecturerIdentity);

        assertEquals("Wrong number of coaches after init", 1, businessGroupService.getMembers(campusGroups.getCampusGroupA(), GroupRoles.coach.name()).size());
        assertEquals("Wrong number of participants after init", 2, businessGroupService.getMembers(campusGroups.getCampusGroupA(), GroupRoles.participant.name()).size());

        // Remove one participant (secondParticipantIdentity), add a new participant (thirdParticipantIdentity)
        participants.remove(secondParticipantIdentity);
        participants.add(thirdParticipantIdentity);

        SynchronizedGroupStatistic statistic = campusGroupsSynchronizer.synchronizeCampusGroups(
                campusGroups,
				new CampusCourseTO("CampusCourseTitle", null, Collections.emptySet(),
						Collections.emptySet(), participants, false, null, null,
                        demoCourseRepositoryEntry, campusGroups, null, "DE", null),
                firstLecturerIdentity);

        // 1. Check members
        List<Identity> groupCoaches = businessGroupService.getMembers(campusGroups.getCampusGroupA(), GroupRoles.coach.name());
        List<Identity> groupParticipants = businessGroupService.getMembers(campusGroups.getCampusGroupA(), GroupRoles.participant.name());
        assertEquals("Wrong number of coaches after synchronize", 1, groupCoaches.size());
        assertEquals("Wrong number of participants after synchronize", 2, groupParticipants.size());
        assertTrue("Missing identity (" + firstLecturerIdentity + ") as coach of course-group", groupCoaches.contains(firstLecturerIdentity));
        assertTrue("Missing identity (" + firstParicipantIdentity + ") in participant-group of course-group", groupParticipants.contains(firstParicipantIdentity));
        assertTrue("Missing identity (" + thirdParticipantIdentity + ")in participant-group of course-group", groupParticipants.contains(thirdParticipantIdentity));
        assertFalse("Identity (" + secondParticipantIdentity + ")is no longer member of participant-group of course-group", groupParticipants.contains(secondParticipantIdentity));

        // 2. Check statistic
        assertEquals("Wrong number of added identity in statistic", 0, statistic.getCoachGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 0, statistic.getCoachGroupStatistic().getRemovedStatistic());
        assertEquals("Wrong number of added identity in statistic", 1, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals("Wrong number of removed identity in statistic", 1, statistic.getParticipantGroupStatistic().getRemovedStatistic());
    }
}
