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
 * <p>
 */
package ch.uzh.campus.presentation;

import ch.uzh.campus.service.learn.CampusCourseService;
import ch.uzh.campus.service.learn.impl.CampusCourseServiceImpl;
import org.olat.admin.securitygroup.gui.GroupMemberView;
import org.olat.admin.securitygroup.gui.IdentitiesOfGroupTableDataModel;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.*;
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
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Initial Date: 02.05.2013 <br>
 * 
 * @author aabouc
 */
public class DelegationController extends BasicController {

    private VelocityContainer myContent;
    private Link addUserButton;

    protected TableController tableCtr;
    private Translator myTrans;

    private CloseableModalController cmc;
    private UserSearchController usc;

    private List<Identity> toAdd, toRemove;

    private CampusCourseService campusService;
    private UserManager userManager;

    private Identity delegator;

    private IdentitiesOfGroupTableDataModel identitiesTableModel;

    private DialogBoxController confirmDelete;

    protected static final String usageIdentifyer = IdentitiesOfGroupTableDataModel.class.getCanonicalName();
    protected boolean isAdministrativeUser;

    protected static final String COMMAND_REMOVEUSER = "removesubjectofgroup";
    protected static final String COMMAND_VCARD = "show.vcard";
    protected static final String COMMAND_SELECTUSER = "select.user";

