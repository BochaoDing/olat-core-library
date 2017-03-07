package ch.uzh.extension.campuscourse.service;

import ch.uzh.extension.campuscourse.model.CampusCourseWithoutListsTO;
import ch.uzh.extension.campuscourse.model.IdentityDate;
import ch.uzh.extension.campuscourse.model.SapUserType;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;

import java.util.List;
import java.util.Set;

/**
 * Initial Date: 16.07.2012 <br>
 * 
 * @author cg
 */
public interface CampusCourseCoreService {

    boolean isIdentityLecturerOrDelegateeOfSapCourse(Long sapCampusCourseId, Identity identity);

	RepositoryEntry createOlatCampusCourseFromStandardTemplate(Long sapCampusCourseId, Identity creator) throws Exception;

    RepositoryEntry createOlatCampusCourseFromTemplate(RepositoryEntry templateRepositoryEntry, Long sapCampusCourseId, Identity creator) throws Exception;

    RepositoryEntry continueOlatCampusCourse(Long childSapCampusCourseId, Long parentSapCampusCourseId, Identity creator);

	CampusCourseWithoutListsTO getCourseOrLastChildOfContinuedCourseByRepositoryEntryKey(RepositoryEntry repositoryEntry);

    void resetRepositoryEntryAndParentCourse(RepositoryEntry repositoryEntry);

    void resetCampusGroup(BusinessGroup campusGroup);

    void deleteCampusGroups(RepositoryEntry repositoryEntry);

	Set<CampusCourseWithoutListsTO> getNotCreatedCourses(Identity identity, SapUserType userType, String searchString);

    Set<CampusCourseWithoutListsTO> getCreatedCourses(Identity identity, SapUserType userType, String searchString);

    boolean isContinuedCourse(RepositoryEntry repositoryEntry);

	void undoCourseContinuation(RepositoryEntry repositoryEntry, Identity creator);

	List<String> getTitlesOfChildAndParentCoursesInAscendingOrder(RepositoryEntry repositoryEntry);

    void createDelegation(Identity delegator, Identity delegatee);

    boolean existsDelegation(Identity delegator, Identity delegatee);

    boolean existCoursesForRepositoryEntry(RepositoryEntry repositoryEntry);

    Set<Long> getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters();

    List<IdentityDate> getDelegateesAndCreationDateByDelegator(Identity delegator);

    List<IdentityDate> getDelegatorsAndCreationDateByDelegatee(Identity delegatee);

    void deleteDelegation(Identity delegator, Identity delegatee);
}
