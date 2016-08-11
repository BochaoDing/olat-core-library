package ch.uzh.campus.olat.dialog.controller.table;

import ch.uzh.campus.olat.controller.CampusCourseTableController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.ui.RepositoryTableModel;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class CampusCourseDialogTableController extends CampusCourseTableController {
	public CampusCourseDialogTableController(TableGuiConfiguration tableConfig,
											 WindowControl windowControl,
											 UserRequest userRequest) {
		super(tableConfig, windowControl, userRequest);

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
