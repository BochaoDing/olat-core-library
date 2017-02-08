package ch.uzh.extension.campuscourse.presentation.admin;

import ch.uzh.extension.campuscourse.model.IdentityDate;
import ch.uzh.extension.campuscourse.service.CampusCourseService;
import ch.uzh.extension.campuscourse.service.CampusCourseServiceImpl;
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

import java.util.*;

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
 *
 * Initial Date: 02.05.2013 <br>
 * 
 * @author aabouc
 * @author Martin Schraner
 */
public class DelegationController extends BasicController {

	private final Identity userIdentity;
	private final CampusCourseService campusService;
	private final UserManager userManager;
    private final Link addUserButton;
	private final VelocityContainer myContent;
	private final IdentitiesOfGroupTableDataModel delegateesTableDataModel;
	private final IdentitiesOfGroupTableDataModel delegatorsTableDataModel;
    private final TableController delegateesTableController;
	private final TableController delegatorsTableController;

    private CloseableModalController closeableModalController;
    private UserSearchController userSearchController;
    private List<Identity> usersToBeAdded, usersToBeRemoved;
    private DialogBoxController confirmDelete;

    private static final String USAGE_IDENTIFIER = IdentitiesOfGroupTableDataModel.class.getCanonicalName();
    private static final String COMMAND_REMOVE_USER = "removesubjectofgroup";
    private static final String COMMAND_VISIT_CARD = "show.vcard";
    private static final String COMMAND_SELECT_USER = "select.user";

    public DelegationController(UserRequest ureq, WindowControl wControl, Identity userIdentity) {

    	super(ureq, wControl);

		this.userIdentity = userIdentity;

        campusService = (CampusCourseServiceImpl) CoreSpringFactory.getBean(CampusCourseServiceImpl.class);
        userManager = UserManager.getInstance();
        myContent = createVelocityContainer("delegation");

		Translator translator = userManager.getUserPropertiesConfig().getTranslator(getTranslator());
		TableGuiConfiguration tableGuiConfiguration = new TableGuiConfiguration();
		tableGuiConfiguration.setPreferencesOffered(true, "DelegationTableGuiPrefs");
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(USAGE_IDENTIFIER, false);

		// Add "add user" button
		addUserButton = LinkFactory.createButtonSmall("delegation.add.user", myContent, this);

		// Add delegators table
		List<IdentityDate> delegateesAndCreationDate = campusService.getDelegateesAndCreationDateByDelegator(userIdentity);
		delegateesTableDataModel = createTableDataModel(ureq, delegateesAndCreationDate, userPropertyHandlers);
		delegateesTableController = createTable(ureq, delegateesTableDataModel, translator, tableGuiConfiguration,
				userPropertyHandlers, true, "delegateestable");

		// Add delegatees table
		List<IdentityDate> delegatorsAndCreationDate = campusService.getDelegatorsAndCreationDateByDelegatee(userIdentity);
		delegatorsTableDataModel = createTableDataModel(ureq, delegatorsAndCreationDate, userPropertyHandlers);
		delegatorsTableController = createTable(ureq, delegatorsTableDataModel, translator, tableGuiConfiguration,
				userPropertyHandlers, false, "delegatorstable");

        putInitialPanel(myContent);
    }

	private IdentitiesOfGroupTableDataModel createTableDataModel(UserRequest ureq,
																 List<IdentityDate> identitiesAndCreationDate,
																 List<UserPropertyHandler> userPropertyHandlers) {

		List<GroupMemberView> tableView = new ArrayList<>(identitiesAndCreationDate.size());
		for (IdentityDate delegateeAndCreationDate : identitiesAndCreationDate) {
			Identity identity = delegateeAndCreationDate.getIdentity();
			Date addedAt = delegateeAndCreationDate.getDate();
			GroupMemberView groupMemberView = new GroupMemberView(identity, addedAt, null);
			tableView.add(groupMemberView);
		}

		return new DelegationsTableDataModel(tableView, ureq.getLocale(), userPropertyHandlers, true);
	}

