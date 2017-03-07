package ch.uzh.extension.campuscourse.presentation.coursecreation.selection;

import ch.uzh.extension.campuscourse.presentation.CampusCourseSelectionController;
import ch.uzh.extension.campuscourse.presentation.CampusCourseTableController;
import ch.uzh.extension.campuscourse.presentation.coursecreation.CreateCampusCourseCompletedEventListener;
import ch.uzh.extension.campuscourse.presentation.coursecreation.table.CampusCourseDialogTableController;
import ch.uzh.extension.campuscourse.service.CampusCourseService;
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
