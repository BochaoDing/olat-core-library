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
package org.olat.repository.ui.author;

import java.util.Collections;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.author.AuthoringEntryDataModel.Cols;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthorListController extends FormBasicController implements Activateable2, AuthoringEntryDataSourceUIFactory {

	private FlexiTableElement tableEl;
	private AuthoringEntryDataModel model;
	private AuthoringEntryDataSource dataSource;
	private final SearchAuthorRepositoryEntryViewParams searchParams;
	private final MarkManager markManager;
	private final TooledStackedPanel stackPanel;

	private AuthorSearchController searchCtrl;
	private AuthoringEntryDetailsController detailsCtrl;
	
	public AuthorListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			SearchAuthorRepositoryEntryViewParams searchParams) {
		super(ureq, wControl, "repoentry_table");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		markManager = CoreSpringFactory.getImpl(MarkManager.class);

		this.stackPanel = stackPanel;
		this.searchParams = searchParams;
		dataSource = new AuthoringEntryDataSource(searchParams, this);
		initForm(ureq);
	}
	
	public boolean isEmpty() {
		return dataSource.getRowCount() == 0;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//search form
		searchCtrl = new AuthorSearchController(ureq, getWindowControl(), true, mainForm);
		listenTo(searchCtrl);
		formLayout.add("search", searchCtrl.getInitialFormItem());
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18nKey(), Cols.key.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.mark.i18nKey(), Cols.mark.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.ac.i18nKey(), Cols.ac.ordinal(), new ACRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.type.i18nKey(), Cols.type.ordinal(), new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleLabel.i18nKey(), Cols.lifecycleLabel.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleSoftkey.i18nKey(), Cols.lifecycleSoftkey.ordinal()));
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer("select", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel(Cols.displayName.i18nKey(), Cols.displayName.ordinal(), "select", renderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleStart.i18nKey(), Cols.lifecycleStart.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleEnd.i18nKey(), Cols.lifecycleEnd.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.author.i18nKey(), Cols.author.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.authors.i18nKey(), Cols.authors.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.access.i18nKey(), Cols.access.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT,  new AccessRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.creationDate.i18nKey(), Cols.creationDate.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastUsage.i18nKey(), Cols.lastUsage.ordinal(), false, null));
		
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("details", -1, "details",
				new StaticFlexiCellRenderer("" /* translate("details")*/, "details", "o_icon-lg o_icon_details")));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("edit", -1, "edit",
				new StaticFlexiCellRenderer("" /* translate("edit") */, "edit", "o_icon-lg o_icon_edit")));
		
		model = new AuthoringEntryDataModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(ureq, getWindowControl(), "table", model, 20, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_coursetable o_rendertype_custom");
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("mark".equals(cmd)) {
				AuthoringEntryRow row = (AuthoringEntryRow)link.getUserObject();
				boolean marked = doMark(row);
				link.setIconCSS(marked ? "o_icon o_icon_bookmark o_icon-lg" : "o_icon o_icon_bookmark_add o_icon-lg");
				link.getComponent().setDirty(true);
				row.setMarked(marked);
			}
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AuthoringEntryRow row = model.getObject(se.getIndex());
				if("details".equals(cmd)) {
					doOpenDetails(ureq, row);
				} else if("edit".equals(cmd)) {
					doEdit(ureq, row);
				} else if("select".equals(cmd)) {
					doOpen(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchCtrl == source) {
			if(event instanceof SearchEvent) {
				SearchEvent se = (SearchEvent)event;
				doSearch(se);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc) {
		//do not update the 
	}
	
	protected void doSearch(SearchEvent se) {
		if(se.getType() != null) {
			searchParams.setResourceTypes(Collections.singletonList(se.getType()));
		} else {
			searchParams.setResourceTypes(null);
		}

		tableEl.reset();
	}
	
	protected void doOpen(UserRequest ureq, AuthoringEntryRow row) {
		String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	protected void doEdit(UserRequest ureq, AuthoringEntryRow row) {
		String businessPath = "[RepositoryEntry:" + row.getKey() + "][Editor:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	protected void doOpenDetails(UserRequest ureq, AuthoringEntryRow row) {
		stackPanel.popUpToRootController(ureq);

		removeAsListenerAndDispose(detailsCtrl);
		
		detailsCtrl = new AuthoringEntryDetailsController(ureq, getWindowControl(), stackPanel, row);
		listenTo(detailsCtrl);
	}

	@Override
	public void forgeMarkLink(AuthoringEntryRow row) {
		FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "", null, null, Link.NONTRANSLATED);
		markLink.setIconCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
	}
	
	protected boolean doMark(AuthoringEntryRow row) {
		OLATResourceable item = OresHelper.createOLATResourceableInstance("RepositoryEntry", row.getKey());
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			return false;
		} else {
			String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			return true;
		}
	}
}