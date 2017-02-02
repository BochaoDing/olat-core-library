package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import ch.uzh.extension.campuscourse.CampusCourseTestDataGenerator;
import ch.uzh.extension.campuscourse.data.entity.BatchJobSkippedItem;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
public class BatchJobSkippedItemDaoTest extends CampusCourseTestCase {

    @Autowired
    private BatchJobSkippedItemDao batchJobSkippedItemDao;

    @Autowired
    private CampusCourseTestDataGenerator campusCourseTestDataGenerator;

    @Test
	public void testSave() {
		List<BatchJobSkippedItem> batchJobSkippedItems = campusCourseTestDataGenerator.createBatchJobSkippedItems();
		for (BatchJobSkippedItem batchJobSkippedItem : batchJobSkippedItems) {
			batchJobSkippedItemDao.save(batchJobSkippedItem);
			dbInstance.flush();
			Long id = batchJobSkippedItem.getId();
			assertNotNull(dbInstance.getCurrentEntityManager().find(BatchJobSkippedItem.class, id));
		}
	}

}
