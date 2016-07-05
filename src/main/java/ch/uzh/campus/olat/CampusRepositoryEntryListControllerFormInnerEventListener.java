package ch.uzh.campus.olat;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.olat.popup.CampusCourseCreationController;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.learn.CampusCourseService;
import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.event.EventBus;
import org.olat.repository.ui.author.AuthorListController;
import org.olat.repository.ui.author.AuthoringListChangeEvent;
import org.olat.repository.ui.list.RepositoryEntryListController.RepositoryEntryListControllerFormInnerEventListener;
import org.olat.repository.ui.list.RepositoryEntryRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static ch.uzh.campus.olat.CampusBeanFactory.RESOURCEABLE_TYPE_NAME;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
@Component
public class CampusRepositoryEntryListControllerFormInnerEventListener extends RepositoryEntryListControllerFormInnerEventListener {

	private final CampusCourseService campusCourseService;
	private final CampusCourseConfiguration campusCourseConfiguration;
	private final CampusBeanFactory campusBeanFactory;
	private final CampusCourseOlatHelper campusCourseOlatHelper;

	@Autowired
	public CampusRepositoryEntryListControllerFormInnerEventListener(
			CampusCourseService campusCourseService,
			CampusCourseConfiguration campusCourseConfiguration,
			CampusBeanFactory campusBeanFactory,
			CampusCourseOlatHelper campusCourseOlatHelper
	) {
		this.campusCourseService = campusCourseService;
		this.campusCourseConfiguration = campusCourseConfiguration;
		this.campusBeanFactory = campusBeanFactory;
		this.campusCourseOlatHelper = campusCourseOlatHelper;
	}

	@Override
	public void event(UserRequest userRequest, FormItem source, FormEvent event,
					  WindowControl windowControl, ControllerEventListener parent) {
		if (source instanceof FormLink) {
			FormLink formLink = (FormLink) source;

			RepositoryEntryRow row = (RepositoryEntryRow) formLink.getUserObject();
			switch (formLink.getCmd()) {
				case "create":
					/**
					 * TODO sev26
					 * Resourceable id + set language.
					 */
					//Long courseResourceableId = campusCourseConfiguration.getTemplateCourseResourcableId(null);
					Long courseResourceableId = 93901584917897L;
					campusCourseOlatHelper.createCampusCourseFromResourcableId(userRequest, windowControl, row.getKey(), courseResourceableId);
					break;
				case "create_by_copying":
				case "continue":
					Translator translator = Util.createPackageTranslator(
							CampusCourseCreationController.class,
							userRequest.getLocale());

					CampusCourseCreationController campusCourseCreationController = campusBeanFactory
							.createCampusCourseCreationController("FooBar", windowControl, userRequest, formLink.getCmd(), row.getKey());
					campusCourseCreationController.addControllerListener(parent);

					String titleKey = formLink.getCmd().equals("continue") ? "campus.course.continue.title" : "campus.course.creation.title";
					CloseableModalController cmc = new CloseableModalController(
							windowControl, translator.translate("close"),
							campusCourseCreationController.getInitialComponent(),
							true, translator.translate(titleKey));
					cmc.addControllerListener(parent);
					cmc.activate();

					campusCourseCreationController.setCmc(cmc);

					break;

			}
		}
	}


}
