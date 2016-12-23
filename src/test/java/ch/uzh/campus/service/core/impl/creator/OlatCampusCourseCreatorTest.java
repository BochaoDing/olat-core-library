package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseJunitTestHelper;
import ch.uzh.campus.CampusCourseTestCase;
import ch.uzh.campus.service.data.CampusCourseTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

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
@Component
public class OlatCampusCourseCreatorTest extends CampusCourseTestCase {

    private static final String OWNER_NAME = "ownerNameCampusCourseCreatorTest";
    private static final String TITLE = "Test Title";
    private static final String DESCRIPTION = "Test Description";

    @Autowired
    private DB dbInstance;

    @Autowired
    private UserManager userManager;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    private OlatCampusCourseCreator olatCampusCourseCreatorTestObject;

    @Before
    public void setup() {

        CampusCourseRepositoryEntryDescriptionBuilder campusCourseRepositoryEntryDescriptionBuilderMock = mock(CampusCourseRepositoryEntryDescriptionBuilder.class);
        when(campusCourseRepositoryEntryDescriptionBuilderMock.buildDescription(any())).thenReturn(DESCRIPTION);

        olatCampusCourseCreatorTestObject = new OlatCampusCourseCreator(repositoryService, campusCourseConfiguration, campusCourseRepositoryEntryDescriptionBuilderMock);
    }

    @After
    public void tearDown() throws Exception {
        dbInstance.rollback();
    }

    @Test
    public void testCreateCampusCourseFromTemplate() {

        // Create owner
        Identity ownerIdentity = CampusCourseJunitTestHelper.createTestUser(userManager, dbInstance, OWNER_NAME);

        // Create TO of olat course to be created
        CampusCourseTO campusCourseTO = new CampusCourseTO(TITLE,
                null, Collections.emptySet(),
                Collections.emptySet(), Collections.emptySet(), false, null, DESCRIPTION,
                null, null, null, "DE", null);

        // Create template repository entry
        RepositoryEntry templateRepositoryEntry = JunitTestHelper.deployDemoCourse(ownerIdentity);

        RepositoryEntry createdRepositoryEntry = olatCampusCourseCreatorTestObject.createOlatCampusCourseFromTemplate(templateRepositoryEntry, campusCourseTO, ownerIdentity, true);

        assertNotNull(createdRepositoryEntry);

        // Check repository entry and olat resource
        assertNotNull(createdRepositoryEntry);
        assertNotEquals("Copy must have different repository id", templateRepositoryEntry.getKey(), createdRepositoryEntry.getKey());
        assertNotEquals("Copy must have different resourceable id", templateRepositoryEntry.getOlatResource().getResourceableId(), createdRepositoryEntry.getOlatResource().getResourceableId());
        assertEquals("Wrong ownerName in copy", OWNER_NAME, createdRepositoryEntry.getInitialAuthor());
        assertEquals(TITLE, createdRepositoryEntry.getDisplayname());
        assertEquals(DESCRIPTION, createdRepositoryEntry.getDescription());
        assertEquals(RepositoryEntry.ACC_USERS_GUESTS, createdRepositoryEntry.getAccess());

        // Check course
        ICourse course = CourseFactory.loadCourse(createdRepositoryEntry);
        assertNotNull(course);
        assertEquals(TITLE, course.getCourseTitle());
    }
}

