package ch.uzh.campus.olat;

import ch.uzh.campus.data.SapOlatUser;
import ch.uzh.campus.service.learn.CampusCourseService;
import ch.uzh.campus.service.learn.SapCampusCourseTo;
import org.olat.core.id.Persistable;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryModule;
import org.olat.repository.manager.coursequery.MyCourseRepositoryQuery;
import org.olat.repository.model.RepositoryEntryMyCourseImpl;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Initial date: 2016-06-15<br />
 * @author sev26 (UZH)
 */
@Service
public class CampusMyCourseRepositoryQuery implements MyCourseRepositoryQuery {

	private final static OLATResource CAMPUSKURS_RESOURCE_DUMMY = new OLATResource() {
		@Override
		public Date getCreationDate() {
			return null;
		}

		@Override
		public String getResourceableTypeName() {
			return "Campuskurs";
		}

		@Override
		public Long getResourceableId() {
			return 0L;
		}

		@Override
		public Long getKey() {
			return 0L;
		}

		@Override
		public boolean equalsByPersistableKey(Persistable persistable) {
			return false;
		}
	};

	private final CampusCourseService campusCourseService;
	private final RepositoryModule repositoryModule;

	@Autowired
	public CampusMyCourseRepositoryQuery(CampusCourseService campusCourseService,
										 RepositoryModule repositoryModule) {
		this.campusCourseService = campusCourseService;
		this.repositoryModule = repositoryModule;
	}

	@Override
	public int countViews(SearchMyRepositoryEntryViewParams param) {
		return 0;
	}

	@Override
	public List<RepositoryEntryMyView> searchViews(SearchMyRepositoryEntryViewParams param, int firstResult, int maxResults) {
		List<RepositoryEntryMyView> result = new ArrayList<>();
		List<SapCampusCourseTo> sapCampusCourseTos = campusCourseService.getCoursesWhichCouldBeCreated(param.getIdentity(),
				SapOlatUser.SapUserType.LECTURER,
				param.getIdRefsAndTitle());

		boolean requiresStatistics = repositoryModule.isRatingEnabled() || repositoryModule.isCommentEnabled();

		for (SapCampusCourseTo sapCampusCourseTo : sapCampusCourseTos) {
			RepositoryEntry repositoryEntry = getRepositoryEntry(sapCampusCourseTo);
			RepositoryEntryMyCourseImpl view = new RepositoryEntryMyCourseImpl(repositoryEntry,
					requiresStatistics ? repositoryEntry.getStatistics() : null, false, 0, 0);
			result.add(view);
		}

		return result;
	}

	private static RepositoryEntry getRepositoryEntry(SapCampusCourseTo sapCampusCourseTo) {
		RepositoryEntry result = new RepositoryEntry();
		result.setDisplayname(sapCampusCourseTo.getTitle());
		result.setOlatResource(CAMPUSKURS_RESOURCE_DUMMY);
		return result;
	}
}
