package ch.uzh.extension.campuscourse.service;

import ch.uzh.extension.campuscourse.model.CampusCourseWithoutListsTO;
import ch.uzh.extension.campuscourse.model.SapUserType;
import ch.uzh.extension.campuscourse.model.SemesterName;
import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import ch.uzh.extension.campuscourse.data.entity.Course;
import ch.uzh.extension.campuscourse.data.entity.Semester;
import ch.uzh.extension.campuscourse.model.CampusCourseTOForUI;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

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
    private Set<CampusCourseWithoutListsTO> coursesWithRepositoryEntry = new HashSet<>();
    private Set<CampusCourseWithoutListsTO> coursesWithoutRepositoryEntry = new HashSet<>();
    private Identity identityMock;

    @Before
    public void setup() {
        CampusCourseCoreService campusCourseCoreServiceMock = mock(CampusCourseCoreService.class);
        DaoManager daoManagerMock = mock(DaoManager.class);
        campusCourseLearnServiceImplTestObject = new CampusCourseServiceImpl(campusCourseCoreServiceMock, daoManagerMock);
        RepositoryEntry repositoryEntryMock = mock(RepositoryEntry.class);

        // Course which could be created
        Semester semester = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2016, false);
        Course course1 = new Course();
        course1.setId(50550670L);
        course1.setTitle("Wahlpflichtmodul MA: Forschungsseminar Policy Analyse, 2-sem. (SA)");
        course1.setLvKuerzel("ABCD11");
        course1.setLvNr("111111");
        course1.setSemester(semester);
        coursesWithoutRepositoryEntry.add(new CampusCourseWithoutListsTO(course1.getId(), null, null));
        when(daoManagerMock.loadCampusCourseTOForUI(course1.getId())).thenReturn(new CampusCourseTOForUI(course1.getTitleToBeDisplayed(), course1.getId()));

        // Course which could be opened
        Course course2 = new Course();
        course2.setId(50541483L);
        course2.setTitle("English Literature: Textual Analysis, Part II (Vorlesung zum Seminar)");
        course2.setLvKuerzel("ABCD22");
        course2.setLvNr("2222222");
        course2.setRepositoryEntry(repositoryEntryMock);
        course2.setSemester(semester);
        coursesWithRepositoryEntry.add(new CampusCourseWithoutListsTO(course2.getId(), null, repositoryEntryMock));
        when(daoManagerMock.loadCampusCourseTOForUI(course2.getId())).thenReturn(new CampusCourseTOForUI(course2.getTitleToBeDisplayed(), course2.getId()));

        // Course which could be created
        Course course3 = new Course();
        course3.setId(50541762L);
        course3.setTitle("Pflichtmodul M.A.-Seminar Sprachwissenschaft (6 KP)");
        course3.setLvKuerzel("ABC33");
        course3.setLvNr("333333");
        course3.setSemester(semester);
        coursesWithRepositoryEntry.add(new CampusCourseWithoutListsTO(course3.getId(), null, repositoryEntryMock));
        when(daoManagerMock.loadCampusCourseTOForUI(course3.getId())).thenReturn(new CampusCourseTOForUI(course3.getTitleToBeDisplayed(), course3.getId()));

        // Mock for Identity
        identityMock = mock(Identity.class);

        when(campusCourseCoreServiceMock.getNotCreatedCourses(identityMock, SapUserType.LECTURER, null))
                .thenReturn(coursesWithoutRepositoryEntry);
        when(campusCourseCoreServiceMock.getCreatedCourses(identityMock, SapUserType.LECTURER, null))
                .thenReturn(coursesWithRepositoryEntry);
    }

    @Test
    public void getCoursesWhichCouldBeOpened() {
        assertTrue(campusCourseLearnServiceImplTestObject.getCoursesWhichCouldBeOpened(identityMock, SapUserType.LECTURER, null).size() == coursesWithRepositoryEntry.size());
    }

    @Test
    public void getCoursesWhichCouldBeCreated() {
        assertTrue(campusCourseLearnServiceImplTestObject.getCoursesWhichCouldBeCreated(identityMock, null).size() == coursesWithoutRepositoryEntry.size());
    }
}
