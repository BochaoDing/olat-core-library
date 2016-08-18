package ch.uzh.campus.olat.dialog.controller.selection;

import ch.uzh.campus.data.Course;
import ch.uzh.campus.olat.CampusCourseOlatHelper;
import ch.uzh.campus.olat.dialog.controller.CreateCampusCourseCompletedEventListener;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.learn.CampusCourseService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class ContinueCampusCourseSelectionController extends CampusCourseDialogSelectionController {

	private static List<String> getButtonLabels(Translator translator) {
		List<String> result = new ArrayList<>();
		result.add(translator.translate("popup.course.continuation.button.label.yes"));
		result.add(translator.translate("popup.course.continuation.button.label.no"));
		return result;
	}

	private class ContinueCampusCourseDialogBoxController extends DialogBoxController {

		private final RepositoryEntry repositoryEntry;

		ContinueCampusCourseDialogBoxController(RepositoryEntry repositoryEntry,
												UserRequest userRequest,
												WindowControl windowControl,
												Translator translator) {
			super(userRequest, windowControl,
				  translator.translate("popup.course.continuation.title",
						  new String[] {repositoryEntry.getDisplayname()}),
				  translator.translate("popup.course.continuation.text",
						  new String[] {StringHelper.escapeHtml(repositoryEntry.getDisplayname())}),
				  getButtonLabels(translator));

			this.repositoryEntry = repositoryEntry;
		}

		protected void event(UserRequest userRequest, Component source, Event event) {
			super.event(userRequest, source, event);

			if ("link_0".equals(event.getCommand())) {
				if (campusCourseService.checkDelegation(sapCampusCourseId, userRequest.getIdentity()) == false) {
					showError("popup.course.notContinued.becauseOfRemovedDelegation.text");
				} else {
					try {
						Course parentCourse = campusCourseService.getLatestCourseByResourceable(repositoryEntry
								.getOlatResource().getResourceableId());
						CampusCourse campusCourse = campusCourseService.continueCampusCourse(sapCampusCourseId,
								parentCourse.getId(), userRequest.getIdentity());
						listener.onSuccess(campusCourse);
					} catch (Exception e) {
						listener.onError(e);
					}
				}
			}
		}
	}

	public ContinueCampusCourseSelectionController(Long sapCampusCourseId,
												   CampusCourseService campusCourseService,
												   RepositoryManager repositoryManager,
												   CampusCourseOlatHelper campusCourseOlatHelper,
												   CreateCampusCourseCompletedEventListener listener,
												   WindowControl windowControl,
												   UserRequest userRequest
	) {
		super(sapCampusCourseId, campusCourseService, repositoryManager,
				campusCourseOlatHelper, listener, windowControl, userRequest);

		List<RepositoryEntry> campusCourseEntries = new ArrayList<>();
		List<RepositoryEntry> entries = repositoryManager.queryByOwner(userRequest.getIdentity(), "CourseModule");
		if (!entries.isEmpty()) {
			List<Long> resourceableIdsOfAllCreatedCoursesOfPreviousSemesters = campusCourseService.getResourceableIdsOfAllCreatedCoursesOfPreviousSemesters();
			for (RepositoryEntry entry : entries) {
				Long resourcableId = entry.getOlatResource().getResourceableId();
				if (resourceableIdsOfAllCreatedCoursesOfPreviousSemesters.contains(resourcableId)) {
                    campusCourseEntries.add(entry);
				}
			}
		}

		table.getTableDataModel().setObjects(campusCourseEntries);
		table.modelChanged(true);
	}

	@Override
	protected void event(UserRequest userRequest, Controller source, Event event) {
		new ContinueCampusCourseDialogBoxController(table.getSelectedEntry(event),
				userRequest, getWindowControl(), getTranslator()).activate();
	}
}
