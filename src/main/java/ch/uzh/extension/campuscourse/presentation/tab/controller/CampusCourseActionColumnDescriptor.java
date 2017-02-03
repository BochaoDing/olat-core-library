package ch.uzh.extension.campuscourse.presentation.tab.controller;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.repository.ui.RepositoryTableModel;

import java.util.Locale;

/**
 * Initial date: 2016-08-15<br />
 * @author sev26 (UZH)
 */
class CampusCourseActionColumnDescriptor extends DefaultColumnDescriptor {

	private static String getAction(boolean isAuthor) {
		return isAuthor ?
				RepositoryTableModel.TABLE_ACTION_SELECT_LINK
				:
				null;
	}
	CampusCourseActionColumnDescriptor(String headerKey,
									   Locale locale,
									   boolean isAuthor) {
		super(headerKey, -1, getAction(isAuthor),
				locale);
	}
}
