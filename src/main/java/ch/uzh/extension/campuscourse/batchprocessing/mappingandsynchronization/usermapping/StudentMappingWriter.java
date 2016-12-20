package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.usermapping;

import ch.uzh.extension.campuscourse.data.entity.Student;
import ch.uzh.extension.campuscourse.service.usermapping.StudentMapper;
import ch.uzh.extension.campuscourse.service.usermapping.UserMappingResult;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

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
public class StudentMappingWriter implements ItemWriter<Student> {

    private static final OLog LOG = Tracing.createLoggerFor(StudentMappingWriter.class);

    private final DB dbInstance;
    private final StudentMapper studentMapper;
    private final UserMappingStatistic userMappingStatistic;

    @Autowired
    public StudentMappingWriter(DB dbInstance, StudentMapper studentMapper, UserMappingStatistic userMappingStatistic) {
        this.dbInstance = dbInstance;
        this.studentMapper = studentMapper;
        this.userMappingStatistic = userMappingStatistic;
    }

    @PreDestroy
    public void destroy() {
        LOG.info("MappingStatistic(Students)=" + userMappingStatistic);
    }

    @Override
    public void write(List<? extends Student> students) throws Exception {
        try {
            for (Student student : students) {
                UserMappingResult userMappingResult = studentMapper.tryToMap(student);
                userMappingStatistic.addMappingResult(userMappingResult);
            }
            dbInstance.commitAndCloseSession();
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            // In the case of an exception, Spring Batch calls this method several times:
            // First for the sapCourses according to commit-interval in campusBatchJobContext.xml, and then (after rollbacking)
            // for each entry of the original students separately enabling commits containing only one entry.
            // To avoid duplicated warnings we only log a warning in the latter case.
            if (students.size() == 1) {
                LOG.error(t.getMessage());
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
