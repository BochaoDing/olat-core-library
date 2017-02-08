package ch.uzh.extension.campuscourse.presentation;

import org.olat.repository.RepositoryEntry;
import org.olat.repository.listener.AfterRepositoryEntryDeletionListener;
import org.olat.resource.OLATResource;
import org.springframework.stereotype.Component;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
@Component
public class CampusCourseAfterRepositoryEntryDeletionListener extends AfterRepositoryEntryDeletionListener {

	@Override
	public void onAction(RepositoryEntry repositoryEntry,
						 OLATResource resource) {
	}
}
