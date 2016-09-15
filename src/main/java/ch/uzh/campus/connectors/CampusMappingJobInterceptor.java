package ch.uzh.campus.connectors;

import ch.uzh.campus.data.ImportStatistic;
import ch.uzh.campus.data.ImportStatisticDao;
import ch.uzh.campus.metric.CampusNotifier;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

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
public class CampusMappingJobInterceptor implements JobExecutionListener {

    private static final OLog LOG = Tracing.createLoggerFor(CampusMappingJobInterceptor.class);

    private final CampusNotifier campusNotifier;
    private final DB dbInstance;
    private final ImportStatisticDao importStatisticDao;

    @Autowired
    public CampusMappingJobInterceptor(DB dbInstance, ImportStatisticDao importStatisticDao, CampusNotifier campusNotifier) {
        this.dbInstance = dbInstance;
        this.importStatisticDao = importStatisticDao;
        this.campusNotifier = campusNotifier;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LOG.info("afterJob " + jobExecution.getJobInstance().getJobName());
        campusNotifier.notifyJobExecution(jobExecution);
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        LOG.info("beforeJob " + jobExecution.getJobInstance().getJobName());
        // Check if "importJob" has ran today
        List<ImportStatistic> importStatsOfToday = importStatisticDao.getImportStatisticOfToday();
        if (importStatsOfToday.size() == 0) {
            LOG.warn("Import procedure did not run today! Mapping does not make so much sense!");
        }
        dbInstance.closeSession();
    }

}
