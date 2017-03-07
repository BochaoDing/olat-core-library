package ch.uzh.extension.campuscourse.presentation;

import ch.uzh.extension.campuscourse.service.CampusCourseCoreService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.listener.BeforeRepositoryEntryDeletionListener;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial date: 2016-07-28<br />
 * @author Martin Schraner
 */
@Component
public class CampusCourseBeforeRepositoryEntryDeletionListener extends BeforeRepositoryEntryDeletionListener {

	private final CampusCourseCoreService campusCourseCoreService;

	@Autowired
	public CampusCourseBeforeRepositoryEntryDeletionListener(CampusCourseCoreService campusCourseCoreService) {
		this.campusCourseCoreService = campusCourseCoreService;

	}

	@Override
	public void onAction(RepositoryEntry repositoryEntry, OLATResource olatResource) {
		campusCourseCoreService.deleteCampusGroups(repositoryEntry);
		campusCourseCoreService.resetRepositoryEntryAndParentCourse(repositoryEntry);
	}
}
