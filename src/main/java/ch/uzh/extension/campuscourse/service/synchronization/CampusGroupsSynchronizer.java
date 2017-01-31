package ch.uzh.extension.campuscourse.service.synchronization;

import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.model.CampusGroups;
import ch.uzh.extension.campuscourse.model.CampusCourseTO;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
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
    
    private GroupSynchronizationResult synchronizeGroupCoaches(Identity courseOwner, BusinessGroup businessGroup, Set<Identity> lecturers, Set<Identity> delegatees) {
        Set<Identity> lecturersAndDelegatees = new HashSet<>(lecturers);
        lecturersAndDelegatees.addAll(delegatees);
        // addChoaches() would be the better name, since businessGroupService.addOwners() adds the role of a coach
        // (coach == owner for business groups)
    	BusinessGroupAddResponse businessGroupAddResponse = businessGroupService.addOwners(courseOwner, null, new ArrayList<>(lecturersAndDelegatees), businessGroup, null);
    	return new GroupSynchronizationResult(businessGroupAddResponse.getAddedIdentities().size(), 0);
    }
    
    private GroupSynchronizationResult synchronizeGroupParticipants(Identity courseOwner, BusinessGroup businessGroup, Set<Identity> participants) {
    	int removedParticipants = removeNonParticipantsFromBusinessGroup(courseOwner, businessGroup, participants);
        int addedParticipants = addNewParticipantsToBusinessGroup(courseOwner, businessGroup, participants);
        return new GroupSynchronizationResult(addedParticipants, removedParticipants);
    }

    private int removeNonParticipantsFromBusinessGroup(Identity courseOwner, BusinessGroup businessGroup, Set<Identity> allNewParticipants) {
        int removedParticipants = 0;
        List<Identity> previousMembers = businessGroupService.getMembers(businessGroup, GroupRoles.participant.name());
        List<Identity> removableParticipants = new ArrayList<>();
        for (Identity previousMember : previousMembers) {
            // Check if previous member is still in new member-list
            if (!allNewParticipants.contains(previousMember)) {
                LOG.debug("Campus course ynchronization: Remove participant '" + previousMember + "' from group =" + businessGroup);
                removableParticipants.add(previousMember);
                removedParticipants++;
            }
        }
        if (removableParticipants.size() > 0) {
            businessGroupService.removeParticipants(courseOwner, removableParticipants, businessGroup, null);
        }
        return removedParticipants;
    }
    
    private int addNewParticipantsToBusinessGroup(Identity courseOwner, BusinessGroup businessGroup, Set<Identity> allNewMembers) {
        BusinessGroupAddResponse businessGroupAddResponse = businessGroupService.addParticipants(courseOwner, null, new ArrayList<>(allNewMembers), businessGroup, null);
        int addedParticipants = businessGroupAddResponse.getAddedIdentities().size();
        LOG.debug("added participants: " + addedParticipants);
        return addedParticipants;
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
