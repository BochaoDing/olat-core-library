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

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.CampusCourseFactory;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedGroupStatistic;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedSecurityGroupStatistic;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
public class CampusCourseSynchronizerTest {

    private static final long NOT_EXISTING_SAP_COURSE_ID = 4455;
    private static final long EXISTING_SAP_COURSE_ID = 4456;
    private CampusCourseSynchronizer campusCourseSynchronizerTestObject;

    private CampusCourseImportTO campusCourseImportTO;
    private List<Identity> lecturers = new ArrayList<>();
    private List<Identity> participants = new ArrayList<>();

    @Before
    public void setup() {

        String title = "title";
        String eventDescription = "eventDescription";

        // Prepare a test CampusCourse
        ICourse course = mock(ICourse.class);
        RepositoryEntry repositoryEntry = mock(RepositoryEntry.class);
        when(repositoryEntry.getDisplayname()).thenReturn(title);
        when(repositoryEntry.getDescription()).thenReturn(eventDescription);
        CampusCourse campusCourse = new CampusCourse(course, repositoryEntry);

        // Prepare a test CampusCourseImportTO
        campusCourseImportTO = new CampusCourseImportTO(
                title, "HS2012", lecturers, Collections.emptyList(), participants, eventDescription,
                1045L, EXISTING_SAP_COURSE_ID, null, null
        );

        // mock injections for CampusCourseSynchronizer
        SynchronizedGroupStatistic groupStatistic = new SynchronizedGroupStatistic(title, null, new SynchronizedSecurityGroupStatistic(5, 10));
        CampusCourseGroupSynchronizer campusCourseGroupSynchronizerMock = mock(CampusCourseGroupSynchronizer.class);
        when(campusCourseGroupSynchronizerMock.synchronizeCourseGroups(campusCourse, campusCourseImportTO)).thenReturn(groupStatistic);
        CampusCourseAttributeSynchronizer campusCourseAttributeSynchronizerMock = mock(CampusCourseAttributeSynchronizer.class);
        CampusCourseConfiguration campusCourseConfigurationMock = mock(CampusCourseConfiguration.class);
        CampusCourseFactory campusCourseFactoryMock = mock(CampusCourseFactory.class);
        when(campusCourseFactoryMock.getCampusCourse(EXISTING_SAP_COURSE_ID)).thenReturn(campusCourse);

        campusCourseSynchronizerTestObject = new CampusCourseSynchronizer(
                campusCourseGroupSynchronizerMock, campusCourseAttributeSynchronizerMock,
                campusCourseConfigurationMock, campusCourseFactoryMock
        );
    }
    
    @Test
    public void synchronizeCourse_CouldNotFindCourse() {
        DaoManager daoManagerMock = mock(DaoManager.class);
        when(daoManagerMock.getSapCampusCourse(NOT_EXISTING_SAP_COURSE_ID)).thenReturn(null);

        SynchronizedGroupStatistic statistic = campusCourseSynchronizerTestObject.synchronizeCourse(null);
        assertNotNull(statistic);
        assertTrue(statistic.getCourseTitle().startsWith(SynchronizedGroupStatistic.EMPTY_STATISTIC));
        assertEquals(0, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals(0, statistic.getParticipantGroupStatistic().getRemovedStatistic());
    }
    
    @Test
    public void synchronizeCourse_FoundCourse() {
        DaoManager daoManagerMock = mock(DaoManager.class);
        when(daoManagerMock.getSapCampusCourse(EXISTING_SAP_COURSE_ID)).thenReturn(campusCourseImportTO);

        SynchronizedGroupStatistic statistic = campusCourseSynchronizerTestObject.synchronizeCourse(campusCourseImportTO);
        assertNotNull(statistic);
        assertFalse(statistic.getCourseTitle().startsWith(SynchronizedGroupStatistic.EMPTY_STATISTIC));
        assertEquals(5, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals(10, statistic.getParticipantGroupStatistic().getRemovedStatistic());
    }
}
