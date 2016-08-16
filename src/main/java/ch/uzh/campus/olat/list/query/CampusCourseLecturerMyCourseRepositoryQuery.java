package ch.uzh.campus.olat.list.query;

import ch.uzh.campus.olat.CampusCourseOlatHelper;
import ch.uzh.campus.service.learn.CampusCourseService;
import ch.uzh.campus.service.learn.SapCampusCourseTo;
import org.olat.core.id.Roles;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

import static ch.uzh.campus.data.SapOlatUser.SapUserType.LECTURER;
import static org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter.asAuthor;
import static org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter.asCoach;

/**
 * Initial date: 2016-08-12<br />
 * @author sev26 (UZH)
 */
@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class CampusCourseLecturerMyCourseRepositoryQuery extends CampusCourseMyCourseRepositoryQuery {

	@Autowired
	public CampusCourseLecturerMyCourseRepositoryQuery(
			CampusCourseService campusCourseService,
			RepositoryModule repositoryModule) {
		super(LECTURER, campusCourseService, repositoryModule);
	}

	@Override
	protected boolean filter(List<SearchMyRepositoryEntryViewParams.Filter> filters) {
		return filters.contains(asAuthor) || filters.contains(asCoach);
	}

	@Override
	protected RepositoryEntry getRepositoryEntry(
			SapCampusCourseTo sapCampusCourseTo, Roles roles) {
		return CampusCourseOlatHelper.getLecturerRepositoryEntry(sapCampusCourseTo, roles);
	}
}
