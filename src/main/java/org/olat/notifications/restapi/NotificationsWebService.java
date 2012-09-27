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

package org.olat.notifications.restapi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.notifications.NotificationHelper;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.notifications.SubscriptionInfo;
import org.olat.core.util.notifications.items.SubscriptionListItem;
import org.olat.notifications.restapi.vo.SubscriptionInfoVO;
import org.olat.notifications.restapi.vo.SubscriptionListItemVO;
import org.olat.restapi.security.RestSecurityHelper;

/**
 * 
 * <h3>Description:</h3>
 * REST API for notifications
 * <p>
 * Initial Date:  25 aug 2010 <br>
 * @author srosse, srosse@frentix.com, http://www.frentix.com
 */
@Path("notifications")
public class NotificationsWebService {
	
	/**
	 * Retrieves the notification of the logged in user.
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The notifications
	 * @response.representation.200.example {@link org.olat.notifications.restapi.vo.Examples#SAMPLE_INFOVOes}
	 * @response.representation.404.doc The identity not found
	 * @param date The date (optional)
	 * @param type The type of notifications (User, Forum...) (optional)
	 * @param httpRequest The HTTP request
	 * @return an xml or json representation of a the user being search. The xml
	 *         correspond to a <code>SubscriptionInfoVO</code>. <code>SubscriptionInfoVO</code>
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response getNotifications(@QueryParam("date") String date,
			@QueryParam("type") String type, @Context HttpServletRequest httpRequest) {
		Identity identity = RestSecurityHelper.getIdentity(httpRequest);
		Locale locale = RestSecurityHelper.getLocale(httpRequest);
		
		Date compareDate;
		if(StringHelper.containsNonWhitespace(date)) {
			compareDate = parseDate(date, locale);
		} else {
			NotificationsManager man = NotificationsManager.getInstance();
			compareDate = man.getCompareDateFromInterval(man.getUserIntervalOrDefault(identity));
		}
		
		List<String> types = new ArrayList<String>(1);
		if(StringHelper.containsNonWhitespace(type)) {
			types.add(type);
		}
		
		Map<Subscriber,SubscriptionInfo> subsInfoMap = NotificationHelper.getSubscriptionMap(identity, locale, true, compareDate, types);
		List<SubscriptionInfoVO> voes = new ArrayList<SubscriptionInfoVO>();
		for(Map.Entry<Subscriber, SubscriptionInfo> entry: subsInfoMap.entrySet()) {
			SubscriptionInfo info = entry.getValue();
			if(info.hasNews()) {
				Subscriber subscriber = entry.getKey();
				voes.add(createSubscriptionInfoVO(subscriber.getPublisher(), info));
			}
		}
		SubscriptionInfoVO[] voesArr = new SubscriptionInfoVO[voes.size()];
		voes.toArray(voesArr);
		return Response.ok(voesArr).build();
	}
	
	private SubscriptionInfoVO createSubscriptionInfoVO(Publisher publisher, SubscriptionInfo info) {
		SubscriptionInfoVO infoVO  = new SubscriptionInfoVO(info);
		if(info.getSubscriptionListItems() != null && !info.getSubscriptionListItems().isEmpty()) {
			List<SubscriptionListItemVO> itemVOes = new ArrayList<SubscriptionListItemVO>(info.getSubscriptionListItems().size());
			
			String publisherType = publisher.getType();
			String resourceType = publisher.getResName();
			for(SubscriptionListItem item:info.getSubscriptionListItems()) {
				SubscriptionListItemVO itemVO = new SubscriptionListItemVO(item); 
				//resource specific
				if("BusinessGroup".equals(resourceType)) {
					itemVO.setGroupKey(publisher.getResId());
				} else if("CourseModule".equals(resourceType)) {
					itemVO.setCourseKey(publisher.getResId());
					itemVO.setCourseNodeId(publisher.getSubidentifier());
				}
				
				//publisher specififc
				if("Forum".equals(publisherType)) {
					//extract the message id
					List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(item.getBusinessPath());
					if(ces.size() > 0) {
						ContextEntry lastCe = ces.get(ces.size() - 1);
						if("Message".equals(lastCe.getOLATResourceable().getResourceableTypeName())) {
							itemVO.setMessageKey(lastCe.getOLATResourceable().getResourceableId());
						}
					}	
				} else if("FolderModule".equals(publisherType)) {
					List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(item.getBusinessPath());
					if(ces.size() > 0) {
						ContextEntry lastCe = ces.get(ces.size() - 1);
						if(lastCe.getOLATResourceable().getResourceableTypeName().startsWith("path=")) {
							String path = BusinessControlFactory.getInstance().getPath(lastCe);
							itemVO.setPath(path);
						}
					}	
				}
				itemVOes.add(itemVO);
			}
			infoVO.setItems(itemVOes);
		}
		return infoVO;
	}
	
	private Date parseDate(String date, Locale locale) {
		if(StringHelper.containsNonWhitespace(date)) {
			if(date.indexOf('T') > 0) {
				if(date.indexOf('.') > 0) {
					try {
						return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.S").parse(date);
					} catch (ParseException e) {
						//fail silently
					}
				} else {
					try {
						return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(date);
					} catch (ParseException e) {
						//fail silently
					}
				}
			}
			
			//try with the locale
			if(date.length() > 10) {
				//probably date time
				try {
					DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
					format.setLenient(true);
					return format.parse(date);
				} catch (ParseException e) {
					//fail silently
				}
			} else {
				try {
					DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
					format.setLenient(true);
					return format.parse(date);
				} catch (ParseException e) {
					//fail silently
				}
			}
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MONTH, -1);
		return cal.getTime();
	}
}
