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

    public void createCampusCourseFromResourcableId(UserRequest userRequest,
													WindowControl windowControl,
													Long sapCampusCourseId,
													Long courseResourceableId) throws Exception {
        CampusCourse campusCourse = campusCourseService.createCampusCourseFromTemplate(courseResourceableId,
                sapCampusCourseId, userRequest.getIdentity());

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
}
