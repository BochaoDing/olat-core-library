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

import ch.uzh.campus.data.StudentIdCourseId;
import ch.uzh.campus.data.StudentIdCourseIdModifiedDate;

import org.springframework.batch.item.ItemProcessor;

import java.util.Date;

/**
 * @author aabouc
 * @author Martin Schraner
 */
public class StudentCourseProcessor implements ItemProcessor<StudentIdCourseId, StudentIdCourseIdModifiedDate> {

    public StudentIdCourseIdModifiedDate process(StudentIdCourseId studentIdCourseId) {
        return new StudentIdCourseIdModifiedDate(studentIdCourseId.getStudentId(), studentIdCourseId.getCourseId(), new Date());
    }

}
