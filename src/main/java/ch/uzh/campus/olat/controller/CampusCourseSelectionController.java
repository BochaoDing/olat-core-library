package ch.uzh.campus.olat.controller;

import ch.uzh.campus.olat.CampusCourseOlatHelper;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.learn.CampusCourseService;
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

import java.util.ArrayList;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class CampusCourseSelectionController extends BasicController {

	private final static String CANCEL = "cancel";
	private final static int RESULTS_PER_PAGE = 5;

	protected final Long sapCampusCourseId;
	protected final CampusCourseService campusCourseService;
	protected final RepositoryManager repositoryManager;
	protected final CampusCourseOlatHelper campusCourseOlatHelper;
	protected final UserRequest userRequest;

	protected final VelocityContainer campusCourseVC;
	protected final CampusCourseTableController table;
	protected final Link cancelButton;

	private ArrayList<CreateCampusCourseCompletedEventListener> createCampusCourseCompletedEventListeners = new ArrayList<>(1);

	public interface CreateCampusCourseCompletedEventListener {
		void onSuccess(CampusCourse campusCourse);
		void onCancel();
		void onError();
	}

	public void addCampusCourseCreateEventListener(CreateCampusCourseCompletedEventListener eventListener) {
		createCampusCourseCompletedEventListeners.add(eventListener);
	}

	protected final void onError() {
		createCampusCourseCompletedEventListeners.forEach(CreateCampusCourseCompletedEventListener::onError);
		createCampusCourseCompletedEventListeners.clear();
	}

	protected final void onSuccess(CampusCourse campusCourse) {
		createCampusCourseCompletedEventListeners.forEach(l -> l.onSuccess(campusCourse));
		createCampusCourseCompletedEventListeners.clear();
	}

	protected final void onCancel() {
		createCampusCourseCompletedEventListeners.forEach(CreateCampusCourseCompletedEventListener::onCancel);
		createCampusCourseCompletedEventListeners.clear();
	}

	private static TableGuiConfiguration createTableGuiConfiguration() {
		TableGuiConfiguration result = new TableGuiConfiguration();
		result.setResultsPerPage(RESULTS_PER_PAGE);
		result.setPreferencesOffered(true, "CampusCourseCreationTableGuiPrefs");
		return result;
	}

	CampusCourseSelectionController(Long sapCampusCourseId,
										   CampusCourseService campusCourseService,
										   RepositoryManager repositoryManager,
										   CampusCourseOlatHelper campusCourseOlatHelper,
										   WindowControl windowControl,
										   UserRequest userRequest) {
		super(userRequest, windowControl, CampusCourseOlatHelper.getTranslator(userRequest.getLocale()));

		this.sapCampusCourseId = sapCampusCourseId;
		this.campusCourseService = campusCourseService;
		this.repositoryManager = repositoryManager;
		this.campusCourseOlatHelper = campusCourseOlatHelper;
		this.userRequest = userRequest;

		this.table = new CampusCourseTableController(createTableGuiConfiguration(),
				userRequest, windowControl,
				CampusCourseOlatHelper.getTranslator(userRequest.getLocale()));
		listenTo(table);

		this.campusCourseVC = createVelocityContainer("campusCourseCreation");
		campusCourseVC.remove(getInitialComponent());
		campusCourseVC.put("tableController", table.getInitialComponent());

		this.cancelButton = LinkFactory.createButton(CANCEL, campusCourseVC, this);

		putInitialPanel(campusCourseVC);
	}

	@Override
	protected final void event(UserRequest ureq, Component source, Event event) {
		if (source == cancelButton) {
			onCancel();
		}
	}

	@Override
	protected final void doDispose() {
	}
}
