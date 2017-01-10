package ch.uzh.campus.data;

import ch.uzh.campus.CampusCourseTestCase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
@Component
public class ImportStatisticDaoTest extends CampusCourseTestCase {

    @Autowired
    private ImportStatisticDao importStatisticDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    private List<ImportStatistic> importStatistics;

    @Before
    public void setup() {
        importStatistics = mockDataGeneratorProvider.get().getImportStatistics();
    }

    @Test
    public void getLastCompletedImportedStatistic_notFound() {        
    	importStatisticDao.save(importStatistics.get(2));
        dbInstance.flush();

        assertTrue(importStatisticDao.getLastCompletedImportedStatistic().isEmpty());
    }

    @Test
    public void getLastCompletedImportedStatistic_found() {        
       importStatisticDao.save(importStatistics.get(0));
       importStatisticDao.save(importStatistics.get(1));
       dbInstance.flush();

       assertEquals(1, importStatisticDao.getLastCompletedImportedStatistic().size());
       assertEquals(20L, importStatisticDao.getLastCompletedImportedStatistic().get(0).getStepId());
    }

    @Test
    public void testGetStartTimeOfMostRecentCompletedCourseImport() {
        importStatisticDao.save(importStatistics.get(3));
        dbInstance.flush();

        Calendar startTimeOfMostRecentCourseImportAsCalendar = new GregorianCalendar(2099, Calendar.OCTOBER, 11);
        startTimeOfMostRecentCourseImportAsCalendar.set(Calendar.HOUR_OF_DAY, 10);
        startTimeOfMostRecentCourseImportAsCalendar.set(Calendar.MINUTE, 13);
        startTimeOfMostRecentCourseImportAsCalendar.set(Calendar.SECOND, 0);
        assertEquals(startTimeOfMostRecentCourseImportAsCalendar.getTime(), importStatisticDao.getStartTimeOfMostRecentCompletedCourseImport());
    }

}
