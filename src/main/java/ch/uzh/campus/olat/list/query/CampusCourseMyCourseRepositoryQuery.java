package ch.uzh.campus.olat.list.query;

import ch.uzh.campus.service.learn.CampusCourseService;
import ch.uzh.campus.service.learn.SapCampusCourseTo;
import org.olat.core.id.Roles;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryModule;
import org.olat.repository.manager.coursequery.MyCourseRepositoryQuery;
import org.olat.repository.model.RepositoryEntryMyCourseImpl;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ch.uzh.campus.data.SapOlatUser.SapUserType;
import static org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter;

/**
 * Initial date: 2016-06-15<br />
 * @author sev26 (UZH)
 */
public abstract class CampusCourseMyCourseRepositoryQuery implements MyCourseRepositoryQuery {

	private final SapUserType userType;
	private final CampusCourseService campusCourseService;
	private final RepositoryModule repositoryModule;

	public CampusCourseMyCourseRepositoryQuery(SapUserType userType,
											   CampusCourseService campusCourseService,
											   RepositoryModule repositoryModule) {
		this.userType = userType;
		this.campusCourseService = campusCourseService;
		this.repositoryModule = repositoryModule;
	}

	@Override
	public int countViews(SearchMyRepositoryEntryViewParams param) {
		return 0;
	}

	@Override
	public List<RepositoryEntryMyView> searchViews(SearchMyRepositoryEntryViewParams params,
												   int firstResult,
												   int maxResults) {
		if ((params.getMarked() == null ||
				params.getMarked() == Boolean.FALSE) &&
				params.getParentEntry() == null && (params.getFilters() == null ||
				params.getFilters().contains(Filter.showAll) ||
				params.getFilters().contains(Filter.upcomingCourses) ||
				filter(params.getFilters()))) {

			List<RepositoryEntryMyView> result = new ArrayList<>();

			List<SapCampusCourseTo> sapCampusCourseTos = campusCourseService.getCoursesWhichCouldBeCreated(
					params.getIdentity(), userType, params.getIdRefsAndTitle());

			boolean requiresStatistics = repositoryModule.isRatingEnabled() || repositoryModule.isCommentEnabled();

			for (SapCampusCourseTo sapCampusCourseTo : sapCampusCourseTos) {
				RepositoryEntry repositoryEntry = getRepositoryEntry(
						sapCampusCourseTo, params.getRoles());
				RepositoryEntryMyCourseImpl view = new RepositoryEntryMyCourseImpl(
						repositoryEntry,
						requiresStatistics ? repositoryEntry.getStatistics() : null, false,
						0, 0);
				result.add(view);
			}

			return result;
		} else {
			return Collections.emptyList();
		}
	}

	protected abstract boolean filter(List<Filter> filters);

	protected abstract RepositoryEntry getRepositoryEntry(
			SapCampusCourseTo sapCampusCourseTo, Roles roles);
}
