package ch.uzh.campus.service.learn;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

import ch.uzh.campus.data.SapOlatUser;
import ch.uzh.campus.service.CampusCourse;

/**
 * This is called from presentation.
 * 
 * Initial Date: 30.11.2011 <br>
 * 
 * @author guretzki
 */
//public interface CampusCourseLearnService extends LearnService {
public interface CampusCourseService {

    public boolean checkDelegation(Long sapCampusCourseId, Identity creator);

    /**
     * Create a new campus-course from a course template. Copy template and update title, description, owner and participants.
     */
    public CampusCourse createCampusCourseFromTemplate(Long courseResourceableId, Long sapCampusCourseId, Identity creator);

    /**
     * Uses an existing campus-course. It updates the title, description (including the vvz link), owner and participants.
     */
    public CampusCourse continueCampusCourse(Long courseResourceableId, Long sapCampusCourseId, String courseTitle, Identity creator);

    /**
     * Get a list of SAP campus-course which an identity could create. The courses must be not created and the identity must be owner of the courses.
     */
    public List<SapCampusCourseTo> getCoursesWhichCouldBeCreated(Identity identity, SapOlatUser.SapUserType userType, String searchString);

    /**
     * Get a list of SAP campus-courses which are already created and identity is owner or participant.
     */
    public List<SapCampusCourseTo> getCoursesWhichCouldBeOpened(Identity identity, SapOlatUser.SapUserType userType, String searchString);

    public RepositoryEntry getRepositoryEntryFor(Long sapCourseId);

    public void createDelegation(Identity delegator, Identity delegatee);

    public boolean existDelegation(Identity delegator, Identity delegatee);

    public boolean existResourceableId(Long resourceableId);

    public List<Long> getAllCreatedSapCourcesResourceableIds();

    public List getDelegatees(Identity delegator);

    public void deleteDelegation(Identity delegator, Identity delegatee);
}
