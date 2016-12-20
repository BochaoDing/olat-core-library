package ch.uzh.extension.campuscourse.service.usermapping;

import ch.uzh.extension.campuscourse.data.entity.Student;
import ch.uzh.extension.campuscourse.data.dao.StudentDao;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.Identity;

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
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 * @author Martin Schraner
 */
public class StudentMapperTest {

    private StudentMapper studentMapperTestObject;
    private UserMapper userMapperMock;
    private Student studentMock;
    private Identity identityMock;

    @Before
    public void setup() {
        StudentDao studentDaoMock =  mock(StudentDao.class);
        userMapperMock = mock(UserMapper.class);
        studentMapperTestObject = new StudentMapper(studentDaoMock, userMapperMock);
        studentMock = mock(Student.class);
        identityMock = mock(Identity.class);
    }

    @Test
    public void testTryToMap_AlreadyMapped() {
        when(studentMock.getMappedIdentity()).thenReturn(identityMock);

        UserMappingResult result = studentMapperTestObject.tryToMap(studentMock);

        assertEquals("Mapping should already exist", UserMappingResult.ALREADY_MAPPED, result);
    }

    @Test
    public void testTryToMap_MappingByMatriculationNumber() {
        when(studentMock.getMappedIdentity()).thenReturn(null);
        when(userMapperMock.tryToMapByMatriculationNumber(studentMock.getRegistrationNr())).thenReturn(identityMock);

        UserMappingResult result = studentMapperTestObject.tryToMap(studentMock);

        assertEquals("Mapping should be found for the matriculation number", UserMappingResult.NEW_MAPPING_BY_MATRICULATION_NR, result);
    }

    @Test
    public void testTryToMap_MappingByEmail() {
        when(studentMock.getMappedIdentity()).thenReturn(null);
        when(userMapperMock.tryToMapByMatriculationNumber(studentMock.getRegistrationNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(studentMock.getEmail())).thenReturn(identityMock);

        UserMappingResult result = studentMapperTestObject.tryToMap(studentMock);

        assertEquals("Mapping should be found for the email", UserMappingResult.NEW_MAPPING_BY_EMAIL, result);
    }

    @Test
    public void testTryToMap_CouldBeMappedManually() {
        when(studentMock.getMappedIdentity()).thenReturn(null);
        when(userMapperMock.tryToMapByMatriculationNumber(studentMock.getRegistrationNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(studentMock.getEmail())).thenReturn(null);
        when(userMapperMock.tryToMapByFirstNameLastName(studentMock.getFirstName(), studentMock.getLastName())).thenReturn(identityMock);

        UserMappingResult result = studentMapperTestObject.tryToMap(studentMock);

        assertEquals("Mapping should be done manually", UserMappingResult.COULD_BE_MAPPED_MANUALLY, result);
    }

    @Test
    public void testTryToMap_CouldNotMap() {
        when(studentMock.getMappedIdentity()).thenReturn(null);
        when(userMapperMock.tryToMapByMatriculationNumber(studentMock.getRegistrationNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(studentMock.getEmail())).thenReturn(null);
        when(userMapperMock.tryToMapByFirstNameLastName(studentMock.getFirstName(), studentMock.getLastName())).thenReturn(null);

        UserMappingResult result = studentMapperTestObject.tryToMap(studentMock);

        assertEquals("Mapping should not be found", UserMappingResult.COULD_NOT_MAP, result);
    }

}
