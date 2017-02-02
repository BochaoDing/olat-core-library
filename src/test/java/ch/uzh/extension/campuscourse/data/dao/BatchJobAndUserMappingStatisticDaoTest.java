package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import ch.uzh.extension.campuscourse.CampusCourseTestDataGenerator;
import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.data.entity.BatchJobAndUserMappingStatistic;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
public class BatchJobAndUserMappingStatisticDaoTest extends CampusCourseTestCase {

    @Autowired
    private BatchJobAndUserMappingStatisticDao batchJobAndUserMappingStatisticDao;

    @Autowired
    private CampusCourseTestDataGenerator campusCourseTestDataGenerator;

	@Before
	public void setup() throws CampusCourseException {
		insertTestData();
	}

    @Test
	public void testGetLastCreatedUserMappingStatisticForCampusBatchStepName() {

		BatchJobAndUserMappingStatistic batchJobAndUserMappingStatistic = batchJobAndUserMappingStatisticDao.getLastCreatedUserMappingStatisticForCampusBatchStepName(CampusBatchStepName.LECTURER_MAPPING);

		assertNotNull(batchJobAndUserMappingStatistic);
		assertEquals(CampusBatchStepName.LECTURER_MAPPING, batchJobAndUserMappingStatistic.getCampusBatchStepName());
		Calendar startTime = new GregorianCalendar(2099, Calendar.NOVEMBER, 11);
		startTime.set(Calendar.HOUR_OF_DAY, 10);
		startTime.set(Calendar.MINUTE, 10);
		assertEquals(startTime.getTime(), batchJobAndUserMappingStatistic.getStartTime());
	}

	private void insertTestData() throws CampusCourseException {
		List<BatchJobAndUserMappingStatistic> batchJobAndUserMappingStatistics = campusCourseTestDataGenerator.createBatchJobAndUserMappingStatistics();
		batchJobAndUserMappingStatisticDao.save(batchJobAndUserMappingStatistics);
		dbInstance.flush();
	}

}
