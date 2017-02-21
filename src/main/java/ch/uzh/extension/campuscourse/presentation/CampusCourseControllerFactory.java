package ch.uzh.extension.campuscourse.presentation;

import ch.uzh.extension.campuscourse.presentation.coursecreation.CampusCourseCreationChoiceController;
import ch.uzh.extension.campuscourse.presentation.coursecreation.CampusCourseCreationDialogController;
import ch.uzh.extension.campuscourse.presentation.coursecreation.CreateCampusCourseCompletedEventListener;
import ch.uzh.extension.campuscourse.presentation.coursecreation.selection.CampusCourseSubmitController;
import ch.uzh.extension.campuscourse.presentation.coursecreation.selection.ContinueCampusCourseSelectionController;
import ch.uzh.extension.campuscourse.presentation.coursecreation.selection.CreationCampusCourseSelectionController;
import ch.uzh.extension.campuscourse.presentation.tab.controller.CampusCourseTabTableController;
import ch.uzh.extension.campuscourse.service.CampusCourseService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.id.context.StateSite;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Martin Schraner
 */
@Component
public class CampusCourseControllerFactory {

	private final CampusCourseService campusCourseService;
	private final RepositoryManager repositoryManager;
	private final CampusCoursePresentationHelper campusCoursePresentationHelper;

	@Autowired
	public CampusCourseControllerFactory(CampusCourseService campusCourseService, RepositoryManager repositoryManager, CampusCoursePresentationHelper campusCoursePresentationHelper) {
		this.campusCourseService = campusCourseService;
		this.repositoryManager = repositoryManager;
		this.campusCoursePresentationHelper = campusCoursePresentationHelper;
	}

	public CampusCourseCreationDialogController createCampusCourseCreateDialogController(
			Long sapCampusCourseId,
			WindowControl windowControl,
			UserRequest userRequest
	) {
		return new CampusCourseCreationDialogController(sapCampusCourseId,
				this, windowControl, userRequest);
	}

	public CampusCourseCreationChoiceController createCampusCourseCreationChoiceController(
			CampusCourseCreationChoiceController.CampusCourseCreationChoiceControllerListener listener,
			WindowControl windowControl,
			UserRequest userRequest
	) {
		return new CampusCourseCreationChoiceController(listener,
				windowControl, userRequest);
	}

	public CampusCourseSubmitController createCampusCourseSubmitController(
			Long sapCampusCourseId,
			CreateCampusCourseCompletedEventListener listener,
			WindowControl windowControl, UserRequest userRequest
	) {
		return new CampusCourseSubmitController(sapCampusCourseId,
				campusCourseService, campusCoursePresentationHelper, listener,
				windowControl, userRequest);
	}

	public CreationCampusCourseSelectionController createCreationCampusCourseSelectionTableController(
			Long sapCampusCourseId, CreateCampusCourseCompletedEventListener listener,
			WindowControl windowControl, UserRequest userRequest
	) {
		return new CreationCampusCourseSelectionController(
				sapCampusCourseId,
				campusCourseService,
				repositoryManager,
				listener,
				windowControl,
				userRequest
		);
	}

	public ContinueCampusCourseSelectionController createContinueCampusCourseSelectionTableController(
			Long sapCampusCourseId,
			CreateCampusCourseCompletedEventListener listener,
			WindowControl windowControl, UserRequest userRequest
	) {
		return new ContinueCampusCourseSelectionController(
				sapCampusCourseId,
				campusCourseService,
				repositoryManager,
				listener,
				windowControl,
				userRequest
		);
	}

	public CampusCourseTabTableController createCampusCourseTabController(
			SiteInstance parent,
			WindowControl windowControl,
			UserRequest userRequest
	) {
		return new CampusCourseTabTableController(
				campusCourseService,
				campusCoursePresentationHelper,
				this,
				new StateSite(parent),
				windowControl,
				userRequest
		);
	}
}
