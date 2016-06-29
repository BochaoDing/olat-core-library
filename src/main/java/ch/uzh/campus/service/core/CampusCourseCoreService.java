package ch.uzh.campus.service.core;

import java.util.List;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.repository.RepositoryEntry;

import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.SapOlatUser;
import ch.uzh.campus.service.CampusCourse;

/**
 * Initial Date: 16.07.2012 <br>
 * 
 * @author cg
 */
public interface CampusCourseCoreService {

    boolean checkDelegation(Long sapCampusCourseId, Identity creator);

    CampusCourse createCampusCourseFromTemplate(Long courseResourceableId, Long sapCampusCourseId, Identity creator);

    CampusCourse continueCampusCourse(Long courseResourceableId, Long sapCampusCourseId, String courseTitle, Identity creator);

    /**
     * TODO: olatng: this used to be called from CourseRepositoryHandler, this doesn't exist in OpenOLAT anymore. Wo aufrufen?
     */
    void resetResourceableIdReference(OLATResourceable res);

    RepositoryEntry getRepositoryEntryFor(Long sapCourseId);

    /**
     * Get a list of Campus-courses which have resourceableId=null. resourceableId=null means no OLAT course is created in the OLAT course-repository yet.
     */
    Set<Course> getCampusCoursesWithoutResourceableId(Identity identity, SapOlatUser.SapUserType userType);

	Set<Course> getCampusCoursesWithoutResourceableId(Identity identity, SapOlatUser.SapUserType userType, String searchString);

    /**
     * Get list of Campus courses which already are created in the OLAT course-repository.
     */
    Set<Course> getCampusCoursesWithResourceableId(Identity identity, SapOlatUser.SapUserType userType);

    Set<Course> getCampusCoursesWithResourceableId(Identity identity, SapOlatUser.SapUserType userType, String searchString);

    void createDelegation(Identity delegator, Identity delegatee);

    boolean existDelegation(Identity delegator, Identity delegatee);

    boolean existResourceableId(Long resourceableId);

    List<Long> getAllCreatedSapCourcesResourceableIds();

    List getDelegatees(Identity delegator);

    void deleteDelegation(Identity delegator, Identity delegatee);

}
