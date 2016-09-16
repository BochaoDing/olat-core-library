package ch.uzh.campus.service.core;

import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.SapUserType;
import ch.uzh.campus.service.CampusCourse;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.repository.RepositoryEntry;

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

    CampusCourse createCampusCourseFromTemplate(Long courseResourceableId, Long sapCampusCourseId, Identity creator) throws Exception;

    CampusCourse continueCampusCourse(Long childSapCampusCourseId, Long parentSapCampusCourseId, Identity creator);

    CampusCourse loadCampusCourse(CampusCourseImportTO campusCourseImportTO);

    CampusCourse loadCampusCourseByResourceable(Long resourceableId);

    Course getLatestCourseByResourceable(Long resourceableId) throws Exception;

    void resetResourceableIdAndParentCourseReference(OLATResourceable res);

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

    boolean existDelegation(Identity delegator, Identity delegatee);

    boolean existResourceableId(Long resourceableId);

    List<Long> getResourceableIdsOfAllCreatedNotContinuedCoursesOfPreviousSemesters();

    List getDelegatees(Identity delegator);

    void deleteDelegation(Identity delegator, Identity delegatee);
}
