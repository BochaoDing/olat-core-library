package ch.uzh.campus.service.core.impl.syncer;

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
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
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.creator.CourseCreateCoordinator;

/**
 * Initial Date: 20.06.2012 <br>
 * 
 * @author cg
 */
@Component
public class CampusCourseGroupSynchronizer {
    //private static final Logger log = LoggerHelper.getLogger();
	private static final OLog log = Tracing.createLoggerFor(CampusCourseGroupSynchronizer.class);

    @Autowired
    CampusConfiguration campusConfiguration;
    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    CampuskursCoOwners campuskursCoOwners;
    DB dBImpl;

    /*public void addAllLecturesAsOwner(CampusCourse campusCourse, List<Identity> lecturers) {
        addAllIdentitiesAsOwner(campusCourse, lecturers);
    }

    public void addAllLecturesAsOwner(ICourse course, List<Identity> lecturers) {
        RepositoryEntry repositoryEntry = getRepositoryService().lookupRepositoryEntry(course.getCourseEnvironment().getRepositoryEntryId());
        synchronizeSecurityGroup(repositoryEntry.getOwnerGroup(), lecturers, false);
    }

    public void addAllDelegateesAsOwner(CampusCourse campusCourse, List<Identity> delegatees) {
        addAllIdentitiesAsOwner(campusCourse, delegatees);
    }

    private void addAllIdentitiesAsOwner(CampusCourse campusCourse, List<Identity> identities) {
        synchronizeSecurityGroup(campusCourse.getRepositoryEntry().getOwnerGroup(), identities, false);
    }

    public List<Identity> getCampusGroupAParticipants(CampusCourse campusCourse) {
        BusinessGroup campusGroupA = CampusGroupHelper.lookupCampusGroup(campusCourse.getCourse(), campusConfiguration.getCourseGroupAName());
        List<Identity> participants = baseSecurity.getIdentitiesOfSecurityGroup(campusGroupA.getPartipiciantGroup());
        return participants;
    }

    public void addDefaultCoOwnersAsOwner(CampusCourse campusCourse) {
        addNewMembersToSecurityGroup(campusCourse.getRepositoryEntry().getOwnerGroup(), campuskursCoOwners.getDefaultCoOwners());
    }

    public SynchronizedGroupStatistic synchronizeCourseGroups(ICourse course, CampusCourseImportTO campusCourseImportData) {
        BusinessGroup campusGroupA = CampusGroupHelper.lookupCampusGroup(course, campusConfiguration.getCourseGroupAName());
        BusinessGroup campusGroupB = CampusGroupHelper.lookupCampusGroup(course, campusConfiguration.getCourseGroupBName());

        synchronizeGroupOwners(campusGroupB, campusCourseImportData.getLecturers());
        SynchronizedSecurityGroupStatistic ownerGroupStatistic = synchronizeGroupOwners(campusGroupA, campusCourseImportData.getLecturers());
        SynchronizedSecurityGroupStatistic participantGroupStatistic = synchronizeGroupParticipants(campusGroupA, campusCourseImportData.getParticipants());

        return new SynchronizedGroupStatistic(campusCourseImportData.getTitle(), ownerGroupStatistic, participantGroupStatistic);
    }

    public SynchronizedGroupStatistic synchronizeCourseGroupsForStudentsOnly(ICourse course, CampusCourseImportTO campusCourseImportData) {
        BusinessGroup campusGroupA = CampusGroupHelper.lookupCampusGroup(course, campusConfiguration.getCourseGroupAName());
        SynchronizedSecurityGroupStatistic participantGroupStatistic = synchronizeGroupParticipants(campusGroupA, campusCourseImportData.getParticipants());
        return new SynchronizedGroupStatistic(campusCourseImportData.getTitle(), null, participantGroupStatistic);
    }

    private SynchronizedSecurityGroupStatistic synchronizeGroupParticipants(BusinessGroup businessGroup, List<Identity> participants) {
        return synchronizeSecurityGroup(businessGroup.getPartipiciantGroup(), participants, true);
    }

    private SynchronizedSecurityGroupStatistic synchronizeGroupOwners(BusinessGroup businessGroup, List<Identity> lecturers) {
        return synchronizeSecurityGroup(businessGroup.getOwnerGroup(), lecturers, false);
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

    private RepositoryService getRepositoryService() {
        return CoreSpringFactory.getBean(RepositoryServiceImpl.class);
    }*/

}
