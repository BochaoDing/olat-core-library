package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import ch.uzh.extension.campuscourse.CampusCourseTestDataGenerator;
import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.data.entity.Course;
import ch.uzh.extension.campuscourse.data.entity.Event;
import ch.uzh.extension.campuscourse.model.CourseIdDateStartEnd;
import ch.uzh.extension.campuscourse.model.CourseSemesterOrgId;
import ch.uzh.extension.campuscourse.model.EventCourseId;
import ch.uzh.extension.campuscourse.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.sql.Time;
import java.text.ParseException;
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
public class EventDaoTest extends CampusCourseTestCase {

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private CampusCourseTestDataGenerator campusCourseTestDataGenerator;

    @Before
    public void setup() throws CampusCourseException {
        // Insert some courses
        List<CourseSemesterOrgId> courseSemesterOrgIds = campusCourseTestDataGenerator.createCourseSemesterOrgIds();
        courseDao.save(courseSemesterOrgIds);
        dbInstance.flush();
    }
    
    @Test
    public void testSaveOrUpdate() throws ParseException {
		Course course = courseDao.getCourseById(100L);
		assertNotNull(course);
		assertTrue(course.getEvents().isEmpty());

		// Insert event
		EventCourseId eventCourseId = new EventCourseId(100L,
				new GregorianCalendar(2099, Calendar.FEBRUARY, 23).getTime(),
				"13:30:00",
				"15:20:00",
				new Date());
		eventDao.save(eventCourseId);
		dbInstance.flush();

		eventDao.saveOrUpdate(eventCourseId);

		// Check before flush
		course = courseDao.getCourseById(100L);
		assertEquals(1, course.getEvents().size());

		dbInstance.flush();
		dbInstance.clear();

		Event event = eventDao.getEventById(eventCourseId.getCourseId(), eventCourseId.getDate(), Time.valueOf(eventCourseId.getStart()), Time.valueOf(eventCourseId.getEnd()));
		assertNotNull(event);
		assertNotNull(event.getCourse());
		assertEquals(100L, event.getCourse().getId().longValue());
		assertNotNull(event.getDate());
		assertEquals("2099-02-23", event.getDate().toString());
		assertNotNull(event.getStart());
		assertEquals("13:30:00", event.getStart().toString());
		assertNotNull(event.getEnd());
		assertEquals("15:20:00", event.getEnd().toString());

		course = courseDao.getCourseById(100L);
		assertEquals(1, course.getEvents().size());
		assertEquals(1, eventDao.getEventsByCourseId(100L).size());

		// Add the same event a second time
		eventDao.saveOrUpdate(eventCourseId);

		// Check before flush
		assertEquals(1, course.getEvents().size());

		dbInstance.flush();
		dbInstance.clear();

		course = courseDao.getCourseById(100L);
		assertEquals(1, course.getEvents().size());
		assertEquals(1, eventDao.getEventsByCourseId(100L).size());
    }

    @Test
    public void testSaveOrUpdate_NotExistingCourse() {
		EventCourseId eventCourseId = new EventCourseId(999L,
				new GregorianCalendar(2099, Calendar.FEBRUARY, 23).getTime(),
				"13:30:00",
				"15:20:00",
				new Date());

		try {
			eventDao.save(eventCourseId);
			fail("Expected exception has not occurred.");
		} catch(EntityNotFoundException e) {
			// All good, that's exactly what we expect
		} catch(Exception e) {
			fail("Unexpected exception has occurred: " + e.getMessage());
		}

		dbInstance.flush();
		dbInstance.clear();

		assertTrue(eventDao.getEventsByCourseId(999L).isEmpty());
    }

	@Test
	public void testGetById() throws ParseException {
		EventCourseId eventCourseId = new EventCourseId(100L,
				new GregorianCalendar(2099, Calendar.FEBRUARY, 23).getTime(),
				"13:30:00",
				"15:20:00",
				new Date());
		eventDao.save(eventCourseId);
		dbInstance.flush();

		Event event = eventDao.getEventById(eventCourseId.getCourseId(), eventCourseId.getDate(), Time.valueOf(eventCourseId.getStart()), Time.valueOf(eventCourseId.getEnd()));
		assertNotNull(event);
		assertNotNull(event.getCourse());
		assertEquals(100L, event.getCourse().getId().longValue());
		assertNotNull(event.getDate());
		assertEquals(new GregorianCalendar(2099, Calendar.FEBRUARY, 23).getTime(), event.getDate());
		assertNotNull(event.getStart());
		assertEquals("13:30:00", event.getStart().toString());
		assertNotNull(event.getEnd());
		assertEquals("15:20:00", event.getEnd().toString());
	}

