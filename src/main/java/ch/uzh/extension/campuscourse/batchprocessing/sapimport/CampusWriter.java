package ch.uzh.extension.campuscourse.batchprocessing.sapimport;

import ch.uzh.extension.campuscourse.data.dao.CampusDao;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemWriter;

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
 * This class is a generic {@link ItemWriter} that writes data to the database. <br>
 * It delegates the actual writing (save or update) of data to the database to a <br>
 * concrete implementation of {@link CampusDao}.<br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */
public class CampusWriter<T> implements ItemWriter<T> {

    private static final OLog LOG = Tracing.createLoggerFor(CampusWriter.class);

    private final DB dbInstance;
    private final CampusDao<T> campuskursDao;

    CampusWriter(DB dbInstance, CampusDao<T> campuskursDao) {
        this.dbInstance = dbInstance;
        this.campuskursDao = campuskursDao;
    }

    /**
     * Delegates the actual saving or updating of the given list of items to the <br>
     * concrete implementation of {@link CampusDao}
     *
     * Ensure no active {@link DB} session is open i.e. bound to the
     * {@link ThreadLocal}. If this is not the case, do not try to close
     * the session here. Close it where it was opened.
     *
     * @param items
     *            the items to send
     * 
     * @see ItemWriter#write(List)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void write(List<? extends T> items) throws Exception {
        try {
            campuskursDao.saveOrUpdate((List<T>) items);
            dbInstance.commitAndCloseSession();
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            // In the case of an exception, Spring Batch calls this method several times:
            // First for the items according to commit-interval in campusBatchJobContext.xml, and then (after rollbacking)
            // for each entry of the original items separately enabling commits containing only one entry.
            // To avoid duplicated warnings we only log a warning in the latter case.
            if (items.size() == 1) {
                LOG.warn(t.getMessage());
            } else {
                LOG.debug(t.getMessage());
            }
            throw t;
        }
    }
}

