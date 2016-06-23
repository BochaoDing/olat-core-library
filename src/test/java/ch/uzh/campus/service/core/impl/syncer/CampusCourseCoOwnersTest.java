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
package ch.uzh.campus.service.core.impl.syncer;

import ch.uzh.campus.CampusCourseConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Initial Date: 12.07.2012 <br>
 * 
 * @author cg
 */
public class CampusCourseCoOwnersTest {

    private CampusCourseCoOwners campusCourseCoOwnersTestObject;
    private BaseSecurity baseSecurityMock;
    private CampusCourseConfiguration campusCourseConfigurationMock;

    @Before
    public void setup() {
        baseSecurityMock = mock(BaseSecurity.class);
        campusCourseConfigurationMock = mock(CampusCourseConfiguration.class);
        campusCourseCoOwnersTestObject = new CampusCourseCoOwners();
        campusCourseCoOwnersTestObject.baseSecurity = baseSecurityMock;
        campusCourseCoOwnersTestObject.campusCourseConfiguration = campusCourseConfigurationMock;
    }

    @Test
    public void getDefaultCoOwners_emptyCoOwnerNames() {
        String EMPTY_CO_OWNER_NAMES = "";
        when(campusCourseConfigurationMock.getDefaultCoOwnerUserNames()).thenReturn(EMPTY_CO_OWNER_NAMES);
        // Exercise
        List<Identity> coOwners = campusCourseCoOwnersTestObject.getDefaultCoOwners();
        assertEquals("Wrong number of owners, must 0 when value is empty", 0, coOwners.size());
    }

    @Test
    public void getDefaultCoOwners_notExistingCoOwnerName() {
        String nonExistingCoOwnerNames = "test1,test2";
        when(campusCourseConfigurationMock.getDefaultCoOwnerUserNames()).thenReturn(nonExistingCoOwnerNames);
        // Exercise
        List<Identity> coOwners = campusCourseCoOwnersTestObject.getDefaultCoOwners();
        assertEquals("Wrong number of owners, must be 0 when no identities exist", 0, coOwners.size());
    }

    @Test
    public void getDefaultCoOwners_oneCoOwnerName() {
        String coOwnerName = "test1";
        Identity coOwnerIdentityMock = mock(Identity.class);
        when(campusCourseConfigurationMock.getDefaultCoOwnerUserNames()).thenReturn(coOwnerName);
        when(baseSecurityMock.findIdentityByName(coOwnerName)).thenReturn(coOwnerIdentityMock);
        // Exercise
        List<Identity> coOwners = campusCourseCoOwnersTestObject.getDefaultCoOwners();
        assertEquals("Wrong number of owners", 1, coOwners.size());
    }

    @Test
    public void getDefaultCoOwners_twoCoOwnerName() {
        String coOwnerName = "test1";
        Identity coOwnerIdentityMock = mock(Identity.class);
        String secondCoOwnerName = "test2";
        Identity secondCoOwnerIdentityMock = mock(Identity.class);
        String coOwnerConigValue = coOwnerName + "," + secondCoOwnerName;
        when(campusCourseConfigurationMock.getDefaultCoOwnerUserNames()).thenReturn(coOwnerConigValue);
        when(baseSecurityMock.findIdentityByName(coOwnerName)).thenReturn(coOwnerIdentityMock);
        when(baseSecurityMock.findIdentityByName(secondCoOwnerName)).thenReturn(secondCoOwnerIdentityMock);
        // Exercise
        List<Identity> coOwners = campusCourseCoOwnersTestObject.getDefaultCoOwners();
        assertEquals("Wrong number of owners", 2, coOwners.size());
    }

    @Test
    public void getDefaultCoOwners_duplicateCoOwnerName() {
        String coOwnerName = "test3";
        Identity coOwnerIdentityMock = mock(Identity.class);
        String coOwnerConigValue = coOwnerName + "," + coOwnerName;
        when(campusCourseConfigurationMock.getDefaultCoOwnerUserNames()).thenReturn(coOwnerConigValue);
        when(baseSecurityMock.findIdentityByName(coOwnerName)).thenReturn(coOwnerIdentityMock);
        // Exercise
        List<Identity> coOwners = campusCourseCoOwnersTestObject.getDefaultCoOwners();
        assertEquals("Wrong number of owners, duplicate identity can be added only once", 1, coOwners.size());
    }

}
