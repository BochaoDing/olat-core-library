package ch.uzh.extension.campuscourse.batchprocessing.sapimport;

import ch.uzh.extension.campuscourse.model.EventCourseId;
import ch.uzh.extension.campuscourse.data.dao.EventDao;
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
 * @author Martin Schraner
 */
@Component
@Scope("step")
public class EventWriter implements ItemWriter<EventCourseId> {

    private static final OLog LOG = Tracing.createLoggerFor(EventWriter.class);

    private final EventDao eventDao;
    private final DB dbInstance;

    private Long courseIdOfLatestWarning = null;

    @Autowired
    public EventWriter(EventDao eventDao, DB dbInstance) {
        this.eventDao = eventDao;
        this.dbInstance = dbInstance;
    }

    @Override
    public void write(List<? extends EventCourseId> eventCourseIds) throws Exception {
        try {
            for (EventCourseId eventCourseId : eventCourseIds) {
                eventDao.saveOrUpdate(eventCourseId);
            }
            dbInstance.commitAndCloseSession();
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            // In the case of an exception, Spring Batch calls this method several times:
            // First for the eventCourseIds according to commit-interval in campusBatchJobContext.xml, and then (after rollbacking)
            // for each entry of the original eventCourseIds separately enabling commits containing only one entry.
            // To avoid duplicated warnings we only log a warning in the latter case.
            // Furthermore, if the same course has several associated events, the warning should only logged once.
            if (eventCourseIds.size() == 1 && eventCourseIds.get(0).getCourseId() != null && !eventCourseIds.get(0).getCourseId().equals(courseIdOfLatestWarning)) {
                LOG.warn(t.getMessage());
                courseIdOfLatestWarning = eventCourseIds.get(0).getCourseId();
            } else {
                LOG.debug(t.getMessage());
            }
            throw t;
        }
    }
}
