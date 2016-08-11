package ch.uzh.campus.olat.dialog.controller.selection;

import ch.uzh.campus.olat.CampusCourseOlatHelper;
import ch.uzh.campus.olat.controller.CampusCourseSelectionController;
import ch.uzh.campus.olat.controller.CampusCourseTableController;
import ch.uzh.campus.olat.dialog.controller.CreateCampusCourseCompletedEventListener;
import ch.uzh.campus.olat.dialog.controller.table.CampusCourseDialogTableController;
import ch.uzh.campus.service.learn.CampusCourseService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryManager;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class CampusCourseDialogSelectionController extends CampusCourseSelectionController {

	CampusCourseDialogSelectionController(Long sapCampusCourseId,
										  CampusCourseService campusCourseService,
										  RepositoryManager repositoryManager,
										  CampusCourseOlatHelper campusCourseOlatHelper,
										  CreateCampusCourseCompletedEventListener listener,
										  WindowControl windowControl,
										  UserRequest userRequest) {
		super(sapCampusCourseId, campusCourseService, repositoryManager,
				campusCourseOlatHelper, listener, windowControl, userRequest);
	}

	@Override
	protected CampusCourseTableController createTableController(WindowControl windowControl,
																UserRequest userRequest) {
		return new CampusCourseDialogTableController(createTableGuiConfiguration(),
				windowControl, userRequest);
	}
}
