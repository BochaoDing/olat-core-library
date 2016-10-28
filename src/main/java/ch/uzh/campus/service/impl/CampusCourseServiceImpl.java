package ch.uzh.campus.service.impl;

import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.SapUserType;
import ch.uzh.campus.service.CampusCourseService;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import ch.uzh.campus.service.data.OlatCampusCourse;
import ch.uzh.campus.service.data.SapCampusCourseTOForUI;
import edu.emory.mathcs.backport.java.util.Collections;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ch.uzh.campus.data.SapUserType.LECTURER;
import static ch.uzh.campus.data.SapUserType.STUDENT;
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

	@Autowired
	public CampusCourseServiceImpl(CampusCourseCoreService campusCourseCoreService) {
		assert campusCourseCoreService != null;
		this.campusCourseCoreService = campusCourseCoreService;
	}

	@Override
	public boolean isIdentityLecturerOrDelegateeOfSapCourse(Long sapCampusCourseId, Identity identity) {
		return campusCourseCoreService.isIdentityLecturerOrDelegateeOfSapCourse(sapCampusCourseId, identity);
	}

	@Override
	public OlatCampusCourse createOlatCampusCourseFromStandardTemplate(Long sapCampusCourseId, Identity creator) throws Exception {
		return campusCourseCoreService.createOlatCampusCourseFromStandardTemplate(sapCampusCourseId, creator);
	}

	@Override
	public OlatCampusCourse createOlatCampusCourseFromTemplate(OLATResource templateOlatResource, Long sapCampusCourseId, Identity creator) throws Exception {
		return campusCourseCoreService.createOlatCampusCourseFromTemplate(templateOlatResource, sapCampusCourseId, creator);
	}

	@Override
	public OlatCampusCourse continueOlatCampusCourse(Long sapCampusCourseId, Long parentSapCampusCourseId, Identity creator) {
		return campusCourseCoreService.continueOlatCampusCourse(sapCampusCourseId, parentSapCampusCourseId, creator);
	}

	@Override
	public List<SapCampusCourseTOForUI> getCoursesOfStudent(Identity identity, String searchString) {
		List<SapCampusCourseTOForUI> sapCampusCourseTOForUIs = new ArrayList<>();
		{
			Set<Course> courses = campusCourseCoreService.getCoursesWithoutResourceableId(identity, STUDENT, searchString);
			for (Course course : courses) {
				sapCampusCourseTOForUIs.add(new SapCampusCourseTOForUI(course.getTitleToBeDisplayed(), course.getId()));
			}
		}

		{
			/*
			 * A sap course, for which an OLAT course has already been
			 * created, is listed only if the user cannot see the linked OLAT
			 * course due to its permissions.
			 */
			Set<Course> courses = campusCourseCoreService.getCoursesWithResourceableId(identity, STUDENT, searchString);
			for (Course course : courses) {
				RepositoryEntry repositoryEntry = campusCourseCoreService.loadOlatCampusCourse(course.getOlatResource()).getRepositoryEntry();
				if (repositoryEntry != null) {
					int access = repositoryEntry.getAccess();
					if (repositoryEntry.isMembersOnly() == false && (access == ACC_OWNERS || access == ACC_OWNERS_AUTHORS)) {
						sapCampusCourseTOForUIs.add(new SapCampusCourseTOForUI(course.getTitleToBeDisplayed(), course.getId()));
					}
				}
			}
		}

		Collections.sort(sapCampusCourseTOForUIs);
		return sapCampusCourseTOForUIs;
	}

	@Override
	public List<SapCampusCourseTOForUI> getCoursesWhichCouldBeCreated(Identity identity, String searchString) {
		List<SapCampusCourseTOForUI> sapCampusCourseTOForUIs = new ArrayList<>();
		Set<Course> courses = campusCourseCoreService.getCoursesWithoutResourceableId(identity, LECTURER, searchString);
		for (Course course : courses) {
			sapCampusCourseTOForUIs.add(new SapCampusCourseTOForUI(course.getTitleToBeDisplayed(), course.getId()));
		}
		Collections.sort(sapCampusCourseTOForUIs);
		return sapCampusCourseTOForUIs;
	}

	@Override
	public List<SapCampusCourseTOForUI> getCoursesWhichCouldBeOpened(Identity identity, SapUserType userType, String searchString) {
		List<SapCampusCourseTOForUI> sapCampusCourseTOForUIs = new ArrayList<>();
		Set<Course> courses = campusCourseCoreService.getCoursesWithResourceableId(identity, userType, searchString);
		for (Course course : courses) {
			sapCampusCourseTOForUIs.add(new SapCampusCourseTOForUI(course.getTitleToBeDisplayed(), course.getId()));
		}
		Collections.sort(sapCampusCourseTOForUIs);
		return sapCampusCourseTOForUIs;
	}

	@Override
	public Course getLatestCourseByOlatResource(OLATResource olatResource) throws Exception {
		return campusCourseCoreService.getLatestCourseByOlatResource(olatResource);
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
