package ch.uzh.extension.campuscourse.service;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.data.entity.Course;
import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import ch.uzh.extension.campuscourse.model.SapUserType;
import ch.uzh.extension.campuscourse.olat.admin.CampusCourseEvent;
import ch.uzh.extension.campuscourse.service.coursecreation.CampusCoursePublisher;
import ch.uzh.extension.campuscourse.service.coursecreation.CampusGroupsCreator;
import ch.uzh.extension.campuscourse.service.coursecreation.OlatCampusCourseCreator;
import ch.uzh.extension.campuscourse.model.CampusGroups;
import ch.uzh.extension.campuscourse.service.synchronization.CampusCourseDefaultCoOwners;
import ch.uzh.extension.campuscourse.service.synchronization.CampusCourseRepositoryEntrySynchronizer;
import ch.uzh.extension.campuscourse.service.synchronization.CampusGroupsSynchronizer;
import ch.uzh.extension.campuscourse.model.CampusCourseTO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.tree.PublishTreeModel;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 *
 * Use this service only within a {@link javax.servlet.Servlet}. Otherwise commitAndCloseSession / closeSession would be needed.
 *
 * Initial Date: 16.07.2012 <br>
 * 
 * @author cg
 */
@Service
public class CampusCourseCoreServiceImpl implements CampusCourseCoreService {
	
	private static final OLog LOG = Tracing.createLoggerFor(CampusCourseCoreServiceImpl.class);

    private final DB dbInstance;
    private final DaoManager daoManager;
    private final RepositoryService repositoryService;
    private final OlatCampusCourseCreator olatCampusCourseCreator;
    private final CampusCoursePublisher campusCoursePublisher;
    private final CampusGroupsCreator campusGroupsCreator;
    private final CampusCourseConfiguration campusCourseConfiguration;
    private final CampusGroupsSynchronizer campusGroupsSynchronizer;
    private final CampusCourseRepositoryEntrySynchronizer campusCourseRepositoryEntrySynchronizer;
    private final OLATResourceManager olatResourceManager;
    private final BusinessGroupService businessGroupService;
    private final CampusCourseDefaultCoOwners campusCourseDefaultCoOwners;

    @Autowired
    public CampusCourseCoreServiceImpl(DB dbInstance,
                                       DaoManager daoManager,
                                       RepositoryService repositoryService,
                                       OlatCampusCourseCreator olatCampusCourseCreator,
                                       CampusCoursePublisher campusCoursePublisher,
                                       CampusGroupsCreator campusGroupsCreator,
                                       CampusCourseConfiguration campusCourseConfiguration,
                                       CampusGroupsSynchronizer campusGroupsSynchronizer,
                                       CampusCourseRepositoryEntrySynchronizer campusCourseRepositoryEntrySynchronizer,
                                       OLATResourceManager olatResourceManager,
                                       BusinessGroupService businessGroupService,
                                       CampusCourseDefaultCoOwners campusCourseDefaultCoOwners) {
        this.dbInstance = dbInstance;
        this.daoManager = daoManager;
        this.repositoryService = repositoryService;
        this.olatCampusCourseCreator = olatCampusCourseCreator;
        this.campusCoursePublisher = campusCoursePublisher;
        this.campusGroupsCreator = campusGroupsCreator;
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.campusGroupsSynchronizer = campusGroupsSynchronizer;
        this.campusCourseRepositoryEntrySynchronizer = campusCourseRepositoryEntrySynchronizer;
        this.olatResourceManager = olatResourceManager;
        this.businessGroupService = businessGroupService;
        this.campusCourseDefaultCoOwners = campusCourseDefaultCoOwners;
    }

    @Override
    public boolean isIdentityLecturerOrDelegateeOfSapCourse(Long sapCampusCourseId, Identity identity) {
        CampusCourseTO campusCourseTO = daoManager.loadCampusCourseTO(sapCampusCourseId);
        for (Identity lecturerOfCourse : campusCourseTO.getLecturersOfCourse()) {
            if (lecturerOfCourse.getName().equalsIgnoreCase(identity.getName())) {
                return true;
            }
        }
        for (Identity delegateeOfCourse : campusCourseTO.getDelegateesOfCourse()) {
            if (delegateeOfCourse.getName().equalsIgnoreCase(identity.getName())) {
                return true;
            }
        }
        return false;
    }

