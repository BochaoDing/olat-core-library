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

import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
@Component
public class LecturerMappingByPersonalNumber extends AbstractMappingByInstitutionalIdentifier {

    private static final OLog LOG = Tracing.createLoggerFor(LecturerMappingByPersonalNumber.class);

    public Identity tryToMap(Long personalNr) {
        // append '%' because personal-number starts with 0 e.g. 012345
        Identity mappedIdentity = tryToMap(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, "%" + personalNr.toString());
        if (mappedIdentity != null) {
            String personalNumber = mappedIdentity.getUser().getProperty(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, null);
            try {
                Long personalNumberAsLong = Long.valueOf(personalNumber);
                if (personalNumberAsLong.equals(personalNr)) {
                    return mappedIdentity;
                }
                LOG.warn("User-Property as Long (" + personalNumberAsLong + ") has not the same value as lecturer personal-number=" + personalNr);
            } catch (NumberFormatException ex) {
                LOG.warn("Could not convert personal-number to Long");
            }
        }
        return null;

    }

}
