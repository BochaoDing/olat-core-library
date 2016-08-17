package ch.uzh.campus.service.core.impl;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.data.SapOlatUser;
import ch.uzh.campus.presentation.CampusCourseEvent;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.CampusCourseGroups;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import ch.uzh.campus.service.core.impl.creator.CampusCourseCreator;
import ch.uzh.campus.service.core.impl.creator.CampusCourseDescriptionBuilder;
import ch.uzh.campus.service.core.impl.creator.CampusCoursePublisher;
import ch.uzh.campus.service.core.impl.syncer.CampusCourseGroupsFinder;
import ch.uzh.campus.service.core.impl.syncer.CampusCourseGroupSynchronizer;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
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
    private final CampusCourseFactory campusCourseFactory;
    private final CampusCourseDescriptionBuilder campusCourseDescriptionBuilder;
    private final CampusCourseCreator campusCourseCreator;
    private final CampusCoursePublisher campusCoursePublisher;
    private final CampusCourseConfiguration campusCourseConfiguration;
    private final CampusCourseGroupSynchronizer campusCourseGroupSynchronizer;
    private final OLATResourceManager olatResourceManager;
    private final CampusCourseGroupsFinder campusCourseGroupsFinder;
    private final BusinessGroupService businessGroupService;

    @Autowired
    public CampusCourseCoreServiceImpl(DB dbInstance,
                                       DaoManager daoManager,
                                       RepositoryService repositoryService,
                                       CampusCourseFactory campusCourseFactory,
                                       CampusCourseDescriptionBuilder campusCourseDescriptionBuilder,
                                       CampusCourseCreator campusCourseCreator,
                                       CampusCoursePublisher campusCoursePublisher,
                                       CampusCourseConfiguration campusCourseConfiguration,
                                       CampusCourseGroupSynchronizer campusCourseGroupSynchronizer,
                                       OLATResourceManager olatResourceManager,
                                       CampusCourseGroupsFinder campusCourseGroupsFinder,
                                       BusinessGroupService businessGroupService) {
        this.dbInstance = dbInstance;
        this.daoManager = daoManager;
        this.repositoryService = repositoryService;
        this.campusCourseFactory = campusCourseFactory;
        this.campusCourseDescriptionBuilder = campusCourseDescriptionBuilder;
        this.campusCourseCreator = campusCourseCreator;
        this.campusCoursePublisher = campusCoursePublisher;
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.campusCourseGroupSynchronizer = campusCourseGroupSynchronizer;
        this.olatResourceManager = olatResourceManager;
        this.campusCourseGroupsFinder = campusCourseGroupsFinder;
        this.businessGroupService = businessGroupService;
    }

    @Override
    public boolean checkDelegation(Long sapCampusCourseId, Identity creator) {
        CampusCourseImportTO campusCourseImportData = daoManager.getSapCampusCourse(sapCampusCourseId);
        for (Identity identity : campusCourseImportData.getLecturersOfCourseAndParentCourses()) {
            if (identity.getName().equalsIgnoreCase(creator.getName())) {
                return true;
            }
        }
        return false;
    }

	@Override
    public CampusCourse createCampusCourseFromStandardTemplate(Long sapCampusCourseId, Identity creator) throws Exception {
		assert sapCampusCourseId != null;

		CampusCourseImportTO campusCourseImportTO = daoManager.getSapCampusCourse(sapCampusCourseId);
		Long courseResourceableId = campusCourseConfiguration.getTemplateCourseResourcableId(campusCourseImportTO.getLanguage());

		if (courseResourceableId == null) {
			throw new IllegalArgumentException("The template for Campuskurs not be found: " + sapCampusCourseId);
		}

		// Check if template has unpublished changes. If so return.
		ICourse defaultTemplateCourse = CourseFactory.loadCourse(courseResourceableId);
		PublishTreeModel publishTreeModel = new PublishTreeModel(defaultTemplateCourse.getEditorTreeModel(), defaultTemplateCourse.getRunStructure());
		if (publishTreeModel.hasPublishableChanges()) {
			LOG.warn("Campuskurs template course " + defaultTemplateCourse.getCourseTitle() + " (" + defaultTemplateCourse.getResourceableId()
					+ ") is not published completely.");
			return null;
		}

		return createCampusCourseFromTemplate(courseResourceableId, sapCampusCourseId, creator, true);
	}

    @Override
    public CampusCourse createCampusCourseFromTemplate(Long courseResourceableId,
													   Long sapCampusCourseId,
													   Identity creator) throws Exception {
		return createCampusCourseFromTemplate(courseResourceableId, sapCampusCourseId, creator, false);
	}

	private CampusCourse createCampusCourseFromTemplate(Long courseResourceableId,
														Long sapCampusCourseId,
														Identity creator,
														boolean isDefaultTemplateUsed) throws Exception {
		assert courseResourceableId != null;
		assert sapCampusCourseId != null;
		assert creator != null;

		CampusCourseImportTO campusCourseImportTO = daoManager.getSapCampusCourse(sapCampusCourseId);
        if (campusCourseImportTO.isOlatResourceableIdUndefined()) {
            CampusCourse campusCourse = null;
            try {
                // Create the campus course by copying the appropriate template (default or custom)
                campusCourse = campusCourseCreator.createCampusCourseFromTemplate(campusCourseImportTO, courseResourceableId, creator, isDefaultTemplateUsed);
                campusCourse.updateCampusCourseCreatedFromTemplate(campusCourseImportTO, creator, isDefaultTemplateUsed, campusCourseCreator, campusCoursePublisher, campusCourseGroupSynchronizer, campusCourseConfiguration);
                Long resourceableId = campusCourse.getRepositoryEntry().getOlatResource().getResourceableId();
                daoManager.saveCampusCourseResoureableId(sapCampusCourseId, resourceableId);
                dbInstance.intermediateCommit();
                // Notify possible listeners about CREATED event
                sendCampusCourseEvent(resourceableId, CampusCourseEvent.CREATED);
                return campusCourse;
            } catch (Exception e1) {
                // CLEAN UP TO ENSURE CONSISTENT STATE
                if (campusCourse != null) {
                    if (campusCourse.getRepositoryEntry() != null) {
                        try {
                            repositoryService.deleteRepositoryEntryAndBaseGroups(campusCourse.getRepositoryEntry());
                        } catch (Exception e2) {
                            // we tried best to delete entry - ignore exceptions during deletion
                        }
                    }
                    if (campusCourse.getCourse() != null) {
                        try {
                            olatResourceManager.deleteOLATResourceable(campusCourse.getCourse());
                        } catch (Exception e2) {
                            // we tried best to delete entry - ignore exceptions during deletion
                        }
                    }
                }
                dbInstance.rollbackAndCloseSession();

				throw e1;
            }
        } else {
            // Campus course has already been created
            return loadCampusCourse(campusCourseImportTO);
        }
    }

    @Override
    public CampusCourse continueCampusCourse(Long childSapCampusCourseId,
											 Long parentSapCampusCourseId,
											 Identity creator) {
		assert childSapCampusCourseId != null;
		assert parentSapCampusCourseId != null;
		assert creator != null;

		/*
		 * Check first if ids exist.
		 */
		CampusCourseImportTO campusCourseImportTO = daoManager.getSapCampusCourse(childSapCampusCourseId);
		if (campusCourseImportTO == null) {
			throw new IllegalArgumentException("SAP campus course does not exists: " + childSapCampusCourseId);
		}
		CampusCourse parentCampusCourse = loadCampusCourse(daoManager.getSapCampusCourse(parentSapCampusCourseId));
		if (parentCampusCourse == null) {
			throw new IllegalArgumentException("Parent SAP campus course does not exists: " + childSapCampusCourseId);
		}

		daoManager.saveParentCourseId(childSapCampusCourseId, parentSapCampusCourseId);
		parentCampusCourse.continueCampusCourse(campusCourseImportTO, creator, repositoryService, campusCourseDescriptionBuilder, campusCourseCreator, campusCourseGroupSynchronizer);
        dbInstance.intermediateCommit();
        Long resourceableId = parentCampusCourse.getRepositoryEntry().getOlatResource().getResourceableId();
        daoManager.saveCampusCourseResoureableId(childSapCampusCourseId, resourceableId);
        dbInstance.intermediateCommit();
        // Notify possible listeners about CONTINUED event
        sendCampusCourseEvent(resourceableId, CampusCourseEvent.CONTINUED);
        return parentCampusCourse;
    }

    @Override
    public CampusCourse loadCampusCourse(CampusCourseImportTO campusCourseImportTO) {
        return campusCourseFactory.getCampusCourse(campusCourseImportTO);
    }

    @Override
    public CampusCourse loadCampusCourseByResourceable(Long resourceableId) {
        return campusCourseFactory.getCampusCourseByResourceable(resourceableId);
    }

    @Override
    public Course getLatestCourseByResourceable(Long resourceableId) throws Exception {
        return daoManager.getLatestCourseByResourceable(resourceableId);
    }

    @Override
    public void resetResourceableIdAndParentCourseReference(OLATResourceable res) {
        LOG.info("resetResourceableIdAndParentCourseReference for resourceableId=" + res.getResourceableId());
        daoManager.resetResourceableIdAndParentCourseReference(res.getResourceableId());

        // Notify possible listeners about DELETED event
        sendCampusCourseEvent(res.getResourceableId(), CampusCourseEvent.DELETED);
    }

    @Override
    public void deleteCampusCourseGroupsIfExist(RepositoryEntry repositoryEntry) {
        CampusCourseGroups campusCourseGroups = campusCourseGroupsFinder.findCampusCourseGroups(repositoryEntry);
        if (campusCourseGroups == null) {
            return;
        }
        BusinessGroup campusCourseGroupA = campusCourseGroups.getCampusCourseGroupA();
        if (campusCourseGroupA != null) {
            businessGroupService.deleteBusinessGroup(campusCourseGroupA);
        }
        BusinessGroup campusCourseGroupB = campusCourseGroups.getCampusCourseGroupB();
        if (campusCourseGroupB != null) {
            businessGroupService.deleteBusinessGroup(campusCourseGroupB);
        }
    }

    /**
     * @return Mapped RepositoryEntry or null when no could be found
     */
    @Override
    public RepositoryEntry getRepositoryEntryFor(Long sapCampusCourseId) {
        CampusCourse campusCourse = campusCourseFactory.getCampusCourse(daoManager.getSapCampusCourse(sapCampusCourseId));
        return (campusCourse == null ? null : campusCourse.getRepositoryEntry());
    }

	@Override
    public Set<Course> getCampusCoursesWithoutResourceableId(Identity identity, SapOlatUser.SapUserType userType, String searchString) {
        return daoManager.getCampusCoursesWithoutResourceableId(identity, userType, searchString);
    }

	@Override
	public Set<Course> getCampusCoursesWithoutResourceableId(Identity identity, SapOlatUser.SapUserType userType) {
		return getCampusCoursesWithoutResourceableId(identity, userType, null);
	}

    @Override
    public Set<Course> getCampusCoursesWithResourceableId(Identity identity, SapOlatUser.SapUserType userType, String searchString) {
        return daoManager.getCampusCoursesWithResourceableId(identity, userType, searchString);
    }

	@Override
	public Set<Course> getCampusCoursesWithResourceableId(Identity identity, SapOlatUser.SapUserType userType) {
		return getCampusCoursesWithResourceableId(identity, userType, null);
	}

	@Override
    public void createDelegation(Identity delegator, Identity delegatee) {
        daoManager.saveDelegation(delegator, delegatee);
        dbInstance.intermediateCommit();
    }

    @Override
    public boolean existDelegation(Identity delegator, Identity delegatee) {
        return daoManager.existDelegation(delegator, delegatee);
    }

    @Override
    public boolean existResourceableId(Long resourceableId) {
        return daoManager.existResourceableId(resourceableId);
    }

    @Override
    public List<Long> getResourceableIdsOfAllCreatedCoursesOfPreviousSemesters() {
        return daoManager.getResourceableIdsOfAllCreatedCoursesOfPreviousSemesters();
    }

    @Override
    public List getDelegatees(Identity delegator) {
        return daoManager.getDelegatees(delegator);
    }

    public void deleteDelegation(Identity delegator, Identity delegatee) {
        daoManager.deleteDelegation(delegator, delegatee);
        dbInstance.intermediateCommit();
    }

    private void sendCampusCourseEvent(Long resourceableId, int event) {
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(
                new CampusCourseEvent(resourceableId, event), OresHelper.lookupType(CampusCourse.class)
        );
    }
}
