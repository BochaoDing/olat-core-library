package ch.uzh.campus.olat.list;

import ch.uzh.campus.olat.CampusCourseBeanFactory;
import ch.uzh.campus.olat.CampusCourseOlatHelper;
import ch.uzh.campus.olat.dialog.controller.CampusCourseCreateDialogController;
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

	private final CampusCourseBeanFactory campusCourseBeanFactory;
	private final CampusCourseOlatHelper campusCourseOlatHelper;

	@Autowired
	public CampusCourseRepositoryEntryListControllerFormInnerEventListener(
			CampusCourseBeanFactory campusCourseBeanFactory,
			CampusCourseOlatHelper campusCourseOlatHelper
	) {
		this.campusCourseBeanFactory = campusCourseBeanFactory;
		this.campusCourseOlatHelper = campusCourseOlatHelper;
	}

	@Override
	public void event(UserRequest userRequest, FormItem source, FormEvent event,
					  WindowControl windowControl, ControllerEventListener parent) {
		if (source instanceof FormLink) {
			FormLink formLink = (FormLink) source;
			if ("createCampusCourse".equals(formLink.getCmd())) {
				RepositoryEntryRow row = (RepositoryEntryRow) formLink.getUserObject();

				CampusCourseCreateDialogController controller = campusCourseBeanFactory
						.createCampusCourseCreateDialogController(row.getKey(),
								windowControl, userRequest);
				controller.addControllerListener(parent);
				campusCourseOlatHelper.showDialog("campus.course.creation.title",
						controller, userRequest.getLocale(), windowControl, parent);
			}
		}
	}
}
