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
package ch.uzh.campus.service.core.impl.syncer;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.CampusCourseFactory;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedGroupStatistic;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Synchronize the entire course with groups, title, description.
 * 
 * @author cg
 */
@Component
public class CampusCourseSynchronizer {

    private static final OLog LOG = Tracing.createLoggerFor(CampusCourseSynchronizer.class);

    private final CampusCourseGroupSynchronizer courseGroupSynchronizer;
    private final CampusCourseAttributeSynchronizer campusCourseAttributeSynchronizer;
    private final CampusCourseConfiguration campusCourseConfiguration;
    private final CampusCourseFactory campusCourseFactory;

    @Autowired
    public CampusCourseSynchronizer(
            CampusCourseGroupSynchronizer courseGroupSynchronizer,
            CampusCourseAttributeSynchronizer campusCourseAttributeSynchronizer,
            CampusCourseConfiguration campusCourseConfiguration,
            CampusCourseFactory campusCourseFactory
    ) {
        this.courseGroupSynchronizer = courseGroupSynchronizer;
        this.campusCourseAttributeSynchronizer = campusCourseAttributeSynchronizer;
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.campusCourseFactory = campusCourseFactory;
    }

    SynchronizedGroupStatistic synchronizeCourse(CampusCourseImportTO sapCourse) {
        if (sapCourse != null) {
            LOG.debug("synchronizeCourse sapCourseId=" + sapCourse.getSapCourseId() + "  resourceableId=" + sapCourse.getOlatResourceableId());
            LOG.debug("synchronizeCourse Lecturer size=" + sapCourse.getLecturers().size());
            LOG.debug("synchronizeCourse Participants size=" + sapCourse.getParticipants().size());

            CampusCourse campusCourse = campusCourseFactory.getCampusCourse(sapCourse.getSapCourseId());

            courseGroupSynchronizer.addAllLecturesAsOwner(campusCourse, sapCourse.getLecturers());
            SynchronizedGroupStatistic groupStatistic = courseGroupSynchronizer.synchronizeCourseGroups(campusCourse, sapCourse);

            LOG.debug("synchronizeCourse statistic=" + groupStatistic);
            if (campusCourseConfiguration.isSynchronizeTitleAndDescriptionEnabled()) {
                LOG.debug("SynchronizeTitleAndDescription is enabled");
                campusCourseAttributeSynchronizer.synchronizeTitleAndDescription(sapCourse);
            }
            return groupStatistic;
        } else {
            return SynchronizedGroupStatistic.createEmptyStatistic();
        }
    }
}