	private TableController createTable(UserRequest ureq,
										IdentitiesOfGroupTableDataModel tableDataModel,
										Translator translator,
										TableGuiConfiguration tableGuiConfiguration,
										List<UserPropertyHandler> userPropertyHandlers,
										boolean removeUserEnabled,
										String tableIdentifierForVelocityContainer) {

    	TableController tableController = new TableController(tableGuiConfiguration, ureq, getWindowControl(), translator);

    	// Add columns
		// First the login name ...
		DefaultColumnDescriptor defaultColumnDescriptor = new DefaultColumnDescriptor("table.user.login", 0, COMMAND_VISIT_CARD, ureq.getLocale());
		defaultColumnDescriptor.setIsPopUpWindowAction(true, "height=700, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
		tableController.addColumnDescriptor(defaultColumnDescriptor);
		int visibleColId = 0;
		// ... followed by the users fields ...
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(USAGE_IDENTIFIER, userPropertyHandler);
			tableController.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(i + 1, null, ureq.getLocale()));
			if (visible) {
				visibleColId++;
			}
		}
		// ... followed by added at
		tableController.addColumnDescriptor(true, new DefaultColumnDescriptor("table.subject.addeddate", userPropertyHandlers.size() + 1, null, ureq.getLocale()));
		tableController.setSortColumn(++visibleColId, true);
		if (removeUserEnabled) {
			tableController.addMultiSelectAction("action.remove", COMMAND_REMOVE_USER);
			tableController.setMultiSelect(true);
		}

		// Set the table data model
		tableController.setTableDataModel(tableDataModel);

		// Add listener
		listenTo(tableController);

		// Add to velocity container
		myContent.put(tableIdentifierForVelocityContainer, tableController.getInitialComponent());

