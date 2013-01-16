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

package org.olat.admin.securitygroup.gui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.admin.securitygroup.gui.multi.UsersToGroupWizardController;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailNotificationEditController;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.core.util.session.UserSessionManager;
import org.olat.group.ui.main.OnlineIconRenderer;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.Presence;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description:<BR>
 * Generic group management controller. Displays the list of users that are in
 * the given security group and features an add button to add users to the
 * group.
 * <p>
 * Fired events:
 * <ul>
 * <li>IdentityAddedEvent</li>
 * <li>IdentityRemovedEvent</li>
 * <li>SingleIdentityChosenEvent</li>
 * <li>Event.CANCELLED_EVENT</li>
 * </ul>
 * <P>
 * Initial Date: Jan 25, 2005
 * 
 * @author Felix Jost, Florian Gnägi
 */

public class GroupController extends BasicController {

	protected boolean keepAtLeastOne;
	protected boolean mayModifyMembers;

	protected static final String COMMAND_REMOVEUSER = "removesubjectofgroup";
	protected static final String COMMAND_IM = "im";
	protected static final String COMMAND_VCARD = "show.vcard";
	protected static final String COMMAND_SELECTUSER = "select.user";

	protected SecurityGroup securityGroup;
	protected VelocityContainer groupmemberview;

	protected IdentitiesOfGroupTableDataModel identitiesTableModel;

	private List<Identity> toAdd, toRemove;

	private UserSearchController usc;
	private MailNotificationEditController addUserMailCtr, removeUserMailCtr;
	private UsersToGroupWizardController userToGroupWizardCtr;
	private DialogBoxController confirmDelete;

	protected TableController tableCtr;
	private Link addUsersButton;
	private Link addUserButton;
	private Translator myTrans;

	private MailTemplate addUserMailDefaultTempl, removeUserMailDefaultTempl, removeUserMailCustomTempl;
	private boolean showSenderInAddMailFooter;
	private boolean showSenderInRemovMailFooter;
	private CloseableModalController cmc;
	protected static final String usageIdentifyer = IdentitiesOfGroupTableDataModel.class.getCanonicalName();
	protected boolean isAdministrativeUser;
	protected boolean mandatoryEmail;

	protected BaseSecurity securityManager;
	private UserManager userManager;
	private InstantMessagingModule imModule;
	private InstantMessagingService imService;
	private UserSessionManager sessionManager;

	/**
	 * @param ureq
	 * @param wControl
	 * @param mayModifyMembers
	 * @param keepAtLeastOne
	 * @param enableTablePreferences
	 * @param aSecurityGroup
	 * @param enableUserSelection
	 */	 
	public GroupController(UserRequest ureq, WindowControl wControl, 
			boolean mayModifyMembers, boolean keepAtLeastOne, boolean enableTablePreferences, boolean enableUserSelection,
			boolean allowDownload, boolean mandatoryEmail, SecurityGroup aSecurityGroup) {
		super(ureq, wControl);
		init(ureq, mayModifyMembers, keepAtLeastOne, enableTablePreferences, enableUserSelection, allowDownload, mandatoryEmail, aSecurityGroup);
	}

