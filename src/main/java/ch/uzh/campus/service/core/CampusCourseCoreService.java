package ch.uzh.campus.service.core;

import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.SapUserType;
import ch.uzh.campus.service.data.OlatCampusCourse;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.resource.OLATResource;

import java.util.List;
import java.util.Set;

/**
 * Initial Date: 16.07.2012 <br>
 * 
 * @author cg
 */
public interface CampusCourseCoreService {

    boolean isIdentityLecturerOrDelegateeOfSapCourse(Long sapCampusCourseId, Identity identity);

	OlatCampusCourse createOlatCampusCourseFromStandardTemplate(Long sapCampusCourseId, Identity creator) throws Exception;

    OlatCampusCourse createOlatCampusCourseFromTemplate(OLATResource templateOlatResource, Long sapCampusCourseId, Identity creator) throws Exception;

    OlatCampusCourse continueOlatCampusCourse(Long childSapCampusCourseId, Long parentSapCampusCourseId, Identity creator);

    OlatCampusCourse loadOlatCampusCourse(OLATResource olatResource);

    Course getLatestCourseByOlatResource(OLATResource olatResource) throws Exception;

    void resetOlatResourceAndParentCourse(OLATResource olatResource);

    void resetCampusGroup(BusinessGroup campusGroup);

    void deleteCampusGroups(OLATResource olatResource);

	Set<Course> getCoursesWithoutResourceableId(Identity identity, SapUserType userType, String searchString);

    Set<Course> getCoursesWithResourceableId(Identity identity, SapUserType userType, String searchString);

    void createDelegation(Identity delegator, Identity delegatee);

    boolean existsDelegation(Identity delegator, Identity delegatee);

    boolean existCoursesForOlatResource(OLATResource olatResource);

    List<Long> getOlatResourceKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters();

    List getDelegatees(Identity delegator);

    void deleteDelegation(Identity delegator, Identity delegatee);
}
