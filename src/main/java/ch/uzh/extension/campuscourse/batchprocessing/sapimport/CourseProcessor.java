package ch.uzh.extension.campuscourse.batchprocessing.sapimport;

import ch.uzh.extension.campuscourse.model.CourseSemesterOrgId;
import org.apache.commons.lang.StringUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;

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
 * This is an implementation of {@link ItemProcessor} that validates the input Course item, <br>
 * modifies it according to some criteria and returns it as output Course item.<br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */
@Component
@Scope("step")
public class CourseProcessor implements ItemProcessor<CourseSemesterOrgId, CourseSemesterOrgId> {

    private static final String SEMICOLON_REPLACEMENT = "&Semikolon&";
    private static final String SEMICOLON = ";";

    /**
     * Modifies course according to some criteria and returns it as output.
     * 
     * @param courseSemesterOrgId the Course to be processed
     */
    @Override
    public CourseSemesterOrgId process(CourseSemesterOrgId courseSemesterOrgId) throws Exception {

        courseSemesterOrgId.setDateOfImport(new Date());

        if (courseSemesterOrgId.getTitle().contains(SEMICOLON_REPLACEMENT)) {
            courseSemesterOrgId.setTitle(StringUtils.replace(courseSemesterOrgId.getTitle(), SEMICOLON_REPLACEMENT, SEMICOLON));
        }

        if (courseSemesterOrgId.getOrg1() != null && courseSemesterOrgId.getOrg1() == 0L) {
            courseSemesterOrgId.setOrg1(null);
        }
        if (courseSemesterOrgId.getOrg2() != null && courseSemesterOrgId.getOrg2() == 0L) {
            courseSemesterOrgId.setOrg2(null);
        }
        if (courseSemesterOrgId.getOrg3() != null && courseSemesterOrgId.getOrg3() == 0L) {
            courseSemesterOrgId.setOrg3(null);
        }
        if (courseSemesterOrgId.getOrg4() != null && courseSemesterOrgId.getOrg4() == 0L) {
            courseSemesterOrgId.setOrg4(null);
        }
        if (courseSemesterOrgId.getOrg5() != null && courseSemesterOrgId.getOrg5() == 0L) {
            courseSemesterOrgId.setOrg5(null);
        }
        if (courseSemesterOrgId.getOrg6() != null && courseSemesterOrgId.getOrg6() == 0L) {
            courseSemesterOrgId.setOrg6(null);
        }
        if (courseSemesterOrgId.getOrg7() != null && courseSemesterOrgId.getOrg7() == 0L) {
            courseSemesterOrgId.setOrg7(null);
        }
        if (courseSemesterOrgId.getOrg8() != null && courseSemesterOrgId.getOrg8() == 0L) {
            courseSemesterOrgId.setOrg8(null);
        }
        if (courseSemesterOrgId.getOrg9() != null && courseSemesterOrgId.getOrg9() == 0L) {
            courseSemesterOrgId.setOrg9(null);
        }

        return courseSemesterOrgId;
    }
}
