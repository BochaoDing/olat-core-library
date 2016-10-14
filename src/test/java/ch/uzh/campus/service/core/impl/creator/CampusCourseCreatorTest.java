package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.data.Semester;
import ch.uzh.campus.data.SemesterName;
import ch.uzh.campus.service.CampusCourse;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Initial Date: 11.03.2016 <br>
 *
 * @author cg
 * @author Martin Schraner
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class CampusCourseCreatorTest extends OlatTestCase {

    private static final String TITLE = "Test Title";
    private static final String DESCRIPTION = "Test Description";

    @Autowired
    private BGAreaManager areaManager;

    @Autowired
    private RepositoryManager repositoryManager;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    BusinessGroupService businessGroupService;

    @Autowired
    CampusCourseConfiguration campusCourseConfiguration;

    private CampusCourseCreator campusCourseCreatorTestObject;
    private OLATResource templateOlatResource;
    private Identity ownerIdentity;
    private RepositoryEntry sourceRepositoryEntry;
    private ICourse sourceCourse;
    private String ownerName = "owner";
    private String lvLanguage = "DE";
    private String areaName;
    private String groupNameA;
    private String groupDescriptionA;
    private String groupNameB;
    private String groupDescriptionB;
    private CampusCourseImportTO campusCourseImportData;

    @Before
    public void setup() {

        CampusCourseDescriptionBuilder campusCourseDescriptionBuilderMock = mock(CampusCourseDescriptionBuilder.class);
        when(campusCourseDescriptionBuilderMock.buildDescriptionFrom(any(), any())).thenReturn(DESCRIPTION);

        campusCourseCreatorTestObject = new CampusCourseCreator(repositoryManager, repositoryService, areaManager, businessGroupService, campusCourseConfiguration, campusCourseDescriptionBuilderMock);

        ownerIdentity = JunitTestHelper.createAndPersistIdentityAsUser(ownerName);
        sourceRepositoryEntry = JunitTestHelper.deployDemoCourse(ownerIdentity);
        templateOlatResource = sourceRepositoryEntry.getOlatResource();
        sourceCourse = CourseFactory.loadCourse(templateOlatResource);
        DBFactory.getInstance().closeSession();

        Translator translator = campusCourseCreatorTestObject.getTranslator(lvLanguage);
        areaName = translator.translate("campus.course.learningArea.name");
        groupNameA = translator.translate("campus.course.businessGroupA.name");
        groupDescriptionA = translator.translate("campus.course.businessGroupA.desc");
        groupNameB = translator.translate("campus.course.businessGroupB.name");
        groupDescriptionB = translator.translate("campus.course.businessGroupB.desc");

        Semester semester = new Semester(SemesterName.HERBSTSEMESTER, 2016, false);

        campusCourseImportData = new CampusCourseImportTO(TITLE,
				semester, Collections.emptyList(),
				Collections.emptyList(), Collections.emptyList(), DESCRIPTION,
				null, null, "DE", null);
    }

    @Test
    public void createCampusCourseFromTemplateTest() {
        CampusCourse campusCourse = campusCourseCreatorTestObject.createCampusCourseFromTemplate(campusCourseImportData, templateOlatResource, ownerIdentity, true);
        assertNotNull(campusCourse);

        assertNotNull(campusCourse.getRepositoryEntry());
        assertTrue("Copy must have different resourcableId", !Objects.equals(templateOlatResource.getResourceableId(), campusCourse.getCourse().getResourceableId()));
        assertEquals("Wrong initialAuthor in copy", ownerName, campusCourse.getRepositoryEntry().getInitialAuthor());
        assertEquals(TITLE, campusCourse.getRepositoryEntry().getDisplayname());
        assertEquals(DESCRIPTION, campusCourse.getRepositoryEntry().getDescription());
        assertEquals(RepositoryEntry.ACC_USERS_GUESTS, campusCourse.getRepositoryEntry().getAccess());

        assertNotNull(campusCourse.getCourse());
        assertEquals(TITLE, campusCourse.getCourse().getCourseTitle());
    }

    @Test
    public void createCampusLearningAreaAndCampusBusinessGroupsTest() {

        assertNull(areaManager.findBGArea(areaName, sourceRepositoryEntry.getOlatResource()));

        campusCourseCreatorTestObject.createCampusLearningAreaAndCampusBusinessGroups(sourceCourse, sourceRepositoryEntry, ownerIdentity, lvLanguage);

        // Check learning area
        BGArea campusLearningArea = areaManager.findBGArea(areaName, sourceRepositoryEntry.getOlatResource());
        assertNotNull(campusLearningArea);

        // Check business groups A and B
        List<BusinessGroup> groupsOfArea = areaManager.findBusinessGroupsOfArea(campusLearningArea);
        checkBusinessGroup(groupsOfArea, groupNameA, groupDescriptionA);
        checkBusinessGroup(groupsOfArea, groupNameB, groupDescriptionB);

        // Call method again -> no other business groups must be created
        int numberOfGroupsOfAreaBeforeCallingMethod = groupsOfArea.size();

        campusCourseCreatorTestObject.createCampusLearningAreaAndCampusBusinessGroups(sourceCourse, sourceRepositoryEntry, ownerIdentity, lvLanguage);

        assertEquals(numberOfGroupsOfAreaBeforeCallingMethod, areaManager.findBusinessGroupsOfArea(campusLearningArea).size());
    }

    private void checkBusinessGroup(List<BusinessGroup> groupsOfArea, String groupName, String groupDescription) {
        BusinessGroup businessGroupFound = null;
        for (BusinessGroup businessGroup : groupsOfArea) {
            if (businessGroup.getName().equals(groupName)) {
                businessGroupFound = businessGroup;
            }
        }
        assertNotNull(businessGroupFound);
        assertEquals(groupDescription, businessGroupFound.getDescription());
    }
}

