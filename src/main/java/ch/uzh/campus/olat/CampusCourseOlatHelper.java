package ch.uzh.campus.olat;

import ch.uzh.campus.olat.dialog.controller.CampusCourseCreateDialogController;
import ch.uzh.campus.olat.dialog.controller.CreateCampusCourseCompletedEventListener;
import ch.uzh.campus.service.data.OlatCampusCourse;
import ch.uzh.campus.service.data.SapCampusCourseTOForUI;
import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.event.EventBus;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static ch.uzh.campus.olat.CampusCourseBeanFactory.*;

@Component
public class CampusCourseOlatHelper {

	private static final OLog LOG = Tracing.createLoggerFor(
			CampusCourseOlatHelper.class);


	public void openCourseInNewTab(OlatCampusCourse olatCampusCourse,
								   WindowControl windowControl,
								   UserRequest userRequest) {
		/*
		 * Open the OLAT course in a new tab.
		 */
		String businessPath = "[RepositoryEntry:" + olatCampusCourse.getRepositoryEntry().getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, userRequest, windowControl);

		/*
		 * Inform the {@link AuthorListController} about the new list entry.
		 */
		EventBus singleUserEventBus = userRequest.getUserSession().getSingleUserEventCenter();
		singleUserEventBus.fireEventToListenersOf(
				new CampusCourseChangeEvent(),
				olatCampusCourse.getRepositoryEntry().getOlatResource());
	}

	public static Translator getTranslator(Locale locale) {
		return Util.createPackageTranslator(CampusCourseOlatHelper.class,
				locale);
	}

	public static Translator getTranslator(Locale locale, Class<?> fallbackTranslatorClazz) {
		return Util.createPackageTranslator(CampusCourseOlatHelper.class,
				locale, Util.createPackageTranslator(fallbackTranslatorClazz, locale));
	}

	private final static String CONTACT_OLAT_SUPPORT = "Please contact the OLAT support.";

	private static void showErrorCreatingCampusCourse(WindowControl windowControl) {
		windowControl.setError("An error occurred while creating the Campuskurs. " +
				CONTACT_OLAT_SUPPORT);
	}

	private final static OLATResource STUDENT_CAMPUS_COURSE_RESOURCE_DUMMY =
			new CampusCourseOlatResource(NOT_CREATED_CAMPUS_COURSE_KEY,
					STUDENT_RESOURCEABLE_TYPE_NAME);

	public static RepositoryEntry getStudentRepositoryEntry(SapCampusCourseTOForUI sapCampusCourseTOForUI) {
		RepositoryEntry result = getRepositoryEntry(sapCampusCourseTOForUI);
		result.setOlatResource(STUDENT_CAMPUS_COURSE_RESOURCE_DUMMY);
		return result;
	}

	private final static OLATResource LECTURER_CAMPUS_COURSE_RESOURCE_DUMMY =
			new CampusCourseOlatResource(NOT_CREATED_CAMPUS_COURSE_KEY,
					LECTURER_RESOURCEABLE_TYPE_NAME);

	private final static OLATResource AUTHOR_LECTURER_CAMPUS_COURSE_RESOURCE_DUMMY =
			new CampusCourseOlatResource(NOT_CREATED_CAMPUS_COURSE_KEY,
					AUTHOR_LECTURER_RESOURCEABLE_TYPE_NAME);

	public static RepositoryEntry getLecturerRepositoryEntry(SapCampusCourseTOForUI sapCampusCourseTOForUI, Roles roles) {
		RepositoryEntry result = getRepositoryEntry(sapCampusCourseTOForUI);
		if (roles.isAuthor()) {
			result.setOlatResource(AUTHOR_LECTURER_CAMPUS_COURSE_RESOURCE_DUMMY);
		} else {
			result.setOlatResource(LECTURER_CAMPUS_COURSE_RESOURCE_DUMMY);
		}
		return result;
	}

	private static RepositoryEntry getRepositoryEntry(SapCampusCourseTOForUI sapCampusCourseTOForUI) {
		RepositoryEntry result = new RepositoryEntry();
		result.setKey(sapCampusCourseTOForUI.getSapCourseId());
		result.setDisplayname(sapCampusCourseTOForUI.getTitle());
		return result;
	}

	public void showDialog(String titleKey, CampusCourseCreateDialogController controller,
								   Locale locale, WindowControl windowControl,
								   ControllerEventListener parent) {
		Translator translator = CampusCourseOlatHelper.getTranslator(locale);
		CloseableModalController cmc = new CloseableModalController(
				windowControl, translator.translate("close"), controller.getInitialComponent(), true,
				translator.translate(titleKey));
		cmc.addControllerListener(parent);
		cmc.activate();

		controller.addCampusCourseCreateEventListener(new CreateCampusCourseCompletedEventListener() {

			@Override
			public void onSuccess(UserRequest userRequest, OlatCampusCourse olatCampusCourse) {
				cmc.deactivate();
				CampusCourseOlatHelper.this.openCourseInNewTab(olatCampusCourse,
						windowControl, userRequest);
			}

			@Override
			public void onCancel(UserRequest ureq) {
				cmc.deactivate();
			}

			@Override
			public void onError(UserRequest ureq, Exception e) {
				LOG.error(e.getMessage());
				CampusCourseOlatHelper.showErrorCreatingCampusCourse(windowControl);
				cmc.deactivate();
			}
		});
	}
}
