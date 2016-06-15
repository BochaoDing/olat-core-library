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

import ch.uzh.campus.CampusConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.CampusCourseFactory;
import ch.uzh.campus.service.core.impl.creator.CourseDescriptionBuilder;
import ch.uzh.campus.service.core.impl.syncer.statistic.TitleAndDescriptionStatistik;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.test.OlatTestCase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Initial Date: 28.08.2012 <br>
 * 
 * @author cg
 */
@Component
public class CourseAttributeSynchronizerTest extends OlatTestCase {

    private long sapCampusCourseId = 1L;
    private Long resourceableId = 1002L;

    private String semester = "HS2012";
    private List<Identity> lecturers = new ArrayList<>();
    private List<Identity> participants = new ArrayList<>();
    private String title = "title";
    private String eventDescription = "eventDescription";

    private CourseAttributeSynchronizer courseAttributeSynchronizerTestObject;
    private CampusCourseFactory campusCourseFactoryMock;
    private CourseDescriptionBuilder courseDescriptionBuilderMock;
    private CampusConfiguration campusConfigurationMock;
    private CampusCourse campusCourse;

    @Before
    public void setup() {
        courseAttributeSynchronizerTestObject = new CourseAttributeSynchronizer();
        campusCourseFactoryMock = mock(CampusCourseFactory.class);
        courseDescriptionBuilderMock = mock(CourseDescriptionBuilder.class);
        campusConfigurationMock = mock(CampusConfiguration.class);
        courseAttributeSynchronizerTestObject.campusCourseFactory = campusCourseFactoryMock;
        courseAttributeSynchronizerTestObject.courseDescriptionBuilder = courseDescriptionBuilderMock;
        courseAttributeSynchronizerTestObject.campusConfiguration = campusConfigurationMock;
        ICourse course = mock(ICourse.class);
        RepositoryEntry repositoryEntry = mock(RepositoryEntry.class);
        when(repositoryEntry.getDisplayname()).thenReturn(title);
        when(repositoryEntry.getDescription()).thenReturn(eventDescription);
        campusCourse = new CampusCourse(course, repositoryEntry, null);
        when(campusCourseFactoryMock.getCampusCourse(sapCampusCourseId, resourceableId)).thenReturn(campusCourse);
    }

    @Test
    public void synchronizeTitleAndDescription_nothingToUpdate() {

        CampusCourseImportTO campusCourseTO = new CampusCourseImportTO(title, semester, lecturers, null, participants, eventDescription, resourceableId, sapCampusCourseId, null, null);
        when(courseDescriptionBuilderMock.buildDescriptionFrom(campusCourseTO, "de")).thenReturn(campusCourseTO.getEventDescription());
        when(campusConfigurationMock.getTemplateLanguage(campusCourseTO.getLanguage())).thenReturn("de");
        TitleAndDescriptionStatistik titleAndDescriptionStatistik = courseAttributeSynchronizerTestObject.synchronizeTitleAndDescription(campusCourseTO);

        assertNotNull("Missing TitleAndDescriptionStatistik", titleAndDescriptionStatistik);
        assertFalse("Title should not be updated", titleAndDescriptionStatistik.isTitleUpdated());
        assertFalse("Description should not be updated", titleAndDescriptionStatistik.isDescriptionUpdated());
    }

    @Test
    public void synchronizeTitleAndDescription_updateDescription() {

        CampusCourseImportTO campusCourseTO = new CampusCourseImportTO(title, semester, lecturers, null, participants, eventDescription + "_new", resourceableId, sapCampusCourseId, null, null);

        TitleAndDescriptionStatistik titleAndDescriptionStatistik = courseAttributeSynchronizerTestObject.synchronizeTitleAndDescription(campusCourseTO);

        assertNotNull("Missing TitleAndDescriptionStatistik", titleAndDescriptionStatistik);
        assertFalse("Title should not be updated", titleAndDescriptionStatistik.isTitleUpdated());
        assertTrue("Description should be updated", titleAndDescriptionStatistik.isDescriptionUpdated());
    }

    @Test
    public void synchronizeTitleAndDescription_updateTitle() {

        CampusCourseImportTO campusCourseTO = new CampusCourseImportTO(title + "_new", semester, lecturers, null, participants, eventDescription, resourceableId, sapCampusCourseId, null, null);
        // do not call real CampusCourse.setTitle(..) because there is a static call which try to save runstructure.xml
        CampusCourse spyCampusCourse = spy(campusCourse);
        doNothing().when(spyCampusCourse).setTitle(anyString());
        when(campusCourseFactoryMock.getCampusCourse(sapCampusCourseId, resourceableId)).thenReturn(spyCampusCourse);
        when(courseDescriptionBuilderMock.buildDescriptionFrom(campusCourseTO, campusCourseTO.getLanguage())).thenReturn(campusCourseTO.getEventDescription());
        TitleAndDescriptionStatistik titleAndDescriptionStatistik = courseAttributeSynchronizerTestObject.synchronizeTitleAndDescription(campusCourseTO);

        assertNotNull("Missing TitleAndDescriptionStatistik", titleAndDescriptionStatistik);
        assertTrue("Title should not be updated", titleAndDescriptionStatistik.isTitleUpdated());
        assertFalse("Description should be updated", titleAndDescriptionStatistik.isDescriptionUpdated());
    }

}
