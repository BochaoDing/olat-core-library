package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.data.Semester;
import ch.uzh.campus.data.SemesterName;
import ch.uzh.campus.service.data.SapCampusCourseTO;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.test.OlatTestCase;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
@ContextConfiguration(locations = {"classpath:/org/olat/_spring/mainContext.xml"})
public class CampusCourseRepositoryEntryDescriptionBuilderTest extends OlatTestCase {

    private static final String SORTED_LECTURERS = "first_lectureA last_lectureA, first_lectureB last_lectureB, first_lectureC last_lectureC";

    @Autowired
    private CampusCourseRepositoryEntryDescriptionBuilder campusCourseRepositoryEntryDescriptionBuilder;

    private Set<Identity> lecturers = new HashSet<>();

    @Before
    public void setup() {
        Identity identityLectureA = mock(IdentityImpl.class);
        UserImpl userA = mock(UserImpl.class);
        when(identityLectureA.getUser()).thenReturn(userA);
        when(identityLectureA.getUser().getProperty(UserConstants.FIRSTNAME, null)).thenReturn("first_lectureA");
        when(identityLectureA.getUser().getProperty(UserConstants.LASTNAME, null)).thenReturn("last_lectureA");
                
        Identity identityLectureB = mock(IdentityImpl.class);
        UserImpl userB = mock(UserImpl.class);
        when(identityLectureB.getUser()).thenReturn(userB);
        when(identityLectureB.getUser().getProperty(UserConstants.FIRSTNAME, null)).thenReturn("first_lectureB");
        when(identityLectureB.getUser().getProperty(UserConstants.LASTNAME, null)).thenReturn("last_lectureB");

        Identity identityLectureC = mock(IdentityImpl.class);
        UserImpl userC = mock(UserImpl.class);
        when(identityLectureC.getUser()).thenReturn(userC);
        when(identityLectureC.getUser().getProperty(UserConstants.FIRSTNAME, null)).thenReturn("first_lectureC");
        when(identityLectureC.getUser().getProperty(UserConstants.LASTNAME, null)).thenReturn("last_lectureC");

        // Not sorted
        lecturers.add(identityLectureB);
        lecturers.add(identityLectureC);
        lecturers.add(identityLectureA);
    }

    @Test
    public void testBuildDescription_notContinuedCampusCourse() {
        String eventDescription = "Course";
        Semester semester = new Semester(SemesterName.HERBSTSEMESTER, 2012, false);

        SapCampusCourseTO campusCourseData = new SapCampusCourseTO(
                null, semester, lecturers, Collections.emptySet(),
                Collections.emptySet(), false, Collections.emptyList(), eventDescription,
                null, null, null, "de", null);

        String description = campusCourseRepositoryEntryDescriptionBuilder.buildDescription(campusCourseData);
        assertTrue(description.contains(semester.getSemesterNameYear()));
        assertTrue(description.contains(SORTED_LECTURERS));
        assertTrue(description.contains(eventDescription));
    }

    @Test
    public void testBuildDescription_continuedCampusCourse() {
        String title1 = "Course I";
        String title2 = "Course II";
        String eventDescription = "Detail_description";

        List<String> titlesOfCourseAndParentCourses = new ArrayList<>();
        titlesOfCourseAndParentCourses.add(title1);
        titlesOfCourseAndParentCourses.add(title2);

        SapCampusCourseTO campusCourseData = new SapCampusCourseTO(
				null, null, lecturers, Collections.emptySet(),
				Collections.emptySet(), true, titlesOfCourseAndParentCourses, eventDescription,
				null, null, null, "de", null);

        String description = campusCourseRepositoryEntryDescriptionBuilder.buildDescription(campusCourseData);
        assertTrue(description.contains(campusCourseRepositoryEntryDescriptionBuilder.createMultiSemesterTitle(titlesOfCourseAndParentCourses)));
        assertTrue(description.contains(SORTED_LECTURERS));
        assertTrue(description.contains(eventDescription));
    }

    @Test
    public void testCreateMultiSemesterTitle() {
        List<String> titleOfCourseAndParentCourses = new ArrayList<>();
        titleOfCourseAndParentCourses.add("Course I");
        titleOfCourseAndParentCourses.add("Course II");
        titleOfCourseAndParentCourses.add("Course III");
        assertEquals("Course I<br>Course II<br>Course III", campusCourseRepositoryEntryDescriptionBuilder.createMultiSemesterTitle(titleOfCourseAndParentCourses));
    }

    @Test
    public void testCreateStringOfAlphabeticallySortedLecturers() {
        String generatedLecturesAsString = campusCourseRepositoryEntryDescriptionBuilder.createStringOfAlphabeticallySortedLecturers(lecturers);
        assertEquals(SORTED_LECTURERS, generatedLecturesAsString);
    }
}
