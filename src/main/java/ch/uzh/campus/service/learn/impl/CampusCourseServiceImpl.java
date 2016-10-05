package ch.uzh.campus.service.learn.impl;

import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.SapUserType;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import ch.uzh.campus.service.learn.CampusCourseService;
import ch.uzh.campus.service.learn.SapCampusCourseTo;
import edu.emory.mathcs.backport.java.util.Collections;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ch.uzh.campus.data.SapUserType.LECTURER;
import static ch.uzh.campus.data.SapUserType.STUDENT;
import static java.lang.Boolean.TRUE;
import static org.olat.repository.RepositoryEntry.ACC_OWNERS;
import static org.olat.repository.RepositoryEntry.ACC_OWNERS_AUTHORS;

/**
 * Initial Date: 30.11.2011<br />
 * 
 * @author guretzki
 */
@Service
public class CampusCourseServiceImpl implements CampusCourseService {

	private final CampusCourseCoreService campusCourseCoreService;
	private final boolean shortTitleActivated;

	@Autowired
	public CampusCourseServiceImpl(CampusCourseCoreService campusCourseCoreService,
								   @Value("${campus.lv_kuerzel.activated}") String shortTitleActivated) {
		assert campusCourseCoreService != null;
		this.campusCourseCoreService = campusCourseCoreService;
		this.shortTitleActivated = TRUE.toString().equals(shortTitleActivated);
	}

	@Override
	public boolean checkDelegation(Long sapCampusCourseId, Identity creator) {
		return campusCourseCoreService.checkDelegation(sapCampusCourseId, creator);
	}

	@Override
	public CampusCourse createCampusCourseFromStandardTemplate(Long sapCampusCourseId, Identity creator) throws Exception {
		return campusCourseCoreService.createCampusCourseFromStandardTemplate(sapCampusCourseId, creator);
	}

	@Override
	public CampusCourse createCampusCourseFromTemplate(OLATResource templateOlatResource, Long sapCampusCourseId, Identity creator) throws Exception {
		return campusCourseCoreService.createCampusCourseFromTemplate(templateOlatResource, sapCampusCourseId, creator);
	}

	@Override
	public CampusCourse continueCampusCourse(Long sapCampusCourseId, Long parentSapCampusCourseId, Identity creator) {
		return campusCourseCoreService.continueCampusCourse(sapCampusCourseId, parentSapCampusCourseId, creator);
	}

	@Override
	public List<SapCampusCourseTo> getCoursesOfStudent(Identity identity,
													   String searchString) {
		List<SapCampusCourseTo> result = new ArrayList<>();
		{
			Set<Course> sapCampusCourses = campusCourseCoreService
					.getCampusCoursesWithoutResourceableId(identity, STUDENT,
							searchString);
			for (Course sapCampusCourse : sapCampusCourses) {
				result.add(new SapCampusCourseTo(sapCampusCourse
						.getTitleToBeDisplayed(shortTitleActivated),
						sapCampusCourse.getId()));
			}
		}

		{
			/*
			 * A campus course, for which an OLAT course has already been
			 * created, is listed only if the user cannot see the linked OLAT
			 * course due to its permissions.
			 */
			Set<Course> sapCampusCourses = campusCourseCoreService
					.getCampusCoursesWithResourceableId(identity, STUDENT,
							searchString);
			for (Course sapCampusCourse : sapCampusCourses) {
				RepositoryEntry repositoryEntry = campusCourseCoreService
						.loadCampusCourseByOlatResource(sapCampusCourse.getOlatResource()).getRepositoryEntry();
				if (repositoryEntry != null) {
					int access = repositoryEntry.getAccess();
					if (repositoryEntry.isMembersOnly() == false &&
							(access == ACC_OWNERS || access == ACC_OWNERS_AUTHORS)) {
						result.add(new SapCampusCourseTo(sapCampusCourse
								.getTitleToBeDisplayed(shortTitleActivated),
								sapCampusCourse.getId()));
					}
				}
			}
		}

		Collections.sort(result);
		return result;
	}

	@Override
	public List<SapCampusCourseTo> getCoursesWhichCouldBeCreated(Identity identity, String searchString) {
		List<SapCampusCourseTo> courseList = new ArrayList<>();
		Set<Course> sapCampusCourses = campusCourseCoreService.getCampusCoursesWithoutResourceableId(identity, LECTURER, searchString);
		for (Course sapCampusCourse : sapCampusCourses) {
			courseList.add(new SapCampusCourseTo(sapCampusCourse.getTitleToBeDisplayed(shortTitleActivated), sapCampusCourse.getId()));
		}
		Collections.sort(courseList);
		return courseList;
	}

	@Override
	public List<SapCampusCourseTo> getCoursesWhichCouldBeOpened(Identity identity, SapUserType userType, String searchString) {
		List<SapCampusCourseTo> courseList = new ArrayList<>();
		Set<Course> sapCampusCourses = campusCourseCoreService.getCampusCoursesWithResourceableId(identity, userType, searchString);
		for (Course sapCampusCourse : sapCampusCourses) {
			courseList.add(new SapCampusCourseTo(sapCampusCourse.getTitleToBeDisplayed(shortTitleActivated), sapCampusCourse.getId()));
		}
		Collections.sort(courseList);
		return courseList;
	}

	@Override
	public Course getLatestCourseByOlatResource(OLATResource olatResource) throws Exception {
		return campusCourseCoreService.getLatestCourseByOlatResource(olatResource);
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
	public boolean existsDelegation(Identity delegator, Identity delegatee) {
		return campusCourseCoreService.existsDelegation(delegator, delegatee);
	}

	@Override
	public boolean existCampusCoursesForOlatResource(OLATResource olatResource) {
		return campusCourseCoreService.existCampusCoursesForOlatResource(olatResource);
	}

	@Override
	public List<Long> getOlatResourceKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters() {
		return campusCourseCoreService.getOlatResourceKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters();
	}

	@Override
	public List getDelegatees(Identity delegator) {
		return campusCourseCoreService.getDelegatees(delegator);
	}

	@Override
	public void deleteDelegation(Identity delegator, Identity delegatee) {
		campusCourseCoreService.deleteDelegation(delegator, delegatee);
	}
}
