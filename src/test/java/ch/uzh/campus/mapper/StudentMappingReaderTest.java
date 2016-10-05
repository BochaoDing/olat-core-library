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
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Initial Date: 27.11.2012 <br>
 * 
 * @author aabouc
 */
public class StudentMappingReaderTest {
    private StudentMappingReader studentMappingReaderTestObject;
    private DaoManager daoManagerMock;

    @Before
    public void setup() {
        // Mocks for DaoManager and DB
        daoManagerMock = mock(DaoManager.class);
        DB dbMock = mock(DB.class);
        studentMappingReaderTestObject = new StudentMappingReader(daoManagerMock, dbMock);
    }

    @Test
    public void destroy_nullStudentsList() {
        when(daoManagerMock.getAllStudents()).thenReturn(null);
        studentMappingReaderTestObject.init();
        studentMappingReaderTestObject.destroy();
    }

    @Test
    public void destroy_emptyStudentsList() {
        when(daoManagerMock.getAllStudents()).thenReturn(new ArrayList<>());
        studentMappingReaderTestObject.init();
        studentMappingReaderTestObject.destroy();
    }

    @Test
    public void read_nullStudentsList() throws Exception {
        when(daoManagerMock.getAllStudents()).thenReturn(null);
        studentMappingReaderTestObject.init();
        assertNull(studentMappingReaderTestObject.read());
    }

    @Test
    public void read_emptyStudentsList() throws Exception {
        when(daoManagerMock.getAllStudents()).thenReturn(new ArrayList<>());
        studentMappingReaderTestObject.init();
        assertNull(studentMappingReaderTestObject.read());
    }

    @Test
    public void read_twoStudentsList() throws Exception {
        List<Student> twoStudentsList = new ArrayList<>();
        Student studentMock1 = mock(Student.class);
        Student studentMock2 = mock(Student.class);
        twoStudentsList.add(studentMock1);
        twoStudentsList.add(studentMock2);
        when(daoManagerMock.getAllStudents()).thenReturn(twoStudentsList);
        studentMappingReaderTestObject.init();
        // The first read delivers the first student
        assertNotNull(studentMappingReaderTestObject.read());
        // The second read delivers the second student
        assertNotNull(studentMappingReaderTestObject.read());
        // The third read delivers null
        assertNull(studentMappingReaderTestObject.read());
    }

}
