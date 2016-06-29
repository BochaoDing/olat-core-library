package ch.uzh.campus.olat;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.service.learn.CampusCourseService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.repository.ui.list.RepositoryEntryListController.RepositoryEntryListControllerFormInnerEventListener;
import org.olat.repository.ui.list.RepositoryEntryRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
@Component
public class CampusRepositoryEntryListControllerFormInnerEventListener extends RepositoryEntryListControllerFormInnerEventListener {

	private final CampusCourseService campusCourseService;
	private final CampusCourseConfiguration campusCourseConfiguration;

	@Autowired
	public CampusRepositoryEntryListControllerFormInnerEventListener(
			CampusCourseService campusCourseService,
			CampusCourseConfiguration campusCourseConfiguration) {
		this.campusCourseService = campusCourseService;
		this.campusCourseConfiguration = campusCourseConfiguration;
	}

	@Override
	public void event(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink formLink = (FormLink) source;
			if ("create".equals(formLink.getCmd())) {
				/**
				 * TODO sev26
				 * Set language.
				 */
				RepositoryEntryRow row = (RepositoryEntryRow) formLink.getUserObject();
				// TODO sev26
				//Long courseResourceableId = campusConfiguration.getTemplateCourseResourcableId(null);
				Long courseResourceableId = 93901196447328L;
				Long sapCampusCourseId = row.getKey();
				campusCourseService.createCampusCourseFromTemplate(courseResourceableId,
						sapCampusCourseId, ureq.getIdentity());
			}
		}
	}
}