	@Test
	public void testGetEventsByCourseId() throws ParseException {
		insertEvents();
		assertEquals(2, eventDao.getEventsByCourseId(100L).size());

		// Not existing course
		assertTrue(eventDao.getEventsByCourseId(999L).isEmpty());
	}

	@Test
	public void testGetAllNotUpdatedEventsOfCurrentImportProcess() throws ParseException {
		Course course1CurrentSemester = courseDao.getCourseById(100L);
		Course course2CurrentSemester = courseDao.getCourseById(200L);
		Course course3FormerSemester = courseDao.getCourseById(400L);

		Date referenceDateOfImport = new Date();

		// Insert event to course of current semester with date of import in the past (-> should be returned by method)
		Date dateOfImport1 = DateUtil.addHoursToDate(referenceDateOfImport, -1);
		EventCourseId eventCourseId1 = new EventCourseId(course1CurrentSemester.getId(),
				new GregorianCalendar(2099, Calendar.FEBRUARY, 23).getTime(),
				"13:30:00",
				"15:20:00",
				dateOfImport1);
		eventDao.save(eventCourseId1);

		// Insert event to course of current semester with date of import in the future (-> should not be returned by method)
		Date dateOfImport2 = DateUtil.addHoursToDate(referenceDateOfImport, 1);
		EventCourseId eventCourseId2 = new EventCourseId(course2CurrentSemester.getId(),
				new GregorianCalendar(2099, Calendar.FEBRUARY, 23).getTime(),
				"13:30:00",
				"15:20:00",
				dateOfImport2);
		eventDao.save(eventCourseId2);

		// Insert event to course from former semester with date of import in the past (-> should not be returned by method)
		Date dateOfImport3 = DateUtil.addHoursToDate(referenceDateOfImport, -1);
		EventCourseId eventCourseId3 = new EventCourseId(course3FormerSemester.getId(),
				new GregorianCalendar(2099, Calendar.FEBRUARY, 23).getTime(),
				"13:30:00",
				"15:20:00",
				dateOfImport3);
		eventDao.save(eventCourseId3);

		dbInstance.flush();

		assertNotNull(eventDao.getEventById(eventCourseId1.getCourseId(), eventCourseId1.getDate(), Time.valueOf(eventCourseId1.getStart()), Time.valueOf(eventCourseId1.getEnd())));
		assertNotNull(eventDao.getEventById(eventCourseId2.getCourseId(), eventCourseId2.getDate(), Time.valueOf(eventCourseId2.getStart()), Time.valueOf(eventCourseId2.getEnd())));
		assertNotNull(eventDao.getEventById(eventCourseId3.getCourseId(), eventCourseId3.getDate(), Time.valueOf(eventCourseId3.getStart()), Time.valueOf(eventCourseId3.getEnd())));

		List<CourseIdDateStartEnd> courseIdDateStartEnds = eventDao.getAllNotUpdatedEventsOfCurrentImportProcess(referenceDateOfImport, course1CurrentSemester.getSemester());

		assertEquals(1, courseIdDateStartEnds.size());
		CourseIdDateStartEnd courseIdEventTypeIdLineNumberExpected = new CourseIdDateStartEnd(
				eventCourseId1.getCourseId(),
				new GregorianCalendar(2099, Calendar.FEBRUARY, 23).getTime(),
				Time.valueOf("13:30:00"),
				Time.valueOf("15:20:00"));
		assertTrue(courseIdDateStartEnds.contains(courseIdEventTypeIdLineNumberExpected));
	}

