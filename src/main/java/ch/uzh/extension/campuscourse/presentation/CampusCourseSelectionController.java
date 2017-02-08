package ch.uzh.extension.campuscourse.presentation;

import ch.uzh.extension.campuscourse.presentation.coursecreation.CreateCampusCourseCompletedEventListener;
import ch.uzh.extension.campuscourse.service.CampusCourseService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.repository.RepositoryManager;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class CampusCourseSelectionController<T> extends BasicController {

	private final static String CANCEL = "cancel";
	private final static int RESULTS_PER_PAGE = 5;

	protected final Long sapCampusCourseId;
	protected final CampusCourseService campusCourseService;
	protected final RepositoryManager repositoryManager;
	protected final CreateCampusCourseCompletedEventListener listener;
	protected final UserRequest userRequest;

	protected final CampusCourseTableController<T> table;
	protected final Link cancelButton;

	protected static TableGuiConfiguration createTableGuiConfiguration() {
		TableGuiConfiguration result = new TableGuiConfiguration();
		result.setResultsPerPage(RESULTS_PER_PAGE);
		result.setPreferencesOffered(true, "CampusCourseCreationTableGuiPrefs");
		return result;
	}

	public CampusCourseSelectionController(Long sapCampusCourseId,
										   CampusCourseService campusCourseService,
										   RepositoryManager repositoryManager,
										   CreateCampusCourseCompletedEventListener listener,
										   WindowControl windowControl,
										   UserRequest userRequest) {
		super(userRequest, windowControl,
				CampusCoursePresentationHelper.getTranslator(userRequest.getLocale()));

		this.sapCampusCourseId = sapCampusCourseId;
		this.campusCourseService = campusCourseService;
		this.repositoryManager = repositoryManager;
		this.listener = listener;
		this.userRequest = userRequest;

		this.table = createTableController(windowControl, userRequest);
		listenTo(table);

		VelocityContainer velocityContainer = createVelocityContainer(
				CampusCourseSelectionController.class.getSimpleName());
		velocityContainer.remove(getInitialComponent());
		velocityContainer.put(CampusCourseTableController.class.getSimpleName(),
				table.getInitialComponent());

		this.cancelButton = LinkFactory.createButton(CANCEL, velocityContainer, this);

		putInitialPanel(velocityContainer);
	}

	protected CampusCourseTableController<T> createTableController(WindowControl windowControl,
																UserRequest userRequest) {
		return new CampusCourseTableController<>(createTableGuiConfiguration(),
				null, windowControl, userRequest);
	}

	@Override
	protected final void event(UserRequest userRequest, Component source,
							   Event event) {
		if (source == cancelButton) {
			listener.onCancel(userRequest);
		}
	}

	@Override
	protected final void doDispose() {
	}
}
