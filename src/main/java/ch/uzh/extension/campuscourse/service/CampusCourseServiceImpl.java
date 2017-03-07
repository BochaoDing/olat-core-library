package ch.uzh.extension.campuscourse.service;

import ch.uzh.extension.campuscourse.model.CampusCourseTOForUI;
import ch.uzh.extension.campuscourse.model.CampusCourseWithoutListsTO;
import ch.uzh.extension.campuscourse.model.IdentityDate;
import ch.uzh.extension.campuscourse.model.SapUserType;
import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import edu.emory.mathcs.backport.java.util.Collections;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ch.uzh.extension.campuscourse.model.SapUserType.LECTURER;
import static ch.uzh.extension.campuscourse.model.SapUserType.STUDENT;
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
	private final DaoManager daoManager;

	@Autowired
	public CampusCourseServiceImpl(CampusCourseCoreService campusCourseCoreService, DaoManager daoManager) {
		this.campusCourseCoreService = campusCourseCoreService;
		this.daoManager = daoManager;
	}

	@Override
	public boolean isIdentityLecturerOrDelegateeOfSapCourse(Long sapCampusCourseId, Identity identity) {
		return campusCourseCoreService.isIdentityLecturerOrDelegateeOfSapCourse(sapCampusCourseId, identity);
	}

	@Override
	public RepositoryEntry createOlatCampusCourseFromStandardTemplate(Long sapCampusCourseId, Identity creator) throws Exception {
		return campusCourseCoreService.createOlatCampusCourseFromStandardTemplate(sapCampusCourseId, creator);
	}

	@Override
	public RepositoryEntry createOlatCampusCourseFromTemplate(RepositoryEntry templateRepositoryEntry, Long sapCampusCourseId, Identity creator) throws Exception {
		return campusCourseCoreService.createOlatCampusCourseFromTemplate(templateRepositoryEntry, sapCampusCourseId, creator);
	}

	@Override
	public RepositoryEntry continueOlatCampusCourse(Long sapCampusCourseId, Long parentSapCampusCourseId, Identity creator) {
		return campusCourseCoreService.continueOlatCampusCourse(sapCampusCourseId, parentSapCampusCourseId, creator);
	}

	@Override
	public List<CampusCourseTOForUI> getCoursesOfStudent(Identity identity, String searchString) {
		List<CampusCourseTOForUI> campusCourseTOForUIs = new ArrayList<>();
		{
			Set<CampusCourseWithoutListsTO> campusCourseWithoutListsTOs = campusCourseCoreService.getNotCreatedCourses(identity, STUDENT, searchString);
			for (CampusCourseWithoutListsTO campusCourseWithoutListsTO : campusCourseWithoutListsTOs) {
				CampusCourseTOForUI campusCourseTOForUI = daoManager.loadCampusCourseTOForUI(campusCourseWithoutListsTO.getSapCourseId());
				campusCourseTOForUIs.add(campusCourseTOForUI);
			}
		}

		{
			/*
			 * A sap course, for which an OLAT course has already been
			 * created, is listed only if the user cannot see the linked OLAT
			 * course due to its permissions.
			 */
			Set<CampusCourseWithoutListsTO> campusCourseWithoutListsTOs = campusCourseCoreService.getCreatedCourses(identity, STUDENT, searchString);
			for (CampusCourseWithoutListsTO campusCourseWithoutListsTO : campusCourseWithoutListsTOs) {
				RepositoryEntry repositoryEntry = campusCourseWithoutListsTO.getRepositoryEntry();
				if (repositoryEntry != null) {
					int access = repositoryEntry.getAccess();
					if (!repositoryEntry.isMembersOnly() && (access == ACC_OWNERS || access == ACC_OWNERS_AUTHORS)) {
						CampusCourseTOForUI campusCourseTOForUI = daoManager.loadCampusCourseTOForUI(campusCourseWithoutListsTO.getSapCourseId());
						campusCourseTOForUIs.add(campusCourseTOForUI);
					}
				}
			}
		}

		Collections.sort(campusCourseTOForUIs);
		return campusCourseTOForUIs;
	}

	@Override
	public List<CampusCourseTOForUI> getCoursesWhichCouldBeCreated(Identity identity, String searchString) {
		List<CampusCourseTOForUI> campusCourseTOForUIs = new ArrayList<>();
		Set<CampusCourseWithoutListsTO> campusCourseWithoutListsTOs = campusCourseCoreService.getNotCreatedCourses(identity, LECTURER, searchString);
		for (CampusCourseWithoutListsTO campusCourseWithoutListsTO : campusCourseWithoutListsTOs) {
			CampusCourseTOForUI campusCourseTOForUI = daoManager.loadCampusCourseTOForUI(campusCourseWithoutListsTO.getSapCourseId());
			campusCourseTOForUIs.add(campusCourseTOForUI);
		}
		Collections.sort(campusCourseTOForUIs);
		return campusCourseTOForUIs;
	}

	@Override
	public List<CampusCourseTOForUI> getCoursesWhichCouldBeOpened(Identity identity, SapUserType userType, String searchString) {
		List<CampusCourseTOForUI> campusCourseTOForUIs = new ArrayList<>();
		Set<CampusCourseWithoutListsTO> campusCourseWithoutListsTOs = campusCourseCoreService.getCreatedCourses(identity, userType, searchString);
		for (CampusCourseWithoutListsTO campusCourseWithoutListsTO : campusCourseWithoutListsTOs) {
			CampusCourseTOForUI campusCourseTOForUI = daoManager.loadCampusCourseTOForUI(campusCourseWithoutListsTO.getSapCourseId());
			campusCourseTOForUIs.add(campusCourseTOForUI);
		}
		Collections.sort(campusCourseTOForUIs);
		return campusCourseTOForUIs;
	}

	@Override
	public CampusCourseWithoutListsTO getCourseOrLastChildOfContinuedCourseByRepositoryEntryKey(RepositoryEntry repositoryEntry) {
		return campusCourseCoreService.getCourseOrLastChildOfContinuedCourseByRepositoryEntryKey(repositoryEntry);
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
	public Set<Long> getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters() {
		return campusCourseCoreService.getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters();
	}

	@Override
	public boolean isContinuedCourse(RepositoryEntry repositoryEntry) {
		return campusCourseCoreService.isContinuedCourse(repositoryEntry);
	}

	@Override
	public void undoCourseContinuation(RepositoryEntry repositoryEntry, Identity creator) {
		campusCourseCoreService.undoCourseContinuation(repositoryEntry, creator);
	}

	@Override
	public List<String> getTitlesOfChildAndParentCoursesInAscendingOrder(RepositoryEntry repositoryEntry) {
		return campusCourseCoreService.getTitlesOfChildAndParentCoursesInAscendingOrder(repositoryEntry);
	}

	@Override
	public List<IdentityDate> getDelegateesAndCreationDateByDelegator(Identity delegator) {
		return campusCourseCoreService.getDelegateesAndCreationDateByDelegator(delegator);
	}

	@Override
	public List<IdentityDate> getDelegatorsAndCreationDateByDelegatee(Identity delegatee) {
		return campusCourseCoreService.getDelegatorsAndCreationDateByDelegatee(delegatee);
	}

	@Override
	public void deleteDelegation(Identity delegator, Identity delegatee) {
		campusCourseCoreService.deleteDelegation(delegator, delegatee);
	}
}