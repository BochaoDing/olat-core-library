package ch.uzh.campus.data;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Initial Date: Oct 27, 2014 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml" })
public class TextDaoTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;

 	@Autowired
    private TextDao textDao;

 	@Autowired	
 	private CourseDao courseDao;

	@Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    @Before
	public void setup() {
        // Insert some courses
        List<Course> courses = mockDataGeneratorProvider.get().getCourses();
        courseDao.save(courses);
        dbInstance.flush();
	}
	    
	@After
	public void after() {
	  	dbInstance.rollback();
	}

	@Test
	public void testGetTextsByCourseId_notFound() {
        addTextsToCourses();
        assertTrue(textDao.getTextsByCourseId(999L).isEmpty());
	}

	@Test
	public void testGetTextsByCourseId_foundTowTexts() {
	    addTextsToCourses();
        assertEquals(6, textDao.getTextsByCourseId(100L).size());
	}

    @Test
    public void testAddTextToTCourse() {
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(0, course.getTexts().size());
        assertTrue(textDao.getTextsByCourseId(100L).isEmpty());

        // Add a text
        TextCourseId textCourseId = mockDataGeneratorProvider.get().getTextCourseIds().get(0);
        textDao.addTextToCourse(textCourseId);

        // Check before flush
        assertEquals(1, course.getTexts().size());

        dbInstance.flush();
        dbInstance.clear();

        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getTexts().size());
        assertEquals(1, textDao.getTextsByCourseId(100L).size());

        // Add the same text a second time
        textDao.addTextToCourse(textCourseId);

        // Check before flush
        assertEquals(1, course.getTexts().size());

        dbInstance.flush();
        dbInstance.clear();

        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getTexts().size());
        assertEquals(1, textDao.getTextsByCourseId(100L).size());
    }

    @Test
    public void testAddTextToTCourse_NotExistingCourse() {
        TextCourseId textCourseId = new TextCourseId();
        textCourseId.setCourseId(999L);
        textCourseId.setType("Veranstaltungsinhalt");
        textCourseId.setLineSeq(1);
        textCourseId.setLine("- praktische Tätigkeiten im chemischen Labor");
        textCourseId.setModifiedDate(new Date());

        textDao.addTextToCourse(textCourseId);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, textDao.getTextsByCourseId(999L).size());
    }

    @Test
    public void testAddTextsToTCourse() {
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(0, course.getTexts().size());
        assertTrue(textDao.getTextsByCourseId(100L).isEmpty());

        addTextsToCourses();
        dbInstance.clear();

        course = courseDao.getCourseById(100L);
        assertEquals(6, course.getTexts().size());
        assertEquals(6, textDao.getTextsByCourseId(100L).size());
    }

    @Test
    public void testBlankGetContentsByCourseId() {
        addTextsToCourses();
        assertTrue(StringUtils.isBlank(textDao.getContentsByCourseId(999L)));
    }

    @Test
    public void testNotBlankGetContentsByCourseId() {
        addTextsToCourses();
        assertTrue(StringUtils.isNotBlank(textDao.getContentsByCourseId(100L)));
        assertEquals("- praktische Tätigkeiten im chemischen Labor<br>- Herstellung von Lösungen unterschiedlicher Konzentration<br>", textDao.getContentsByCourseId(100L));
    }

    @Test
    public void testBlankGetMaterialsByCourseId() {
        addTextsToCourses();
        assertTrue(StringUtils.isBlank(textDao.getMaterialsByCourseId(999L)));
    }

    @Test
    public void testNotBlankGetMaterialsByCourseId() {
        addTextsToCourses();
        assertTrue(StringUtils.isNotBlank(textDao.getMaterialsByCourseId(100L)));
        assertEquals("Versuchsanleitungen,<br>download von homepage (s. link)<br>", textDao.getMaterialsByCourseId(100L));
    }

    @Test
    public void testBlankGetInfosByCourseId() {
        addTextsToCourses();
        assertTrue(StringUtils.isBlank(textDao.getInfosByCourseId(999L)));
    }

    @Test
    public void testNotBlankGetInfosByCourseId() {
        addTextsToCourses();
        assertTrue(StringUtils.isNotBlank(textDao.getInfosByCourseId(100L)));
        assertEquals("Selbsttestfragen:<br>Zugriff über www.vetpharm.uzh.ch/cyberpharm<br>", textDao.getInfosByCourseId(100L));
    }

    @Test
    public void testDeleteAllTexts() {
        addTextsToCourses();
        assertEquals(6, textDao.getTextsByCourseId(100L).size());
        assertEquals(2, textDao.getTextsByCourseId(200L).size());
        Course course = courseDao.getCourseById(100L);
        assertEquals(6, course.getTexts().size());

        textDao.deleteAllTexts();

        // Check before flush
        assertEquals(0, course.getTexts().size());

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, textDao.getTextsByCourseId(100L).size());
        assertEquals(0, textDao.getTextsByCourseId(200L).size());
        course = courseDao.getCourseById(100L);
        assertEquals(0, course.getTexts().size());
    }

    @Test
    public void testDeleteAllTextsAsBulkDelete() {
        addTextsToCourses();
        assertEquals(6, textDao.getTextsByCourseId(100L).size());
        assertEquals(2, textDao.getTextsByCourseId(200L).size());
        Course course = courseDao.getCourseById(100L);
        assertEquals(6, course.getTexts().size());

        textDao.deleteAllTextsAsBulkDelete();

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, textDao.getTextsByCourseId(100L).size());
        assertEquals(0, textDao.getTextsByCourseId(200L).size());
        course = courseDao.getCourseById(100L);
        assertEquals(0, course.getTexts().size());
    }

    @Test
    public void testDeleteTextsByCourseId() {
        addTextsToCourses();
        assertEquals(6, textDao.getTextsByCourseId(100L).size());
        assertEquals(2, textDao.getTextsByCourseId(200L).size());
        Course course = courseDao.getCourseById(100L);
        assertEquals(6, course.getTexts().size());

        textDao.deleteTextsByCourseId(100L);

        // Check before flush
        assertEquals(0, course.getTexts().size());

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, textDao.getTextsByCourseId(100L).size());
        assertEquals(2, textDao.getTextsByCourseId(200L).size());
        course = courseDao.getCourseById(100L);
        assertEquals(0, course.getTexts().size());
    }

    @Test
    public void testDeleteTextsByCourseIds() {
        addTextsToCourses();
        assertEquals(6, textDao.getTextsByCourseId(100L).size());
        assertEquals(2, textDao.getTextsByCourseId(200L).size());
        Course course = courseDao.getCourseById(100L);
        assertEquals(6, course.getTexts().size());

        List<Long> courseIds = new ArrayList<>();
        courseIds.add(100L);
        courseIds.add(200L);

        textDao.deleteTextsByCourseIds(courseIds);

        // Check before flush
        assertEquals(0, course.getTexts().size());

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, textDao.getTextsByCourseId(100L).size());
        assertEquals(0, textDao.getTextsByCourseId(200L).size());
        course = courseDao.getCourseById(100L);
        assertEquals(0, course.getTexts().size());
    }

    private void addTextsToCourses() {
        List<TextCourseId> textCourseIds = mockDataGeneratorProvider.get().getTextCourseIds();
        textDao.addTextsToCourse(textCourseIds);
        dbInstance.flush();
    }
}
