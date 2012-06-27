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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.user.imp;

import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.AddToGroupsEvent;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description:<br>
 * TODO: Felix Class Description for UserImportController
 * <P>
 * Initial Date: 17.08.2005 <br>
 * 
 * @author Felix, Roman Haag
 */
public class UserImportController extends BasicController {

	private List<UserPropertyHandler> userPropertyHandlers;
	private static final String usageIdentifyer = UserImportController.class.getCanonicalName();
	private List<List<String>> newIdents;
	private boolean canCreateOLATPassword;
	private VelocityContainer mainVC;
	private Link startLink;
	
	StepsMainRunController importStepsController;
	
	private final BusinessGroupService businessGroupService;

	/**
	 * @param ureq
	 * @param wControl
	 * @param canCreateOLATPassword true: workflow offers column to create
	 *          passwords; false: workflow does not offer pwd column
	 */
	public UserImportController(UserRequest ureq, WindowControl wControl, boolean canCreateOLATPassword) {
		super(ureq, wControl);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		this.canCreateOLATPassword = canCreateOLATPassword;
		mainVC = createVelocityContainer("importindex");
		startLink = LinkFactory.createButton("import.start", mainVC, this);
		putInitialPanel(mainVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source==importStepsController){
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(importStepsController);
			} else if (event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(importStepsController);
				showInfo("import.success");
			}
		}
	}

	private Identity doCreateAndPersistIdentity(List<String> singleUser) {
		// Create new user and identity and put user to users group
		String login = singleUser.get(1); //pos 0 is used for existing/non-existing user flag
		String pwd = singleUser.get(2);
		String lang = singleUser.get(3);

		// use password only when configured to do so
		if (canCreateOLATPassword) {
			if (!StringHelper.containsNonWhitespace(pwd)) {
				// treat white-space passwords as no-password. This is fine, a password
				// can be set later on
				pwd = null;
			}
		}

		// Create transient user without firstName,lastName, email
		UserManager um = UserManager.getInstance();
		User newUser = um.createUser(null, null, null);

		List<UserPropertyHandler> userProperties = userPropertyHandlers;
		int col = 4;
		String thisValue = "", stringValue;
		for (UserPropertyHandler userPropertyHandler : userProperties) {
			thisValue = singleUser.get(col);
			stringValue = userPropertyHandler.getStringValue(thisValue, getLocale());
			userPropertyHandler.setUserProperty(newUser, stringValue);
			col++;
		}
		// Init preferences
		newUser.getPreferences().setLanguage(lang);
		newUser.getPreferences().setInformSessionTimeout(true);
		// Save everything in database
		Identity ident = AuthHelper.createAndPersistIdentityAndUserWithUserGroup(login, pwd, newUser);
		return ident;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
	// child controllers disposed by basic controller
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == startLink){
		// use fallback translator for user property translation
		setTranslator(UserManager.getInstance().getPropertyHandlerTranslator(getTranslator()));
		userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, true);
		
		Step start = new ImportStep00(ureq, canCreateOLATPassword);
		// callback executed in case wizard is finished.
		StepRunnerCallback finish = new StepRunnerCallback() {
			public Step execute(UserRequest ureq1, WindowControl wControl1, StepsRunContext runContext) {
				// all information to do now is within the runContext saved
				boolean hasChanges = false;
				try {
					if (runContext.containsKey("validImport") && ((Boolean) runContext.get("validImport")).booleanValue()) {
						// create new users and persist 
						newIdents = (List<List<String>>) runContext.get("newIdents");
						for (Iterator<List<String>> it_news = newIdents.iterator(); it_news.hasNext();) {
							List<String> singleUser = it_news.next();
							doCreateAndPersistIdentity(singleUser);
						}
						// fxdiff: 101 add to groups
						Identity addingIdentity = ureq1.getIdentity();
						List<Long> ownGroups = (List<Long>) runContext.get("ownerGroups");
						List<Long> partGroups = (List<Long>) runContext.get("partGroups");
						List<Long> mailGroups = (List<Long>) runContext.get("mailGroups");
						if (ownGroups.size() != 0 || partGroups.size() != 0){
							List<Object> allIdents = (List<Object>) runContext.get("idents");
							processGroupAdditionForAllIdents(allIdents, ownGroups, partGroups, mailGroups, addingIdentity);
						}
						hasChanges = true;
					}
				} catch (Exception any) {
					// return new ErrorStep
				}
				// signal correct completion and tell if changes were made or not.
				return hasChanges ? StepsMainRunController.DONE_MODIFIED : StepsMainRunController.DONE_UNCHANGED;
			}

		};

		importStepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("title"));
		listenTo(importStepsController);
			getWindowControl().pushAsModalDialog(importStepsController.getInitialComponent());
		}
	}

	//fxdiff: 101 add idents to groups
	void processGroupAdditionForAllIdents(List<Object> allIdents, List<Long> ownGroups, List<Long> partGroups, List<Long> mailGroups, Identity addingIdentity) {

		int counter = 0;
		for (Object o : allIdents) {
			Identity ident;
			if (o instanceof Identity) {
				// existing user
				ident = (Identity) o;
			} else {
				List<String> userArray = (List<String>) o;
				String identName = userArray.get(1);
				ident = BaseSecurityManager.getInstance().findIdentityByName(identName);
			}
			if(ident != null){
				AddToGroupsEvent groupsEv = new AddToGroupsEvent(ownGroups, partGroups, mailGroups);
				businessGroupService.addIdentityToGroups(groupsEv, ident, addingIdentity);
				counter ++;
				if (counter % 5 == 0) {
					DBFactory.getInstance().intermediateCommit();
				}
			}			
		}		
	}
}