package ch.uzh.campus.mapper;

import ch.uzh.campus.CampusCourseConfiguration;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;

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
 * Initial Date: 02.07.2012 <br>
 * 
 * @author cg
 */
public class AbstractMappingByInstitutionalIdentifier {

    private static final OLog LOG = Tracing.createLoggerFor(AbstractMappingByInstitutionalIdentifier.class);

    private final BaseSecurity baseSecurity;
    private final CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    public AbstractMappingByInstitutionalIdentifier(BaseSecurity baseSecurity, CampusCourseConfiguration campusCourseConfiguration) {
        this.baseSecurity = baseSecurity;
        this.campusCourseConfiguration = campusCourseConfiguration;
    }

    /**
     * @return Mapped identity or null when no identity could be found
     */
    protected Identity tryToMap(final String userProperty, final String queryString) {
        // user property is either matriculation number or employee number
        HashMap<String, String> userProperties = new HashMap<>();
        userProperties.put(userProperty, queryString);
        // Institutional name must be UZH
        //TODO
        //userProperties.put(UserConstants.INSTITUTIONALNAME, )
        // search only visible user and not deleted
        List<Identity> results = baseSecurity.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);

        // TODO remove after INSTITUTIONALUSERIDENTIFIER has been deleted in db (scheduled for 7.8.x)
        // try again with generic INSTITUTIONALUSERIDENTIFIER
        if (results.isEmpty()) {
            userProperties = new HashMap<>();
            userProperties.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, queryString);
            // search only visible user and not deleted
            results = baseSecurity.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);
        }

        if (results.isEmpty()) {
            return null;
        } else if (results.size() == 1) {
            return results.get(0);
        } else {
            LOG.warn("tryToMap " + userProperties.keySet().iterator().next() + "=" + queryString + " and found more than one matching identity!");
            return null;
        }
    }

    /**
     * Returns the property from identity using the same fall-back logic as tryToMap()
     */
    protected String findProperty(Identity mappedIdentity, String propertyName) {
        String personalNumber = mappedIdentity.getUser().getProperty(propertyName, null);
        if (personalNumber == null) {
            personalNumber = mappedIdentity.getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
        }
        return personalNumber;
    }

}
