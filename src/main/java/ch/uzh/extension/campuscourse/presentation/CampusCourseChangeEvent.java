package ch.uzh.extension.campuscourse.presentation;

import org.olat.core.util.event.MultiUserEvent;

/**
 * Initial date: 2016-07-01<br />
 * @author sev26 (UZH)
 */
class CampusCourseChangeEvent extends MultiUserEvent {

	CampusCourseChangeEvent() {
		super(CampusOlatConfig.RESOURCEABLE_TYPE_NAME);
	}
}