		return tableController;
	}

	@Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == addUserButton) {
            userSearchController = new UserSearchController(ureq, getWindowControl(), true, true, false);
            listenTo(userSearchController);

            Component usersearchview = userSearchController.getInitialComponent();
            removeAsListenerAndDispose(closeableModalController);

            closeableModalController = new CloseableModalController(getWindowControl(), translate("close"), usersearchview, true, translate("delegation.add.searchuser"));
            listenTo(closeableModalController);

            closeableModalController.activate();
        }
    }

    @Override
    protected void event(UserRequest ureq, Controller sourceController, Event event) {
        if (sourceController == delegateesTableController || sourceController == delegatorsTableController) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                // Single row selects
                TableEvent te = (TableEvent) event;
                String actionid = te.getActionId();
                if (actionid.equals(COMMAND_VISIT_CARD)) {
                    // Get identity and open new visiting card controller in new window
                    int rowid = te.getRowId();
					Identity identity;
                    if (sourceController == delegateesTableController) {
                    	identity = delegateesTableDataModel.getObject(rowid).getIdentity();
					} else {
						identity = delegatorsTableDataModel.getObject(rowid).getIdentity();
					}
                    ControllerCreator userInfoMainControllerCreator = (userRequest, windowControl) -> new UserInfoMainController(userRequest, windowControl, identity, false, false);
                    // Wrap the content controller into a full header layout
                    ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, userInfoMainControllerCreator);
                    // Open in new browser window
                    PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
                    pbw.open(ureq);

                } else if (sourceController == delegateesTableController && actionid.equals(COMMAND_SELECT_USER)) {
                    int rowid = te.getRowId();
					Identity identity = delegateesTableDataModel.getObject(rowid).getIdentity();
                    fireEvent(ureq, new SingleIdentityChosenEvent(identity));
                }

            } else if (sourceController == delegateesTableController && event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
                TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
                if (tmse.getAction().equals(COMMAND_REMOVE_USER)) {
                    if (tmse.getSelection().isEmpty()) {
                        // Empty selection
                        myContent.setDirty(true);
                        showWarning("msg.selectionempty");
                        return;
                    }
                    usersToBeRemoved = delegateesTableDataModel.getIdentities(tmse.getSelection());
                    doBuildConfirmDeleteDialog(ureq);
                }
            }

        } else if (sourceController == userSearchController) {
            if (event == Event.CANCELLED_EVENT) {
                closeableModalController.deactivate();
            } else {
                if (event instanceof SingleIdentityChosenEvent) {
                    SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent) event;
                    Identity choosenIdentity = singleEvent.getChosenIdentity();
                    if (choosenIdentity == null) {
                        return;
                    }
                    usersToBeAdded = new ArrayList<>();
                    usersToBeAdded.add(choosenIdentity);
                } else if (event instanceof MultiIdentityChosenEvent) {
                    MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent) event;
                    usersToBeAdded = multiEvent.getChosenIdentities();
                    if (usersToBeAdded.isEmpty()) {
                        showError("msg.selectionempty");
                        return;
                    }
                }

                if (usersToBeAdded.size() == 1) {
                    // Check if already in delegation [makes only sense for a single chosen identity]
                    if (campusService.existsDelegation(userIdentity, usersToBeAdded.get(0))) {
                        getWindowControl().setInfo(translate("delegation.msg.delegateealreadyindelegation", new String[] { usersToBeAdded.get(0).getName() }));
                        return;
                    }
                    // Check if delegatee to be added is identical with user himself
					if (Objects.equals(userIdentity.getKey(), usersToBeAdded.get(0).getKey())) {
						getWindowControl().setInfo(translate("delegation.msg.usercannotbedelegateeofhimself", new String[] {}));
                    	return;
					}
                } else if (usersToBeAdded.size() > 1) {
                    // Check if already in group
                    boolean someAlreadyInGroup = false;
                    List<Identity> alreadyInGroup = new ArrayList<>();
                    for (int i = 0; i < usersToBeAdded.size(); i++) {
                        if (campusService.existsDelegation(userIdentity, usersToBeAdded.get(i))) {
                            delegateesTableController.setMultiSelectSelectedAt(i, false);
                            alreadyInGroup.add(usersToBeAdded.get(i));
                            someAlreadyInGroup = true;
                        }
                    }
                    if (someAlreadyInGroup) {
                        StringBuilder names = new StringBuilder();
                        for (Identity ident : alreadyInGroup) {
                            names.append(ident.getName()).append(", ");
                            usersToBeAdded.remove(ident);
                        }
                        names.setLength(names.length() - 2);
                        getWindowControl().setInfo(translate("delegation.msg.delegateesalreadyindelegation", new String[] {names.toString()}));
                    }
					// Check if delegatee to be added is identical with user himself
					Iterator<Identity> usersToBeAddedIterator = usersToBeAdded.iterator();
                    while (usersToBeAddedIterator.hasNext()) {
                    	if (Objects.equals(usersToBeAddedIterator.next().getKey(), userIdentity.getKey())) {
							getWindowControl().setInfo(translate("delegation.msg.usercannotbedelegateeofhimself", new String[] {}));
							usersToBeAddedIterator.remove();
							break;
						}
					}
                    if (usersToBeAdded.isEmpty()) {
                    	return;
                    }
                }

                closeableModalController.deactivate();
                if (usersToBeAdded != null && !usersToBeAdded.isEmpty()) {
                    for (Identity identity : usersToBeAdded) {
                        campusService.createDelegation(this.userIdentity, identity);
                    }
                    delegateesTableDataModel.add(identitiesToGroupMemberViews(usersToBeAdded));
                    delegateesTableController.modelChanged();
                }
            }

        } else if (sourceController == confirmDelete) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                for (Identity delegatee : usersToBeRemoved) {
                    campusService.deleteDelegation(userIdentity, delegatee);
                }
                delegateesTableDataModel.remove(usersToBeRemoved);
                delegateesTableController.modelChanged();
            }
        }
    }

    @Override
    protected void doDispose() {}

    private List<GroupMemberView> identitiesToGroupMemberViews(List<Identity> identities) {
        List<GroupMemberView> groupMemberViews = new ArrayList<>();
        for (Identity identity : identities) {
            groupMemberViews.add(new GroupMemberView(identity, new Date(), null));
        }
        return groupMemberViews;
    }

    private void doBuildConfirmDeleteDialog(UserRequest ureq) {
        if (confirmDelete != null) {
            confirmDelete.dispose();
        }
        StringBuilder names = new StringBuilder();
        for (Identity identity : usersToBeRemoved) {
            names.append(identity.getName()).append(", ");
        }
        // Remove last ", "
        names.setLength(names.length() - 2);
        // Trusted text, no need to escape, identity names are safe
		String translationKey = (usersToBeRemoved.size() == 1 ? "delegation.remove.delegatee" : "delegation.remove.delegatees");
        confirmDelete = activateYesNoDialog(ureq, null, translate(translationKey, names.toString()), confirmDelete);
    }

}
