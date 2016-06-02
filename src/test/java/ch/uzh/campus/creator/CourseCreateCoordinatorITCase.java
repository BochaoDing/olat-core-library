/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package ch.uzh.campus.creator;

import ch.uzh.campus.CampusConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.creator.CourseCreateCoordinator;
import ch.uzh.campus.service.core.impl.creator.CoursePublisher;
import ch.uzh.campus.syncer.CampusGroupHelper;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author cg
 */
public class CourseCreateCoordinatorITCase extends OlatTestCase {

    private static final OLog log = Tracing.createLoggerFor(CourseCreateCoordinatorITCase.class);

    private static final String TEST_TITLE_TEXT = "Test Title";
    private static final String TEST_SEMESTER_TEXT = "Herbstsemester 2012";
    private static final String TEST_EVENT_DESCRIPTION_TEXT = "Event description";
    private static final String TEST_COURSE_GROUP_A_NAME = "Campusgroup A";
    private static final String TEST_COURSE_GROUP_B_NAME = "Campusgroup B";
    private static final Long TEST_RESOURCEABLE_ID = 1234L;

    @Autowired
    private CourseCreateCoordinator courseCreateCoordinator;
    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private BusinessGroupService businessGroupService;

    private Long sourceResourceableId;
    private ICourse sourceCourse;
    private RepositoryEntry sourceRepositoryEntry;
    private String ownerName = "TestOwner";
    private Identity ownerIdentity;
    private String ownerNameSecond = "SecondTestOwner";
    private Identity secondOwnerIdentity;
    private String testUserName = "TestUser";
    private Identity testIdentity;
    private String secondTestUserName = "SecondTestUser";
    private Identity secondTestIdentity;

    private CampusConfiguration campusConfigurationMock;

    @Before
    public void setup() {
        ownerIdentity = JunitTestHelper.createAndPersistIdentityAsUser(ownerName);
        secondOwnerIdentity = JunitTestHelper.createAndPersistIdentityAsUser(ownerNameSecond);
        testIdentity = JunitTestHelper.createAndPersistIdentityAsUser(testUserName);
        secondTestIdentity = JunitTestHelper.createAndPersistIdentityAsUser(secondTestUserName);

        sourceRepositoryEntry = JunitTestHelper.deployDemoCourse(ownerIdentity); // TODO OLATng check if this is correct identity
        sourceResourceableId = sourceRepositoryEntry.getOlatResource().getResourceableId();

        final DB db = DBFactory.getInstance();
        sourceCourse = CourseFactory.loadCourse(sourceResourceableId);
        DBFactory.getInstance().closeSession();

        ObjectMother.setupCampusCourseGroupForTest(sourceCourse, TEST_COURSE_GROUP_A_NAME, businessGroupService);
        ObjectMother.setupCampusCourseGroupForTest(sourceCourse, TEST_COURSE_GROUP_B_NAME, businessGroupService);
        // DBFactory.getInstance().closeSession();

        campusConfigurationMock = mock(CampusConfiguration.class);
        when(campusConfigurationMock.getTemplateCourseResourcableId(null)).thenReturn(sourceResourceableId);
        when(campusConfigurationMock.getCourseGroupAName()).thenReturn(TEST_COURSE_GROUP_A_NAME);
        when(campusConfigurationMock.getCourseGroupBName()).thenReturn(TEST_COURSE_GROUP_B_NAME);
        when(campusConfigurationMock.getTemplateLanguage(null)).thenReturn("DE");
        // TODO OLATng
//        courseCreateCoordinator.campusConfiguration(campusConfigurationMock);
//        courseCreateCoordinator.campusCourseGroupSynchronizer.setCampusConfiguration(campusConfigurationMock);
//        CoursePublisher coursePublisherMock = mock(CoursePublisher.class);
//        courseCreateCoordinator.coursePublisher = coursePublisherMock;

    }

    @After
    public void tearDown() throws Exception {
        // TODO: Does not cleanup Demo-course because other Test which use Demo-Course too, will be have failures
        // remove demo course on file system
        // CourseFactory.deleteCourse(course);
        try {
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("tearDown failed: ", e);
        }
    }

    private CampusCourse createCampusCourseTestObject() {
        String semester = "Herbstsemester 2012";
        List<Identity> lecturers = new ArrayList<Identity>();
        lecturers.add(ownerIdentity);
        lecturers.add(secondOwnerIdentity);
        List<Identity> participants = new ArrayList<Identity>();
        participants.add(testIdentity);
        participants.add(secondTestIdentity);
        CampusCourseImportTO campusCourseImportData = new CampusCourseImportTO(TEST_TITLE_TEXT, semester, lecturers, participants, TEST_EVENT_DESCRIPTION_TEXT,
                TEST_RESOURCEABLE_ID, null);
        CampusCourse campusCourse = courseCreateCoordinator.createCampusCourse(null, campusCourseImportData, ownerIdentity);
        return campusCourse;
    }

