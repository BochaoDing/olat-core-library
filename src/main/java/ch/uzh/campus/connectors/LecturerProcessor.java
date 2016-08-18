package ch.uzh.campus.connectors;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import ch.uzh.campus.data.Lecturer;

import org.springframework.batch.item.ItemProcessor;

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
 * This is an implementation of {@link ItemProcessor} that validates the input Lecturer item, <br>
 * modifies it according to some criteria and returns it as output Lecturer item. <br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */
public class LecturerProcessor implements ItemProcessor<Lecturer, Lecturer> {

	private static final OLog LOG = Tracing.createLoggerFor(LecturerProcessor.class);

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
     * Returns null if the input lecturer has been already processed, <br>
     * otherwise modifies it according to some criteria and returns it as output
     * 
     * @param lecturer
     *            the Lecturer to be processed
     */
    @Override
    public Lecturer process(Lecturer lecturer) throws Exception {
        // JUST IGNORE THE DUPLICATES
        if (!CampusUtils.addIfNotAlreadyProcessed(processedIdsSet, lecturer.getPersonalNr())) {
            LOG.debug("This is a duplicate of this lecturer [" + lecturer.getPersonalNr() + "]");
            return null;
        }
        if (StringUtils.isBlank(lecturer.getEmail())) {
            lecturer.setEmail(lecturer.getPrivateEmail());
        }
        lecturer.setDateOfImport(new Date());
        return lecturer;
    }

}
