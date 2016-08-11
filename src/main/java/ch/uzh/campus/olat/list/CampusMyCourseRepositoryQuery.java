package ch.uzh.campus.olat.list;

import ch.uzh.campus.olat.CampusCourseOlatHelper;
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

import static ch.uzh.campus.data.SapOlatUser.*;
import static ch.uzh.campus.olat.CampusCourseBeanFactory.*;

/**
 * Initial date: 2016-06-15<br />
 * @author sev26 (UZH)
 */
@Service
public class CampusMyCourseRepositoryQuery implements MyCourseRepositoryQuery {

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
	public List<RepositoryEntryMyView> searchViews(SearchMyRepositoryEntryViewParams params,
												   int firstResult,
												   int maxResults) {
		List<RepositoryEntryMyView> result = new ArrayList<>();
		/**
		 * The difference between calling the method with LECTURER rather than
		 * STUDENT is that the method returns also all courses the user is
		 * delegated to.
		 */
		SapUserType userType = params.getRoles().isAuthor() ?
			SapUserType.LECTURER : SapUserType.STUDENT;

		List<SapCampusCourseTo> sapCampusCourseTos = campusCourseService.getCoursesWhichCouldBeCreated(
				params.getIdentity(), userType, params.getIdRefsAndTitle());

		boolean requiresStatistics = repositoryModule.isRatingEnabled() || repositoryModule.isCommentEnabled();

		for (SapCampusCourseTo sapCampusCourseTo : sapCampusCourseTos) {
			RepositoryEntry repositoryEntry = CampusCourseOlatHelper
					.getRepositoryEntry(sapCampusCourseTo);
			RepositoryEntryMyCourseImpl view = new RepositoryEntryMyCourseImpl(
					repositoryEntry,
					requiresStatistics ? repositoryEntry.getStatistics() : null, false,
					0, 0);
			result.add(view);
		}

		return result;
	}
}
