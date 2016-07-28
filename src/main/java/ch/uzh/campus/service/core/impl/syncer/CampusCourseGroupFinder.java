package ch.uzh.campus.service.core.impl.syncer;


import ch.uzh.campus.CampusCourseConfiguration;
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
public class CampusCourseGroupFinder {

    private final BGAreaManager bgAreaManager;
    private final CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    public CampusCourseGroupFinder(BGAreaManager bgAreaManager, CampusCourseConfiguration campusCourseConfiguration) {
        this.bgAreaManager = bgAreaManager;
        this.campusCourseConfiguration = campusCourseConfiguration;
    }

    /**
     * @return campus group found or null, if no campus group has been found
     */
    public BusinessGroup lookupCampusGroup(RepositoryEntry repositoryEntry, String campusGroupName) {

        // Find learning learning area
        String campusLearningAreaName = campusCourseConfiguration.getCampusCourseLearningAreaName();
        BGArea campusLearningArea = bgAreaManager.findBGArea(campusLearningAreaName, repositoryEntry.getOlatResource());
        if (campusLearningArea == null) {
            return null;
        }

        // Find group in learning area
        List<BusinessGroup> groupsOfArea = bgAreaManager.findBusinessGroupsOfArea(campusLearningArea);
        for (BusinessGroup businessGroup : groupsOfArea ) {
        	if (businessGroup.getName().equals(campusGroupName)) {
        		return businessGroup;
        	}
        }
        return null;
    }

}
