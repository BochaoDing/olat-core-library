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

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
public class MappingStatistic {

    private int newMappingByEmailCounter;
    private int newMappingByMatriculationNrCounter;
    private int newMappingByPersonalNrCounter;
    private int couldNotMapCounter;
    private int couldBeMappedManuallyCounter;
    private int newMappingByAdditionalPersonalNrCounter;

    void addMappingResult(MappingResult mappingResult) {
        if (mappingResult.equals(MappingResult.NEW_MAPPING_BY_EMAIL)) {
            newMappingByEmailCounter++;
        } else if (mappingResult.equals(MappingResult.NEW_MAPPING_BY_MATRICULATION_NR)) {
            newMappingByMatriculationNrCounter++;
        } else if (mappingResult.equals(MappingResult.NEW_MAPPING_BY_PERSONAL_NR)) {
            newMappingByPersonalNrCounter++;
        } else if (mappingResult.equals(MappingResult.COULD_NOT_MAP)) {
            couldNotMapCounter++;
        } else if (mappingResult.equals(MappingResult.COULD_BE_MAPPED_MANUALLY)) {
            couldBeMappedManuallyCounter++;
        } else if (mappingResult.equals(MappingResult.NEW_MAPPING_BY_ADDITIONAL_PERSONAL_NR)) {
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

    public String toStringForStudentMapping() {
        return "MappedByMatriculationNumber="
                + newMappingByMatriculationNrCounter
                + " , MappedByEmail="
                + newMappingByEmailCounter
                + " , couldNotMappedBecauseNotRegistered="
                + couldNotMapCounter
                + " , couldBeMappedManually="
                + couldBeMappedManuallyCounter;
    }

    public String toStringForLecturerMapping() {
        return "MappedByPersonalNumber="
                + newMappingByPersonalNrCounter
                + " , MappedByEmail="
                + newMappingByEmailCounter
                + " , MappedByAdditionalPersonalNumber="
                + newMappingByAdditionalPersonalNrCounter
                + " , couldNotMappedBecauseNotRegistered="
                + couldNotMapCounter
                + " , couldBeMappedManually="
                + couldBeMappedManuallyCounter;
    }

}
