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
import org.junit.runner.RunWith;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class EventDaoTest extends OlatTestCase {

    @Autowired
    private DB dbInstance;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private MockDataGenerator mockDataGenerator;

    private List<Course> courses;
    private List<Event> events;

    @Before
    public void setup() {
        // Insert some courses
        courses = mockDataGenerator.getCourses();
        courseDao.save(courses);
        dbInstance.flush();

        // Insert some events
        events = mockDataGenerator.getEvents();
        eventDao.save(events);
        dbInstance.flush();
    }

    @After
    public void after() {
        dbInstance.rollback();
    }

    @Test
    public void testGetEventsByCourseId_notFound() {
        assertTrue(eventDao.getEventsByCourseId(999L).isEmpty());
    }

    @Test
    public void testGetEventsByCourseId_foundTwoEvents() {
        assertEquals(eventDao.getEventsByCourseId(100L).size(), 2);
    }

    @Test
    public void testDeleteAllEvents() {
        assertEquals(eventDao.getEventsByCourseId(100L).size(), 2);
        assertEquals(eventDao.getEventsByCourseId(200L).size(), 2);

        eventDao.deleteAllEvents();
        dbInstance.flush();
        dbInstance.clear();

        assertEquals(eventDao.getEventsByCourseId(100L).size(), 0);
        assertEquals(eventDao.getEventsByCourseId(200L).size(), 0);
    }

    @Test
    public void testDeleteEventsByCourseId() {
        assertEquals(eventDao.getEventsByCourseId(100L).size(), 2);
        eventDao.deleteEventsByCourseId(100L);
        dbInstance.flush();
        dbInstance.clear();
        assertEquals(eventDao.getEventsByCourseId(100L).size(), 0);
        assertEquals(eventDao.getEventsByCourseId(200L).size(), 2);
        eventDao.deleteEventsByCourseId(200L);
        dbInstance.flush();
        dbInstance.clear();
        assertEquals(eventDao.getEventsByCourseId(200L).size(), 0);
    }

    @Test
    public void testDeleteEventsByCourseIds() {
        assertEquals(eventDao.getEventsByCourseId(100L).size(), 2);
        assertEquals(eventDao.getEventsByCourseId(200L).size(), 2);

        // TODO
//        Course course = courseDao.getCourseById(100L);
//        assertEquals(2, course.getTexts().size());

        List<Long> courseIds = new LinkedList<Long>();
        courseIds.add(100L);
        courseIds.add(200L);

        eventDao.deleteEventsByCourseIds(courseIds);
        dbInstance.flush();
        dbInstance.clear();

        // TODO
//        assertEquals(eventDao.getEventsByCourseId(100L).size(), 0);
//        assertEquals(eventDao.getEventsByCourseId(200L).size(), 0);
    }

}
