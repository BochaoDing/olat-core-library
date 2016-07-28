package ch.uzh.campus;

import ch.uzh.campus.service.CampusCourseGroups;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;

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
 * Initial Date: 04.06.2012 <br>
 * 
 * @author cg
 */
public class CampusCourseJunitTestHelper {

    public static CampusCourseGroups setupCampusCourseGroupsForTest(Identity creatorIdentity, RepositoryEntry repositoryEntry, CampusCourseConfiguration campusCourseConfiguration, BGAreaManager bgAreaManager, BusinessGroupService businessGroupService) {
        // Create learning area
        String learningAreaName = campusCourseConfiguration.getCampusCourseLearningAreaName();
        BGArea campusLearningArea = bgAreaManager.createAndPersistBGArea(learningAreaName, "Campuslernbereich Test", repositoryEntry.getOlatResource());

        // Create Campusgruppe A
        BusinessGroup bgA = businessGroupService.createBusinessGroup(creatorIdentity, campusCourseConfiguration.getCourseGroupAName(), "Campusgruppe A Test", null, null, false, false, repositoryEntry);
        bgAreaManager.addBGToBGArea(bgA, campusLearningArea);

        // Create Campusgruppe B
        BusinessGroup bgB = businessGroupService.createBusinessGroup(creatorIdentity, campusCourseConfiguration.getCourseGroupBName(), "Campusgruppe B Test", null, null, false, false, repositoryEntry);
        bgAreaManager.addBGToBGArea(bgB, campusLearningArea);

        return new CampusCourseGroups(bgA, bgB);
	}
}