	@Test
	public void testDeleteStudentCourse() throws ParseException {
		// Insert event
		EventCourseId eventCourseId = new EventCourseId(100L,
				new GregorianCalendar(2099, Calendar.FEBRUARY, 23).getTime(),
				"13:30:00",
				"15:20:00",
				new Date());
		eventDao.save(eventCourseId);
		dbInstance.flush();

		Event event = eventDao.getEventById(eventCourseId.getCourseId(), eventCourseId.getDate(), Time.valueOf(eventCourseId.getStart()), Time.valueOf(eventCourseId.getEnd()));
		assertNotNull(event);
		Course course = courseDao.getCourseById(100L);
		assertEquals(1, course.getEvents().size());

		// Delete
		eventDao.delete(event);

		// Check before flush
		assertTrue(course.getEvents().isEmpty());

		dbInstance.flush();
		dbInstance.clear();

		assertNull(eventDao.getEventById(eventCourseId.getCourseId(), eventCourseId.getDate(), Time.valueOf(eventCourseId.getStart()), Time.valueOf(eventCourseId.getEnd())));

		course = courseDao.getCourseById(100L);
		assertTrue(course.getEvents().isEmpty());
	}

	@Test
	public void testDeleteEventsByCourseIdsAsBulkDelete() throws ParseException {
		insertEvents();

		assertNotNull(eventDao.getEventById(100L, new GregorianCalendar(2014, Calendar.OCTOBER, 13).getTime(), Time.valueOf("10:00:00"), Time.valueOf("11:30:00")));
		assertNotNull(eventDao.getEventById(100L, new GregorianCalendar(2014, Calendar.OCTOBER, 13).getTime(), Time.valueOf("16:00:00"), Time.valueOf("17:30:00")));
		assertNotNull(eventDao.getEventById(200L, new GregorianCalendar(2014, Calendar.OCTOBER, 16).getTime(), Time.valueOf("09:00:00"), Time.valueOf("10:30:00")));
		assertNotNull(eventDao.getEventById(200L, new GregorianCalendar(2014, Calendar.OCTOBER, 16).getTime(), Time.valueOf("14:00:00"), Time.valueOf("15:30:00")));
		assertNotNull(eventDao.getEventById(300L, new GregorianCalendar(2014, Calendar.OCTOBER, 19).getTime(), Time.valueOf("19:00:00"), Time.valueOf("20:30:00")));
		assertNotNull(eventDao.getEventById(300L, new GregorianCalendar(2014, Calendar.OCTOBER, 19).getTime(), Time.valueOf("21:00:00"), Time.valueOf("23:30:00")));

		List<Long> courseIds = new ArrayList<>();
		courseIds.add(100L);
		courseIds.add(200L);

		eventDao.deleteEventsByCourseIdsAsBulkDelete(courseIds);

		dbInstance.flush();
		dbInstance.clear();

		assertNull(eventDao.getEventById(100L, new GregorianCalendar(2014, Calendar.OCTOBER, 13).getTime(), Time.valueOf("10:00:00"), Time.valueOf("11:30:00")));
		assertNull(eventDao.getEventById(100L, new GregorianCalendar(2014, Calendar.OCTOBER, 13).getTime(), Time.valueOf("16:00:00"), Time.valueOf("17:30:00")));
		assertNull(eventDao.getEventById(200L, new GregorianCalendar(2014, Calendar.OCTOBER, 16).getTime(), Time.valueOf("09:00:00"), Time.valueOf("10:30:00")));
		assertNull(eventDao.getEventById(200L, new GregorianCalendar(2014, Calendar.OCTOBER, 16).getTime(), Time.valueOf("14:00:00"), Time.valueOf("15:30:00")));
		assertNotNull(eventDao.getEventById(300L, new GregorianCalendar(2014, Calendar.OCTOBER, 19).getTime(), Time.valueOf("19:00:00"), Time.valueOf("20:30:00")));
		assertNotNull(eventDao.getEventById(300L, new GregorianCalendar(2014, Calendar.OCTOBER, 19).getTime(), Time.valueOf("21:00:00"), Time.valueOf("23:30:00")));

		// Test with empty list
		assertEquals(0, eventDao.deleteEventsByCourseIdsAsBulkDelete(new ArrayList<>()));
	}

