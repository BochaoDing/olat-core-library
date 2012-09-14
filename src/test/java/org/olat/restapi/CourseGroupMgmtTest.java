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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/


package org.olat.restapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Description:<br>
 * Test the learning group management of a course
 * 
 * <P>
 * Initial Date:  6 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CourseGroupMgmtTest extends OlatJerseyTestCase {
	
	private static final OLog log = Tracing.createLoggerFor(CourseGroupMgmtTest.class);
	
	private Identity id1, id2;
	private BusinessGroup g1, g2;
	private BusinessGroup g3, g4;
	private RepositoryEntry courseRepoEntry;

	private RestConnection conn;
	
	@Autowired
	DB dbInstance;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BaseSecurity securityManager;
	
	
	/**
	 * Set up a course with learn group and group area
	 * @see org.olat.test.OlatJerseyTestCase#setUp()
	 */
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		conn = new RestConnection();
		//create a course with learn group
		
		id1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-c-g-1");
		id2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-c-g-2");
		JunitTestHelper.createAndPersistIdentityAsUser("rest-c-g-3");
		
		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl
		OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse",System.currentTimeMillis());
		OLATResource r =  rm.createOLATResourceInstance(resourceable);
		rm.saveOLATResource(r);
		courseRepoEntry =  JunitTestHelper.createAndPersistRepositoryEntry(r, false);
		dbInstance.saveObject(courseRepoEntry);
		dbInstance.commitAndCloseSession();
		
    // create groups without waiting list
    g1 = businessGroupService.createBusinessGroup(null, "rest-g1", null, 0, 10, false, false, courseRepoEntry);
    g2 = businessGroupService.createBusinessGroup(null, "rest-g2", null, 0, 10, false, false, courseRepoEntry);
    // members
    securityManager.addIdentityToSecurityGroup(id1, g2.getOwnerGroup());
    securityManager.addIdentityToSecurityGroup(id1, g1.getPartipiciantGroup());
    securityManager.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
    securityManager.addIdentityToSecurityGroup(id2, g2.getPartipiciantGroup());
    
    // groups
    g3 = businessGroupService.createBusinessGroup(null, "rest-g3", null, -1, -1, false, false, courseRepoEntry);
    g4 = businessGroupService.createBusinessGroup(null, "rest-g4", null, -1, -1, false, false, courseRepoEntry);
    // members
    securityManager.addIdentityToSecurityGroup(id1, g3.getPartipiciantGroup());
    securityManager.addIdentityToSecurityGroup(id2, g4.getPartipiciantGroup());
    
    dbInstance.commitAndCloseSession(); // simulate user clicks
	}
	
  @After
	public void tearDown() throws Exception {
		try {
			if(conn != null) {
				conn.shutdown();
			}
      DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
      throw e;
		}
	}
	
	@Test
	public void testGetCourseGroups() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));

		Long courseId = courseRepoEntry.getOlatResource().getResourceableId();
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseId + "/groups").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		
		List<GroupVO> vos = parseGroupArray(body);
		assertNotNull(vos);
		assertEquals(4, vos.size());//g1, g2, g3, g4
		
		List<Long> voKeys = new ArrayList<Long>(4);
		for(GroupVO vo:vos) {
			voKeys.add(vo.getKey());
		}
		assertTrue(voKeys.contains(g1.getKey()));
		assertTrue(voKeys.contains(g2.getKey()));
		assertTrue(voKeys.contains(g3.getKey()));
		assertTrue(voKeys.contains(g4.getKey()));
	}
	
	@Test
	public void testGetCourseGroup() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		Long courseId = courseRepoEntry.getOlatResource().getResourceableId();
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseId + "/groups/" + g1.getKey()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		GroupVO vo = conn.parse(response, GroupVO.class);
		assertNotNull(vo);
		assertEquals(g1.getKey(), vo.getKey());
	}
	
	@Test
	public void testPutCourseGroup() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		GroupVO vo = new GroupVO();
		vo.setName("hello");
		vo.setDescription("hello description");
		vo.setMinParticipants(new Integer(-1));
		vo.setMaxParticipants(new Integer(-1));
		
		Long courseId = courseRepoEntry.getOlatResource().getResourceableId();
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseId + "/groups").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		GroupVO responseVo = conn.parse(response, GroupVO.class);
		assertNotNull(responseVo);
		assertEquals(vo.getName(), responseVo.getName());

    BusinessGroup bg = businessGroupService.loadBusinessGroup(responseVo.getKey());
    assertNotNull(bg);
    assertEquals(bg.getKey(), responseVo.getKey());
    assertEquals(bg.getName(), vo.getName());
    assertEquals(bg.getDescription(), vo.getDescription());
    assertNull(bg.getMinParticipants());
    assertNull(bg.getMaxParticipants());
	}
	
	@Test
	public void testUpdateCourseGroup() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		GroupVO vo = new GroupVO();
		vo.setKey(g1.getKey());
		vo.setName("rest-g1-mod");
		vo.setDescription("rest-g1 description");
		vo.setMinParticipants(g1.getMinParticipants());
		vo.setMaxParticipants(g1.getMaxParticipants());
		vo.setType(g1.getType());
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseRepoEntry.getResourceableId() + "/groups/" + g1.getKey()).build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		HttpResponse response = conn.execute(method);
		
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		
    BusinessGroup bg = businessGroupService.loadBusinessGroup(g1.getKey());
    assertNotNull(bg);
    assertEquals(bg.getKey(), vo.getKey());
    assertEquals("rest-g1-mod", bg.getName());
    assertEquals("rest-g1 description", bg.getDescription());
	}
	
	@Test
	public void testDeleteCourseGroup() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseRepoEntry.getResourceableId() + "/groups/" + g1.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		assertEquals(200, response.getStatusLine().getStatusCode());
		
    BusinessGroup bg = businessGroupService.loadBusinessGroup(g1.getKey());
    assertNull(bg);
	}
	
	@Test
	public void testBasicSecurityDeleteCall() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-c-g-3", "A6B7C8"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseRepoEntry.getResourceableId() + "/groups/" + g2.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		assertEquals(401, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testBasicSecurityPutCall() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-c-g-3", "A6B7C8"));
		
		GroupVO vo = new GroupVO();
		vo.setName("hello dont put");
		vo.setDescription("hello description dont put");
		vo.setMinParticipants(new Integer(-1));
		vo.setMaxParticipants(new Integer(-1));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseRepoEntry.getResourceableId() + "/groups").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		assertEquals(401, response.getStatusLine().getStatusCode());
	}
	
	protected List<GroupVO> parseGroupArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<GroupVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
