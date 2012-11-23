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
package org.olat.course.member;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.group.ui.main.AbstractMemberListController;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MemberListWithOriginFilterController extends AbstractMemberListController {
	
	private final SearchMembersParams searchParams;
	private final OriginFilterController filterController;
	
	public MemberListWithOriginFilterController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry repoEntry, SearchMembersParams searchParams, String infos) {
		super(ureq, wControl, repoEntry, "member_list_origin_filter");
		this.searchParams = searchParams;
		
		filterController = new OriginFilterController(ureq, wControl);
		listenTo(filterController);
		mainVC.put("originFilter", filterController.getInitialComponent());
		
		if(StringHelper.containsNonWhitespace(infos)) {
			mainVC.contextPut("infos", infos);
		}
	}

	@Override
	public SearchMembersParams getSearchParams() {
		return searchParams;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == filterController) {
			if(event instanceof SearchOriginParams) {
				SearchOriginParams filter = (SearchOriginParams)event;
				searchParams.setRepoOrigin(filter.isRepoOrigin());
				searchParams.setGroupOrigin(filter.isGroupOrigin());
				reloadModel();
			}
		}
		super.event(ureq, source, event);
	}
}