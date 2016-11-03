package ch.uzh.campus.service.core.impl;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseException;
import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.data.SapUserType;
import ch.uzh.campus.presentation.CampusCourseEvent;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import ch.uzh.campus.service.core.impl.creator.CampusGroupsCreator;
import ch.uzh.campus.service.core.impl.creator.OlatCampusCourseCreator;
import ch.uzh.campus.service.core.impl.creator.CampusCoursePublisher;
import ch.uzh.campus.service.core.impl.syncer.CampusCourseDefaultCoOwners;
import ch.uzh.campus.service.core.impl.syncer.CampusGroupsSynchronizer;
import ch.uzh.campus.service.core.impl.syncer.CampusCourseRepositoryEntrySynchronizer;
import ch.uzh.campus.service.data.CampusGroups;
import ch.uzh.campus.service.data.OlatCampusCourse;
import ch.uzh.campus.service.data.SapCampusCourseTO;
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
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
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
    private final OlatCampusCourseProvider olatCampusCourseProvider;
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
                                       OlatCampusCourseProvider olatCampusCourseProvider,
                                       OlatCampusCourseCreator olatCampusCourseCreator,
                                       CampusCoursePublisher campusCoursePublisher,
                                       CampusGroupsCreator campusGroupsCreator,
                                       CampusCourseConfiguration campusCourseConfiguration,
                                       CampusGroupsSynchronizer campusGroupsSynchronizer,
                                       CampusCourseRepositoryEntrySynchronizer campusCourseRepositoryEntrySynchronizer, OLATResourceManager olatResourceManager,
                                       BusinessGroupService businessGroupService,
                                       CampusCourseDefaultCoOwners campusCourseDefaultCoOwners) {
        this.dbInstance = dbInstance;
        this.daoManager = daoManager;
        this.repositoryService = repositoryService;
        this.olatCampusCourseProvider = olatCampusCourseProvider;
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
        SapCampusCourseTO sapCampusCourseTO = daoManager.loadSapCampusCourseTO(sapCampusCourseId);
        for (Identity lecturerOfCourse : sapCampusCourseTO.getLecturersOfCourse()) {
            if (lecturerOfCourse.getName().equalsIgnoreCase(identity.getName())) {
                return true;
            }
        }
        for (Identity delegateeOfCourse : sapCampusCourseTO.getDelegateesOfCourse()) {
            if (delegateeOfCourse.getName().equalsIgnoreCase(identity.getName())) {
                return true;
            }
        }
        return false;
    }

	@Override
    public OlatCampusCourse createOlatCampusCourseFromStandardTemplate(Long sapCampusCourseId,
                                                                       Identity creator) throws Exception {
		assert sapCampusCourseId != null;
        assert creator != null;

        // Load sap campus course TO
		SapCampusCourseTO sapCampusCourseTO = daoManager.loadSapCampusCourseTO(sapCampusCourseId);

		Long standardTemplateRepositoryEntryId = campusCourseConfiguration.getTemplateRepositoryEntryId(sapCampusCourseTO.getLanguage());
        if (standardTemplateRepositoryEntryId == null) {
            throw new IllegalArgumentException("No standard template found for language " + sapCampusCourseTO.getLanguage());
        }

        OLATResource standardTemplateOlatResource = repositoryService.loadRepositoryEntryResource(standardTemplateRepositoryEntryId);
		if (standardTemplateOlatResource == null) {
            throw new IllegalArgumentException("No standard template found with repository entry id " + standardTemplateRepositoryEntryId);
        }

		// Check if template has unpublished changes. If so return.
		ICourse defaultTemplateCourse = CourseFactory.loadCourse(standardTemplateOlatResource.getResourceableId());
		PublishTreeModel publishTreeModel = new PublishTreeModel(defaultTemplateCourse.getEditorTreeModel(), defaultTemplateCourse.getRunStructure());
		if (publishTreeModel.hasPublishableChanges()) {
			throw new CampusCourseException("Campus course template " +
					defaultTemplateCourse.getCourseTitle() + " (" +
					defaultTemplateCourse.getResourceableId() +
					") is not published completely.");
		}

		return createOlatCampusCourseFromTemplate(standardTemplateOlatResource, sapCampusCourseTO, creator, true);
	}

    @Override
    public OlatCampusCourse createOlatCampusCourseFromTemplate(OLATResource templateOlatResource,
                                                               Long sapCampusCourseId,
                                                               Identity creator) throws Exception {
        assert templateOlatResource != null;
        assert sapCampusCourseId != null;
        assert creator != null;

        // Load sap campus course TO
        SapCampusCourseTO sapCampusCourseTO = daoManager.loadSapCampusCourseTO(sapCampusCourseId);

		return createOlatCampusCourseFromTemplate(templateOlatResource, sapCampusCourseTO, creator, false);
	}

	private OlatCampusCourse createOlatCampusCourseFromTemplate(OLATResource templateOlatResource,
                                                                SapCampusCourseTO sapCampusCourseTO,
                                                                Identity creator,
                                                                boolean isStandardTemplateUsed) throws Exception {

        if (sapCampusCourseTO.getOlatResource() == null) {

            OlatCampusCourse createdOlatCampusCourse = null;

            try {
                // Create the campus course by copying the appropriate template (default or custom)
                createdOlatCampusCourse = olatCampusCourseCreator.createOlatCampusCourseFromTemplate(sapCampusCourseTO, templateOlatResource, creator, isStandardTemplateUsed);

                // Update the copied course run and editor models
                olatCampusCourseCreator.updateCourseRunAndEditorModels(
                        createdOlatCampusCourse.getCourse(),
                        createdOlatCampusCourse.getRepositoryEntry().getKey(),
                        sapCampusCourseTO.getTitleToBeDisplayed(),
                        sapCampusCourseTO.getVvzLink(),
                        sapCampusCourseTO.getLanguage(),
                        isStandardTemplateUsed);

                // Publish run and editor models
                if (isStandardTemplateUsed) {
                    campusCoursePublisher.publish(createdOlatCampusCourse.getCourse(), creator);
                }

                // Create campus learning area and campus groups A and B if necessary
                CampusGroups campusGroups = campusGroupsCreator.createCampusLearningAreaAndCampusGroupsIfNecessary(createdOlatCampusCourse, creator, sapCampusCourseTO.getLanguage());
                dbInstance.intermediateCommit();

                // Add course owner role to lecturers and default co-owners
                campusGroupsSynchronizer.addCourseOwnerRole(createdOlatCampusCourse.getRepositoryEntry(), sapCampusCourseTO.getLecturersOfCourse());
                campusGroupsSynchronizer.addCourseOwnerRole(createdOlatCampusCourse.getRepositoryEntry(), campusCourseDefaultCoOwners.getDefaultCoOwners());

                // Execute the first synchronization
                try {
                    campusGroupsSynchronizer.synchronizeCampusGroups(campusGroups, sapCampusCourseTO, creator);
                } catch (CampusCourseException e) {
                    LOG.error(e.getMessage());
                }

                // Add olat resource and campus groups to sap campus course
                daoManager.saveCampusCourseOlatResource(sapCampusCourseTO.getSapCourseId(), createdOlatCampusCourse.getRepositoryEntry().getOlatResource().getKey());
                daoManager.saveCampusGroupA(sapCampusCourseTO.getSapCourseId(), campusGroups.getCampusGroupA().getKey());
                daoManager.saveCampusGroupB(sapCampusCourseTO.getSapCourseId(), campusGroups.getCampusGroupB().getKey());
                dbInstance.intermediateCommit();

                // Notify possible listeners about CREATED event
                sendCampusCourseEvent(CampusCourseEvent.CREATED);

                return createdOlatCampusCourse;

            } catch (Exception e1) {
                // CLEAN UP TO ENSURE CONSISTENT STATE
                if (createdOlatCampusCourse != null) {
                    if (createdOlatCampusCourse.getRepositoryEntry() != null) {
                        try {
                            repositoryService.deleteRepositoryEntryAndBaseGroups(createdOlatCampusCourse.getRepositoryEntry());
                        } catch (Exception e2) {
                            // we tried best to delete entry - ignore exceptions during deletion
                        }
                    }
                    if (createdOlatCampusCourse.getCourse() != null) {
                        try {
                            olatResourceManager.deleteOLATResourceable(createdOlatCampusCourse.getCourse());
                        } catch (Exception e2) {
                            // we tried best to delete entry - ignore exceptions during deletion
                        }
                    }
                }
                dbInstance.rollbackAndCloseSession();

				throw e1;
            }
        } else {

            // Olat campus course has already been created
            return loadOlatCampusCourse(sapCampusCourseTO.getOlatResource());
        }
    }

    @Override
    public OlatCampusCourse continueOlatCampusCourse(Long childSapCampusCourseId, Long parentSapCampusCourseId, Identity creator) {
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

		// Add parent course, olat resource and campus groups of parent course to child course
		daoManager.saveParentCourseId(childSapCampusCourseId, parentSapCampusCourseId);
        daoManager.saveCampusCourseOlatResource(childSapCampusCourseId, parentCourse.getOlatResource().getKey());
        daoManager.saveCampusGroupA(childSapCampusCourseId, parentCourse.getCampusGroupA().getKey());
        daoManager.saveCampusGroupB(childSapCampusCourseId, parentCourse.getCampusGroupB().getKey());
        dbInstance.intermediateCommit();

        // childSapCampusCourseTO must be loaded AFTER setting the parent course id and the campus groups such that
        // childSapCampusCourseTO also contains the lecturers and students of the parent course and the campus groups
        SapCampusCourseTO childSapCampusCourseTO = daoManager.loadSapCampusCourseTO(childSapCampusCourseId);

        // Load (existing) olat campus course
        OlatCampusCourse olatCampusCourse = loadOlatCampusCourse(parentCourse.getOlatResource());

        // Update course run and editor models
        olatCampusCourseCreator.updateCourseRunAndEditorModels(
                olatCampusCourse.getCourse(),
                olatCampusCourse.getRepositoryEntry().getKey(),
                childSapCampusCourseTO.getTitleToBeDisplayed(),
                childSapCampusCourseTO.getVvzLink(),
                childSapCampusCourseTO.getLanguage(),
                false);

        // Synchronize olat campus course repository entry
        campusCourseRepositoryEntrySynchronizer.synchronizeDisplaynameAndDescriptionAndInitialAuthor(olatCampusCourse.getRepositoryEntry(), childSapCampusCourseTO, creator);

        // Add owner role to lecturers, delegatees and default co-owners
        campusGroupsSynchronizer.addCourseOwnerRole(olatCampusCourse.getRepositoryEntry(), childSapCampusCourseTO.getLecturersOfCourse());
        campusGroupsSynchronizer.addCourseOwnerRole(olatCampusCourse.getRepositoryEntry(), childSapCampusCourseTO.getDelegateesOfCourse());
        campusGroupsSynchronizer.addCourseOwnerRole(olatCampusCourse.getRepositoryEntry(), campusCourseDefaultCoOwners.getDefaultCoOwners());

        // Synchronize campus groups
        try {
            campusGroupsSynchronizer.synchronizeCampusGroups(childSapCampusCourseTO.getCampusGroups(), childSapCampusCourseTO, creator);
        } catch (CampusCourseException e) {
            LOG.error(e.getMessage());
        }

        dbInstance.intermediateCommit();

        // Notify possible listeners about CONTINUED event
        sendCampusCourseEvent(CampusCourseEvent.CONTINUED);

        return olatCampusCourse;
    }

    @Override
    public OlatCampusCourse loadOlatCampusCourse(OLATResource olatResource) {
        return olatCampusCourseProvider.loadOlatCampusCourse(olatResource);
    }

    @Override
    public Course getLatestCourseByOlatResource(OLATResource olatResource) throws Exception {
        return daoManager.getLatestCourseByOlatResource(olatResource.getKey());
    }

    @Override
    public void resetOlatResourceAndParentCourse(OLATResource olatResource) {
        LOG.debug("resetOlatResourceAndParentCourse for resource_id =" + olatResource.getKey());
        daoManager.resetOlatResourceAndParentCourse(olatResource.getKey());

        // Notify possible listeners about DELETED event
        sendCampusCourseEvent(CampusCourseEvent.DELETED);
    }

    @Override
    public void resetCampusGroup(BusinessGroup campusGroup) {
        LOG.debug("resetCampusGroup for group_id =" + campusGroup.getKey());
        daoManager.resetCampusGroup(campusGroup.getKey());
    }

    @Override
    public void deleteCampusGroups(OLATResource olatResource) {
        Set<CampusGroups> setOfCampusGroups = daoManager.getCampusGroupsByOlatResource(olatResource.getKey());

        Set<BusinessGroup> campusGroupsA = setOfCampusGroups.stream().map(CampusGroups::getCampusGroupA).collect(Collectors.toSet());
        Set<BusinessGroup> campusGroupsB = setOfCampusGroups.stream().map(CampusGroups::getCampusGroupB).collect(Collectors.toSet());

        campusGroupsA.forEach(businessGroupService::deleteBusinessGroup);
        campusGroupsB.forEach(businessGroupService::deleteBusinessGroup);
    }

	@Override
    public Set<Course> getCoursesWithoutResourceableId(Identity identity, SapUserType userType, String searchString) {
        return daoManager.getCampusCoursesWithoutResourceableId(identity, userType, searchString);
    }

    @Override
    public Set<Course> getCoursesWithResourceableId(Identity identity, SapUserType userType, String searchString) {
        return daoManager.getCampusCoursesWithResourceableId(identity, userType, searchString);
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
    public boolean existCoursesForOlatResource(OLATResource olatResource) {
        return daoManager.existCoursesForOlatResource(olatResource.getKey());
    }

    @Override
    public List<Long> getOlatResourceKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters() {
        return daoManager.getOlatResourceKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters();
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
                new CampusCourseEvent(event), OresHelper.lookupType(OlatCampusCourse.class)
        );
    }
}
