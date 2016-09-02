package ch.uzh.campus.olat.dialog.controller.selection;

import ch.uzh.campus.olat.dialog.controller.CreateCampusCourseCompletedEventListener;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.learn.CampusCourseService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResourceManager;

import java.util.List;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class CreationCampusCourseSelectionController extends CampusCourseDialogSelectionController {

	public CreationCampusCourseSelectionController(Long sapCampusCourseId,
												   CampusCourseService campusCourseService,
												   RepositoryManager repositoryManager,
												   CreateCampusCourseCompletedEventListener listener,
												   WindowControl windowControl,
												   UserRequest userRequest
	) {
		super(sapCampusCourseId, campusCourseService, repositoryManager,
				listener, windowControl, userRequest);

		List<RepositoryEntry> entries = repositoryManager.queryByOwner(
				userRequest.getIdentity(), "CourseModule");
		table.getTableDataModel().setObjects(entries);
		table.modelChanged(true);
	}

	@Override
	protected void event(UserRequest userRequest, Controller source, Event event) {
		Long resourceableId = OLATResourceManager.getInstance().findResourceable(
				table.getSelectedEntry(event).getOlatResource())
				.getResourceableId();
		try {
			CampusCourse campusCourse = campusCourseService.createCampusCourseFromStandardTemplate(resourceableId,
					sapCampusCourseId, userRequest.getIdentity());

			listener.onSuccess(userRequest, campusCourse);
		} catch (Exception e) {
			listener.onError(userRequest, e);
		}
	}
}
