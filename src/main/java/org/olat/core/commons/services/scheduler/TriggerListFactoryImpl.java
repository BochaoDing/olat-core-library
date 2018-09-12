package org.olat.core.commons.services.scheduler;

import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

	private final CronTrigger notificationsEmailTrigger;
	private final CronTrigger adobeCleanupJob;
	private final CronTrigger updateStatisticsTrigger;
	private final CronTrigger searchIndexingTrigger;
	private final CronTrigger invitationCleanupTrigger;
	private final CronTrigger epDeadlineTrigger;
	private final CronTrigger restTokenTrigger;
	private final CronTrigger taskExecutorTrigger;
	private final CronTrigger procSamplerTrigger;
	private final CronTrigger systemSamplerTrigger;
	private final CronTrigger updateQtiResultsTriggerOnyx;
	private final SimpleTrigger acReservationCleanupJob;
	private final CronTrigger mapperSlayerTrigger;
	private final CronTrigger assessmentNotificationsTrigger;
	private final CronTrigger reminderTrigger;
	private final CronTrigger videoTranscodingTrigger;
	private final CronTrigger automaticLifecycleTrigger;
	private final CronTrigger courseCleanupTrigger;
	private final ActiveTrigger[] activeTriggers;

	@Autowired
	public TriggerListFactoryImpl(@Qualifier("sendNotificationsEmailTrigger") CronTrigger notificationsEmailTrigger,
								  @Qualifier("adobeCleanupJob") CronTrigger adobeCleanupJob,
								  @Qualifier("updateStatisticsTrigger") CronTrigger updateStatisticsTrigger,
								  @Qualifier("searchIndexingTrigger") CronTrigger searchIndexingTrigger,
								  @Qualifier("invitationCleanupTrigger") CronTrigger invitationCleanupTrigger,
								  @Qualifier("epDeadlineTrigger") CronTrigger epDeadlineTrigger,
								  @Qualifier("restTokenTrigger") CronTrigger restTokenTrigger,
								  @Qualifier("taskExecutorTrigger") CronTrigger taskExecutorTrigger,
								  @Qualifier("procSamplerTrigger") CronTrigger procSamplerTrigger,
								  @Qualifier("systemSamplerTrigger") CronTrigger systemSamplerTrigger,
								  @Qualifier("updateQtiResultsTriggerOnyx") CronTrigger updateQtiResultsTriggerOnyx,
								  @Qualifier("acReservationCleanupJob") SimpleTrigger acReservationCleanupJob,
								  @Qualifier("mapperSlayerTrigger") CronTrigger mapperSlayerTrigger,
								  @Qualifier("assessmentNotificationsTrigger") CronTrigger assessmentNotificationsTrigger,
								  @Qualifier("reminderTrigger") CronTrigger reminderTrigger,
								  @Qualifier("videoTranscodingTrigger") CronTrigger videoTranscodingTrigger,
								  @Qualifier("automaticLifecycleTrigger") CronTrigger automaticLifecycleTrigger,
								  @Qualifier("courseCleanupTrigger") CronTrigger courseCleanupTrigger,
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

		// Add active triggers
		for (ActiveTrigger activeTrigger : activeTriggers) {
			Trigger trigger = activeTrigger.getTrigger();
			if (trigger != null) {
				triggerList.add(trigger);
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

		@Override
		public Trigger getTrigger() {
			return null;
		}
	}
}
