package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.synchronization;

import ch.uzh.extension.campuscourse.service.synchronization.CampusCourseSynchronizer;
import ch.uzh.extension.campuscourse.model.CampusCourseTO;
import ch.uzh.extension.campuscourse.service.synchronization.statistic.OverallSynchronizeStatistic;
import ch.uzh.extension.campuscourse.service.synchronization.statistic.SynchronizedGroupStatistic;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
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
 * This is an implementation of {@link ItemWriter} that synchronizes the SAP data (courses and their participants) <br>
 * with the OLAT courses and the appropriate groups.<br>
 * It delegates the actual synchronizing process to the {@link CampusCourseSynchronizer}. <br>
 * 
 * Initial Date: 31.10.2012 <br>
 * 
 * @author aabouc
 */
@Component
@Scope("step")
public class CampusCourseSynchronizationWriter implements ItemWriter<CampusCourseTO> {

    private static final OLog LOG = Tracing.createLoggerFor(CampusCourseSynchronizationWriter.class);

    private final CampusCourseSynchronizer campusCourseSynchronizer;
    private final DB dbInstance;
    private final OverallSynchronizeStatistic overallSynchronizeStatistic;

    @Autowired
    public CampusCourseSynchronizationWriter(CampusCourseSynchronizer campusCourseSynchronizer, DB dbInstance, OverallSynchronizeStatistic synchronizeStatistic) {
        this.campusCourseSynchronizer = campusCourseSynchronizer;
        this.dbInstance = dbInstance;
		this.overallSynchronizeStatistic = synchronizeStatistic;
	}

    @PreDestroy
    public void destroy() {
        LOG.info("synchronizeAllSapCourses overallSynchronizeStatistic=" + overallSynchronizeStatistic.calculateOverallStatistic());
    }

    /**
     * Delegates the actual synchronizing of the SAP data to the OLAT data to the {@link CampusCourseSynchronizer}.<br>
     *
     */
    public void write(List<? extends CampusCourseTO> campusCourseTOs) throws Exception {
        for (CampusCourseTO campusCourseTO : campusCourseTOs) {
            try {
                SynchronizedGroupStatistic courseSynchronizeStatistic = campusCourseSynchronizer.synchronizeOlatCampusCourse(campusCourseTO);
                overallSynchronizeStatistic.add(courseSynchronizeStatistic);
                dbInstance.commitAndCloseSession();
            } catch (Throwable t) {
                dbInstance.rollbackAndCloseSession();
                // In the case of an exception, Spring Batch calls this method several times:
                // First for the sapCourses according to commit-interval in campusBatchJobContext.xml, and then (after rollbacking)
                // for each entry of the original sapCourses separately enabling commits containing only one entry.
                // To avoid duplicated warnings we only log a warning in the latter case.
                String msg = "Could not synchronize campus course '" + campusCourseTO.getTitleToBeDisplayed() + "': " + t.getMessage();
                if (campusCourseTOs.size() == 1) {
                    LOG.error(msg);
                } else {
                    LOG.debug(msg);
                }
                throw t;
            }
        }
    }

	OverallSynchronizeStatistic getOverallSynchronizeStatistic() {
		return overallSynchronizeStatistic;
	}
}
