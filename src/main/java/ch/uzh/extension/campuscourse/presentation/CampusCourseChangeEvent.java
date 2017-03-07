package ch.uzh.extension.campuscourse.presentation;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import org.olat.core.util.event.MultiUserEvent;

/**
 * Initial date: 2016-07-01<br />
 * @author sev26 (UZH)
 */
class CampusCourseChangeEvent extends MultiUserEvent {

	CampusCourseChangeEvent() {
		super(CampusCourseConfiguration.RESOURCEABLE_TYPE_NAME);
	}
}
