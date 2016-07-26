package ch.uzh.campus.olat.controller;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryTableModel;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
class CampusCourseTableController extends TableController {

	CampusCourseTableController(TableGuiConfiguration tableConfig,
									   UserRequest userRequest,
									   WindowControl wControl,
									   Translator tableTrans) {
		super(tableConfig, userRequest, wControl, tableTrans, true);

		addColumnDescriptor(new DefaultColumnDescriptor("campus.course.table.header.displayname", RepositoryTableModel.RepoCols.displayname.ordinal(), RepositoryTableModel.TABLE_ACTION_SELECT_LINK, getLocale()));
		addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", RepositoryTableModel.RepoCols.author.ordinal(), null, getLocale()));
		addColumnDescriptor(false, new DefaultColumnDescriptor("campus.course.table.header.access", RepositoryTableModel.RepoCols.access.ordinal(), null, getLocale()));
		addColumnDescriptor(new DefaultColumnDescriptor("table.header.date", RepositoryTableModel.RepoCols.creationDate.ordinal(), null, getLocale()));
		addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.lastusage", RepositoryTableModel.RepoCols.lastUsage.ordinal(), null, getLocale()));
		setSortColumn(0, true);
		setMultiSelect(false);
		setTableDataModel(new RepositoryTableModel(getLocale()));
	}

	protected RepositoryEntry getSelectedEntry(Event event) {
		assert event instanceof TableEvent;
		TableEvent tableEvent = (TableEvent) event;
		assert RepositoryTableModel.TABLE_ACTION_SELECT_LINK.equals(tableEvent.getActionId());
		return (RepositoryEntry) getTableDataModel().getObject(tableEvent.getRowId());
	}
}
