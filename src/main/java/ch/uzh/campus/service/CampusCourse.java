package ch.uzh.campus.service;

import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;



/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
public class CampusCourse {

    private final ICourse course;
    private final RepositoryEntry repositoryEntry;

    public CampusCourse(ICourse course, RepositoryEntry repositoryEntry) {
        this.course = course;
        this.repositoryEntry = repositoryEntry;
    }

    public ICourse getCourse() {
        return course;
    }

    public RepositoryEntry getRepositoryEntry() {
        return repositoryEntry;
    }

}