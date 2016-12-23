package ch.uzh.campus.connectors;

import ch.uzh.campus.data.Student;
import ch.uzh.campus.data.StudentDao;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
@Scope("step")
@Component
public class StudentWriter implements ItemWriter<Student> {

    private static final OLog LOG = Tracing.createLoggerFor(StudentWriter.class);

    private final StudentDao studentDao;
    private final DB dbInstance;

    private Set<Long> successfullyProcessedIds = new HashSet<>();

    @Autowired
    public StudentWriter(StudentDao studentDao, DB dbInstance) {
        this.studentDao = studentDao;
        this.dbInstance = dbInstance;
    }

    @Override
    public void write(List<? extends Student> students) throws Exception {
        try {
            for (Student student : students) {
                // Avoid duplicates
                if (!successfullyProcessedIds.contains(student.getId())) {
                    successfullyProcessedIds.add(student.getId());
                    studentDao.saveOrUpdate(student);
                } else {
                    LOG.debug("This is a duplicate of student with id " + student.getId());
                }
            }
            dbInstance.commitAndCloseSession();
        } catch (Throwable t) {
            dbInstance.rollbackAndCloseSession();
            for (Student student : students) {
                successfullyProcessedIds.remove(student.getId());
            }
            // In the case of an exception, Spring Batch calls this method several times:
            // First for the student according to commit-interval in campusBatchJobContext.xml, and then (after rollbacking)
            // for each entry of the original students separately enabling commits containing only one entry.
            // To avoid duplicated warnings we only log a warning in the latter case.
            if (students.size() == 1) {
                LOG.warn(t.getMessage());
            } else {
                LOG.debug(t.getMessage());
            }
            throw t;
        }
    }
}
