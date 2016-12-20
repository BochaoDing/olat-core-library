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
package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.usermapping;

import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import ch.uzh.extension.campuscourse.data.entity.Lecturer;
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
public class LecturerMappingReaderTest {
    private LecturerMappingReader lecturerMappingReaderTestObject;
    private DaoManager daoManagerMock;

    @Before
    public void setup() {
        // Mocks for DaoManager and DB
        daoManagerMock = mock(DaoManager.class);
        DB dbMock = mock(DB.class);
        lecturerMappingReaderTestObject = new LecturerMappingReader(daoManagerMock, dbMock);
    }

    @Test
    public void destroy_nullLecturersList() {
        when(daoManagerMock.getAllLecturers()).thenReturn(null);
        lecturerMappingReaderTestObject.init();
        lecturerMappingReaderTestObject.destroy();
    }

    @Test
    public void destroy_emptyLecturersList() {
        when(daoManagerMock.getAllLecturers()).thenReturn(new ArrayList<>());
        lecturerMappingReaderTestObject.init();
        lecturerMappingReaderTestObject.destroy();
    }

    @Test
    public void read_nullLecturersList() throws Exception {
        when(daoManagerMock.getAllLecturers()).thenReturn(null);
        lecturerMappingReaderTestObject.init();
        assertNull(lecturerMappingReaderTestObject.read());
    }

    @Test
    public void read_emptyLecturersList() throws Exception {
        when(daoManagerMock.getAllLecturers()).thenReturn(new ArrayList<>());
        lecturerMappingReaderTestObject.init();
        assertNull(lecturerMappingReaderTestObject.read());
    }

    @Test
    public void read_twoLecturersList() throws Exception {
        List<Lecturer> twoLecturersList = new ArrayList<>();
        Lecturer lecturerMock1 = mock(Lecturer.class);
        Lecturer lecturerMock2 = mock(Lecturer.class);
        twoLecturersList.add(lecturerMock1);
        twoLecturersList.add(lecturerMock2);
        when(daoManagerMock.getAllLecturers()).thenReturn(twoLecturersList);
        lecturerMappingReaderTestObject.init();
        // The first read delivers the first lecturer
        assertNotNull(lecturerMappingReaderTestObject.read());
        // The second read delivers the second lecturer
        assertNotNull(lecturerMappingReaderTestObject.read());
        // The third read delivers null
        assertNull(lecturerMappingReaderTestObject.read());
    }

}
