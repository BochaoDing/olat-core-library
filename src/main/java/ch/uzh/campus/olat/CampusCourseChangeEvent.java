package ch.uzh.campus.olat;

import org.olat.core.util.event.MultiUserEvent;

import static ch.uzh.campus.olat.CampusCourseBeanFactory.RESOURCEABLE_TYPE_NAME;

/**
 * Initial date: 2016-07-01<br />
 * @author sev26 (UZH)
 */
public class CampusCourseChangeEvent extends MultiUserEvent {

	public CampusCourseChangeEvent() {
		super(RESOURCEABLE_TYPE_NAME);
	}
}
