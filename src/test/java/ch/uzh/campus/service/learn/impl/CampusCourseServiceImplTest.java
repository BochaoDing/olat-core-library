package ch.uzh.campus.service.learn.impl;

import ch.uzh.campus.data.Course;
import ch.uzh.campus.data.SapOlatUser;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryManager;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Initial Date: 25.06.2012<br />
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
		RepositoryManager repositoryManager = mock(RepositoryManager.class);
        campusCourseLearnServiceImplTestObject = new CampusCourseServiceImpl(
				campusCourseCoreServiceMock,
				repositoryManager,
				"true"
		);

        // Course which could be created
        Course course1 = new Course();
        course1.setId(new Long(50550670));
        course1.setTitle("Wahlpflichtmodul MA: Forschungsseminar Policy Analyse, 2-sem. (SA)");
        course1.setShortTitle("Short Title 1");
        course1.setVstNr("111111");
        coursesWithoutResourceableId.add(course1);

        // Course which could be opened
        Course course2 = new Course();
        course2.setId(new Long(50541483));
        course2.setTitle("English Literature: Textual Analysis, Part II (Vorlesung zum Seminar)");
        course2.setShortTitle("Short Title 2");
        course2.setVstNr("2222222");
        course2.setResourceableId(new Long(99999));
        coursesWithResourceableId.add(course2);

        // Course which could be created
        Course course3 = new Course();
        course3.setId(new Long(50541762));
        course3.setTitle("Pflichtmodul M.A.-Seminar Sprachwissenschaft (6 KP)");
        course3.setShortTitle("Short Title 3");
        course3.setVstNr("333333");
        coursesWithoutResourceableId.add(course3);

        // Mock for Identity
        identityMock = mock(Identity.class);

        when(campusCourseCoreServiceMock.getCampusCoursesWithoutResourceableId(identityMock, SapOlatUser.SapUserType.LECTURER, null))
                .thenReturn(coursesWithoutResourceableId);
        when(campusCourseCoreServiceMock.getCampusCoursesWithResourceableId(identityMock, SapOlatUser.SapUserType.LECTURER, null))
                .thenReturn(coursesWithResourceableId);
    }

    @Test
    public void getCoursesWhichCouldBeOpened() {
        assertTrue(campusCourseLearnServiceImplTestObject.getCoursesWhichCouldBeOpened(identityMock, SapOlatUser.SapUserType.LECTURER, null).size() == coursesWithResourceableId.size());
    }

    @Test
    public void getCoursesWhichCouldBeCreated() {
        assertTrue(campusCourseLearnServiceImplTestObject.getCoursesWhichCouldBeCreated(identityMock, null).size() == coursesWithoutResourceableId.size());
    }
}