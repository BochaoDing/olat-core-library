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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package com.frentix.olat.course.nodes;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.StatusDescriptionHelper;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

import com.frentix.olat.course.nodes.vitero.ViteroEditController;
import com.frentix.olat.course.nodes.vitero.ViteroPeekViewController;
import com.frentix.olat.vitero.manager.ViteroManager;
import com.frentix.olat.vitero.ui.ViteroBookingsRunController;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = 8680935159748506305L;

	private static final String TYPE = "vitero";

	// configuration
	public static final String CONF_VC_CONFIGURATION = "vc_configuration";

	public ViteroCourseNode() {
		super(TYPE);
	}

	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
		// no update to default config necessary
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, ICourse course,
			UserCourseEnvironment userCourseEnv) {
		updateModuleConfigDefaults(false);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		// create edit controller
		ViteroEditController childTabCntrllr = new ViteroEditController(ureq, wControl, this, course, userCourseEnv);
		
		NodeEditController nodeEditCtr = new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode,
				course.getCourseEnvironment().getCourseGroupManager(), userCourseEnv, childTabCntrllr);
		nodeEditCtr.addControllerListener(childTabCntrllr);
		return nodeEditCtr;
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		updateModuleConfigDefaults(false);
		// check if user is moderator of the virtual classroom
		Roles roles = ureq.getUserSession().getRoles();
		boolean moderator = roles.isOLATAdmin();
		Long key = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		if (!moderator) {
			if(roles.isInstitutionalResourceManager() | roles.isAuthor()) {
				RepositoryManager rm = RepositoryManager.getInstance();
				ICourse course = CourseFactory.loadCourse(key);
				RepositoryEntry re = rm.lookupRepositoryEntry(course, false);
				if (re != null) {
					moderator = rm.isOwnerOfRepositoryEntry(ureq.getIdentity(), re);
					if(!moderator) {
						moderator = rm.isInstitutionalRessourceManagerFor(re, ureq.getIdentity());
					}
				}
			}
		}
		// create run controller
		
		Long resourceId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, resourceId);
		String courseTitle = userCourseEnv.getCourseEnvironment().getCourseTitle();
		Controller runCtr = new ViteroBookingsRunController(ureq, wControl, null, ores, courseTitle, moderator);
		Controller controller = TitledWrapperHelper.getWrapper(ureq, wControl, runCtr, this, "o_vc_icon");
		return new NodeRunConstructionResult(controller);
	}

	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		return new ViteroPeekViewController(ureq, wControl, userCourseEnv);
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }
		StatusDescription status = StatusDescription.NOERROR;
		return status;
	}
	
	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
	
	@Override
	public void cleanupOnDelete(ICourse course) {
		// load configuration
		ViteroManager provider = (ViteroManager)CoreSpringFactory.getBean("viteroManager");
		// remove meeting
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(course.getResourceableTypeName(), course.getResourceableId());
		provider.deleteAll(null, ores);
	}
}