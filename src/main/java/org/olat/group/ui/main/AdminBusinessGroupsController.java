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
package org.olat.group.ui.main;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.main.BusinessGroupTableModelWithType.Cols;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AdminBusinessGroupsController extends AbstractBusinessGroupListController {
	private static final String TABLE_ACTION_DELETE = "bgTblDelete";
	
	private AdminBusinessGroupSearchController searchController;

	public AdminBusinessGroupsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_group_list");
		if(!isAdmin()) {
			return;
		}
		
		//search controller
		searchController = new AdminBusinessGroupSearchController(ureq, wControl, isAdmin(), true);
		listenTo(searchController);
		mainVC.put("searchPanel", searchController.getInitialComponent());
	}
	
	@Override
	protected int initColumns() {
		CustomCellRenderer markRenderer = new BGMarkCellRenderer(this, mainVC, getTranslator());
		groupListCtr.addColumnDescriptor(false, new CustomRenderColumnDescriptor(Cols.mark.i18n(), Cols.resources.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, markRenderer));
		CustomCellRenderer acRenderer = new BGAccessControlledCellRenderer();
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.accessTypes.i18n(), Cols.accessTypes.ordinal(), null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, acRenderer));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.name.i18n(), Cols.name.ordinal(), TABLE_ACTION_LAUNCH, getLocale()));
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.description.i18n(), Cols.description.ordinal(), null, getLocale()));
		CustomCellRenderer resourcesRenderer = new BGResourcesCellRenderer(this, mainVC, getTranslator());
		groupListCtr.addColumnDescriptor(false, new CustomRenderColumnDescriptor(Cols.resources.i18n(), Cols.resources.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, resourcesRenderer));
		groupListCtr.addColumnDescriptor(false, new BooleanColumnDescriptor(Cols.allowLeave.i18n(), Cols.allowLeave.ordinal(), TABLE_ACTION_LEAVE, translate("table.header.leave"), null));
		groupListCtr.addColumnDescriptor(new BooleanColumnDescriptor(Cols.allowDelete.i18n(), Cols.allowDelete.ordinal(), TABLE_ACTION_DELETE, translate("table.header.delete"), null));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.accessControlLaunch.i18n(), Cols.accessControlLaunch.ordinal(), TABLE_ACTION_ACCESS, getLocale()));
		return 8;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == searchController) {
			if(event instanceof SearchEvent) {
				doSearch((SearchEvent)event);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doSearch(SearchEvent event) {
		long start = isLogDebugEnabled() ? System.currentTimeMillis() : 0;

		search(event);

		if(isLogDebugEnabled()) {
			logDebug("Group search takes (ms): " + (System.currentTimeMillis() - start), null);
		}
	}

	private void search(SearchEvent event) {
		Long id = event.getId();
		String name = event.getName();
		String description = event.getDescription();
		String ownerName = event.getOwnerName();
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		if(id != null) {
			params.setGroupKeys(Collections.singletonList(id));
		}
		params.setName(StringHelper.containsNonWhitespace(name) ? name : null);
		params.setDescription(StringHelper.containsNonWhitespace(description) ? description : null);
		params.setOwnerName(StringHelper.containsNonWhitespace(ownerName) ? ownerName : null);
		params.setOwner(event.isOwner());
		params.setAttendee(event.isAttendee());
		params.setWaiting(event.isWaiting());
		params.setPublicGroup(event.getPublicGroups());
		params.setResources(event.getResources());
		params.setIdentity(getIdentity());
		
		//security
		List<BusinessGroup> groups;
		if(event.isAttendee() || event.isOwner()) {
			params.setIdentity(getIdentity());
		}
		groups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		
		updateTableModel(groups, false);
	}
}
