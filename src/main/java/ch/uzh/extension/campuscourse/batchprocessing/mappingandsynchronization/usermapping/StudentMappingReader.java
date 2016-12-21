package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.usermapping;

import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import ch.uzh.extension.campuscourse.data.entity.Student;
import ch.uzh.extension.campuscourse.util.ListUtil;

import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
 */
@Component
@Scope("step")
public class StudentMappingReader implements ItemReader<Student> {

    private static final OLog LOG = Tracing.createLoggerFor(StudentMappingReader.class);

    private DaoManager daoManager;
    private DB dbInstance;
    private List<Student> students;

    @Autowired
    public StudentMappingReader(DaoManager daoManager, DB dbInstance) {
        this.daoManager = daoManager;
        this.dbInstance = dbInstance;
    }

    @PostConstruct
    public void init() {
        try {
            students = daoManager.getAllStudents();
            dbInstance.commitAndCloseSession();
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            LOG.warn(t.getMessage());
            throw t;
        }
    }

    @PreDestroy
    public void destroy() {
        if (ListUtil.isNotBlank(students)) {
            students.clear();
        }
    }

    public synchronized Student read() throws Exception {
        if (ListUtil.isNotBlank(students)) {
            return students.remove(0);
        }
        return null;
    }

}
