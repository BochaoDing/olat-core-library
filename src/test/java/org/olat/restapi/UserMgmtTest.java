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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatNamedContainerImpl;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManager;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumManager;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.restapi.ForumVO;
import org.olat.modules.fo.restapi.ForumVOes;
import org.olat.modules.fo.restapi.MessageVOes;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.vo.ErrorVO;
import org.olat.restapi.support.vo.FileVO;
import org.olat.restapi.support.vo.FolderVO;
import org.olat.restapi.support.vo.FolderVOes;
import org.olat.restapi.support.vo.GroupInfoVOes;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.restapi.support.vo.GroupVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.restapi.UserVO;

/**
 * 
 * Description:<br>
 * Test the <code>UserWebservice</code>
 * 
 * <P>
 * Initial Date:  15 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class UserMgmtTest extends OlatJerseyTestCase {
	
	private OLog log = Tracing.createLoggerFor(UserMgmtTest.class);
	
	private static Identity owner1, id1, id2, id3;
	private static BusinessGroup g1, g2, g3, g4;
	
	private static ICourse demoCourse;
	private static FOCourseNode demoForumNode;
	private static BCCourseNode demoBCCourseNode;
	
	private static boolean setuped = false;
	
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		if(setuped) return;
		
		//create identities
		owner1 = JunitTestHelper.createAndPersistIdentityAsUser("user-rest-zero");
		id1 = JunitTestHelper.createAndPersistIdentityAsUser("user-rest-one");
		id2 = JunitTestHelper.createAndPersistIdentityAsUser("user-rest-two");
		DBFactory.getInstance().intermediateCommit();
		id2.getUser().setProperty("telMobile", "39847592");
		id2.getUser().setProperty("gender", "female");
		id2.getUser().setProperty("birthDay", "20091212");
		DBFactory.getInstance().updateObject(id2.getUser());
		DBFactory.getInstance().intermediateCommit();
		
		id3 = JunitTestHelper.createAndPersistIdentityAsUser("user-rest-three");
		OlatRootFolderImpl id3HomeFolder = new OlatRootFolderImpl(FolderConfig.getUserHome(id3.getName()), null);
		VFSContainer id3PublicFolder = (VFSContainer)id3HomeFolder.resolve("public");
		if(id3PublicFolder == null) {
			id3PublicFolder = id3HomeFolder.createChildContainer("public");
		}
		VFSItem portrait = id3PublicFolder.resolve("portrait.jpg");
		if(portrait == null) {
			URL portraitUrl = CoursesElementsTest.class.getResource("portrait.jpg");
			File ioPortrait = new File(portraitUrl.toURI());
			FileUtils.copyFileToDirectory(ioPortrait, ((LocalImpl)id3PublicFolder).getBasefile(), false);
		}

		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl
		OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse",System.currentTimeMillis());
		OLATResource course =  rm.createOLATResourceInstance(resourceable);
		DBFactory.getInstance().saveObject(course);
		DBFactory.getInstance().intermediateCommit();
		
		//create learn group

    BGContextManager cm = BGContextManagerImpl.getInstance();
    BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
    BaseSecurity secm = BaseSecurityManager.getInstance();
		
    // 1) context one: learning groups
    BGContext c1 = cm.createAndAddBGContextToResource("c1name-learn", course, BusinessGroup.TYPE_LEARNINGROUP, owner1, true);
    // create groups without waiting list
    g1 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "user-rest-g1", null, new Integer(0), new Integer(10), false, false, c1);
    g2 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "user-rest-g2", null, new Integer(0), new Integer(10), false, false, c1);
    // members g1
    secm.addIdentityToSecurityGroup(id1, g1.getOwnerGroup());
    secm.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
    // members g2
    secm.addIdentityToSecurityGroup(id2, g2.getOwnerGroup());
    secm.addIdentityToSecurityGroup(id1, g2.getPartipiciantGroup());

    // 2) context two: right groups
    BGContext c2 = cm.createAndAddBGContextToResource("c2name-area", course, BusinessGroup.TYPE_RIGHTGROUP, owner1, true);
    // groups
    g3 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "user-rest-g3", null, null, null, null/* enableWaitinglist */, null/* enableAutoCloseRanks */, c2);
    g4 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "user-rest-g4", null, null, null, null/* enableWaitinglist */, null/* enableAutoCloseRanks */, c2);
    // members
    secm.addIdentityToSecurityGroup(id1, g3.getPartipiciantGroup());
    secm.addIdentityToSecurityGroup(id2, g4.getPartipiciantGroup());
		DBFactory.getInstance().closeSession();
		
		//add some collaboration tools
		CollaborationTools g1CTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(g1);
		g1CTSMngr.setToolEnabled(CollaborationTools.TOOL_FORUM, true);
		Forum g1Forum = g1CTSMngr.getForum();//create the forum
		Message m1 = ForumManager.getInstance().createMessage();
		m1.setTitle("Thread-1");
		m1.setBody("Body of Thread-1");
		ForumManager.getInstance().addTopMessage(id1, g1Forum, m1);
		
		DBFactory.getInstance().commitAndCloseSession();
		
		//add some folder tool
		CollaborationTools g2CTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(g2);
		g2CTSMngr.setToolEnabled(CollaborationTools.TOOL_FOLDER, true);
		OlatRootFolderImpl g2Folder = new OlatRootFolderImpl(g2CTSMngr.getFolderRelPath(), null);
		g2Folder.getBasefile().mkdirs();
		VFSItem groupPortrait = g2Folder.resolve("portrait.jpg");
		if(groupPortrait == null) {
			URL portraitUrl = UserMgmtTest.class.getResource("portrait.jpg");
			File ioPortrait = new File(portraitUrl.toURI());
			FileUtils.copyFileToDirectory(ioPortrait, g2Folder.getBasefile(), false);
		}
		
		DBFactory.getInstance().commitAndCloseSession();
		
		//prepare some courses
		RepositoryEntry entry = JunitTestHelper.deployDemoCourse();
		if(entry.getParticipantGroup() == null) {
			assertTrue(false);
		} else if (!secm.isIdentityInSecurityGroup(id1, entry.getParticipantGroup())){
	    secm.addIdentityToSecurityGroup(id1, entry.getParticipantGroup());
		}
		
		demoCourse = CourseFactory.loadCourse(entry.getOlatResource());
		TreeVisitor visitor = new TreeVisitor(new Visitor() {
			@Override
			public void visit(INode node) {
				if(node instanceof FOCourseNode) {
					if(demoForumNode == null) {
						demoForumNode = (FOCourseNode)node;
						Forum courseForum = demoForumNode.loadOrCreateForum(demoCourse.getCourseEnvironment());
						Message m1 = ForumManager.getInstance().createMessage();
						m1.setTitle("Thread-1");
						m1.setBody("Body of Thread-1");
						ForumManager.getInstance().addTopMessage(id1, courseForum, m1);
					}	
				} else if (node instanceof BCCourseNode) {
					if(demoBCCourseNode == null) {
						demoBCCourseNode = (BCCourseNode)node;
						OlatNamedContainerImpl container = BCCourseNode.getNodeFolderContainer(demoBCCourseNode, demoCourse.getCourseEnvironment());
						VFSItem example = container.resolve("singlepage.html");
						if(example == null) {
							try {
								InputStream htmlUrl = UserMgmtTest.class.getResourceAsStream("singlepage.html");
								VFSLeaf htmlLeaf = container.createChildLeaf("singlepage.html");
								IOUtils.copy(htmlUrl, htmlLeaf.getOutputStream(false));
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}, demoCourse.getRunStructure().getRootNode(), false);
		visitor.visitAll();

		DBFactory.getInstance().commitAndCloseSession();
		setuped = true;
	}
	
  @After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		try {
      DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
      e.printStackTrace();
      throw e;
		}
	}

	@Test
	public void testGetUsers() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		HttpMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		List<UserVO> vos = parseUserArray(body);
		method.releaseConnection();
		List<Identity> identities = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);

		assertNotNull(vos);
		assertFalse(vos.isEmpty());
		assertEquals(vos.size(), identities.size());
	}
	
	@Test
	public void testFindUsersByLogin() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		GetMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
		method.setQueryString(new NameValuePair[]{
				new NameValuePair("login","administrator"),
				new NameValuePair("authProvider","OLAT")
		});
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		List<UserVO> vos = parseUserArray(body);
		method.releaseConnection();
		String[] authProviders = new String[]{"OLAT"};
		List<Identity> identities = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch("administrator", null, true, null, null, authProviders, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);

		assertNotNull(vos);
		assertFalse(vos.isEmpty());
		assertEquals(vos.size(), identities.size());
		boolean onlyLikeAdmin = true;
		for(UserVO vo:vos) {
			if(!vo.getLogin().startsWith("administrator")) {
				onlyLikeAdmin = false;
			}
		}
		assertTrue(onlyLikeAdmin);
	}
	
	@Test
	public void testFindUsersByProperty() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		GetMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
		method.setQueryString(new NameValuePair[]{
				new NameValuePair("telMobile","39847592"),
				new NameValuePair("gender","Female"),
				new NameValuePair("birthDay", "12/12/2009")
		});
		method.addRequestHeader("Accept-Language", "en");
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		List<UserVO> vos = parseUserArray(body);
		method.releaseConnection();
	
		assertNotNull(vos);
		assertFalse(vos.isEmpty());
	}
	
	@Test
	public void testFindAdminByAuth() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		GetMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
		method.setQueryString(new NameValuePair[]{
				new NameValuePair("authUsername","administrator"),
				new NameValuePair("authProvider","OLAT")
		});
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		List<UserVO> vos = parseUserArray(body);
	
		assertNotNull(vos);
		assertFalse(vos.isEmpty());
		assertEquals(1, vos.size());
		assertEquals("administrator",vos.get(0).getLogin());
	}
	
	@Test
	public void testGetUser() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		HttpMethod method = createGet("/users/" + id1.getKey(), MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		UserVO vo = parse(body, UserVO.class);

		assertNotNull(vo);
		assertEquals(vo.getKey(), id1.getKey());
		assertEquals(vo.getLogin(), id1.getName());
		//are the properties there?
		assertFalse(vo.getProperties().isEmpty());
	}
	
	@Test
	public void testGetUserNotAdmin() throws IOException {
		HttpClient c = loginWithCookie("rest-one", "A6B7C8");
		
		HttpMethod method = createGet("/users/" + id2.getKey(), MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		UserVO vo = parse(body, UserVO.class);

		assertNotNull(vo);
		assertEquals(vo.getKey(), id2.getKey());
		assertEquals(vo.getLogin(), id2.getName());
		//no properties for security reason
		assertTrue(vo.getProperties().isEmpty());
	}
		
	/**
	 * Only print out the raw body of the response
	 * @throws IOException
	 */
	@Test	
	public void testGetRawJsonUser() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		HttpMethod method = createGet("/users/" + id1.getKey(), MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String bodyJson = method.getResponseBodyAsString();
		System.out.println("User");
		System.out.println(bodyJson);
		System.out.println("User");
	}
		
	/**
	 * Only print out the raw body of the response
	 * @throws IOException
	 */
	@Test	
	public void testGetRawXmlUser() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");	
		HttpMethod method = createGet("/users/" + id1.getKey(), MediaType.APPLICATION_XML, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String bodyXml = method.getResponseBodyAsString();
		System.out.println("User");
		System.out.println(bodyXml);
		System.out.println("User");
	}
	
	@Test
	public void testCreateUser() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		UserVO vo = new UserVO();
		String username = UUID.randomUUID().toString();
		vo.setLogin(username);
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail(username + "@frentix.com");
		vo.putProperty("telOffice", "39847592");
		vo.putProperty("telPrivate", "39847592");
		vo.putProperty("telMobile", "39847592");
		vo.putProperty("gender", "Female");//male or female
		vo.putProperty("birthDay", "12/12/2009");

		String stringuifiedAuth = stringuified(vo);
		PutMethod method = createPut("/users", MediaType.APPLICATION_JSON, true);
    RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
    method.setRequestEntity(entity);
		method.addRequestHeader("Accept-Language", "en");
		
		int code = c.executeMethod(method);
		assertTrue(code == 200 || code == 201);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		UserVO savedVo = parse(body, UserVO.class);
		
		Identity savedIdent = BaseSecurityManager.getInstance().findIdentityByName(username);

		assertNotNull(savedVo);
		assertNotNull(savedIdent);
		assertEquals(savedVo.getKey(), savedIdent.getKey());
		assertEquals(savedVo.getLogin(), savedIdent.getName());
		assertEquals("Female", savedIdent.getUser().getProperty("gender", Locale.ENGLISH));
		assertEquals("39847592", savedIdent.getUser().getProperty("telPrivate", Locale.ENGLISH));
		assertEquals("12/12/09", savedIdent.getUser().getProperty("birthDay", Locale.ENGLISH));
	}
	
	/**
	 * Test machine format for gender and date
	 */
	@Test
	public void testCreateUser2() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		UserVO vo = new UserVO();
		String username = UUID.randomUUID().toString();
		vo.setLogin(username);
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail(username + "@frentix.com");
		vo.putProperty("telOffice", "39847592");
		vo.putProperty("telPrivate", "39847592");
		vo.putProperty("telMobile", "39847592");
		vo.putProperty("gender", "female");//male or female
		vo.putProperty("birthDay", "20091212");

		String stringuifiedAuth = stringuified(vo);
		PutMethod method = createPut("/users", MediaType.APPLICATION_JSON, true);
    RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
    method.setRequestEntity(entity);
		method.addRequestHeader("Accept-Language", "en");
		
		int code = c.executeMethod(method);
		assertTrue(code == 200 || code == 201);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		UserVO savedVo = parse(body, UserVO.class);
		
		Identity savedIdent = BaseSecurityManager.getInstance().findIdentityByName(username);

		assertNotNull(savedVo);
		assertNotNull(savedIdent);
		assertEquals(savedVo.getKey(), savedIdent.getKey());
		assertEquals(savedVo.getLogin(), savedIdent.getName());
		assertEquals("Female", savedIdent.getUser().getProperty("gender", Locale.ENGLISH));
		assertEquals("39847592", savedIdent.getUser().getProperty("telPrivate", Locale.ENGLISH));
		assertEquals("12/12/09", savedIdent.getUser().getProperty("birthDay", Locale.ENGLISH));
	}
	
	@Test
	public void testCreateUserWithValidationError() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		UserVO vo = new UserVO();
		vo.setLogin("rest-809");
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail("");
		vo.putProperty("gender", "lu");

		String stringuifiedAuth = stringuified(vo);
		PutMethod method = createPut("/users", MediaType.APPLICATION_JSON, true);
    RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
    method.setRequestEntity(entity);
		
		int code = c.executeMethod(method);
		assertTrue(code == 406);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		List<ErrorVO> errors = parseErrorArray(body);
 		assertNotNull(errors);
		assertFalse(errors.isEmpty());
		assertTrue(errors.size() >= 2);
		assertNotNull(errors.get(0).getCode());
		assertNotNull(errors.get(0).getTranslation());
		assertNotNull(errors.get(1).getCode());
		assertNotNull(errors.get(1).getTranslation());
		
		Identity savedIdent = BaseSecurityManager.getInstance().findIdentityByName("rest-809");
		assertNull(savedIdent);
	}
	
	@Test
	public void testDeleteUser() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		//delete an authentication token
		String request = "/users/" + id2.getKey();
		DeleteMethod method = createDelete(request, MediaType.APPLICATION_XML, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		method.releaseConnection();
		
		Identity deletedIdent = BaseSecurityManager.getInstance().loadIdentityByKey(id2.getKey());
		assertNotNull(deletedIdent);//Identity aren't deleted anymore
		assertEquals(Identity.STATUS_DELETED, deletedIdent.getStatus());
	}
	
	@Test
	public void testUserForums() throws IOException {
		System.out.println("************************************************************************");
		System.out.println("* testUserForums");
		
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("forums")
				.queryParam("start", 0).queryParam("limit", 20).build();

		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		ForumVOes forums = parse(body, ForumVOes.class);
		
		assertNotNull(forums);
		assertNotNull(forums.getForums());
		assertTrue(forums.getForums().length > 0);

    BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		for(ForumVO forum:forums.getForums()) {
			Long groupKey = forum.getGroupKey();
			if(groupKey != null) {
				BusinessGroup bg = bgm.loadBusinessGroup(groupKey, false);
				assertNotNull(bg);
				CollaborationTools bgCTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
				assertTrue(bgCTSMngr.isToolEnabled(CollaborationTools.TOOL_FORUM));
				
				assertNotNull(forum.getForumKey());
				assertEquals(bg.getName(), forum.getName());
				assertEquals(bg.getKey(), forum.getGroupKey());
				assertTrue(bgm.isIdentityInBusinessGroup(id1, bg));
			} else {
				assertNotNull(forum.getCourseKey());
			}
		}
	}
	
	@Test
	public void testUserGroupForum() throws IOException {
		System.out.println("************************************************************************");
		System.out.println("* testUserGroupForum");
		
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("forums")
				.path("group").path(g1.getKey().toString())
				.path("threads").queryParam("start", "0").queryParam("limit", "25").build();

		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		MessageVOes threads = parse(body, MessageVOes.class);
		
		assertNotNull(threads);
		assertNotNull(threads.getMessages());
		assertTrue(threads.getMessages().length > 0);
	}
	
	@Test
	public void testUserCourseForum() throws IOException {
		System.out.println("************************************************************************");
		System.out.println("* testUserCourseForum");
		
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("forums")
				.path("course").path(demoCourse.getResourceableId().toString()).path(demoForumNode.getIdent())
				.path("threads").queryParam("start", "0").queryParam("limit", 25).build();

		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		MessageVOes threads = parse(body, MessageVOes.class);
		
		assertNotNull(threads);
		assertNotNull(threads.getMessages());
		assertTrue(threads.getMessages().length > 0);
	}
	
	@Test
	public void testUserFolders() throws IOException {
		System.out.println("************************************************************************");
		System.out.println("* testUserFolders");
		
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("folders").build();

		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		FolderVOes folders = parse(body, FolderVOes.class);
		
		assertNotNull(folders);
		assertNotNull(folders.getFolders());
		assertTrue(folders.getFolders().length > 0);

		boolean matchG2 = false;
		
    BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		for(FolderVO folder:folders.getFolders()) {
			Long groupKey = folder.getGroupKey();
			if(groupKey != null) {
				BusinessGroup bg = bgm.loadBusinessGroup(groupKey, false);
				assertNotNull(bg);
				CollaborationTools bgCTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
				assertTrue(bgCTSMngr.isToolEnabled(CollaborationTools.TOOL_FOLDER));
				
				assertEquals(bg.getName(), folder.getName());
				assertEquals(bg.getKey(), folder.getGroupKey());
				assertTrue(bgm.isIdentityInBusinessGroup(id1, bg));
				if(g2.getKey().equals(groupKey)) {
					matchG2 = true;
				}
			} else {
				assertNotNull(folder.getCourseKey());
			}
		}
		
		//id1 is participant of g2. Make sure it found the folder
		assertTrue(matchG2);
	}
	
	@Test
	public void testUserGroupFolder() throws IOException {
		System.out.println("************************************************************************");
		System.out.println("* testUserGroupFolder");
		
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("folders")
				.path("group").path(g2.getKey().toString()).build();

		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		List<FileVO> folders = parseFileArray(body);

		assertNotNull(folders);
		assertFalse(folders.isEmpty());
		assertEquals(1, folders.size()); //private and public
		
		FileVO portrait = folders.get(0);
		assertEquals("portrait.jpg", portrait.getTitle());
	}
	
	@Test
	public void testUserBCCourseNodeFolder() throws IOException {
		System.out.println("************************************************************************");
		System.out.println("* testUserBCCourseNodeFolder");
		
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("folders")
				.path("course").path(demoCourse.getResourceableId().toString()).path(demoBCCourseNode.getIdent()).build();

		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		List<FileVO> folders = parseFileArray(body);

		assertNotNull(folders);
		assertFalse(folders.isEmpty());
		assertEquals(1, folders.size()); //private and public
		
		FileVO singlePage = folders.get(0);
		assertEquals("singlepage.html", singlePage.getTitle());
	}
	
	@Test
	public void testUserPersonalFolder() throws Exception {
		System.out.println("************************************************************************");
		System.out.println("* testUserPersonalFolder");
		
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("folders").path("personal").build();

		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		List<FileVO> files = parseFileArray(body);
		
		assertNotNull(files);
		assertFalse(files.isEmpty());
		assertEquals(2, files.size()); //private and public
	}
	
	@Test
	public void testOtherUserPersonalFolder() throws Exception {
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString()).path("folders").path("personal").build();

		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		List<FileVO> files = parseFileArray(body);
		
		assertNotNull(files);
		assertTrue(files.isEmpty());
		assertEquals(0, files.size()); //private and public
	}
	
	@Test
	public void testOtherUserPersonalFolderOfId3() throws Exception {
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id3.getKey().toString()).path("folders").path("personal").build();

		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		List<FileVO> files = parseFileArray(body);
		
		assertNotNull(files);
		assertFalse(files.isEmpty());
		assertEquals(1, files.size()); //private and public
		
		FileVO portrait = files.get(0);
		assertEquals("portrait.jpg", portrait.getTitle());
	}
	
	@Test
	public void testUserGroup() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		//retrieve all groups
		String request = "/users/" + id1.getKey() + "/groups";
		GetMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);

		String body = method.getResponseBodyAsString();
		List<GroupVO> groups = parseGroupArray(body);
		assertNotNull(groups);
		assertTrue(groups.size() >= 4);
	}
	
	@Test
	public void testUserGroupWithPaging() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		//retrieve all groups
		URI uri =UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("groups")
			.queryParam("start", 0).queryParam("limit", 1).build();

		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);

		InputStream body = method.getResponseBodyAsStream();
		GroupVOes groups = parse(body, GroupVOes.class);
		method.releaseConnection();
		assertNotNull(groups);
		assertNotNull(groups.getGroups());
		assertEquals(1, groups.getGroups().length);
		assertTrue(groups.getTotalCount() >= 4);
	}
	
	@Test
	public void testUserGroupInfosWithPaging() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		//retrieve all groups
		URI uri =UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("groups").path("infos")
			.queryParam("start", 0).queryParam("limit", 1).build();

		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);

		InputStream body = method.getResponseBodyAsStream();
		GroupInfoVOes groups = parse(body, GroupInfoVOes.class);
		method.releaseConnection();
		assertNotNull(groups);
		assertNotNull(groups.getGroups());
		assertEquals(1, groups.getGroups().length);
		assertTrue(groups.getTotalCount() >= 4);
	}
	
	@Test
	public void testPortrait() throws IOException, URISyntaxException {
		URL portraitUrl = CoursesElementsTest.class.getResource("portrait.jpg");
		assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		//upload portrait
		String request = "/users/" + id1.getKey() + "/portrait";
		PostMethod method = createPost(request, MediaType.APPLICATION_JSON, true);
		method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		Part[] parts = { 
				new FilePart("file", portrait),
				new StringPart("filename","portrait.jpg")
		};
		method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		method.releaseConnection();
		
		//check if big and small portraits exist
		DisplayPortraitManager dps = DisplayPortraitManager.getInstance();
		File uploadDir = dps.getPortraitDir(id1);
		assertTrue(new File(uploadDir, DisplayPortraitManager.PORTRAIT_SMALL_FILENAME).exists());
		assertTrue(new File(uploadDir, DisplayPortraitManager.PORTRAIT_BIG_FILENAME).exists());
		
		//check get portrait
		String getRequest = "/users/" + id1.getKey() + "/portrait";
		GetMethod getMethod = createGet(getRequest, MediaType.APPLICATION_OCTET_STREAM, true);
		int getCode = c.executeMethod(getMethod);
		assertEquals(getCode, 200);
		InputStream in = getMethod.getResponseBodyAsStream();
		int b = 0;
		int count = 0;
		while((b = in.read()) > -1) {
			count++;
		}
		getMethod.releaseConnection();
		
		assertEquals(-1, b);//up to end of file
		assertTrue(count > 1000);//enough bytes
		File bigPortrait = new File(uploadDir, DisplayPortraitManager.PORTRAIT_BIG_FILENAME);
		assertEquals(count, bigPortrait.length());

		//check get portrait as Base64
		UriBuilder getRequest2 = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).queryParam("withPortrait", "true");
		GetMethod getMethod2 = createGet(getRequest2.build(), MediaType.APPLICATION_JSON, true);
		int getCode2 = c.executeMethod(getMethod2);
		assertEquals(getCode2, 200);
		InputStream in2 = getMethod2.getResponseBodyAsStream();
		UserVO userVo = parse(in2, UserVO.class);
		getMethod2.releaseConnection();
		assertNotNull(userVo);
		assertNotNull(userVo.getPortrait());
		byte[] datas = Base64.decodeBase64(userVo.getPortrait().getBytes());
		assertNotNull(datas);
		assertTrue(datas.length > 0);
		
		File smallPortrait = new File(uploadDir, DisplayPortraitManager.PORTRAIT_SMALL_FILENAME);
		assertEquals(datas.length, smallPortrait.length());
		
		try {
			ImageIO.read(new ByteArrayInputStream(datas));
		} catch (Exception e) {
			assertFalse("Cannot read the portrait after Base64 encoding/decoding", false);
		}
	}
	
	protected List<UserVO> parseUserArray(String body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<UserVO> parseUserArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<GroupVO> parseGroupArray(String body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<GroupVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}