package ch.uzh.campus.service.core.impl.syncer;

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
/*import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.core.course.campus.CampusConfiguration;
import org.olat.lms.core.course.campus.CampusCourseImportTO;
import org.olat.lms.core.course.campus.impl.creator.CampusCourse;
import org.olat.lms.core.course.campus.impl.syncer.statistic.SynchronizedGroupStatistic;
import org.olat.lms.core.course.campus.impl.syncer.statistic.SynchronizedSecurityGroupStatistic;
import org.olat.lms.course.ICourse;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;*/
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.uzh.campus.CampusConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.creator.CourseCreateCoordinator;
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
		   if(!repositoryService.hasRole(identity, campusCourse.getRepositoryEntry(), "owner")) {
			   repositoryService.addRole(identity, campusCourse.getRepositoryEntry(), "owner");
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
  
/*
    public SynchronizedGroupStatistic synchronizeCourseGroups(ICourse course, CampusCourseImportTO campusCourseImportData) {
        BusinessGroup campusGroupA = lookupCampusGroup(course, campusConfiguration.getCourseGroupAName());
        BusinessGroup campusGroupB = lookupCampusGroup(course, campusConfiguration.getCourseGroupBName());

        synchronizeGroupOwners(campusGroupB, campusCourseImportData.getLecturers());
        SynchronizedSecurityGroupStatistic ownerGroupStatistic = synchronizeGroupOwners(campusGroupA, campusCourseImportData.getLecturers());
        SynchronizedSecurityGroupStatistic participantGroupStatistic = synchronizeGroupParticipants(campusGroupA, campusCourseImportData.getParticipants());

        return new SynchronizedGroupStatistic(campusCourseImportData.getTitle(), ownerGroupStatistic, participantGroupStatistic);
    }
    
    private SynchronizedSecurityGroupStatistic synchronizeGroupOwners(BusinessGroup businessGroup, List<Identity> lecturers) {
        //return synchronizeSecurityGroup(businessGroup.getOwnerGroup(), lecturers, false);
    	businessGroupService.addOwners(ureqIdentity, ureqRoles, addIdentities, group, mailing)
    }
       
    private SynchronizedSecurityGroupStatistic synchronizeSecurityGroup(SecurityGroup group, List<Identity> allNewMembers, boolean isParticipant) {
        int removedIdentityCounter = 0;
        if (isParticipant) {
            removedIdentityCounter = removeNonMembersFromSecurityGroup(group, allNewMembers);
        }
        int addedIdentityCounter = addNewMembersToSecurityGroup(group, allNewMembers);

        return new SynchronizedSecurityGroupStatistic(addedIdentityCounter, removedIdentityCounter);
    }
    
    private int removeNonMembersFromSecurityGroup(SecurityGroup group, List<Identity> allNewMembers) {
        int removedIdentityCounter = 0;
        List<Identity> previousMembers = baseSecurity.getIdentitiesOfSecurityGroup(group);
        commitDBImplTransaction();
        for (Identity previousMember : previousMembers) {
            // check if previous member is still in new member-list
            if (!allNewMembers.contains(previousMember)) {
                log.debug("Course-Group Synchronisation: Remove identity=" + previousMember + " from group=" + group);
                baseSecurity.removeIdentityFromSecurityGroup(previousMember, group);
                commitDBImplTransaction();
                removedIdentityCounter++;
            }
        }
        return removedIdentityCounter;
    }
    */
    
   /*
    public SynchronizedGroupStatistic synchronizeCourseGroupsForStudentsOnly(ICourse course, CampusCourseImportTO campusCourseImportData) {
        BusinessGroup campusGroupA = CampusGroupHelper.lookupCampusGroup(course, campusConfiguration.getCourseGroupAName());
        SynchronizedSecurityGroupStatistic participantGroupStatistic = synchronizeGroupParticipants(campusGroupA, campusCourseImportData.getParticipants());
        return new SynchronizedGroupStatistic(campusCourseImportData.getTitle(), null, participantGroupStatistic);
    }

    private SynchronizedSecurityGroupStatistic synchronizeGroupParticipants(BusinessGroup businessGroup, List<Identity> participants) {
        return synchronizeSecurityGroup(businessGroup.getPartipiciantGroup(), participants, true);
    }

    
   

    private int addNewMembersToSecurityGroup(SecurityGroup group, List<Identity> allNewMembers) {
        int addedIdentityCounter = 0;
        for (Identity identity : allNewMembers) {
            if (!baseSecurity.isIdentityInSecurityGroup(identity, group)) {
                // log.debug("Course-Group Synchronisation: Add identity=" + identity + " to group=" + group);
                baseSecurity.addIdentityToSecurityGroup(identity, group);
                addedIdentityCounter++;
            }
            commitDBImplTransaction();
        }
        return addedIdentityCounter;

    }

    // only for testing
    public void setCampusConfiguration(CampusConfiguration campusConfigurationMock) {
        this.campusConfiguration = campusConfigurationMock;
    }

    @SuppressWarnings("deprecation")
    private void commitDBImplTransaction() {
        if (dBImpl == null) {
            dBImpl = DBFactory.getInstance(false);
        }
        // TO FIX A PROBLEM DO NOT COMMIT THE TRANSACTION FOR NOW
        // dBImpl.intermediateCommit();
    }
   */
    
    public static BusinessGroup lookupCampusGroup(ICourse course, String campusGruppe) {
        CourseGroupManager courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();
        
        List <BusinessGroup> foundCampusGroups = courseGroupManager.getAllBusinessGroups();
        
        // TODO: Possible problem to many groups => Solution : lookup only in default context
        //List foundCampusGroups = courseGroupManager.getLearningGroupsFromAllContexts(campusGruppe, course);
        if (foundCampusGroups.isEmpty()) {
            log.error("Found no course-group with name=" + campusGruppe);
            throw new AssertException("Found no course-group with name=" + campusGruppe);
        }
        if (foundCampusGroups.size() > 1) {
            log.error("Found more than one course-group with name=" + campusGruppe);
            throw new AssertException("Found more than one course-group with name=" + campusGruppe);
        }
        
        for(BusinessGroup businessGroup:foundCampusGroups ) {
        	if(businessGroup.getName().equals(campusGruppe)) {
        		return businessGroup;
        	}
        }
        return null;
    }

}
