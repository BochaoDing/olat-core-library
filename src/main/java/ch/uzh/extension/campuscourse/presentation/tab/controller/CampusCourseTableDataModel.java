package ch.uzh.extension.campuscourse.presentation.tab.controller;

import ch.uzh.extension.campuscourse.presentation.CampusCoursePresentationHelper;
import ch.uzh.extension.campuscourse.model.CampusCourseTOForUI;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.olat.repository.ui.RepositoryTableModel.RepoCols.displayname;

/**
 * Initial date: 2016-08-15<br />
 * @author sev26 (UZH)
 */
class CampusCourseTableDataModel extends DefaultTableDataModel<CampusCourseTOForUI> {

	private final boolean isAuthor;
	private final Locale locale;

	private CampusCourseTableDataModel(boolean isAuthor, Locale locale) {
		this(Collections.emptyList(), isAuthor, locale);
	}

	CampusCourseTableDataModel(List<CampusCourseTOForUI> objects,
							   boolean isAuthor, Locale locale) {
		super(objects);
		this.isAuthor = isAuthor;
		this.locale = locale;
	}

	@Override
	public int getColumnCount() {
		return objects.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == displayname.ordinal()) {
			CampusCourseTOForUI campusCourseTOForUI = objects.get(row);
			return campusCourseTOForUI.getTitle();
		} else {
			Translator translator = CampusCoursePresentationHelper
					.getTranslator(locale);
			return this.isAuthor ?
					translator.translate("list.course.create")
					:
					translator.translate("list.course.author.right.required");
		}
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new CampusCourseTableDataModel(isAuthor, locale);
	}
}
