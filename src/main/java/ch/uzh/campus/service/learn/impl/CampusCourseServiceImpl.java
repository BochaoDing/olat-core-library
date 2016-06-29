package ch.uzh.campus.service.learn.impl;

import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.SapOlatUser;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import ch.uzh.campus.service.learn.CampusCourseService;
import ch.uzh.campus.service.learn.SapCampusCourseTo;
import edu.emory.mathcs.backport.java.util.Collections;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Initial Date: 30.11.2011<br />
 * 
 * @author guretzki
 */
@Service
public class CampusCourseServiceImpl implements CampusCourseService {

	@Autowired
	CampusCourseCoreService campusCourseCoreService;

	@Value("${campus.lv_kuerzel.activated}")
	private String shortTitleActivated;

	@Override
	public boolean checkDelegation(Long sapCampusCourseId, Identity creator) {
		return campusCourseCoreService.checkDelegation(sapCampusCourseId, creator);
	}

	@Override
	public CampusCourse createCampusCourseFromTemplate(Long courseResourceableId, Long sapCampusCourseId, Identity creator) {
		return campusCourseCoreService.createCampusCourseFromTemplate(courseResourceableId, sapCampusCourseId, creator);
	}

	@Override
	public CampusCourse continueCampusCourse(Long sapCampusCourseId, Long parentSapCampusCourseId, Identity creator) {
		return campusCourseCoreService.continueCampusCourse(sapCampusCourseId, parentSapCampusCourseId, creator);
	}

	@Override
	public List<SapCampusCourseTo> getCoursesWhichCouldBeCreated(Identity identity, SapOlatUser.SapUserType userType, String searchString) {
		List<SapCampusCourseTo> courseList = new ArrayList<>();
		Set<Course> sapCampusCourses = campusCourseCoreService.getCampusCoursesWithoutResourceableId(identity, userType, searchString);
		for (Course sapCampusCourse : sapCampusCourses) {
			courseList.add(new SapCampusCourseTo(sapCampusCourse.getTitleToBeDisplayed(shortTitleActivated), sapCampusCourse.getId(), null));
		}
		Collections.sort(courseList);
		return courseList;
	}

	@Override
	public List<SapCampusCourseTo> getCoursesWhichCouldBeOpened(Identity identity, SapOlatUser.SapUserType userType, String searchString) {
		List<SapCampusCourseTo> courseList = new ArrayList<>();
		Set<Course> sapCampusCourses = campusCourseCoreService.getCampusCoursesWithResourceableId(identity, userType, searchString);
		for (Course sapCampusCourse : sapCampusCourses) {
			courseList.add(new SapCampusCourseTo(sapCampusCourse.getTitleToBeDisplayed(shortTitleActivated), sapCampusCourse.getId(), sapCampusCourse.getResourceableId()));
		}
		Collections.sort(courseList);
		return courseList;
	}

	@Override
	public RepositoryEntry getRepositoryEntryFor(Long sapCourseId) {
		return campusCourseCoreService.getRepositoryEntryFor(sapCourseId);
	}

	@Override
	public void createDelegation(Identity delegator, Identity delegatee) {
		campusCourseCoreService.createDelegation(delegator, delegatee);
	}

	@Override
	public boolean existDelegation(Identity delegator, Identity delegatee) {
		return campusCourseCoreService.existDelegation(delegator, delegatee);
	}

	@Override
	public boolean existResourceableId(Long resourceableId) {
		return campusCourseCoreService.existResourceableId(resourceableId);
	}

	public List<Long> getAllCreatedSapCourcesResourceableIds() {
		return campusCourseCoreService.getAllCreatedSapCourcesResourceableIds();
	}

	public List getDelegatees(Identity delegator) {
		return getCampusCourseCoreService().getDelegatees(delegator);
	}

	public void deleteDelegation(Identity delegator, Identity delegatee) {
		campusCourseCoreService.deleteDelegation(delegator, delegatee);
	}

	private CampusCourseCoreService getCampusCourseCoreService() {
		// Ensure that the bean is not null (can happen if it was not injected)
		if (campusCourseCoreService == null) {
			campusCourseCoreService = (CampusCourseCoreService) CoreSpringFactory.getBean(CampusCourseCoreService.class);
		}

		return campusCourseCoreService;
	}
}
