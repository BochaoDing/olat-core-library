package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.usermapping;

import ch.uzh.extension.campuscourse.data.entity.Student;
import ch.uzh.extension.campuscourse.service.usermapping.UserMappingResult;
import ch.uzh.extension.campuscourse.service.usermapping.StudentMapper;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
 *
 * Initial Date: 27.11.2012 <br>
 * 
 * @author aabouc
 */
public class StudentMappingWriterTest {

    private StudentMappingWriter studentMappingWriterTestObject;
    private List<Student> twoStudentsList = new ArrayList<>();

    @Before
    public void setup() {
        // Mock for StudentMapper
        StudentMapper studentMapperMock = mock(StudentMapper.class);

        // Mock for Student
        Student studentMock1 = mock(Student.class);
        Student studentMock2 = mock(Student.class);
        twoStudentsList.add(studentMock1);
        twoStudentsList.add(studentMock2);

        when(studentMapperMock.tryToMap(studentMock1)).thenReturn(UserMappingResult.NEW_MAPPING_BY_EMAIL);
        when(studentMapperMock.tryToMap(studentMock2)).thenReturn(UserMappingResult.NEW_MAPPING_BY_MATRICULATION_NR);

        // Mock for DBImpl
        DB dBImplMock = mock(DB.class);

        studentMappingWriterTestObject = new StudentMappingWriter(dBImplMock, studentMapperMock, new UserMappingStatistic());
    }

    @Test
    public void write_emptyStudentsList() throws Exception {
        studentMappingWriterTestObject.write(new ArrayList<>());
        assertEquals(
                studentMappingWriterTestObject.getUserMappingStatistic().toString(),
                "MappedByEmail=0 , MappedByMatriculationNumber=0 , MappedByPersonalNumber=0 , MappedByAdditionalPersonalNumber=0 , couldNotMappedBecauseNotRegistered=0 , couldBeMappedManually=0");
    }

    @Test
    public void write_twoStudentsList() throws Exception {
        studentMappingWriterTestObject.write(twoStudentsList);
        assertEquals(
                studentMappingWriterTestObject.getUserMappingStatistic().toString(),
                "MappedByEmail=1 , MappedByMatriculationNumber=1 , MappedByPersonalNumber=0 , MappedByAdditionalPersonalNumber=0 , couldNotMappedBecauseNotRegistered=0 , couldBeMappedManually=0");
    }

}
