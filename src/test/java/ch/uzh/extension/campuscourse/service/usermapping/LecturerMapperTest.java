package ch.uzh.extension.campuscourse.service.usermapping;

import ch.uzh.extension.campuscourse.data.entity.Lecturer;
import ch.uzh.extension.campuscourse.data.dao.LecturerDao;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
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
    private UserMapper userMapperMock;
    private Lecturer lecturerMock;
    private Identity identityMock;

    @Before
    public void setup() {
        LecturerDao lecturerDaoMock = mock(LecturerDao.class);
        userMapperMock = mock(UserMapper.class);
        BaseSecurity baseSecurityMock = mock(BaseSecurity.class);
        lecturerMapperTestObject = new LecturerMapper(lecturerDaoMock, userMapperMock, baseSecurityMock);
        lecturerMock = mock(Lecturer.class);
        when(lecturerMock.getPersonalNr()).thenReturn(1L);
        when(lecturerMock.getAdditionalPersonalNrs()).thenReturn("1");
        when(baseSecurityMock.isIdentityInSecurityGroup(any(), any())).thenReturn(false);
        identityMock = mock(Identity.class);
    }

    @Test
    public void testTryToMap_AlreadyMapped() {
        when(lecturerMock.getMappedIdentity()).thenReturn(identityMock);

        UserMappingResult result = lecturerMapperTestObject.tryToMap(lecturerMock);

        assertEquals("Mapping should already exist", UserMappingResult.ALREADY_MAPPED, result);
    }

    @Test
    public void testTryToMap_MappingByPersonalNumber() {
        when(lecturerMock.getMappedIdentity()).thenReturn(null);
        when(userMapperMock.tryToMapByPersonalNumber(lecturerMock.getPersonalNr())).thenReturn(identityMock);

        UserMappingResult result = lecturerMapperTestObject.tryToMap(lecturerMock);

        assertEquals("Mapping should be found for the personal number", UserMappingResult.NEW_MAPPING_BY_PERSONAL_NR, result);
    }

    @Test
    public void testTryToMap_MappingByEmail() {
        when(lecturerMock.getMappedIdentity()).thenReturn(null);
        when(userMapperMock.tryToMapByPersonalNumber(lecturerMock.getPersonalNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(lecturerMock.getEmail())).thenReturn(identityMock);

        UserMappingResult result = lecturerMapperTestObject.tryToMap(lecturerMock);

        assertEquals("Mapping should be found for the email", UserMappingResult.NEW_MAPPING_BY_EMAIL, result);
    }

    @Test
    public void testTryToMap_CouldBeAdditionalPersonalNumber() {
        when(lecturerMock.getMappedIdentity()).thenReturn(null);
        when(userMapperMock.tryToMapByPersonalNumber(lecturerMock.getPersonalNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(lecturerMock.getEmail())).thenReturn(null);
        when(userMapperMock.tryToMapByAdditionalPersonalNumber(anyString())).thenReturn(identityMock);

        UserMappingResult result = lecturerMapperTestObject.tryToMap(lecturerMock);

        assertEquals("Mapping should be done manually", UserMappingResult.NEW_MAPPING_BY_ADDITIONAL_PERSONAL_NR, result);
    }

    @Test
    public void testTryToMap_CouldBeMappedManually() {
        when(lecturerMock.getMappedIdentity()).thenReturn(null);
        when(userMapperMock.tryToMapByPersonalNumber(lecturerMock.getPersonalNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(lecturerMock.getEmail())).thenReturn(null);
        when(userMapperMock.tryToMapByAdditionalPersonalNumber(anyString())).thenReturn(null);
        when(userMapperMock.tryToMapByFirstNameLastName(lecturerMock.getFirstName(), lecturerMock.getLastName())).thenReturn(identityMock);

        UserMappingResult result = lecturerMapperTestObject.tryToMap(lecturerMock);

        assertEquals("Mapping should be done manually", UserMappingResult.COULD_BE_MAPPED_MANUALLY, result);
    }

    @Test
    public void testTryToMap_CouldNotMap() {
        when(lecturerMock.getMappedIdentity()).thenReturn(null);
        when(userMapperMock.tryToMapByPersonalNumber(lecturerMock.getPersonalNr())).thenReturn(null);
        when(userMapperMock.tryToMapByEmail(lecturerMock.getEmail())).thenReturn(null);
        when(userMapperMock.tryToMapByAdditionalPersonalNumber(anyString())).thenReturn(null);
        when(userMapperMock.tryToMapByFirstNameLastName(lecturerMock.getFirstName(), lecturerMock.getLastName())).thenReturn(null);

        UserMappingResult result = lecturerMapperTestObject.tryToMap(lecturerMock);

        assertEquals("Mapping should not be found", UserMappingResult.COULD_NOT_MAP, result);
    }

}
