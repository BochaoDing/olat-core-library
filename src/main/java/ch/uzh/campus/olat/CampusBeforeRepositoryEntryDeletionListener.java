package ch.uzh.campus.olat;

import ch.uzh.campus.service.core.CampusCourseCoreService;
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
public class CampusBeforeRepositoryEntryDeletionListener extends BeforeRepositoryEntryDeletionListener {

	private final CampusCourseCoreService campusCourseCoreService;

	@Autowired
	public CampusBeforeRepositoryEntryDeletionListener(
			CampusCourseCoreService campusCourseCoreService) {
		this.campusCourseCoreService = campusCourseCoreService;

	}

	@Override
	public void onAction(RepositoryEntry repositoryEntry,
						 OLATResource resource) {
		campusCourseCoreService.resetResourceableIdAndParentCourseReference(resource);
		campusCourseCoreService.deleteCampusCourseGroupsIfExist(repositoryEntry);
	}
}
