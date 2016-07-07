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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioElement;
import org.olat.modules.portfolio.PortfolioElementType;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.model.AccessRightChange;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.ui.event.AccessRightsEvent;
import org.olat.modules.portfolio.ui.renderer.PortfolioRendererHelper;
import org.olat.modules.portfolio.ui.wizard.AccessRightsContext;
import org.olat.modules.portfolio.ui.wizard.AddMember_1_ChooseMemberStep;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PublishController extends BasicController implements TooledController {
	
	private Dropdown accessDropdown;
	private Link addAccessRightsLink, addInvitationLink;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;

	private CloseableModalController cmc;
	private AddInvitationRightsController addInvitationCtrl;
	private AccessRightsEditController editAccessRightsCtrl;
	private StepsMainRunController addMembersWizard;
	
	private int counter;
	private Binder binder;
	private PortfolioElementRow binderRow;
	private final BinderConfiguration config;
	private final BinderSecurityCallback secCallback;
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioService portfolioService;
	
	public PublishController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Binder binder, BinderConfiguration config) {
		super(ureq, wControl);
		this.binder = binder;
		this.config = config;
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		
		mainVC = createVelocityContainer("publish");
		mainVC.contextPut("binderTitle", binder.getTitle());
		
		binderRow = new PortfolioElementRow(binder, null);
		mainVC.contextPut("binderRow", binderRow);
		putInitialPanel(mainVC);
		reloadData();
	}
	
	@Override
	public void initTools() {
		if(secCallback.canEditAccessRights(binder)) {
			accessDropdown = new Dropdown("access.rights", "access.rights", false, getTranslator());
			accessDropdown.setIconCSS("o_icon o_icon-fw o_icon_new_portfolio");
			accessDropdown.setOrientation(DropdownOrientation.right);
			
			addAccessRightsLink = LinkFactory.createToolLink("add.member", translate("add.member"), this);
			addAccessRightsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			accessDropdown.addComponent(addAccessRightsLink);
			
			addInvitationLink = LinkFactory.createToolLink("add.invitation", translate("add.invitation"), this);
			addInvitationLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			accessDropdown.addComponent(addInvitationLink);
			
			stackPanel.addTool(accessDropdown, Align.right);
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public void reloadData() {
		binderRow.getChildren().clear();
		binderRow.getAccessRights().clear();
		
		List<AccessRights> rights = portfolioService.getAccessRights(binder);
		boolean canEditBinderAccessRights = secCallback.canEditAccessRights(binder);
		for(AccessRights right:rights) {
			if(right.getSectionKey() == null && right.getPageKey() == null) {
				Link editLink = null;
				if(canEditBinderAccessRights
						&& !PortfolioRoles.owner.equals(right.getRole())
						&&!PortfolioRoles.invitee.equals(right.getRole())) {
					editLink = LinkFactory.createLink("edit_" + (counter++), "edit", "edit_access", mainVC, this);
				}
				binderRow.getAccessRights().add(new AccessRightsRow(binder, right, editLink));
			}
		}
		
		List<AssessmentSection> assessmentSections = portfolioService.getAssessmentSections(binder, getIdentity());
		Map<Section,AssessmentSection> sectionToAssessmentSectionMap = new HashMap<>();
		for(AssessmentSection assessmentSection:assessmentSections) {
			sectionToAssessmentSectionMap.put(assessmentSection.getSection(), assessmentSection);
		}

		//sections
		List<Section> sections = portfolioService.getSections(binder);
		Map<Long,PortfolioElementRow> sectionMap = new HashMap<>();
		for(Section section:sections) {
			PortfolioElementRow sectionRow = new PortfolioElementRow(section, sectionToAssessmentSectionMap.get(section));
			binderRow.getChildren().add(sectionRow);
			sectionMap.put(section.getKey(), sectionRow);	

			boolean canEditSectionAccessRights = secCallback.canEditAccessRights(section);
			for(AccessRights right:rights) {
				if(section.getKey().equals(right.getSectionKey()) && right.getPageKey() == null) {
					Link editLink = null;
					if(canEditSectionAccessRights && !PortfolioRoles.owner.equals(right.getRole())) {
						editLink = LinkFactory.createLink("edit_" + (counter++), "edit", "edit_access", mainVC, this);
					}
					sectionRow.getAccessRights().add(new AccessRightsRow(section, right, editLink));
				}
			}
		}
		
		//pages
		List<Page> pages = portfolioService.getPages(binder, null);
		for(Page page:pages) {
			Section section = page.getSection();
			PortfolioElementRow sectionRow = sectionMap.get(section.getKey());
			
			PortfolioElementRow pageRow = new PortfolioElementRow(page, null);
			sectionRow.getChildren().add(pageRow);

			boolean canEditPageAccessRights = secCallback.canEditAccessRights(page);
			for(AccessRights right:rights) {
				if(page.getKey().equals(right.getPageKey())) {
					Link editLink = null;
					if(canEditPageAccessRights && !PortfolioRoles.owner.equals(right.getRole())) {
						editLink = LinkFactory.createLink("edit_" + (counter++), "edit", "edit_access", mainVC, this);
					}
					pageRow.getAccessRights().add(new AccessRightsRow(page, right, editLink));
				}
			}
		}
		
		mainVC.setDirty(true);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(addAccessRightsLink == source) {
			doAddAccessRights(ureq);
		} else if(addInvitationLink == source) {
			doAddInvitation(ureq);
		} else if(source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if("edit_access".equals(cmd)) {
				AccessRightsRow row = (AccessRightsRow)link.getUserObject();
				doEditAccessRights(ureq, row.getElement(), row.getIdentity());
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (addMembersWizard == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					reloadData();
				}
				cleanUp();
			}
		} else if(addInvitationCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					reloadData();
				}
				cleanUp();
			}
		} else if(editAccessRightsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				List<AccessRightChange> changes = editAccessRightsCtrl.getChanges();
				List<Identity> identities = Collections.singletonList(editAccessRightsCtrl.getMember());
				portfolioService.changeAccessRights(identities, changes);
				reloadData();
			} else if(AccessRightsEvent.REMOVE_ALL_RIGHTS.equals(event.getCommand())) {
				portfolioService.removeAccessRights(binder, editAccessRightsCtrl.getMember());
				reloadData();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editAccessRightsCtrl);
		removeAsListenerAndDispose(addInvitationCtrl);
		removeAsListenerAndDispose(addMembersWizard);
		removeAsListenerAndDispose(cmc);
		editAccessRightsCtrl = null;
		addInvitationCtrl = null;
		addMembersWizard = null;
		cmc = null;
	}
	
	private void doAddInvitation(UserRequest ureq) {
		if(addInvitationCtrl != null) return;
		
		addInvitationCtrl = new AddInvitationRightsController(ureq, getWindowControl(), binder);
		listenTo(addInvitationCtrl);
		
		String title = translate("add.invitation");
		cmc = new CloseableModalController(getWindowControl(), null, addInvitationCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditAccessRights(UserRequest ureq, PortfolioElement element, Identity member) {
		if(editAccessRightsCtrl != null) return;
		
		boolean canEdit = secCallback.canEditAccessRights(element);
		editAccessRightsCtrl = new AccessRightsEditController(ureq, getWindowControl(), binder, member, canEdit);
		listenTo(editAccessRightsCtrl);
		
		String title = translate("edit.access.rights");
		cmc = new CloseableModalController(getWindowControl(), null, editAccessRightsCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddAccessRights(UserRequest ureq) {
		removeAsListenerAndDispose(addMembersWizard);

		Step start = new AddMember_1_ChooseMemberStep(ureq, binder);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				AccessRightsContext rightsContext = (AccessRightsContext)runContext.get("rightsContext");
				MailTemplate mailTemplate = (MailTemplate)runContext.get("mailTemplate");
				addMembers(rightsContext, mailTemplate);
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		addMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.member"), "o_sel_course_member_import_1_wizard");
		listenTo(addMembersWizard);
		getWindowControl().pushAsModalDialog(addMembersWizard.getInitialComponent());
		
	}
	
	private void addMembers(AccessRightsContext rightsContext, MailTemplate mailTemplate) {
		List<Identity> identities = rightsContext.getIdentities();
		List<AccessRightChange> changes = rightsContext.getAccessRightChanges();
		portfolioService.changeAccessRights(identities, changes);
		
		if(mailTemplate != null) {
			sendInvitation(identities, mailTemplate);
		}
		reloadData();
	}
	
	private void sendInvitation(List<Identity> identities, MailTemplate mailTemplate) {
		ContactList contactList = new ContactList("Invitation");
		contactList.addAllIdentites(identities);

		boolean success = false;
		try {
			MailContext context = new MailContextImpl(binder, null, getWindowControl().getBusinessControl().getAsString()); 
			MailBundle bundle = new MailBundle();
			bundle.setContext(context);
			bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
			bundle.setContactList(contactList);
			bundle.setContent(mailTemplate.getSubjectTemplate(), mailTemplate.getBodyTemplate());
			MailerResult result = mailManager.sendMessage(bundle);
			success = result.isSuccessful();
		} catch (Exception e) {
			logError("Error on sending invitation mail to contactlist, invalid address.", e);
		}
		if (success) {
			showInfo("invitation.mail.success");
		}	else {
			showError("invitation.mail.failure");			
		}
	}
	
	public class AccessRightsRow {
		
		private final AccessRights rights;
		private final PortfolioElement element;
		private String fullName;
		private Link editLink;
		
		public AccessRightsRow(PortfolioElement element, AccessRights rights, Link editLink) {
			this.rights = rights;
			this.editLink = editLink;
			this.element = element;
			fullName = userManager.getUserDisplayName(rights.getIdentity());
			if(editLink != null) {
				editLink.setUserObject(this);
			}
		}
		
		public String getRole() {
			return rights.getRole().name();
		}
		
		public Identity getIdentity() {
			return rights.getIdentity();
		}
		
		public PortfolioElement getElement() {
			return element;
		}
		
		public String getFullName() {
			return fullName;
		}
		
		public String getCssClass() {
			if(PortfolioRoles.reviewer.equals(rights.getRole())) {
				return "o_icon o_icon_reviewer";
			}
			return "o_icon o_icon_user";
		}
		
		public boolean hasEditLink() {
			return editLink != null;
		}
		
		public String getEditLinkComponentName() {
			return editLink == null ? null : editLink.getComponentName();
		}
		
		public String getExplanation() {
			String explanation = null;
			if(PortfolioRoles.owner.equals(rights.getRole())) {
				explanation = translate("access.rights.owner.long");
			} else if(PortfolioRoles.coach.equals(rights.getRole())) {
				explanation = translate("access.rights.coach.long");
			} else if(PortfolioRoles.reviewer.equals(rights.getRole())) {
				explanation = translate("access.rights.reviewer.long");
			} else if(PortfolioRoles.invitee.equals(rights.getRole())) {
				explanation = translate("access.rights.invitee.long");
			}
			return explanation;
		}
	}

	public class PortfolioElementRow {
		
		private final PortfolioElement element;
		private List<PortfolioElementRow> children;
		private List<AccessRightsRow> accessRights = new ArrayList<>();
		
		private final AssessmentSection assessmentSection;
		
		public PortfolioElementRow(PortfolioElement element, AssessmentSection assessmentSection) {
			this.element = element;
			this.assessmentSection = assessmentSection;
		}
		
		public boolean isAssessable() {
			return config.isAssessable();
		}
		
		
		public String getTitle() {
			return element.getTitle();
		}
		
		public String getCssClassStatus() {
			if(element.getType() == PortfolioElementType.section) {
				Section section = (Section)element;
				return section.getSectionStatus() == null
					? SectionStatus.notStarted.cssClass() : section.getSectionStatus().cssClass();
			}
			return "";
		}
		
		public String getFormattedResult() {
			if(element.getType() == PortfolioElementType.section) {
				return PortfolioRendererHelper.getFormattedResult(assessmentSection, getTranslator());
			}
			return "";
		}
		
		public List<AccessRightsRow> getAccessRights() {
			return accessRights;
		}
		
		public List<PortfolioElementRow> getChildren() {
			if(children == null) {
				children = new ArrayList<>();
			}
			return children;
		}
	}
}
