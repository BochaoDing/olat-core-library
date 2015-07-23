/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.assessment.manager;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentServiceImpl implements AssessmentService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;

	@Override
	public AssessmentEntry getOrCreateAssessmentEntry(Identity assessedIdentity, RepositoryEntry entry, String subIdent,
			RepositoryEntry referenceEntry) {
		
		AssessmentEntry assessmentEntry = assessmentEntryDao.loadAssessmentEntry(assessedIdentity, entry, subIdent);
		if(assessmentEntry == null) {
			assessmentEntry = assessmentEntryDao.createCourseNodeAssessment(assessedIdentity, entry, subIdent, referenceEntry);
			dbInstance.commit();
		}
		return assessmentEntry;
	}

	@Override
	public AssessmentEntry loadAssessmentEntry(Identity assessedIdentity, RepositoryEntry entry, String subIdent) {
		if(assessedIdentity == null || entry == null) return null;
		return assessmentEntryDao.loadAssessmentEntry(assessedIdentity, entry, subIdent);
	}

	@Override
	public AssessmentEntry loadAssessmentEntry(Identity assessedIdentity, RepositoryEntry entry, String subIdent, String referenceSoftKey) {
		if(assessedIdentity == null || entry == null) return null;
		return assessmentEntryDao.loadAssessmentEntry(assessedIdentity, entry, subIdent, referenceSoftKey);
	}

	@Override
	public AssessmentEntry updateAssessmentEntry(AssessmentEntry entry) {
		return assessmentEntryDao.updateAssessmentEntry(entry);
	}

	@Override
	public List<AssessmentEntry> loadAssessmentEntriesBySubIdent(RepositoryEntry entry, String subIdent) {
		return assessmentEntryDao.loadAssessmentEntryBySubIdent(entry, subIdent);
	}
	
	
	
	

}
