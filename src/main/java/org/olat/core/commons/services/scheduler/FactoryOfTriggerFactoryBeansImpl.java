package org.olat.core.commons.services.scheduler;

import org.quartz.Trigger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This factory creates the list of trigger factory beans to be considered by the Quartz scheduler.
 * If a new trigger is created, it has to be added to this class.
 *
 * @author Martin Schraner
 */
@Component
public class FactoryOfTriggerFactoryBeansImpl implements FactoryOfTriggerFactoryBeans {

	private final CronTriggerFactoryBean notificationsEmailTriggerFactoryBean;
	private final CronTriggerFactoryBean adobeCleanupJobTriggerFactoryBean;
	private final CronTriggerFactoryBean updateStatisticsTriggerFactoryBean;
	private final CronTriggerFactoryBean searchIndexingTriggerFactoryBean;
	private final CronTriggerFactoryBean invitationCleanupTriggerFactoryBean;
	private final CronTriggerFactoryBean epDeadlineTriggerFactoryBean;
	private final CronTriggerFactoryBean restTokenTriggerFactoryBean;
	private final CronTriggerFactoryBean taskExecutorTriggerFactoryBean;
	private final CronTriggerFactoryBean procSamplerTriggerFactoryBean;
	private final CronTriggerFactoryBean systemSamplerTriggerFactoryBean;
	private final CronTriggerFactoryBean updateQtiResultsTriggerOnyxTriggerFactoryBean;
	private final SimpleTriggerFactoryBean acReservationCleanupJobTriggerFactoryBean;
	private final CronTriggerFactoryBean mapperSlayerTriggerFactoryBean;
	private final CronTriggerFactoryBean assessmentNotificationsTriggerFactoryBean;
	private final CronTriggerFactoryBean reminderTriggerFactoryBean;
	private final CronTriggerFactoryBean videoTranscodingTriggerFactoryBean;
	private final CronTriggerFactoryBean automaticLifecycleTriggerFactoryBean;
	private final CronTriggerFactoryBean courseCleanupTriggerFactoryBean;
	private final ActiveCronTriggerFactoryBean[] activeCronTriggerFactoryBeans;

	@Autowired
	public FactoryOfTriggerFactoryBeansImpl(@Qualifier("sendNotificationsEmailTrigger") CronTriggerFactoryBean notificationsEmailTriggerFactoryBean,
											@Qualifier("adobeCleanupJob") CronTriggerFactoryBean adobeCleanupJobTriggerFactoryBean,
											@Qualifier("updateStatisticsTrigger") CronTriggerFactoryBean updateStatisticsTriggerFactoryBean,
											@Qualifier("searchIndexingTrigger") CronTriggerFactoryBean searchIndexingTriggerFactoryBean,
											@Qualifier("invitationCleanupTrigger") CronTriggerFactoryBean invitationCleanupTriggerFactoryBean,
											@Qualifier("epDeadlineTrigger") CronTriggerFactoryBean epDeadlineTriggerFactoryBean,
											@Qualifier("restTokenTrigger") CronTriggerFactoryBean restTokenTriggerFactoryBean,
											@Qualifier("taskExecutorTrigger") CronTriggerFactoryBean taskExecutorTriggerFactoryBean,
											@Qualifier("procSamplerTrigger") CronTriggerFactoryBean procSamplerTriggerFactoryBean,
											@Qualifier("systemSamplerTrigger") CronTriggerFactoryBean systemSamplerTriggerFactoryBean,
											@Qualifier("updateQtiResultsTriggerOnyx") CronTriggerFactoryBean updateQtiResultsTriggerOnyxTriggerFactoryBean,
											@Qualifier("acReservationCleanupJob") SimpleTriggerFactoryBean acReservationCleanupJobTriggerFactoryBean,
											@Qualifier("mapperSlayerTrigger") CronTriggerFactoryBean mapperSlayerTriggerFactoryBean,
											@Qualifier("assessmentNotificationsTrigger") CronTriggerFactoryBean assessmentNotificationsTriggerFactoryBean,
											@Qualifier("reminderTrigger") CronTriggerFactoryBean reminderTriggerFactoryBean,
											@Qualifier("videoTranscodingTrigger") CronTriggerFactoryBean videoTranscodingTriggerFactoryBean,
											@Qualifier("automaticLifecycleTrigger") CronTriggerFactoryBean automaticLifecycleTriggerFactoryBean,
											@Qualifier("courseCleanupTrigger") CronTriggerFactoryBean courseCleanupTriggerFactoryBean,
											ActiveCronTriggerFactoryBean[] activeCronTriggerFactoryBeans) {
		this.notificationsEmailTriggerFactoryBean = notificationsEmailTriggerFactoryBean;
		this.adobeCleanupJobTriggerFactoryBean = adobeCleanupJobTriggerFactoryBean;
		this.updateStatisticsTriggerFactoryBean = updateStatisticsTriggerFactoryBean;
		this.searchIndexingTriggerFactoryBean = searchIndexingTriggerFactoryBean;
		this.invitationCleanupTriggerFactoryBean = invitationCleanupTriggerFactoryBean;
		this.epDeadlineTriggerFactoryBean = epDeadlineTriggerFactoryBean;
		this.restTokenTriggerFactoryBean = restTokenTriggerFactoryBean;
		this.taskExecutorTriggerFactoryBean = taskExecutorTriggerFactoryBean;
		this.procSamplerTriggerFactoryBean = procSamplerTriggerFactoryBean;
		this.systemSamplerTriggerFactoryBean = systemSamplerTriggerFactoryBean;
		this.updateQtiResultsTriggerOnyxTriggerFactoryBean = updateQtiResultsTriggerOnyxTriggerFactoryBean;
		this.acReservationCleanupJobTriggerFactoryBean = acReservationCleanupJobTriggerFactoryBean;
		this.mapperSlayerTriggerFactoryBean = mapperSlayerTriggerFactoryBean;
		this.assessmentNotificationsTriggerFactoryBean = assessmentNotificationsTriggerFactoryBean;
		this.reminderTriggerFactoryBean = reminderTriggerFactoryBean;
		this.videoTranscodingTriggerFactoryBean = videoTranscodingTriggerFactoryBean;
		this.automaticLifecycleTriggerFactoryBean = automaticLifecycleTriggerFactoryBean;
		this.courseCleanupTriggerFactoryBean = courseCleanupTriggerFactoryBean;
		this.activeCronTriggerFactoryBeans = activeCronTriggerFactoryBeans;
	}

