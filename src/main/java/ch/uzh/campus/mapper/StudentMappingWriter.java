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
package ch.uzh.campus.mapper;

import ch.uzh.campus.data.Student;
import ch.uzh.campus.metric.CampusNotifier;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import java.util.List;

/**
 * {@link ItemWriter} for
 * 
 * Initial Date: 27.11.2012 <br>
 * 
 * @author aabouc
 */
public class StudentMappingWriter implements ItemWriter<Student> {

    private static final OLog LOG = Tracing.createLoggerFor(StudentMappingWriter.class);

    private MappingStatistic mappingStatistic;

    @Autowired
    DB dbInstance;

    @Autowired
    CampusNotifier campusNotifier;

    @Autowired
    StudentMapper studentMapper;

    public void setMappingStatistic(MappingStatistic mappingStatistic) {
        this.mappingStatistic = mappingStatistic;
    }

    MappingStatistic getMappingStatistic() {
        return mappingStatistic;
    }

    public void setDbInstance(DB dbInstance) {
        this.dbInstance = dbInstance;
    }

    @PreDestroy
    public void destroy() {
        LOG.info("MappingStatistic(Students)=" + mappingStatistic);
        campusNotifier.notifyUserMapperStatistic(new OverallUserMapperStatistic(null, mappingStatistic));
    }

    @Override
    public void write(List<? extends Student> students) throws Exception {
        try {
            for (Student student : students) {
                mappingStatistic.addMappingResult(studentMapper.synchronizeStudentMapping(student));
            }
            dbInstance.commitAndCloseSession();
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            LOG.warn(t.getMessage());
            throw t;
        }
    }
}