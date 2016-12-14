package ch.uzh.campus.olat.dialog.controller.selection;

import ch.uzh.campus.olat.dialog.controller.CreateCampusCourseCompletedEventListener;
import ch.uzh.campus.service.data.OlatCampusCourse;
import ch.uzh.campus.service.CampusCourseService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class CreationCampusCourseSelectionController extends CampusCourseDialogSelectionController {

	private static final OLog LOG = Tracing.createLoggerFor(CreationCampusCourseSelectionController.class);

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
		OLATResource templateOlatResource = OLATResourceManager.getInstance().findResourceable(
				table.getSelectedEntry(event).getOlatResource());
		try {
			// Check template course for custom quotas only if it is requested, otherwise let copying of any course
			if (checkTemplateForQuotas()) {
				Roles roles = userRequest.getUserSession().getRoles();
				ICourse template = CourseFactory.loadCourse(templateOlatResource.getResourceableId());
				if (!roles.isOLATAdmin() && template.exceedsSizeLimit()) {
					showError("copy.skipped.sizelimit.exceeded");
					return;
				}
			}
			OlatCampusCourse olatCampusCourse = campusCourseService.createOlatCampusCourseFromTemplate(templateOlatResource,
					sapCampusCourseId, userRequest.getIdentity());
			listener.onSuccess(userRequest, olatCampusCourse);
		} catch (Exception e) {

			// OLATNG-341: Error log to find out more about error (to be removed after problem is solved)
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			LOG.error("OLATNG-341: Error when trying to create campus course from template: " + e.getMessage() + " \nStack trace: " + sw.toString());


			listener.onError(userRequest, e);
		}
	}

	private boolean checkTemplateForQuotas() {
		return false; // TODO make it configurable
	}

}
