package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.CampusCourseImportTO;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.resource.OLATResource;
import org.olat.user.UserImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
public class CampusCourseDescriptionBuilderTest {

    private final static String LECTURE_SOLL = "first_lectureA last_lectureA, first_lectureB last_lectureB";

    private CampusCourseDescriptionBuilder campusCourseDescriptionBuilder;
    private List<Identity> lecturers;

    @Before
    public void setup() {
        campusCourseDescriptionBuilder = new CampusCourseDescriptionBuilder();
        lecturers = new ArrayList<>();
        
        Identity identityLectureA = mock(IdentityImpl.class);
        UserImpl userA = mock(UserImpl.class);
        when(identityLectureA.getUser()).thenReturn(userA);
        when(identityLectureA.getUser().getProperty(UserConstants.FIRSTNAME, null)).thenReturn("first_lectureA");
        when(identityLectureA.getUser().getProperty(UserConstants.LASTNAME, null)).thenReturn("last_lectureA");                
        lecturers.add(identityLectureA);
                
        Identity identityLectureB = mock(IdentityImpl.class);
        UserImpl userB = mock(UserImpl.class);
        when(identityLectureB.getUser()).thenReturn(userB);
        when(identityLectureB.getUser().getProperty(UserConstants.FIRSTNAME, null)).thenReturn("first_lectureB");
        when(identityLectureB.getUser().getProperty(UserConstants.LASTNAME, null)).thenReturn("last_lectureB");
        lecturers.add(identityLectureB);
    }

    @Test
    public void getLectureList() {
        String generatedLecturesAsString = campusCourseDescriptionBuilder.getLectureList(lecturers);

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

        OLATResource olatResourceMock = mock(OLATResource.class);

        List<Identity> participants = new ArrayList<>();
        CampusCourseImportTO campusCourseData = new CampusCourseImportTO(
				title, semester, lecturers, participants,
				Collections.emptyList(), eventDescription,
				olatResourceMock, null, null, null);
        Translator translatorMock = mock(Translator.class);
        campusCourseDescriptionBuilder.translator = translatorMock;
        // Description example :
        // 'Herbstsemester 2012/n/nDozent1_Vorname Dozent1_Nachname, Dozent2_Vorname Dozent2_Nachname, Dozent3_Vorname Dozent3_Nachname/nContent'
        String descriptionSoll = semester + "/n/n" + LECTURE_SOLL + "/n/n" + eventDescription;
        when(translatorMock.translate(CampusCourseDescriptionBuilder.KEY_DESCRIPTION_TEMPLATE, argsMock)).thenReturn(descriptionSoll);
        campusCourseDescriptionBuilder.buildDescriptionFrom(campusCourseData, "de");

        // Check if argsMock contains the right parameter (argsMock[0] = semester; argsMock[1] = LECTURE_SOLL; argsMock[2] = eventDescription;)
        verify(translatorMock).translate(CampusCourseDescriptionBuilder.KEY_DESCRIPTION_TEMPLATE, argsMock);
    }
}
