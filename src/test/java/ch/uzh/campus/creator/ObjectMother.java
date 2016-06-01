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
package ch.uzh.campus.creator;


import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.BusinessGroupService;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author cg
 */
public class ObjectMother {

    // TODO OLATng
//    public static Identity createIdentity(String name) {
//        User newUser = createTestUserForJunit(name);
//
//        // How to create identity???
//        return identity;
//    }
//

    public static void setupCampusCourseGroupForTest(ICourse course, String groupName, BusinessGroupService businessGroupService) {
        CourseGroupManager courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();

        // TODO OLATng - there are no GroupContexts any more
//        List groupContexts = courseGroupManager.getLearningGroupContexts(course);
//        for (Object item : groupContexts) {
//            BGContext bgContext = (BGContext) item;
//            businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, groupName, null, new Integer(0), new Integer(10), false, false,
//                    bgContext);
//        }
    }

    public static User createTestUserForJunit(final String name) {
        User newUser = new TestUser();
        newUser.setProperty(UserConstants.FIRSTNAME, "first_" + name);
        newUser.setProperty(UserConstants.LASTNAME, "last_" + name);
        newUser.setProperty(UserConstants.EMAIL, "@email.tst");

        return newUser;
    }

}
