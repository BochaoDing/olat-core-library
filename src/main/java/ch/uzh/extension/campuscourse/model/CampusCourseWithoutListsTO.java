package ch.uzh.extension.campuscourse.model;

import org.olat.repository.RepositoryEntry;

/**
 * @author Martin Schraner
 */
public class CampusCourseWithoutListsTO {

	private final Long sapCourseId;
	private final Long parentSapCourseId;
	private final RepositoryEntry repositoryEntry;

	public CampusCourseWithoutListsTO(Long sapCourseId, Long parentSapCourseId, RepositoryEntry repositoryEntry) {
		this.sapCourseId = sapCourseId;
		this.parentSapCourseId = parentSapCourseId;
		this.repositoryEntry = repositoryEntry;
	}

	public Long getSapCourseId() {
		return sapCourseId;
	}

	public Long getParentSapCourseId() {
		return parentSapCourseId;
	}

	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}
}
