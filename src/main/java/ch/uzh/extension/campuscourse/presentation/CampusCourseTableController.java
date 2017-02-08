package ch.uzh.extension.campuscourse.presentation;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.repository.ui.RepositoryTableModel;

import java.util.List;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class CampusCourseTableController<T> extends TableController  implements Activateable2 {

	public CampusCourseTableController(TableGuiConfiguration tableConfig,
									   String columnActionForDisplayName,
									   WindowControl windowControl,
									   UserRequest userRequest) {
		super(tableConfig, userRequest, windowControl,
				CampusCoursePresentationHelper.getTranslator(userRequest.getLocale()),
				true);

		addColumnDescriptor(new DefaultColumnDescriptor(
				"campus.course.table.header.displayname",
				RepositoryTableModel.RepoCols.displayname.ordinal(),
				columnActionForDisplayName, getLocale()));

		setSortColumn(0, true);
		setMultiSelect(false);
	}

	public T getSelectedEntry(Event event) {
		assert event instanceof TableEvent;
		TableEvent tableEvent = (TableEvent) event;
		assert RepositoryTableModel.TABLE_ACTION_SELECT_LINK.equals(tableEvent.getActionId());
		return (T) getTableDataModel().getObject(tableEvent.getRowId());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		addToHistory(ureq);
	}

}
