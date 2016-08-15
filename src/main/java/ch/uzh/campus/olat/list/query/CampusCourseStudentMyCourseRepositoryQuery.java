package ch.uzh.campus.olat.list.query;

import ch.uzh.campus.olat.CampusCourseOlatHelper;
import ch.uzh.campus.service.learn.CampusCourseService;
import ch.uzh.campus.service.learn.SapCampusCourseTo;
import org.olat.core.id.Roles;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import static ch.uzh.campus.data.SapOlatUser.SapUserType.STUDENT;

/**
 * Initial date: 2016-08-12<br />
 * @author sev26 (UZH)
 */
@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class CampusCourseStudentMyCourseRepositoryQuery extends CampusCourseMyCourseRepositoryQuery {

	@Autowired
	public CampusCourseStudentMyCourseRepositoryQuery(
			CampusCourseService campusCourseService,
			RepositoryModule repositoryModule) {
		super(STUDENT, campusCourseService, repositoryModule);
	}

	@Override
	protected RepositoryEntry getRepositoryEntry(
			SapCampusCourseTo sapCampusCourseTo, Roles roles) {
		return CampusCourseOlatHelper.getStudentRepositoryEntry(sapCampusCourseTo);
	}
}
