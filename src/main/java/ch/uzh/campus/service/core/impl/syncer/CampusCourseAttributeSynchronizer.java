package ch.uzh.campus.service.core.impl.syncer;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.CampusCourseFactory;
import ch.uzh.campus.service.core.impl.CampusCourseTool;
import ch.uzh.campus.service.core.impl.creator.CampusCourseDescriptionBuilder;
import ch.uzh.campus.service.core.impl.syncer.statistic.TitleAndDescriptionStatistik;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
 *
 * Initial Date: 20.08.2012 <br>
 * 
 * @author cg
 */
@Component
public class CampusCourseAttributeSynchronizer {

    private final CampusCourseFactory campusCourseFactory;
    private final CampusCourseDescriptionBuilder campusCourseDescriptionBuilder;
    private final CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    public CampusCourseAttributeSynchronizer(CampusCourseFactory campusCourseFactory, CampusCourseDescriptionBuilder campusCourseDescriptionBuilder, CampusCourseConfiguration campusCourseConfiguration) {
        this.campusCourseFactory = campusCourseFactory;
        this.campusCourseDescriptionBuilder = campusCourseDescriptionBuilder;
        this.campusCourseConfiguration = campusCourseConfiguration;
    }

    TitleAndDescriptionStatistik synchronizeTitleAndDescription(CampusCourseImportTO campusCourseTO) {
        boolean titleUpdated = synchronizeTitle(getCampusCourse(campusCourseTO), campusCourseTO.getTitle());
        boolean descriptionUpdated = synchronizeDescription(getCampusCourse(campusCourseTO),
                campusCourseDescriptionBuilder.buildDescriptionFrom(campusCourseTO, campusCourseConfiguration.getTemplateLanguage(campusCourseTO.getLanguage())));

        return new TitleAndDescriptionStatistik(titleUpdated, descriptionUpdated);
    }

    private CampusCourse getCampusCourse(CampusCourseImportTO campusCourseImportTO) {
        return campusCourseFactory.getCampusCourse(campusCourseImportTO);
    }

    /**
     * @return 'true' when description is updated; 'false' when description is NOT updated.
     */
    private boolean synchronizeDescription(CampusCourse campusCourse, String newDescription) {
        if (isDescriptionChanged(campusCourse.getRepositoryEntry(), newDescription)) {
            campusCourse.getRepositoryEntry().setDescription(newDescription);
            return true;
        }
        return false;
    }

    /**
     * @return 'true' when title is updated; 'false' when title is NOT updated.
     */
    private boolean synchronizeTitle(CampusCourse campusCourse, String newTitle) {
        if (isTitleChanged(campusCourse.getRepositoryEntry(), newTitle)) {
            // Do not update short semester(s) (causes problems for continued campus courses)
            String newDisplayname = CampusCourseTool.getShortSemestersOfDisplayname(campusCourse.getRepositoryEntry().getDisplayname()) + " " + CampusCourseTool.getTruncatedDisplaynameWithoutShortSemesters(newTitle);
            campusCourse.getRepositoryEntry().setDisplayname(newDisplayname);
            return true;
        }
        return false;
    }

    private boolean isDescriptionChanged(RepositoryEntry repositoryEntry, String newDescription) {
        return (repositoryEntry.getDescription() == null && newDescription != null) || !repositoryEntry.getDescription().equals(newDescription);
    }

    private boolean isTitleChanged(RepositoryEntry repositoryEntry, String newTitle) {
        return (repositoryEntry.getDisplayname() == null && newTitle != null)
                || !CampusCourseTool.getTruncatedDisplaynameWithoutShortSemesters(repositoryEntry.getDisplayname()).equals(CampusCourseTool.getTruncatedDisplaynameWithoutShortSemesters(newTitle));
    }
}
