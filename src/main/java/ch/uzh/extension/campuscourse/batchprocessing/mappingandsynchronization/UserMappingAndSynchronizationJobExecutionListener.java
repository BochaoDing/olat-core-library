package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization;

import ch.uzh.extension.campuscourse.data.dao.BatchJobAndSapImportStatisticDao;
import ch.uzh.extension.campuscourse.data.entity.BatchJobAndSapImportStatistic;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
 * Initial Date: 08.01.2014 <br>
 * 
 * @author aabouc
 */
@Component
public class UserMappingAndSynchronizationJobExecutionListener implements JobExecutionListener {

    private static final OLog LOG = Tracing.createLoggerFor(UserMappingAndSynchronizationJobExecutionListener.class);

    private final DB dbInstance;
    private final BatchJobAndSapImportStatisticDao batchJobAndSapImportStatisticDao;

    @Autowired
    public UserMappingAndSynchronizationJobExecutionListener(DB dbInstance, BatchJobAndSapImportStatisticDao batchJobAndSapImportStatisticDao) {
        this.dbInstance = dbInstance;
        this.batchJobAndSapImportStatisticDao = batchJobAndSapImportStatisticDao;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LOG.info("afterJob " + jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        LOG.info("beforeJob " + jobExecution.getJobInstance().getJobName());
        // Check if "importJob" has ran today
        List<BatchJobAndSapImportStatistic> batchJobAndSapImportStatisticOfToday = batchJobAndSapImportStatisticDao.getSapImportStatisticOfToday();
        if (batchJobAndSapImportStatisticOfToday.size() == 0) {
            LOG.warn("Import procedure did not run today! Mapping does not make so much sense!");
        }
        dbInstance.closeSession();
    }

}
