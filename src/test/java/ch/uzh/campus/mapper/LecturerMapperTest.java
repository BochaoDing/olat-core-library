package ch.uzh.campus.mapper;

import ch.uzh.campus.data.Lecturer;
import ch.uzh.campus.data.SapOlatUserDao;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.Identity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
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
public class LecturerMapperTest {

    private LecturerMapper lecturerMapperTestObject;
    private SapOlatUserDao userMappingDaoMock;
    private UserMapper userMapperMock;
    private Lecturer lecturerMock;
    private Identity identityMock;

    @Before
    public void setup() {
        userMappingDaoMock = mock(SapOlatUserDao.class);
        userMapperMock = mock(UserMapper.class);
        lecturerMapperTestObject = new LecturerMapper(userMappingDaoMock, userMapperMock);
        lecturerMock = mock(Lecturer.class);
        when(lecturerMock.getPersonalNr()).thenReturn(1L);
        when(lecturerMock.getAdditionalPersonalNrs()).thenReturn("1");
        identityMock = mock(Identity.class);
    }

    @Test
    public void synchronizeStudentMapping_MappingAlreadyExist() {
        when(userMappingDaoMock.existsMappingForSapUserId(lecturerMock.getPersonalNr())).thenReturn(true);

        MappingResult result = lecturerMapperTestObject.synchronizeLecturerMapping(lecturerMock);

        assertEquals("Mapping should already exist", MappingResult.MAPPING_ALREADY_EXIST, result);
    }

    @Test
    public void synchronizeLecturerMapping_MappingByPersonalNumber() {
        when(userMappingDaoMock.existsMappingForSapUserId(lecturerMock.getPersonalNr())).thenReturn(false);
        when(userMapperMock.tryToMapByPersonalNumber(lecturerMock.getPersonalNr())).thenReturn(identityMock);

        MappingResult result = lecturerMapperTestObject.synchronizeLecturerMapping(lecturerMock);

        assertEquals("Mapping should be found for the personal number", MappingResult.NEW_MAPPING_BY_PERSONAL_NR, result);
    }

    @Test
    public void synchronizeLecturerMapping_MappingByEmail() {
        when(userMappingDaoMock.existsMappingForSapUserId(lecturerMock.getPersonalNr())).thenReturn(false);
        when(userMapperMock.tryToMapByPersonalNumber(lecturerMock.getPersonalNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(lecturerMock.getEmail())).thenReturn(identityMock);

        MappingResult result = lecturerMapperTestObject.synchronizeLecturerMapping(lecturerMock);

        assertEquals("Mapping should be found for the email", MappingResult.NEW_MAPPING_BY_EMAIL, result);
    }

    @Test
    public void synchronizeLecturerMapping_CouldBeAdditionalPersonalNumber() {
        when(userMappingDaoMock.existsMappingForSapUserId(lecturerMock.getPersonalNr())).thenReturn(false);
        when(userMapperMock.tryToMapByPersonalNumber(lecturerMock.getPersonalNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(lecturerMock.getEmail())).thenReturn(null);
        when(userMapperMock.tryToMapByAdditionalPersonalNumber(anyString())).thenReturn(identityMock);

        MappingResult result = lecturerMapperTestObject.synchronizeLecturerMapping(lecturerMock);

        assertEquals("Mapping should be done manually", MappingResult.NEW_MAPPING_BY_ADDITIONAL_PERSONAL_NR, result);
    }

    @Test
    public void synchronizeLecturerMapping_CouldBeMappedManually() {
        when(userMappingDaoMock.existsMappingForSapUserId(lecturerMock.getPersonalNr())).thenReturn(false);
        when(userMapperMock.tryToMapByPersonalNumber(lecturerMock.getPersonalNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(lecturerMock.getEmail())).thenReturn(null);
        when(userMapperMock.tryToMapByAdditionalPersonalNumber(anyString())).thenReturn(null);
        when(userMapperMock.tryToMapByFirstNameLastName(lecturerMock.getFirstName(), lecturerMock.getLastName())).thenReturn(identityMock);

        MappingResult result = lecturerMapperTestObject.synchronizeLecturerMapping(lecturerMock);

        assertEquals("Mapping should be done manually", MappingResult.COULD_BE_MAPPED_MANUALLY, result);
    }

    @Test
    public void synchronizeLecturerMapping_CouldNotMap() {
        when(userMappingDaoMock.existsMappingForSapUserId(lecturerMock.getPersonalNr())).thenReturn(false);
        when(userMapperMock.tryToMapByPersonalNumber(lecturerMock.getPersonalNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(lecturerMock.getEmail())).thenReturn(null);
        when(userMapperMock.tryToMapByAdditionalPersonalNumber(anyString())).thenReturn(null);
        when(userMapperMock.tryToMapByFirstNameLastName(lecturerMock.getFirstName(), lecturerMock.getLastName())).thenReturn(null);

        MappingResult result = lecturerMapperTestObject.synchronizeLecturerMapping(lecturerMock);

        assertEquals("Mapping should not be found", MappingResult.COULD_NOT_MAP, result);
    }

}
