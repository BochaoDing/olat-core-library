package ch.uzh.campus.olat;

import ch.uzh.campus.service.core.CampusCourseCoreService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.listener.AfterRepositoryEntryDeletionListener;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
@Component
public class CampusAfterRepositoryEntryDeletionListener extends AfterRepositoryEntryDeletionListener {

	private final CampusCourseCoreService campusCourseCoreService;

	@Autowired
	public CampusAfterRepositoryEntryDeletionListener(
			CampusCourseCoreService campusCourseCoreService) {
		this.campusCourseCoreService = campusCourseCoreService;

	}

	@Override
	public void onAction(RepositoryEntry repositoryEntry,
						 OLATResource resource) {
		campusCourseCoreService.resetResourceableIdAndParentCourseReference(resource);
	}
}
