package ch.uzh.extension.campuscourse.service.synchronization;

import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.model.CampusCourseTO;
import ch.uzh.extension.campuscourse.model.CampusGroups;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Initial Date: 20.06.2012 <br>
 * 
 * @author cg
 * 
 * All lecturers are added as owners to the course. <br>
 * All students are added as participants to campus group A and all lecturers are added as coaches of both campus groups
 * A and B.
 * 
 */
@Component
public class CampusGroupsSynchronizer {
    
	private static final OLog LOG = Tracing.createLoggerFor(CampusGroupsSynchronizer.class);

    private final RepositoryService repositoryService;
    private final BusinessGroupService businessGroupService;

    @Autowired
    public CampusGroupsSynchronizer(RepositoryService repositoryService,
                                    BusinessGroupService businessGroupService) {
        this.repositoryService = repositoryService;
        this.businessGroupService = businessGroupService;
    }

   /**
    * Add the list of identities as owners of this resource/course.
    */
   public void addCourseOwnerRole(RepositoryEntry repositoryEntry, Collection<Identity> identities) {
	   for (Identity identity : identities) {
		   if (!repositoryService.hasRole(identity, repositoryEntry, GroupRoles.owner.name())) {
			   repositoryService.addRole(identity, repositoryEntry, GroupRoles.owner.name());
		   }
	   }
	}

   /**
    * Synchronize group coaches and participants of campus group A and the group coaches of campus group B.
    */
    public CampusCourseSynchronizationResult synchronizeCampusGroups(CampusGroups campusGroups,
                                                                     CampusCourseTO campusCourseTO,
                                                                     Identity creator) throws CampusCourseException {

        if (campusGroups.getCampusGroupA() == null) {
            throw new CampusCourseException("Campus course groups A does not exist");
        }
        if (campusGroups.getCampusGroupB() == null) {
            throw new CampusCourseException("Campus course groups B does not exist");
        }

        // Synchronize group coaches and participants of campus group A
        GroupSynchronizationResult groupCoachesSynchronizationResult = synchronizeGroupCoaches(creator, campusGroups.getCampusGroupA(), campusCourseTO.getLecturersOfCourse(), campusCourseTO.getDelegateesOfCourse());
        GroupSynchronizationResult groupParticipantsSynchronizationResult = synchronizeGroupParticipants(creator, campusGroups.getCampusGroupA(), campusCourseTO.getParticipantsOfCourse());

        // Synchronize group coaches of campus group B
        synchronizeGroupCoaches(creator, campusGroups.getCampusGroupB(), campusCourseTO.getLecturersOfCourse(), campusCourseTO.getDelegateesOfCourse());
        
        return new CampusCourseSynchronizationResult(
                campusCourseTO.getTitleToBeDisplayed(),
                groupCoachesSynchronizationResult.getAddedMembers(),
                groupCoachesSynchronizationResult.getRemovedMembers(),
                groupParticipantsSynchronizationResult.getAddedMembers(),
                groupParticipantsSynchronizationResult.getRemovedMembers());
    }
    
    private GroupSynchronizationResult synchronizeGroupCoaches(Identity courseOwner, BusinessGroup businessGroup, Set<Identity> newLecturers, Set<Identity> newDelegatees) {
    	// Group coaches = lecturers + delegatees
        Set<Identity> newGroupCoaches = new HashSet<>(newLecturers);
        newGroupCoaches.addAll(newDelegatees);
        Set<Identity> previousGroupCoaches = getPreviousGroupCoaches(businessGroup);
        // We only add, but never remove group coaches when synchronizing. Reason: Lecturers have the possibility to
		// add group coaches manually. We do not want to remove them again here.
		int addedGroupCoaches = addNewGroupCoachesToBusinessGroup(courseOwner, businessGroup, previousGroupCoaches, newGroupCoaches);
    	return new GroupSynchronizationResult(addedGroupCoaches, 0);
    }
    