	protected void init(UserRequest ureq,
			boolean mayModifyMembers, boolean keepAtLeastOne, boolean enableTablePreferences, boolean enableUserSelection,
			boolean allowDownload, boolean mandatoryEmail, SecurityGroup aSecurityGroup) {
		this.securityGroup = aSecurityGroup;
		this.mayModifyMembers = mayModifyMembers;
		this.keepAtLeastOne = keepAtLeastOne;
		this.mandatoryEmail = mandatoryEmail;
		securityManager = BaseSecurityManager.getInstance();
		imModule = CoreSpringFactory.getImpl(InstantMessagingModule.class);
		imService = CoreSpringFactory.getImpl(InstantMessagingService.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		sessionManager = CoreSpringFactory.getImpl(UserSessionManager.class);
		
		Roles roles = ureq.getUserSession().getRoles();
		BaseSecurityModule securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);

		// default group controller has no mail functionality
		this.addUserMailDefaultTempl = null;
		this.removeUserMailDefaultTempl = null;

		groupmemberview = createVelocityContainer("index");

		addUsersButton = LinkFactory.createButtonSmall("overview.addusers", groupmemberview, this);
		addUsersButton.setElementCssClass("o_sel_group_import_users");
		addUserButton = LinkFactory.createButtonSmall("overview.adduser", groupmemberview, this);
		addUserButton.setElementCssClass("o_sel_group_add_user");

		if (mayModifyMembers) {
			groupmemberview.contextPut("mayadduser", Boolean.TRUE);
		}

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(allowDownload);
		if (enableTablePreferences) {
			// save table preferences for each group seperatly
			if (mayModifyMembers) {
				tableConfig.setPreferencesOffered(true, "groupcontroller" + securityGroup.getKey());
			} else {
				// different rowcount...
				tableConfig.setPreferencesOffered(true, "groupcontrollerreadonly" + securityGroup.getKey());
			}
		}
		
		myTrans = userManager.getPropertyHandlerTranslator(getTranslator());
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), myTrans);	
		listenTo(tableCtr);
		
		initGroupTable(tableCtr, ureq, enableTablePreferences, enableUserSelection);

		// set data model
		reloadData();
		groupmemberview.put("subjecttable", tableCtr.getInitialComponent());
		putInitialPanel(groupmemberview);
	}

	/**
	 * @param addUserMailDefaultTempl Set a template to send mail when adding
	 *          users to group
	 */
	public void setAddUserMailTempl(MailTemplate addUserMailTempl, boolean showSenderInAddFooter) {
		this.addUserMailDefaultTempl = addUserMailTempl;
		this.showSenderInAddMailFooter = showSenderInAddFooter;
	}

	/**
	 * @param removeUserMailDefaultTempl Set a template to send mail when removing
	 *          a user from the group
	 */
	public void setRemoveUserMailTempl(MailTemplate removeUserMailTempl, boolean showSenderInRemoveFooter) {
		this.removeUserMailDefaultTempl = removeUserMailTempl;
		this.showSenderInRemovMailFooter = showSenderInRemoveFooter;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == addUserButton) {
			if (!mayModifyMembers) throw new AssertException("not allowed to add a member!");
			
			removeAsListenerAndDispose(usc);
			usc = new UserSearchController(ureq, getWindowControl(), true, true);			
			listenTo(usc);
			
			Component usersearchview = usc.getInitialComponent();
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), usersearchview, true, translate("add.searchuser"));
			listenTo(cmc);
			
			cmc.activate();
		} else if (source == addUsersButton) {
			if (!mayModifyMembers) throw new AssertException("not allowed to add members!");
			
			removeAsListenerAndDispose(userToGroupWizardCtr);
			userToGroupWizardCtr = new UsersToGroupWizardController(ureq, getWindowControl(), securityGroup, addUserMailDefaultTempl, mandatoryEmail);			
			listenTo(userToGroupWizardCtr);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), userToGroupWizardCtr.getInitialComponent());
			listenTo(cmc);
			
			cmc.activate();
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller sourceController, Event event) {
		if (sourceController == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				// Single row selects
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				final Identity identity = identitiesTableModel.getObject(te.getRowId()).getIdentity();
				if (actionid.equals(COMMAND_VCARD)) {
					//get identity and open new visiting card controller in new window
					ControllerCreator userInfoMainControllerCreator = new ControllerCreator() {
						public Controller createController(UserRequest lureq, WindowControl lwControl) {
							return new UserInfoMainController(lureq, lwControl, identity);
						}					
					};
					//wrap the content controller into a full header layout
					ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, userInfoMainControllerCreator);
					//open in new browser window
					PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
					pbw.open(ureq);
					//
				} else if (actionid.equals(COMMAND_SELECTUSER)) {
					fireEvent(ureq, new SingleIdentityChosenEvent(identity));
				} else if (COMMAND_IM.equals(actionid)) {
					doIm(ureq, identity);
				}
			} else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				// Multiselect events
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals(COMMAND_REMOVEUSER)) {
					if(tmse.getSelection().isEmpty()){
						//empty selection 
						showWarning("msg.selectionempty");
						return;
					}
					int size = identitiesTableModel.getObjects().size(); 
					toRemove = identitiesTableModel.getIdentities(tmse.getSelection());
					// list is never null, but can be empty
					if (keepAtLeastOne && (size == 1 || size - toRemove.size() == 0)) {
						//at least one must be kept
						//do not delete the last one => ==1
						//do not allow to delete all => size - selectedCnt == 0
						showError("msg.atleastone");
					} else {
						//valid selection to be deleted.
						if (removeUserMailDefaultTempl == null) {
							doBuildConfirmDeleteDialog(ureq);
						} else {
							removeAsListenerAndDispose(removeUserMailCtr);
							removeUserMailCtr = new MailNotificationEditController(getWindowControl(), ureq, removeUserMailDefaultTempl, true, false);							
							listenTo(removeUserMailCtr);
							
							removeAsListenerAndDispose(cmc);
							cmc = new CloseableModalController(getWindowControl(), translate("close"), removeUserMailCtr.getInitialComponent());
							listenTo(cmc);
							
							cmc.activate();
						}
					}
				}
			}
		} else if (sourceController == removeUserMailCtr) {
			if (event == Event.DONE_EVENT) {
				removeUserMailCustomTempl = removeUserMailCtr.getMailTemplate();
				cmc.deactivate();
				doBuildConfirmDeleteDialog(ureq);
			} else {
				cmc.deactivate();
			}
		} else if (sourceController == usc) {
			if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			} else {
				if (event instanceof SingleIdentityChosenEvent) {
					SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent) event;
					Identity choosenIdentity = singleEvent.getChosenIdentity();
					if (choosenIdentity == null) {
						showError("msg.selectionempty");
						return;
					}
					toAdd = new ArrayList<Identity>();
					toAdd.add(choosenIdentity);
				} else if (event instanceof MultiIdentityChosenEvent) {
					MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent) event;
					toAdd = multiEvent.getChosenIdentities();
					if (toAdd.size() == 0) {
						showError("msg.selectionempty");
						return;
					}
				} else {
					throw new RuntimeException("unknown event ::" + event.getCommand());
				}
				
				if (toAdd.size() == 1) {
					//check if already in group [makes only sense for a single choosen identity]
					if (securityManager.isIdentityInSecurityGroup(toAdd.get(0), securityGroup)) {
						getWindowControl().setInfo(translate("msg.subjectalreadyingroup", new String[]{toAdd.get(0).getName()}));
						return;
					}
				} else if (toAdd.size() > 1) {
					//check if already in group
					List<Identity> alreadyInGroup = new ArrayList<Identity>();
					for (int i = 0; i < toAdd.size(); i++) {
						if (securityManager.isIdentityInSecurityGroup(toAdd.get(i), securityGroup)) {
							tableCtr.setMultiSelectSelectedAt(i, false);
							alreadyInGroup.add(toAdd.get(i));
						}
					}
					if (!alreadyInGroup.isEmpty()) {
						StringBuilder names = new StringBuilder();
						for(Identity ident: alreadyInGroup) {
							names.append(" ").append(ident.getName());
							toAdd.remove(ident);
						}
						getWindowControl().setInfo(translate("msg.subjectsalreadyingroup", names.toString()));
					}
					if (toAdd.isEmpty()) {
						return;
					}
				}
				
				// in both cases continue adding the users or asking for the mail
				// template if available (=not null)
				cmc.deactivate();
				if (addUserMailDefaultTempl == null) {
					doAddIdentitiesToGroup(ureq, toAdd, null);
				} else {
					removeAsListenerAndDispose(addUserMailCtr);
					addUserMailCtr = new MailNotificationEditController(getWindowControl(), ureq, addUserMailDefaultTempl, true, mandatoryEmail);					
					listenTo(addUserMailCtr);
					
					removeAsListenerAndDispose(cmc);
					cmc = new CloseableModalController(getWindowControl(), translate("close"), addUserMailCtr.getInitialComponent());
					listenTo(cmc);
					
					cmc.activate();
				}
			}
			// in any case cleanup this controller, not used anymore
			usc.dispose();
			usc = null;

		} else if (sourceController == addUserMailCtr) {
			if (event == Event.DONE_EVENT) {
				MailTemplate customTemplate = addUserMailCtr.getMailTemplate();
				doAddIdentitiesToGroup(ureq, toAdd, customTemplate);
				cmc.deactivate();
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			} else {
				throw new RuntimeException("unknown event ::" + event.getCommand());
			}

		} else if (sourceController == confirmDelete) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				// before deleting, assure it is allowed
				if (!mayModifyMembers) throw new AssertException("not allowed to remove member!");
				// list is never null, but can be empty
				// TODO: Theoretically it can happen that the table model here is not accurate!
				// the 'keep at least one' should be handled by the security manager that should 
				// synchronizes the method on the group
				int size = identitiesTableModel.getObjects().size(); 
				if (keepAtLeastOne && (size - toRemove.size() == 0)) {
					showError("msg.atleastone");
				} else {
					doRemoveIdentitiesFromGroup(ureq, toRemove, removeUserMailCustomTempl);
				}
			}

		} else if (sourceController == userToGroupWizardCtr) {
			if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent) event;
				List<Identity> choosenIdentities = multiEvent.getChosenIdentities();
				MailTemplate customTemplate = multiEvent.getMailTemplate();
				if (choosenIdentities.size() == 0) {
					showError("msg.selectionempty");
					return;
				}
				doAddIdentitiesToGroup(ureq, choosenIdentities, customTemplate);

			} else if (event == Event.CANCELLED_EVENT) {
				// nothing special to do
			}
			
			// else cancelled
			cmc.deactivate();

		} 
	}
	
	private void doIm(UserRequest ureq, Identity identity) {
		Buddy buddy = imService.getBuddyById(identity.getKey());
		OpenInstantMessageEvent e = new OpenInstantMessageEvent(ureq, buddy);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
	}

	private void doBuildConfirmDeleteDialog(UserRequest ureq) {
		if (confirmDelete != null) confirmDelete.dispose();
		StringBuilder names = new StringBuilder();
		for (Identity identity : toRemove) {
			names.append(identity.getName()).append(" ");
		}
		confirmDelete = activateYesNoDialog(ureq, null, translate("remove.text", names.toString()), confirmDelete);
		return;
	}

	private void doRemoveIdentitiesFromGroup(UserRequest ureq, List<Identity> toBeRemoved, MailTemplate mailTemplate) {
		fireEvent(ureq, new IdentitiesRemoveEvent(toBeRemoved));
		identitiesTableModel.remove(toBeRemoved);
		if(tableCtr != null){
			// can be null in the follwoing case.
			// the user which does the removal is also in the list of toBeRemoved
			// hence the fireEvent does trigger a disposal of a GroupController, which
			// in turn nullifies the tableCtr... see also OLAT-3331
			tableCtr.modelChanged();
		}

		// send the notification mail
		if (mailTemplate != null) {
			MailerWithTemplate mailer = MailerWithTemplate.getInstance();
			Identity sender = null; // means no sender in footer
			if (this.showSenderInRemovMailFooter) {
				sender = ureq.getIdentity();
			}
			List<Identity> ccIdentities = new ArrayList<Identity>();
			if(mailTemplate.getCpfrom()) {
				ccIdentities.add(ureq.getIdentity());// add sender to cc-list
			} else {
				ccIdentities = null;	
			}
			//fxdiff VCRP-16: intern mail system
			MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
			MailerResult mailerResult = mailer.sendMailAsSeparateMails(context, toBeRemoved, ccIdentities, mailTemplate, sender);
			MailHelper.printErrorsAndWarnings(mailerResult, getWindowControl(), ureq.getLocale());
		}
	}

	/**
	 * Add users from the identites array to the group if they are not guest users
	 * and not already in the group
	 * 
	 * @param ureq
	 * @param choosenIdentities
	 */
	private void doAddIdentitiesToGroup(UserRequest ureq, List<Identity> choosenIdentities, MailTemplate mailTemplate) {
		// additional security check
		if (!mayModifyMembers) throw new AssertException("not allowed to add member!");

		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(choosenIdentities);
		// process workflow to BusinessGroupManager via BusinessGroupEditController
		fireEvent(ureq, identitiesAddedEvent); 
		if (!identitiesAddedEvent.getAddedIdentities().isEmpty()) {
  		// update table model
			List<Identity> addedIdentities = identitiesAddedEvent.getAddedIdentities();
			List<GroupMemberView> newMembers = new ArrayList<GroupMemberView>(addedIdentities.size());
			Date currentDate = new Date();
			for(Identity addedIdentity:addedIdentities) {
				newMembers.add(forgeGroupMemberView(addedIdentity, currentDate));
			}
      identitiesTableModel.add(newMembers);
			tableCtr.modelChanged();			
		}
		// build info message for identities which could be added.
		StringBuilder infoMessage = new StringBuilder();
		for (Identity identity : identitiesAddedEvent.getIdentitiesWithoutPermission()) {
	    infoMessage.append(translate("msg.isingroupanonymous", identity.getName())).append("<br />");
		}
		for (Identity identity : identitiesAddedEvent.getIdentitiesAlreadyInGroup()) {
			infoMessage.append(translate("msg.subjectalreadyingroup", identity.getName())).append("<br />");
		}
		// send the notification mail fro added users
		StringBuilder errorMessage = new StringBuilder();
		if (mailTemplate != null) {
			MailerWithTemplate mailer = MailerWithTemplate.getInstance();
			Identity sender = null; // means no sender in footer
			if (this.showSenderInAddMailFooter) {
				sender = ureq.getIdentity();
			}
			List<Identity> ccIdentities = new ArrayList<Identity>();
			if(mailTemplate.getCpfrom()) {
				ccIdentities.add(ureq.getIdentity());// add sender to cc-list
			} else {
				ccIdentities = null;	
			}
			//fxdiff VCRP-16: intern mail system
			MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
			MailerResult mailerResult = mailer.sendMailAsSeparateMails(context, identitiesAddedEvent.getAddedIdentities(), ccIdentities, mailTemplate, sender);
			MailHelper.appendErrorsAndWarnings(mailerResult, errorMessage, infoMessage, ureq.getLocale());
		}
		// report any errors on screen
		if (infoMessage.length() > 0) getWindowControl().setWarning(infoMessage.toString());
		if (errorMessage.length() > 0) getWindowControl().setError(errorMessage.toString());
	}
	
	private GroupMemberView forgeGroupMemberView(Identity identity, Date addedAt) {
		String onlineStatus;
		if(getIdentity().equals(identity)) {
			onlineStatus = "me";
		} else if(sessionManager.isOnline(identity.getKey())) {
			onlineStatus = Presence.available.name();
		} else {
			onlineStatus = Presence.unavailable.name();
		}
		return new GroupMemberView(identity, addedAt, onlineStatus);
	}

	protected void doDispose() {
    // DialogBoxController and TableController get disposed by BasicController
		// usc, userToGroupWizardCtr, addUserMailCtr, and removeUserMailCtr are registerd with listenTo and get disposed in BasicController
		super.doPreDispose();		
	}

	/**
	 * Init GroupList-table-controller for non-waitinglist (participant-list,
	 * owner-list).
	 */
	protected void initGroupTable(TableController tableCtr, UserRequest ureq, boolean enableTablePreferences, boolean enableUserSelection) {			
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		if (isAdministrativeUser) {
			// first the login name, but only if administrative user
			DefaultColumnDescriptor cd0 = new DefaultColumnDescriptor("table.user.login", 0, COMMAND_VCARD, ureq.getLocale());
			cd0.setIsPopUpWindowAction(true, "height=700, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
			tableCtr.addColumnDescriptor(cd0);
		}
		if (imModule.isEnabled() && imModule.isViewOnlineUsersEnabled()) {
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.online", 1, COMMAND_IM, getLocale(),
					ColumnDescriptor.ALIGNMENT_LEFT, new OnlineIconRenderer()));
		}
		
		int visibleColId = 0;
		// followed by the users fields
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			ColumnDescriptor cd = userPropertyHandler.getColumnDescriptor(i + 3, COMMAND_VCARD, ureq.getLocale());
			// make all user attributes clickable to open visiting card
			if (cd instanceof DefaultColumnDescriptor) {
				DefaultColumnDescriptor dcd = (DefaultColumnDescriptor) cd;
				dcd.setIsPopUpWindowAction(true, "height=700, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
				
			}
			tableCtr.addColumnDescriptor(visible, cd);
			if (visible) {
				visibleColId++;
			}
		}
		
		// in the end
		if (enableTablePreferences) {
			tableCtr.addColumnDescriptor(true, new DefaultColumnDescriptor("table.subject.addeddate", 2, COMMAND_VCARD, ureq.getLocale()));
			tableCtr.setSortColumn(++visibleColId,true);	
		}
		if (enableUserSelection) {
			tableCtr.addColumnDescriptor(new StaticColumnDescriptor(COMMAND_SELECTUSER, "table.subject.action", myTrans.translate("action.general")));
		}
		if (mayModifyMembers) {
			tableCtr.addMultiSelectAction("action.remove", COMMAND_REMOVEUSER);
			tableCtr.setMultiSelect(true);
		}
	}

	public void reloadData() {
		// refresh view		
		List<Object[]> combo = securityManager.getIdentitiesAndDateOfSecurityGroup(securityGroup); 
		List<GroupMemberView> views = new ArrayList<GroupMemberView>(combo.size());
		for(Object[] co:combo) {
			views.add(forgeGroupMemberView((Identity)co[0], (Date)co[1]));
		}
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		identitiesTableModel = new IdentitiesOfGroupTableDataModel(views, getLocale(), userPropertyHandlers, isAdministrativeUser);
		tableCtr.setTableDataModel(identitiesTableModel);
	}
}