	@Override
    public RepositoryEntry createOlatCampusCourseFromStandardTemplate(Long sapCampusCourseId, Identity creator) throws Exception {
		assert sapCampusCourseId != null;
        assert creator != null;

        // Load campus course TO
		CampusCourseTO campusCourseTO = daoManager.loadCampusCourseTO(sapCampusCourseId);

		Long standardTemplateRepositoryEntryId = campusCourseConfiguration.getTemplateRepositoryEntryId(campusCourseTO.getLanguage());
        if (standardTemplateRepositoryEntryId == null) {
            throw new IllegalArgumentException("No standard template found for language " + campusCourseTO.getLanguage());
        }

        RepositoryEntry standardTemplateRepositoryEntry = repositoryService.loadByKey(standardTemplateRepositoryEntryId);
		if (standardTemplateRepositoryEntry == null) {
            throw new IllegalArgumentException("No standard template found with repository entry id " + standardTemplateRepositoryEntryId);
        }

		// Check if template has unpublished changes. If so return.
		ICourse standardTemplateOlatCourse = CourseFactory.loadCourse(standardTemplateRepositoryEntry);
		PublishTreeModel publishTreeModel = new PublishTreeModel(standardTemplateOlatCourse.getEditorTreeModel(), standardTemplateOlatCourse.getRunStructure());
		if (publishTreeModel.hasPublishableChanges()) {
			throw new CampusCourseException("Campus course template " +
					standardTemplateOlatCourse.getCourseTitle() + " (" +
					standardTemplateOlatCourse.getResourceableId() +
					") is not published completely.");
		}

		return createOlatCampusCourseFromTemplate(standardTemplateRepositoryEntry, campusCourseTO, creator, true);
	}

    @Override
    public RepositoryEntry createOlatCampusCourseFromTemplate(RepositoryEntry templateRepositoryEntry,
                                                              Long sapCampusCourseId,
                                                              Identity creator) throws Exception {
        assert templateRepositoryEntry != null;
        assert sapCampusCourseId != null;
        assert creator != null;

        // Load campus course TO
        CampusCourseTO campusCourseTO = daoManager.loadCampusCourseTO(sapCampusCourseId);

		return createOlatCampusCourseFromTemplate(templateRepositoryEntry, campusCourseTO, creator, false);
	}

