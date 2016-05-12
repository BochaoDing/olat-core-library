package ch.uzh.campus.data;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;

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

	private List<TextCourseId> textCourseIds;

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
        assertEquals(textDao.getTextsByCourseId(100L).size(), 6);
	}

    @Test
    public void testAddTextToTCourse() {
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(0, course.getTexts().size());
        assertTrue(textDao.getTextsByCourseId(100L).isEmpty());

        // Add a text
        Text text = mockDataGeneratorProvider.get().getTextCourseIds().get(0);
        textDao.addTextToCourse(text, 100L);
        dbInstance.flush();
        dbInstance.getCurrentEntityManager().clear();

        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getTexts().size());
        assertEquals(textDao.getTextsByCourseId(100L).size(), 1);
    }

    @Test
    public void testAddTextsToTCourse() {
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(0, course.getTexts().size());
        assertTrue(textDao.getTextsByCourseId(100L).isEmpty());

        addTextsToCourses();
        dbInstance.getCurrentEntityManager().clear();

        course = courseDao.getCourseById(100L);
        assertEquals(6, course.getTexts().size());
        assertEquals(textDao.getTextsByCourseId(100L).size(), 6);
    }

	    /*
	    @Test
	    public void testBlankGetContentsByCourseId() {
	        assertTrue(StringUtils.isBlank(textDao.getContentsByCourseId(999L)));
	    }

	    @Test
	    public void testNotBlankGetContentsByCourseId() {
	        assertTrue(StringUtils.isNotBlank(textDao.getContentsByCourseId(100L)));
	        assertEquals(textDao.getContentsByCourseId(100L),
	                "- praktische Tätigkeiten im chemischen Labor<br>- Herstellung von Lösungen unterschiedlicher Konzentration<br>");
	    }

	    @Test
	    public void testBlankGetMaterialsByCourseId() {
	        assertTrue(StringUtils.isBlank(textDao.getMaterialsByCourseId(999L)));
	    }

	    @Test
	    public void testNotBlankGetMaterialsByCourseId() {
	        assertTrue(StringUtils.isNotBlank(textDao.getMaterialsByCourseId(100L)));
	        assertEquals(textDao.getMaterialsByCourseId(100L), "Versuchsanleitungen,<br>download von homepage (s. link)<br>");
	    }

	    @Test
	    public void testBlankGetInfosByCourseId() {
	        assertTrue(StringUtils.isBlank(textDao.getInfosByCourseId(999L)));
	    }

	    @Test
	    public void testNotBlankGetInfosByCourseId() {
	        assertTrue(StringUtils.isNotBlank(textDao.getInfosByCourseId(100L)));
	        assertEquals(textDao.getInfosByCourseId(100L), "Selbsttestfragen:<br>Zugriff über www.vetpharm.uzh.ch/cyberpharm<br>");
	    }

	    @Test
	    public void testDeleteAllTexts() {
	        assertEquals(textDao.getTextsByCourseId(100L).size(), 6);
	        assertEquals(textDao.getTextsByCourseId(200L).size(), 2);

	        textDao.deleteAllTexts();

	        assertEquals(textDao.getTextsByCourseId(100L).size(), 0);
	        assertEquals(textDao.getTextsByCourseId(200L).size(), 0);
	    }

	    @Test
	    public void testDeleteTextsByCourseId() {
	        assertEquals(textDao.getTextsByCourseId(100L).size(), 6);
	        assertEquals(textDao.getTextsByCourseId(200L).size(), 2);

	        textDao.deleteTextsByCourseId(100L);

	        assertEquals(textDao.getTextsByCourseId(100L).size(), 0);
	        assertEquals(textDao.getTextsByCourseId(200L).size(), 2);
	    }

	    @Test
	    public void testDeleteTextsByCourseIds() {
	        assertEquals(textDao.getTextsByCourseId(100L).size(), 6);
	        assertEquals(textDao.getTextsByCourseId(200L).size(), 2);

	        List<Long> courseIds = new LinkedList<Long>();
	        courseIds.add(100L);
	        courseIds.add(200L);

	        textDao.deleteTextsByCourseIds(courseIds);

	        assertEquals(textDao.getTextsByCourseId(100L).size(), 0);
	        assertEquals(textDao.getTextsByCourseId(200L).size(), 0);
	    }
*/
    private void addTextsToCourses() {
        textCourseIds = mockDataGeneratorProvider.get().getTextCourseIds();
        textDao.addTextsToCourse(textCourseIds);
        dbInstance.flush();
    }
}
