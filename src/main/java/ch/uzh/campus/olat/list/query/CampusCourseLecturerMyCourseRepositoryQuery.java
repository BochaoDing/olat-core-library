package ch.uzh.campus.olat.list.query;

import ch.uzh.campus.olat.CampusCourseOlatHelper;
import ch.uzh.campus.service.CampusCourseService;
import ch.uzh.campus.service.data.SapCampusCourseTOForUI;
import org.olat.core.id.Roles;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import java.util.List;

import static org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter.asAuthor;
import static org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter.asCoach;

/**
 * Initial date: 2016-08-12<br />
 * @author sev26 (UZH)
 */
//@Service // Uncomment if courses of lecturers should be listed under "My courses".
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class CampusCourseLecturerMyCourseRepositoryQuery extends CampusCourseMyCourseRepositoryQuery {

	@Autowired
	public CampusCourseLecturerMyCourseRepositoryQuery(
			CampusCourseService campusCourseService,
			RepositoryModule repositoryModule) {
		super(campusCourseService, repositoryModule);
	}

	@Override
	protected boolean filter(List<SearchMyRepositoryEntryViewParams.Filter> filters) {
		return filters.contains(asAuthor) || filters.contains(asCoach);
	}

	@Override
	protected List<SapCampusCourseTOForUI> getSapCampusCourseTOForUIs(SearchMyRepositoryEntryViewParams param) {
		return campusCourseService.getCoursesWhichCouldBeCreated(param.getIdentity(), param.getIdRefsAndTitle());
	}

	@Override
	protected RepositoryEntry getRepositoryEntry(SapCampusCourseTOForUI sapCampusCourseTOForUI, Roles roles) {
		return CampusCourseOlatHelper.getLecturerRepositoryEntry(sapCampusCourseTOForUI, roles);
	}
}
