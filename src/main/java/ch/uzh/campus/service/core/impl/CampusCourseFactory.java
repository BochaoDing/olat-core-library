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
package ch.uzh.campus.service.core.impl;


import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.service.CampusCourse;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 20.08.2012 <br>
 * 
 * @author cg
 */
@Component
public class CampusCourseFactory {
   
	private static final OLog LOG = Tracing.createLoggerFor(CampusCourseFactory.class);

	@Autowired
	private RepositoryManager repositoryManager;
	
    @Autowired
    private DaoManager daoManager;

    public CampusCourse getCampusCourse(Long sapCampusCourseId) {
        CampusCourseImportTO campusCourseTo = daoManager.getSapCampusCourse(sapCampusCourseId);
        LOG.debug("getRepositoryEntryFor sapCourseId=" + sapCampusCourseId + "  campusCourseTo.getOlatResourceableId()=" + campusCourseTo.getOlatResourceableId());
        if (campusCourseTo.getOlatResourceableId() == null) {
            LOG.warn("sapCourseId = " + sapCampusCourseId + ": no OLAT course found");
            return null;
        }
        ICourse olatCourse = CourseFactory.loadCourse(campusCourseTo.getOlatResourceableId());
        RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(olatCourse, true);
        return new CampusCourse(olatCourse, repositoryEntry);
    }
}
