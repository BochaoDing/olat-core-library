package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.data.Semester;
import ch.uzh.campus.data.SemesterName;
import ch.uzh.campus.service.data.SapCampusCourseTO;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.resource.OLATResource;
import org.olat.user.UserImpl;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
public class CampusCourseRepositoryEntryDescriptionBuilderTest {

    private final static String SORTED_LECTURERS = "first_lectureA last_lectureA, first_lectureB last_lectureB, first_lectureC last_lectureC";

    private CampusCourseRepositoryEntryDescriptionBuilder campusCourseRepositoryEntryDescriptionBuilder;
    private Set<Identity> lecturers = new HashSet<>();

    @Before
    public void setup() {
        campusCourseRepositoryEntryDescriptionBuilder = new CampusCourseRepositoryEntryDescriptionBuilder();
        
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

        lecturers.add(identityLectureB);
        lecturers.add(identityLectureC);
        lecturers.add(identityLectureA);
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
    public void testGetAlphabeticallySortedLecturerList() {
        String generatedLecturesAsString = campusCourseRepositoryEntryDescriptionBuilder.getAlphabeticallySortedLecturerList(lecturers);
        assertEquals("Wrong lecture list", SORTED_LECTURERS, generatedLecturesAsString);
    }

    @Test
    public void testBuildDescriptionFrom() {
        String title = "Example title";
        String eventDescription = "Detail_description";

        Semester semester = new Semester(SemesterName.HERBSTSEMESTER, 2012, false);

        String[] argsMock = new String[3];
        argsMock[0] = semester.getSemesterNameYear();
        argsMock[1] = SORTED_LECTURERS;
        argsMock[2] = eventDescription;

        OLATResource olatResourceMock = mock(OLATResource.class);

        SapCampusCourseTO campusCourseData = new SapCampusCourseTO(
				title, semester, lecturers, Collections.emptySet(),
				Collections.emptySet(), false, Collections.emptyList(), eventDescription,
				olatResourceMock, null, null, null, null, null);
        Translator translatorMock = mock(Translator.class);
        campusCourseRepositoryEntryDescriptionBuilder.translator = translatorMock;

        // Description example :
        // 'Herbstsemester 2012/n/nDozent1_Vorname Dozent1_Nachname, Dozent2_Vorname Dozent2_Nachname, Dozent3_Vorname Dozent3_Nachname/nContent'
        String descriptionSoll = semester + "/n/n" + SORTED_LECTURERS + "/n/n" + eventDescription;
        when(translatorMock.translate(CampusCourseRepositoryEntryDescriptionBuilder.KEY_DESCRIPTION_TEMPLATE, argsMock)).thenReturn(descriptionSoll);
        campusCourseRepositoryEntryDescriptionBuilder.buildDescriptionFrom(campusCourseData, "de");

        // Check if argsMock contains the right parameter (argsMock[0] = semester; argsMock[1] = SORTED_LECTURERS; argsMock[2] = eventDescription;)
        verify(translatorMock).translate(CampusCourseRepositoryEntryDescriptionBuilder.KEY_DESCRIPTION_TEMPLATE, argsMock);
    }


}
