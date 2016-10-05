package org.olat.repository.listener;

import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * In order the event listener array is never null, one listener must exist.
 * Therefore this listener is implemented as class.
 *
 * Initial date: 2016-06-15<br />
 * @author sev26 (UZH)
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AfterRepositoryEntryDeletionListener {

	/**
	 * This method should never commit the running database transaction.
	 */
	public void onAction(RepositoryEntry repositoryEntry,
						 OLATResource resource) {
	}
}
