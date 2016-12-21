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
package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.usermapping;

import ch.uzh.extension.campuscourse.service.usermapping.UserMappingResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
@Repository
@Scope("prototype")
public class UserMappingStatistic {

    private int newMappingByEmailCounter;
    private int newMappingByMatriculationNrCounter;
    private int newMappingByPersonalNrCounter;
    private int couldNotMapCounter;
    private int couldBeMappedManuallyCounter;
    private int newMappingByAdditionalPersonalNrCounter;

    void addMappingResult(UserMappingResult userMappingResult) {
        if (userMappingResult.equals(UserMappingResult.NEW_MAPPING_BY_EMAIL)) {
            newMappingByEmailCounter++;
        } else if (userMappingResult.equals(UserMappingResult.NEW_MAPPING_BY_MATRICULATION_NR)) {
            newMappingByMatriculationNrCounter++;
        } else if (userMappingResult.equals(UserMappingResult.NEW_MAPPING_BY_PERSONAL_NR)) {
            newMappingByPersonalNrCounter++;
        } else if (userMappingResult.equals(UserMappingResult.COULD_NOT_MAP)) {
            couldNotMapCounter++;
        } else if (userMappingResult.equals(UserMappingResult.COULD_BE_MAPPED_MANUALLY)) {
            couldBeMappedManuallyCounter++;
        } else if (userMappingResult.equals(UserMappingResult.NEW_MAPPING_BY_ADDITIONAL_PERSONAL_NR)) {
            newMappingByAdditionalPersonalNrCounter++;
        }
    }

    public String toString() {
        return "MappedByEmail="
                + newMappingByEmailCounter
                + " , MappedByMatriculationNumber="
                + newMappingByMatriculationNrCounter
                + " , MappedByPersonalNumber="
                + newMappingByPersonalNrCounter
                + " , MappedByAdditionalPersonalNumber="
                + newMappingByAdditionalPersonalNrCounter
                + " , couldNotMappedBecauseNotRegistered="
                + couldNotMapCounter
                + " , couldBeMappedManually="
                + couldBeMappedManuallyCounter;
    }
}
