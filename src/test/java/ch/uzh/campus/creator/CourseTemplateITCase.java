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

import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.creator.CourseTemplate;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author cg
 */
public class CourseTemplateITCase extends OlatTestCase {

    private Long sourceResourceableId;
    @Autowired
    private CourseTemplate courseTemplate;
    private Identity ownerIdentity;
    private ICourse sourceCourse;
    RepositoryEntry sourceRepositoryEntry;
    String ownerName = "owner";

    @Before
    public void setup() {
        ownerIdentity = JunitTestHelper.createAndPersistIdentityAsUser(ownerName);
        sourceRepositoryEntry = JunitTestHelper.deployDemoCourse(ownerIdentity); // TODO OLATng - check if this is acceptable identity
        sourceResourceableId = sourceRepositoryEntry.getOlatResource().getResourceableId();
        sourceCourse = CourseFactory.loadCourse(sourceResourceableId);
        DBFactory.getInstance().closeSession();
    }

    @Test
    public void createCampusCourseFromTemplate() {
        CampusCourse campusCourse = courseTemplate.createCampusCourseFromTemplate(sourceResourceableId, ownerIdentity);
        assertNotNull(campusCourse);
        assertNotNull(campusCourse.getCourse());
        assertNotNull(campusCourse.getRepositoryEntry());
        assertTrue("Copy must have different resourcableId", (sourceResourceableId != campusCourse.getCourse().getResourceableId()));
        ICourse copyCourse = CourseFactory.loadCourse(campusCourse.getCourse().getResourceableId());
        assertEquals("Course-title must be the same in the copy", sourceCourse.getCourseTitle(), copyCourse.getCourseTitle());
        assertEquals("Displayname of RepositoryEntry must be the same in the copy", sourceRepositoryEntry.getDisplayname(), campusCourse.getRepositoryEntry()
                .getDisplayname());
        assertEquals("Wrong initialAuthor in copy", ownerName, campusCourse.getRepositoryEntry().getInitialAuthor());
    }
}
