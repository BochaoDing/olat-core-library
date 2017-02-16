package ch.uzh.extension.campuscourse;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.data.entity.Course;
import ch.uzh.extension.campuscourse.data.dao.CourseDao;
import ch.uzh.extension.campuscourse.data.entity.Semester;
import ch.uzh.extension.campuscourse.data.dao.SemesterDao;
import ch.uzh.extension.campuscourse.model.CampusGroups;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;

import java.util.Date;

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
 * @author Martin Schraner
 */
public class CampusCourseJunitTestHelper {

    public static Course createCourseForTest(Long id, Semester semester, SemesterDao semesterDao, CourseDao courseDao, DB dbInstance) {
        semesterDao.save(semester);

        Course sapCampusCourse = new Course(id, "XXXXlvKuerzel", "title", "lvNr", false, "DE", "category", new Date(), new Date(), "vvzLink", false, new Date(), semester);
        courseDao.save(sapCampusCourse);

        dbInstance.flush();
        return sapCampusCourse;
    }

    public static CampusGroups createCampusGroupsForTest(Identity creatorIdentity, RepositoryEntry repositoryEntry, CampusCourseConfiguration campusCourseConfiguration, CourseDao courseDao, BGAreaManager bgAreaManager, BusinessGroupService businessGroupService, DB dbInstance) {
        return createCampusGroupsForTest(null, creatorIdentity, repositoryEntry, campusCourseConfiguration, courseDao, bgAreaManager, businessGroupService, dbInstance);
    }

    private static CampusGroups createCampusGroupsForTest(Course sapCampusCourse, Identity creatorIdentity, RepositoryEntry repositoryEntry, CampusCourseConfiguration campusCourseConfiguration, CourseDao courseDao, BGAreaManager bgAreaManager, BusinessGroupService businessGroupService, DB dbInstance) {
        // Create learning area
        String learningAreaName = campusCourseConfiguration.getCampusGroupsLearningAreaName();
        BGArea campusLearningArea = bgAreaManager.createAndPersistBGArea(learningAreaName, "Campuslernbereich Test", repositoryEntry.getOlatResource());

        // Create campus group A
        BusinessGroup campusGroupA = businessGroupService.createBusinessGroup(creatorIdentity, campusCourseConfiguration.getCampusGroupADefaultName(), "Campusgruppe A Test", null, null, false, false, repositoryEntry);
        bgAreaManager.addBGToBGArea(campusGroupA, campusLearningArea);
        if (sapCampusCourse != null) {
            courseDao.saveCampusGroupA(sapCampusCourse.getId(), campusGroupA.getKey());
        }

        // Create campus group B
        BusinessGroup campusGroupB = businessGroupService.createBusinessGroup(creatorIdentity, campusCourseConfiguration.getCampusGroupBDefaultName(), "Campusgruppe B Test", null, null, false, false, repositoryEntry);
        bgAreaManager.addBGToBGArea(campusGroupB, campusLearningArea);
        if (sapCampusCourse != null) {
            courseDao.saveCampusGroupB(sapCampusCourse.getId(), campusGroupB.getKey());
        }

        dbInstance.flush();
        return new CampusGroups(campusGroupA, campusGroupB);
	}

    public static Identity createTestUser(UserManager userManager, DB dbInstance, String userName) {
        User user = userManager.createUser("testUserFirstName" + userName, "testUserLastName" + userName, userName + "@uzh.ch");
        dbInstance.saveObject(user);
        Identity identity = new IdentityImpl(userName, user);
        dbInstance.saveObject(identity);
        dbInstance.flush();
        return identity;
    }
}
