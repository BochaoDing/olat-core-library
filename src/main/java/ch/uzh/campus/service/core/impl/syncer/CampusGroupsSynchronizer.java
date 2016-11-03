package ch.uzh.campus.service.core.impl.syncer;

import ch.uzh.campus.CampusCourseException;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedGroupStatistic;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedSecurityGroupStatistic;
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
    public SynchronizedGroupStatistic synchronizeCampusGroups(BusinessGroup campusGroupA,
                                                              BusinessGroup campusGroupB,
                                                              Set<Identity> lecturers,
                                                              Set<Identity> delegatees,
                                                              Set<Identity> participants,
                                                              String titleToBeDisplayed,
                                                              Identity creator) throws CampusCourseException {

        if (campusGroupA == null) {
            throw new CampusCourseException("Campus course groups A does not exist");
        }
        if (campusGroupB == null) {
            throw new CampusCourseException("Campus course groups B does not exist");
        }

        // Synchronize group coaches and participants of campus group A
        SynchronizedSecurityGroupStatistic coachGroupStatistic = synchronizeGroupCoaches(creator, campusGroupA, lecturers, delegatees);
        SynchronizedSecurityGroupStatistic participantGroupStatistic = synchronizeGroupParticipants(creator, campusGroupA, participants);

        // Synchronize group coaches of campus group B
        synchronizeGroupCoaches(creator, campusGroupB, lecturers, delegatees);
        
        return new SynchronizedGroupStatistic(titleToBeDisplayed, coachGroupStatistic, participantGroupStatistic);
    }
    
    private SynchronizedSecurityGroupStatistic synchronizeGroupCoaches(Identity courseOwner, BusinessGroup businessGroup, Set<Identity> lecturers, Set<Identity> delegatees) {
        Set<Identity> lecturersAndDelegatees = new HashSet<>(lecturers);
        lecturersAndDelegatees.addAll(delegatees);
        // addChoaches() would be the better name, since businessGroupService.addOwners() adds the role of a coach
        // (coach == owner for business groups)
    	BusinessGroupAddResponse businessGroupAddResponse = businessGroupService.addOwners(courseOwner, null, new ArrayList<>(lecturersAndDelegatees), businessGroup, null);
    	return new SynchronizedSecurityGroupStatistic(businessGroupAddResponse.getAddedIdentities().size(), 0);
    }
    
    private SynchronizedSecurityGroupStatistic synchronizeGroupParticipants(Identity courseOwner, BusinessGroup businessGroup, Set<Identity> participants) {
    	int removedIdentityCounter = removeNonParticipantsFromBusinessGroup(courseOwner, businessGroup, participants);
        int addedIdentityCounter = addNewParticipantsToBusinessGroup(courseOwner, businessGroup, participants);
        return new SynchronizedSecurityGroupStatistic(addedIdentityCounter, removedIdentityCounter);
    }

    private int removeNonParticipantsFromBusinessGroup(Identity courseOwner, BusinessGroup businessGroup, Set<Identity> allNewMembers) {
        int removedIdentityCounter = 0;        
        List<Identity> previousMembers = businessGroupService.getMembers(businessGroup, GroupRoles.participant.name());
        List<Identity> removableMembers = new ArrayList<>();
        for (Identity previousMember : previousMembers) {
            // Check if previous member is still in new member-list
            if (!allNewMembers.contains(previousMember)) {
                LOG.debug("Course-Group Synchronisation: Remove identity =" + previousMember + " from group =" + businessGroup);
                removableMembers.add(previousMember);                
                removedIdentityCounter++;
            }
        }
        if (removableMembers.size() > 0) {
            businessGroupService.removeParticipants(courseOwner, removableMembers, businessGroup, null);
        }
        return removedIdentityCounter;
    }
    
    private int addNewParticipantsToBusinessGroup(Identity courseOwner, BusinessGroup businessGroup, Set<Identity> allNewMembers) {
        BusinessGroupAddResponse businessGroupAddResponse = businessGroupService.addParticipants(courseOwner, null, new ArrayList<>(allNewMembers), businessGroup, null);
        int addedIdentityCounter = businessGroupAddResponse.getAddedIdentities().size();
        LOG.debug("added identities: " + addedIdentityCounter);
        return addedIdentityCounter;
    }
}