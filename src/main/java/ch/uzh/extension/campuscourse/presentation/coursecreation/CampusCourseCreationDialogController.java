package ch.uzh.extension.campuscourse.presentation.coursecreation;

import ch.uzh.extension.campuscourse.presentation.CampusCourseControllerFactory;
import ch.uzh.extension.campuscourse.presentation.coursecreation.selection.CampusCourseDialogSelectionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.repository.RepositoryEntry;

import java.util.ArrayList;

/**
 * Initial date: 2016-08-09<br />
 * @author sev26 (UZH)
 */
public class CampusCourseCreationDialogController extends BasicController implements CreateCampusCourseCompletedEventListener {

	private ArrayList<CreateCampusCourseCompletedEventListener> createCampusCourseCompletedEventListeners =
			new ArrayList<>(1);
	private final VelocityContainer velocityContainer;

	public CampusCourseCreationDialogController(Long sapCampusCourseId,
												CampusCourseControllerFactory campusCourseControllerFactory,
												WindowControl windowControl,
												UserRequest userRequest) {
		super(userRequest, windowControl);

		velocityContainer = createVelocityContainer(CampusCourseCreationDialogController.class.getSimpleName());

		CampusCourseCreationChoiceController.CampusCourseCreationChoiceControllerListener listener =
				(x, w, u) -> {
					switch (x) {
						case 1:
							velocityContainer.put(CampusCourseDialogSelectionController.class.getSimpleName(),
									campusCourseControllerFactory
											.createCreationCampusCourseSelectionTableController(
													sapCampusCourseId, this, w, u)
											.getInitialComponent());
							break;
						case 2:
							velocityContainer.put(CampusCourseDialogSelectionController.class.getSimpleName(),
									campusCourseControllerFactory
											.createContinueCampusCourseSelectionTableController(
													sapCampusCourseId, this, w, u)
											.getInitialComponent());
							break;
						default:
							velocityContainer.put(CampusCourseDialogSelectionController.class.getSimpleName(),
									campusCourseControllerFactory
											.createCampusCourseSubmitController(
													sapCampusCourseId, this, w, u)
											.getInitialComponent());
					}
				};

		CampusCourseCreationChoiceController campusCourseCreationChoiceController = campusCourseControllerFactory
				.createCampusCourseCreationChoiceController(listener,
						windowControl, userRequest);
		campusCourseCreationChoiceController.addControllerListener(this);

		velocityContainer.put(CampusCourseCreationChoiceController.class.getSimpleName(),
				campusCourseCreationChoiceController.getInitialComponent());
		velocityContainer.put(CampusCourseDialogSelectionController.class.getSimpleName(),
				campusCourseControllerFactory.createCampusCourseSubmitController(
						sapCampusCourseId, this, windowControl, userRequest).getInitialComponent());

		putInitialPanel(velocityContainer);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	}

	@Override
	protected void doDispose() {
	}

	public void addCampusCourseCreateEventListener(CreateCampusCourseCompletedEventListener eventListener) {
		createCampusCourseCompletedEventListeners.add(eventListener);
	}

	@Override
	public final void onError(UserRequest ureq, Exception e) {
		createCampusCourseCompletedEventListeners.forEach(l -> l.onError(ureq, e));
		createCampusCourseCompletedEventListeners.clear();
	}

	@Override
	public final void onSuccess(UserRequest ureq, RepositoryEntry repositoryEntry) {
		createCampusCourseCompletedEventListeners.forEach(l -> l.onSuccess(ureq, repositoryEntry));
		createCampusCourseCompletedEventListeners.clear();
	}

	@Override
	public final void onCancel(UserRequest ureq) {
		createCampusCourseCompletedEventListeners.forEach(l -> l.onCancel(ureq));
		createCampusCourseCompletedEventListeners.clear();
	}
}
