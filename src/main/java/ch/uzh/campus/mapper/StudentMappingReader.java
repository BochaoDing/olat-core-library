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

import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.data.Student;
import ch.uzh.campus.utils.ListUtil;

import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

public class StudentMappingReader implements ItemReader<Student> {

    @Autowired
    DaoManager daoManager;

    private List<Student> students;

    @PostConstruct
    public void init() {
        daoManager.deleteOldStudentMapping();
        students = daoManager.getAllStudents();
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
