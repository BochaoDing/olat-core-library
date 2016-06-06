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

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
//import org.olat.lms.core.course.campus.impl.metric.CampusNotifier;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Initial Date: 08.01.2014 <br>
 * 
 * @author aabouc
 */
public class CampusJobInterceptor implements JobExecutionListener {
	
	private static final OLog LOG = Tracing.createLoggerFor(CampusJobInterceptor.class);

	//TODO: olatng
    //@Autowired
    //private CampusNotifier campusNotifier;

    @Override
    public void afterJob(JobExecution je) {
    	//TODO: olatng
        //campusNotifier.notifyJobExecution(je);
    	LOG.info("afterJob - notifyJobExecution");
    }

    @Override
    public void beforeJob(JobExecution je) {
    	LOG.info("beforeJob - notifyJobExecution");
        switch (je.getJobInstance().getJobName()) {
            case "importJob":
                Map<String, JobParameter> parameters = je.getJobInstance().getJobParameters().getParameters();
                for (String parameterKey : parameters.keySet()) {
                    if (!parameterKey.equals("run.ts")) {
                        String filePath = parameters.get(parameterKey).getValue().toString();
                        LOG.info("Check if CSV file exists: " + filePath);
                        if (!Files.exists(Paths.get(URI.create(filePath)))) {
                            LOG.error("CSV file for parameter '" + parameterKey + "' does not exist: " + filePath);
                        }
                    }
                }
                break;
            case "userMappingJob":
                // TODO OLATng
                // ... check if "importJob" has ran today
                break;
        }
    }

}
