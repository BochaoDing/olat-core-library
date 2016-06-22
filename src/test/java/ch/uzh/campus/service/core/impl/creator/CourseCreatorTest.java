package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.service.CampusCourse;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 * Initial Date: 11.03.2016 <br>
 *
 * @author cg
 * @author Martin Schraner
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class CourseCreatorTest extends OlatTestCase {

    @Autowired
    private CourseCreator courseCreator;

    @Autowired
    private BGAreaManager areaManager;

    private Long sourceResourceableId;
    private Identity ownerIdentity;
    private ICourse sourceCourse;
    private RepositoryEntry sourceRepositoryEntry;
    private String ownerName = "owner";
    private String lvLanguage = "DE";
    private String areaName;
    private String groupNameA;
    private String groupDescriptionA;
    private String groupNameB;
    private String groupDescriptionB;

    @Before
    public void setup() {
        ownerIdentity = JunitTestHelper.createAndPersistIdentityAsUser(ownerName);

        sourceRepositoryEntry = JunitTestHelper.deployDemoCourse(ownerIdentity);
        sourceResourceableId = sourceRepositoryEntry.getOlatResource().getResourceableId();
        sourceCourse = CourseFactory.loadCourse(sourceResourceableId);
        DBFactory.getInstance().closeSession();

        Translator translator = courseCreator.getTranslator(lvLanguage);
        areaName = translator.translate("campus.course.learningArea.name");
        groupNameA = translator.translate("campus.course.businessGroupA.name");
        groupDescriptionA = translator.translate("campus.course.businessGroupA.desc");
        groupNameB = translator.translate("campus.course.businessGroupB.name");
        groupDescriptionB = translator.translate("campus.course.businessGroupB.desc");
    }

    @Test
    public void createCampusCourseFromTemplateTest() {
        CampusCourse campusCourse = courseCreator.createCampusCourseFromTemplate(sourceResourceableId, ownerIdentity);
        assertNotNull(campusCourse);
        assertNotNull(campusCourse.getCourse());
        assertNotNull(campusCourse.getRepositoryEntry());
        assertTrue("Copy must have different resourcableId", !Objects.equals(sourceResourceableId, campusCourse.getCourse().getResourceableId()));
        assertEquals("Displayname of RepositoryEntry must be the same in the copy", sourceRepositoryEntry.getDisplayname(), campusCourse.getRepositoryEntry().getDisplayname());
        assertEquals("Wrong initialAuthor in copy", ownerName, campusCourse.getRepositoryEntry().getInitialAuthor());

        ICourse copyCourse = CourseFactory.loadCourse(campusCourse.getCourse().getResourceableId());
        assertEquals("Course-title must be the same in the copy", sourceCourse.getCourseTitle(), copyCourse.getCourseTitle());
    }

    @Test
    public void createCampusLearningAreaAndCampusBusinessGroupsTest() {

        assertNull(areaManager.findBGArea(areaName, sourceRepositoryEntry.getOlatResource()));

        courseCreator.createCampusLearningAreaAndCampusBusinessGroups(sourceRepositoryEntry, ownerIdentity, lvLanguage);

        // Check learning area
        BGArea campusLearningArea = areaManager.findBGArea(areaName, sourceRepositoryEntry.getOlatResource());
        assertNotNull(campusLearningArea);

        // Check business groups A and B
        List<BusinessGroup> groupsOfArea = areaManager.findBusinessGroupsOfArea(campusLearningArea);
        checkBusinessGroup(groupsOfArea, groupNameA, groupDescriptionA);
        checkBusinessGroup(groupsOfArea, groupNameB, groupDescriptionB);

        // Call method again -> no other business groups must be created
        int numberOfGroupsOfAreaBeforeCallingMethod = groupsOfArea.size();

        courseCreator.createCampusLearningAreaAndCampusBusinessGroups(sourceRepositoryEntry, ownerIdentity, lvLanguage);

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

