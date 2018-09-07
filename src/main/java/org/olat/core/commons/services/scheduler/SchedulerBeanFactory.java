package org.olat.core.commons.services.scheduler;

import org.quartz.Trigger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
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

	private final FactoryOfTriggerFactoryBeans factoryOfTriggerFactoryBeans;

	@Autowired
	public SchedulerBeanFactory(FactoryOfTriggerFactoryBeans factoryOfTriggerFactoryBeans) {
		this.factoryOfTriggerFactoryBeans = factoryOfTriggerFactoryBeans;
	}

	/**
	 * This tells spring to create the quartz scheduler
	 */
	@Bean
	@DependsOn("database")
	public SchedulerFactoryBean schedulerFactoryBean() throws Exception {
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

		schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);

		Properties quartzProperties = new Properties();
		quartzProperties.setProperty("org.quartz.threadPool.threadCount", "5");
		quartzProperties.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
		schedulerFactoryBean.setQuartzProperties(quartzProperties);

		// Add triggers
		List<FactoryBean<? extends Trigger>> triggerFactoryBeans = factoryOfTriggerFactoryBeans.create();
		Trigger[] triggers = new Trigger[triggerFactoryBeans.size()];
		for (int i = 0; i < triggerFactoryBeans.size(); i++) {
			triggers[i] = triggerFactoryBeans.get(i).getObject();
		}
		schedulerFactoryBean.setTriggers(triggers);

		return schedulerFactoryBean;
	}
}
