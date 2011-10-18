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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.restapi.group;

import static org.olat.restapi.security.RestSecurityHelper.isGroupManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.admin.quota.QuotaConstants;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.restapi.VFSWebServiceSecurityCallback;
import org.olat.core.util.vfs.restapi.VFSWebservice;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.properties.BusinessGroupPropertyManager;
import org.olat.group.ui.BGConfigFlags;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumManager;
import org.olat.modules.fo.restapi.ForumWebService;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.GroupInfoVO;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;

/**
 * Description:<br>
 * This handles the learning groups.
 * 
 * <P>
 * Initial Date:  23 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("groups")
public class LearningGroupWebService {
	
	private OLog log = Tracing.createLoggerFor(LearningGroupWebService.class);
	
	private static final String VERSION = "1.0";
	
	private static CacheControl cc = new CacheControl();
	static {
		cc.setMaxAge(-1);
	}
	
	/**
	 * Retrieves the version of the Group Web Service.
   * @response.representation.200.mediaType text/plain
   * @response.representation.200.doc The version of this specific Web Service
   * @response.representation.200.example 1.0
	 * @return
	 */
	@GET
	@Path("version")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Return the list of all groups if you have group manager permission, or all
	 * learning group that you particip with or owne.
	 * @response.representation.200.qname {http://www.example.com}groupVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc This is the list of all groups in OLAT system
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_GROUPVOes}
   * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getGroupList(@Context HttpServletRequest request) {
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		List<BusinessGroup> bgs;
		if(isGroupManager(request)) {
			bgs = bgm.getAllBusinessGroups();
		} else {
			bgs = new ArrayList<BusinessGroup>();
			Identity identity = RestSecurityHelper.getIdentity(request);
			List<BusinessGroup> ownedGroups = bgm.findBusinessGroupsOwnedBy(null, identity, null);
			for (BusinessGroup group:ownedGroups) {
				bgs.add(group);
			}
			List<BusinessGroup> partGroups = bgm.findBusinessGroupsAttendedBy(null, identity, null);
			for (BusinessGroup group:partGroups) {
				bgs.add(group);
			}
		}
		
		int count = 0;
		GroupVO[] groupVOs = new GroupVO[bgs.size()];
		for(BusinessGroup bg:bgs) {
			groupVOs[count++] = ObjectFactory.get(bg);
		}
		return Response.ok(groupVOs).build();
	}
	
	/**
	 * Return the group specified by the key of the group.
	 * @response.representation.200.qname {http://www.example.com}groupVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc A business group in the OLAT system
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_GROUPVO}
	 * @param groupKey The key of the group
	 * @param request The REST request
	 * @param httpRequest The HTTP request
	 * @return
	 */
	@GET
	@Path("{groupKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response findById(@PathParam("groupKey") Long groupKey, @Context Request request,
			@Context HttpServletRequest httpRequest) {
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		BusinessGroup bg = bgm.loadBusinessGroup(groupKey, false);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		Identity identity = RestSecurityHelper.getIdentity(httpRequest);
		if(!isGroupManager(httpRequest) && !bgm.isIdentityInBusinessGroup(identity, bg)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Date lastModified = bg.getLastModified();
		Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
		if(response == null) {
			GroupVO vo = ObjectFactory.get(bg);
			response = Response.ok(vo);
		}
		return response.build();
	}
	
	/**
	 * Updates a group.
	 * @response.representation.qname {http://www.example.com}groupVO
   * @response.representation.mediaType application/xml, application/json
   * @response.representation.doc A business group in the OLAT system
   * @response.representation.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_GROUPVO}
	 * @response.representation.200.qname {http://www.example.com}groupVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The saved business group
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_GROUPVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group cannot be found
	 * @param groupKey The key of the group
	 * @param group The group
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("{groupKey}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postGroup(@PathParam("groupKey") Long groupKey, final GroupVO group, @Context HttpServletRequest request) {
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		final BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		final BusinessGroup bg = bgm.loadBusinessGroup(groupKey, false);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(!StringHelper.containsNonWhitespace(group.getName())) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}

		BusinessGroup savedBg = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(bg, new SyncerCallback<BusinessGroup>(){
			public BusinessGroup execute() {
				BusinessGroup reloadedBG = bgm.loadBusinessGroup(bg);
				reloadedBG.setName(group.getName());
				reloadedBG.setDescription(group.getDescription());
				reloadedBG.setMinParticipants(normalize(group.getMinParticipants()));
				reloadedBG.setMaxParticipants(normalize(group.getMaxParticipants()));
				bgm.updateBusinessGroup(reloadedBG);
				return reloadedBG;
			}
		});
		
		//save the updated group
		GroupVO savedVO = ObjectFactory.get(savedBg);
		return Response.ok(savedVO).build();
	}
	
	/**
	 * Deletes the business group specified by the groupKey.
	 * @response.representation.200.doc The business group is deleted
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group cannot be found
	 * @param groupKey The key of the group
	 * @param request The HTTP request
	 * @return
	 */
	@DELETE
	@Path("{groupKey}")
	public Response deleteGroup(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		BusinessGroup bg = bgm.loadBusinessGroup(groupKey, false);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		bgm.deleteBusinessGroup(bg);
		return Response.ok().build();
	}
	
	/**
	 * Returns the informations of the group specified by the groupKey.
	 * @response.representation.200.qname {http://www.example.com}groupInfoVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc Participants of the business group
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_GROUPINFOVO}
	 * @response.representation.404.doc The business group cannot be found
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Path("{groupKey}/infos")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getInformations(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		BusinessGroup bg = bgm.loadBusinessGroup(groupKey, false);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgm.isIdentityInBusinessGroup(identity, bg)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		}
		
		GroupInfoVO info = new GroupInfoVO();
		
		//forum
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
		if(collabTools.isToolEnabled(CollaborationTools.TOOL_FORUM)) {
			info.setForumKey(collabTools.getForum().getKey());
		}
		
		String news = collabTools.lookupNews();
		info.setNews(news);
		
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_WIKI)) {
			info.setHasWiki(Boolean.TRUE);
		} else {
			info.setHasWiki(Boolean.FALSE);
		}
		
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
			info.setHasFolder(Boolean.TRUE);
		} else {
			info.setHasFolder(Boolean.FALSE);
		}
		
		return Response.ok(info).build();
	}
	
	/**
	 * Return the Forum web service
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@Path("{groupKey}/forum")
	public ForumWebService getForum(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		BusinessGroup bg = bgm.loadBusinessGroup(groupKey, false);
		if(bg == null) {
			return null;
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgm.isIdentityInBusinessGroup(identity, bg)) {
				return null;
			}
		}
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
		if(collabTools.isToolEnabled(CollaborationTools.TOOL_FORUM)) {
			Forum forum = collabTools.getForum();
			return new ForumWebService(forum);
		}
		return null;
	}
	
	@Path("{groupKey}/folder")
	public VFSWebservice getFolder(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		BusinessGroup bg = bgm.loadBusinessGroup(groupKey, false);
		if(bg == null) {
			return null;
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgm.isIdentityInBusinessGroup(identity, bg)) {
				return null;
			}
		}
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
		if(!collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
			return null;
		}
		
		String relPath = collabTools.getFolderRelPath();
		QuotaManager qm = QuotaManager.getInstance();
		Quota folderQuota = qm.getCustomQuota(relPath);
		if (folderQuota == null) {
			Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS);
			folderQuota = QuotaManager.getInstance().createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
		SubscriptionContext subsContext = null;
		VFSSecurityCallback secCallback = new VFSWebServiceSecurityCallback(true, true, true, folderQuota, subsContext);
		OlatRootFolderImpl rootContainer = new OlatRootFolderImpl(relPath, null);
		rootContainer.setLocalSecurityCallback(secCallback);
		return new VFSWebservice(rootContainer);
	}
	
	/**
	 * Returns the list of owners of the group specified by the groupKey.
	 * @response.representation.200.qname {http://www.example.com}userVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc Owners of the business group
   * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.404.doc The business group cannot be found
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Path("{groupKey}/owners")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTutors(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		BusinessGroup bg = bgm.loadBusinessGroup(groupKey, false);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgm.isIdentityInBusinessGroup(identity, bg)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(bg);
			if(!bgpm.showOwners()) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		}
		
		return getIdentityInGroup(bg.getOwnerGroup());
	}
	
	/**
	 * Returns the list of participants of the group specified by the groupKey.
	 * @response.representation.200.qname {http://www.example.com}userVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc Participants of the business group
   * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.404.doc The business group cannot be found
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Path("{groupKey}/participants")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getParticipants(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		BusinessGroup bg = bgm.loadBusinessGroup(groupKey, false);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgm.isIdentityInBusinessGroup(identity, bg)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(bg);
			if(!bgpm.showPartips()) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		}
		
		return getIdentityInGroup(bg.getPartipiciantGroup());
	}
	
	private Response getIdentityInGroup(SecurityGroup sg) {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		List<Object[]> owners = securityManager.getIdentitiesAndDateOfSecurityGroup(sg);
		
		int count = 0;
		UserVO[] ownerVOs = new UserVO[owners.size()];
		for(int i=0; i<owners.size(); i++) {
			Identity identity = (Identity)(owners.get(i))[0];
			ownerVOs[count++] = UserVOFactory.get(identity);
		}
		return Response.ok(ownerVOs).build();
	}
	
	/**
	 * Adds an owner to the group.
	 * @response.representation.200.doc The user is added as owner of the group
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group or the user cannot be found
	 * @param groupKey The key of the group 
	 * @param identityKey The user's id
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("{groupKey}/owners/{identityKey}")
	public Response addTutor(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			if(!isGroupManager(request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			final UserRequest ureq = RestSecurityHelper.getUserRequest(request);
			
			final BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
			final BusinessGroup group = bgm.loadBusinessGroup(groupKey, false);
			final Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
			if(identity == null || group == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerCallback<Boolean>(){
				public Boolean execute() {
					BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
					bgm.addOwnerAndFireEvent(ureq.getIdentity(), identity, group, flags, false);
					return Boolean.TRUE;
				}
			});// end of doInSync
			
			return Response.ok().build();
		} catch (Exception e) {
			log.error("Trying to add an owner to a group", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Fallback method for browser.
	 * @response.representation.200.doc The user is added as owner of the group
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group or the user cannot be found
	 * @param groupKey The key of the group
	 * @param identityKey The user's id
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("{groupKey}/owners/{identityKey}/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response addTutorPost(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		return addTutor(groupKey, identityKey, request);
	}
	
	/**
	 * Removes the owner from the group.
	 * @response.representation.200.doc The user is removed as owner from the group
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group or the user cannot be found
	 * @param groupKey The key of the group
	 * @param identityKey The user's id
	 * @param request The HTTP request
	 * @return
	 */
	@DELETE
	@Path("{groupKey}/owners/{identityKey}")
	public Response removeTutor(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			if(!isGroupManager(request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			final UserRequest ureq = RestSecurityHelper.getUserRequest(request);
			
			final BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
			final BusinessGroup group = bgm.loadBusinessGroup(groupKey, false);
			final Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
			if(identity == null || group == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerCallback<Boolean>(){
				public Boolean execute() {
					BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
					bgm.removeOwnerAndFireEvent(ureq.getIdentity(), identity, group, flags, false);
					return Boolean.TRUE;
				}
			});// end of doInSync

			return Response.ok().build();
		} catch (Exception e) {
			log.error("Trying to remove an owner to a group", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Fallback method for browser.
	 * @response.representation.200.doc The user is removed as owner from the group
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group or the user cannot be found
	 * @param groupKey The key of the group
	 * @param identityKey The user's id
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("{groupKey}/owners/{identityKey}/delete")
	public Response removeTutorPost(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		return removeTutor(groupKey, identityKey, request);
	}
	
	/**
	 * Adds a participant to the group.
	 * @response.representation.200.doc The user is added as participant of the group
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group or the user cannot be found
	 * @param groupKey The key of the group
	 * @param identityKey The user's id
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("{groupKey}/participants/{identityKey}")
	public Response addParticipant(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			if(!isGroupManager(request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			final UserRequest ureq = RestSecurityHelper.getUserRequest(request);
			
			final BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
			final BusinessGroup group = bgm.loadBusinessGroup(groupKey, false);
			final Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
			if(identity == null || group == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerCallback<Boolean>(){
				public Boolean execute() {
					BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
					bgm.addParticipantAndFireEvent(ureq.getIdentity(), identity, group, flags, false);
					return Boolean.TRUE;
				}
			});// end of doInSync
			
			return Response.ok().build();
		} catch (Exception e) {
			log.error("Trying to add a participant to a group", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Fallback method for browser.
	 * @response.representation.200.doc The user is added as participant of the group
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group or the user cannot be found
	 * @param groupKey The id of the group
	 * @param identityKey The user's id
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("{groupKey}/participants/{identityKey}/new")
	public Response addParticipantPost(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		return addParticipant(groupKey, identityKey, request);
	}
	
	/**
	 * Removes a participant from the group.
	 * @response.representation.200.doc The user is remove from the group as participant
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group or the user cannot be found
	 * @param groupKey The key of the group
	 * @param identityKey The id of the user
	 * @param request The HTTP request
	 * @return
	 */
	@DELETE
	@Path("{groupKey}/participants/{identityKey}")
	public Response removeParticipant(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			if(!isGroupManager(request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			final UserRequest ureq = RestSecurityHelper.getUserRequest(request);
			
			final BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
			final BusinessGroup group = bgm.loadBusinessGroup(groupKey, false);
			final Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
			if(identity == null || group == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerExecutor(){
				public void execute() {
					BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
					bgm.removeParticipantAndFireEvent(ureq.getIdentity(), identity, group, flags, false);
				}
			});

			return Response.ok().build();
		} catch (Exception e) {
			log.error("Trying to remove a participant to a group", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Fallback method for browser.
	 * @response.representation.200.doc The user is remove from the group as participant
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group or the user cannot be found
	 * @param groupKey The key of the group
	 * @param identityKey The id of the user
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("{groupKey}/participants/{identityKey}/delete")
	public Response removeParticipantPost(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		return removeParticipant(groupKey, identityKey, request);
	}
	
	/**
	 * @param integer
	 * @return value bigger or equal than 0
	 */
	private static final Integer normalize(Integer integer) {
		if(integer == null) return new Integer(0);
		if(integer.intValue() < 0) return new Integer(0);
		return integer;
	}
}
