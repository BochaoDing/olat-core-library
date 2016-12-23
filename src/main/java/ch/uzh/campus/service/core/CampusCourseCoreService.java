package ch.uzh.campus.service.core;

import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.SapUserType;
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

    Course getLatestCourseByRepositoryEntry(RepositoryEntry repositoryEntry) throws Exception;

    void resetRepositoryEntryAndParentCourse(RepositoryEntry repositoryEntry);

    void resetCampusGroup(BusinessGroup campusGroup);

    void deleteCampusGroups(RepositoryEntry repositoryEntry);

	Set<Course> getNotCreatedCourses(Identity identity, SapUserType userType, String searchString);

    Set<Course> getCreatedCourses(Identity identity, SapUserType userType, String searchString);

    void createDelegation(Identity delegator, Identity delegatee);

    boolean existsDelegation(Identity delegator, Identity delegatee);

    boolean existCoursesForRepositoryEntry(RepositoryEntry repositoryEntry);

    List<Long> getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters();

    List getDelegatees(Identity delegator);

    void deleteDelegation(Identity delegator, Identity delegatee);
}
