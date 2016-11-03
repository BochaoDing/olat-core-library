package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseJunitTestHelper;
import ch.uzh.campus.service.data.OlatCampusCourse;
import ch.uzh.campus.service.data.SapCampusCourseTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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
@ContextConfiguration(locations = {"classpath:org/olat/core/commons/persistence/_spring/testDatabaseCorecontext.xml"})
public class OlatCampusCourseCreatorTest extends OlatTestCase {

    private static final String OWNER_NAME = "ownerNameCampusCourseCreatorTest";
    private static final String TITLE = "Test Title";
    private static final String DESCRIPTION = "Test Description";

    @Autowired
    private DB dbInstance;

    @Autowired
    private RepositoryManager repositoryManager;

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

        olatCampusCourseCreatorTestObject = new OlatCampusCourseCreator(repositoryManager, repositoryService, campusCourseConfiguration, campusCourseRepositoryEntryDescriptionBuilderMock);
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
        SapCampusCourseTO sapCampusCourseTO = new SapCampusCourseTO(TITLE,
                null, Collections.emptySet(),
                Collections.emptySet(), Collections.emptySet(), false, null, DESCRIPTION,
                null, null, null, "DE", null);

        // Create template repository entry
        RepositoryEntry templateRepositoryEntry = JunitTestHelper.deployDemoCourse(ownerIdentity);
        OLATResource templateOlatResource = templateRepositoryEntry.getOlatResource();

        OlatCampusCourse createdOlatCampusCourse = olatCampusCourseCreatorTestObject.createOlatCampusCourseFromTemplate(sapCampusCourseTO, templateOlatResource, ownerIdentity, true);

        assertNotNull(createdOlatCampusCourse);

        // Check repository entry and olat resource
        assertNotNull(createdOlatCampusCourse.getRepositoryEntry());
        assertNotEquals("Copy must have different repository id", templateRepositoryEntry.getKey(), createdOlatCampusCourse.getRepositoryEntry().getKey());
        assertNotEquals("Copy must have different resourceable id", templateOlatResource.getResourceableId(), createdOlatCampusCourse.getCourse().getResourceableId());
        assertEquals("Wrong ownerName in copy", OWNER_NAME, createdOlatCampusCourse.getRepositoryEntry().getInitialAuthor());
        assertEquals(TITLE, createdOlatCampusCourse.getRepositoryEntry().getDisplayname());
        assertEquals(DESCRIPTION, createdOlatCampusCourse.getRepositoryEntry().getDescription());
        assertEquals(RepositoryEntry.ACC_USERS_GUESTS, createdOlatCampusCourse.getRepositoryEntry().getAccess());

        // Check course
        assertNotNull(createdOlatCampusCourse.getCourse());
        assertEquals(TITLE, createdOlatCampusCourse.getCourse().getCourseTitle());
    }
}

