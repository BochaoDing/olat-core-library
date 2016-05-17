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
package ch.uzh.campus.data;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class ImportStatisticDaoTest extends OlatTestCase {

    @Autowired
    private DB dbInstance;

    @Autowired
    private ImportStatisticDao importStatisticDao;

    @Autowired
    private MockDataGenerator mockDataGenerator;

    private List<ImportStatistic> importStatistics;

    @Before
    public void setup() {
        importStatistics = mockDataGenerator.getImportStatistics();
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
        assertEquals(importStatisticDao.getLastCompletedImportedStatistic().size(), 1);
        assertEquals(importStatisticDao.getLastCompletedImportedStatistic().get(0).getStepId(), 20L);
    }

}
