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
package org.olat.modules.qpool.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StudyFieldAdminController extends BasicController {

	private final MenuTree studyFieldTree;
	private final StudyFieldTreeModel studyFieldTreeModel;
	
	
	private final VelocityContainer mainVC;
	
	public StudyFieldAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("admin_study_fields");

		studyFieldTree = new MenuTree("qpoolTree");
		studyFieldTreeModel = new StudyFieldTreeModel();
		studyFieldTree.setTreeModel(studyFieldTreeModel);
		studyFieldTree.setSelectedNode(studyFieldTree.getTreeModel().getRootNode());
		studyFieldTree.setDropEnabled(false);
		studyFieldTree.addListener(this);
		studyFieldTree.setRootVisible(false);

		mainVC.put("tree", studyFieldTree);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}


	
	

}
