package ch.uzh.extension.campuscourse.service;

import ch.uzh.extension.campuscourse.data.entity.Course;
import ch.uzh.extension.campuscourse.model.CampusCourseTOForUI;
import ch.uzh.extension.campuscourse.model.IdentityDate;
import ch.uzh.extension.campuscourse.model.SapUserType;
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

    boolean isIdentityLecturerOrDelegateeOfSapCourse(Long sapCampusCourseId, Identity identity);

	/**
     * Create a new campus-course from the standard course template. Copy template and update title, description, owner and participants.
     */
	RepositoryEntry createOlatCampusCourseFromStandardTemplate(Long sapCampusCourseId, Identity creator) throws Exception;

    /**
     * Create a new campus-course from a course template. Copy template and update title, description, owner and participants.
     */
    RepositoryEntry createOlatCampusCourseFromTemplate(RepositoryEntry templateRepositoryEntry, Long sapCampusCourseId, Identity creator) throws Exception;

    /**
     * Uses an existing campus-course. It updates the title, description (including the vvz link), owner and participants.
     */
    RepositoryEntry continueOlatCampusCourse(Long childSapCampusCourseId, Long parentSapCampusCourseId, Identity creator);

	/**
	 * Get the list of SAP campus courses of a student which has not been
	 * created or the student has not (yet) access to.
	 */
	List<CampusCourseTOForUI> getCoursesOfStudent(Identity identity, String searchString);

    /**
     * Get a list of SAP campus-course which a lecturer identity could create. The courses must be not created and the identity must be owner of the courses.
     */
    List<CampusCourseTOForUI> getCoursesWhichCouldBeCreated(Identity identity, String searchString);

    /**
     * Get a list of SAP campus-courses which are already created and identity is owner or participant.
     */
    List<CampusCourseTOForUI> getCoursesWhichCouldBeOpened(Identity identity, SapUserType userType, String searchString);

	Course getLatestCourseByRepositoryEntry(RepositoryEntry repositoryEntry) throws Exception;

    void createDelegation(Identity delegator, Identity delegatee);

    boolean existsDelegation(Identity delegator, Identity delegatee);

    List<Long> getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters();

    List<IdentityDate> getDelegateesAndCreationDateByDelegator(Identity delegator);

	List<IdentityDate> getDelegatorsAndCreationDateByDelegatee(Identity delegatee);

    void deleteDelegation(Identity delegator, Identity delegatee);
}
