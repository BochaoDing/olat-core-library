package org.olat.core.commons.services.scheduler;

import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.SimpleTriggerBean;
import org.springframework.stereotype.Component;

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

	private final CronTriggerBean notificationsEmailTrigger;
	private final CronTriggerBean adobeCleanupJob;
	private final CronTriggerBean updateStatisticsTrigger;
	private final CronTriggerBean searchIndexingTrigger;
	private final CronTriggerBean invitationCleanupTrigger;
	private final CronTriggerBean epDeadlineTrigger;
	private final CronTriggerBean restTokenTrigger;
	private final CronTriggerBean taskExecutorTrigger;
	private final CronTriggerBean procSamplerTrigger;
	private final CronTriggerBean systemSamplerTrigger;
	private final CronTriggerBean updateQtiResultsTriggerOnyx;
	private final SimpleTriggerBean acReservationCleanupJob;
	private final CronTriggerBean mapperSlayerTrigger;
	private final CronTriggerBean assessmentNotificationsTrigger;
	private final CronTriggerBean reminderTrigger;
	private final CronTriggerBean videoTranscodingTrigger;
	private final CronTriggerBean automaticLifecycleTrigger;
	private final CronTriggerBean courseCleanupTrigger;
	private final ActiveTrigger[] activeTriggers;

	@Autowired
	public TriggerListFactoryImpl(@Qualifier("sendNotificationsEmailTrigger") CronTriggerBean notificationsEmailTrigger,
								  @Qualifier("adobeCleanupJob") CronTriggerBean adobeCleanupJob,
								  @Qualifier("updateStatisticsTrigger") CronTriggerBean updateStatisticsTrigger,
								  @Qualifier("searchIndexingTrigger") CronTriggerBean searchIndexingTrigger,
								  @Qualifier("invitationCleanupTrigger") CronTriggerBean invitationCleanupTrigger,
								  @Qualifier("epDeadlineTrigger") CronTriggerBean epDeadlineTrigger,
								  @Qualifier("restTokenTrigger") CronTriggerBean restTokenTrigger,
								  @Qualifier("taskExecutorTrigger") CronTriggerBean taskExecutorTrigger,
								  @Qualifier("procSamplerTrigger") CronTriggerBean procSamplerTrigger,
								  @Qualifier("systemSamplerTrigger") CronTriggerBean systemSamplerTrigger,
								  @Qualifier("updateQtiResultsTriggerOnyx") CronTriggerBean updateQtiResultsTriggerOnyx,
								  @Qualifier("acReservationCleanupJob") SimpleTriggerBean acReservationCleanupJob,
								  @Qualifier("mapperSlayerTrigger") CronTriggerBean mapperSlayerTrigger,
								  @Qualifier("assessmentNotificationsTrigger") CronTriggerBean assessmentNotificationsTrigger,
								  @Qualifier("reminderTrigger") CronTriggerBean reminderTrigger,
								  @Qualifier("videoTranscodingTrigger") CronTriggerBean videoTranscodingTrigger,
								  @Qualifier("automaticLifecycleTrigger") CronTriggerBean automaticLifecycleTrigger,
								  @Qualifier("courseCleanupTrigger") CronTriggerBean courseCleanupTrigger,
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
	public List<Trigger> create() {
		List<Trigger> triggerList = new ArrayList<>();

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
				triggerList.add((Trigger) activeTrigger);
			}
		}

		return triggerList;
	}

	/**
	 * In order the event listener array is never null, one listener must
	 * exists. Therefore this listener is implemented as class.
	 */
	@Component
	public static class ActiveTriggerDummy implements ActiveTrigger {
	}
}
