package ch.uzh.extension.campuscourse.olat.tab.controller;

import ch.uzh.extension.campuscourse.olat.CampusCourseBeanFactory;
import ch.uzh.extension.campuscourse.olat.CampusCourseOlatHelper;
import ch.uzh.extension.campuscourse.olat.common.controller.CampusCourseTableController;
import ch.uzh.extension.campuscourse.olat.coursecreation.controller.CampusCourseCreateDialogController;
import ch.uzh.extension.campuscourse.olat.tab.CampusCourseTab;
import ch.uzh.extension.campuscourse.service.CampusCourseService;
import ch.uzh.extension.campuscourse.model.CampusCourseTOForUI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StateSite;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.UserSession;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.resource.Resourceable;
import org.olat.util.logging.activity.LoggingResourceable;

import java.util.List;

/**
 * Initial date: 2016-08-05<br />
 * @author sev26 (UZH)
 */
public class CampusCourseTabTableController extends CampusCourseTableController<CampusCourseTOForUI> {

	private static TableGuiConfiguration createTableGuiConfiguration() {
		TableGuiConfiguration result = new TableGuiConfiguration();
		result.setPreferencesOffered(true, "CampusCourseCreationTableGuiPrefs");
		return result;
	}

	private static WindowControl getBusinessWindowControl(StateSite stateSite,
														  WindowControl windowControl,
														  UserRequest userRequest) {
		OLATResourceable olatResourceable = OresHelper
				.createOLATResourceableInstance(CampusCourseTab.class, 0L);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(
				LoggingResourceable.wrapBusinessPath(olatResourceable));
		return BusinessControlFactory.getInstance()
				.createBusinessWindowControl(userRequest, olatResourceable,
						stateSite, windowControl, true);
	}

	private final CampusCourseService campusCourseService;
	private final CampusCourseOlatHelper campusCourseOlatHelper;
	private final CampusCourseBeanFactory campusCourseBeanFactory;
	private final NavElement navElement;
	private final boolean isAuthor;
	private GenericEventListener campusCourseTabTableControllerListener;
	private UserSession userSession;
	private Resourceable courseModule;

	public CampusCourseTabTableController(CampusCourseService campusCourseService,
										  CampusCourseOlatHelper campusCourseOlatHelper,
										  CampusCourseBeanFactory campusCourseBeanFactory,
										  StateSite stateSite,
										  WindowControl windowControl,
										  UserRequest userRequest) {
		super(createTableGuiConfiguration(), null,
				getBusinessWindowControl(stateSite, windowControl, userRequest),
				userRequest);

		this.isAuthor = userRequest.getUserSession().getRoles().isAuthor();
		addColumnDescriptor(new CampusCourseActionColumnDescriptor(
				"tab.table.action.course",getLocale(), isAuthor));

		this.campusCourseService = campusCourseService;
		this.campusCourseOlatHelper = campusCourseOlatHelper;
		this.campusCourseBeanFactory = campusCourseBeanFactory;
		this.userSession = userRequest.getUserSession();
		campusCourseTabTableControllerListener = new CampusCourseTabTableControllerListener();
		courseModule = new Resourceable("CourseModule", null);

		/*
		 * Only if the user has author rights and campus courses are
		 * available, the tab is shown.
		 */
		if (reloadData() > 0) {

			/*
			 * TODO sev26
			 * Verify if this is the best way to inform the controller about
			 * list entry changes.
			 */
			userSession.getSingleUserEventCenter()
					.registerFor(campusCourseTabTableControllerListener, userRequest.getIdentity(), courseModule);

			Translator translator = CampusCourseOlatHelper.getTranslator(
					getLocale());
			NavElement tmp = new DefaultNavElement(translator
					.translate("topnav.campuscourses"), translator
					.translate("topnav.campuscourses.alt"),
					"o_site_campuscourse");
			tmp.setAccessKey("r".charAt(0));

			navElement = new DefaultNavElement(tmp);

			return;
		}

		navElement = null;
	}

	private int reloadData() {
		List<CampusCourseTOForUI> campusCourseTOForUIs = campusCourseService
				.getCoursesWhichCouldBeCreated(getIdentity(), "");

		setTableDataModel(new CampusCourseTableDataModel(campusCourseTOForUIs,
				isAuthor,
				getLocale()));
		modelChanged(true);

		return campusCourseTOForUIs.size();
	}

	@Override
	public void event(UserRequest userRequest, Component source, Event event) {
		super.event(userRequest, source, event);

		/*
		 * Only react if its an event triggered by the table rows and not by
		 * the surroundings like a "change sorting" event.
		 */
		if ("r".equals(event.getCommand())) {
			CampusCourseTOForUI campusCourseTOForUI = getSelectedEntry(event);

			CampusCourseCreateDialogController controller = campusCourseBeanFactory
					.createCampusCourseCreateDialogController(
							campusCourseTOForUI.getSapCourseId(),
							getWindowControl(),
							userRequest);
			controller.addControllerListener(this);

			campusCourseOlatHelper.showDialog("campus.course.creation.title",
					controller, userRequest.getLocale(), getWindowControl(), this);
		}
	}

	@Override
	public void doDispose() {
		userSession.getSingleUserEventCenter()
				.deregisterFor(campusCourseTabTableControllerListener, courseModule);
		super.doDispose();
	}

	public NavElement getNavElement() {
		return navElement;
	}

	private class CampusCourseTabTableControllerListener  implements GenericEventListener {
		@Override
		public void event(Event event) {
			reloadData();
		}
	}
}
