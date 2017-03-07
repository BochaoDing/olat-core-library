package ch.uzh.extension.campuscourse.batchprocessing;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

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
 * This class serves as a generic targetObject for the quartz MethodInvokingJobDetailFactoryBean. <br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */
public class CampusBatchJobLauncher {

	private static final OLog LOG = Tracing.createLoggerFor(CampusBatchJobLauncher.class);
	private static final String PROCESS_DISABLED = "disabled";

	private final Job job;
	private final Map<String, JobParameter> jobParameters;
    private final JobLauncher jobLauncher;

	/**
	 *
	 * @param job the job needed to be run
	 * @param jobParameters the map of the JobParameters needed to run the job
	 * @param jobLauncher he JobLauncher needed to run the job
	 */
	public CampusBatchJobLauncher(Job job, Map<String, JobParameter> jobParameters, JobLauncher jobLauncher) {
		this.job = job;
		this.jobParameters = jobParameters;
		this.jobLauncher = jobLauncher;
	}

    @PostConstruct
    public void init() {
        LOG.info("JobParameters: [" + jobParameters + "]");
    }

    /**
     * Delegates the actual launching of the given job to the given jobLauncher <br>
     * only in the case that the process is in the enabled status.
     * 
     * @param status
     *            the status indicating whether the job is enabled or disabled
     * @param campusProcess
     *            the name of the process
     */
    public void process(String status, String campusProcess) throws Exception {

        LOG.info("THE " + campusProcess + " IS: [" + status + "]");

        if (PROCESS_DISABLED.equalsIgnoreCase(status)) {
            LOG.warn("Job is disabled! Check campusJobSchedulerContext.xml");
            return;
        }
        jobParameters.put("run.ts", new JobParameter(System.currentTimeMillis()));

        if (job == null) {
            LOG.warn("Job is not set! Check campusJobSchedulerContext.xml");
            return;
        }

        jobLauncher.run(job, new JobParameters(jobParameters));
    }
}