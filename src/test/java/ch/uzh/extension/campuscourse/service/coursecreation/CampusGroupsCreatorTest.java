package ch.uzh.extension.campuscourse.service.coursecreation;

import ch.uzh.extension.campuscourse.CampusCourseJunitTestHelper;
import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import ch.uzh.extension.campuscourse.model.CampusGroups;
import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Martin Schraner
 */
@Component
public class CampusGroupsCreatorTest extends CampusCourseTestCase {

    @Autowired
    private CampusGroupsCreator campusGroupsCreator;

    @Autowired
    private DB dbInstance;

    @Autowired
    private BGAreaManager areaManager;

    @Autowired
    private UserManager userManager;

    private static final String LV_LANGUAGE = "DE";

    @After
    public void tearDown() throws Exception {
        dbInstance.rollback();
    }

    @Test
    public void testCreateCampusLearningAreaAndCampusGroups() {

        Translator translator = campusGroupsCreator.getTranslator(LV_LANGUAGE);
        String learningAreaName = translator.translate("campus.course.learningArea.name");
        String groupNameA = translator.translate("campus.course.businessGroupA.name");
        String groupDescriptionA = translator.translate("campus.course.businessGroupA.desc");
        String groupNameB = translator.translate("campus.course.businessGroupB.name");
        String groupDescriptionB = translator.translate("campus.course.businessGroupB.desc");

        // Create owner
        Identity ownerIdentity = CampusCourseJunitTestHelper.createTestUser(userManager, dbInstance, "ownerNameCampusCourseCreatorTest");

        // Create olat demo course
        RepositoryEntry demoCourseRepositoryEntry = JunitTestHelper.deployDemoCourse(ownerIdentity);

        // Check that no learning area is related to olat resource
        assertNull(areaManager.findBGArea(learningAreaName, demoCourseRepositoryEntry.getOlatResource()));

        CampusGroups createdCampusGroups1 = campusGroupsCreator.createCampusLearningAreaAndCampusGroupsIfNecessary(demoCourseRepositoryEntry, ownerIdentity, LV_LANGUAGE);
        dbInstance.flush();

        // Check campus groups
        assertNotNull(createdCampusGroups1);
        BusinessGroup campusGroupA = createdCampusGroups1.getCampusGroupA();
        assertNotNull(campusGroupA);
        assertEquals(groupNameA, campusGroupA.getName());
        assertEquals(groupDescriptionA, campusGroupA.getDescription());

        BusinessGroup campusGroupB = createdCampusGroups1.getCampusGroupB();
        assertNotNull(campusGroupB);
        assertEquals(groupNameB, campusGroupB.getName());
        assertEquals(groupDescriptionB, campusGroupB.getDescription());

        // Check learning area
        BGArea campusLearningArea = areaManager.findBGArea(learningAreaName, demoCourseRepositoryEntry.getOlatResource());
        assertNotNull(campusLearningArea);

        List<BusinessGroup> businessGroupsOfLearningArea = areaManager.findBusinessGroupsOfArea(campusLearningArea);
        List<Long> keysOfBusinessGroupsOfLearningArea = businessGroupsOfLearningArea.stream().map(BusinessGroupRef::getKey).collect(Collectors.toList());
        assertTrue(keysOfBusinessGroupsOfLearningArea.contains(campusGroupA.getKey()));
        assertTrue(keysOfBusinessGroupsOfLearningArea.contains(campusGroupB.getKey()));

        // Call method again -> We should get the same campus groups as before
        int numberOfGroupsOfAreaBeforeCallingMethod = businessGroupsOfLearningArea.size();
        CampusGroups createdCampusGroups2 = campusGroupsCreator.createCampusLearningAreaAndCampusGroupsIfNecessary(demoCourseRepositoryEntry, ownerIdentity, LV_LANGUAGE);

        assertEquals(createdCampusGroups1, createdCampusGroups2);
        assertEquals(numberOfGroupsOfAreaBeforeCallingMethod, areaManager.findBusinessGroupsOfArea(campusLearningArea).size());
    }

}