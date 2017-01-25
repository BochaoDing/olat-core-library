package ch.uzh.extension.campuscourse.olat.list;

import ch.uzh.extension.campuscourse.olat.list.query.CampusCourseMyCourseRepositoryQuery;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.manager.RepositoryEntryMyCourseQueries;
import org.olat.repository.manager.coursequery.MyCourseRepositoryQuery;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * Initial date: 2016-06-15<br />
 * @author sev26 (UZH)
 */
@Service
@Primary // This service implementation should preferred over the default implementation.
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class CombineMyCourseRepositoryQuery implements MyCourseRepositoryQuery {

	private final RepositoryEntryMyCourseQueries repositoryEntryMyCourseQueries;
	private final CampusCourseMyCourseRepositoryQuery[] campusCourseMyCourseRepositoryQueries;

	@Autowired
	public CombineMyCourseRepositoryQuery(RepositoryEntryMyCourseQueries repositoryEntryMyCourseQueries,
										  CampusCourseMyCourseRepositoryQuery[] campusCourseMyCourseRepositoryQueries) {
		this.repositoryEntryMyCourseQueries = repositoryEntryMyCourseQueries;
		this.campusCourseMyCourseRepositoryQueries = campusCourseMyCourseRepositoryQueries;
	}

	@Override
	public int countViews(SearchMyRepositoryEntryViewParams param) {
		int result = 0;

		for (CampusCourseMyCourseRepositoryQuery campusCourseMyCourseRepositoryQuery : campusCourseMyCourseRepositoryQueries) {
			result += campusCourseMyCourseRepositoryQuery.countViews(param);
		}

		result += repositoryEntryMyCourseQueries.countViews(param);

		return result;
	}

	@Override
	public List<RepositoryEntryMyView> searchViews(SearchMyRepositoryEntryViewParams param, int firstResult, int maxResults) {
		List<RepositoryEntryMyView> result = new LinkedList<>();

		for (CampusCourseMyCourseRepositoryQuery campusCourseMyCourseRepositoryQuery : campusCourseMyCourseRepositoryQueries) {
			result.addAll(campusCourseMyCourseRepositoryQuery.searchViews(
					param, 0, Integer.MAX_VALUE));
		}

		int offset = result.size();

		for (int slice = maxResults; slice < firstResult + maxResults; slice += maxResults) {
			if (result.size() > maxResults) {
				result.subList(0, maxResults).clear();
				offset = result.size();
			} else {
				result.clear();
			}
		}

		if (result.size() > maxResults) {
			result = result.subList(0, maxResults);
		}

		/*
		 * OpenOLAT courses must always come last.
		 */
		if (result.size() > 0) {
			result.addAll(repositoryEntryMyCourseQueries.searchViews(param,
					firstResult, maxResults - offset));
		} else {
			result.addAll(repositoryEntryMyCourseQueries.searchViews(param,
					firstResult - offset, maxResults));
		}

		return result;
	}
}
