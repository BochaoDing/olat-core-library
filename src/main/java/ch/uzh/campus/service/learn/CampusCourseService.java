package ch.uzh.campus.service.learn;

import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.SapUserType;
import ch.uzh.campus.service.CampusCourse;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

import java.util.List;

/**
 * This is called from presentation.
 * 
 * Initial Date: 30.11.2011 <br>
 * 
 * @author guretzki
 */
public interface CampusCourseService {

    boolean checkDelegation(Long sapCampusCourseId, Identity creator);

	/**
     * Create a new campus-course from the standard course template. Copy template and update title, description, owner and participants.
     */
	CampusCourse createCampusCourseFromStandardTemplate(Long sapCampusCourseId, Identity creator) throws Exception;

    /**
     * Create a new campus-course from a course template. Copy template and update title, description, owner and participants.
     */
    CampusCourse createCampusCourseFromStandardTemplate(Long courseResourceableId, Long sapCampusCourseId, Identity creator) throws Exception;

    /**
     * Uses an existing campus-course. It updates the title, description (including the vvz link), owner and participants.
     */
    CampusCourse continueCampusCourse(Long sapCampusCourseId, Long parentSapCampusCourseId, Identity creator);

	/**
	 * Get the list of SAP campus courses of a student which has not been
	 * created or the student has not (yet) access to.
	 */
	List<SapCampusCourseTo> getCoursesOfStudent(Identity identity, String searchString);

    /**
     * Get a list of SAP campus-course which a lecturer identity could create. The courses must be not created and the identity must be owner of the courses.
     */
    List<SapCampusCourseTo> getCoursesWhichCouldBeCreated(Identity identity, String searchString);

    /**
     * Get a list of SAP campus-courses which are already created and identity is owner or participant.
     */
    List<SapCampusCourseTo> getCoursesWhichCouldBeOpened(Identity identity, SapUserType userType, String searchString);

	Course getLatestCourseByResourceable(Long resourceableId) throws Exception;

    RepositoryEntry getRepositoryEntryFor(Long sapCourseId);

    void createDelegation(Identity delegator, Identity delegatee);

    boolean existDelegation(Identity delegator, Identity delegatee);

    boolean existResourceableId(Long resourceableId);

    List<Long> getResourceableIdsOfAllCreatedNotContinuedCoursesOfPreviousSemesters();

    List getDelegatees(Identity delegator);

    void deleteDelegation(Identity delegator, Identity delegatee);
}
