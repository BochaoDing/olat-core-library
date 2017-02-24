package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import ch.uzh.extension.campuscourse.CampusCourseTestDataGenerator;
import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.data.entity.Course;
import ch.uzh.extension.campuscourse.data.entity.Text;
import ch.uzh.extension.campuscourse.model.CourseIdTextTypeIdLineNumber;
import ch.uzh.extension.campuscourse.model.CourseSemesterOrgId;
import ch.uzh.extension.campuscourse.model.TextCourseId;
import ch.uzh.extension.campuscourse.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Initial Date: Oct 27, 2014 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
@Component
public class TextDaoTest extends CampusCourseTestCase {

	@Autowired
	private CampusCourseConfiguration campusCourseConfiguration;

 	@Autowired
    private TextDao textDao;

 	@Autowired	
 	private CourseDao courseDao;

	@Autowired
    private CampusCourseTestDataGenerator campusCourseTestDataGenerator;

    @Before
	public void setup() throws CampusCourseException {
        // Insert some courseOrgIds
        List<CourseSemesterOrgId> courseSemesterOrgIds = campusCourseTestDataGenerator.createCourseSemesterOrgIds();
        courseDao.save(courseSemesterOrgIds);
        dbInstance.flush();
	}

    @Test
    public void testSaveOrUpdate() {

		Course course = courseDao.getCourseById(100L);
		assertNotNull(course);
		assertTrue(course.getTexts().isEmpty());

		// Insert text
		TextCourseId textCourseId = new TextCourseId(100L,
				9999999,
				"Veranstaltungsinhalt",
				1,
				"- praktische Tätigkeiten im chemischen Labor",
				new Date());
		textDao.save(textCourseId);
		dbInstance.flush();

        textDao.saveOrUpdate(textCourseId);

        // Check before flush
		course = courseDao.getCourseById(100L);
        assertEquals(1, course.getTexts().size());

        dbInstance.flush();
        dbInstance.clear();

		Text text = textDao.getTextById(textCourseId.getCourseId(), textCourseId.getTextTypeId(), textCourseId.getLineNumber());
		assertNotNull(text);
		assertNotNull(text.getCourse());
		assertEquals(100L, text.getCourse().getId().longValue());
		assertNotNull(text.getTextType());
		assertEquals(9999999, text.getTextType().getId());
		assertEquals(1, text.getLineNumber());
		assertEquals("- praktische Tätigkeiten im chemischen Labor", text.getLine());


        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getTexts().size());
        assertEquals(1, textDao.getTextsByCourseId(100L).size());

        // Add the same text a second time
        textDao.saveOrUpdate(textCourseId);

        // Check before flush
        assertEquals(1, course.getTexts().size());

