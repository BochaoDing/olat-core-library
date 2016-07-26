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

import ch.uzh.campus.data.EventCourseId;
import ch.uzh.campus.data.EventDao;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Martin Schraner
 */
@Component
public class EventWriter implements ItemWriter<EventCourseId> {

    private static final OLog LOG = Tracing.createLoggerFor(EventWriter.class);

    @Autowired
    public EventDao eventDao;

    @Autowired
    DB dbInstance;

    @Override
    public void write(List<? extends EventCourseId> eventCourseIds) throws Exception {
        try {
            for (EventCourseId eventCourseId : eventCourseIds) {
                eventDao.addEventToCourse(eventCourseId);
            }
            dbInstance.commitAndCloseSession();
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            // In the case of an exception, Spring Batch calls this method several times:
            // First for the eventCourseIds according to commit-interval in campusBatchJobContext.xml, and then (after rollbacking)
            // for each entry of the original eventCourseIds separately enabling commits containing only one entry.
            // To avoid duplicated warnings we only log a warning in the latter case.
            if (eventCourseIds.size() == 1) {
                LOG.warn(t.getMessage());
            } else {
                LOG.debug(t.getMessage());
            }
            throw t;
        }
    }
}
