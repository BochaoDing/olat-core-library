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
package ch.uzh.campus.service.core.impl.creator;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.uzh.campus.service.CampusCourse;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author cg
 */
@Component
public class CourseTemplate {
    
	@Autowired
	private RepositoryManager repositoryManager;   
    
    @Autowired
    RepositoryService repositoryService;

    public CampusCourse createCampusCourseFromTemplate(Long templateCourseResourceableId, Identity owner) {
        // 1. Lookup template
        OLATResourceable templateCourse = CourseFactory.loadCourse(templateCourseResourceableId);
        RepositoryEntry sourceRepositoryEntry = repositoryManager.lookupRepositoryEntry(templateCourse, true);
             
        // 2. Copy RepositoryEntry and implicit the Course
        RepositoryEntry copyOfRepositoryEntry = repositoryService.copy(sourceRepositoryEntry, owner, sourceRepositoryEntry.getDisplayname());
        OLATResourceable copyCourseOlatResourcable = repositoryService.loadRepositoryEntryResource(copyOfRepositoryEntry.getKey());
        ICourse copyCourse = CourseFactory.loadCourse(copyCourseOlatResourcable.getResourceableId());
        
        return new CampusCourse(copyCourse, copyOfRepositoryEntry);
    }
  

}