	@Test
	public void testDeleteByCourseIdEventTypeIdLineNumberAsBulkDelete() throws ParseException {
		insertEvents();

		assertNotNull(eventDao.getEventById(100L, new GregorianCalendar(2014, Calendar.OCTOBER, 13).getTime(), Time.valueOf("10:00:00"), Time.valueOf("11:30:00")));
		assertNotNull(eventDao.getEventById(100L, new GregorianCalendar(2014, Calendar.OCTOBER, 13).getTime(), Time.valueOf("16:00:00"), Time.valueOf("17:30:00")));
		assertNotNull(eventDao.getEventById(200L, new GregorianCalendar(2014, Calendar.OCTOBER, 16).getTime(), Time.valueOf("09:00:00"), Time.valueOf("10:30:00")));
		assertNotNull(eventDao.getEventById(200L, new GregorianCalendar(2014, Calendar.OCTOBER, 16).getTime(), Time.valueOf("14:00:00"), Time.valueOf("15:30:00")));
		assertNotNull(eventDao.getEventById(300L, new GregorianCalendar(2014, Calendar.OCTOBER, 19).getTime(), Time.valueOf("19:00:00"), Time.valueOf("20:30:00")));
		assertNotNull(eventDao.getEventById(300L, new GregorianCalendar(2014, Calendar.OCTOBER, 19).getTime(), Time.valueOf("21:00:00"), Time.valueOf("23:30:00")));

		List<CourseIdDateStartEnd> courseIdDateStartEnds = new ArrayList<>();
		courseIdDateStartEnds.add(new CourseIdDateStartEnd(100L, new GregorianCalendar(2014, Calendar.OCTOBER, 13).getTime(), Time.valueOf("10:00:00"), Time.valueOf("11:30:00")));
		courseIdDateStartEnds.add(new CourseIdDateStartEnd(200L, new GregorianCalendar(2014, Calendar.OCTOBER, 16).getTime(), Time.valueOf("14:00:00"), Time.valueOf("15:30:00")));
		courseIdDateStartEnds.add(new CourseIdDateStartEnd(300L, new GregorianCalendar(2014, Calendar.OCTOBER, 19).getTime(), Time.valueOf("21:00:00"), Time.valueOf("23:30:00")));

		int numberOfDeletedEntities = eventDao.deleteByCourseIdDateStartEndsAsBulkDelete(courseIdDateStartEnds);

		dbInstance.flush();
		dbInstance.clear();

		assertEquals(3, numberOfDeletedEntities);

		assertNull(eventDao.getEventById(100L, new GregorianCalendar(2014, Calendar.OCTOBER, 13).getTime(), Time.valueOf("10:00:00"), Time.valueOf("11:30:00")));
		assertNotNull(eventDao.getEventById(100L, new GregorianCalendar(2014, Calendar.OCTOBER, 13).getTime(), Time.valueOf("16:00:00"), Time.valueOf("17:30:00")));
		assertNotNull(eventDao.getEventById(200L, new GregorianCalendar(2014, Calendar.OCTOBER, 16).getTime(), Time.valueOf("09:00:00"), Time.valueOf("10:30:00")));
		assertNull(eventDao.getEventById(200L, new GregorianCalendar(2014, Calendar.OCTOBER, 16).getTime(), Time.valueOf("14:00:00"), Time.valueOf("15:30:00")));
		assertNotNull(eventDao.getEventById(300L, new GregorianCalendar(2014, Calendar.OCTOBER, 19).getTime(), Time.valueOf("19:00:00"), Time.valueOf("20:30:00")));
		assertNull(eventDao.getEventById(300L, new GregorianCalendar(2014, Calendar.OCTOBER, 19).getTime(), Time.valueOf("21:00:00"), Time.valueOf("23:30:00")));
	}

	@Test
	public void testDeleteAllEventsOfNotContinuedCoursesTooFarInThePastAsBulkDelete() {
		Course course1 = courseDao.getCourseById(100L);
		Course course2 = courseDao.getCourseById(200L);
		Course course3 = courseDao.getCourseById(300L);
		Course course4 = courseDao.getCourseById(400L);

		// Make course 400 to be the parent course of 300
		// -> course 3 has a parent course, course 4 has a child course
		courseDao.saveParentCourseIdAndDateOfOlatCourseCreation(300L, 400L);

		Date referenceDateOfImport = new GregorianCalendar(1800, Calendar.JANUARY, 1).getTime();

		// Insert event to course with date too far in the past (-> should be deleted)
		Date dateOfImport1 = DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1);
		course1.setDateOfLatestImport(dateOfImport1);
		EventCourseId eventCourseId1 = new EventCourseId(course1.getId(),
				new GregorianCalendar(1800, Calendar.FEBRUARY, 23).getTime(),
				"13:30:00",
				"15:20:00",
				dateOfImport1);
		eventDao.save(eventCourseId1);