	private RepositoryEntry createOlatCampusCourseFromTemplate(RepositoryEntry templateRepositoryEntry,
                                                               CampusCourseTO campusCourseTO,
                                                               Identity creator,
                                                               boolean isStandardTemplateUsed) throws Exception {

        if (campusCourseTO.getRepositoryEntry() != null) {
            // Olat campus course already created
            return campusCourseTO.getRepositoryEntry();
        }

        RepositoryEntry createdRepositoryEntry = null;

        try {
            // Create olat course by copying the appropriate template (default or custom)
            createdRepositoryEntry = olatCampusCourseCreator.createOlatCampusCourseFromTemplate(templateRepositoryEntry, campusCourseTO, creator, isStandardTemplateUsed);

            // Update the copied course run and editor models
            ICourse updatedCourseRunAndEditorModels = olatCampusCourseCreator.updateCourseRunAndEditorModels(createdRepositoryEntry, campusCourseTO, isStandardTemplateUsed);

            // Publish run and editor models
            if (isStandardTemplateUsed) {
                campusCoursePublisher.publish(updatedCourseRunAndEditorModels, creator);
            }

            // Create campus learning area and campus groups A and B if necessary
            CampusGroups campusGroups = campusGroupsCreator.createCampusLearningAreaAndCampusGroupsIfNecessary(createdRepositoryEntry, creator, campusCourseTO.getLanguage());
            dbInstance.intermediateCommit();

            // Add course owner role to lecturers and default co-owners
            campusGroupsSynchronizer.addCourseOwnerRole(createdRepositoryEntry, campusCourseTO.getLecturersOfCourse());
            campusGroupsSynchronizer.addCourseOwnerRole(createdRepositoryEntry, campusCourseDefaultCoOwners.getDefaultCoOwners());

            // Execute the first synchronization
            try {
                campusGroupsSynchronizer.synchronizeCampusGroups(campusGroups, campusCourseTO, creator);
            } catch (CampusCourseException e) {
                LOG.error(e.getMessage());
            }

            // Add repository entry and campus groups to sap campus course
            daoManager.saveCampusCourseRepositoryEntry(campusCourseTO.getSapCourseId(), createdRepositoryEntry.getKey());
            daoManager.saveCampusGroupA(campusCourseTO.getSapCourseId(), campusGroups.getCampusGroupA().getKey());
            daoManager.saveCampusGroupB(campusCourseTO.getSapCourseId(), campusGroups.getCampusGroupB().getKey());
            dbInstance.intermediateCommit();

            // Notify possible listeners about CREATED event
            sendCampusCourseEvent(CampusCourseEvent.CREATED);

            return createdRepositoryEntry;

        } catch (Exception e1) {
            // CLEAN UP TO ENSURE CONSISTENT STATE
            if (createdRepositoryEntry != null) {
                try {
                    repositoryService.deleteRepositoryEntryAndBaseGroups(createdRepositoryEntry);
                } catch (Exception e2) {
                    // we tried best to delete entry - ignore exceptions during deletion
                }
                ICourse olatCourse = CourseFactory.loadCourse(createdRepositoryEntry);
                if (olatCourse != null) {
                    try {
                        olatResourceManager.deleteOLATResourceable(olatCourse);
                    } catch (Exception e2) {
                        // we tried best to delete entry - ignore exceptions during deletion
                    }
                }
            }
            dbInstance.rollbackAndCloseSession();

            throw e1;
        }
    }

    @Override
    public RepositoryEntry continueOlatCampusCourse(Long childSapCampusCourseId, Long parentSapCampusCourseId, Identity creator) {
		assert childSapCampusCourseId != null;
		assert parentSapCampusCourseId != null;
		assert creator != null;

		// Check first if sap campus courses exist
		Course childCourse = daoManager.getCourseById(childSapCampusCourseId);
		if (childCourse == null) {
			throw new IllegalArgumentException("SAP course does not exists: " + childSapCampusCourseId);
		}
		Course parentCourse = daoManager.getCourseById(parentSapCampusCourseId);
		if (parentCourse == null) {
			throw new IllegalArgumentException("Parent SAP course does not exists: " + parentSapCampusCourseId);
		}

		// Add parent course, repository entry and campus groups of parent course to child course
		daoManager.saveParentCourseId(childSapCampusCourseId, parentSapCampusCourseId);
        daoManager.saveCampusCourseRepositoryEntry(childSapCampusCourseId, parentCourse.getRepositoryEntry().getKey());
        daoManager.saveCampusGroupA(childSapCampusCourseId, parentCourse.getCampusGroupA().getKey());
        daoManager.saveCampusGroupB(childSapCampusCourseId, parentCourse.getCampusGroupB().getKey());
        dbInstance.intermediateCommit();

        // childCampusCourseTO must be loaded AFTER setting the parent course id and the campus groups such that
        // childCampusCourseTO also contains the lecturers and students of the parent course and the campus groups
        CampusCourseTO childCampusCourseTO = daoManager.loadCampusCourseTO(childSapCampusCourseId);

        // Update course run and editor models
        olatCampusCourseCreator.updateCourseRunAndEditorModels(childCampusCourseTO.getRepositoryEntry(), childCampusCourseTO, false);

        // Synchronize olat campus course repository entry
        campusCourseRepositoryEntrySynchronizer.synchronizeDisplaynameAndDescriptionAndInitialAuthor(childCampusCourseTO, creator);

        // Add owner role to lecturers, delegatees and default co-owners
        campusGroupsSynchronizer.addCourseOwnerRole(childCampusCourseTO.getRepositoryEntry(), childCampusCourseTO.getLecturersOfCourse());
        campusGroupsSynchronizer.addCourseOwnerRole(childCampusCourseTO.getRepositoryEntry(), childCampusCourseTO.getDelegateesOfCourse());
        campusGroupsSynchronizer.addCourseOwnerRole(childCampusCourseTO.getRepositoryEntry(), campusCourseDefaultCoOwners.getDefaultCoOwners());

        // Synchronize campus groups
        try {
            campusGroupsSynchronizer.synchronizeCampusGroups(childCampusCourseTO.getCampusGroups(), childCampusCourseTO, creator);
        } catch (CampusCourseException e) {
            LOG.error(e.getMessage());
        }

        dbInstance.intermediateCommit();

        // Notify possible listeners about CONTINUED event
        sendCampusCourseEvent(CampusCourseEvent.CONTINUED);

        return childCampusCourseTO.getRepositoryEntry();
    }

