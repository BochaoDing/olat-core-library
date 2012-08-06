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

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.user.restapi.UserVOFactory.get;
import static org.olat.user.restapi.UserVOFactory.link;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroupService;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  21 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Path("contacts")
public class ContactsWebService {
	
	/**
	 * Retrieve the contacts of the logged in identity.
	 * @response.representation.200.doc The list of contacts
	 * @param start
	 * @param limit
	 * @param httpRequest The HTTP request
	 * @param uriInfo The URI information
	 * @return The list of contacts
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getMyContacts(@QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("limit") @DefaultValue("25") Integer limit,
			@Context HttpServletRequest httpRequest, @Context UriInfo uriInfo) {
		
		Identity identity = getIdentity(httpRequest);
		
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		List<Identity> contacts = bgs.findContacts(identity, start, limit);
		int totalCount = bgs.countContacts(identity);
		
		int count = 0;
		UserVO[] userVOs = new UserVO[contacts.size()];
		for(Identity contact:contacts) {
			userVOs[count++] = link(get(contact, null, true, false, true), uriInfo);
		}
		UserVOes voes = new UserVOes();
		voes.setUsers(userVOs);
		voes.setTotalCount(totalCount);
		return Response.ok(voes).build();
	}
}
