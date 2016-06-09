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

import ch.uzh.campus.CampusConfiguration;
import ch.uzh.campus.CampusCourseImportTO;

import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.CampusCourseFactory;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedGroupStatistic;

import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Synchronize the entire course with groups, title, description.
 * 
 * @author cg
 */
@Component
public class CourseSynchronizer {

    private static final OLog LOG = Tracing.createLoggerFor(CourseSynchronizer.class);

    @Autowired
    private DB dbInstance;

    @Autowired
    CampusCourseGroupSynchronizer courseGroupSynchronizer;
   
    @Autowired
    CourseAttributeSynchronizer courseAttributeSynchronizer;
    
    @Autowired
    CampusConfiguration campusConfiguration;
    
    @Autowired
    CampusCourseFactory campusCourseFactory;

    
    public SynchronizedGroupStatistic synchronizeCourse(CampusCourseImportTO sapCourse) {
        if (sapCourse != null) {
            long resourceableId = sapCourse.getOlatResourceableId();
            LOG.debug("synchronizeCourse sapCourseId=" + sapCourse.getSapCourseId() + "  resourceableId=" + resourceableId);
            ICourse course = CourseFactory.loadCourse(resourceableId);
            commitDBImplTransaction();
            LOG.debug("synchronizeCourse start for course=" + course.getCourseTitle());
            LOG.debug("synchronizeCourse Lecturer size=" + sapCourse.getLecturers().size());
            LOG.debug("synchronizeCourse Participants size=" + sapCourse.getParticipants().size());
            
            CampusCourse campusCourse = campusCourseFactory.getCampusCourse(sapCourse.getSapCourseId(), resourceableId);

            courseGroupSynchronizer.addAllLecturesAsOwner(campusCourse, sapCourse.getLecturers());
            // SynchronizedGroupStatistic groupStatistic = courseGroupSynchronizer.synchronizeCourseGroupsForStudentsOnly(course, sapCourse);
            SynchronizedGroupStatistic groupStatistic = courseGroupSynchronizer.synchronizeCourseGroups(campusCourse, sapCourse);
            commitDBImplTransaction();
            LOG.debug("synchronizeCourse statistic=" + groupStatistic);
            if (campusConfiguration.isSynchronizeTitleAndDescriptionEnabled()) {
                LOG.debug("SynchronizeTitleAndDescription is enabled");
                courseAttributeSynchronizer.synchronizeTitleAndDescription(sapCourse.getSapCourseId(), sapCourse);
                commitDBImplTransaction();
            }
            return groupStatistic;
        } else {
            return SynchronizedGroupStatistic.createEmptyStatistic();
        }
    }

    private void commitDBImplTransaction() {
        dbInstance.intermediateCommit();
    }
}
