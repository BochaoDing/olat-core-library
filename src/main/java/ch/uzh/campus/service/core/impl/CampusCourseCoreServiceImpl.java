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
 */
package ch.uzh.campus.service.core.impl;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.data.SapOlatUser;
import ch.uzh.campus.presentation.CampusCourseEvent;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import ch.uzh.campus.service.core.impl.creator.CampusCourseCreator;
import ch.uzh.campus.service.core.impl.creator.CampusCourseDescriptionBuilder;
import ch.uzh.campus.service.core.impl.creator.CampusCoursePublisher;
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
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
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

    @Autowired
    public CampusCourseCoreServiceImpl(DB dbInstance, DaoManager daoManager, RepositoryService repositoryService, CampusCourseFactory campusCourseFactory, CampusCourseDescriptionBuilder campusCourseDescriptionBuilder, CampusCourseCreator campusCourseCreator, CampusCoursePublisher campusCoursePublisher, CampusCourseConfiguration campusCourseConfiguration, CampusCourseGroupSynchronizer campusCourseGroupSynchronizer, OLATResourceManager olatResourceManager) {
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
    }

    @Override
    public boolean checkDelegation(Long sapCampusCourseId, Identity creator) {
        CampusCourseImportTO campusCourseImportData = daoManager.getSapCampusCourse(sapCampusCourseId);
        for (Identity identity : campusCourseImportData.getLecturers()) {
            if (identity.getName().equalsIgnoreCase(creator.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CampusCourse createCampusCourseFromTemplate(Long courseResourceableId, Long sapCampusCourseId, Identity creator) {
        CampusCourseImportTO campusCourseImportData = daoManager.getSapCampusCourse(sapCampusCourseId);
        if (campusCourseImportData.isOlatResourceableIdUndefined()) {
            // Campus course has not been created yet
            Long templateCourseResourceableId;
            boolean isDefaultTemplateUsed = (courseResourceableId == null);

            if (isDefaultTemplateUsed) {
                templateCourseResourceableId = campusCourseConfiguration.getTemplateCourseResourcableId(campusCourseImportData.getLanguage());

                // The case that no template was found
                if (templateCourseResourceableId == null) {
                    return null;
                }

                // Check if template has unpublished changes. If so return.
                ICourse defaultTemplateCourse = CourseFactory.loadCourse(templateCourseResourceableId);
                PublishTreeModel publishTreeModel = new PublishTreeModel(defaultTemplateCourse.getEditorTreeModel(), defaultTemplateCourse.getRunStructure());
                if (publishTreeModel.hasPublishableChanges()) {
                    LOG.warn("Campuskurs template course " + defaultTemplateCourse.getCourseTitle() + " (" + defaultTemplateCourse.getResourceableId()
                            + ") is not published completely.");
                    return null;
                }
            } else {
                templateCourseResourceableId = courseResourceableId;
            }

            CampusCourse campusCourse = null;
            try {
                // Create the campus course by copying the appropriate template (default or custom)
                campusCourse = campusCourseCreator.createCampusCourseFromTemplate(campusCourseImportData, templateCourseResourceableId, creator, isDefaultTemplateUsed);
                campusCourse.updateCampusCourseCreatedFromTemplate(campusCourseImportData, creator, isDefaultTemplateUsed, campusCourseCreator, campusCoursePublisher, campusCourseGroupSynchronizer, campusCourseConfiguration);
                daoManager.saveCampusCourseResoureableId(sapCampusCourseId, campusCourse.getRepositoryEntry().getOlatResource().getResourceableId());
                dbInstance.intermediateCommit();
                return campusCourse;

            } catch (Exception ex) {
                // CLEAN UP TO ENSURE CONSISTENT STATE
                if (campusCourse != null) {
                    if (campusCourse.getRepositoryEntry() != null) {
                        try {
                            repositoryService.deleteRepositoryEntryAndBaseGroups(campusCourse.getRepositoryEntry());
                        } catch (Exception e) {
                            // we tried best to delete entry - ignore exceptions during deletion
                        }
                    }
                    if (campusCourse.getCourse() != null) {
                        try {
                            olatResourceManager.deleteOLATResourceable(campusCourse.getCourse());
                        } catch (Exception e) {
                            // we tried best to delete entry - ignore exceptions during deletion
                        }
                    }
                }
                dbInstance.rollbackAndCloseSession();
                return null;
            }
        } else {
            // Campus course has already been created
            return loadCampusCourse(sapCampusCourseId);
        }
    }

    @Override
    public CampusCourse continueCampusCourse(Long sapCampusCourseId, Long parentSapCampusCourseId, Identity creator) {
        daoManager.saveParentCourseId(sapCampusCourseId, parentSapCampusCourseId);
        CampusCourse campusCourse = loadCampusCourse(parentSapCampusCourseId);
        campusCourse.continueCampusCourse(daoManager.getSapCampusCourse(sapCampusCourseId), creator, repositoryService, campusCourseDescriptionBuilder, campusCourseCreator);
        dbInstance.intermediateCommit();
        return campusCourse;
    }

    @Override
    public CampusCourse loadCampusCourse(Long sapCampusCourseId) {
        return campusCourseFactory.getCampusCourse(sapCampusCourseId);
    }

    //TODO olatng: not yet used anywhere in the code
    // In OLAT 7.8, it is used in CourseRepositoryHandler.cleanupOnDelete(final OLATResourceable res);
    // and in OpenOLAT this class does not exist
    @Override
    public void resetResourceableIdReference(OLATResourceable res) {
        LOG.info("deleteResourceableIdReference for resourceableId=" + res.getResourceableId());
        daoManager.resetResourceableId(res.getResourceableId());
        dbInstance.intermediateCommit();

        // Notify possible listeners about DELETED event
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(
                new CampusCourseEvent(
                        res.getResourceableId(), CampusCourseEvent.DELETED
                ), OresHelper.lookupType(CampusCourse.class)
        );
    }

    /**
     * @return Mapped RepositoryEntry or null when no could be found
     */
    @Override
    public RepositoryEntry getRepositoryEntryFor(Long sapCampusCourseId) {
        CampusCourse campusCourse = campusCourseFactory.getCampusCourse(sapCampusCourseId);
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
    public List<Long> getAllCreatedSapCourcesResourceableIds() {
        return daoManager.getAllCreatedSapCourcesResourceableIds();
    }

    @Override
    public List getDelegatees(Identity delegator) {
        return daoManager.getDelegatees(delegator);
    }

    public void deleteDelegation(Identity delegator, Identity delegatee) {
        daoManager.deleteDelegation(delegator, delegatee);
        dbInstance.intermediateCommit();
    }
}
