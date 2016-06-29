package ch.uzh.campus.service.learn;

import ch.uzh.campus.data.SapOlatUser;
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
     * Create a new campus-course from a course template. Copy template and update title, description, owner and participants.
     */
    CampusCourse createCampusCourseFromTemplate(Long courseResourceableId, Long sapCampusCourseId, Identity creator);

    /**
     * Uses an existing campus-course. It updates the title, description (including the vvz link), owner and participants.
     */
    CampusCourse continueCampusCourse(Long sapCampusCourseId, Long parentSapCampusCourseId, Identity creator);

    /**
     * Get a list of SAP campus-course which an identity could create. The courses must be not created and the identity must be owner of the courses.
     */
    List<SapCampusCourseTo> getCoursesWhichCouldBeCreated(Identity identity, SapOlatUser.SapUserType userType, String searchString);

    /**
     * Get a list of SAP campus-courses which are already created and identity is owner or participant.
     */
    List<SapCampusCourseTo> getCoursesWhichCouldBeOpened(Identity identity, SapOlatUser.SapUserType userType, String searchString);

    RepositoryEntry getRepositoryEntryFor(Long sapCourseId);

    void createDelegation(Identity delegator, Identity delegatee);

    boolean existDelegation(Identity delegator, Identity delegatee);

    boolean existResourceableId(Long resourceableId);

    List<Long> getAllCreatedSapCourcesResourceableIds();

    List getDelegatees(Identity delegator);

    void deleteDelegation(Identity delegator, Identity delegatee);
}