    public DelegationController(final UserRequest ureq, final WindowControl wControl, final Identity delegator) {
        super(ureq, wControl);

        campusService = (CampusCourseServiceImpl) CoreSpringFactory
				.getBean(CampusCourseServiceImpl.class);
        userManager = UserManager.getInstance();
        this.delegator = delegator;

        myContent = createVelocityContainer("delegation");
        addUserButton = LinkFactory.createButtonSmall("delegation.add.user", myContent, this);

        myTrans = userManager.getUserPropertiesConfig().getTranslator(getTranslator());
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setPreferencesOffered(true, "DelegationTableGuiPrefs");
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), myTrans);

        listenTo(tableCtr);

        initGroupTable(tableCtr, ureq, true, false);
        final List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
        final List combo = campusService.getDelegatees(this.delegator);

        List<GroupMemberView> views = new ArrayList<GroupMemberView>(combo.size());
        for (Object identityObject:combo) {
            Object[] IdentityInfo = (Object[]) identityObject;
            Identity identity = (Identity) IdentityInfo[0];
            Date addedAt = (Date) IdentityInfo[1];
            String onlineStatus = null;
            GroupMemberView member = new GroupMemberView(identity, addedAt, onlineStatus);
            views.add(member);
        }

        identitiesTableModel = new DelegationIdentitiesOfGroupTableDataModel(views, ureq.getLocale(), userPropertyHandlers, true);
        tableCtr.setTableDataModel(identitiesTableModel);
        myContent.put("subjecttable", tableCtr.getInitialComponent());

        putInitialPanel(myContent);

    }

    protected void initGroupTable(final TableController tableCtr, final UserRequest ureq, final boolean enableTablePreferences, final boolean enableUserSelection) {
        final List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
        // first the login name
        final DefaultColumnDescriptor cd0 = new DefaultColumnDescriptor("table.user.login", 0, COMMAND_VCARD, ureq.getLocale());
        cd0.setIsPopUpWindowAction(true, "height=700, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
        tableCtr.addColumnDescriptor(cd0);
        int visibleColId = 0;
        // followed by the users fields
        for (int i = 0; i < userPropertyHandlers.size(); i++) {
            final UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
            final boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer, userPropertyHandler);
            tableCtr.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(i + 1, null, ureq.getLocale()));
            if (visible) {
                visibleColId++;
            }
        }

        // in the end
        if (enableTablePreferences) {
            tableCtr.addColumnDescriptor(true, new DefaultColumnDescriptor("table.subject.addeddate", userPropertyHandlers.size() + 1, null, ureq.getLocale()));
            tableCtr.setSortColumn(++visibleColId, true);
        }
        if (enableUserSelection) {
            tableCtr.addColumnDescriptor(new StaticColumnDescriptor(COMMAND_SELECTUSER, "table.subject.action", myTrans.translate("action.general")));
        }

        tableCtr.addMultiSelectAction("action.remove", COMMAND_REMOVEUSER);
        tableCtr.setMultiSelect(true);

    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == addUserButton) {
            usc = new UserSearchController(ureq, getWindowControl(), true, true, false);
            listenTo(usc);

            final Component usersearchview = usc.getInitialComponent();
            removeAsListenerAndDispose(cmc);

            cmc = new CloseableModalController(getWindowControl(), translate("close"), usersearchview, true, translate("delegation.add.searchuser"));
            listenTo(cmc);

            cmc.activate();
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller sourceController, final Event event) {
        if (sourceController == tableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                // Single row selects
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                if (actionid.equals(COMMAND_VCARD)) {
                    // get identitiy and open new visiting card controller in new window
                    final int rowid = te.getRowId();
                    final Identity identity = identitiesTableModel.getObject(rowid).getIdentity();
                    final ControllerCreator userInfoMainControllerCreator = new ControllerCreator() {
                        @Override
                        public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                            return new UserInfoMainController(lureq, lwControl, identity, false, false);
                        }
                    };
                    // wrap the content controller into a full header layout
                    final ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, userInfoMainControllerCreator);
                    // open in new browser window
                    final PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
                    pbw.open(ureq);
                    //
                } else if (actionid.equals(COMMAND_SELECTUSER)) {
                    final int rowid = te.getRowId();
                    final Identity identity = identitiesTableModel.getObject(rowid).getIdentity();
                    fireEvent(ureq, new SingleIdentityChosenEvent(identity));
                }

            } else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
                final TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
                if (tmse.getAction().equals(COMMAND_REMOVEUSER)) {
                    if (tmse.getSelection().isEmpty()) {
                        // empty selection
                        myContent.setDirty(true);
                        showWarning("msg.selectionempty");
                        return;
                    }
                    toRemove = identitiesTableModel.getIdentities(tmse.getSelection());
                    doBuildConfirmDeleteDialog(ureq);

                }
            }

        } else if (sourceController == usc) {
            if (event == Event.CANCELLED_EVENT) {
                cmc.deactivate();
            } else {
                if (event instanceof SingleIdentityChosenEvent) {
                    final SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent) event;
                    final Identity choosenIdentity = singleEvent.getChosenIdentity();
                    if (choosenIdentity == null) {
                        return;
                    }
                    toAdd = new ArrayList<Identity>();
                    toAdd.add(choosenIdentity);
                } else if (event instanceof MultiIdentityChosenEvent) {
                    final MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent) event;
                    toAdd = multiEvent.getChosenIdentities();
                    if (toAdd.isEmpty()) {
                        showError("msg.selectionempty");
                        return;
                    }
                }

                if (toAdd.size() == 1) {
                    // check if already in delegation [makes only sense for a single choosen identity]
                    if (campusService.existDelegation(delegator, toAdd.get(0))) {
                        getWindowControl().setInfo(translate("delegation.msg.delegateealreadyindelegation", new String[] { toAdd.get(0).getName() }));
                        return;
                    }
                } else if (toAdd.size() > 1) {
                    // check if already in group
                    boolean someAlreadyInGroup = false;
                    final List<Identity> alreadyInGroup = new ArrayList<Identity>();
                    for (int i = 0; i < toAdd.size(); i++) {
                        if (campusService.existDelegation(delegator, toAdd.get(i))) {
                            tableCtr.setMultiSelectSelectedAt(i, false);
                            alreadyInGroup.add(toAdd.get(i));
                            someAlreadyInGroup = true;
                        }
                    }
                    if (someAlreadyInGroup) {
                        String names = "";
                        for (final Identity ident : alreadyInGroup) {
                            names += " " + ident.getName();
                            toAdd.remove(ident);
                        }
                        getWindowControl().setInfo(translate("delegation.msg.delegateesalreadyindelegation", names));
                    }
                    if (toAdd.isEmpty()) {
                        return;
                    }
                }

                cmc.deactivate();
                if (toAdd != null && !toAdd.isEmpty()) {
                    for (Identity identity : toAdd) {
                        campusService.createDelegation(this.delegator, identity);
                    }
                    identitiesTableModel.add(identitiesToGroupMemberViews(toAdd));
                    tableCtr.modelChanged();
                }
            }

        } else if (sourceController == confirmDelete) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                for (Identity delegatee : toRemove) {
                    campusService.deleteDelegation(delegator, delegatee);
                }
                identitiesTableModel.remove(toRemove);
                tableCtr.modelChanged();
            }
        }
    }

    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    private List<GroupMemberView> identitiesToGroupMemberViews(List<Identity> identities) {
        List<GroupMemberView> groupMemberViews = new ArrayList<GroupMemberView>();
        for (Identity identity : identities) {
            groupMemberViews.add(new GroupMemberView(identity, new Date(), null));
        }
        return groupMemberViews;
    }

    private void doBuildConfirmDeleteDialog(final UserRequest ureq) {
        if (confirmDelete != null) {
            confirmDelete.dispose();
        }
        final StringBuilder names = new StringBuilder();
        for (final Identity identity : toRemove) {
            names.append(identity.getName()).append(" ");
        }
        // trusted text, no need to escape, identity names are safe
        confirmDelete = activateYesNoDialog(ureq, null, translate("delegation.remove.text", names.toString().trim()), confirmDelete);
    }

}
