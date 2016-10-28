package ch.uzh.campus.olat.tab.controller;

import ch.uzh.campus.olat.CampusCourseOlatHelper;
import ch.uzh.campus.service.data.SapCampusCourseTOForUI;
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
public class CampusCourseTableDataModel extends DefaultTableDataModel<SapCampusCourseTOForUI> {

	private final boolean isAuthor;
	private final Locale locale;

	public CampusCourseTableDataModel(boolean isAuthor, Locale locale) {
		this(Collections.emptyList(), isAuthor, locale);
	}

	public CampusCourseTableDataModel(List<SapCampusCourseTOForUI> objects,
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
			SapCampusCourseTOForUI sapCampusCourseTOForUI = objects.get(row);
			return sapCampusCourseTOForUI.getTitle();
		} else {
			Translator translator = CampusCourseOlatHelper
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