	@Override
	public List<FactoryBean<? extends Trigger>> create() {
		List<FactoryBean<? extends Trigger>> triggerFactoryBeans = new ArrayList<>();
		// Include every bean that should be triggered
		triggerFactoryBeans.add(notificationsEmailTriggerFactoryBean);
		triggerFactoryBeans.add(adobeCleanupJobTriggerFactoryBean);
		triggerFactoryBeans.add(updateStatisticsTriggerFactoryBean);
		triggerFactoryBeans.add(searchIndexingTriggerFactoryBean);
		triggerFactoryBeans.add(invitationCleanupTriggerFactoryBean);
		triggerFactoryBeans.add(epDeadlineTriggerFactoryBean);
		triggerFactoryBeans.add(restTokenTriggerFactoryBean);
		triggerFactoryBeans.add(taskExecutorTriggerFactoryBean);
		triggerFactoryBeans.add(procSamplerTriggerFactoryBean);
		triggerFactoryBeans.add(systemSamplerTriggerFactoryBean);
		triggerFactoryBeans.add(updateQtiResultsTriggerOnyxTriggerFactoryBean);
		triggerFactoryBeans.add(acReservationCleanupJobTriggerFactoryBean);
		triggerFactoryBeans.add(mapperSlayerTriggerFactoryBean);
		triggerFactoryBeans.add(assessmentNotificationsTriggerFactoryBean);
		triggerFactoryBeans.add(reminderTriggerFactoryBean);
		triggerFactoryBeans.add(videoTranscodingTriggerFactoryBean);
		triggerFactoryBeans.add(adobeCleanupJobTriggerFactoryBean);
		triggerFactoryBeans.add(automaticLifecycleTriggerFactoryBean);
		triggerFactoryBeans.add(courseCleanupTriggerFactoryBean);

		// Add triggers extending from ActiveCronTriggerFactoryBean
		triggerFactoryBeans.addAll(Arrays.asList(activeCronTriggerFactoryBeans));

		return triggerFactoryBeans;
	}

	/**
	 * In order the event listener array is never null, one listener must
	 * exists. Therefore this listener is implemented as class.
	 */
	@Component
	public static class ActiveCronTriggerFactoryBeanDummy extends ActiveCronTriggerFactoryBean {

		@Override
		public void afterPropertiesSet() {
		}
	}
}
