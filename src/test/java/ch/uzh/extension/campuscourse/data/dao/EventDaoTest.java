package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import ch.uzh.extension.campuscourse.data.MockDataGenerator;
import ch.uzh.extension.campuscourse.data.entity.Course;
import ch.uzh.extension.campuscourse.model.CourseSemesterOrgId;
import ch.uzh.extension.campuscourse.model.EventCourseId;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import javax.persistence.EntityNotFoundException;
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
@Component
public class EventDaoTest extends CampusCourseTestCase {

    @Autowired
    private EventDao eventDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    @Before
    public void setup() throws CampusCourseException {
        // Insert some courses
        List<CourseSemesterOrgId> courseSemesterOrgIds = mockDataGeneratorProvider.get().getCourseSemesterOrgIds();
        courseDao.save(courseSemesterOrgIds);
        dbInstance.flush();
    }

    @Test
    public void testGetEventsByCourseId_notFound() {
        addEventsToCourses();
        assertTrue(eventDao.getEventsByCourseId(999L).isEmpty());
    }

    @Test
    public void testGetEventsByCourseId_foundTwoEvents() {
        addEventsToCourses();
        assertEquals(2, eventDao.getEventsByCourseId(100L).size());
    }
    
    @Test
    public void testAddEventToTCourse() {
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(0, course.getEvents().size());
        assertTrue(eventDao.getEventsByCourseId(100L).isEmpty());

        // Add an event
        EventCourseId eventCourseId = mockDataGeneratorProvider.get().getEventCourseIds().get(0);
        eventDao.addEventToCourse(eventCourseId);

        // Check before flush
        assertEquals(1, course.getEvents().size());

        dbInstance.flush();
        dbInstance.clear();

        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getEvents().size());
        assertEquals(1, eventDao.getEventsByCourseId(100L).size());

        // Add the same event a second time
        eventDao.addEventToCourse(eventCourseId);

        // Check before flush
        assertEquals(1, course.getEvents().size());

        dbInstance.flush();
        dbInstance.clear();

        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getEvents().size());
        assertEquals(1, eventDao.getEventsByCourseId(100L).size());
    }

    @Test
    public void testAddEventToTCourse_NotExistingCourse() {
        EventCourseId eventCourseId = new EventCourseId();
        eventCourseId.setCourseId(999L);
        eventCourseId.setDate(new Date());
        eventCourseId.setStart("10:00");
        eventCourseId.setEnd("13:00");
        eventCourseId.setDateOfImport(new Date());

        try {
            eventDao.addEventToCourse(eventCourseId);
            fail("Expected exception has not occurred.");
        } catch(EntityNotFoundException e) {
            // All good, that's exactly what we expect
        } catch(Exception e) {
            fail("Unexpected exception has occurred: " + e.getMessage());
        }

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, eventDao.getEventsByCourseId(999L).size());
    }

    @Test
    public void testAddEventsToTCourse() {
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(0, course.getEvents().size());
        assertTrue(eventDao.getEventsByCourseId(100L).isEmpty());

        addEventsToCourses();
        dbInstance.clear();

        course = courseDao.getCourseById(100L);
        assertEquals(2, course.getEvents().size());
        assertEquals(2, eventDao.getEventsByCourseId(100L).size());
    }

    @Test
    public void testDeleteAllEvents() {
        addEventsToCourses();
        assertEquals(2, eventDao.getEventsByCourseId(100L).size());
        assertEquals(2, eventDao.getEventsByCourseId(200L).size());
        Course course = courseDao.getCourseById(100L);
        assertEquals(2, course.getEvents().size());

        eventDao.deleteAllEvents();

        // Check before flush
        assertEquals(0, course.getEvents().size());

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, eventDao.getEventsByCourseId(100L).size());
        assertEquals(0, eventDao.getEventsByCourseId(200L).size());
        course = courseDao.getCourseById(100L);
        assertEquals(0, course.getEvents().size());
    }

    @Test
    public void testDeleteAllEventsAsBulkDelete() {
        addEventsToCourses();
        assertEquals(2, eventDao.getEventsByCourseId(100L).size());
        assertEquals(2, eventDao.getEventsByCourseId(200L).size());
        Course course = courseDao.getCourseById(100L);
        assertEquals(2, course.getEvents().size());

        eventDao.deleteAllEventsAsBulkDelete();

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, eventDao.getEventsByCourseId(100L).size());
        assertEquals(0, eventDao.getEventsByCourseId(200L).size());
        course = courseDao.getCourseById(100L);
        assertEquals(0, course.getEvents().size());
    }


    @Test
    public void testDeleteEventsByCourseId() {
        addEventsToCourses();
        assertEquals(2, eventDao.getEventsByCourseId(100L).size());
        assertEquals(2, eventDao.getEventsByCourseId(200L).size());
        Course course = courseDao.getCourseById(100L);
        assertEquals(2, course.getEvents().size());

        eventDao.deleteEventsByCourseId(100L);

        // Check before flush
        assertEquals(0, course.getEvents().size());

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, eventDao.getEventsByCourseId(100L).size());
        assertEquals(2, eventDao.getEventsByCourseId(200L).size());
        course = courseDao.getCourseById(100L);
        assertEquals(0, course.getEvents().size());
    }

    @Test
    public void testDeleteEventsByCourseIds() {
        addEventsToCourses();
        assertEquals(2, eventDao.getEventsByCourseId(100L).size());
        assertEquals(2, eventDao.getEventsByCourseId(200L).size());
        Course course = courseDao.getCourseById(100L);
        assertEquals(2, course.getEvents().size());

        List<Long> courseIds = new ArrayList<>();
        courseIds.add(100L);
        courseIds.add(200L);

        eventDao.deleteEventsByCourseIds(courseIds);

        // Check before flush
        assertEquals(0, course.getEvents().size());

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, eventDao.getEventsByCourseId(100L).size());
        assertEquals(0, eventDao.getEventsByCourseId(200L).size());
        course = courseDao.getCourseById(100L);
        assertEquals(0, course.getEvents().size());
    }

    @Test
    public void testDeleteEventsByCourseIdsAsBulkDelete() {
        addEventsToCourses();
        assertEquals(2, eventDao.getEventsByCourseId(100L).size());
        assertEquals(2, eventDao.getEventsByCourseId(200L).size());

        List<Long> courseIds = new ArrayList<>();
        courseIds.add(100L);
        courseIds.add(200L);

        eventDao.deleteEventsByCourseIdsAsBulkDelete(courseIds);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, eventDao.getEventsByCourseId(100L).size());
        assertEquals(0, eventDao.getEventsByCourseId(200L).size());
    }

    private void addEventsToCourses() {
        List<EventCourseId> eventCourseIds = mockDataGeneratorProvider.get().getEventCourseIds();
        eventDao.addEventsToCourse(eventCourseIds);
        dbInstance.flush();
    }

}
