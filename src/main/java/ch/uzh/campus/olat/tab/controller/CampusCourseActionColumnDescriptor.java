package ch.uzh.campus.olat.tab.controller;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.repository.ui.RepositoryTableModel;

/**
 * Initial date: 2016-08-15<br />
 * @author sev26 (UZH)
 */
public class CampusCourseActionColumnDescriptor extends DefaultColumnDescriptor {

	private static String getAction(UserRequest userRequest) {
		return userRequest.getUserSession().getRoles().isAuthor() ?
				RepositoryTableModel.TABLE_ACTION_SELECT_LINK
				:
				null;
	}

	public CampusCourseActionColumnDescriptor(String headerKey,
											  UserRequest userRequest) {
		super(headerKey, -1, getAction(userRequest),
				userRequest.getLocale());
	}
}
