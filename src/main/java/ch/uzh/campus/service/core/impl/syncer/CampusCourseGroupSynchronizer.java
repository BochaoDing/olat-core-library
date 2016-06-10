package ch.uzh.campus.service.core.impl.syncer;

import java.util.ArrayList;
import java.util.List;


import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupService;
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
 * 
 * All lecturers are added as owners to the course. <br>
 * All students are added as participants to GroupA and all lecturers are added as coaches of both GroupA and GroupB.
 * 
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
    RepositoryService repositoryService;

    @Autowired
    BusinessGroupService businessGroupService;    
    
    @Autowired
    DB dBImpl;
   

   public void addAllLecturesAsOwner(CampusCourse campusCourse, List<Identity> lecturers) {
        addAllIdentitiesAsOwner(campusCourse, lecturers);
    }
   
   private void addAllIdentitiesAsOwner(CampusCourse campusCourse, List<Identity> identities) {
       //synchronizeSecurityGroup(campusCourse.getRepositoryEntry().getOwnerGroup(), identities, false);	   
	   addAsOwners(campusCourse, identities);
   }

   /**
    * Adds the list of identities as owners of this resource/course.
    * @param campusCourse
    * @param identities
    */
   private void addAsOwners(CampusCourse campusCourse, List<Identity> identities) {	   
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
	   commitDBImplTransaction();
	}
   
   public void addDefaultCoOwnersAsOwner(CampusCourse campusCourse) {
       //addNewMembersToSecurityGroup(campusCourse.getRepositoryEntry().getOwnerGroup(), campuskursCoOwners.getDefaultCoOwners());
	   addAsOwners(campusCourse, campuskursCoOwners.getDefaultCoOwners());
   }
     
   public List<Identity> getCampusGroupAParticipants(CampusCourse campusCourse) {
        BusinessGroup campusGroupA = CampusGroupHelper.lookupCampusGroup(campusCourse.getCourse(), campusConfiguration.getCourseGroupAName());        
        List<Identity> participants = businessGroupService.getMembers(campusGroupA, GroupRoles.participant.name());
        return participants;
   }
      

   /**
    * Synchronizes the coaches of the GroupB, and the coaches and participants of the GroupA.
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
    	//commitDBImplTransaction();
    	return new SynchronizedSecurityGroupStatistic(businessGroupAddResponse.getAddedIdentities().size(), 0);
    }
    
    private SynchronizedSecurityGroupStatistic synchronizeGroupParticipants(Identity courseOwner, BusinessGroup businessGroup, List<Identity> participants) {
    	int removedIdentityCounter = removeNonMembersFromSecurityGroup(courseOwner, businessGroup, participants);       
        int addedIdentityCounter = addNewMembersToSecurityGroup(courseOwner, businessGroup, participants);

        return new SynchronizedSecurityGroupStatistic(addedIdentityCounter, removedIdentityCounter);
    }
          
    
    private int removeNonMembersFromSecurityGroup(Identity courseOwner, BusinessGroup businessGroup, List<Identity> allNewMembers) {
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
          businessGroupService.removeParticipants(courseOwner, removableMembers, businessGroup, null);
        }
        //commitDBImplTransaction();
        return removedIdentityCounter;
    }
    
    private int addNewMembersToSecurityGroup(Identity courseOwner, BusinessGroup businessGroup, List<Identity> allNewMembers) {    
    	
        BusinessGroupAddResponse businessGroupAddResponse = businessGroupService.addParticipants(courseOwner, null, allNewMembers, businessGroup, null);
        //commitDBImplTransaction();
        int addedIdentityCounter = businessGroupAddResponse.getAddedIdentities().size(); // - businessGroupAddResponse.getIdentitiesAlreadyInGroup().size();
        System.out.println("added identities: " + businessGroupAddResponse.getAddedIdentities().size());
        return addedIdentityCounter;
    }
    
    // only for testing
    public void setCampusConfiguration(CampusConfiguration campusConfigurationMock) {
        this.campusConfiguration = campusConfigurationMock;
    }
    
    private void commitDBImplTransaction() {
    	dBImpl.intermediateCommit();
    }
       
}
