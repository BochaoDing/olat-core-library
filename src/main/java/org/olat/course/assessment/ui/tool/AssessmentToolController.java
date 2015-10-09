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
package org.olat.course.assessment.ui.tool;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentMainController;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentToolController extends MainLayoutBasicController implements Activateable2 {


	private RepositoryEntry courseEntry;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	private Link usersLink;
	private final TooledStackedPanel stackPanel;
	private AssessmentCourseOverviewController overviewCtrl;
	private AssessmentIdentitiesCourseTreeController currentCtl;
	
	public AssessmentToolController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentMainController.class, getLocale(), getTranslator()));
		this.courseEntry = courseEntry;
		this.stackPanel = stackPanel;
		this.assessmentCallback = assessmentCallback;

		
		overviewCtrl = new AssessmentCourseOverviewController(ureq, getWindowControl(), courseEntry, assessmentCallback);
		listenTo(overviewCtrl);
		putInitialPanel(overviewCtrl.getInitialComponent());
	}
	
	public void initToolbar() {
		usersLink = LinkFactory.createToolLink("users", translate("users"), this, "o_icon_user");
		stackPanel.addTool(usersLink);
		
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.size() == 0) {
			//
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == usersLink) {
			doSelectUsersView(ureq);
		}
	}
	
	private void doSelectUsersView(UserRequest ureq) {
		removeAsListenerAndDispose(currentCtl);
		
		AssessmentIdentitiesCourseTreeController treeCtrl = new AssessmentIdentitiesCourseTreeController(ureq, getWindowControl(), stackPanel,
				courseEntry, assessmentCallback);
		listenTo(treeCtrl);
		stackPanel.pushController(translate("users"), treeCtrl);
		currentCtl = treeCtrl;
		treeCtrl.activate(ureq, null, null);
	}
}
