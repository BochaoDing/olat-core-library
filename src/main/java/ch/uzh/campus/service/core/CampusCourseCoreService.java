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

/*import org.olat.data.basesecurity.Identity;
import org.olat.data.course.campus.Course;
import org.olat.data.course.campus.SapOlatUser;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.core.CoreService;
import org.olat.lms.core.course.campus.CampusCourseImportTO;
import org.olat.lms.core.course.campus.impl.creator.CampusCourse;
import org.olat.system.commons.resource.OLATResourceable;
*/

/**
 * Initial Date: 16.07.2012 <br>
 * 
 * @author cg
 */
public interface CampusCourseCoreService /*extends CoreService*/ {

    public boolean checkDelegation(Long sapCampusCourseId, Identity creator);

    public CampusCourse createCampusCourseFromTemplate(Long courseResourceableId, Long sapCampusCourseId, Identity creator);

    public CampusCourse continueCampusCourse(Long courseResourceableId, Long sapCampusCourseId, String courseTitle, Identity creator);

    public CampusCourse createCampusCourse(Long resourceableId, Long sapCampusCourseId, Identity creator, CampusCourseImportTO campusCourseImportData);

    public void deleteResourceableIdReference(OLATResourceable res);

    public RepositoryEntry getRepositoryEntryFor(Long sapCourseId);

    /**
     * Get a list of Campus-courses which have resourceableId=null. resourceableId=null means no OLAT course is created in the OLAT course-repository yet.
     */
    public Set<Course> getCampusCoursesWithoutResourceableId(Identity identity, SapOlatUser.SapUserType userType);

    /**
     * Get list of Campus courses which already are created in the OLAT course-repository.
     */
    public Set<Course> getCampusCoursesWithResourceableId(Identity identity, SapOlatUser.SapUserType userType);

    public CampusCourse loadCampusCourse(Long sapCampusCourseId, Long resourceableId);

    public void createDelegation(Identity delegator, Identity delegatee);

    public boolean existDelegation(Identity delegator, Identity delegatee);

    public boolean existResourceableId(Long resourceableId);

    public List<Long> getAllCreatedSapCourcesResourceableIds();

    public List getDelegatees(Identity delegator);

    public void deleteDelegation(Identity delegator, Identity delegatee);

}
