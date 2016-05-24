package ch.uzh.campus.service.core.impl.creator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import ch.uzh.campus.service.CampusCourse;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author cg
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class CourseTemplateTest extends OlatTestCase {

    private Long sourceResourceableId;
    @Autowired
    private CourseTemplate courseTemplate;
    private Identity ownerIdentity;
    private ICourse sourceCourse;
    RepositoryEntry sourceRepositoryEntry;
    String ownerName = "owner";

    @Before
    public void setup() {
    	ownerIdentity = JunitTestHelper.createAndPersistIdentityAsUser(ownerName);
    	
        sourceRepositoryEntry = JunitTestHelper.deployDemoCourse(ownerIdentity);
        sourceResourceableId = sourceRepositoryEntry.getOlatResource().getResourceableId();
        sourceCourse = CourseFactory.loadCourse(sourceResourceableId);
        DBFactory.getInstance().closeSession();        
    }

    @Test
    public void createCampusCourseFromTemplate() {
        CampusCourse campusCourse = courseTemplate.createCampusCourseFromTemplate(sourceResourceableId, ownerIdentity);
        assertNotNull(campusCourse);
        assertNotNull(campusCourse.getCourse());
        assertNotNull(campusCourse.getRepositoryEntry());
        assertTrue("Copy must have different resourcableId", sourceResourceableId != campusCourse.getCourse().getResourceableId());
        
        ICourse copyCourse = CourseFactory.loadCourse(campusCourse.getCourse().getResourceableId());
        assertEquals("Course-title must be the same in the copy", sourceCourse.getCourseTitle(), copyCourse.getCourseTitle());
        assertEquals("Displayname of RepositoryEntry must be the same in the copy", sourceRepositoryEntry.getDisplayname(), campusCourse.getRepositoryEntry()
                .getDisplayname());
        assertEquals("Wrong initialAuthor in copy", ownerName, campusCourse.getRepositoryEntry().getInitialAuthor());
    }
}

