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

    @Autowired
    CampusCourseFactory campusCourseFactory;
    @Autowired
    CourseDescriptionBuilder courseDescriptionBuilder;
    @Autowired
    CampusConfiguration campusConfiguration;

    public TitleAndDescriptionStatistik synchronizeTitleAndDescription(long sapCourseId, CampusCourseImportTO campusCourseTO) {
        boolean titleUpdated = synchronizeTitle(getCampusCourse(sapCourseId, campusCourseTO.getOlatResourceableId()), campusCourseTO.getTitle());
        boolean descriptionUpdated = synchronizeDescription(getCampusCourse(sapCourseId, campusCourseTO.getOlatResourceableId()),
                courseDescriptionBuilder.buildDescriptionFrom(campusCourseTO, campusConfiguration.getTemplateLanguage(campusCourseTO.getLanguage())));

        return new TitleAndDescriptionStatistik(titleUpdated, descriptionUpdated);
    }

    private CampusCourse getCampusCourse(long sapCampusCourseId, Long resourceableId) {
        return campusCourseFactory.getCampusCourse(sapCampusCourseId, resourceableId);
    }

    /**
     * @return 'true' when description is updated; 'false' when description is NOT updated.
     */
    boolean synchronizeDescription(CampusCourse campusCourse, String newDescription) {
        if (campusCourse.descriptionChanged(newDescription)) {
            campusCourse.setDescription(newDescription);
            return true;
        }
        return false;
    }

    /**
     * @return 'true' when title is updated; 'false' when title is NOT updated.
     */
    boolean synchronizeTitle(CampusCourse campusCourse, String newTitle) {
        if (campusCourse.titleChanged(newTitle)) {
            campusCourse.setTitle(newTitle);
            return true;
        }
        return false;
    }
}
