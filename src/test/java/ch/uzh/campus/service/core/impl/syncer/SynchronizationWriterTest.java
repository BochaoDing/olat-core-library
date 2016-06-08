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

import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.service.core.impl.syncer.CourseSynchronizer;
import ch.uzh.campus.service.core.impl.syncer.SynchronizationWriter;
import ch.uzh.campus.service.core.impl.syncer.statistic.OverallSynchronizeStatistic;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedGroupStatistic;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedSecurityGroupStatistic;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Initial Date: 08.11.2012 <br>
 * 
 * @author aabouc
 */
public class SynchronizationWriterTest {
    private SynchronizationWriter synchronizationWriterTestObject;
    private CourseSynchronizer courseSynchronizerMock;
    private List<CampusCourseImportTO> twoCoursesList = new ArrayList<CampusCourseImportTO>();
    private SynchronizedGroupStatistic synchronizedGroupStatisticforCourse1, synchronizedGroupStatisticforCourse2;

    @Before
    public void setup() {
        synchronizationWriterTestObject = new SynchronizationWriter();
        // Mock for CourseSynchronizer
        courseSynchronizerMock = mock(CourseSynchronizer.class);
        synchronizationWriterTestObject.courseSynchronizer = courseSynchronizerMock;
        // Test OverallSynchronizeStatistic
        synchronizationWriterTestObject.setSynchronizeStatistic(new OverallSynchronizeStatistic());
        // Mock for DBImpl
        // synchronizatioWriterTestObject.dBImpl = mock(DB.class);
        // Test SynchronizedGroupStatistic
        synchronizedGroupStatisticforCourse1 = new SynchronizedGroupStatistic("course1", null, new SynchronizedSecurityGroupStatistic(15, 0));
        synchronizedGroupStatisticforCourse2 = new SynchronizedGroupStatistic("course2", null, new SynchronizedSecurityGroupStatistic(0, 9));
        // Mock for CampusCourseImportTO
        CampusCourseImportTO courseMock1 = mock(CampusCourseImportTO.class);
        CampusCourseImportTO courseMock2 = mock(CampusCourseImportTO.class);
        twoCoursesList.add(courseMock1);
        twoCoursesList.add(courseMock2);

        // TODO OLATng
//        when(courseSynchronizerMock.synchronizeCourse(courseMock1)).thenReturn(synchronizedGroupStatisticforCourse1);
//        when(courseSynchronizerMock.synchronizeCourse(courseMock2)).thenReturn(synchronizedGroupStatisticforCourse2);

    }

    // TODO OLATng
//    @Test
//    public void write_emptyCoursesList() throws Exception {
//        synchronizationWriterTestObject.write(Collections.emptyList());
//        assertEquals(synchronizationWriterTestObject.getSynchronizeStatistic().calculateOverallStatistic(),
//                "overallAddedOwners=0 , overallRemovedOwners=0 ; overallAddedParticipants=0 , overallRemovedParticipants=0");
//    }
//
//    @Test
//    public void write_twoCoursesList() throws Exception {
//        synchronizationWriterTestObject.write(twoCoursesList);
//        assertEquals(synchronizationWriterTestObject.getSynchronizeStatistic().calculateOverallStatistic(),
//                "overallAddedOwners=0 , overallRemovedOwners=0 ; overallAddedParticipants=15 , overallRemovedParticipants=9");
//    }
}