    private GroupSynchronizationResult synchronizeGroupParticipants(Identity courseOwner, BusinessGroup businessGroup, Set<Identity> newParticipants) {
    	Set<Identity> previousParticipants = getPreviousParticipants(businessGroup);
    	int removedParticipants = removeNonParticipantsFromBusinessGroup(courseOwner, businessGroup, previousParticipants, newParticipants);
        int addedParticipants = addNewParticipantsToBusinessGroup(courseOwner, businessGroup, previousParticipants, newParticipants);
        return new GroupSynchronizationResult(addedParticipants, removedParticipants);
    }

	private Set<Identity> getPreviousGroupCoaches(BusinessGroup businessGroup) {
		List<Identity> previousGroupCoaches = businessGroupService.getMembers(businessGroup, GroupRoles.coach.name());
		return new HashSet<>(previousGroupCoaches);
	}

	private int addNewGroupCoachesToBusinessGroup(Identity courseOwner, BusinessGroup businessGroup, Set<Identity> previousGroupCoaches, Set<Identity> newGroupCoaches) {
		List<Identity> groupCoachesToBeAdded = getMembersToBeAdded(businessGroup, previousGroupCoaches, newGroupCoaches);
		if (!groupCoachesToBeAdded.isEmpty()) {
			// addChoaches() would be the better name, since businessGroupService.addOwners() adds the role of a coach
			// (coach == owner for business groups)
			businessGroupService.addOwners(courseOwner, null, new ArrayList<>(groupCoachesToBeAdded), businessGroup, null);
		}
		return groupCoachesToBeAdded.size();
	}

	private Set<Identity> getPreviousParticipants(BusinessGroup businessGroup) {
		List<Identity> previousParticipants = businessGroupService.getMembers(businessGroup, GroupRoles.participant.name());
		return new HashSet<>(previousParticipants);
	}

    private int removeNonParticipantsFromBusinessGroup(Identity courseOwner, BusinessGroup businessGroup, Set<Identity> previousParticipants, Set<Identity> newParticipants) {
        List<Identity> participantsToBeRemoved = getMembersToBeRemoved(businessGroup, previousParticipants, newParticipants);
        if (!participantsToBeRemoved.isEmpty()) {
            businessGroupService.removeParticipants(courseOwner, participantsToBeRemoved, businessGroup, null);
        }
        return participantsToBeRemoved.size();
    }

	private int addNewParticipantsToBusinessGroup(Identity courseOwner, BusinessGroup businessGroup, Set<Identity> previousParticipants, Set<Identity> newParticipants) {
		List<Identity> participantsToBeAdded = getMembersToBeAdded(businessGroup, previousParticipants, newParticipants);
		if (!participantsToBeAdded.isEmpty()) {
			businessGroupService.addParticipants(courseOwner, null, new ArrayList<>(participantsToBeAdded), businessGroup, null);
		}
		return participantsToBeAdded.size();
	}

	private List<Identity> getMembersToBeRemoved(BusinessGroup businessGroup, Set<Identity> previousMembers, Set<Identity> newMembers) {
		List<Identity> membersToBeRemoved = new ArrayList<>();
		for (Identity previousMember : previousMembers) {
			// Check if previous member is still in new members list
			if (!newMembers.contains(previousMember)) {
				LOG.debug("Campus course synchronization: Remove member '" + previousMember + "' from group '" + businessGroup + "'");
				membersToBeRemoved.add(previousMember);
			}
		}
		return membersToBeRemoved;
	}

	private List<Identity> getMembersToBeAdded(BusinessGroup businessGroup, Set<Identity> previousMembers, Set<Identity> newMembers) {
		List<Identity> membersToBeAdded = new ArrayList<>();
		for (Identity newMember : newMembers) {
			// Check if new member is already in previous members list
			if (!previousMembers.contains(newMember)) {
				LOG.debug("Campus course synchronization: Add member '" + newMember + "' to group '" + businessGroup + "'");
				membersToBeAdded.add(newMember);
			}
		}
		return membersToBeAdded;
	}

    private class GroupSynchronizationResult {

    	private final int addedMembers;
		private final int removedMembers;

    	private GroupSynchronizationResult(int addedMembers, int removedMembers) {
			this.addedMembers = addedMembers;
			this.removedMembers = removedMembers;
		}

		private int getAddedMembers() {
			return addedMembers;
		}

		private int getRemovedMembers() {
			return removedMembers;
		}
	}
}
