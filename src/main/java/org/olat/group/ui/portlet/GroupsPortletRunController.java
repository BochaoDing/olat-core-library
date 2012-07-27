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

package org.olat.group.ui.portlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortletRunController;
import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.core.gui.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.core.gui.control.generic.portal.SortingCriteria;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;

/**
 * Description:<br>
 * Run view controller for the groups list portlet
 * <P>
 * Initial Date: 11.07.2005 <br>
 * 
 * @author gnaegi
 */
public class GroupsPortletRunController extends AbstractPortletRunController<BusinessGroup> implements GenericEventListener {
	
	private static final String CMD_LAUNCH = "cmd.launch";

	private final TableController tableCtr;
	private final GroupTableDataModel groupListModel;
	private VelocityContainer groupsVC;
	private Link showAllLink;
	
	private final BusinessGroupService businessGroupService;
	

	/**
	 * Constructor
	 * 
	 * @param ureq
	 * @param component
	 */
	public GroupsPortletRunController(WindowControl wControl, UserRequest ureq, Translator trans, String portletName) {
		super(wControl, ureq, trans, portletName);

		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
		sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
		sortingTermsList.add(SortingCriteria.DATE_SORTING);
		
		groupsVC = createVelocityContainer("groupsPortlet");
		showAllLink = LinkFactory.createLink("groupsPortlet.showAll", groupsVC, this);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("groupsPortlet.nogroups"));
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("b_portlet_table");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
    //disable the default sorting for this table
		tableConfig.setSortingEnabled(false);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), trans);
		listenTo(tableCtr);

		// dummy header key, won't be used since setDisplayTableHeader is set to
		// false
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("groupsPortlet.bgname", 0, CMD_LAUNCH, trans.getLocale()));
		
		sortingCriteria = getPersistentSortingConfiguration(ureq);
		groupListModel = new GroupTableDataModel(Collections.<PortletEntry<BusinessGroup>>emptyList());
		tableCtr.setTableDataModel(groupListModel);
		reloadModel(sortingCriteria);
     
		groupsVC.put("table", tableCtr.getInitialComponent());		
		putInitialPanel(groupsVC);

		// register for businessgroup type events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(BusinessGroup.class));
	}
	
	private List<PortletEntry<BusinessGroup>> convertBusinessGroupToPortletEntryList(List<BusinessGroup> groups) {
		List<PortletEntry<BusinessGroup>> convertedList = new ArrayList<PortletEntry<BusinessGroup>>();
		for(BusinessGroup group:groups) {
			convertedList.add(new GroupPortletEntry(group));
		}
		return convertedList;
	}
	
	protected void reloadModel(SortingCriteria sortingCriteria) {
		if (sortingCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
			SearchBusinessGroupParams params = new SearchBusinessGroupParams(getIdentity(), true, true);
			BusinessGroupOrder order = null;
			if(sortingCriteria.getSortingTerm()==SortingCriteria.ALPHABETICAL_SORTING) {
		  	order = sortingCriteria.isAscending() ? BusinessGroupOrder.nameAsc : BusinessGroupOrder.nameDesc;
		  } else if(sortingCriteria.getSortingTerm()==SortingCriteria.DATE_SORTING) {
		  	order = sortingCriteria.isAscending() ? BusinessGroupOrder.creationDateAsc : BusinessGroupOrder.creationDateDesc;
		  }
			List<BusinessGroup> groupList = businessGroupService.findBusinessGroups(params, null, 0, sortingCriteria.getMaxEntries(), order);
			List<PortletEntry<BusinessGroup>> entries = convertBusinessGroupToPortletEntryList(groupList);
			groupListModel.setObjects(entries);
			tableCtr.modelChanged();
		} else {
			reloadModel(getPersistentManuallySortedItems());
		}
	}
	
	protected void reloadModel(List<PortletEntry<BusinessGroup>> sortedItems) {						
		groupListModel.setObjects(sortedItems);
		tableCtr.modelChanged();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showAllLink) {
			NewControllerFactory.getInstance().launch("[GroupsSite:0]", ureq, getWindowControl());
		} 
	}

	/**
	 * @see org.olat.core.gui.control.ControllerEventListener#dispatchEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_LAUNCH)) {
					int rowid = te.getRowId();
					BusinessGroup currBusinessGroup = groupListModel.getBusinessGroupAt(rowid);
					boolean isInBusinessGroup = businessGroupService.isIdentityInBusinessGroup(ureq.getIdentity(), currBusinessGroup);
					if(isInBusinessGroup) {
					  BGControllerFactory.getInstance().createRunControllerAsTopNavTab(currBusinessGroup, ureq, getWindowControl());
					} else {
						showInfo("groupsPortlet.no_member");
					}
				}
			}
		}	
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		super.doDispose();
		// de-register for businessgroup type events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(BusinessGroup.class));
		// POST: all firing event for the source just deregistered are finished
		// (listeners lock in EventAgency)
	}

	public void event(Event event) {
		if (event instanceof BusinessGroupModifiedEvent) {
			BusinessGroupModifiedEvent mev = (BusinessGroupModifiedEvent) event;
			if(getIdentity().getKey().equals(mev.getAffectedIdentityKey())) {
				Long modifiedKey = mev.getModifiedGroupKey();
				for(PortletEntry<BusinessGroup> portlet:groupListModel.getObjects()) {
					if(modifiedKey.equals(portlet.getKey())) {
						GroupPortletEntry groupPortlet = (GroupPortletEntry)portlet;
						if(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT.equals(event.getCommand())) {
							groupListModel.getObjects().remove(groupPortlet);
							tableCtr.modelChanged();
							break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, 
	 * creates the table model for the manual sorting, and instantiates the PortletToolSortingControllerImpl.
	 * @param ureq
	 * @param wControl
	 * @return a PortletToolSortingControllerImpl instance.
	 */
	protected PortletToolSortingControllerImpl<BusinessGroup> createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {
			SearchBusinessGroupParams params = new SearchBusinessGroupParams(getIdentity(), true, true);
			List<BusinessGroup> groupList = businessGroupService.findBusinessGroups(params, null, 0, -1);
			List<PortletEntry<BusinessGroup>> portletEntryList = convertBusinessGroupToPortletEntryList(groupList);
			GroupsManualSortingTableDataModel tableDataModel = new GroupsManualSortingTableDataModel(portletEntryList);
			List<PortletEntry<BusinessGroup>> sortedItems = getPersistentManuallySortedItems();
			
			portletToolsController = new PortletToolSortingControllerImpl<BusinessGroup>(ureq, wControl, getTranslator(), sortingCriteria, tableDataModel, sortedItems);
			portletToolsController.setConfigManualSorting(true);
			portletToolsController.setConfigAutoSorting(true);
			portletToolsController.addControllerListener(this);
		}		
		return portletToolsController;
	}
	
	 /**
   * Retrieves the persistent manually sorted items for the current portlet.
   * @param ureq
   * @return
   */
  private List<PortletEntry<BusinessGroup>> getPersistentManuallySortedItems() { 
  	@SuppressWarnings("unchecked")
		Map<Long, Integer> storedPrefs = (Map<Long, Integer>)guiPreferences.get(Map.class, getPreferenceKey(SORTED_ITEMS_PREF));
  	
  	SearchBusinessGroupParams params = new SearchBusinessGroupParams(getIdentity(), true, true);
  	params.setGroupKeys(storedPrefs.keySet());
  	List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, null, 0, -1);
  	List<PortletEntry<BusinessGroup>> portletEntryList = convertBusinessGroupToPortletEntryList(groups);
		return getPersistentManuallySortedItems(portletEntryList);
	}
  
  /**
	 * Comparator implementation used for sorting BusinessGroup entries according with the
	 * input sortingCriteria.
	 * <p>
	 * @param sortingCriteria
	 * @return a Comparator for the input sortingCriteria
	 */
  protected Comparator<BusinessGroup> getComparator(final SortingCriteria sortingCriteria) {
		return new Comparator<BusinessGroup>(){			
			public int compare(final BusinessGroup group1, final BusinessGroup group2) {
				int comparisonResult = 0;
			  if(sortingCriteria.getSortingTerm()==SortingCriteria.ALPHABETICAL_SORTING) {			  	
			  	comparisonResult = collator.compare(group1.getName(), group2.getName());			  		  	
			  } else if(sortingCriteria.getSortingTerm()==SortingCriteria.DATE_SORTING) {
			  	comparisonResult = group1.getCreationDate().compareTo(group2.getCreationDate());
			  }
			  if(!sortingCriteria.isAscending()) {
			  	//if not isAscending return (-comparisonResult)			  	
			  	return -comparisonResult;
			  }
			  return comparisonResult;
			}};
	}
  
  /**
   * 
   * PortletDefaultTableDataModel implementation for the current portlet.
   * 
   * <P>
   * Initial Date:  10.12.2007 <br>
   * @author Lavinia Dumitrescu
   */
  private class GroupTableDataModel extends PortletDefaultTableDataModel<BusinessGroup> {  	
  	public GroupTableDataModel(List<PortletEntry<BusinessGroup>> objects) {
  		super(objects, 1);
  	}
  	
  	public Object getValueAt(int row, int col) {
  		PortletEntry<BusinessGroup> entry = getObject(row);
  		BusinessGroup businessGroup = entry.getValue();
  		switch (col) {
  			case 0:
  				String name = businessGroup.getName();
  				name = StringEscapeUtils.escapeHtml(name).toString();
  				return name;
  			default:
  				return "ERROR";
  		}
  	}
  	
  	public BusinessGroup getBusinessGroupAt(int row) {
  		return getPortletValue(row);
  	}
  }

  /**
   * 
   * PortletDefaultTableDataModel implementation for the manual sorting component.
   * 
   * <P>
   * Initial Date:  10.12.2007 <br>
   * @author Lavinia Dumitrescu
   */
	private class GroupsManualSortingTableDataModel extends PortletDefaultTableDataModel<BusinessGroup>  {		
		/**
		 * @param objects
		 * @param locale
		 */
		public GroupsManualSortingTableDataModel(List<PortletEntry<BusinessGroup>> objects) {
			super(objects, 3);
		}

		/**
		 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
		 */
		public final Object getValueAt(int row, int col) {
			PortletEntry<BusinessGroup> portletEntry = getObject(row);
			BusinessGroup group = (BusinessGroup) portletEntry.getValue();
			switch (col) {
				case 0:
					return group.getName();
				case 1:
					String description = group.getDescription();
					description = FilterFactory.getHtmlTagsFilter().filter(description);
					return (description == null ? "n/a" : description);
				case 2:
					Date date = group.getCreationDate();
					return date;
				default:
					return "error";
			}
		}	
	}
	
	private class GroupPortletEntry implements PortletEntry<BusinessGroup> {
	  	private BusinessGroup value;
	  	private Long key;
	  	
	  	public GroupPortletEntry(BusinessGroup group) {
	  		value = group;
	  		key = group.getKey();
	  	}
	  	
	  	public Long getKey() {
	  		return key;
	  	}
	  	
	  	public BusinessGroup getValue() {
	  		return value;
	  	}
	}
}
