package ch.uzh.extension.campuscourse.service.synchronization;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import ch.uzh.extension.campuscourse.model.CampusGroups;
import ch.uzh.extension.campuscourse.model.CampusCourseTO;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
 *
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
public class CampusCourseSynchronizerTest {

    private static final long EXISTING_SAP_COURSE_ID = 4456;
    private CampusCourseSynchronizer campusCourseSynchronizerTestObject;

    private CampusCourseTO campusCourseTO;

    @Before
    public void setup() throws CampusCourseException {

        String title = "title";
        String eventDescription = "eventDescription";

        // Prepare a test sap campus course
        Set<Identity> lecturers = Collections.emptySet();
        Set<Identity> delegatees = Collections.emptySet();
        Set<Identity> students = Collections.emptySet();
        RepositoryEntry repositoryEntryMock = mock(RepositoryEntry.class);
        when(repositoryEntryMock.getDisplayname()).thenReturn(title);
        when(repositoryEntryMock.getDescription()).thenReturn(eventDescription);
        BusinessGroup campusGroupAMock = mock(BusinessGroup.class);
        BusinessGroup campusGroupBMock = mock(BusinessGroup.class);
        campusCourseTO = new CampusCourseTO(
                title, null, students, delegatees, lecturers, false, null, eventDescription,
                repositoryEntryMock, new CampusGroups(campusGroupAMock, campusGroupBMock), null, null, null
        );

        // Prepare a test campusCourseSynchronizer
        // CampusGroupsSynchronizerMock
        Identity creatorMock = mock(Identity.class);
        CampusCourseSynchronizationResult campusCourseSynchronizationResult = new CampusCourseSynchronizationResult(title, 1, 0, 7, 3);
        CampusGroupsSynchronizer campusGroupsSynchronizerMock = mock(CampusGroupsSynchronizer.class);
        when(campusGroupsSynchronizerMock.synchronizeCampusGroups(refEq(campusCourseTO.getCampusGroups()), refEq(campusCourseTO), refEq(creatorMock))).thenReturn(campusCourseSynchronizationResult);

        // OlatCampusCourseAttributeSynchronizerMock
        CampusCourseRepositoryEntrySynchronizer campusCourseRepositoryEntrySynchronizerMock = mock(CampusCourseRepositoryEntrySynchronizer.class);
        CampusCourseConfiguration campusCourseConfigurationMock = mock(CampusCourseConfiguration.class);

        // RepositoryServiceMock
        RepositoryService repositoryServiceMock = mock(RepositoryService.class);
        List<Identity> courseOwners = new ArrayList<>();
        courseOwners.add(creatorMock);
        when(repositoryServiceMock.getMembers(refEq(repositoryEntryMock), eq(GroupRoles.owner.name()))).thenReturn(courseOwners);

        campusCourseSynchronizerTestObject = new CampusCourseSynchronizer(
                campusGroupsSynchronizerMock,
                campusCourseRepositoryEntrySynchronizerMock,
                campusCourseConfigurationMock,
                repositoryServiceMock);
    }

    @Test
    public void testSynchronizeOlatCampusCourse() throws CampusCourseException {
        DaoManager daoManagerMock = mock(DaoManager.class);
        when(daoManagerMock.loadCampusCourseTO(EXISTING_SAP_COURSE_ID)).thenReturn(campusCourseTO);

        CampusCourseSynchronizationResult campusCourseSynchronizationResult = campusCourseSynchronizerTestObject.synchronizeOlatCampusCourse(campusCourseTO);
        assertNotNull(campusCourseSynchronizationResult);
        assertEquals(1, campusCourseSynchronizationResult.getAddedCoaches());
        assertEquals(0, campusCourseSynchronizationResult.getRemovedCoaches());
        assertEquals(7, campusCourseSynchronizationResult.getAddedParticipants());
        assertEquals(3, campusCourseSynchronizationResult.getRemovedParticipants());
    }
}
