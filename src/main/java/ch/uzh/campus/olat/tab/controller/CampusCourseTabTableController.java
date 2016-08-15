package ch.uzh.campus.olat.tab.controller;

import ch.uzh.campus.data.SapOlatUser;
import ch.uzh.campus.olat.CampusCourseBeanFactory;
import ch.uzh.campus.olat.CampusCourseOlatHelper;
import ch.uzh.campus.olat.controller.CampusCourseTableController;
import ch.uzh.campus.olat.dialog.controller.CampusCourseCreateDialogController;
import ch.uzh.campus.olat.tab.CampusCourseTab;
import ch.uzh.campus.service.learn.CampusCourseService;
import ch.uzh.campus.service.learn.SapCampusCourseTo;
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
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.resource.Resourceable;
import org.olat.util.logging.activity.LoggingResourceable;

import java.util.List;

/**
 * Initial date: 2016-08-05<br />
 * @author sev26 (UZH)
 */
public class CampusCourseTabTableController extends CampusCourseTableController<SapCampusCourseTo> {

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

	public CampusCourseTabTableController(CampusCourseService campusCourseService,
										  CampusCourseOlatHelper campusCourseOlatHelper,
										  CampusCourseBeanFactory campusCourseBeanFactory,
										  StateSite stateSite,
										  WindowControl windowControl,
										  UserRequest userRequest) {
		super(createTableGuiConfiguration(), null,
				getBusinessWindowControl(stateSite, windowControl, userRequest),
				userRequest);

		addColumnDescriptor(new CampusCourseActionColumnDescriptor(
				"tab.table.action.course", userRequest));

		this.campusCourseService = campusCourseService;
		this.campusCourseOlatHelper = campusCourseOlatHelper;
		this.campusCourseBeanFactory = campusCourseBeanFactory;

		/**
		 * Only if the user has author rights and campus courses are
		 * available, the tab is shown.
		 */
		if (reloadData(userRequest) > 0) {

			/**
			 * TODO sev26
			 * Verify if this is the best way to inform the controller about
			 * list entry changes.
			 */
			userRequest.getUserSession().getSingleUserEventCenter()
					.registerFor(new GenericEventListener() {
				@Override
				public void event(Event event) {
					reloadData(userRequest);
				}
			}, userRequest.getIdentity(), new Resourceable("CourseModule",
					null));

			Translator translator = CampusCourseOlatHelper.getTranslator(
					userRequest.getLocale());
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

	private int reloadData(UserRequest userRequest) {
		List<SapCampusCourseTo> sapCampusCourseTos = campusCourseService
				.getCoursesWhichCouldBeCreated(getIdentity(),
						SapOlatUser.SapUserType.LECTURER, "");

		setTableDataModel(new CampusCourseTableDataModel(sapCampusCourseTos,
				userRequest.getUserSession().getRoles().isAuthor(),
				userRequest.getLocale()));
		modelChanged(true);

		return sapCampusCourseTos.size();
	}

	@Override
	public void event(UserRequest userRequest, Component source, Event event) {
		super.event(userRequest, source, event);

		/**
		 * Only react if its an event triggered by the table rows and not by
		 * the surroundings like a "change sorting" event.
		 */
		if ("r".equals(event.getCommand())) {
			SapCampusCourseTo sapCampusCourseTo = getSelectedEntry(event);

			CampusCourseCreateDialogController controller = campusCourseBeanFactory
					.createCampusCourseCreateDialogController(
							sapCampusCourseTo.getSapCourseId(),
							sapCampusCourseTo.getTitle(), getWindowControl(),
							userRequest);
			controller.addControllerListener(this);

			campusCourseOlatHelper.showDialog("campus.course.creation.title",
					controller, userRequest, getWindowControl(), this);
		}
	}

	public NavElement getNavElement() {
		return navElement;
	}
}
