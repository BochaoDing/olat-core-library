package ch.uzh.campus.olat;

import ch.uzh.campus.olat.dialog.controller.CampusCourseCreateDialogController;
import ch.uzh.campus.olat.dialog.controller.CreateCampusCourseCompletedEventListener;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.learn.CampusCourseService;
import ch.uzh.campus.service.learn.SapCampusCourseTo;
import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.event.EventBus;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.AuthorListController;
import org.olat.repository.ui.author.AuthoringListChangeEvent;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static ch.uzh.campus.olat.CampusCourseBeanFactory.NOT_CREATED_CAMPUSKURS_KEY;
import static ch.uzh.campus.olat.CampusCourseBeanFactory.RESOURCEABLE_TYPE_NAME;

@Component
public class CampusCourseOlatHelper {

    private final CampusCourseService campusCourseService;

    @Autowired
    public CampusCourseOlatHelper(CampusCourseService campusCourseService) {
        this.campusCourseService = campusCourseService;
    }

    public CampusCourse createCampusCourseFromResourcableId(UserRequest userRequest,
													Long sapCampusCourseId,
													Long courseResourceableId) throws Exception {
        return campusCourseService.createCampusCourseFromTemplate(courseResourceableId,
                sapCampusCourseId, userRequest.getIdentity());
    }

	public void openCourseInNewTab(CampusCourse campusCourse,
								   WindowControl windowControl,
								   UserRequest userRequest) {
		/**
		 * Open the OLAT course in a new tab.
		 */
		String businessPath = "[RepositoryEntry:" + campusCourse.getRepositoryEntry().getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, userRequest, windowControl);

		/**
		 * Inform the {@link AuthorListController} about the new list entry.
		 */
		EventBus singleUserEventBus = userRequest.getUserSession().getSingleUserEventCenter();
		singleUserEventBus.fireEventToListenersOf(
				new AuthoringListChangeEvent(RESOURCEABLE_TYPE_NAME),
				campusCourse.getRepositoryEntry().getOlatResource());
	}

	public static Translator getTranslator(Locale locale) {
		return Util.createPackageTranslator(CampusCourseOlatHelper.class, locale);
	}

	private final static String CONTACT_OLAT_SUPPORT = "Please contact the OLAT support.";

	public static void showErrorCreatingCampusCourseFromDefaultTemplate(WindowControl windowControl,
																		Locale locale) {
		windowControl.setError("An error occurred while crating a Campuskurs from the default template. " +
				CONTACT_OLAT_SUPPORT
		);
	}

	public static void showErrorCreatingCampusCourse(WindowControl windowControl,
													 Locale locale) {
		windowControl.setError("An error occurred while crating the Campuskurs. " +
				CONTACT_OLAT_SUPPORT);
	}

	private final static OLATResource CAMPUSKURS_RESOURCE_DUMMY =
			new CampusCourseOlatResource(NOT_CREATED_CAMPUSKURS_KEY);

	public static RepositoryEntry getRepositoryEntry(SapCampusCourseTo sapCampusCourseTo) {
		RepositoryEntry result = new RepositoryEntry();
		result.setKey(sapCampusCourseTo.getSapCourseId());
		result.setDisplayname(sapCampusCourseTo.getTitle());
		result.setOlatResource(CAMPUSKURS_RESOURCE_DUMMY);
		return result;
	}

	public void showDialog(String titleKey, CampusCourseCreateDialogController controller,
								   UserRequest userRequest, WindowControl windowControl,
								   ControllerEventListener parent) {
		Translator translator = CampusCourseOlatHelper.getTranslator(userRequest.getLocale());
		CloseableModalController cmc = new CloseableModalController(
				windowControl, translator.translate("close"), controller.getInitialComponent(), true,
				translator.translate(titleKey));
		cmc.addControllerListener(parent);
		cmc.activate();

		controller.addCampusCourseCreateEventListener(new CreateCampusCourseCompletedEventListener() {

			@Override
			public void onSuccess(CampusCourse campusCourse) {
				cmc.deactivate();
				CampusCourseOlatHelper.this.openCourseInNewTab(campusCourse,
						windowControl, userRequest);
			}

			@Override
			public void onCancel() {
				cmc.deactivate();
			}

			@Override
			public void onError() {
				CampusCourseOlatHelper.showErrorCreatingCampusCourse(windowControl,
						userRequest.getLocale());
				cmc.deactivate();
			}
		});
	}
}
