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
package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Initial Date: Oct 27, 2014 <br>
 *
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class EventDaoTest extends OlatTestCase {

    @Autowired
    private DB dbInstance;

    @Autowired
    private EventDao eventDao;

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
        dbInstance.getCurrentEntityManager().clear();

        course = courseDao.getCourseById(100L);
        assertEquals(1, course.getEvents().size());
        assertEquals(1, eventDao.getEventsByCourseId(100L).size());
    }

    @Test
    public void testAddEventsToTCourse() {
        Course course = courseDao.getCourseById(100L);
        assertNotNull(course);
        assertEquals(0, course.getEvents().size());
        assertTrue(eventDao.getEventsByCourseId(100L).isEmpty());

        addEventsToCourses();
        dbInstance.getCurrentEntityManager().clear();

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

    private void addEventsToCourses() {
        List<EventCourseId> eventCourseIds = mockDataGeneratorProvider.get().getEventCourseIds();
        eventDao.addEventsToCourse(eventCourseIds);
        dbInstance.flush();
    }

}
