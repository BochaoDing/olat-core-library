package ch.uzh.campus.olat.dialog.controller;

import ch.uzh.campus.olat.CampusCourseBeanFactory;
import ch.uzh.campus.olat.dialog.controller.CampusCourseCreationChoiceController.CampusCourseCreationChoiceControllerListener;
import ch.uzh.campus.olat.dialog.controller.selection.CampusCourseDialogSelectionController;
import ch.uzh.campus.service.CampusCourse;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

import java.util.ArrayList;

/**
 * Initial date: 2016-08-09<br />
 * @author sev26 (UZH)
 */
public class CampusCourseCreateDialogController extends BasicController implements CreateCampusCourseCompletedEventListener {

	private ArrayList<CreateCampusCourseCompletedEventListener> createCampusCourseCompletedEventListeners =
			new ArrayList<>(1);
	private final CampusCourseCreationChoiceController campusCourseCreationChoiceController;
	private final VelocityContainer velocityContainer;

	public CampusCourseCreateDialogController(Long sapCampusCourseId,
											  CampusCourseBeanFactory campusCourseBeanFactory,
											  WindowControl windowControl,
											  UserRequest userRequest) {
		super(userRequest, windowControl);

		velocityContainer = createVelocityContainer(CampusCourseCreateDialogController.class.getSimpleName());

		CampusCourseCreationChoiceControllerListener listener =
				(x, w, u) -> {
					switch (x) {
						case 1:
							velocityContainer.put(CampusCourseDialogSelectionController.class.getSimpleName(),
									campusCourseBeanFactory
											.createCreationCampusCourseSelectionTableController(
													sapCampusCourseId, this, w, u)
											.getInitialComponent());
							break;
						case 2:
							velocityContainer.put(CampusCourseDialogSelectionController.class.getSimpleName(),
									campusCourseBeanFactory
											.createContinueCampusCourseSelectionTableController(
													sapCampusCourseId, this, w, u)
											.getInitialComponent());
							break;
						default:
							velocityContainer.put(CampusCourseDialogSelectionController.class.getSimpleName(),
									campusCourseBeanFactory
											.createCampusCourseSubmitController(
													sapCampusCourseId, this, w, u)
											.getInitialComponent());
					}
				};

		this.campusCourseCreationChoiceController = campusCourseBeanFactory
				.createCampusCourseCreationChoiceController(listener,
						windowControl, userRequest);
		this.campusCourseCreationChoiceController.addControllerListener(this);

		velocityContainer.put(CampusCourseCreationChoiceController.class.getSimpleName(),
				campusCourseCreationChoiceController.getInitialComponent());
		velocityContainer.put(CampusCourseDialogSelectionController.class.getSimpleName(),
				campusCourseBeanFactory.createCampusCourseSubmitController(
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
	public final void onError() {
		createCampusCourseCompletedEventListeners.forEach(CreateCampusCourseCompletedEventListener::onError);
		createCampusCourseCompletedEventListeners.clear();
	}

	@Override
	public final void onSuccess(CampusCourse campusCourse) {
		createCampusCourseCompletedEventListeners.forEach(l -> l.onSuccess(campusCourse));
		createCampusCourseCompletedEventListeners.clear();
	}

	@Override
	public final void onCancel() {
		createCampusCourseCompletedEventListeners.forEach(CreateCampusCourseCompletedEventListener::onCancel);
		createCampusCourseCompletedEventListeners.clear();
	}
}
