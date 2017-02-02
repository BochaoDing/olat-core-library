package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.synchronization;

import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.data.dao.BatchJobAndCampusCourseSynchronizationStatisticDao;
import ch.uzh.extension.campuscourse.data.entity.BatchJobAndCampusCourseSynchronizationStatistic;
import ch.uzh.extension.campuscourse.model.CampusCourseTO;
import ch.uzh.extension.campuscourse.service.synchronization.CampusCourseSynchronizationResult;
import ch.uzh.extension.campuscourse.service.synchronization.CampusCourseSynchronizer;
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

		// Mock for CampusCourseTOs
		CampusCourseTO campusCourseTOMock1 = mock(CampusCourseTO.class);
		CampusCourseTO campusCourseTOMock2 = mock(CampusCourseTO.class);
		twoCoursesList.add(campusCourseTOMock1);
		twoCoursesList.add(campusCourseTOMock2);

        // Mock for CourseSynchronizer
        CampusCourseSynchronizer campusCourseSynchronizerMock = mock(CampusCourseSynchronizer.class);
		when(campusCourseSynchronizerMock.synchronizeOlatCampusCourse(campusCourseTOMock1)).thenReturn(new CampusCourseSynchronizationResult("course1", 2, 1, 12, 4));
		when(campusCourseSynchronizerMock.synchronizeOlatCampusCourse(campusCourseTOMock2)).thenReturn(new CampusCourseSynchronizationResult("course2", 1, 0, 7, 2));

		// Mock for UserMappingStatisticDao
		BatchJobAndCampusCourseSynchronizationStatisticDao batchJobAndCampusCourseSynchronizationStatisticDao = mock(BatchJobAndCampusCourseSynchronizationStatisticDao.class);
		when(batchJobAndCampusCourseSynchronizationStatisticDao.getLastCreatedBatchJobAndCampusCourseSynchronizationStatistic()).thenReturn(new BatchJobAndCampusCourseSynchronizationStatistic());

        // Mock for DBImpl
        DB campusCourseDBImplMock = mock(DB.class);

        // Test object
        campusCourseSynchronizationWriterTestObject = new CampusCourseSynchronizationWriter(campusCourseSynchronizerMock, campusCourseDBImplMock, batchJobAndCampusCourseSynchronizationStatisticDao);
    }

    @Test
    public void write_emptyCoursesList() throws Exception {
        campusCourseSynchronizationWriterTestObject.write(Collections.emptyList());
        assertEquals("added coaches: 0, removed coaches: 0, added participants: 0, removed participants: 0",
                campusCourseSynchronizationWriterTestObject.getCampusCourseSynchronizationStatistic().toString());
    }

    @Test
    public void write_twoCoursesList() throws Exception {
        campusCourseSynchronizationWriterTestObject.write(twoCoursesList);
        assertEquals("added coaches: 3, removed coaches: 1, added participants: 19, removed participants: 6",
                campusCourseSynchronizationWriterTestObject.getCampusCourseSynchronizationStatistic().toString());
    }
}
