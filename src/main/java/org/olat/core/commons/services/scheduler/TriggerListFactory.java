package org.olat.core.commons.services.scheduler;

import org.quartz.Trigger;
import org.springframework.beans.factory.FactoryBean;

import java.util.List;

/**
 * @author Martin Schraner
 */
public interface TriggerListFactory {

	List<FactoryBean<? extends Trigger>> create();
}
