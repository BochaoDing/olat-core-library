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

import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.data.SapOlatUser;
import ch.uzh.campus.presentation.CampusCourseEvent;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import ch.uzh.campus.service.core.impl.creator.CampusCourseCreateCoordinator;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
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
	
	private static final OLog log = Tracing.createLoggerFor(CampusCourseCoreServiceImpl.class);

    @Autowired
    DB dbInstance;

    @Autowired
    DaoManager daoManager;
    
    @Autowired
    CampusCourseCreateCoordinator campusCourseCreateCoordinator;
    
    @Autowired
    RepositoryService repositoryService;
    
    @Autowired
    CampusCourseFactory campusCourseFactory;

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
            CampusCourse campusCourse = campusCourseCreateCoordinator.createCampusCourse(courseResourceableId, campusCourseImportData, creator);
            if (campusCourse != null) {
                daoManager.saveCampusCourseResoureableId(sapCampusCourseId, campusCourse.getRepositoryEntry().getOlatResource().getResourceableId());
            }
            dbInstance.intermediateCommit();
            return campusCourse;
        } else {
            // Campus course has already been created
            return loadCampusCourse(sapCampusCourseId, campusCourseImportData.getOlatResourceableId());
        }
    }

    @Override
    public CampusCourse continueCampusCourse(Long courseResourceableId, Long sapCampusCourseId, String courseTitle, Identity creator) {
        daoManager.saveCampusCourseResoureableIdAndDisableSynchronization(sapCampusCourseId, courseResourceableId);
        dbInstance.intermediateCommit();
        CampusCourseImportTO campusCourseImportData = daoManager.getSapCampusCourse(sapCampusCourseId);
        CampusCourse campusCourse = loadCampusCourse(sapCampusCourseId, courseResourceableId);
        campusCourse = campusCourseCreateCoordinator.continueCampusCourse(campusCourse, campusCourseImportData, creator);
        dbInstance.intermediateCommit();
        return campusCourse;
    }

    private CampusCourse loadCampusCourse(Long sapCampusCourseId, Long resourceableId) {
        return campusCourseFactory.getCampusCourse(sapCampusCourseId, resourceableId);
    }

    //TODO olatng: not yet used anywhere in the code
    // In OLAT 7.8, it is used in CourseRepositoryHandler.cleanupOnDelete(final OLATResourceable res);
    // and in OpenOLAT this class does not exist
    @Override
    public void resetResourceableIdReference(OLATResourceable res) {
        log.info("deleteResourceableIdReference for resourceableId=" + res.getResourceableId());
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
    public RepositoryEntry getRepositoryEntryFor(Long sapCourseId) {
        return campusCourseFactory.getRepositoryEntryFor(sapCourseId);
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
