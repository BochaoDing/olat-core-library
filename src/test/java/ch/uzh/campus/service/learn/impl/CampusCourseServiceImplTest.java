package ch.uzh.campus.service.learn.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.core.id.Identity;

import ch.uzh.campus.data.Course;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import ch.uzh.campus.data.SapOlatUser;


/**
 * Initial Date: 25.06.2012 <br>
 * 
 * @author aabouc
 */
public class CampusCourseServiceImplTest {
	
	private CampusCourseServiceImpl campusCourseLearnServiceImplTestObject;

    private Set<Course> coursesWithResourceableId = new HashSet<Course>();
    private Set<Course> coursesWithoutResourceableId = new HashSet<Course>();

    private Identity identityMock;

    @Before
    public void setup() {
        CampusCourseCoreService campusCourseCoreServiceMock = mock(CampusCourseCoreService.class);
        campusCourseLearnServiceImplTestObject = new CampusCourseServiceImpl();
        campusCourseLearnServiceImplTestObject.campusCourseCoreService = campusCourseCoreServiceMock;

        // Course which could be created
        Course course1 = new Course();
        course1.setId(new Long(50550670));
        course1.setTitle("Wahlpflichtmodul MA: Forschungsseminar Policy Analyse, 2-sem. (SA)");
        coursesWithoutResourceableId.add(course1);

        // Course which could be opened
        Course course2 = new Course();
        course2.setId(new Long(50541483));
        course2.setTitle("English Literature: Textual Analysis, Part II (Vorlesung zum Seminar)");
        course2.setResourceableId(new Long(99999));
        coursesWithResourceableId.add(course2);

        // Course which could be created
        Course course3 = new Course();
        course3.setId(new Long(50541762));
        course3.setTitle("Pflichtmodul M.A.-Seminar Sprachwissenschaft (6 KP)");
        coursesWithoutResourceableId.add(course3);

        // Mock for Identity
        identityMock = mock(Identity.class);
        // when(usesrServiceMock.getUserProperty(identityMock, propertyName))
        when(campusCourseCoreServiceMock.getCampusCoursesWithoutResourceableId(any(Identity.class), SapOlatUser.SapUserType.LECTURER)).thenReturn(
                coursesWithoutResourceableId);

    }

    @Ignore
    @Test
    public void getCoursesWhichCouldBeCreated() {
        assertTrue(campusCourseLearnServiceImplTestObject.getCoursesWhichCouldBeCreated(identityMock, SapOlatUser.SapUserType.LECTURER).size() == 1);
    }

}
