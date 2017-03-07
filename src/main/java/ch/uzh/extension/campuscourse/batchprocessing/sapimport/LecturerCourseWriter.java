package ch.uzh.extension.campuscourse.batchprocessing.sapimport;

import ch.uzh.extension.campuscourse.data.dao.LecturerCourseDao;
import ch.uzh.extension.campuscourse.model.LecturerIdCourseIdDateOfLatestImport;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
 * @author Martin Schraner
 */
@Component
@Scope("step")
public class LecturerCourseWriter implements ItemWriter<LecturerIdCourseIdDateOfLatestImport> {

    private static final OLog LOG = Tracing.createLoggerFor(LecturerCourseWriter.class);

    private final LecturerCourseDao lecturerCourseDao;
    private final DB dbInstance;

    @Autowired
    public LecturerCourseWriter(LecturerCourseDao lecturerCourseDao, DB dbInstance) {
        this.lecturerCourseDao = lecturerCourseDao;
        this.dbInstance = dbInstance;
    }

    @Override
    public void write(List<? extends LecturerIdCourseIdDateOfLatestImport> lecturerIdCourseIdDateOfLatestImportList) throws Exception {
        try {
            for (LecturerIdCourseIdDateOfLatestImport lecturerIdCourseIdDateOfLatestImport : lecturerIdCourseIdDateOfLatestImportList) {
                lecturerCourseDao.saveOrUpdateWithoutBidirectionalUpdate(lecturerIdCourseIdDateOfLatestImport);
            }
            dbInstance.commitAndCloseSession();
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            // In the case of an exception, Spring Batch calls this method several times:
            // First for the lecturerIdCourseIdDateOfImportList according to commit-interval in campusBatchJobContext.xml, and then (after rollbacking)
            // for each entry of the original lecturerIdCourseIdDateOfImportList separately enabling commits containing only one entry.
            // To avoid duplicated warnings we only log a warning in the latter case.
            if (lecturerIdCourseIdDateOfLatestImportList.size() == 1) {
                LOG.warn(t.getMessage());
            } else {
                LOG.debug(t.getMessage());
            }
            throw t;
        }
    }
}