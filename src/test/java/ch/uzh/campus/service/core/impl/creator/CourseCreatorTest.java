package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.service.CampusCourse;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Initial Date: 11.03.2016 <br>
 * 
 * @author lavinia
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class CourseCreatorTest extends OlatTestCase {

    @Autowired
    private CourseCreator courseCreator;

    private Long sourceResourceableId;
    private Identity ownerIdentity;
    private ICourse sourceCourse;
    private RepositoryEntry sourceRepositoryEntry;
    private String ownerName = "owner";

    @Before
    public void setup() {
        ownerIdentity = JunitTestHelper.createAndPersistIdentityAsUser(ownerName);

        sourceRepositoryEntry = JunitTestHelper.deployDemoCourse(ownerIdentity);
        sourceResourceableId = sourceRepositoryEntry.getOlatResource().getResourceableId();
        sourceCourse = CourseFactory.loadCourse(sourceResourceableId);
        DBFactory.getInstance().closeSession();
    }

    @Test
    public void createCampusCourseFromTemplate() {
        CampusCourse campusCourse = courseCreator.createCampusCourseFromTemplate(sourceResourceableId, ownerIdentity);
        assertNotNull(campusCourse);
        assertNotNull(campusCourse.getCourse());
        assertNotNull(campusCourse.getRepositoryEntry());
        assertTrue("Copy must have different resourcableId", !Objects.equals(sourceResourceableId, campusCourse.getCourse().getResourceableId()));

        ICourse copyCourse = CourseFactory.loadCourse(campusCourse.getCourse().getResourceableId());
        assertEquals("Course-title must be the same in the copy", sourceCourse.getCourseTitle(), copyCourse.getCourseTitle());
        assertEquals("Displayname of RepositoryEntry must be the same in the copy", sourceRepositoryEntry.getDisplayname(), campusCourse.getRepositoryEntry()
                .getDisplayname());
        assertEquals("Wrong initialAuthor in copy", ownerName, campusCourse.getRepositoryEntry().getInitialAuthor());
    }

    @Test
    public void test() {
        String vvzLink = "http://www.vorlesungen.uzh.ch/FS16/suche/e-50778939.details.html";
        String objectives = "&lt;p&gt;&lt;img alt=&quot;&quot; height=&quot;25&quot; src=&quot;/olat/raw/_noversion_/images/campuslogo.png&quot; width=&quot;136&quot; /&gt;&lt;/p&gt; &lt;p&gt;Willkommen!&lt;/p&gt; &lt;p&gt;Informationen zu den Lehrveranstaltungen finden Sie im &lt;a href=&quot;http://www.vorlesungen.uzh.ch/&quot; target=&quot;_blank&quot;&gt;Vorlesungsverzeichnis&lt;/a&gt;.&lt;/p&gt; &lt;p&gt;Dieser Kurs wurde als &lt;span style=&quot;color: rgb(0,0,0);&quot;&gt;Campuskurs&lt;/span&gt; erstellt.     Wenn Sie das zugeh&amp;ouml;rige Modul gebucht haben, sind Sie hier     automatisch eingeschrieben worden.&lt;/p&gt;";
        String newObjective = objectives.replaceFirst("http://www.vorlesungen.uzh.ch/", vvzLink);
        assertTrue(!newObjective.isEmpty());
        assertTrue(newObjective.contains(vvzLink));
    }
}