    @Ignore // TODO OLATng
    @Test
    public void createCampusCourse() {
        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
        assertNotNull("campusCourse is null, could not create course", createdCampusCourseTestObject);
        assertNotNull("Missing repositoryEntry in CampusCourse return-object", createdCampusCourseTestObject.getRepositoryEntry());
        assertNotNull("Missing Course in CampusCourse return-object", createdCampusCourseTestObject.getCourse());
    }

    @Ignore // TODO OLATng
    @Test
    public void createCampusCourse_CheckAccess() {
        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
        assertTrue("CampusCourse Access must be 'BARG'", createdCampusCourseTestObject.getRepositoryEntry().getAccess() == RepositoryEntry.ACC_USERS_GUESTS);
    }

    @Ignore // TODO OLATng
    @Test
    public void createCampusCourse_CheckTitle() {
        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
        assertEquals("Wrong title in RepositoryEntry", TEST_TITLE_TEXT, createdCampusCourseTestObject.getRepositoryEntry().getDisplayname());
        assertEquals("Wrong title in Course", TEST_TITLE_TEXT, createdCampusCourseTestObject.getCourse().getCourseTitle());
    }

    @Ignore // TODO OLATng
    @Test
    public void createCampusCourse_CheckDescription() {
        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
        System.out.println("### TEST createdCampusCourseTestObject.getRepositoryEntry().getDescription()="
                + createdCampusCourseTestObject.getRepositoryEntry().getDescription());
        // Description Soll :
        // Campuskurs Herbstsemester 2012
        // Dozierende : Firstname Lastname, Firstname Lastname
        // Lehrveranstaltungsinhalt : Event description
        assertTextInDescription(createdCampusCourseTestObject, TEST_SEMESTER_TEXT);
        assertTextInDescription(createdCampusCourseTestObject, ownerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertTextInDescription(createdCampusCourseTestObject, secondOwnerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertTextInDescription(createdCampusCourseTestObject, ownerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertTextInDescription(createdCampusCourseTestObject, secondOwnerIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null));
        assertTextInDescription(createdCampusCourseTestObject, TEST_EVENT_DESCRIPTION_TEXT);
    }

    private void assertTextInDescription(CampusCourse createdCampusCourseTestObject, String attribute) {
        assertTrue("Missing attribute of test-identity (" + attribute + ") in RepositoryEntry-Description", createdCampusCourseTestObject.getRepositoryEntry()
                .getDescription().indexOf(attribute) != -1);
    }

    // TODO OLATng
//    @Test
//    public void createCampusCourse_CheckOwners() {
//        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
//        SecurityGroup ownerGroup = createdCampusCourseTestObject.getRepositoryEntry().getOwnerGroup();
//        assertTrue("Missing identity (" + ownerIdentity + ") in owner-group", baseSecurity.isIdentityInSecurityGroup(ownerIdentity, ownerGroup));
//        assertTrue("Missing identity (" + secondOwnerIdentity + ")in owner-group", baseSecurity.isIdentityInSecurityGroup(secondOwnerIdentity, ownerGroup));
//    }

    // TODO OLATng
//    @Test
//    public void createCampusCourse_CheckCourseGroup() {
//        CampusCourse createdCampusCourseTestObject = createCampusCourseTestObject();
//        BusinessGroup campusCourseGroup = CampusGroupHelper.lookupCampusGroup(createdCampusCourseTestObject.getCourse(), campusConfigurationMock.getCourseGroupAName());
//
//        assertTrue("Missing identity (" + ownerIdentity + ") in owner-group of course-group",
//                baseSecurity.isIdentityInSecurityGroup(ownerIdentity, campusCourseGroup.getOwnerGroup()));
//        assertTrue("Missing identity (" + secondOwnerIdentity + ")in owner-group of course-group",
//                baseSecurity.isIdentityInSecurityGroup(secondOwnerIdentity, campusCourseGroup.getOwnerGroup()));
//        assertTrue("Missing identity (" + testIdentity + ") in participant-group of course-group",
//                baseSecurity.isIdentityInSecurityGroup(testIdentity, campusCourseGroup.getPartipiciantGroup()));
//        assertTrue("Missing identity (" + secondTestIdentity + ")in participant-group of course-group",
//                baseSecurity.isIdentityInSecurityGroup(secondTestIdentity, campusCourseGroup.getPartipiciantGroup()));
//
//    }

}
