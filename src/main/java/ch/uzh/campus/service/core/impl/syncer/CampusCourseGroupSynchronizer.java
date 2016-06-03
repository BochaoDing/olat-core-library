package ch.uzh.campus.service.core.impl.syncer;

import java.util.ArrayList;
import java.util.List;


import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;

import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupService;

import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.uzh.campus.CampusConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.service.CampusCourse;

import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedGroupStatistic;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedSecurityGroupStatistic;

/**
 * Initial Date: 20.06.2012 <br>
 * 
 * @author cg
 */
@Component
public class CampusCourseGroupSynchronizer {
    
	private static final OLog log = Tracing.createLoggerFor(CampusCourseGroupSynchronizer.class);

    @Autowired
    CampusConfiguration campusConfiguration;
    
    @Autowired
    BaseSecurity baseSecurity;

    @Autowired
    CampuskursCoOwners campuskursCoOwners;

    @Autowired
    RepositoryManager repositoryManager;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    BusinessGroupService businessGroupService;    
    

    public void setCampusConfiguration(CampusConfiguration campusConfiguration) {
        this.campusConfiguration = campusConfiguration;
    }

    public CampuskursCoOwners getCampuskursCoOwners() {
        return campuskursCoOwners;
    }

    @Autowired
    DB dBImpl;

   public void addAllLecturesAsOwner(CampusCourse campusCourse, List<Identity> lecturers) {
        addAllIdentitiesAsOwner(campusCourse, lecturers);
    }
   
   private void addAllIdentitiesAsOwner(CampusCourse campusCourse, List<Identity> identities) {
       //synchronizeSecurityGroup(campusCourse.getRepositoryEntry().getOwnerGroup(), identities, false);	   
	   addAsOwners(campusCourse, identities);
   }

   private void addAsOwners(CampusCourse campusCourse, List<Identity> identities) {
	   //TODO: olatng 
	   //var 1: use the repositoryManager
	   //IdentitiesAddEvent iae = new IdentitiesAddEvent(identities);
	   //Identity addingIdentity = campuskursCoOwners.getDefaultCoOwners().get(0);//or get the campuskursAdminIdentity
	   //repositoryManager.addOwners(addingIdentity, iae, campusCourse.getRepositoryEntry());
	   
	   //var 2: use the repositoryService 
	   for (Identity identity: identities) {
		   if(!repositoryService.hasRole(identity, campusCourse.getRepositoryEntry(), GroupRoles.owner.name())) {
			   repositoryService.addRole(identity, campusCourse.getRepositoryEntry(), GroupRoles.owner.name());
		   }
	   }
	}
   
   public void addDefaultCoOwnersAsOwner(CampusCourse campusCourse) {
       //addNewMembersToSecurityGroup(campusCourse.getRepositoryEntry().getOwnerGroup(), campuskursCoOwners.getDefaultCoOwners());
	   addAsOwners(campusCourse, campuskursCoOwners.getDefaultCoOwners());
   }
   
   
   /**/
   
   /*
    public void addAllLecturesAsOwner(ICourse course, List<Identity> lecturers) {
        RepositoryEntry repositoryEntry = getRepositoryService().lookupRepositoryEntry(course.getCourseEnvironment().getRepositoryEntryId());
        synchronizeSecurityGroup(repositoryEntry.getOwnerGroup(), lecturers, false);
    }

    public void addAllDelegateesAsOwner(CampusCourse campusCourse, List<Identity> delegatees) {
        addAllIdentitiesAsOwner(campusCourse, delegatees);
    }
   

    public List<Identity> getCampusGroupAParticipants(CampusCourse campusCourse) {
        BusinessGroup campusGroupA = CampusGroupHelper.lookupCampusGroup(campusCourse.getCourse(), campusConfiguration.getCourseGroupAName());
        List<Identity> participants = baseSecurity.getIdentitiesOfSecurityGroup(campusGroupA.getPartipiciantGroup());
        return participants;
    }

   */     

