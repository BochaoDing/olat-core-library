package ch.uzh.campus.connectors;

import ch.uzh.campus.data.CourseSemesterOrgId;
import org.apache.commons.lang.StringUtils;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemProcessor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

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
public class CourseProcessor implements ItemProcessor<CourseSemesterOrgId, CourseSemesterOrgId> {
	
	private static final OLog LOG = Tracing.createLoggerFor(CourseProcessor.class);

    private Set<Long> processedIdsSet;

    @PostConstruct
    public void init() {
        processedIdsSet = new HashSet<>();
    }

    @PreDestroy
    public void cleanUp() {
        processedIdsSet.clear();
    }

    /**
     * Returns null if the input course has been already processed, <br>
     * otherwise modifies it according to some criteria and returns it as output
     * 
     * @param courseSemesterOrgId
     *            the Course to be processed
     */
    @Override
    public CourseSemesterOrgId process(CourseSemesterOrgId courseSemesterOrgId) throws Exception {

        // JUST IGNORE THE DUPLICATES
        if (!CampusUtils.addIfNotAlreadyProcessed(processedIdsSet, courseSemesterOrgId.getId())) {
            LOG.debug("This is a duplicate of this course [" + courseSemesterOrgId.getId() + "]");
            return null;
        }

        courseSemesterOrgId.setDateOfImport(new Date());

        if (courseSemesterOrgId.getTitle().contains(CampusUtils.SEMICOLON_REPLACEMENT)) {
            courseSemesterOrgId.setTitle(StringUtils.replace(courseSemesterOrgId.getTitle(), CampusUtils.SEMICOLON_REPLACEMENT, CampusUtils.SEMICOLON));
        }

        if (courseSemesterOrgId.getOrg1().equals(0L)) {
            courseSemesterOrgId.setOrg1(null);
        }
        if (courseSemesterOrgId.getOrg2().equals(0L)) {
            courseSemesterOrgId.setOrg2(null);
        }
        if (courseSemesterOrgId.getOrg3().equals(0L)) {
            courseSemesterOrgId.setOrg3(null);
        }
        if (courseSemesterOrgId.getOrg4().equals(0L)) {
            courseSemesterOrgId.setOrg4(null);
        }
        if (courseSemesterOrgId.getOrg5().equals(0L)) {
            courseSemesterOrgId.setOrg5(null);
        }
        if (courseSemesterOrgId.getOrg6().equals(0L)) {
            courseSemesterOrgId.setOrg6(null);
        }
        if (courseSemesterOrgId.getOrg7().equals(0L)) {
            courseSemesterOrgId.setOrg7(null);
        }
        if (courseSemesterOrgId.getOrg8().equals(0L)) {
            courseSemesterOrgId.setOrg8(null);
        }
        if (courseSemesterOrgId.getOrg9().equals(0L)) {
            courseSemesterOrgId.setOrg9(null);
        }

        return courseSemesterOrgId;
    }
}