    @Override
    public Course getLatestCourseByRepositoryEntry(RepositoryEntry repositoryEntry) throws Exception {
        return daoManager.getLatestCourseByRepositoryEntry(repositoryEntry.getKey());
    }

    @Override
    public void resetRepositoryEntryAndParentCourse(RepositoryEntry repositoryEntry) {
        LOG.debug("resetRepositoryEntryAndParentCourse for repositoryentry_id =" + repositoryEntry.getKey());
        daoManager.resetRepositoryEntryAndParentCourse(repositoryEntry.getKey());

        // Notify possible listeners about DELETED event
        sendCampusCourseEvent(CampusCourseEvent.DELETED);
    }

    @Override
    public void resetCampusGroup(BusinessGroup campusGroup) {
        LOG.debug("resetCampusGroup for group_id =" + campusGroup.getKey());
        daoManager.resetCampusGroup(campusGroup.getKey());
    }

    @Override
    public void deleteCampusGroups(RepositoryEntry repositoryEntry) {
        Set<CampusGroups> setOfCampusGroups = daoManager.getCampusGroupsByRepositoryEntry(repositoryEntry.getKey());

        Set<BusinessGroup> campusGroupsA = setOfCampusGroups.stream().map(CampusGroups::getCampusGroupA).collect(Collectors.toSet());
        Set<BusinessGroup> campusGroupsB = setOfCampusGroups.stream().map(CampusGroups::getCampusGroupB).collect(Collectors.toSet());

        campusGroupsA.forEach(businessGroupService::deleteBusinessGroup);
        campusGroupsB.forEach(businessGroupService::deleteBusinessGroup);
    }

	@Override
    public Set<Course> getNotCreatedCourses(Identity identity, SapUserType userType, String searchString) {
        return daoManager.getNotCreatedCourses(identity, userType, searchString);
    }

    @Override
    public Set<Course> getCreatedCourses(Identity identity, SapUserType userType, String searchString) {
        return daoManager.getCreatedCourses(identity, userType, searchString);
    }

	@Override
    public void createDelegation(Identity delegator, Identity delegatee) {
        daoManager.saveDelegation(delegator, delegatee);
        dbInstance.intermediateCommit();
    }

    @Override
    public boolean existsDelegation(Identity delegator, Identity delegatee) {
        return daoManager.existsDelegation(delegator, delegatee);
    }

    @Override
    public boolean existCoursesForRepositoryEntry(RepositoryEntry repositoryEntry) {
        return daoManager.existCoursesForRepositoryEntry(repositoryEntry.getKey());
    }

    @Override
    public List<Long> getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters() {
        return daoManager.getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters();
    }

    @Override
    public List getDelegatees(Identity delegator) {
        return daoManager.getDelegatees(delegator);
    }

    public void deleteDelegation(Identity delegator, Identity delegatee) {
        daoManager.deleteDelegation(delegator, delegatee);
        dbInstance.intermediateCommit();
    }

    private void sendCampusCourseEvent(int event) {
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(
                new CampusCourseEvent(event), OresHelper.lookupType(RepositoryEntry.class)
        );
    }
}
