package ch.uzh.campus.service.core.impl.syncer;

import ch.uzh.campus.CampusCourseException;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.CampusCourseGroups;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedGroupStatistic;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedSecurityGroupStatistic;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Initial Date: 20.06.2012 <br>
 * 
 * @author cg
 * 
 * All lecturers are added as owners to the course. <br>
 * All students are added as participants to GroupA and all lecturers are added as coaches of both GroupA and GroupB.
 * 
 */
@Component
public class CampusCourseGroupSynchronizer {
    
	private static final OLog LOG = Tracing.createLoggerFor(CampusCourseGroupSynchronizer.class);

    private final CampusCourseCoOwners campusCourseCoOwners;
    private final RepositoryService repositoryService;
    private final BusinessGroupService businessGroupService;
    private final CampusCourseGroupsFinder campusCourseGroupsFinder;

    @Autowired
    public CampusCourseGroupSynchronizer(CampusCourseCoOwners campusCourseCoOwners,
                                         RepositoryService repositoryService,
                                         BusinessGroupService businessGroupService,
                                         CampusCourseGroupsFinder campusCourseGroupsFinder) {
        this.campusCourseCoOwners = campusCourseCoOwners;
        this.repositoryService = repositoryService;
        this.businessGroupService = businessGroupService;
        this.campusCourseGroupsFinder = campusCourseGroupsFinder;
    }

    public void addGroupOwnerRoleToLecturers(CampusCourse campusCourse, List<Identity> lecturers) {
        addGroupOwnerRole(campusCourse, lecturers);
    }

   /**
    * Adds the list of identities as owners of this resource/course.
    */
   private void addGroupOwnerRole(CampusCourse campusCourse, List<Identity> identities) {
	   for (Identity identity : identities) {
		   if (!repositoryService.hasRole(identity, campusCourse.getRepositoryEntry(), GroupRoles.owner.name())) {
			   repositoryService.addRole(identity, campusCourse.getRepositoryEntry(), GroupRoles.owner.name());
		   }
	   }
	}
   
   public void addGroupOwnerRoleToCoOwners(CampusCourse campusCourse) {
	   addGroupOwnerRole(campusCourse, campusCourseCoOwners.getDefaultCoOwners());
   }
     
   public List<Identity> getCampusGroupAParticipants(CampusCourse campusCourse) {
       CampusCourseGroups campusCourseGroups = campusCourseGroupsFinder.findCampusCourseGroups(campusCourse.getRepositoryEntry());
       assert campusCourseGroups != null;
       BusinessGroup campusGroupA = campusCourseGroups.getCampusCourseGroupA();
       assert campusGroupA != null;
       return businessGroupService.getMembers(campusGroupA, GroupRoles.participant.name());
   }

   public SynchronizedGroupStatistic synchronizeCourseGroups(CampusCourse campusCourse,
                                                             CampusCourseImportTO campusCourseImportData) throws CampusCourseException {
       CampusCourseGroups campusCourseGroups = campusCourseGroupsFinder.findCampusCourseGroups(campusCourse.getRepositoryEntry());
       if (campusCourseGroups == null || campusCourseGroups.getCampusCourseGroupA() == null || campusCourseGroups.getCampusCourseGroupB() == null) {
           throw new CampusCourseException("Campus course groups A and/or B do not exist or campus learning area does not exist or missing database relation between campus course groups and campus course learning area");
       }
       return synchronizeCourseGroups(campusCourse, campusCourseImportData, campusCourseGroups);
   }

   /**
    * Synchronizes the coaches of the GroupB, and the coaches and participants of the GroupA.
    */
    public SynchronizedGroupStatistic synchronizeCourseGroups(CampusCourse campusCourse,
                                                              CampusCourseImportTO campusCourseImportData,
                                                              CampusCourseGroups campusCourseGroups) {
        assert campusCourseGroups != null;

        BusinessGroup campusGroupA = campusCourseGroups.getCampusCourseGroupA();
        BusinessGroup campusGroupB = campusCourseGroups.getCampusCourseGroupB();

        assert campusGroupA != null;
        assert campusGroupB != null;

        // Get the course owner identities
        List<Identity> courseOwners = repositoryService.getMembers(campusCourse.getRepositoryEntry(), GroupRoles.owner.name());

        synchronizeGroupOwners(courseOwners.get(0), campusGroupB, campusCourseImportData.getLecturersOfCourseAndParentCourses());
        SynchronizedSecurityGroupStatistic ownerGroupStatistic = synchronizeGroupOwners(courseOwners.get(0), campusGroupA, campusCourseImportData.getLecturersOfCourseAndParentCourses());
        SynchronizedSecurityGroupStatistic participantGroupStatistic = synchronizeGroupParticipants(courseOwners.get(0), campusGroupA, campusCourseImportData.getParticipantsOfCourseAndParentCourses());
        
        return new SynchronizedGroupStatistic(campusCourseImportData.getTitle(), ownerGroupStatistic, participantGroupStatistic);
    }
    
    private SynchronizedSecurityGroupStatistic synchronizeGroupOwners(Identity courseOwner, BusinessGroup businessGroup, List<Identity> lecturers) {
    	BusinessGroupAddResponse businessGroupAddResponse = businessGroupService.addOwners(courseOwner, null, lecturers, businessGroup, null);
    	return new SynchronizedSecurityGroupStatistic(businessGroupAddResponse.getAddedIdentities().size(), 0);
    }
    
    private SynchronizedSecurityGroupStatistic synchronizeGroupParticipants(Identity courseOwner, BusinessGroup businessGroup, List<Identity> participants) {
    	int removedIdentityCounter = removeNonMembersFromBusinessGroup(courseOwner, businessGroup, participants);
        int addedIdentityCounter = addNewMembersToBusinessGroup(courseOwner, businessGroup, participants);

        return new SynchronizedSecurityGroupStatistic(addedIdentityCounter, removedIdentityCounter);
    }

    private int removeNonMembersFromBusinessGroup(Identity courseOwner, BusinessGroup businessGroup, List<Identity> allNewMembers) {
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
    
    private int addNewMembersToBusinessGroup(Identity courseOwner, BusinessGroup businessGroup, List<Identity> allNewMembers) {
        BusinessGroupAddResponse businessGroupAddResponse = businessGroupService.addParticipants(courseOwner, null, allNewMembers, businessGroup, null);
        int addedIdentityCounter = businessGroupAddResponse.getAddedIdentities().size();
        LOG.debug("added identities: " + addedIdentityCounter);
        return addedIdentityCounter;
    }

    CampusCourseCoOwners getCampusCourseCoOwners() {
        return campusCourseCoOwners;
    }
}
