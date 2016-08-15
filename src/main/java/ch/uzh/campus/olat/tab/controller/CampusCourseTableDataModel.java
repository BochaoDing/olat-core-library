package ch.uzh.campus.olat.tab.controller;

import ch.uzh.campus.olat.CampusCourseOlatHelper;
import ch.uzh.campus.service.learn.SapCampusCourseTo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;

import java.util.Collections;
import java.util.List;

import static org.olat.repository.ui.RepositoryTableModel.RepoCols.displayname;
/**
 * Initial date: 2016-08-15<br />
 * @author sev26 (UZH)
 */
public class CampusCourseTableDataModel extends DefaultTableDataModel<SapCampusCourseTo> {

	private final UserRequest userRequest;

	public CampusCourseTableDataModel(UserRequest userRequest) {
		this(Collections.emptyList(), userRequest);
	}

	public CampusCourseTableDataModel(List<SapCampusCourseTo> objects,
									  UserRequest userRequest) {
		super(objects);
		this.userRequest = userRequest;
	}

	@Override
	public int getColumnCount() {
		return objects.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == displayname.ordinal()) {
			SapCampusCourseTo sapCampusCourseTo = objects.get(row);
			return sapCampusCourseTo.getTitle();
		} else {
			Translator translator = CampusCourseOlatHelper
					.getTranslator(userRequest.getLocale());
			return userRequest.getUserSession().getRoles().isAuthor() ?
					translator.translate("list.course.create")
					:
					translator.translate("list.course.author.right.required");
		}
	}
}
