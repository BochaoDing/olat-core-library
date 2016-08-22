package ch.uzh.campus.mapper;

import ch.uzh.campus.data.SapOlatUserDao;
import ch.uzh.campus.data.Student;
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
    private SapOlatUserDao userMappingDaoMock;
    private UserMapper userMapperMock;
    private Student studentMock;
    private Identity identityMock;

    @Before
    public void setup() {
        userMappingDaoMock = mock(SapOlatUserDao.class);
        userMapperMock = mock(UserMapper.class);
        studentMapperTestObject = new StudentMapper(userMappingDaoMock, userMapperMock);
        studentMock = mock(Student.class);
        identityMock = mock(Identity.class);
    }

    @Test
    public void synchronizeStudentMapping_MappingAlreadyExist() {
        when(userMappingDaoMock.existsMappingForSapUserId(studentMock.getId())).thenReturn(true);

        MappingResult result = studentMapperTestObject.synchronizeStudentMapping(studentMock);

        assertEquals("Mapping should already exist", MappingResult.MAPPING_ALREADY_EXIST, result);
    }

    @Test
    public void synchronizeStudentMapping_MappingByMatriculationNumber() {
        when(userMappingDaoMock.existsMappingForSapUserId(studentMock.getId())).thenReturn(false);
        when(userMapperMock.tryToMapByMatriculationNumber(studentMock.getRegistrationNr())).thenReturn(identityMock);

        MappingResult result = studentMapperTestObject.synchronizeStudentMapping(studentMock);

        assertEquals("Mapping should be found for the matriculation number", MappingResult.NEW_MAPPING_BY_MATRICULATION_NR, result);
    }

    @Test
    public void synchronizeStudentMapping_MappingByEmail() {
        when(userMappingDaoMock.existsMappingForSapUserId(studentMock.getId())).thenReturn(false);
        when(userMapperMock.tryToMapByMatriculationNumber(studentMock.getRegistrationNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(studentMock.getEmail())).thenReturn(identityMock);

        MappingResult result = studentMapperTestObject.synchronizeStudentMapping(studentMock);

        assertEquals("Mapping should be found for the email", MappingResult.NEW_MAPPING_BY_EMAIL, result);
    }

    @Test
    public void synchronizeStudentMapping_CouldBeMappedManually() {
        when(userMappingDaoMock.existsMappingForSapUserId(studentMock.getId())).thenReturn(false);
        when(userMapperMock.tryToMapByMatriculationNumber(studentMock.getRegistrationNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(studentMock.getEmail())).thenReturn(null);
        when(userMapperMock.tryToMapByFirstNameLastName(studentMock.getFirstName(), studentMock.getLastName())).thenReturn(identityMock);

        MappingResult result = studentMapperTestObject.synchronizeStudentMapping(studentMock);

        assertEquals("Mapping should be done manually", MappingResult.COULD_BE_MAPPED_MANUALLY, result);
    }

    @Test
    public void synchronizeStudentMapping_CouldNotMap() {
        when(userMappingDaoMock.existsMappingForSapUserId(studentMock.getId())).thenReturn(false);
        when(userMapperMock.tryToMapByMatriculationNumber(studentMock.getRegistrationNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(studentMock.getEmail())).thenReturn(null);
        when(userMapperMock.tryToMapByFirstNameLastName(studentMock.getFirstName(), studentMock.getLastName())).thenReturn(null);

        MappingResult result = studentMapperTestObject.synchronizeStudentMapping(studentMock);

        assertEquals("Mapping should not be found", MappingResult.COULD_NOT_MAP, result);
    }

}
