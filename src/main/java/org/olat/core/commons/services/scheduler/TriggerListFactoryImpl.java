package org.olat.core.commons.services.scheduler;

import org.quartz.Trigger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * This factory creates the list of triggers to be considered by the Quartz scheduler.
 * If a new trigger is created, it has to be added to this class.
 *
 * @author Martin Schraner
 */
@Component("triggerListFactoryImpl")
public class TriggerListFactoryImpl implements TriggerListFactory {

	private final CronTriggerFactoryBean notificationsEmailTrigger;
	private final CronTriggerFactoryBean adobeCleanupJob;
	private final CronTriggerFactoryBean updateStatisticsTrigger;
	private final CronTriggerFactoryBean searchIndexingTrigger;
	private final CronTriggerFactoryBean invitationCleanupTrigger;
	private final CronTriggerFactoryBean epDeadlineTrigger;
	private final CronTriggerFactoryBean restTokenTrigger;
	private final CronTriggerFactoryBean taskExecutorTrigger;
	private final CronTriggerFactoryBean procSamplerTrigger;
	private final CronTriggerFactoryBean systemSamplerTrigger;
	private final CronTriggerFactoryBean updateQtiResultsTriggerOnyx;
	private final SimpleTriggerFactoryBean acReservationCleanupJob;
	private final CronTriggerFactoryBean mapperSlayerTrigger;
	private final CronTriggerFactoryBean assessmentNotificationsTrigger;
	private final CronTriggerFactoryBean reminderTrigger;
	private final CronTriggerFactoryBean videoTranscodingTrigger;
	private final CronTriggerFactoryBean automaticLifecycleTrigger;
	private final CronTriggerFactoryBean courseCleanupTrigger;
	private final ActiveTrigger[] activeTriggers;

	@Autowired
	public TriggerListFactoryImpl(@Qualifier("sendNotificationsEmailTrigger") CronTriggerFactoryBean notificationsEmailTrigger,
								  @Qualifier("adobeCleanupJob") CronTriggerFactoryBean adobeCleanupJob,
								  @Qualifier("updateStatisticsTrigger") CronTriggerFactoryBean updateStatisticsTrigger,
								  @Qualifier("searchIndexingTrigger") CronTriggerFactoryBean searchIndexingTrigger,
								  @Qualifier("invitationCleanupTrigger") CronTriggerFactoryBean invitationCleanupTrigger,
								  @Qualifier("epDeadlineTrigger") CronTriggerFactoryBean epDeadlineTrigger,
								  @Qualifier("restTokenTrigger") CronTriggerFactoryBean restTokenTrigger,
								  @Qualifier("taskExecutorTrigger") CronTriggerFactoryBean taskExecutorTrigger,
								  @Qualifier("procSamplerTrigger") CronTriggerFactoryBean procSamplerTrigger,
								  @Qualifier("systemSamplerTrigger") CronTriggerFactoryBean systemSamplerTrigger,
								  @Qualifier("updateQtiResultsTriggerOnyx") CronTriggerFactoryBean updateQtiResultsTriggerOnyx,
								  @Qualifier("acReservationCleanupJob") SimpleTriggerFactoryBean acReservationCleanupJob,
								  @Qualifier("mapperSlayerTrigger") CronTriggerFactoryBean mapperSlayerTrigger,
								  @Qualifier("assessmentNotificationsTrigger") CronTriggerFactoryBean assessmentNotificationsTrigger,
								  @Qualifier("reminderTrigger") CronTriggerFactoryBean reminderTrigger,
								  @Qualifier("videoTranscodingTrigger") CronTriggerFactoryBean videoTranscodingTrigger,
								  @Qualifier("automaticLifecycleTrigger") CronTriggerFactoryBean automaticLifecycleTrigger,
								  @Qualifier("courseCleanupTrigger") CronTriggerFactoryBean courseCleanupTrigger,
								  ActiveTrigger[] activeTriggers) {
		this.notificationsEmailTrigger = notificationsEmailTrigger;
		this.adobeCleanupJob = adobeCleanupJob;
		this.updateStatisticsTrigger = updateStatisticsTrigger;
		this.searchIndexingTrigger = searchIndexingTrigger;
		this.invitationCleanupTrigger = invitationCleanupTrigger;
		this.epDeadlineTrigger = epDeadlineTrigger;
		this.restTokenTrigger = restTokenTrigger;
		this.taskExecutorTrigger = taskExecutorTrigger;
		this.procSamplerTrigger = procSamplerTrigger;
		this.systemSamplerTrigger = systemSamplerTrigger;
		this.updateQtiResultsTriggerOnyx = updateQtiResultsTriggerOnyx;
		this.acReservationCleanupJob = acReservationCleanupJob;
		this.mapperSlayerTrigger = mapperSlayerTrigger;
		this.assessmentNotificationsTrigger = assessmentNotificationsTrigger;
		this.reminderTrigger = reminderTrigger;
		this.videoTranscodingTrigger = videoTranscodingTrigger;
		this.automaticLifecycleTrigger = automaticLifecycleTrigger;
		this.courseCleanupTrigger = courseCleanupTrigger;
		this.activeTriggers = activeTriggers;
	}

	@Override
	public List<FactoryBean<? extends Trigger>> create() {
		List<FactoryBean<? extends Trigger>> triggerList = new ArrayList<>();
		// Include every bean that should be triggered
		triggerList.add(notificationsEmailTrigger);
		triggerList.add(adobeCleanupJob);
		triggerList.add(updateStatisticsTrigger);
		triggerList.add(searchIndexingTrigger);
		triggerList.add(invitationCleanupTrigger);
		triggerList.add(epDeadlineTrigger);
		triggerList.add(restTokenTrigger);
		triggerList.add(taskExecutorTrigger);
		triggerList.add(procSamplerTrigger);
		triggerList.add(systemSamplerTrigger);
		triggerList.add(updateQtiResultsTriggerOnyx);
		triggerList.add(acReservationCleanupJob);
		triggerList.add(mapperSlayerTrigger);
		triggerList.add(assessmentNotificationsTrigger);
		triggerList.add(reminderTrigger);
		triggerList.add(videoTranscodingTrigger);
		triggerList.add(adobeCleanupJob);
		triggerList.add(automaticLifecycleTrigger);
		triggerList.add(courseCleanupTrigger);

		// Add triggers implementing ActiveTrigger marker interface
		for (ActiveTrigger activeTrigger : activeTriggers) {
			if (activeTrigger instanceof Trigger) {
				triggerList.add(activeTrigger);
			}
		}

		return triggerList;
	}

	/**
	 * In order the event listener array is never null, one listener must
	 * exists. Therefore this listener is implemented as class.
	 */
	@Component
	public static class ActiveTriggerDummy extends ActiveTrigger {

		@Override
		public void afterPropertiesSet() throws ParseException {
		}
	}
}
