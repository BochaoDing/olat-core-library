package ch.uzh.campus.service.core.impl.syncer;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseException;
import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import ch.uzh.campus.service.core.impl.OlatCampusCourseProvider;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedGroupStatistic;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedSecurityGroupStatistic;
import ch.uzh.campus.service.data.CampusGroups;
import ch.uzh.campus.service.data.OlatCampusCourse;
import ch.uzh.campus.service.data.SapCampusCourseTO;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.refEq;
import static org.mockito.Matchers.eq;
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

    private SapCampusCourseTO sapCampusCourseTO;

    @Before
    public void setup() throws CampusCourseException {

        String title = "title";
        String eventDescription = "eventDescription";

        // Prepare a test sap campus course
        Set<Identity> lecturers = Collections.emptySet();
        Set<Identity> delegatees = Collections.emptySet();
        Set<Identity> students = Collections.emptySet();
        OLATResource olatResourceMock = mock(OLATResource.class);
        BusinessGroup campusGroupAMock = mock(BusinessGroup.class);
        BusinessGroup campusGroupBMock = mock(BusinessGroup.class);
        sapCampusCourseTO = new SapCampusCourseTO(
                title, null, students, delegatees, lecturers, false, null, eventDescription,
                olatResourceMock, new CampusGroups(campusGroupAMock, campusGroupBMock), null, null, null
        );

        // Prepare a test olat campus course
        ICourse courseMock = mock(ICourse.class);
        RepositoryEntry repositoryEntryMock = mock(RepositoryEntry.class);
        when(repositoryEntryMock.getDisplayname()).thenReturn(title);
        when(repositoryEntryMock.getDescription()).thenReturn(eventDescription);
        OlatCampusCourse olatCampusCourse = new OlatCampusCourse(courseMock, repositoryEntryMock);

        // CampusGroupsSynchronizerMock
        Identity creatorMock = mock(Identity.class);
        SynchronizedGroupStatistic groupStatistic = new SynchronizedGroupStatistic(title, null, new SynchronizedSecurityGroupStatistic(5, 10));
        CampusGroupsSynchronizer campusGroupsSynchronizerMock = mock(CampusGroupsSynchronizer.class);
        when(campusGroupsSynchronizerMock.synchronizeCampusGroups(sapCampusCourseTO.getCampusGroups(), sapCampusCourseTO, refEq(creatorMock))).thenReturn(groupStatistic);

        // OlatCampusCourseAttributeSynchronizerMock
        CampusCourseRepositoryEntrySynchronizer campusCourseRepositoryEntrySynchronizerMock = mock(CampusCourseRepositoryEntrySynchronizer.class);
        CampusCourseConfiguration campusCourseConfigurationMock = mock(CampusCourseConfiguration.class);

        // OlatCampusCourseProviderMock
        OlatCampusCourseProvider olatCampusCourseProviderMock = mock(OlatCampusCourseProvider.class);
        when(olatCampusCourseProviderMock.loadOlatCampusCourse(refEq(sapCampusCourseTO.getOlatResource()))).thenReturn(olatCampusCourse);

        // CampusCourseCoreServiceMock
        CampusCourseCoreService campusCourseCoreServiceMock = mock(CampusCourseCoreService.class);
        when(campusCourseCoreServiceMock.loadOlatCampusCourse(refEq(sapCampusCourseTO.getOlatResource()))).thenReturn(olatCampusCourse);

        // RepositoryServiceMock
        RepositoryService repositoryServiceMock = mock(RepositoryService.class);
        List<Identity> courseOwners = new ArrayList<>();
        courseOwners.add(creatorMock);
        when(repositoryServiceMock.getMembers(refEq(olatCampusCourse.getRepositoryEntry()), eq(GroupRoles.owner.name()))).thenReturn(courseOwners);

        campusCourseSynchronizerTestObject = new CampusCourseSynchronizer(
                campusGroupsSynchronizerMock,
                campusCourseRepositoryEntrySynchronizerMock,
                campusCourseConfigurationMock,
                campusCourseCoreServiceMock,
                repositoryServiceMock);
    }

    @Test
    public void synchronizeCourse() throws CampusCourseException {
        DaoManager daoManagerMock = mock(DaoManager.class);
        when(daoManagerMock.loadSapCampusCourseTO(EXISTING_SAP_COURSE_ID)).thenReturn(sapCampusCourseTO);

        SynchronizedGroupStatistic statistic = campusCourseSynchronizerTestObject.synchronizeOlatCampusCourse(sapCampusCourseTO);
        assertNotNull(statistic);
        assertEquals(5, statistic.getParticipantGroupStatistic().getAddedStatistic());
        assertEquals(10, statistic.getParticipantGroupStatistic().getRemovedStatistic());
    }
}
