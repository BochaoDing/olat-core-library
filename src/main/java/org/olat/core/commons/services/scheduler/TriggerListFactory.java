package org.olat.core.commons.services.scheduler;

import org.quartz.Trigger;

import java.util.List;

/**
 * @author Martin Schraner
 */
public interface TriggerListFactory {

	List<Trigger> create();
}
