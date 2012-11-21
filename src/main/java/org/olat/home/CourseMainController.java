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
package org.olat.home;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.SearchRepositoryEntryParameters;
import org.olat.repository.ui.AbstractRepositoryEntryListController;

/**
 * 
 * Initial date: 19.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseMainController extends AbstractRepositoryEntryListController {

	private SearchRepositoryEntryParameters searchParams;
	
	public CourseMainController(UserRequest ureq, WindowControl wContorl) {
		super(ureq, wContorl);
		
		searchParams = new SearchRepositoryEntryParameters(getIdentity(), ureq.getUserSession().getRoles(), "CourseModule");
		searchParams.setOnlyExplicitMember(true);
		
		updateModel(searchParams, false);
	}
}
