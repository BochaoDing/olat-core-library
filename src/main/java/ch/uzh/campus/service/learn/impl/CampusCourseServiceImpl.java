package ch.uzh.campus.service.learn.impl;

import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.SapOlatUser;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import ch.uzh.campus.service.learn.CampusCourseService;
import ch.uzh.campus.service.learn.SapCampusCourseTo;
import edu.emory.mathcs.backport.java.util.Collections;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ch.uzh.campus.data.SapOlatUser.SapUserType.LECTURER;
import static ch.uzh.campus.data.SapOlatUser.SapUserType.STUDENT;
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
	private final RepositoryManager repositoryManager;
	private final boolean shortTitleActivated;

	@Autowired
	public CampusCourseServiceImpl(CampusCourseCoreService campusCourseCoreService,
								   RepositoryManager repositoryManager,
								   @Value("${campus.lv_kuerzel.activated}") String shortTitleActivated) {
		assert campusCourseCoreService != null;
		assert repositoryManager != null;
		this.campusCourseCoreService = campusCourseCoreService;
		this.repositoryManager = repositoryManager;
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
	public CampusCourse createCampusCourseFromStandardTemplate(Long courseResourceableId, Long sapCampusCourseId, Identity creator) throws Exception {
		return campusCourseCoreService.createCampusCourseFromTemplate(courseResourceableId, sapCampusCourseId, creator);
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
						sapCampusCourse.getId(), null));
			}
		}

		{
			Set<Course> sapCampusCourses = campusCourseCoreService
					.getCampusCoursesWithResourceableId(identity, STUDENT,
							searchString);
			for (Course sapCampusCourse : sapCampusCourses) {
				RepositoryEntry repositoryEntry = campusCourseCoreService
						.loadCampusCourseByResourceable(sapCampusCourse
								.getResourceableId()).getRepositoryEntry();
				if (repositoryEntry != null) {
					int access = repositoryEntry.getAccess();
					if (repositoryEntry.isMembersOnly() == false &&
							(access == ACC_OWNERS || access == ACC_OWNERS_AUTHORS)) {
						result.add(new SapCampusCourseTo(sapCampusCourse
								.getTitleToBeDisplayed(shortTitleActivated),
								sapCampusCourse.getId(), null));
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
	public Course getLatestCourseByResourceable(Long resourceableId) throws Exception {
		return campusCourseCoreService.getLatestCourseByResourceable(resourceableId);
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

	@Override
	public List<Long> getResourceableIdsOfAllCreatedNotContinuedCoursesOfPreviousSemesters() {
		return campusCourseCoreService.getResourceableIdsOfAllCreatedNotContinuedCoursesOfPreviousSemesters();
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
