package ch.uzh.campus.olat;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.olat.controller.CampusCourseSelectionController;
import ch.uzh.campus.service.CampusCourse;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.repository.ui.list.RepositoryEntryListController.RepositoryEntryListControllerFormInnerEventListener;
import org.olat.repository.ui.list.RepositoryEntryRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static ch.uzh.campus.olat.CampusCourseOlatHelper.showErrorCreatingCampusCourseFromDefaultTemplate;
import static ch.uzh.campus.olat.controller.CampusCourseSelectionController.CreateCampusCourseCompletedEventListener;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
@Component
public class CampusRepositoryEntryListControllerFormInnerEventListener extends RepositoryEntryListControllerFormInnerEventListener {

	private final CampusCourseConfiguration campusCourseConfiguration;
	private final CampusBeanFactory campusBeanFactory;
	private final CampusCourseOlatHelper campusCourseOlatHelper;

	@Autowired
	public CampusRepositoryEntryListControllerFormInnerEventListener(
			CampusCourseConfiguration campusCourseConfiguration,
			CampusBeanFactory campusBeanFactory,
			CampusCourseOlatHelper campusCourseOlatHelper
	) {
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
					try {
						Long courseResourceableId = campusCourseConfiguration.getTemplateCourseResourcableId(
								userRequest.getLocale().getLanguage());

						CampusCourse campusCourse = campusCourseOlatHelper.createCampusCourseFromResourcableId(userRequest, row.getKey(), courseResourceableId);
						campusCourseOlatHelper.openCourseInNewTab(campusCourse, windowControl, userRequest);
					} catch (Exception e) {
						showErrorCreatingCampusCourseFromDefaultTemplate(windowControl,
								userRequest.getLocale());
					}
					break;
				case "create_by_copying":
					{
						CampusCourseSelectionController controller = campusBeanFactory
								.createCreationCampusCourseSelectionTableController(windowControl,
										userRequest, row.getKey());
						controller.addControllerListener(parent);
						showDialog("campus.course.creation.title", controller,
								userRequest, windowControl, parent);
					}
					break;
				case "continue":
					{
						CampusCourseSelectionController controller = campusBeanFactory
								.createContinueCampusCourseSelectionTableController(row.getDisplayName(), windowControl,
										userRequest, row.getKey());
						controller.addControllerListener(parent);
						showDialog("campus.course.continue.title", controller,
								userRequest, windowControl, parent);
					}
					break;
			}
		}
	}

	private void showDialog(String titleKey, CampusCourseSelectionController controller,
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
				campusCourseOlatHelper.openCourseInNewTab(campusCourse,
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
