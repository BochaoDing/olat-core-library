package ch.uzh.campus.service.core.impl.syncer;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.data.Semester;
import ch.uzh.campus.data.SemesterName;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.CampusCourseFactory;
import ch.uzh.campus.service.core.impl.creator.CampusCourseDescriptionBuilder;
import ch.uzh.campus.service.core.impl.syncer.statistic.TitleAndDescriptionStatistik;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.test.OlatTestCase;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Initial Date: 28.08.2012 <br>
 * 
 * @author cg
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class CampusCourseAttributeSynchronizerTest extends OlatTestCase {

    private long sapCampusCourseId = 1L;

    private Semester semester = new Semester(SemesterName.HERBSTSEMESTER, 2012, false);
    private List<Identity> lecturers = new ArrayList<>();
    private List<Identity> participants = new ArrayList<>();
    private String title = "title";
    private String eventDescription = "eventDescription";

    private CampusCourseAttributeSynchronizer campusCourseAttributeSynchronizerTestObject;
    private CampusCourseFactory campusCourseFactoryMock;
    private CampusCourseDescriptionBuilder campusCourseDescriptionBuilderMock;
    private CampusCourseConfiguration campusCourseConfigurationMock;
    private OLATResource olatResourceMock;
    private CampusCourse campusCourse;

    @Before
    public void setup() {
        campusCourseFactoryMock = mock(CampusCourseFactory.class);
        campusCourseDescriptionBuilderMock = mock(CampusCourseDescriptionBuilder.class);
        campusCourseConfigurationMock = mock(CampusCourseConfiguration.class);
        olatResourceMock = mock(OLATResource.class);
        campusCourseAttributeSynchronizerTestObject = new CampusCourseAttributeSynchronizer(campusCourseFactoryMock, campusCourseDescriptionBuilderMock, campusCourseConfigurationMock);
        ICourse course = mock(ICourse.class);
        RepositoryEntry repositoryEntry = mock(RepositoryEntry.class);
        when(repositoryEntry.getDisplayname()).thenReturn(title);
        when(repositoryEntry.getDescription()).thenReturn(eventDescription);
        campusCourse = new CampusCourse(course, repositoryEntry);
    }

    @Test
    public void synchronizeTitleAndDescription_nothingToUpdate() {
		CampusCourseImportTO campusCourseImportTO = new CampusCourseImportTO(title, semester, lecturers, Collections.emptyList(), participants, eventDescription, olatResourceMock, sapCampusCourseId, null, null);
		when(campusCourseFactoryMock.getCampusCourse(refEq(campusCourseImportTO))).thenReturn(campusCourse);
		when(campusCourseDescriptionBuilderMock.buildDescriptionFrom(campusCourseImportTO, "de")).thenReturn(campusCourseImportTO.getEventDescription());
        when(campusCourseConfigurationMock.getTemplateLanguage(campusCourseImportTO.getLanguage())).thenReturn("de");
        TitleAndDescriptionStatistik titleAndDescriptionStatistik = campusCourseAttributeSynchronizerTestObject.synchronizeTitleAndDescription(campusCourseImportTO);

        assertNotNull("Missing TitleAndDescriptionStatistik", titleAndDescriptionStatistik);
        assertFalse("Title should not be updated", titleAndDescriptionStatistik.isTitleUpdated());
        assertFalse("Description should not be updated", titleAndDescriptionStatistik.isDescriptionUpdated());
    }

    @Test
    public void synchronizeTitleAndDescription_updateDescription() {
        CampusCourseImportTO campusCourseImportTO = new CampusCourseImportTO(title, semester, lecturers, Collections.emptyList(), participants, eventDescription + "_new", olatResourceMock, sapCampusCourseId, null, null);
		when(campusCourseFactoryMock.getCampusCourse(refEq(campusCourseImportTO))).thenReturn(campusCourse);

        TitleAndDescriptionStatistik titleAndDescriptionStatistik = campusCourseAttributeSynchronizerTestObject.synchronizeTitleAndDescription(campusCourseImportTO);

        assertNotNull("Missing TitleAndDescriptionStatistik", titleAndDescriptionStatistik);
        assertFalse("Title should not be updated", titleAndDescriptionStatistik.isTitleUpdated());
        assertTrue("Description should be updated", titleAndDescriptionStatistik.isDescriptionUpdated());
    }

    @Test
    public void synchronizeTitleAndDescription_updateTitle() {
        CampusCourseImportTO campusCourseImportTO = new CampusCourseImportTO(title + "_new", semester, lecturers, Collections.emptyList(), participants, eventDescription, olatResourceMock, sapCampusCourseId, null, null);
        // do not call real CampusCourse.setTruncatedTitle(..) because there is a static call which try to save runstructure.xml
        CampusCourse spyCampusCourse = spy(campusCourse);
        when(campusCourseFactoryMock.getCampusCourse(any())).thenReturn(spyCampusCourse);
        when(campusCourseDescriptionBuilderMock.buildDescriptionFrom(campusCourseImportTO, campusCourseImportTO.getLanguage())).thenReturn(campusCourseImportTO.getEventDescription());
        TitleAndDescriptionStatistik titleAndDescriptionStatistik = campusCourseAttributeSynchronizerTestObject.synchronizeTitleAndDescription(campusCourseImportTO);

        assertNotNull("Missing TitleAndDescriptionStatistik", titleAndDescriptionStatistik);
        assertTrue("Title should not be updated", titleAndDescriptionStatistik.isTitleUpdated());
        assertFalse("Description should be updated", titleAndDescriptionStatistik.isDescriptionUpdated());
    }
}
