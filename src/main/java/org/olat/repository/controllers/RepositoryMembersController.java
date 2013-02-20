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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.repository.controllers;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.member.wizard.ImportMember_1a_LoginListStep;
import org.olat.course.member.wizard.ImportMember_1b_ChooseMemberStep;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.ui.main.AbstractMemberListController;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryMembersController extends AbstractMemberListController {
	
	private final SearchMembersParams params;
	private final RepositoryEntry repoEntry;
	private final Link importMemberLink, addMemberLink;
	private StepsMainRunController importMembersWizard;
	
	private final RepositoryManager repositoryManager;
	private final BusinessGroupService businessGroupService;

	public RepositoryMembersController(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry) {
		super(ureq, wControl, repoEntry, "all_member_list");
		
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);

		this.repoEntry = repoEntry;
		addMemberLink = LinkFactory.createButton("add.member", mainVC, this);
		mainVC.put("addMembers", addMemberLink);
		importMemberLink = LinkFactory.createButton("import.member", mainVC, this);
		mainVC.put("importMembers", importMemberLink);
		
		params = new SearchMembersParams(true, true, true, true, true, true, true);
		reloadModel();
	}

	@Override
	protected SearchMembersParams getSearchParams() {
		return params;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		 if (source == addMemberLink) {
			doChooseMembers(ureq);
		} else if (source == importMemberLink) {
			doImportMembers(ureq);
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if(source == importMembersWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(importMembersWizard);
				importMembersWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					reloadModel();
				}
			}
		} else {
			super.event(ureq, source, event);
		}
	}
	
	private void doChooseMembers(UserRequest ureq) {
		removeAsListenerAndDispose(importMembersWizard);

		Step start = new ImportMember_1b_ChooseMemberStep(ureq, repoEntry, null);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
				addMembers(ureq, runContext);
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		importMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.member"), "o_sel_group_import_1_wizard");
		listenTo(importMembersWizard);
		getWindowControl().pushAsModalDialog(importMembersWizard.getInitialComponent());
	}
	
	private void doImportMembers(UserRequest ureq) {
		removeAsListenerAndDispose(importMembersWizard);

		Step start = new ImportMember_1a_LoginListStep(ureq, repoEntry, null);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
				addMembers(ureq, runContext);
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		importMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("import.member"), "o_sel_group_import_logins_wizard");
		listenTo(importMembersWizard);
		getWindowControl().pushAsModalDialog(importMembersWizard.getInitialComponent());
	}
	
	protected void addMembers(UserRequest ureq, StepsRunContext runContext) {
		@SuppressWarnings("unchecked")
		List<Identity> members = (List<Identity>)runContext.get("members");
		MailTemplate template = (MailTemplate)runContext.get("mailTemplate");
		MemberPermissionChangeEvent changes = (MemberPermissionChangeEvent)runContext.get("permissions");
		
		//commit changes to the repository entry
		MailerResult result = new MailerResult();
		MailPackage reMailing = new MailPackage(template, result, getWindowControl().getBusinessControl().getAsString(), template != null);
		List<RepositoryEntryPermissionChangeEvent> repoChanges = changes.generateRepositoryChanges(members);
		repositoryManager.updateRepositoryEntryMembership(getIdentity(), ureq.getUserSession().getRoles(), repoEntry, repoChanges, reMailing);

		//commit all changes to the group memberships
		List<BusinessGroupMembershipChange> allModifications = changes.generateBusinessGroupMembershipChange(members);
		MailPackage bgMailing = new MailPackage(template, result, getWindowControl().getBusinessControl().getAsString(), template != null);
		businessGroupService.updateMemberships(getIdentity(), allModifications, bgMailing);
		MailHelper.printErrorsAndWarnings(result, getWindowControl(), getLocale());
	}
}
