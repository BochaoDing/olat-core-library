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
package ch.uzh.campus.connectors;

import ch.uzh.campus.data.ImportStatistic;
import ch.uzh.campus.data.ImportStatisticDao;
import ch.uzh.campus.metric.CampusNotifier;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
//import org.olat.lms.core.course.campus.impl.metric.CampusNotifier;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Initial Date: 08.01.2014 <br>
 * 
 * @author aabouc
 */
public class CampusJobInterceptor implements JobExecutionListener {

    @Autowired
    private CampusNotifier campusNotifier;

    @Autowired
    private ImportStatisticDao importStatisticDao;

	private static final OLog LOG = Tracing.createLoggerFor(CampusJobInterceptor.class);

    @Override
    public void afterJob(JobExecution je) {
        LOG.info("afterJob " + je.getJobInstance().getJobName());
        campusNotifier.notifyJobExecution(je);
    }

    @Override
    public void beforeJob(JobExecution je) {
        LOG.info("beforeJob " + je.getJobInstance().getJobName());
        switch (je.getJobInstance().getJobName()) {
//            case "importJob":
//                break;
            case "userMappingJob":
                // Check if "importJob" has ran today
                List<ImportStatistic> importStatsOfToday = importStatisticDao.getImportStatisticOfToday();
                if (importStatsOfToday.size() == 0) {
                    LOG.warn("Import procedure did not run today! Mapping does not make so much sense!");
                    // TODO OLATng: is there a way to stop execution of a job? should we stop it now?
                }
                break;
        }
    }

}
