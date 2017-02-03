package ch.uzh.extension.campuscourse.presentation;

import ch.uzh.extension.campuscourse.presentation.coursecreation.controller.CampusCourseCreateDialogController;
import ch.uzh.extension.campuscourse.presentation.coursecreation.controller.CampusCourseCreationChoiceController;
import ch.uzh.extension.campuscourse.presentation.coursecreation.controller.CreateCampusCourseCompletedEventListener;
import ch.uzh.extension.campuscourse.presentation.coursecreation.controller.selection.CampusCourseSubmitController;
import ch.uzh.extension.campuscourse.presentation.coursecreation.controller.selection.ContinueCampusCourseSelectionController;
import ch.uzh.extension.campuscourse.presentation.coursecreation.controller.selection.CreationCampusCourseSelectionController;
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
public class CampusOlatControllerFactory {

	private final CampusCourseService campusCourseService;
	private final RepositoryManager repositoryManager;
	private final CampusCourseOlatHelper campusCourseOlatHelper;

	@Autowired
	public CampusOlatControllerFactory(CampusCourseService campusCourseService, RepositoryManager repositoryManager, CampusCourseOlatHelper campusCourseOlatHelper) {
		this.campusCourseService = campusCourseService;
		this.repositoryManager = repositoryManager;
		this.campusCourseOlatHelper = campusCourseOlatHelper;
	}

	public CampusCourseCreateDialogController createCampusCourseCreateDialogController(
			Long sapCampusCourseId,
			WindowControl windowControl,
			UserRequest userRequest
	) {
		return new CampusCourseCreateDialogController(sapCampusCourseId,
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
				campusCourseService, campusCourseOlatHelper, listener,
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
				campusCourseOlatHelper,
				this,
				new StateSite(parent),
				windowControl,
				userRequest
		);
	}
}
