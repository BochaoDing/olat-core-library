package ch.uzh.campus.olat.dialog.controller;

import ch.uzh.campus.service.data.OlatCampusCourse;
import org.olat.core.gui.UserRequest;

/**
 * Initial date: 2016-08-09<br />
 * @author sev26 (UZH)
 */
public interface CreateCampusCourseCompletedEventListener {
	void onSuccess(UserRequest ureq, OlatCampusCourse olatCampusCourse);
	void onCancel(UserRequest ureq);
	void onError(UserRequest ureq, Exception e);
}
