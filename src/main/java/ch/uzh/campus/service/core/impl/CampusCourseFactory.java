package ch.uzh.campus.service.core.impl;


import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.service.CampusCourse;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 20.08.2012 <br>
 * 
 * @author cg
 */
@Component
public class CampusCourseFactory {
   
	private static final OLog LOG = Tracing.createLoggerFor(CampusCourseFactory.class);

    private final RepositoryManager repositoryManager;

    @Autowired
    public CampusCourseFactory(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    public CampusCourse getCampusCourse(CampusCourseImportTO campusCourseTo) {
		if (campusCourseTo == null) {
			return null;
		}
        OLATResource olatResource = campusCourseTo.getOlatResource();
        if (olatResource == null) {
            LOG.warn("sapCourseId = " + campusCourseTo.getSapCourseId() + ": no OLAT course found");
            return null;
        }
        LOG.debug("OLAT resource_id for sapCourseId=" + campusCourseTo.getSapCourseId() + ": campusCourseTo.getOlatResource.getKey()=" + olatResource.getKey());
        return getCampusCourseByOlatResource(olatResource);
    }

    CampusCourse getCampusCourseByOlatResource(OLATResource olatResource) {
        ICourse olatCourse = CourseFactory.loadCourse(olatResource.getResourceableId());
        RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(olatCourse, true);
        return new CampusCourse(olatCourse, repositoryEntry);
    }
}
