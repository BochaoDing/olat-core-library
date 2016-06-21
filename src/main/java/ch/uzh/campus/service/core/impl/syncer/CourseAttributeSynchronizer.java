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
import ch.uzh.campus.service.CourseCreator;
import ch.uzh.campus.service.core.impl.CampusCourseFactory;
import ch.uzh.campus.service.core.impl.creator.CourseDescriptionBuilder;
import ch.uzh.campus.service.core.impl.syncer.statistic.TitleAndDescriptionStatistik;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 20.08.2012 <br>
 * 
 * @author cg
 */
@Component
public class CourseAttributeSynchronizer {

    private final CampusCourseFactory campusCourseFactory;
    private final CourseDescriptionBuilder courseDescriptionBuilder;
    private final CampusConfiguration campusConfiguration;
    private final CourseCreator courseCreator;

    @Autowired
    public CourseAttributeSynchronizer(CampusCourseFactory campusCourseFactory, CourseDescriptionBuilder courseDescriptionBuilder, CampusConfiguration campusConfiguration, CourseCreator courseCreator) {
        this.campusCourseFactory = campusCourseFactory;
        this.courseDescriptionBuilder = courseDescriptionBuilder;
        this.campusConfiguration = campusConfiguration;
        this.courseCreator = courseCreator;
    }

    TitleAndDescriptionStatistik synchronizeTitleAndDescription(CampusCourseImportTO campusCourseTO) {
        boolean titleUpdated = synchronizeTitle(getCampusCourse(campusCourseTO.getSapCourseId(), campusCourseTO.getOlatResourceableId()), campusCourseTO.getTitle());
        boolean descriptionUpdated = synchronizeDescription(getCampusCourse(campusCourseTO.getSapCourseId(), campusCourseTO.getOlatResourceableId()),
                courseDescriptionBuilder.buildDescriptionFrom(campusCourseTO, campusConfiguration.getTemplateLanguage(campusCourseTO.getLanguage())));

        return new TitleAndDescriptionStatistik(titleUpdated, descriptionUpdated);
    }

    private CampusCourse getCampusCourse(long sapCampusCourseId, Long resourceableId) {
        return campusCourseFactory.getCampusCourse(sapCampusCourseId, resourceableId);
    }

    /**
     * @return 'true' when description is updated; 'false' when description is NOT updated.
     */
    private boolean synchronizeDescription(CampusCourse campusCourse, String newDescription) {
        if (courseCreator.descriptionChanged(campusCourse, newDescription)) {
            campusCourse.getRepositoryEntry().setDescription(newDescription);
            return true;
        }
        return false;
    }

    /**
     * @return 'true' when title is updated; 'false' when title is NOT updated.
     */
    private boolean synchronizeTitle(CampusCourse campusCourse, String newTitle) {
        if (courseCreator.titleChanged(campusCourse, newTitle)) {
            String truncatedTitle = courseCreator.getTruncatedTitle(newTitle);
            campusCourse.getRepositoryEntry().setDisplayname(truncatedTitle);
            return true;
        }
        return false;
    }
}
