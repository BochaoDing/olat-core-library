package ch.uzh.campus.service.core.impl.syncer;

import ch.uzh.campus.CampusCourseException;
import ch.uzh.campus.service.data.CampusCourseTO;
import ch.uzh.campus.service.core.impl.syncer.statistic.OverallSynchronizeStatistic;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedGroupStatistic;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedSecurityGroupStatistic;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
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
public class CampusCourseSynchronizationWriterTest {
    private CampusCourseSynchronizationWriter campusCourseSynchronizationWriterTestObject;
    private List<CampusCourseTO> twoCoursesList = new ArrayList<>();

    @Before
    public void setup() throws CampusCourseException {
        // Mock for CourseSynchronizer
        CampusCourseSynchronizer campusCourseSynchronizerMock = mock(CampusCourseSynchronizer.class);
        // Mock for DBImpl
        DB campusCourseDBImplMock = mock(DB.class);
        // Test object
        campusCourseSynchronizationWriterTestObject = new CampusCourseSynchronizationWriter(campusCourseSynchronizerMock, campusCourseDBImplMock);
        // Test OverallSynchronizeStatistic
        campusCourseSynchronizationWriterTestObject.setSynchronizeStatistic(new OverallSynchronizeStatistic());
        // Test SynchronizedGroupStatistic
        SynchronizedGroupStatistic synchronizedGroupStatisticforCourse1 = new SynchronizedGroupStatistic("course1", null, new SynchronizedSecurityGroupStatistic(15, 0));
        SynchronizedGroupStatistic synchronizedGroupStatisticforCourse2 = new SynchronizedGroupStatistic("course2", null, new SynchronizedSecurityGroupStatistic(0, 9));

        CampusCourseTO campusCourseTOMock1 = mock(CampusCourseTO.class);
        CampusCourseTO campusCourseTOMock2 = mock(CampusCourseTO.class);
        twoCoursesList.add(campusCourseTOMock1);
        twoCoursesList.add(campusCourseTOMock2);

        when(campusCourseSynchronizerMock.synchronizeOlatCampusCourse(campusCourseTOMock1)).thenReturn(synchronizedGroupStatisticforCourse1);
        when(campusCourseSynchronizerMock.synchronizeOlatCampusCourse(campusCourseTOMock2)).thenReturn(synchronizedGroupStatisticforCourse2);
    }

    @Test
    public void write_emptyCoursesList() throws Exception {
        campusCourseSynchronizationWriterTestObject.write(Collections.emptyList());
        assertEquals("overallAddedCoaches=0 , overallRemovedCoaches=0 ; overallAddedParticipants=0 , overallRemovedParticipants=0",
                campusCourseSynchronizationWriterTestObject.getSynchronizeStatistic().calculateOverallStatistic());
    }

    @Test
    public void write_twoCoursesList() throws Exception {
        campusCourseSynchronizationWriterTestObject.write(twoCoursesList);
        assertEquals("overallAddedCoaches=0 , overallRemovedCoaches=0 ; overallAddedParticipants=15 , overallRemovedParticipants=9",
                campusCourseSynchronizationWriterTestObject.getSynchronizeStatistic().calculateOverallStatistic());
    }
}
