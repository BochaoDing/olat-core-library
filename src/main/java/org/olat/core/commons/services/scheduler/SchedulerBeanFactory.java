package org.olat.core.commons.services.scheduler;

import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.List;
import java.util.Properties;

/**
 * @author Martin Schraner
 */
@Configuration
public class SchedulerBeanFactory {

	@Autowired
	private TriggerListFactory triggerListFactory;

	/**
	 * This tells spring to create the quartz scheduler
	 */
	@Bean
	@DependsOn("database")
	public SchedulerFactoryBean schedulerFactoryBean() {

		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

		schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);

		Properties quartzProperties = new Properties();
		quartzProperties.setProperty("org.quartz.threadPool.threadCount", "5");
		quartzProperties.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
		schedulerFactoryBean.setQuartzProperties(quartzProperties);

		// Add triggers
		List<Trigger> triggerList = triggerListFactory.create();
		Trigger[] triggers = triggerList.toArray(new Trigger[triggerList.size()]);
		schedulerFactoryBean.setTriggers(triggers);

		return schedulerFactoryBean;
	}

}
