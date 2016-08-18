package ch.uzh.campus.connectors;

import ch.uzh.campus.data.CourseOrgId;
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
public class CourseProcessor implements ItemProcessor<CourseOrgId, CourseOrgId> {
	
	private static final OLog LOG = Tracing.createLoggerFor(CourseProcessor.class);

    private Set<Long> processedIdsSet;

    private Map<String, String> semesterMap = new HashMap<>();

    private static final String WHITESPACE = " ";

    @PostConstruct
    public void init() {
        processedIdsSet = new HashSet<>();
    }

    @PreDestroy
    public void cleanUp() {
        processedIdsSet.clear();
    }

    /**
     * Sets the Map of semesters
     * 
     * @param semesterMap
     *            the Map of semesters
     */
    public void setSemesterMap(Map<String, String> semesterMap) {
        this.semesterMap = semesterMap;
    }

    /**
     * Returns null if the input course has been already processed, <br>
     * otherwise modifies it according to some criteria and returns it as output
     * 
     * @param courseOrgId
     *            the Course to be processed
     */
    @Override
    public CourseOrgId process(CourseOrgId courseOrgId) throws Exception {

        // JUST IGNORE THE DUPLICATES
        if (!CampusUtils.addIfNotAlreadyProcessed(processedIdsSet, courseOrgId.getId())) {
            LOG.debug("This is a duplicate of this course [" + courseOrgId.getId() + "]");
            return null;
        }

        courseOrgId.setDateOfImport(new Date());

        if (courseOrgId.getTitle().contains(CampusUtils.SEMICOLON_REPLACEMENT)) {
            courseOrgId.setTitle(StringUtils.replace(courseOrgId.getTitle(), CampusUtils.SEMICOLON_REPLACEMENT, CampusUtils.SEMICOLON));
        }

        String shortSemester = buildShortSemester(courseOrgId.getSemester());
        if (shortSemester != null) {
            courseOrgId.setShortSemester(shortSemester);
        }

        if (courseOrgId.getOrg1().equals(0L)) {
            courseOrgId.setOrg1(null);
        }
        if (courseOrgId.getOrg2().equals(0L)) {
            courseOrgId.setOrg2(null);
        }
        if (courseOrgId.getOrg3().equals(0L)) {
            courseOrgId.setOrg3(null);
        }
        if (courseOrgId.getOrg4().equals(0L)) {
            courseOrgId.setOrg4(null);
        }
        if (courseOrgId.getOrg5().equals(0L)) {
            courseOrgId.setOrg5(null);
        }
        if (courseOrgId.getOrg6().equals(0L)) {
            courseOrgId.setOrg6(null);
        }
        if (courseOrgId.getOrg7().equals(0L)) {
            courseOrgId.setOrg7(null);
        }
        if (courseOrgId.getOrg8().equals(0L)) {
            courseOrgId.setOrg8(null);
        }
        if (courseOrgId.getOrg9().equals(0L)) {
            courseOrgId.setOrg9(null);
        }

        return courseOrgId;
    }

    /**
     * Build the shortSemester from the given semester
     * 
     * @param semester
     *            The semester from which the shortSemester will be built
     */
    private String buildShortSemester(String semester) {
        String shortSemester = null;

        String[] split = StringUtils.split(semester, WHITESPACE);
        if (split != null && split.length >= 2) {
            String yy = (split[1] != null) ? split[1].substring(2) : "";
            if (split[0] != null) {
                shortSemester = yy.concat(semesterMap.get(split[0].substring(0, 1)));
            }
        }
        return shortSemester;
    }

}
