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


import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.BusinessGroup;


import java.util.List;

/**
 * Initial Date: 25.06.2012 <br>
 * 
 * @author cg
 */
public class CampusGroupHelper {

    private static final OLog LOG = Tracing.createLoggerFor(CampusGroupHelper.class);

    public static BusinessGroup lookupCampusGroup(ICourse course, String campusGruppe) {
        CourseGroupManager courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();
        
        List <BusinessGroup> foundCampusGroups = courseGroupManager.getAllBusinessGroups();
        
        for (BusinessGroup businessGroup : foundCampusGroups ) {
        	if (businessGroup.getName().equals(campusGruppe)) {
        		return businessGroup;
        	}
        }
        LOG.error("Found no course-group with name=" + campusGruppe);
        throw new AssertException("Found no course-group with name=" + campusGruppe);
    }

}
