package ch.uzh.extension.campuscourse.presentation.mycourses;

import ch.uzh.extension.campuscourse.presentation.CampusCoursePresentationHelper;
import ch.uzh.extension.campuscourse.presentation.CampusCourseControllerFactory;
import ch.uzh.extension.campuscourse.presentation.coursecreation.CampusCourseCreationDialogController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.ui.list.RepositoryEntryListController.RepositoryEntryListControllerFormInnerEventListener;
import org.olat.repository.ui.list.RepositoryEntryRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
@Component
public class CampusCourseRepositoryEntryListControllerFormInnerEventListener extends RepositoryEntryListControllerFormInnerEventListener {

	private final CampusCourseControllerFactory campusCourseControllerFactory;
	private final CampusCoursePresentationHelper campusCoursePresentationHelper;

	@Autowired
	public CampusCourseRepositoryEntryListControllerFormInnerEventListener(
			CampusCourseControllerFactory campusCourseControllerFactory,
			CampusCoursePresentationHelper campusCoursePresentationHelper
	) {
		this.campusCourseControllerFactory = campusCourseControllerFactory;
		this.campusCoursePresentationHelper = campusCoursePresentationHelper;
	}

	@Override
	public void event(UserRequest userRequest, FormItem source, FormEvent event,
					  WindowControl windowControl, ControllerEventListener parent) {
		if (source instanceof FormLink) {
			FormLink formLink = (FormLink) source;
			if ("createCampusCourse".equals(formLink.getCmd())) {
				RepositoryEntryRow row = (RepositoryEntryRow) formLink.getUserObject();

				CampusCourseCreationDialogController controller = campusCourseControllerFactory
						.createCampusCourseCreateDialogController(row.getKey(),
								windowControl, userRequest);
				controller.addControllerListener(parent);
				campusCoursePresentationHelper.showDialog("campus.course.creation.title",
						controller, userRequest.getLocale(), windowControl, parent);
			}
		}
	}
}
