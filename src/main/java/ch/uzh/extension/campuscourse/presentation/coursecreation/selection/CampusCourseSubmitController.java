package ch.uzh.extension.campuscourse.presentation.coursecreation.selection;

import ch.uzh.extension.campuscourse.presentation.CampusCoursePresentationHelper;
import ch.uzh.extension.campuscourse.presentation.coursecreation.CreateCampusCourseCompletedEventListener;
import ch.uzh.extension.campuscourse.service.CampusCourseService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class CampusCourseSubmitController extends BasicController {

	private final static String CREATE = "button.create.course";
	private final static String CANCEL = "cancel";

	private final Long sapCampusCourseId;
	private final CampusCourseService campusCourseService;
	private final CampusCoursePresentationHelper campusCoursePresentationHelper;
	private final CreateCampusCourseCompletedEventListener listener;

	private final Link createButton;
	private final Link cancelButton;

	public CampusCourseSubmitController(Long sapCampusCourseId,
										CampusCourseService campusCourseService,
										CampusCoursePresentationHelper campusCoursePresentationHelper,
										CreateCampusCourseCompletedEventListener listener,
										WindowControl windowControl,
										UserRequest userRequest) {
		super(userRequest, windowControl, CampusCoursePresentationHelper
				.getTranslator(userRequest.getLocale()));

		this.sapCampusCourseId = sapCampusCourseId;
		this.campusCourseService = campusCourseService;
		this.campusCoursePresentationHelper = campusCoursePresentationHelper;
		this.listener = listener;

		VelocityContainer velocityContainer = createVelocityContainer(
				CampusCourseSubmitController.class.getSimpleName());
		velocityContainer.remove(getInitialComponent());

		this.createButton = LinkFactory.createButton(CREATE,
				velocityContainer, this);
		this.cancelButton = LinkFactory.createButton(CANCEL,
				velocityContainer, this);

		putInitialPanel(velocityContainer);
	}

	@Override
	protected void event(UserRequest userRequest, Component source, Event event) {
		if (source == createButton) {
			try {
				RepositoryEntry createdRepositoryEntry = campusCourseService
						.createOlatCampusCourseFromStandardTemplate(sapCampusCourseId,
								userRequest.getIdentity());

				listener.onCancel(userRequest);
				campusCoursePresentationHelper.openCourseInNewTab(createdRepositoryEntry, getWindowControl(), userRequest);
			} catch (Exception e) {
				listener.onError(userRequest, e);
			}
		} else if (source == cancelButton) {
			listener.onCancel(userRequest);
		}
	}

	@Override
	protected void doDispose() {
	}
}