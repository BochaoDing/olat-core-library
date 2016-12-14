package ch.uzh.campus.service.core.impl.syncer;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseException;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedGroupStatistic;
import ch.uzh.campus.service.data.OlatCampusCourse;
import ch.uzh.campus.service.data.SapCampusCourseTO;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
 * Synchronize the entire course with groups, title, description.
 * 
 * @author cg
 */
@Component
public class CampusCourseSynchronizer {

    private static final OLog LOG = Tracing.createLoggerFor(CampusCourseSynchronizer.class);

    private final CampusGroupsSynchronizer campusGroupsSynchronizer;
    private final CampusCourseRepositoryEntrySynchronizer campusCourseRepositoryEntrySynchronizer;
    private final CampusCourseConfiguration campusCourseConfiguration;
    private final CampusCourseCoreService campusCourseCoreService;
    private final RepositoryService repositoryService;

    @Autowired
    public CampusCourseSynchronizer(
            CampusGroupsSynchronizer campusGroupsSynchronizer,
            CampusCourseRepositoryEntrySynchronizer campusCourseRepositoryEntrySynchronizer,
            CampusCourseConfiguration campusCourseConfiguration,
            CampusCourseCoreService campusCourseCoreService,
            RepositoryService repositoryService) {
        this.campusGroupsSynchronizer = campusGroupsSynchronizer;
        this.campusCourseRepositoryEntrySynchronizer = campusCourseRepositoryEntrySynchronizer;
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.campusCourseCoreService = campusCourseCoreService;
        this.repositoryService = repositoryService;
    }

    SynchronizedGroupStatistic synchronizeOlatCampusCourse(SapCampusCourseTO sapCampusCourseTO) throws CampusCourseException {

        // Load olat campus course
        OlatCampusCourse olatCampusCourse = campusCourseCoreService.loadOlatCampusCourse(sapCampusCourseTO.getOlatResource());

        // Synchronize olat campus course repository entry
        if (campusCourseConfiguration.isSynchronizeDisplaynameAndDescriptionEnabled()) {
            campusCourseRepositoryEntrySynchronizer.synchronizeDisplaynameAndDescription(olatCampusCourse.getRepositoryEntry(), sapCampusCourseTO);
        }

        // Add course owner role to lectures and delegatees
        campusGroupsSynchronizer.addCourseOwnerRole(olatCampusCourse.getRepositoryEntry(), sapCampusCourseTO.getLecturersOfCourse());
        campusGroupsSynchronizer.addCourseOwnerRole(olatCampusCourse.getRepositoryEntry(), sapCampusCourseTO.getDelegateesOfCourse());

        // Synchronize campus groups
        List<Identity> courseOwners = repositoryService.getMembers(olatCampusCourse.getRepositoryEntry(), GroupRoles.owner.name());
        SynchronizedGroupStatistic groupStatistic = campusGroupsSynchronizer.synchronizeCampusGroups(
                sapCampusCourseTO.getCampusGroups(), sapCampusCourseTO, courseOwners.get(0));
        LOG.debug("synchronizeOlatCampusCourse statistic=" + groupStatistic);

        return groupStatistic;
    }
}
