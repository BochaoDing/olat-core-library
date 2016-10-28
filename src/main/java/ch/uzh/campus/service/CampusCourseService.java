package ch.uzh.campus.service;

import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.SapUserType;
import ch.uzh.campus.service.data.SapCampusCourseTOForUI;
import ch.uzh.campus.service.data.OlatCampusCourse;
import org.olat.core.id.Identity;
import org.olat.resource.OLATResource;

import java.util.List;

/**
 * This is called from presentation.
 * 
 * Initial Date: 30.11.2011 <br>
 * 
 * @author guretzki
 */
public interface CampusCourseService {

    boolean isIdentityLecturerOrDelegateeOfSapCourse(Long sapCampusCourseId, Identity identity);

	/**
     * Create a new campus-course from the standard course template. Copy template and update title, description, owner and participants.
     */
	OlatCampusCourse createOlatCampusCourseFromStandardTemplate(Long sapCampusCourseId, Identity creator) throws Exception;

    /**
     * Create a new campus-course from a course template. Copy template and update title, description, owner and participants.
     */
    OlatCampusCourse createOlatCampusCourseFromTemplate(OLATResource templateOlatResource, Long sapCampusCourseId, Identity creator) throws Exception;

    /**
     * Uses an existing campus-course. It updates the title, description (including the vvz link), owner and participants.
     */
    OlatCampusCourse continueOlatCampusCourse(Long childSapCampusCourseId, Long parentSapCampusCourseId, Identity creator);

	/**
	 * Get the list of SAP campus courses of a student which has not been
	 * created or the student has not (yet) access to.
	 */
	List<SapCampusCourseTOForUI> getCoursesOfStudent(Identity identity, String searchString);

    /**
     * Get a list of SAP campus-course which a lecturer identity could create. The courses must be not created and the identity must be owner of the courses.
     */
    List<SapCampusCourseTOForUI> getCoursesWhichCouldBeCreated(Identity identity, String searchString);

    /**
     * Get a list of SAP campus-courses which are already created and identity is owner or participant.
     */
    List<SapCampusCourseTOForUI> getCoursesWhichCouldBeOpened(Identity identity, SapUserType userType, String searchString);

	Course getLatestCourseByOlatResource(OLATResource olatResource) throws Exception;

    void createDelegation(Identity delegator, Identity delegatee);

    boolean existsDelegation(Identity delegator, Identity delegatee);

    List<Long> getOlatResourceKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters();

    List getDelegatees(Identity delegator);

    void deleteDelegation(Identity delegator, Identity delegatee);
}
