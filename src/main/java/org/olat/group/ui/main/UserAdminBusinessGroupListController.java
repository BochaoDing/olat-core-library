package org.olat.group.ui.main;

import org.olat.admin.user.groups.GroupSearchController;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.*;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.model.AddToGroupsEvent;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.BusinessGroupRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 *
 * @author Martin Schraner, UZH
 */
public class UserAdminBusinessGroupListController extends AbstractBusinessGroupListController {

	private static final String TABLE_ACTION_ADD_TO_GROUPS = "bgTblAddToGroups";

	private Identity userIdentity;
    private FormLink leaveSelectedGroupsButton;
	private FormLink addToGroupButton;
	private GroupSearchController groupSearchController;
	private DialogBoxController confirmSendMailBoxController;
    private GroupLeaveDialogBoxController groupLeaveDialogBoxController;

	public UserAdminBusinessGroupListController(UserRequest ureq, WindowControl wControl, String prefsKey, Identity userIdentity) {
		super(ureq, wControl, "user_admin_business_group_list", prefsKey);
		this.userIdentity = userIdentity;
	}

	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		addToGroupButton = uifactory.addFormLink("add.to.groups", TABLE_ACTION_ADD_TO_GROUPS, "add.to.groups", null, formLayout, Link.BUTTON);
		leaveSelectedGroupsButton = uifactory.addFormLink("table.leave", TABLE_ACTION_LEAVE, "table.leave", null, formLayout, Link.BUTTON);
	}

	@Override
	protected FlexiTableColumnModel initColumnModel() {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		//group name
//		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel(BusinessGroupListFlexiTableModel.Cols.name.i18n(), BusinessGroupListFlexiTableModel.Cols.name.ordinal(), TABLE_ACTION_LAUNCH,
//				true, BusinessGroupListFlexiTableModel.Cols.name.name(), new StaticFlexiCellRenderer(TABLE_ACTION_LAUNCH, new BusinessGroupNameCellRenderer())));
		//id and reference
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BusinessGroupListFlexiTableModel.Cols.key.i18n(), BusinessGroupListFlexiTableModel.Cols.key.ordinal(), true, BusinessGroupListFlexiTableModel.Cols.key.name()));
		if(groupModule.isManagedBusinessGroups()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BusinessGroupListFlexiTableModel.Cols.externalId.i18n(), BusinessGroupListFlexiTableModel.Cols.externalId.ordinal(),
					true, BusinessGroupListFlexiTableModel.Cols.externalId.name()));
		}
		//description
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BusinessGroupListFlexiTableModel.Cols.description.i18n(), BusinessGroupListFlexiTableModel.Cols.description.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		//courses
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, BusinessGroupListFlexiTableModel.Cols.resources.i18n(), BusinessGroupListFlexiTableModel.Cols.resources.ordinal(),
				true, BusinessGroupListFlexiTableModel.Cols.resources.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGResourcesCellRenderer(flc)));
		//launch dates
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, BusinessGroupListFlexiTableModel.Cols.firstTime.i18n(), BusinessGroupListFlexiTableModel.Cols.firstTime.ordinal(),
				true, BusinessGroupListFlexiTableModel.Cols.firstTime.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, BusinessGroupListFlexiTableModel.Cols.lastTime.i18n(), BusinessGroupListFlexiTableModel.Cols.lastTime.ordinal(),
				true, BusinessGroupListFlexiTableModel.Cols.lastTime.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BusinessGroupListFlexiTableModel.Cols.lastUsage.i18n(), BusinessGroupListFlexiTableModel.Cols.lastUsage.ordinal(),
				true, BusinessGroupListFlexiTableModel.Cols.lastUsage.name()));
		//roles
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, BusinessGroupListFlexiTableModel.Cols.role.i18n(), BusinessGroupListFlexiTableModel.Cols.role.ordinal(),
				true, BusinessGroupListFlexiTableModel.Cols.role.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGRoleCellRenderer(getLocale())));

		//actions