        dbInstance.flush();
        dbInstance.clear();

        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getTexts().size());
        assertEquals(1, textDao.getTextsByCourseId(100L).size());
    }

    @Test
    public void testSaveOrUpdate_NotExistingCourse() {
        TextCourseId textCourseId = new TextCourseId(999L,
				9999999,
				"Veranstaltungsinhalt",
				1,
				"- praktische Tätigkeiten im chemischen Labor",
				new Date());

        try {
            textDao.saveOrUpdate(textCourseId);
            fail("Expected exception has not occurred.");
        } catch(EntityNotFoundException e) {
            // All good, that's exactly what we expect
        } catch(Exception e) {
            fail("Unexpected exception has occurred: " + e.getMessage());
        }

        dbInstance.flush();
        dbInstance.clear();

        assertTrue(textDao.getTextsByCourseId(999L).isEmpty());
    }

	@Test
	public void testGetTextById() {
		// Insert text
		TextCourseId textCourseId = new TextCourseId(100L,
				9999999,
				"Veranstaltungsinhalt",
				1,
				"- praktische Tätigkeiten im chemischen Labor",
				new Date());
		textDao.save(textCourseId);
		dbInstance.flush();

		Text text = textDao.getTextById(textCourseId.getCourseId(), textCourseId.getTextTypeId(), textCourseId.getLineNumber());
		assertNotNull(text);
		assertNotNull(text.getCourse());
		assertEquals(100L, text.getCourse().getId().longValue());
		assertNotNull(text.getTextType());
		assertEquals(9999999, text.getTextType().getId());
	}

	@Test
	public void testGetTextsByCourseId() {
		insertTexts();
		assertEquals(6, textDao.getTextsByCourseId(100L).size());

		// Not existing course
		assertTrue(textDao.getTextsByCourseId(999L).isEmpty());
	}

    @Test
    public void testBlankGetContentsByCourseId() {
        insertTexts();
        assertTrue(StringUtils.isBlank(textDao.getContentsByCourseId(999L)));
    }

    @Test
    public void testNotBlankGetContentsByCourseId() {
        insertTexts();
        assertTrue(StringUtils.isNotBlank(textDao.getContentsByCourseId(100L)));
        assertEquals("- praktische Tätigkeiten im chemischen Labor<br>- Herstellung von Lösungen unterschiedlicher Konzentration<br>", textDao.getContentsByCourseId(100L));
    }

    @Test
    public void testBlankGetMaterialsByCourseId() {
        insertTexts();
        assertTrue(StringUtils.isBlank(textDao.getMaterialsByCourseId(999L)));
    }

    @Test
    public void testNotBlankGetMaterialsByCourseId() {
        insertTexts();
        assertTrue(StringUtils.isNotBlank(textDao.getMaterialsByCourseId(100L)));
        assertEquals("Versuchsanleitungen,<br>download von homepage (s. link)<br>", textDao.getMaterialsByCourseId(100L));
    }

    @Test
    public void testBlankGetInfosByCourseId() {
        insertTexts();
        assertTrue(StringUtils.isBlank(textDao.getInfosByCourseId(999L)));
    }

    @Test
    public void testNotBlankGetInfosByCourseId() {
        insertTexts();
        assertTrue(StringUtils.isNotBlank(textDao.getInfosByCourseId(100L)));
        assertEquals("Selbsttestfragen:<br>Zugriff über www.vetpharm.uzh.ch/cyberpharm<br>", textDao.getInfosByCourseId(100L));
    }

	@Test
	public void testGetAllNotUpdatedTextsOfCurrentImportProcess() {
		Course course1CurrentSemester = courseDao.getCourseById(100L);
		Course course2CurrentSemester = courseDao.getCourseById(200L);
		Course course3FormerSemester = courseDao.getCourseById(400L);

		Date referenceDateOfImport = new Date();

		// Insert text to course of current semester with date of import in the past (-> should be returned by method)
		Date dateOfImport1 = DateUtil.addHoursToDate(referenceDateOfImport, -1);
		TextCourseId textCourseId1 = new TextCourseId(course1CurrentSemester.getId(),
				9999999,
				"Veranstaltungsinhalt",
				1,
				"- praktische Tätigkeiten im chemischen Labor",
				dateOfImport1);
		textDao.save(textCourseId1);

		// Insert text to course of current semester with date of import in the future (-> should not be returned by method)
		Date dateOfImport2 = DateUtil.addHoursToDate(referenceDateOfImport, 1);
		TextCourseId textCourseId2 = new TextCourseId(course2CurrentSemester.getId(),
				9999999,
				"Veranstaltungsinhalt",
				1,
				"- praktische Tätigkeiten im chemischen Labor",
				dateOfImport2);
		textDao.save(textCourseId2);

		// Insert text to course from former semester with date of import in the past (-> should not be returned by method)
		Date dateOfImport3 = DateUtil.addHoursToDate(referenceDateOfImport, -1);
		TextCourseId textCourseId3 = new TextCourseId(course3FormerSemester.getId(),
				9999999,
				"Veranstaltungsinhalt",
				1,
				"- praktische Tätigkeiten im chemischen Labor",
				dateOfImport3);
		textDao.save(textCourseId3);

		dbInstance.flush();

		assertNotNull(textDao.getTextById(textCourseId1.getCourseId(), textCourseId1.getTextTypeId(), textCourseId1.getLineNumber()));
		assertNotNull(textDao.getTextById(textCourseId2.getCourseId(), textCourseId2.getTextTypeId(), textCourseId2.getLineNumber()));
		assertNotNull(textDao.getTextById(textCourseId3.getCourseId(), textCourseId3.getTextTypeId(), textCourseId3.getLineNumber()));

		List<CourseIdTextTypeIdLineNumber> courseIdTextTypeIdLineNumbers = textDao.getAllNotUpdatedTextsOfCurrentImportProcess(referenceDateOfImport, course1CurrentSemester.getSemester());

		assertEquals(1, courseIdTextTypeIdLineNumbers.size());
		CourseIdTextTypeIdLineNumber courseIdTextTypeIdLineNumberExpected = new CourseIdTextTypeIdLineNumber(textCourseId1.getCourseId(), textCourseId1.getTextTypeId(), textCourseId1.getLineNumber());
		assertTrue(courseIdTextTypeIdLineNumbers.contains(courseIdTextTypeIdLineNumberExpected));
	}

	@Test
	public void testDeleteStudentCourse() {
    	// Insert text
		TextCourseId textCourseId = new TextCourseId(100L,
				9999999,
				"Veranstaltungsinhalt",
				1,
				"- praktische Tätigkeiten im chemischen Labor",
				new Date());
    	textDao.save(textCourseId);
    	dbInstance.flush();

		Text text = textDao.getTextById(textCourseId.getCourseId(), textCourseId.getTextTypeId(), textCourseId.getLineNumber());
		assertNotNull(text);
		Course course = courseDao.getCourseById(100L);
    	assertEquals(1, course.getTexts().size());

		// Delete
		textDao.delete(text);

		// Check before flush
		assertTrue(course.getTexts().isEmpty());

		dbInstance.flush();
		dbInstance.clear();

		assertNull(textDao.getTextById(textCourseId.getCourseId(), textCourseId.getTextTypeId(), textCourseId.getLineNumber()));

		course = courseDao.getCourseById(100L);
		assertTrue(course.getTexts().isEmpty());
	}

    @Test
    public void testDeleteTextsByCourseIdsAsBulkDelete() {
        insertTexts();

        assertNotNull(textDao.getTextById(100L, 999999991, 1));
		assertNotNull(textDao.getTextById(100L, 999999991, 2));
		assertNotNull(textDao.getTextById(100L, 999999992, 1));
		assertNotNull(textDao.getTextById(100L, 999999992, 2));
		assertNotNull(textDao.getTextById(100L, 999999993, 1));
		assertNotNull(textDao.getTextById(100L, 999999993, 2));
		assertNotNull(textDao.getTextById(200L, 999999991, 1));
		assertNotNull(textDao.getTextById(200L, 999999991, 2));
		assertNotNull(textDao.getTextById(300L, 999999991, 1));
		assertNotNull(textDao.getTextById(300L, 999999991, 2));

        List<Long> courseIds = new ArrayList<>();
        courseIds.add(100L);
        courseIds.add(200L);

        textDao.deleteTextsByCourseIdsAsBulkDelete(courseIds);

        dbInstance.flush();
        dbInstance.clear();

		assertNull(textDao.getTextById(100L, 999999991, 1));
		assertNull(textDao.getTextById(100L, 999999991, 2));
		assertNull(textDao.getTextById(100L, 999999992, 1));
		assertNull(textDao.getTextById(100L, 999999992, 2));
		assertNull(textDao.getTextById(100L, 999999993, 1));
		assertNull(textDao.getTextById(100L, 999999993, 2));
		assertNull(textDao.getTextById(200L, 999999991, 1));
		assertNull(textDao.getTextById(200L, 999999991, 2));
		assertNotNull(textDao.getTextById(300L, 999999991, 1));
		assertNotNull(textDao.getTextById(300L, 999999991, 2));

		// Test with empty list
		assertEquals(0, textDao.deleteTextsByCourseIdsAsBulkDelete(new ArrayList<>()));
    }

	@Test
	public void testDeleteByCourseIdTextTypeIdLineNumberAsBulkDelete() {
    	insertTexts();

		assertNotNull(textDao.getTextById(100L, 999999991, 1));
		assertNotNull(textDao.getTextById(100L, 999999991, 2));
		assertNotNull(textDao.getTextById(100L, 999999992, 1));
		assertNotNull(textDao.getTextById(100L, 999999992, 2));
		assertNotNull(textDao.getTextById(100L, 999999993, 1));
		assertNotNull(textDao.getTextById(100L, 999999993, 2));
		assertNotNull(textDao.getTextById(200L, 999999991, 1));
		assertNotNull(textDao.getTextById(200L, 999999991, 2));
		assertNotNull(textDao.getTextById(300L, 999999991, 1));
		assertNotNull(textDao.getTextById(300L, 999999991, 2));

		List<CourseIdTextTypeIdLineNumber> courseIdTextTypeIdLineNumbersToBeDeleted = new ArrayList<>();
		courseIdTextTypeIdLineNumbersToBeDeleted.add(new CourseIdTextTypeIdLineNumber(100L, 999999991, 1));
		courseIdTextTypeIdLineNumbersToBeDeleted.add(new CourseIdTextTypeIdLineNumber(100L, 999999992, 2));
		courseIdTextTypeIdLineNumbersToBeDeleted.add(new CourseIdTextTypeIdLineNumber(300L, 999999991, 2));

		int numberOfDeletedEntities = textDao.deleteByCourseIdTextTypeIdLineNumbersAsBulkDelete(courseIdTextTypeIdLineNumbersToBeDeleted);

		dbInstance.flush();
		dbInstance.clear();

		assertEquals(3, numberOfDeletedEntities);

		assertNull(textDao.getTextById(100L, 999999991, 1));
		assertNotNull(textDao.getTextById(100L, 999999991, 2));
		assertNotNull(textDao.getTextById(100L, 999999992, 1));
		assertNull(textDao.getTextById(100L, 999999992, 2));
		assertNotNull(textDao.getTextById(100L, 999999993, 1));
		assertNotNull(textDao.getTextById(100L, 999999993, 2));
		assertNotNull(textDao.getTextById(200L, 999999991, 1));
		assertNotNull(textDao.getTextById(200L, 999999991, 2));
		assertNotNull(textDao.getTextById(300L, 999999991, 1));
		assertNull(textDao.getTextById(300L, 999999991, 2));
	}

	@Test
	public void testDeleteAllTextsOfNotContinuedCoursesTooFarInThePastAsBulkDelete() {
		Course course1 = courseDao.getCourseById(100L);
		Course course2 = courseDao.getCourseById(200L);
		Course course3 = courseDao.getCourseById(300L);
		Course course4 = courseDao.getCourseById(400L);

		// Make course 400 to be the parent course of 300
		// -> course 3 has a parent course, course 4 has a child course
		courseDao.saveParentCourseIdAndDateOfOlatCourseCreation(300L, 400L);

		Date referenceDateOfImport = new GregorianCalendar(1800, Calendar.JANUARY, 1).getTime();

		// Insert text to course with date too far in the past (-> should be deleted)
		Date dateOfImport1 = DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1);
		course1.setDateOfLatestImport(dateOfImport1);
		TextCourseId textCourseId1 = new TextCourseId(course1.getId(),
				9999999,
				"Veranstaltungsinhalt",
				1,
				"- praktische Tätigkeiten im chemischen Labor",
				dateOfImport1);
		textDao.save(textCourseId1);

		// Insert text to course with date not too far in the past (-> should not be deleted)
		Date dateOfImport2 = DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() + 1);
		course2.setDateOfLatestImport(dateOfImport2);
		TextCourseId textCourseId2 = new TextCourseId(course2.getId(),
				9999999,
				"Veranstaltungsinhalt",
				1,
				"- praktische Tätigkeiten im chemischen Labor",
				dateOfImport2);
		textDao.save(textCourseId2);

		// Insert text to course with date too far in the past (-> should be deleted)
		Date dateOfImport3 = DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1);
		course3.setDateOfLatestImport(dateOfImport3);
		TextCourseId textCourseId3 = new TextCourseId(course3.getId(),
				9999999,
				"Veranstaltungsinhalt",
				1,
				"- praktische Tätigkeiten im chemischen Labor",
				dateOfImport3);
		textDao.save(textCourseId3);

		// Insert text to course with date too far in the past (-> should not be deleted, because course has a child course)
		Date dateOfImport4 = DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1);
		course4.setDateOfLatestImport(dateOfImport4);
		TextCourseId textCourseId4 = new TextCourseId(course4.getId(),
				9999999,
				"Veranstaltungsinhalt",
				1,
				"- praktische Tätigkeiten im chemischen Labor",
				dateOfImport4);
		textDao.save(textCourseId4);

		dbInstance.flush();

		assertNotNull(textDao.getTextById(textCourseId1.getCourseId(), textCourseId1.getTextTypeId(), textCourseId1.getLineNumber()));
		assertNotNull(textDao.getTextById(textCourseId2.getCourseId(), textCourseId2.getTextTypeId(), textCourseId2.getLineNumber()));
		assertNotNull(textDao.getTextById(textCourseId3.getCourseId(), textCourseId3.getTextTypeId(), textCourseId3.getLineNumber()));
		assertNotNull(textDao.getTextById(textCourseId4.getCourseId(), textCourseId4.getTextTypeId(), textCourseId4.getLineNumber()));

		textDao.deleteAllTextsOfNotContinuedCoursesTooFarInThePastAsBulkDelete(referenceDateOfImport);

		dbInstance.flush();
		dbInstance.clear();

		assertNull(textDao.getTextById(textCourseId1.getCourseId(), textCourseId1.getTextTypeId(), textCourseId1.getLineNumber()));
		assertNotNull(textDao.getTextById(textCourseId2.getCourseId(), textCourseId2.getTextTypeId(), textCourseId2.getLineNumber()));
		assertNull(textDao.getTextById(textCourseId3.getCourseId(), textCourseId3.getTextTypeId(), textCourseId3.getLineNumber()));
		assertNotNull(textDao.getTextById(textCourseId4.getCourseId(), textCourseId4.getTextTypeId(), textCourseId4.getLineNumber()));

		// Remove parent course of course 3 (-> course 4 doesn't have a child course any more and should also be deleted)
		course3 = courseDao.getCourseById(300L);
		course3.removeParentCourse();
		dbInstance.flush();

		textDao.deleteAllTextsOfNotContinuedCoursesTooFarInThePastAsBulkDelete(referenceDateOfImport);

		dbInstance.flush();
		dbInstance.clear();

		assertNull(textDao.getTextById(textCourseId4.getCourseId(), textCourseId4.getTextTypeId(), textCourseId4.getLineNumber()));
	}

	private void insertTexts() {
        List<TextCourseId> textCourseIds = campusCourseTestDataGenerator.createTextCourseIds();
        textDao.save(textCourseIds);
        dbInstance.flush();
    }
}
