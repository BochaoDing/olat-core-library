package ch.uzh.campus.olat.dialog.controller;

import ch.uzh.campus.service.CampusCourse;

/**
 * Initial date: 2016-08-09<br />
 * @author sev26 (UZH)
 */
public interface CreateCampusCourseCompletedEventListener {
	void onSuccess(CampusCourse campusCourse);
	void onCancel();
	void onError(Exception e);
}