//		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel(BusinessGroupListFlexiTableModel.Cols.allowLeave.i18n(), BusinessGroupListFlexiTableModel.Cols.allowLeave.ordinal(), TABLE_ACTION_LEAVE,
//				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.leave"), TABLE_ACTION_LEAVE), null)));
		return columnsModel;
	}

	@Override
	protected BusinessGroupQueryParams getSearchParams(SearchEvent event) {
		BusinessGroupQueryParams params = event.convertToBusinessGroupQueriesParams();
		//security
		if(!params.isAttendee() && !params.isOwner() && !params.isWaiting()) {
			params.setOwner(true);
			params.setAttendee(true);
			params.setWaiting(true);
		}
		return params;
	}

	@Override
	protected BusinessGroupQueryParams getDefaultSearchParams() {
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setAttendee(true);
		params.setOwner(true);
		params.setWaiting(true);
		return params;
	}
	
	@Override
	protected List<BGTableItem> searchTableItems(BusinessGroupQueryParams params) {
		List<BusinessGroupRow> rows = businessGroupService.findBusinessGroupsWithMemberships(params, userIdentity);
		List<BGTableItem> items = new ArrayList<>(rows.size());
		for(BusinessGroupRow row:rows) {
			BusinessGroupMembership membership = row.getMember();
			Boolean allowLeave =  membership != null;
			BGTableItem item = new BGTableItem(row, null, allowLeave, false);
			items.add(item);
		}
		return items;
	}

	@Override
	protected void cleanUpPopups() {
		super.cleanUpPopups();
		removeAsListenerAndDispose(groupSearchController);
        removeAsListenerAndDispose(confirmSendMailBoxController);
        removeAsListenerAndDispose(groupLeaveDialogBoxController);
        groupSearchController = null;
        confirmSendMailBoxController = null;
        groupLeaveDialogBoxController = null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == groupSearchController && event instanceof AddToGroupsEvent) {
			AddToGroupsEvent addToGroupsEvent = (AddToGroupsEvent) event;
			if (addToGroupsEvent.isEmpty()) {
				// no groups selected
				showWarning("error.add.to.groups.result.none");
			} else {
				if (cmc != null) {
					cmc.deactivate();
				}
				boolean mailMandatory = groupModule.isMandatoryEnrolmentEmail(ureq.getUserSession().getRoles());
				if (mailMandatory) {
					doAddToGroups(addToGroupsEvent, true);
				} else {
					confirmSendMailBoxController = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.send.mail"), confirmSendMailBoxController);
					confirmSendMailBoxController.setUserObject(addToGroupsEvent);
				}
			}
		} else if (source == confirmSendMailBoxController) {
			boolean sendMail = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			AddToGroupsEvent groupsEv = (AddToGroupsEvent) confirmSendMailBoxController.getUserObject();
			doAddToGroups(groupsEv, sendMail);
            cleanUpPopups();
		} else if (source == groupLeaveDialogBoxController){
            if (event == Event.DONE_EVENT) {
                boolean sendMail = groupLeaveDialogBoxController.isSendMail();
                List<BusinessGroup> groupsToDelete = groupLeaveDialogBoxController.getGroupsToDelete();
                List<BusinessGroup> groupsToLeave = groupLeaveDialogBoxController.getGroupsToLeave();
                doLeave(groupsToLeave, groupsToDelete, sendMail);
            }
            cmc.deactivate();
            cleanUpPopups();
        }
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        super.formInnerEvent(ureq, source, event);
		if (source == addToGroupButton) {
			doSearchGroup(ureq);
		} else if (source == leaveSelectedGroupsButton) {
            List<BusinessGroup> groupsToLeave = toBusinessGroups(ureq, getSelectedItems(), true);
            if (groupsToLeave == null || groupsToLeave.isEmpty()) {
                showWarning("error.select.one");
                return;
            }
            doConfirmLeaving(ureq, groupsToLeave);
        }
	}

	private void doSearchGroup(UserRequest ureq) {
		groupSearchController = new GroupSearchController(ureq, getWindowControl());
		listenTo(groupSearchController);

		cmc = new CloseableModalController(getWindowControl(), translate("add.to.groups"), groupSearchController.getInitialComponent(), true, translate("add.to.groups"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddToGroups(AddToGroupsEvent e, boolean sendMail) {
		List<BusinessGroupMembershipChange> changes = new ArrayList<>();
		if (e.getOwnerGroupKeys() != null && !e.getOwnerGroupKeys().isEmpty()) {
			for(Long tutorGroupKey:e.getOwnerGroupKeys()) {
				BusinessGroupMembershipChange change = new BusinessGroupMembershipChange(userIdentity, tutorGroupKey);
				change.setTutor(Boolean.TRUE);
				changes.add(change);
			}
		}
		if (e.getParticipantGroupKeys() != null && !e.getParticipantGroupKeys().isEmpty()) {
			for(Long partGroupKey:e.getParticipantGroupKeys()) {
				BusinessGroupMembershipChange change = new BusinessGroupMembershipChange(userIdentity, partGroupKey);
				change.setParticipant(Boolean.TRUE);
				changes.add(change);
			}
		}
		MailPackage mailing = new MailPackage(sendMail);
		businessGroupService.updateMemberships(userIdentity, changes, mailing);
        reloadModel();
	}

	@Override
    protected void doConfirmLeaving(UserRequest ureq, BusinessGroup groupToLeave) {
        doConfirmLeaving(ureq, Collections.singletonList(groupToLeave));
    }

    private void doConfirmLeaving(UserRequest ureq, List<BusinessGroup> groupsToLeave) {
        List<BusinessGroup> groupsToDelete = new ArrayList<>(1);
        for (BusinessGroup group : groupsToLeave) {
            int numOfOwners = businessGroupService.countMembers(group, GroupRoles.coach.name());
            int numOfParticipants = businessGroupService.countMembers(group, GroupRoles.participant.name());
            if ((numOfOwners == 1 && numOfParticipants == 0) || (numOfOwners == 0 && numOfParticipants == 1)) {
                // Delete group in case it would be empty afterwards
                groupsToDelete.add(group);
            }
        }
        groupLeaveDialogBoxController = new GroupLeaveDialogBoxController(ureq, getWindowControl(), userIdentity, groupsToLeave, groupsToDelete);
        listenTo(groupLeaveDialogBoxController);

        cmc = new CloseableModalController(getWindowControl(), translate("close"), groupLeaveDialogBoxController.getInitialComponent(),
                true, translate("unsubscribe.group.title"));
        cmc.activate();
        listenTo(cmc);
    }

    private void doLeave(List<BusinessGroup> groupsToLeave, List<BusinessGroup> groupsToDelete, boolean doSendMail) {
        for (BusinessGroup group : groupsToLeave) {
            if (groupsToDelete.contains(group)) {
                // really delete the group as it has no more owners/participants
                if (doSendMail) {
                    String businessPath = getWindowControl().getBusinessControl().getAsString();
                    businessGroupService.deleteBusinessGroupWithMail(group, businessPath, userIdentity, getLocale());
                } else {
                    businessGroupService.deleteBusinessGroup(group);
                }
            } else {
                // 1) remove as owner
                if (businessGroupService.hasRoles(userIdentity, group, GroupRoles.coach.name())) {
                    businessGroupService.removeOwners(userIdentity, Collections.singletonList(userIdentity), group);
                }
                MailPackage mailing = new MailPackage(doSendMail);
                // 2) remove as participant
                businessGroupService.removeParticipants(userIdentity, Collections.singletonList(userIdentity), group, mailing);
                MailHelper.printErrorsAndWarnings(mailing.getResult(), getWindowControl(), getLocale());
            }
        }
        reloadModel();
    }

}