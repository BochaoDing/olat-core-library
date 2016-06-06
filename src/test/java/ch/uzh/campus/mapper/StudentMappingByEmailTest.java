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

import ch.uzh.campus.data.Lecturer;
import ch.uzh.campus.data.Student;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.user.UserManager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
public class StudentMappingByEmailTest {

    private static final String EMAIL = "student@example.com";

    MappingByEmail mappingByEmailTestObject;

    UserManager userManagerMock;
    Identity identityMock;

    private Student student;

    @Before
    public void setup() {
        mappingByEmailTestObject = new MappingByEmail();
        userManagerMock = mock(UserManager.class);
        mappingByEmailTestObject.userManager = userManagerMock;

        student = mock(Student.class);
        identityMock = mock(Identity.class);
    }

    @Test
    public void tryToMap_foundNoMapping_NoEmail() {
        when(student.getEmail()).thenReturn(null);
        Identity mappedIdentity = mappingByEmailTestObject.tryToMap(student);
        assertNull("Must return null, when student has no email defined", mappedIdentity);
    }

    @Test
    public void tryToMap_foundNoMapping_EmptyEmail() {
        when(student.getEmail()).thenReturn("");
        Identity mappedIdentity = mappingByEmailTestObject.tryToMap(student);
        assertNull("Must return null, when student has empty email", mappedIdentity);
    }

    @Test
    public void tryToMap_foundNoMapping_WhitespaceEmail() {
        when(student.getEmail()).thenReturn("  ");
        Identity mappedIdentity = mappingByEmailTestObject.tryToMap(student);
        assertNull("Must return null, when student email only contains whitespace", mappedIdentity);
    }
    @Test
    public void tryToMap_foundMapping() {
        when(student.getEmail()).thenReturn(EMAIL);
        when(userManagerMock.findIdentityByEmail(EMAIL)).thenReturn(identityMock);
        Identity mappedIdentity = mappingByEmailTestObject.tryToMap(student);
        assertNotNull("Must return identity, when user manager find a student identity", mappedIdentity);
    }

}
