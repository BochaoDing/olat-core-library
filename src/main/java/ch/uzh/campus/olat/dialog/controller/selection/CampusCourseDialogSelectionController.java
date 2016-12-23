package ch.uzh.campus.olat.dialog.controller.selection;

import ch.uzh.campus.olat.controller.CampusCourseSelectionController;
import ch.uzh.campus.olat.controller.CampusCourseTableController;
import ch.uzh.campus.olat.dialog.controller.CreateCampusCourseCompletedEventListener;
import ch.uzh.campus.olat.dialog.controller.table.CampusCourseDialogTableController;
import ch.uzh.campus.service.CampusCourseService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.RepositoryTableModel;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class CampusCourseDialogSelectionController extends CampusCourseSelectionController<RepositoryEntry> {

	CampusCourseDialogSelectionController(Long sapCampusCourseId,
										  CampusCourseService campusCourseService,
										  RepositoryManager repositoryManager,
										  CreateCampusCourseCompletedEventListener listener,
										  WindowControl windowControl,
										  UserRequest userRequest) {
		super(sapCampusCourseId, campusCourseService, repositoryManager,
                listener, windowControl, userRequest);
	}

	@Override
	protected CampusCourseTableController<RepositoryEntry> createTableController(
			WindowControl windowControl, UserRequest userRequest) {
		CampusCourseDialogTableController tmp =
				new CampusCourseDialogTableController(
						createTableGuiConfiguration(), windowControl,
						userRequest);

		tmp.setTableDataModel(new RepositoryTableModel(getLocale()));
		return tmp;
	}
}
