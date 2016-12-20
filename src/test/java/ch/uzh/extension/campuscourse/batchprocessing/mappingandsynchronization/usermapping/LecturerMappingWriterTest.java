package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.usermapping;

import ch.uzh.extension.campuscourse.data.entity.Lecturer;
import ch.uzh.extension.campuscourse.service.usermapping.LecturerMapper;
import ch.uzh.extension.campuscourse.service.usermapping.UserMappingResult;
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
 * <p>
 *
 * Initial Date: 27.11.2012 <br>
 * 
 * @author aabouc
 */
public class LecturerMappingWriterTest {
    private LecturerMappingWriter lecturerMappingWriterTestObject;
    private List<Lecturer> twoLecturersList = new ArrayList<>();

    @Before
    public void setup() {
        // Mock for LecturerMapper
        LecturerMapper lecturerMapperMock = mock(LecturerMapper.class);

        // Mock for Lecturer
        Lecturer lecturerMock1 = mock(Lecturer.class);
        Lecturer lecturerMock2 = mock(Lecturer.class);
        twoLecturersList.add(lecturerMock1);
        twoLecturersList.add(lecturerMock2);

        when(lecturerMapperMock.tryToMap(lecturerMock1)).thenReturn(UserMappingResult.NEW_MAPPING_BY_EMAIL);
        when(lecturerMapperMock.tryToMap(lecturerMock2)).thenReturn(UserMappingResult.NEW_MAPPING_BY_PERSONAL_NR);

        // Mock for DBImpl
        DB dBImplMock = mock(DB.class);

        lecturerMappingWriterTestObject = new LecturerMappingWriter(dBImplMock, lecturerMapperMock, new UserMappingStatistic());
    }

    @Test
    public void write_emptyLecturersList() throws Exception {
        lecturerMappingWriterTestObject.write(new ArrayList<>());
        assertEquals(
                lecturerMappingWriterTestObject.getUserMappingStatistic().toString(),
                "MappedByEmail=0 , MappedByMatriculationNumber=0 , MappedByPersonalNumber=0 , MappedByAdditionalPersonalNumber=0 , couldNotMappedBecauseNotRegistered=0 , couldBeMappedManually=0");
    }

    @Test
    public void write_twoLecturersList() throws Exception {
        lecturerMappingWriterTestObject.write(twoLecturersList);
        assertEquals(
                lecturerMappingWriterTestObject.getUserMappingStatistic().toString(),
                "MappedByEmail=1 , MappedByMatriculationNumber=0 , MappedByPersonalNumber=1 , MappedByAdditionalPersonalNumber=0 , couldNotMappedBecauseNotRegistered=0 , couldBeMappedManually=0");
    }

}
