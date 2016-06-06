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

import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.service.core.impl.syncer.CourseSynchronizer;
import ch.uzh.campus.service.core.impl.syncer.statistic.SynchronizedGroupStatistic;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
public class CourseSynchronizerTest {

    private static final long NOT_EXISTING_SAP_COURSE_ID = 4455;
    private CourseSynchronizer courseSynchronizerTestObject;

    @Before
    public void setup() {
        courseSynchronizerTestObject = new CourseSynchronizer();
    }

    
    @Test
    public void synchronizeCourse_CouldNotFoundCourse() {
        DaoManager daoManagerMock = mock(DaoManager.class);
        when(daoManagerMock.getSapCampusCourse(NOT_EXISTING_SAP_COURSE_ID)).thenReturn(null);
        // courseSynchronizerTestObject.campusDaoManager = daoManagerMock;

        SynchronizedGroupStatistic statistic = courseSynchronizerTestObject.synchronizeCourse(null);
        assertNotNull(statistic);
    }
    
    // TODO OLATng
    @Test
    public void synchronizeCourse_FoundCourse() {
    	
    }
}
