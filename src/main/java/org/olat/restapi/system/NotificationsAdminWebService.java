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
package org.olat.restapi.system;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.restapi.system.vo.NotificationsStatus;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class NotificationsAdminWebService {
	
	private static final OLog log = Tracing.createLoggerFor(NotificationsAdminWebService.class);
	
	@GET
	@Path("status")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getStatus(@Context HttpServletRequest request) {
		return Response.ok(new NotificationsStatus(getJobStatus())).build();
	}
	
	@GET
	@Path("status")
	@Produces({MediaType.TEXT_PLAIN})
	public Response getPlainTextStatus(@Context HttpServletRequest request) {
		return Response.ok(getJobStatus()).build();
	}
	
	private String getJobStatus() {
		try {
			Scheduler scheduler = CoreSpringFactory.getImpl(Scheduler.class);
			@SuppressWarnings("unchecked")
			List<JobExecutionContext> jobs = scheduler.getCurrentlyExecutingJobs();
			for(JobExecutionContext job:jobs) {
				if("org.olat.notifications.job.enabled".equals(job.getJobDetail().getName())) {
					return "running";
				}
			}
			return "stopped";
		} catch (SchedulerException e) {
			log.error("", e);
			return "error";
		}
	}
	
	@POST
	@Path("status")
	public Response setStatus(@FormParam("status") String status, @Context HttpServletRequest request) {
		if("running".equals(status)) {
			try {
				Scheduler scheduler = CoreSpringFactory.getImpl(Scheduler.class);
				JobDetail detail = scheduler.getJobDetail("org.olat.notifications.job.enabled", Scheduler.DEFAULT_GROUP);
				scheduler.triggerJob(detail.getName(), detail.getGroup());
			} catch (SchedulerException e) {
				log.error("", e);
			}
		}
		return Response.ok().build();
	}
}