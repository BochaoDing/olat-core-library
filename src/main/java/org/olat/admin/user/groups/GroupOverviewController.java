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
package org.olat.admin.user.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalWindowWrapperController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.AddToGroupsEvent;
import org.olat.group.model.BGMembership;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.main.BGRoleCellRenderer;
import org.olat.group.ui.main.BGTableItem;
import org.olat.group.ui.main.BusinessGroupTableModelWithType;
import org.olat.group.ui.main.BusinessGroupTableModelWithType.Cols;

/**
 * Description:<br>
 * GroupOverviewController creates a model and displays a table with all groups a user is in.
 * The following rows are shown: type of group, groupname, 
 * role of user in group (participant, owner, on waiting list), date of joining the group
 * 
 * <P>
 * Initial Date:  22.09.2008 <br>
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GroupOverviewController extends BasicController {
	private static final String TABLE_ACTION_LAUNCH = "bgTblLaunch";
	private static final String TABLE_ACTION_UNSUBSCRIBE = "unsubscribe";
	
	private VelocityContainer vc;
	private TableController groupListCtr;
	private BusinessGroupTableModelWithType tableDataModel;
	
	private Link addGroups;
	private CloseableModalController cmc;
	private CloseableModalWindowWrapperController calloutCtrl;
	private GroupSearchController groupsCtrl;
	private GroupLeaveDialogBoxController removeFromGrpDlg;

	private final BaseSecurity securityManager;
	private final BusinessGroupService businessGroupService;
	
	private final Identity identity;

	public GroupOverviewController(UserRequest ureq, WindowControl control, Identity identity, Boolean canStartGroups) {
		super(ureq, control, Util.createPackageTranslator(BusinessGroupTableModelWithType.class, ureq.getLocale()));
		
		this.identity = identity;
		securityManager = BaseSecurityManager.getInstance();
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);

		vc = createVelocityContainer("groupoverview");
		
		groupListCtr = new TableController(null, ureq, control, getTranslator());
		listenTo(groupListCtr);
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.name.i18n(), Cols.name.ordinal(), canStartGroups ? TABLE_ACTION_LAUNCH : null, ureq.getLocale()));
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.key.i18n(), Cols.key.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.firstTime.i18n(), Cols.firstTime.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.lastTime.i18n(), Cols.lastTime.ordinal(), null, getLocale()));
		CustomCellRenderer roleRenderer = new BGRoleCellRenderer(getLocale());
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.role.i18n(), Cols.role.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, roleRenderer));
		groupListCtr.addColumnDescriptor(new BooleanColumnDescriptor(Cols.allowLeave.i18n(), Cols.allowLeave.ordinal(), TABLE_ACTION_UNSUBSCRIBE, translate("table.header.leave"), null));
		
		groupListCtr.setMultiSelect(true);
		groupListCtr.addMultiSelectAction("table.leave", TABLE_ACTION_UNSUBSCRIBE);
		tableDataModel = new BusinessGroupTableModelWithType(getTranslator(), 4);
		groupListCtr.setTableDataModel(tableDataModel);
		
		updateModel(ureq);
		
		addGroups = LinkFactory.createButton("add.groups", vc, this);		
		vc.put("table.groups", groupListCtr.getInitialComponent());	
		putInitialPanel(vc);
	}

	/**
	 * @param ureq
	 * @param control
	 * @param identity
	 * @return
	 */
	private void updateModel(UserRequest ureq) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setIdentity(identity);
		params.setOwner(true);
		params.setAttendee(true);
		params.setWaiting(true);
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		List<Long> groupKeysWithMembers;
		if(groups.size() > 50) {
			groupKeysWithMembers = null;
		} else {
			groupKeysWithMembers = new ArrayList<Long>(groups.size());
			for(BusinessGroup view:groups) {
				groupKeysWithMembers.add(view.getKey());
			}
		}

		//retrieve all user's membership if there are more than 50 groups
		List<BusinessGroupMembership> groupsAsOwner = businessGroupService.getBusinessGroupMembership(identity, groupKeysWithMembers);
		Map<Long, BusinessGroupMembership> memberships = new HashMap<Long, BusinessGroupMembership>();
		for(BusinessGroupMembership membership: groupsAsOwner) {
			if(membership.getOwnerGroupKey() != null) {
				memberships.put(membership.getOwnerGroupKey(), membership);
			}
			if(membership.getParticipantGroupKey() != null) {
				memberships.put(membership.getParticipantGroupKey(), membership);
			}
			if(membership.getWaitingGroupKey() != null) {
				memberships.put(membership.getWaitingGroupKey(), membership);
			}
		}

		List<BGTableItem> items = new ArrayList<BGTableItem>();
		for(BusinessGroup group:groups) {
			BusinessGroupMembership membership =  memberships.get(group.getKey());
			BGMembership member = membership == null ? null : membership.getMembership();
			BGTableItem tableItem = new BGTableItem(group, false, member, Boolean.TRUE, Boolean.FALSE, null);
			items.add(tableItem);
		}
		tableDataModel.setEntries(items, memberships);
		groupListCtr.modelChanged();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(	UserRequest ureq, Component source, Event event) {
		if (source == addGroups){
			groupsCtrl = new GroupSearchController(ureq, getWindowControl());
			listenTo(groupsCtrl);
			
			calloutCtrl = new CloseableModalWindowWrapperController(ureq, getWindowControl(), translate("add.groups"), groupsCtrl.getInitialComponent(), "ccgroupadd");
			calloutCtrl.setInitialWindowSize(500, 400);
//			calloutCtrl = new CloseableCalloutWindowController(ureq, wControl, groupsCtrl.getInitialComponent(), addGroups, translate("add.groups"), false, null);
			listenTo(calloutCtrl);
			calloutCtrl.activate();
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == groupListCtr){
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				BGTableItem item = tableDataModel.getObject(te.getRowId());
				BusinessGroup currBusinessGroup = businessGroupService.loadBusinessGroup(item.getBusinessGroupKey());
				if (currBusinessGroup==null) {
					//group seems to be removed meanwhile, reload table and show error
					showError("group.removed");
					updateModel(ureq);	
				} else if (TABLE_ACTION_LAUNCH.equals(te.getActionId())) {
					NewControllerFactory.getInstance().launch("[BusinessGroup:" + currBusinessGroup.getKey() + "]", ureq, getWindowControl());
				} else if (TABLE_ACTION_UNSUBSCRIBE.equals(te.getActionId())){
					doLeave(ureq, Collections.singletonList(currBusinessGroup));
				}
			} else if (event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent mse = (TableMultiSelectEvent)event;
				List<BGTableItem> items = tableDataModel.getObjects(mse.getSelection());
				if (TABLE_ACTION_UNSUBSCRIBE.equals(mse.getAction())){
					List<BusinessGroup> groups = toBusinessGroups(items);
					doLeave(ureq, groups);
				}
			}
		}	else if (source == groupsCtrl && event instanceof AddToGroupsEvent){
			AddToGroupsEvent groupsEv = (AddToGroupsEvent) event;
			if (groupsEv.getOwnerGroupKeys().isEmpty() && groupsEv.getParticipantGroupKeys().isEmpty()){
				// no groups selected
				showWarning("group.add.result.none");
			} else {
				if (calloutCtrl != null) calloutCtrl.deactivate();				
				String[] resultTextArgs = businessGroupService.addIdentityToGroups(groupsEv, identity, getIdentity());
				if (resultTextArgs != null){
					String message = translate("group.add.result", resultTextArgs);
					getWindowControl().setInfo(message);
				} else {		
					showWarning("group.add.result.none");
				}
				updateModel(ureq);
			}			
		} else if (source == removeFromGrpDlg){
			if(event == Event.DONE_EVENT) {
				boolean sendMail = removeFromGrpDlg.isSendMail();
				List<BusinessGroup> groupsToDelete = removeFromGrpDlg.getGroupsToDelete();
				List<BusinessGroup> groupsToLeave = removeFromGrpDlg.getGroupsToLeave();
				removeUserFromGroup(ureq, groupsToLeave, groupsToDelete, sendMail);
			}
			cmc.deactivate();
			cleanUpPopups();
		} else if (source == cmc) {
			cleanUpPopups();
		}
	}
	
	private void cleanUpPopups() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(removeFromGrpDlg);
		cmc = null;
		removeFromGrpDlg = null;
	}
	
	private void doLeave(UserRequest ureq, List<BusinessGroup> groupsToLeave) {
		List<BusinessGroup> groupsToDelete = new ArrayList<BusinessGroup>(1);
		for(BusinessGroup group:groupsToLeave) {
			int numOfOwners = securityManager.countIdentitiesOfSecurityGroup(group.getOwnerGroup());
			int numOfParticipants = securityManager.countIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
			if ((numOfOwners == 1 && numOfParticipants == 0) || (numOfOwners == 0 && numOfParticipants == 1)) {
				groupsToDelete.add(group);
			}
		}
		removeFromGrpDlg = new GroupLeaveDialogBoxController(ureq, getWindowControl(), identity, groupsToLeave, groupsToDelete);
		listenTo(removeFromGrpDlg);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), removeFromGrpDlg.getInitialComponent(),
				true, "");
		cmc.activate();
		listenTo(cmc);
	}

	/**
	 * 
	 * @param ureq
	 * @param doSendMail
	 */
	private void removeUserFromGroup(UserRequest ureq, List<BusinessGroup> groupsToLeave, List<BusinessGroup> groupsToDelete, boolean doSendMail) {
		for(BusinessGroup group:groupsToLeave) {
			if (groupsToDelete.contains(group)) {
				// really delete the group as it has no more owners/participants
				if(doSendMail) {
					String businessPath = getWindowControl().getBusinessControl().getAsString();
					businessGroupService.deleteBusinessGroupWithMail(group, businessPath, getIdentity(), getLocale());
				} else {
					businessGroupService.deleteBusinessGroup(group);
				}
			} else {
				// 1) remove as owner
				if (securityManager.isIdentityInSecurityGroup(identity, group.getOwnerGroup())) {
					businessGroupService.removeOwners(ureq.getIdentity(), Collections.singletonList(identity), group);
				}
				// 2) remove as participant
				final BusinessGroup toRemFromGroup = group;
				//TODO gsync
				CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerExecutor() {
					public void execute() {
						businessGroupService.removeParticipant(getIdentity(), identity, toRemFromGroup);
					}
				});
	
				// 3) notify user about this action:
				if(doSendMail){
					MailTemplate mailTemplate = BGMailHelper.createRemoveParticipantMailTemplate(group, getIdentity());
					MailerWithTemplate mailer = MailerWithTemplate.getInstance();
					MailerResult mailerResult = mailer.sendMail(null, identity, null, null, mailTemplate, null);
					MailHelper.printErrorsAndWarnings(mailerResult, getWindowControl(), getLocale());
				}
			}
		}

		updateModel(ureq);

		StringBuilder groupNames = new StringBuilder();
		for(BusinessGroup group:groupsToLeave) {
			if(groupNames.length() > 0) groupNames.append(", ");
			groupNames.append(group.getName());
		}
		showInfo("unsubscribe.successful", groupNames.toString());	
	}
	
	private List<BusinessGroup> toBusinessGroups(List<BGTableItem> items) {
		List<Long> groupKeys = new ArrayList<Long>();
		for(BGTableItem item:items) {
			groupKeys.add(item.getBusinessGroupKey());
		}
		List<BusinessGroup> groups = businessGroupService.loadBusinessGroups(groupKeys);
		return groups;
	}
}
