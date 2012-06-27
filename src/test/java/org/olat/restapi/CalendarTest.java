/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.restapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.restapi.CalendarVO;
import org.olat.commons.calendar.restapi.EventVO;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.repository.course.CoursesWebService;
import org.olat.restapi.support.vo.CourseConfigVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CalendarTest extends OlatJerseyTestCase {

	private static ICourse course1, course2;
	private static Identity id1, id2;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private DB dbInstance;
	
	@Before
	public void startup() {
		if(id1 == null) {
			id1 = JunitTestHelper.createAndPersistIdentityAsUser("cal-1-" + UUID.randomUUID().toString());
		}
		if(id2 == null) {
			id2 = JunitTestHelper.createAndPersistIdentityAsUser("cal-2-" + UUID.randomUUID().toString());
		}
		
		if(course1 == null) {
			//create a course with a calendar
			CourseConfigVO config = new CourseConfigVO();
			course1 = CoursesWebService.createEmptyCourse(id1, "Cal course", "Cal course", config);

			dbInstance.commit();
			ICourse course = CourseFactory.openCourseEditSession(course1.getResourceableId());
			CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
			courseConfig.setCalendarEnabled(true);
			CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
			CourseFactory.closeCourseEditSession(course.getResourceableId(),true);

			dbInstance.commit();

			CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
			KalendarRenderWrapper calendarWrapper = calendarManager.getCourseCalendar(course);

			Calendar cal = Calendar.getInstance();
			Date begin = cal.getTime();
			cal.add(Calendar.HOUR_OF_DAY, 1);
			Date end = cal.getTime();
			KalendarEvent event = new KalendarEvent(UUID.randomUUID().toString(), "Unit test", begin, end);
			calendarWrapper.getKalendar().addEvent(event);
			
			RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(course1, false);
			securityManager.addIdentityToSecurityGroup(id1, entry.getParticipantGroup());
			
			dbInstance.commit();
		}
		
		if(course2 == null) {
			//create a course with a calendar
			CourseConfigVO config = new CourseConfigVO();
			course2 = CoursesWebService.createEmptyCourse(id2, "Cal course - 2", "Cal course - 2", config);

			dbInstance.commit();
			ICourse course = CourseFactory.openCourseEditSession(course2.getResourceableId());
			CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
			courseConfig.setCalendarEnabled(true);
			CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
			CourseFactory.closeCourseEditSession(course.getResourceableId(),true);

			dbInstance.commit();
		}
	}
	
	@After
	public void tearDown() {
		DBFactory.getInstance().commitAndCloseSession();
	}

	@Test
	public void testGetCalendars() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1.getName(), "A6B7C8"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("calendars").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertNotNull(vos);
		assertEquals(2, vos.size());//course1 + personal
		
		conn.shutdown();
	}
	
	@Test
	public void testHijackCalendars() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1.getName(), "A6B7C8"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString()).path("calendars").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(401, response.getStatusLine().getStatusCode());
		
		conn.shutdown();
	}
	
	@Test
	public void testGetEvents() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1.getName(), "A6B7C8"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("calendars").path("events").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<EventVO> vos = parseEventArray(response);
		assertNotNull(vos);
		assertEquals(1, vos.size());//Root-1
		
		conn.shutdown();
	}
	
	@Test
	public void testGetCalendarEvents() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1.getName(), "A6B7C8"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("calendars").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertNotNull(vos);
		assertEquals(2, vos.size());//course1 + personal
		CalendarVO calendar = getCourseCalendar(vos);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString())
				.path("calendars").path(calendar.getId()).path("events").build();
		HttpGet eventMethod = conn.createGet(eventUri, MediaType.APPLICATION_JSON, true);
		HttpResponse eventResponse = conn.execute(eventMethod);
		assertEquals(200, eventResponse.getStatusLine().getStatusCode());
		List<EventVO> events = parseEventArray(eventResponse);
		assertNotNull(events);
		assertEquals(1, events.size());//Root-1
		
		conn.shutdown();
	}
	
	@Test
	public void testOutputGetCalendarEvents() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1.getName(), "A6B7C8"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("calendars").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertNotNull(vos);
		assertEquals(2, vos.size());//Root-1
		CalendarVO calendar = getCourseCalendar(vos);

		//get events and output as JSON
		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString())
				.path("calendars").path(calendar.getId()).path("events").build();
		HttpGet eventMethod = conn.createGet(eventUri, MediaType.APPLICATION_JSON, true);
		HttpResponse eventResponse = conn.execute(eventMethod);
		assertEquals(200, eventResponse.getStatusLine().getStatusCode());
		String outputJson = EntityUtils.toString(eventResponse.getEntity());
		System.out.println("*** JSON");
		System.out.println(outputJson);

		//get events and output as XML
		URI eventXmlUri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString())
				.path("calendars").path(calendar.getId()).path("events").build();
		HttpGet eventXmlMethod = conn.createGet(eventXmlUri, MediaType.APPLICATION_XML, true);
		HttpResponse eventXmlResponse = conn.execute(eventXmlMethod);
		assertEquals(200, eventXmlResponse.getStatusLine().getStatusCode());
		String outputXml = EntityUtils.toString(eventXmlResponse.getEntity());
		System.out.println("*** XML");
		System.out.println(outputXml);

		conn.shutdown();
	}
	
	@Test
	public void testPutCalendarEvents() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id2.getName(), "A6B7C8"));
		
		URI calUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString()).path("calendars").build();
		HttpGet calMethod = conn.createGet(calUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(calMethod);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertNotNull(vos);
		assertEquals(2, vos.size());
		CalendarVO calendar = getCourseCalendar(vos);
		
		//create an event
		EventVO event = new EventVO();
		Calendar cal = Calendar.getInstance();
		event.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setEnd(cal.getTime());
		String subject = UUID.randomUUID().toString();
		event.setSubject(subject);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString())
				.path("calendars").path(calendar.getId()).path("events").build();
		HttpPut putEventMethod = conn.createPut(eventUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(putEventMethod, event);
		HttpResponse putEventResponse = conn.execute(putEventMethod);
		assertEquals(200, putEventResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(putEventResponse.getEntity());
		
		
		//check if the event is saved
		CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
		KalendarRenderWrapper calendarWrapper = calendarManager.getCourseCalendar(course2);
		Collection<KalendarEvent> savedEvents = calendarWrapper.getKalendar().getEvents();
		
		boolean found = false;
		for(KalendarEvent savedEvent:savedEvents) {
			if(subject.equals(savedEvent.getSubject())) {
				found = true;
			}
		}
		Assert.assertTrue(found);

		conn.shutdown();
	}
	
	@Test
	public void testPostCalendarEvents() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id2.getName(), "A6B7C8"));
		
		URI calUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString()).path("calendars").build();
		HttpGet calMethod = conn.createGet(calUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(calMethod);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertTrue(vos != null && !vos.isEmpty());
		CalendarVO calendar = getCourseCalendar(vos);
		
		//create an event
		EventVO event = new EventVO();
		Calendar cal = Calendar.getInstance();
		event.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setEnd(cal.getTime());
		String subject = UUID.randomUUID().toString();
		event.setSubject(subject);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString())
				.path("calendars").path(calendar.getId()).path("events").build();
		HttpPost postEventMethod = conn.createPost(eventUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(postEventMethod, event);
		HttpResponse postEventResponse = conn.execute(postEventMethod);
		assertEquals(200, postEventResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(postEventResponse.getEntity());
		
		
		//check if the event is saved
		CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
		KalendarRenderWrapper calendarWrapper = calendarManager.getCourseCalendar(course2);
		Collection<KalendarEvent> savedEvents = calendarWrapper.getKalendar().getEvents();
		
		boolean found = false;
		for(KalendarEvent savedEvent:savedEvents) {
			if(subject.equals(savedEvent.getSubject())) {
				found = true;
			}
		}
		Assert.assertTrue(found);

		conn.shutdown();
	}
	
	@Test
	public void testPutPersonalCalendarEvents() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id2.getName(), "A6B7C8"));
		
		URI calUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString()).path("calendars").build();
		HttpGet calMethod = conn.createGet(calUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(calMethod);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertNotNull(vos);
		assertEquals(2, vos.size());
		CalendarVO calendar = getUserCalendar(vos);
		
		//create an event
		EventVO event = new EventVO();
		Calendar cal = Calendar.getInstance();
		event.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setEnd(cal.getTime());
		String subject = UUID.randomUUID().toString();
		event.setSubject(subject);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString())
				.path("calendars").path(calendar.getId()).path("events").build();
		HttpPut putEventMethod = conn.createPut(eventUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(putEventMethod, event);
		HttpResponse putEventResponse = conn.execute(putEventMethod);
		assertEquals(200, putEventResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(putEventResponse.getEntity());
		
		
		//check if the event is saved
		CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
		KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(id2);
		Collection<KalendarEvent> savedEvents = calendarWrapper.getKalendar().getEvents();
		
		boolean found = false;
		for(KalendarEvent savedEvent:savedEvents) {
			if(subject.equals(savedEvent.getSubject())) {
				found = true;
			}
		}
		Assert.assertTrue(found);

		conn.shutdown();
	}
	
	protected CalendarVO getCourseCalendar(List<CalendarVO> vos) {
		for(CalendarVO vo:vos) {
			if(vo.getId().startsWith("course")) {
				return vo;
			}
		}
		return null;
	}
	
	protected CalendarVO getUserCalendar(List<CalendarVO> vos) {
		for(CalendarVO vo:vos) {
			if(vo.getId().startsWith("user")) {
				return vo;
			}
		}
		return null;
	}
	
	protected List<CalendarVO> parseArray(HttpResponse response) {
		try {
			InputStream body = response.getEntity().getContent();
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<CalendarVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<EventVO> parseEventArray(HttpResponse response) {
		try {
			InputStream body = response.getEntity().getContent();
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<EventVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
