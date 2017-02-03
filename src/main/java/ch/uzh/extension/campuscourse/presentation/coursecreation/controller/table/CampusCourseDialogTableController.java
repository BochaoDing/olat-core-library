package ch.uzh.extension.campuscourse.presentation.coursecreation.controller.table;

import ch.uzh.extension.campuscourse.presentation.common.controller.CampusCourseTableController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryTableModel;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class CampusCourseDialogTableController extends CampusCourseTableController<RepositoryEntry> {
	public CampusCourseDialogTableController(TableGuiConfiguration tableConfig,
											 WindowControl windowControl,
											 UserRequest userRequest) {
		super(tableConfig, RepositoryTableModel.TABLE_ACTION_SELECT_LINK,
				windowControl, userRequest);

		addColumnDescriptor(new DefaultColumnDescriptor("table.header.author",
				RepositoryTableModel.RepoCols.author.ordinal(), null, getLocale()));
		addColumnDescriptor(false, new DefaultColumnDescriptor("campus.course.table.header.access",
				RepositoryTableModel.RepoCols.access.ordinal(), null, getLocale()));
		addColumnDescriptor(new DefaultColumnDescriptor("table.header.date",
				RepositoryTableModel.RepoCols.creationDate.ordinal(), null, getLocale()));
		addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.lastusage",
				RepositoryTableModel.RepoCols.lastUsage.ordinal(), null, getLocale()));
	}
}
