package ch.uzh.campus.service.core.impl;


import ch.uzh.campus.service.data.OlatCampusCourse;
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
public class OlatCampusCourseProvider {

    private final RepositoryManager repositoryManager;

    @Autowired
    public OlatCampusCourseProvider(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    public OlatCampusCourse loadOlatCampusCourse(OLATResource olatResource) {
        if (olatResource == null) {
            return null;
        }
        ICourse olatCourse = CourseFactory.loadCourse(olatResource.getResourceableId());
        RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(olatCourse, true);
        return new OlatCampusCourse(olatCourse, repositoryEntry);
    }
}
