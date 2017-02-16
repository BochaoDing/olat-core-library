package ch.uzh.extension.campuscourse.presentation.undocoursecontinuation;

import ch.uzh.extension.campuscourse.service.CampusCourseService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryLifeCycleChangeController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Schraner
 */
public class ContinuedCampusCourseRepositoryEntryLifeCycleChangeController extends RepositoryEntryLifeCycleChangeController {

	private final CampusCourseService campusCourseService;
	private Link undoCourseContinuationLink;

	public ContinuedCampusCourseRepositoryEntryLifeCycleChangeController(RepositoryService repositoryService,
																		 RepositoryManager repositoryManager,
																		 CampusCourseService campusCourseService,
																		 UserRequest ureq,
																		 WindowControl wControl,
																		 RepositoryEntry re,
																		 RepositoryEntrySecurity repositoryEntrySecurity,
																		 RepositoryHandler repositoryHandler,
																		 Translator translator) {

		super(repositoryService, repositoryManager, ureq, wControl, re, repositoryEntrySecurity, repositoryHandler, translator);

		this.campusCourseService = campusCourseService;

		undoCourseContinuationLink = LinkFactory.createButton("undo.course.continuation", lifeCycleVC, this);
		undoCourseContinuationLink.setCustomDisplayText(translate("undo.course.continuation.button"));
		undoCourseContinuationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_close_resource");
		undoCourseContinuationLink.setElementCssClass("o_sel_repo_close");
	}

	@Override
	protected void event(UserRequest userRequest, Component source, Event event) {
		super.event(userRequest, source, event);
		if (source == undoCourseContinuationLink) {
			new ConfirmUndoDialogBoxController(userRequest, getWindowControl(), getTranslator()).activate();
		}
	}

	private void onUndoCourseContinuationConfirmed(UserRequest userRequest) {
		String titleOfContinuedCourse = campusCourseService.getTitlesOfChildAndParentCoursesInAscendingOrder(re).get(1);
		campusCourseService.undoCourseContinuation(re, userRequest.getIdentity());
		showInfo("info.undo.course.continuation.successful", new String[] {StringHelper.escapeHtml(titleOfContinuedCourse)});
		if (!campusCourseService.isContinuedCourse(re)) {
			re = repositoryService.loadByKey(re.getKey());
			lifeCycleVC.remove(undoCourseContinuationLink);
			undoCourseContinuationLink = null;
		}
	}

	private static List<String> createButtonLabelsOfConfirmUndoDialogBoxController(Translator translator) {
		List<String> result = new ArrayList<>();
		result.add(translator.translate("popup.undo.course.continuation.button.label.yes"));
		result.add(translator.translate("popup.undo.course.continuation.button.label.no"));
		return result;
	}

	private class ConfirmUndoDialogBoxController extends DialogBoxController {

		private ConfirmUndoDialogBoxController(UserRequest userRequest,
									   WindowControl windowControl,
									   Translator translator) {
			super(userRequest, windowControl,
					translator.translate("popup.undo.course.continuation.title"),
					translator.translate("popup.undo.course.continuation.text", new String[] {
							StringHelper.escapeHtml(campusCourseService.getTitlesOfChildAndParentCoursesInAscendingOrder(re).get(0)),
							StringHelper.escapeHtml(campusCourseService.getTitlesOfChildAndParentCoursesInAscendingOrder(re).get(1))}),
					createButtonLabelsOfConfirmUndoDialogBoxController(translator));
		}

		@Override
		protected void event(UserRequest userRequest, Component source, Event event) {
			super.event(userRequest, source, event);

			if ("link_0".equals(event.getCommand())) {
				onUndoCourseContinuationConfirmed(userRequest);
			}
		}


	}

}
