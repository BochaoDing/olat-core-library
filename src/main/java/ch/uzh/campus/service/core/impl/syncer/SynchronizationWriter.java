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

import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.connectors.CampusInterceptor;
import ch.uzh.campus.service.core.impl.syncer.statistic.OverallSynchronizeStatistic;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import java.util.List;

/**
 * This is an implementation of {@link ItemWriter} that synchronizes the SAP data (courses and their participants) <br>
 * with the OLAT courses and the appropriate groups.<br>
 * It delegates the actual synchronizing process to the {@link CourseSynchronizer}. <br>
 * 
 * Initial Date: 31.10.2012 <br>
 * 
 * @author aabouc
 */
public class SynchronizationWriter implements ItemWriter<CampusCourseImportTO> {

    private static final OLog LOG = Tracing.createLoggerFor(SynchronizationWriter.class);

    private OverallSynchronizeStatistic synchronizeStatistic;
    @Autowired
    CourseSynchronizer courseSynchronizer;

    public OverallSynchronizeStatistic getSynchronizeStatistic() {
        return synchronizeStatistic;
    }

    /**
     * Sets the OverallSynchronizeStatistic to be used for gathering the results during the synchronizing.
     * 
     * @param synchronizeStatistic
     *            the OverallSynchronizeStatistic
     */
    public void setSynchronizeStatistic(OverallSynchronizeStatistic synchronizeStatistic) {
        this.synchronizeStatistic = synchronizeStatistic;
    }

    @PreDestroy
    public void destroy() {
        LOG.info("synchronizeAllSapCourses overallSynchronizeStatistic=" + synchronizeStatistic.calculateOverallStatistic());
    }

    /**
     * Delegates the actual synchronizing of the SAP data to the OLAT data to the {@link CourseSynchronizer}.<br>
     * 
     * @param sapCourses
     *            the CampusCourseImportTO
     */
    public void write(List<? extends CampusCourseImportTO> sapCourses) throws Exception {
        for (CampusCourseImportTO sapCourse : sapCourses) {
            // TODO OLATng
//            SynchronizedGroupStatistic courseSynchronizeStatistic = courseSynchronizer.synchronizeCourse(sapCourse);
//            synchronizeStatistic.add(courseSynchronizeStatistic);
        }
    }
}
