package org.olat.core.commons.services.scheduler;

import org.springframework.scheduling.quartz.CronTriggerFactoryBean;

/**
 * Marker interface.
 *
 * Cron trigger factory beans should extend from this class such that they are added to the list of active cron trigger
 * factory beans by the @see FactoryOfTriggerFactoryBeans.
 *
 * @author Martin Schraner
 */
public abstract class ActiveCronTriggerFactoryBean extends CronTriggerFactoryBean {
}
