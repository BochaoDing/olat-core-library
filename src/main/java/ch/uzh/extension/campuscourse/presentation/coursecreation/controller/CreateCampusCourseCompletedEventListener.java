package ch.uzh.extension.campuscourse.presentation.coursecreation.controller;

import org.olat.core.gui.UserRequest;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2016-08-09<br />
 * @author sev26 (UZH)
 */
public interface CreateCampusCourseCompletedEventListener {
	void onSuccess(UserRequest ureq, RepositoryEntry repositoryEntry);
	void onCancel(UserRequest ureq);
	void onError(UserRequest ureq, Exception e);
}
