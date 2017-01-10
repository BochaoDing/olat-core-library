package ch.uzh.campus.service.core.impl.syncer;

import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.service.data.CampusCourseTO;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
 * <p>
 *
 * Initial Date: 08.11.2012 <br>
 * 
 * @author aabouc
 */
public class CampusCourseSynchronizationReaderTest {
    private CampusCourseSynchronizationReader campusCourseSynchronizationReaderTestObject;
    private DaoManager daoManagerMock;

    @Before
    public void setup() {
        // Mocks for DaoManager and DB
        daoManagerMock = mock(DaoManager.class);
        DB dbMock = mock(DB.class);
        campusCourseSynchronizationReaderTestObject = new CampusCourseSynchronizationReader(daoManagerMock, dbMock);
    }

    @Test
    public void destroy_nullCoursesList() {
        when(daoManagerMock.getAllCreatedSapCources()).thenReturn(null);
        campusCourseSynchronizationReaderTestObject.init();
        campusCourseSynchronizationReaderTestObject.destroy();
    }

    @Test
    public void destroy_emptyCoursesList() {
        when(daoManagerMock.getAllCreatedSapCources()).thenReturn(new ArrayList<>());
        campusCourseSynchronizationReaderTestObject.init();
        campusCourseSynchronizationReaderTestObject.destroy();
    }

    @Test
    public void read_nullCoursesList() throws Exception {
        when(daoManagerMock.getAllCreatedSapCources()).thenReturn(null);
        campusCourseSynchronizationReaderTestObject.init();
        assertNull(campusCourseSynchronizationReaderTestObject.read());
    }

    @Test
    public void read_emptyCoursesList() throws Exception {
        when(daoManagerMock.getAllCreatedSapCources()).thenReturn(new ArrayList<>());
        campusCourseSynchronizationReaderTestObject.init();
        assertNull(campusCourseSynchronizationReaderTestObject.read());
    }

    @Test
    public void read_twoCoursesList() throws Exception {
        when(daoManagerMock.checkImportedData()).thenReturn(true);
        List<Long> CreatedSapCourcesIds = new ArrayList<>();
        CreatedSapCourcesIds.add(100L);
        CreatedSapCourcesIds.add(200L);
        Calendar startTimeOfMostRecentCourseImportAsCalendar = new GregorianCalendar(2099, Calendar.OCTOBER, 11);
        startTimeOfMostRecentCourseImportAsCalendar.set(Calendar.HOUR_OF_DAY, 10);
        startTimeOfMostRecentCourseImportAsCalendar.set(Calendar.MINUTE, 13);
        startTimeOfMostRecentCourseImportAsCalendar.set(Calendar.SECOND, 0);
		when(daoManagerMock.getStartTimeOfMostRecentCompletedCourseImport()).thenReturn(startTimeOfMostRecentCourseImportAsCalendar.getTime());
        when(daoManagerMock.getSapIdsOfAllCreatedSynchronizableCoursesOfCurrentSemesterAndMostRecentImport(startTimeOfMostRecentCourseImportAsCalendar.getTime())).thenReturn(CreatedSapCourcesIds);

        CampusCourseTO campusCourseTOMock1 = mock(CampusCourseTO.class);
        CampusCourseTO campusCourseTOMock2 = mock(CampusCourseTO.class);
        when(daoManagerMock.loadCampusCourseTO(100L)).thenReturn(campusCourseTOMock1);
        when(daoManagerMock.loadCampusCourseTO(200L)).thenReturn(campusCourseTOMock2);

        campusCourseSynchronizationReaderTestObject.init();
        // The first read delivers the first course
        assertNotNull(campusCourseSynchronizationReaderTestObject.read());
        // The second read delivers the second course
        assertNotNull(campusCourseSynchronizationReaderTestObject.read());
        // The third read delivers null
        assertNull(campusCourseSynchronizationReaderTestObject.read());
    }
}
