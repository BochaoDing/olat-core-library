package ch.uzh.campus.olat.controller;

import ch.uzh.campus.olat.CampusCourseOlatHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryTableModel;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class CampusCourseTableController extends TableController {

	public CampusCourseTableController(TableGuiConfiguration tableConfig,
									   WindowControl windowControl,
									   UserRequest userRequest) {
		super(tableConfig, userRequest, windowControl,
				CampusCourseOlatHelper.getTranslator(userRequest.getLocale()),
				true);

		addColumnDescriptor(new DefaultColumnDescriptor(
				"campus.course.table.header.displayname",
				RepositoryTableModel.RepoCols.displayname.ordinal(),
				RepositoryTableModel.TABLE_ACTION_SELECT_LINK, getLocale()));

		setSortColumn(0, true);
		setMultiSelect(false);
		setTableDataModel(new RepositoryTableModel(getLocale()));
	}

	public RepositoryEntry getSelectedEntry(Event event) {
		assert event instanceof TableEvent;
		TableEvent tableEvent = (TableEvent) event;
		assert RepositoryTableModel.TABLE_ACTION_SELECT_LINK.equals(tableEvent.getActionId());
		return (RepositoryEntry) getTableDataModel().getObject(tableEvent.getRowId());
	}
}
