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
package org.olat.modules.reminder;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.reminder.model.SendTime;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 01.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ReminderModule extends AbstractSpringModule {
	
	private static final OLog log = Tracing.createLoggerFor(ReminderModule.class);
	
	private static final String REMINDER_ENABLED = "remiNder.enabled";
	private static final String SMS_ENABLED = "sms.enabled";
	private static final String SEND_TIME = "default.send.time";
	private static final String SEND_TIMEZONE = "default.send.timezone";
	
	@Value("${reminders.enabled:true}")
	private boolean enabled;
	@Value("${reminders.sms.enabled:false}")
	private boolean smsEnabled;
	
	@Value("${reminders.default.send.time:9}")
	private String defaultSendTime;
	@Value("${reminders.default.send.timezone:server}")
	private String defaultSendTimeZone;
	
	@Autowired
	private List<RuleSPI> ruleSpies;
	@Autowired
	private Scheduler scheduler;
	
	@Autowired
	public ReminderModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(REMINDER_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String enabledSMSObj = getStringPropertyValue(SMS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledSMSObj)) {
			smsEnabled = "true".equals(enabledSMSObj);
		}
		
		String sendTimeObj = getStringPropertyValue(SEND_TIME, true);
		if(StringHelper.containsNonWhitespace(sendTimeObj)) {
			defaultSendTime = sendTimeObj;
		}
		
		String sendTimezoneObj = getStringPropertyValue(SEND_TIMEZONE, true);
		if(StringHelper.containsNonWhitespace(sendTimezoneObj)) {
			defaultSendTimeZone = sendTimezoneObj;
		} else if("server".equals(defaultSendTimeZone)) {
			defaultSendTimeZone = TimeZone.getDefault().getID();
		}
		
		configureQuartzJob();
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	public List<RuleSPI> getRuleSPIList() {
		return new ArrayList<>(ruleSpies);
	}
	
	public RuleSPI getRuleSPIByType(String type) {
		RuleSPI selectedSpi = null;
		for(RuleSPI ruleSpy: ruleSpies) {
			if(ruleSpy.getClass().getSimpleName().equals(type)) {
				selectedSpi = ruleSpy;
				break;
			}
		}
		return selectedSpi;
	}
	/**
	 * Default 0 0 9 * * ?
	 * 
	 */
	private void configureQuartzJob() {
		try {
			Trigger trigger = scheduler.getTrigger("reminderTrigger", Scheduler.DEFAULT_GROUP);
			if(trigger instanceof CronTrigger) {
				CronTrigger cronTrigger = (CronTrigger)trigger;
				String currentCronExpression = cronTrigger.getCronExpression();
				String cronExpression = getCronExpression();
				if(!cronExpression.equals(currentCronExpression)) {
					cronTrigger.setCronExpression(cronExpression);
					scheduler.rescheduleJob("reminderTrigger", Scheduler.DEFAULT_GROUP, (Trigger)cronTrigger.clone());
				}
			}
		} catch (SchedulerException | ParseException e) {
			log.error("", e);
		}
	}
	
	private String getCronExpression() {
		StringBuilder sb = new StringBuilder();
		int hour = 9;
		int minute = 0;
		
		SendTime parsedTime = SendTime.parse(getDefaultSendTime());
		if(parsedTime.isValid()) {
			hour = parsedTime.getHour();
			minute = parsedTime.getMinute();
		}

		sb.append("0 ").append(minute).append(" ").append(hour).append(" * * ?");
		return sb.toString();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(REMINDER_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isSmsEnabled() {
		return smsEnabled;
	}

	public void setSmsEnabled(boolean smsEnabled) {
		this.smsEnabled = smsEnabled;
		setStringProperty(SMS_ENABLED, Boolean.toString(smsEnabled), true);
	}

	public String getDefaultSendTime() {
		return defaultSendTime;
	}

	public void setDefaultSendTime(String defaultSendTime) {
		this.defaultSendTime = defaultSendTime;
		setStringProperty(SEND_TIME, defaultSendTime, true);
	}
	
	public TimeZone getDefaultSendTimeZone() {
		TimeZone timeZone;
		if("server".equals(defaultSendTimeZone)) {
			timeZone = TimeZone.getDefault();
		} else if(StringHelper.containsNonWhitespace(defaultSendTimeZone)) {
			timeZone = TimeZone.getTimeZone(defaultSendTimeZone);
		} else {
			timeZone = TimeZone.getDefault();
		}
		return timeZone;
	}

	public void setDefaultSendTimeZone(TimeZone timeZone) {
		this.defaultSendTimeZone = timeZone.getID();
		setStringProperty(SEND_TIMEZONE, defaultSendTimeZone, true);
	}
}