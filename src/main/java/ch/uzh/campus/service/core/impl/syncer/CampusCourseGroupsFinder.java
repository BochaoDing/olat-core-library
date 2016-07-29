package ch.uzh.campus.service.core.impl.syncer;


import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.service.CampusCourseGroups;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
 * Initial Date: 25.06.2012 <br>
 * 
 * @author cg
 */
@Service
public class CampusCourseGroupsFinder {

    private final BGAreaManager bgAreaManager;
    private final CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    public CampusCourseGroupsFinder(BGAreaManager bgAreaManager, CampusCourseConfiguration campusCourseConfiguration) {
        this.bgAreaManager = bgAreaManager;
        this.campusCourseConfiguration = campusCourseConfiguration;
    }

    /**
     * @return campus course groups found or null, if no campus groups have been found
     */
    public CampusCourseGroups findCampusCourseGroups(RepositoryEntry repositoryEntry) {

        // Find learning learning area
        String campusLearningAreaName = campusCourseConfiguration.getCampusCourseLearningAreaName();
        BGArea campusLearningArea = bgAreaManager.findBGArea(campusLearningAreaName, repositoryEntry.getOlatResource());
        if (campusLearningArea == null) {
            return null;
        }

        // Find campus course groups in learning area
        List<BusinessGroup> groupsOfLearningArea = bgAreaManager.findBusinessGroupsOfArea(campusLearningArea);
        BusinessGroup campusCourseGroupA = findCampusGroup(campusCourseConfiguration.getCourseGroupAName(), groupsOfLearningArea);
        BusinessGroup campusCourseGroupB = findCampusGroup(campusCourseConfiguration.getCourseGroupBName(), groupsOfLearningArea);

        // No campus course groups found
        if (campusCourseGroupA == null && campusCourseGroupB == null) {
            return null;
        }

        return new CampusCourseGroups(campusCourseGroupA, campusCourseGroupB);
    }

    private BusinessGroup findCampusGroup(String campusGroupName, List<BusinessGroup> groupsOfLearningArea) {
        for (BusinessGroup businessGroup : groupsOfLearningArea) {
            if (businessGroup.getName().equals(campusGroupName)) {
                return businessGroup;
            }
        }
        return null;
    }

}
