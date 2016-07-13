package ch.uzh.campus.olat;

import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.manager.RepositoryEntryMyCourseQueries;
import org.olat.repository.manager.coursequery.MyCourseRepositoryQuery;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * Initial date: 2016-06-15<br />
 * @author sev26 (UZH)
 */
@Service
@Primary // This service implementation should preferred over the default implementation.
public class CombineMyCourseRepositoryQuery implements MyCourseRepositoryQuery {

	private final RepositoryEntryMyCourseQueries repositoryEntryMyCourseQueries;
	private final CampusMyCourseRepositoryQuery campusMyCourseRepositoryQuery;

	@Autowired
	public CombineMyCourseRepositoryQuery(RepositoryEntryMyCourseQueries repositoryEntryMyCourseQueries, CampusMyCourseRepositoryQuery campusMyCourseRepositoryQuery) {
		this.repositoryEntryMyCourseQueries = repositoryEntryMyCourseQueries;
		this.campusMyCourseRepositoryQuery = campusMyCourseRepositoryQuery;
	}

	@Override
	public int countViews(SearchMyRepositoryEntryViewParams param) {
		return repositoryEntryMyCourseQueries.countViews(param) +
				campusMyCourseRepositoryQuery.countViews(param);
	}

	@Override
	public List<RepositoryEntryMyView> searchViews(SearchMyRepositoryEntryViewParams param, int firstResult, int maxResults) {
		List<RepositoryEntryMyView> result = new LinkedList<>();
		if (param.getMarked() == null || param.getMarked() == Boolean.FALSE) {
			result.addAll(campusMyCourseRepositoryQuery.searchViews(param, firstResult, maxResults));
		}
		result.addAll(repositoryEntryMyCourseQueries.searchViews(param, firstResult, maxResults));
		return result;
	}
}
