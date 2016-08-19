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

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.data.Student;

import org.apache.commons.lang.StringUtils;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
@Component
public class StudentMappingByMatriculationNumber extends AbstractMappingByInstitutionalIdentifier {

    @Autowired
    public StudentMappingByMatriculationNumber(BaseSecurity baseSecurity, CampusCourseConfiguration campusCourseConfiguration) {
        super(baseSecurity, campusCourseConfiguration);
    }

    public Identity tryToMap(Student student) {
        Identity identity = tryToMap(UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER, student.getRegistrationNr());
        if (identity == null) {
            identity = tryToMap(UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER, StringUtils.remove(student.getRegistrationNr(), "-"));
        }
        return identity;
    }
}
