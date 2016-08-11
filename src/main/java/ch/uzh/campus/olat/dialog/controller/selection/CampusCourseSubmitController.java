package ch.uzh.campus.olat.dialog.controller.selection;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.olat.CampusCourseOlatHelper;
import ch.uzh.campus.olat.dialog.controller.CreateCampusCourseCompletedEventListener;
import ch.uzh.campus.service.CampusCourse;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class CampusCourseSubmitController extends BasicController {

	private final static String CREATE = "button.create.course";
	private final static String CANCEL = "cancel";

	private final Long sapCampusCourseId;
	private final CampusCourseConfiguration campusCourseConfiguration;
	private final CampusCourseOlatHelper campusCourseOlatHelper;
	private final CreateCampusCourseCompletedEventListener listener;

	private final Link createButton;
	private final Link cancelButton;

	public CampusCourseSubmitController(Long sapCampusCourseId,
										CampusCourseConfiguration campusCourseConfiguration,
										CampusCourseOlatHelper campusCourseOlatHelper,
			                            CreateCampusCourseCompletedEventListener listener,
										WindowControl windowControl,
										UserRequest userRequest) {
		super(userRequest, windowControl, CampusCourseOlatHelper
				.getTranslator(userRequest.getLocale()));

		this.sapCampusCourseId = sapCampusCourseId;
		this.campusCourseConfiguration = campusCourseConfiguration;
		this.campusCourseOlatHelper = campusCourseOlatHelper;
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
				Long courseResourceableId = campusCourseConfiguration.getTemplateCourseResourcableId(
						userRequest.getLocale().getLanguage());

				CampusCourse campusCourse = campusCourseOlatHelper
						.createCampusCourseFromResourcableId(userRequest,
								sapCampusCourseId,
								courseResourceableId);

				campusCourseOlatHelper.openCourseInNewTab(campusCourse, getWindowControl(), userRequest);
			} catch (Exception e) {
				CampusCourseOlatHelper.showErrorCreatingCampusCourseFromDefaultTemplate(
						getWindowControl(), userRequest.getLocale());
			}
		} else if (source == cancelButton) {
			listener.onCancel();
		}
	}

	@Override
	protected void doDispose() {
	}
}
