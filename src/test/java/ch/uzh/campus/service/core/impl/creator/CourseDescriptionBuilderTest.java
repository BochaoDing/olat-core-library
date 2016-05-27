package ch.uzh.campus.service.core.impl.creator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.user.UserImpl;

import ch.uzh.campus.CampusCourseImportTO;


/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
public class CourseDescriptionBuilderTest {

    final static String LECTURE_SOLL = "first_lectureA last_lectureA, first_lectureB last_lectureB";
    final static Long TEST_RESOURCEABLE_ID = 1234L;

    CourseDescriptionBuilder courseDescriptionBuilder;
    private Identity identityLectureA;
    private Identity identityLectureB;
    List<Identity> lecturers;

    @Before
    public void setup() {
        courseDescriptionBuilder = new CourseDescriptionBuilder();
        lecturers = new ArrayList<Identity>();
        
        identityLectureA = mock(IdentityImpl.class);
        UserImpl userA = mock(UserImpl.class);
        when(identityLectureA.getUser()).thenReturn(userA);
        when(identityLectureA.getUser().getProperty(UserConstants.FIRSTNAME, null)).thenReturn("first_lectureA");
        when(identityLectureA.getUser().getProperty(UserConstants.LASTNAME, null)).thenReturn("last_lectureA");                
        lecturers.add(identityLectureA);
                
        identityLectureB = mock(IdentityImpl.class);
        UserImpl userB = mock(UserImpl.class);
        when(identityLectureB.getUser()).thenReturn(userB);
        when(identityLectureB.getUser().getProperty(UserConstants.FIRSTNAME, null)).thenReturn("first_lectureB");
        when(identityLectureB.getUser().getProperty(UserConstants.LASTNAME, null)).thenReturn("last_lectureB");
        lecturers.add(identityLectureB);
    }

    @Test
    public void getLectureList() {
        String generatedLecturesAsString = courseDescriptionBuilder.getLectureList(lecturers);

        assertEquals("Wrong lecture list", LECTURE_SOLL, generatedLecturesAsString);
    }

    @Test
    public void buildDescriptionFrom() {
        String title = "Example title";
        String semester = "Herbstsemester 2012";
        String eventDescription = "Detail_description";

        String[] argsMock = new String[3];
        argsMock[0] = semester;
        argsMock[1] = LECTURE_SOLL;
        argsMock[2] = eventDescription;

        List<Identity> participants = new ArrayList<Identity>();
        CampusCourseImportTO campusCourseData = new CampusCourseImportTO(title, semester, lecturers, participants, eventDescription, TEST_RESOURCEABLE_ID, null);
        Translator translatorMock = mock(Translator.class);
        courseDescriptionBuilder.translator = translatorMock;
        // Description example :
        // 'Herbstsemester 2012/n/nDozent1_Vorname Dozent1_Nachname, Dozent2_Vorname Dozent2_Nachname, Dozent3_Vorname Dozent3_Nachname/nContent'
        String descriptionSoll = semester + "/n/n" + LECTURE_SOLL + "/n/n" + eventDescription;
        when(translatorMock.translate(CourseDescriptionBuilder.KEY_DESCRIPTION_TEMPLATE, argsMock)).thenReturn(descriptionSoll);
        String generatedDescription = courseDescriptionBuilder.buildDescriptionFrom(campusCourseData, "de");

        // Check if argsMock contains the right parameter (argsMock[0] = semester; argsMock[1] = LECTURE_SOLL; argsMock[2] = eventDescription;)
        verify(translatorMock).translate(CourseDescriptionBuilder.KEY_DESCRIPTION_TEMPLATE, argsMock);
    }

}
