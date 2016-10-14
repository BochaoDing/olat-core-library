package ch.uzh.campus.service.core;

import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.SapUserType;
import ch.uzh.campus.service.CampusCourse;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

import java.util.List;
import java.util.Set;

/**
 * Initial Date: 16.07.2012 <br>
 * 
 * @author cg
 */
public interface CampusCourseCoreService {

    boolean checkDelegation(Long sapCampusCourseId, Identity creator);

	CampusCourse createCampusCourseFromStandardTemplate(Long sapCampusCourseId, Identity creator) throws Exception;

    CampusCourse createCampusCourseFromTemplate(OLATResource templateOlatResource, Long sapCampusCourseId, Identity creator) throws Exception;

    CampusCourse continueCampusCourse(Long childSapCampusCourseId, Long parentSapCampusCourseId, Identity creator);

    CampusCourse loadCampusCourse(CampusCourseImportTO campusCourseImportTO);

    CampusCourse loadCampusCourseByOlatResource(OLATResource olatResource);

    Course getLatestCourseByOlatResource(OLATResource olatResource) throws Exception;

    void resetOlatResourceAndParentCourseReference(OLATResource olatResource);

    void deleteCampusCourseGroupsIfExist(RepositoryEntry repositoryEntry);

    RepositoryEntry getRepositoryEntryFor(Long sapCourseId);

    /*
     * Get a list of Campus-courses which have resourceableId=null. resourceableId=null means no OLAT course is created in the OLAT course-repository yet.
     */
    Set<Course> getCampusCoursesWithoutResourceableId(Identity identity, SapUserType userType);

	Set<Course> getCampusCoursesWithoutResourceableId(Identity identity, SapUserType userType, String searchString);

    /*
     * Get list of Campus courses which already are created in the OLAT course-repository.
     */
    Set<Course> getCampusCoursesWithResourceableId(Identity identity, SapUserType userType);

    Set<Course> getCampusCoursesWithResourceableId(Identity identity, SapUserType userType, String searchString);

    void createDelegation(Identity delegator, Identity delegatee);

    boolean existsDelegation(Identity delegator, Identity delegatee);

    boolean existCampusCoursesForOlatResource(OLATResource olatResource);

    List<Long> getOlatResourceKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters();

    List getDelegatees(Identity delegator);

    void deleteDelegation(Identity delegator, Identity delegatee);
}