   /**
    * Synchronizes the owners of the GroupB, and the owners and participants of the GroupA.
    * @param course
    * @param campusCourseImportData
    * @return
    */
    public SynchronizedGroupStatistic synchronizeCourseGroups(CampusCourse campusCourse, CampusCourseImportTO campusCourseImportData) {
        BusinessGroup campusGroupA = CampusGroupHelper.lookupCampusGroup(campusCourse.getCourse(), campusConfiguration.getCourseGroupAName());
        BusinessGroup campusGroupB = CampusGroupHelper.lookupCampusGroup(campusCourse.getCourse(), campusConfiguration.getCourseGroupBName());
        
        //get the course owner identity
        List<Identity> courseOwners = repositoryService.getMembers(campusCourse.getRepositoryEntry(), GroupRoles.owner.name());

        synchronizeGroupOwners(courseOwners.get(0), campusGroupB, campusCourseImportData.getLecturers());
        SynchronizedSecurityGroupStatistic ownerGroupStatistic = synchronizeGroupOwners(courseOwners.get(0), campusGroupA, campusCourseImportData.getLecturers());        
        SynchronizedSecurityGroupStatistic participantGroupStatistic = synchronizeGroupParticipants(courseOwners.get(0), campusGroupA, campusCourseImportData.getParticipants());
        
        return new SynchronizedGroupStatistic(campusCourseImportData.getTitle(), ownerGroupStatistic, participantGroupStatistic);
    }
    
    private SynchronizedSecurityGroupStatistic synchronizeGroupOwners(Identity courseOwner, BusinessGroup businessGroup, List<Identity> lecturers) {
        //return synchronizeSecurityGroup(businessGroup.getOwnerGroup(), lecturers, false);
    	BusinessGroupAddResponse businessGroupAddResponse = businessGroupService.addOwners(courseOwner, null, lecturers, businessGroup, null);
    	dBImpl.commitAndCloseSession();
    	return new SynchronizedSecurityGroupStatistic(businessGroupAddResponse.getAddedIdentities().size(), 0);
    }
    
    private SynchronizedSecurityGroupStatistic synchronizeGroupParticipants(Identity courseOwner, BusinessGroup businessGroup, List<Identity> participants) {
    	int removedIdentityCounter = removeNonMembersFromSecurityGroup(businessGroup, participants);       
        int addedIdentityCounter = addNewMembersToSecurityGroup(courseOwner, businessGroup, participants);

        return new SynchronizedSecurityGroupStatistic(addedIdentityCounter, removedIdentityCounter);
    }
          
    
    private int removeNonMembersFromSecurityGroup(BusinessGroup businessGroup, List<Identity> allNewMembers) {
        int removedIdentityCounter = 0;        
        List<Identity> previousMembers = businessGroupService.getMembers(businessGroup, GroupRoles.participant.name());
        List<Identity> removableMembers = new ArrayList<Identity>();
        for (Identity previousMember : previousMembers) {
            // check if previous member is still in new member-list
            if (!allNewMembers.contains(previousMember)) {
                log.debug("Course-Group Synchronisation: Remove identity=" + previousMember + " from group=" + businessGroup);
                removableMembers.add(previousMember);                
                removedIdentityCounter++;
            }
        }
        if(removableMembers.size()>0) {
          businessGroupService.removeParticipants(null, removableMembers, businessGroup, null);
        }
        dBImpl.commitAndCloseSession();
        return removedIdentityCounter;
    }
    
    private int addNewMembersToSecurityGroup(Identity courseOwner, BusinessGroup businessGroup, List<Identity> allNewMembers) {       
        BusinessGroupAddResponse businessGroupAddResponse = businessGroupService.addParticipants(courseOwner, null, allNewMembers, businessGroup, null);
        dBImpl.commitAndCloseSession();
        int addedIdentityCounter = businessGroupAddResponse.getAddedIdentities().size() - businessGroupAddResponse.getIdentitiesAlreadyInGroup().size();                
        return addedIdentityCounter;
    }
    
    
   /*
    public SynchronizedGroupStatistic synchronizeCourseGroupsForStudentsOnly(ICourse course, CampusCourseImportTO campusCourseImportData) {
        BusinessGroup campusGroupA = CampusGroupHelper.lookupCampusGroup(course, campusConfiguration.getCourseGroupAName());
        SynchronizedSecurityGroupStatistic participantGroupStatistic = synchronizeGroupParticipants(campusGroupA, campusCourseImportData.getParticipants());
        return new SynchronizedGroupStatistic(campusCourseImportData.getTitle(), null, participantGroupStatistic);
    }

*/
  
}
