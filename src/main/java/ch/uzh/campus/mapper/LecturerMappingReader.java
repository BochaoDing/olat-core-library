package ch.uzh.campus.mapper;

import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.data.Lecturer;
import ch.uzh.campus.utils.ListUtil;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
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
 * {@link ItemWriter} for
 * 
 * Initial Date: 27.11.2012 <br>
 * 
 * @author aabouc
 */
public class LecturerMappingReader implements ItemReader<Lecturer> {

    private static final OLog LOG = Tracing.createLoggerFor(LecturerMappingReader.class);

    private DaoManager daoManager;
    private DB dbInstance;
    private List<Lecturer> lecturers;

    @Autowired
    public LecturerMappingReader(DaoManager daoManager, DB dbInstance) {
        this.daoManager = daoManager;
        this.dbInstance = dbInstance;
    }

    @PostConstruct
    public void init() {
        try {
            daoManager.deleteMappingOfLecturersNotFoundInLecturerTable();
            lecturers = daoManager.getAllLecturers();
            dbInstance.commitAndCloseSession();
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            LOG.warn(t.getMessage());
            throw t;
        }
    }

    @PreDestroy
    public void destroy() {
        if (ListUtil.isNotBlank(lecturers)) {
            lecturers.clear();
        }
    }

    @Override
    public synchronized Lecturer read() throws Exception {
        if (ListUtil.isNotBlank(lecturers)) {
            return lecturers.remove(0);
        }
        return null;
    }

}
