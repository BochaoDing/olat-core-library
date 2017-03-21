package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.usermapping;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import ch.uzh.extension.campuscourse.data.dao.BatchJobAndUserMappingStatisticDao;
import ch.uzh.extension.campuscourse.data.entity.Lecturer;
import ch.uzh.extension.campuscourse.data.entity.BatchJobAndUserMappingStatistic;
import ch.uzh.extension.campuscourse.service.usermapping.LecturerMapper;
import ch.uzh.extension.campuscourse.service.usermapping.UserMappingResult;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
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
 * Initial Date: 27.11.2012 <br>
 * 
 * @author aabouc
 */
@Component
@Scope("step")
public class LecturerMappingWriter implements ItemWriter<Lecturer> {

    private static final OLog LOG = Tracing.createLoggerFor(LecturerMappingWriter.class);

    private final DB dbInstance;
    private final LecturerMapper lecturerMapper;
    private final BatchJobAndUserMappingStatisticDao batchJobAndUserMappingStatisticDao;
	private final UserMappingStatistic userMappingStatistic = new UserMappingStatistic();

    @Autowired
    public LecturerMappingWriter(DB dbInstance, LecturerMapper lecturerMapper, BatchJobAndUserMappingStatisticDao batchJobAndUserMappingStatisticDao) {
        this.dbInstance = dbInstance;
        this.lecturerMapper = lecturerMapper;
        this.batchJobAndUserMappingStatisticDao = batchJobAndUserMappingStatisticDao;
    }

    @PreDestroy
    public void destroy() {
        LOG.info("Lecturer mapping statistic: " + userMappingStatistic);
        // Update batch job and user mapping statistic database entry
		BatchJobAndUserMappingStatistic batchJobAndUserMappingStatistic = batchJobAndUserMappingStatisticDao.getLastCreatedUserMappingStatisticForCampusBatchStepName(CampusBatchStepName.LECTURER_MAPPING);
		batchJobAndUserMappingStatistic.setUserMappingStatistic(userMappingStatistic);
		dbInstance.commitAndCloseSession();
    }

    @Override
    public void write(List<? extends Lecturer> lecturers) throws Exception {
        try {
            List<UserMappingResult> userMappingResults = new ArrayList<>();
            for (Lecturer lecturer : lecturers) {
                UserMappingResult userMappingResult = lecturerMapper.tryToMap(lecturer);
                userMappingResults.add(userMappingResult);
            }
            dbInstance.commitAndCloseSession();
            // Update user mapping statistic AFTER commit
			userMappingStatistic.addUserMappingResults(userMappingResults);
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            // In the case of an exception, Spring Batch calls this method several times:
            // First for the sapCourses according to commit-interval in campusBatchJobContext.xml, and then (after rollbacking)
            // for each entry of the original lecturers separately enabling commits containing only one entry.
            // To avoid duplicated warnings we only log a warning in the latter case.
            if (lecturers.size() == 1) {
                String msg = "Error when trying to map lecturer with personal number " + lecturers.get(0).getPersonalNr() + ": " + t.getMessage();
                LOG.error(msg);
            } else {
				LOG.debug(t.getMessage());
            }
            throw t;
        }
    }

    UserMappingStatistic getUserMappingStatistic() {
        return userMappingStatistic;
    }
}
