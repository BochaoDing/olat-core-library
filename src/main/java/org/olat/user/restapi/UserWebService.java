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
package org.olat.user.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getLocale;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;
import static org.olat.restapi.security.RestSecurityHelper.isUserManager;
import static org.olat.user.restapi.UserVOFactory.formatDbUserProperty;
import static org.olat.user.restapi.UserVOFactory.get;
import static org.olat.user.restapi.UserVOFactory.link;
import static org.olat.user.restapi.UserVOFactory.parseUserProperty;
import static org.olat.user.restapi.UserVOFactory.post;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.restapi.group.MyGroupWebService;
import org.olat.restapi.support.vo.ErrorVO;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * This web service handles functionalities related to <code>User</code>.
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("users")
public class UserWebService {
	
	private static final OLog log = Tracing.createLoggerFor(UserWebService.class);
	private static final String VERSION = "1.0";
	
	public static final String PROPERTY_HANDLER_IDENTIFIER = UserWebService.class.getName();
	
	public static CacheControl cc = new CacheControl();
	
	static {
		cc.setMaxAge(-1);
	}
	
	/**
	 * The version of the User Web Service
   * @response.representation.200.mediaType text/plain
   * @response.representation.200.doc The version of this specific Web Service
   * @response.representation.200.example 1.0
	 * @return The version number
	 */
	@GET
	@Path("version")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Search users and return them in a simple form (without user properties). User properties
	 * can be added two the query parameters. If the authUsername and the authProvider are set,
	 * the search is made only with these two parameters because they are sufficient to return
	 * a single user.
	 * @response.representation.200.qname {http://www.example.com}userVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The list of all users in the OLAT system
   * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param login The login (search with like)
	 * @param authProvider An authentication provider (optional)
	 * @param authUsername An specific username from the authentication provider
   * @param uriInfo The URI infos
   * @param httpRequest The HTTP request
	 * @return An array of users
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getUserListQuery(@QueryParam("login") String login, @QueryParam("authProvider") String authProvider,
			@QueryParam("authUsername") String authUsername,
			@Context UriInfo uriInfo, @Context HttpServletRequest httpRequest) {
		MultivaluedMap<String,String> params = uriInfo.getQueryParameters();
		return getUserList(login, authProvider, authUsername, params, uriInfo, httpRequest);
	}

	private Response getUserList(String login, String authProvider, String authUsername, Map<String,List<String>> params,
			UriInfo uriInfo, HttpServletRequest httpRequest) {
		if(!isUserManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		List<Identity> identities;
		//make only a search by authUsername
		if(StringHelper.containsNonWhitespace(authProvider) && StringHelper.containsNonWhitespace(authUsername)) {
			Authentication auth =BaseSecurityManager.getInstance().findAuthenticationByAuthusername(authUsername, authProvider);
			if(auth == null) {
				identities = Collections.emptyList();
			} else {
				identities = Collections.singletonList(auth.getIdentity());
			}
		} else {
			String[] authProviders = null;
			if(StringHelper.containsNonWhitespace(authProvider)) {
				authProviders = new String[]{authProvider};
			}
			
			//retrieve and convert the parameters value
			Map<String,String> userProps = new HashMap<String,String>();
			if(!params.isEmpty()) {
				UserManager um = UserManager.getInstance();
				Locale locale = getLocale(httpRequest);
				List<UserPropertyHandler> propertyHandlers = um.getUserPropertyHandlersFor(PROPERTY_HANDLER_IDENTIFIER, false);
				for(UserPropertyHandler handler:propertyHandlers) {
					if(!params.containsKey(handler.getName())) continue;
					
					List<String> values = params.get(handler.getName());
					if(values.isEmpty()) continue;
					
					String value = formatDbUserProperty(values.get(0), handler, locale);
					userProps.put(handler.getName(), value);
				}
			}

			identities = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(login, userProps, true, null, null, authProviders, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);
		}
		
		int count = 0;
		UserVO[] userVOs = new UserVO[identities.size()];
		for(Identity identity:identities) {
			userVOs[count++] = link(get(identity), uriInfo);
		}
		return Response.ok(userVOs).build();
	}
	
	/**
	 * Creates and persists a new user entity
	 * @response.representation.qname {http://www.example.com}userVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The user to persist
   * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The persisted user
   * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @response.representation.406.doc The list of errors
   * @response.representation.406.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_ERRORVOes}
	 * @param user The user to persist
	 * @param request The HTTP request
	 * @return the new persisted <code>User</code>
	 */
	@PUT
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response create(UserVO user, @Context HttpServletRequest request) {
		if(!isUserManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		// Check if login is still available
		Identity identity = BaseSecurityManager.getInstance().findIdentityByName(user.getLogin());
		if (identity != null) {
			Locale locale = getLocale(request);
			Translator translator = new PackageTranslator("org.olat.admin.user", locale);
			String translation = translator.translate("new.error.loginname.choosen");
			ErrorVO[] errorVos = new ErrorVO[]{
				new ErrorVO("org.olat.admin.user", "new.error.loginname.choosen", translation)
			};
			return Response.ok(errorVos).status(Status.NOT_ACCEPTABLE).build();
		}
		
		List<ErrorVO> errors = validateUser(user, request);
		if(errors.isEmpty()) {
			User newUser = UserManager.getInstance().createUser(user.getFirstName(), user.getLastName(), user.getEmail());
			Identity id = AuthHelper.createAndPersistIdentityAndUserWithUserGroup(user.getLogin(), user.getPassword(), newUser);
			post(newUser, user, getLocale(request));
			UserManager.getInstance().updateUser(newUser);
			return Response.ok(get(id)).build();
		}
		
		//content not ok
		ErrorVO[] errorVos = new ErrorVO[errors.size()];
		errors.toArray(errorVos);
		return Response.ok(errorVos).status(Status.NOT_ACCEPTABLE).build();
	}
	
	/**
	 * Retrieves the roles of a user given its unique key identifier
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The user
   * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_ROLESVO}
   * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The identity not found
	 * @param identityKey The user key identifier of the user being searched
	 * @param httpRequest The HTTP request
	 * @return an xml or json representation of a the roles being search.
	 */
	@GET
	@Path("{identityKey}/roles")
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response getRoles(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			boolean isUserManager = isUserManager(request);
			if(!isUserManager) {
				return Response.serverError().status(Status.FORBIDDEN).build();
			}
			Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
			if(identity == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}

			Roles roles = BaseSecurityManager.getInstance().getRoles(identity);
			return Response.ok(new RolesVO(roles)).build();
		} catch (Throwable e) {
			throw new WebApplicationException(e);
		}
	}
	
	
	@POST
	@Path("{identityKey}/roles")
	@Consumes({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response updateRoles(@PathParam("identityKey") Long identityKey, RolesVO roles, @Context HttpServletRequest request) {
		try {
			boolean isUserManager = isUserManager(request);
			if(!isUserManager) {
				return Response.serverError().status(Status.FORBIDDEN).build();
			}
			Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
			if(identity == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			Roles modRoles = roles.toRoles();
			BaseSecurityManager.getInstance().updateRoles(identity, modRoles);
			return Response.ok(new RolesVO(modRoles)).build();
		} catch (Throwable e) {
			throw new WebApplicationException(e);
		}
	}
	

	/**
	 * Retrieves an user given its unique key identifier
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The user
   * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVO}
   * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The identity not found
	 * @param identityKey The user key identifier of the user being searched
	 * @param withPortrait If true return the portrait as Base64 (default false)
	 * @param uriInfo The URI infos
	 * @param httpRequest The HTTP request
	 * @return an xml or json representation of a the user being search. The xml
	 *         correspond to a <code>UserVO</code>. <code>UserVO</code> is a
	 *         simplified representation of the <code>User</code> and <code>Identity</code>
	 */
	@GET
	@Path("{identityKey}")
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response findById(@PathParam("identityKey") Long identityKey, @QueryParam("withPortrait") @DefaultValue("false") Boolean withPortrait,
			@Context UriInfo uriInfo, @Context HttpServletRequest httpRequest) {
		try {
			Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
			if(identity == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			boolean isUserManager = isUserManager(httpRequest);
			UserVO userVO = link(get(identity, null, true, isUserManager, withPortrait), uriInfo);
			return Response.ok(userVO).build();
		} catch (Throwable e) {
			throw new WebApplicationException(e);
		}
	}
	
	/**
	 * Retrieves the portrait of an user
	 * @response.representation.200.mediaType application/octet-stream
	 * @response.representation.200.doc The portrait as image
   * @response.representation.404.doc The identity or the portrait not found
	 * @param identityKey The identity key of the user being searched
	 * @param request The REST request
	 * @return The image
	 */
	@GET
	@Path("{identityKey}/portrait")
	@Produces({"image/jpeg","image/jpg",MediaType.APPLICATION_OCTET_STREAM})
	public Response getPortrait(@PathParam("identityKey") Long identityKey, @Context Request request) {
		try {
			Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
			if(identity == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			File portrait = DisplayPortraitManager.getInstance().getBigPortrait(identity);
			if(portrait == null || !portrait.exists()) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}

			Date lastModified = new Date(portrait.lastModified());
			Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
			if(response == null) {
				response = Response.ok(portrait).lastModified(lastModified).cacheControl(cc);
			}
			return response.build();
		} catch (Throwable e) {
			throw new WebApplicationException(e);
		}
	}
	
	/**
	 * Upload the portrait of an user
	 * @response.representation.200.mediaType application/octet-stream
	 * @response.representation.200.doc The portrait as image
   * @response.representation.401.doc Not authorized
   * @response.representation.404.doc The identity or the portrait not found
	 * @param identityKey The user key identifier of the user being searched
	 * @param fileName The name of the image (mandatory)
	 * @param file The image
	 * @param request The REST request
	 * @return The image
	 */
	@POST
	@Path("{identityKey}/portrait")
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	public Response postPortrait(@PathParam("identityKey") Long identityKey, @FormParam("filename") String filename, 
			@FormParam("file") InputStream file, @Context HttpServletRequest request) {
		try {
			Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
			if(identity == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			Identity authIdentity = getUserRequest(request).getIdentity();
			if(!isUserManager(request) && !identity.equalsByPersistableKey(authIdentity)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			File tmpFile = getTmpFile(filename);
			FileUtils.save(file, tmpFile);
			DisplayPortraitManager.getInstance().setPortrait(tmpFile, identity);
			tmpFile.delete();
			return Response.ok().build();
		} catch (Throwable e) {
			throw new WebApplicationException(e);
		}	
	}
	
	/**
	 * Deletes the portrait of an user
	 * @response.representation.200.doc The portrait deleted
   * @response.representation.401.doc Not authorized
	 * @param identityKey The identity key identifier of the user being searched
	 * @param request The REST request
	 * @return The image
	 */
	@DELETE
	@Path("{identityKey}/portrait")
	public Response deletePortrait(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			Identity authIdentity = getUserRequest(request).getIdentity();
			Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
			if(identity == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			} else if(!isUserManager(request) && !identity.equalsByPersistableKey(authIdentity)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		
			DisplayPortraitManager.getInstance().deletePortrait(identity);
			return Response.ok().build();
		} catch (Throwable e) {
			throw new WebApplicationException(e);
		}	
	}

	@Path("{identityKey}/groups")
	public MyGroupWebService getUserGroupList(@PathParam("identityKey") Long identityKey) {
		Identity retrievedUser = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
		if(retrievedUser == null) {
			return null;
		}
		return new MyGroupWebService(retrievedUser);
	}

	/**
	 * Update an user
	 * @response.representation.qname {http://www.example.com}userVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The user
   * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVO}
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The user
   * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The identity not found
   * @response.representation.406.qname {http://www.example.com}errorVO
	 * @response.representation.406.mediaType application/xml, application/json
	 * @response.representation.406.doc The list of validation errors
   * @response.representation.406.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_ERRORVOes}
	 * @param identityKey The user key identifier
	 * @param user The user datas
	 * @param uriInfo The URI infos
	 * @param request The HTTP request
	 * @return <code>User</code> object. The operation status (success or fail)
	 */
	@POST
	@Path("{identityKey}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response update(@PathParam("identityKey") Long identityKey, UserVO user, @Context UriInfo uriInfo, @Context HttpServletRequest request) {
		try {
			if(user == null) {
				return Response.serverError().status(Status.NO_CONTENT).build();
			}
			if(!isUserManager(request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}

			BaseSecurity baseSecurity = BaseSecurityManager.getInstance();
			Identity retrievedIdentity = baseSecurity.loadIdentityByKey(identityKey, false);
			if(retrievedIdentity == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			User retrievedUser = retrievedIdentity.getUser();
			List<ErrorVO> errors = validateUser(user, request);
			if(errors.isEmpty()) {
				post(retrievedUser, user, getLocale(request));
				UserManager.getInstance().updateUser(retrievedUser);
				return Response.ok(link(get(retrievedIdentity, true, true), uriInfo)).build();
			}
			
			//content not ok
			ErrorVO[] errorVos = new ErrorVO[errors.size()];
			errors.toArray(errorVos);
			return Response.ok(errorVos).status(Status.NOT_ACCEPTABLE).build();
		} catch (Exception e) {
			log.error("Error updating an user", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	private List<ErrorVO> validateUser(UserVO user, HttpServletRequest request) {
		UserManager um = UserManager.getInstance();
		
		Locale locale = getLocale(request);
		List<ErrorVO> errors = new ArrayList<ErrorVO>();
		List<UserPropertyHandler> propertyHandlers = um.getUserPropertyHandlersFor(PROPERTY_HANDLER_IDENTIFIER, false);
		validateProperty(UserConstants.FIRSTNAME, user.getFirstName(), propertyHandlers, errors, um, locale);
		validateProperty(UserConstants.LASTNAME, user.getLastName(), propertyHandlers, errors, um, locale);
		validateProperty(UserConstants.EMAIL, user.getEmail(), propertyHandlers, errors, um, locale);
		for (UserPropertyHandler propertyHandler : propertyHandlers) {
			if(!UserConstants.FIRSTNAME.equals(propertyHandler.getName())
					&& !UserConstants.LASTNAME.equals(propertyHandler.getName())
					&& !UserConstants.EMAIL.equals(propertyHandler.getName())) {
				validateProperty(user, propertyHandler, errors, um, locale);
			}
		}
		return errors;
	}
	
	private boolean validateProperty(String name, String value, List<UserPropertyHandler> handlers, List<ErrorVO> errors, UserManager um, Locale locale) {
		for(UserPropertyHandler handler:handlers) {
			if(handler.getName().equals(name)) {
				return validateProperty(value, handler, errors, um, locale);
			}
		}
		return true;
	}
	
	private boolean validateProperty(UserVO user, UserPropertyHandler userPropertyHandler, List<ErrorVO> errors, UserManager um, Locale locale) {
		String value = user.getProperty(userPropertyHandler.getName());
		return validateProperty(value, userPropertyHandler, errors, um, locale);
	}
	
	private boolean validateProperty(String value, UserPropertyHandler userPropertyHandler, List<ErrorVO> errors, UserManager um, Locale locale) {
		ValidationError error = new ValidationError();
		if(!StringHelper.containsNonWhitespace(value) && um.isMandatoryUserProperty(PROPERTY_HANDLER_IDENTIFIER, userPropertyHandler)) {
			Translator translator = new PackageTranslator("org.olat.core", locale);
			String translation = translator.translate("new.form.mandatory");
			errors.add(new ErrorVO("org.olat.core", "new.form.mandatory", translation));
			return false;
		}
		
		value = parseUserProperty(value, userPropertyHandler, locale);
		
		if (!userPropertyHandler.isValidValue(value, error, locale)) {
			String pack = userPropertyHandler.getClass().getPackage().getName();
			Translator translator = new PackageTranslator(pack, locale);
			String translation = translator.translate(error.getErrorKey());
			errors.add(new ErrorVO(pack, error.getErrorKey(), translation));
			return false;
		}
		
		return true;
	}

	/**
	 * Delete an user from the system
	 * @response.representation.200.doc The user is removed from the group
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The identity not found
	 * @param identityKey The user key identifier
	 * @param request The HTTP request
	 * @return <code>Response</code> object. The operation status (success or fail)
	 */
	@DELETE
	@Path("{identityKey}")
	public Response delete(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		if(!isUserManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		UserDeletionManager.getInstance().deleteIdentity(identity);
		return Response.ok().build();
	}
	
	/**
	 * Fallback method for browsers
	 * @response.representation.200.doc The user is removed from the group
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The identity not found
	 * @param identityKey The user key identifier
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("{identityKey}/delete")
	@Produces(MediaType.APPLICATION_XML)
	public Response deletePost(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		return delete(identityKey, request);
	}
	
	private File getTmpFile(String suffix) {
		suffix = (suffix == null ? "" : suffix);
		File tmpFile = new File(WebappHelper.getUserDataRoot()	+ "/tmp/", CodeHelper.getGlobalForeverUniqueID() + "_" + suffix);
		FileUtils.createEmptyFile(tmpFile);
		return tmpFile;
	}
}
