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

import static org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter;

/**
 * Initial date: 2016-06-15<br />
 * @author sev26 (UZH)
 */
public abstract class CampusCourseMyCourseRepositoryQuery implements MyCourseRepositoryQuery {

	protected final CampusCourseService campusCourseService;
	protected final RepositoryModule repositoryModule;

	public CampusCourseMyCourseRepositoryQuery(CampusCourseService campusCourseService,
											   RepositoryModule repositoryModule) {
		this.campusCourseService = campusCourseService;
		this.repositoryModule = repositoryModule;
	}

	@Override
	public int countViews(SearchMyRepositoryEntryViewParams param) {
		return searchViews(param, 0, Integer.MAX_VALUE).size();
	}

	@Override
	public List<RepositoryEntryMyView> searchViews(SearchMyRepositoryEntryViewParams param,
												   int firstResult,
												   int maxResults) {
		if ((param.getMarked() == null ||
				param.getMarked() == Boolean.FALSE) &&
				param.getParentEntry() == null && (param.getFilters() == null ||
				param.getFilters().contains(Filter.showAll) ||
				param.getFilters().contains(Filter.upcomingCourses) ||
				filter(param.getFilters()))) {

			List<RepositoryEntryMyView> result = new ArrayList<>();

			List<SapCampusCourseTo> sapCampusCourseTos = getSapCampusCourseTos(param);

			boolean requiresStatistics = repositoryModule.isRatingEnabled() ||
					repositoryModule.isCommentEnabled();

			for (SapCampusCourseTo sapCampusCourseTo : sapCampusCourseTos) {
				RepositoryEntry repositoryEntry = getRepositoryEntry(
						sapCampusCourseTo, param.getRoles());
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

	protected abstract List<SapCampusCourseTo> getSapCampusCourseTos(
			SearchMyRepositoryEntryViewParams param);

	protected abstract RepositoryEntry getRepositoryEntry(
			SapCampusCourseTo sapCampusCourseTo, Roles roles);
}
