package ch.uzh.campus.olat;

import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.learn.CampusCourseService;
import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.event.EventBus;
import org.olat.repository.ui.author.AuthorListController;
import org.olat.repository.ui.author.AuthoringListChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static ch.uzh.campus.olat.CampusBeanFactory.RESOURCEABLE_TYPE_NAME;

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
}
