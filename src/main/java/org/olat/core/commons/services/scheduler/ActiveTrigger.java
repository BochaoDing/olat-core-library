package org.olat.core.commons.services.scheduler;

import org.springframework.scheduling.quartz.CronTriggerFactoryBean;

/**
 * Marker interface.
 *
 * Triggers should implement this interface such that they are added to the list of active triggers by the
 * @see org.olat.core.commons.services.scheduler.TriggerListFactory .
 *
 * @author Martin Schraner
 */
public abstract class ActiveTrigger extends CronTriggerFactoryBean {
}