		// Insert event to course with date not too far in the past (-> should not be deleted)
		Date dateOfImport2 = DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() + 1);
		course2.setDateOfLatestImport(dateOfImport2);
		EventCourseId eventCourseId2 = new EventCourseId(course2.getId(),
				new GregorianCalendar(1800, Calendar.FEBRUARY, 23).getTime(),
				"13:30:00",
				"15:20:00",
				dateOfImport2);
		eventDao.save(eventCourseId2);

		// Insert event to course with date too far in the past (-> should be deleted)
		Date dateOfImport3 = DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1);
		course3.setDateOfLatestImport(dateOfImport3);
		EventCourseId eventCourseId3 = new EventCourseId(course3.getId(),
				new GregorianCalendar(1800, Calendar.FEBRUARY, 23).getTime(),
				"13:30:00",
				"15:20:00",
				dateOfImport3);
		eventDao.save(eventCourseId3);

		// Insert event to course with date too far in the past (-> should not be deleted, because course has a child course)
		Date dateOfImport4 = DateUtil.addYearsToDate(referenceDateOfImport, -campusCourseConfiguration.getMaxYearsToKeepCkData() - 1);
		course4.setDateOfLatestImport(dateOfImport4);
		EventCourseId eventCourseId4 = new EventCourseId(course4.getId(),
				new GregorianCalendar(1800, Calendar.FEBRUARY, 23).getTime(),
				"13:30:00",
				"15:20:00",
				dateOfImport4);
		eventDao.save(eventCourseId4);

		dbInstance.flush();

		assertNotNull(eventDao.getEventById(eventCourseId1.getCourseId(), eventCourseId1.getDate(), Time.valueOf(eventCourseId1.getStart()), Time.valueOf(eventCourseId1.getEnd())));
		assertNotNull(eventDao.getEventById(eventCourseId2.getCourseId(), eventCourseId2.getDate(), Time.valueOf(eventCourseId2.getStart()), Time.valueOf(eventCourseId2.getEnd())));
		assertNotNull(eventDao.getEventById(eventCourseId3.getCourseId(), eventCourseId3.getDate(), Time.valueOf(eventCourseId3.getStart()), Time.valueOf(eventCourseId3.getEnd())));
		assertNotNull(eventDao.getEventById(eventCourseId4.getCourseId(), eventCourseId4.getDate(), Time.valueOf(eventCourseId4.getStart()), Time.valueOf(eventCourseId4.getEnd())));

		eventDao.deleteAllEventsOfNotContinuedCoursesTooFarInThePastAsBulkDelete(referenceDateOfImport);

		dbInstance.flush();
		dbInstance.clear();

		assertNull(eventDao.getEventById(eventCourseId1.getCourseId(), eventCourseId1.getDate(), Time.valueOf(eventCourseId1.getStart()), Time.valueOf(eventCourseId1.getEnd())));
		assertNotNull(eventDao.getEventById(eventCourseId2.getCourseId(), eventCourseId2.getDate(), Time.valueOf(eventCourseId2.getStart()), Time.valueOf(eventCourseId2.getEnd())));
		assertNull(eventDao.getEventById(eventCourseId3.getCourseId(), eventCourseId3.getDate(), Time.valueOf(eventCourseId3.getStart()), Time.valueOf(eventCourseId3.getEnd())));
		assertNotNull(eventDao.getEventById(eventCourseId4.getCourseId(), eventCourseId4.getDate(), Time.valueOf(eventCourseId4.getStart()), Time.valueOf(eventCourseId4.getEnd())));

		// Remove parent course of course 3 (-> course 4 doesn't have a child course any more and should also be deleted)
		course3 = courseDao.getCourseById(300L);
		course3.removeParentCourse();
		dbInstance.flush();

		eventDao.deleteAllEventsOfNotContinuedCoursesTooFarInThePastAsBulkDelete(referenceDateOfImport);

		dbInstance.flush();
		dbInstance.clear();

		assertNull(eventDao.getEventById(eventCourseId4.getCourseId(), eventCourseId4.getDate(), Time.valueOf(eventCourseId4.getStart()), Time.valueOf(eventCourseId4.getEnd())));
	}

    private void insertEvents() throws ParseException {
        List<EventCourseId> eventCourseIds = campusCourseTestDataGenerator.createEventCourseIds();
        eventDao.save(eventCourseIds);
        dbInstance.flush();
    }

}
