package org.olat.repository.ui.list;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.gui.UserRequest;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
public class RepositoryEntryRowFactoryImpl extends RepositoryEntryRowFactory {

	public RepositoryEntryRowFactoryImpl(RepositoryManager repositoryManager,
										 RepositoryModule repositoryModule,
										 MapperService mapperService,
										 UserRequest userRequest) {
		super(repositoryManager, repositoryModule, mapperService, userRequest);
	}

	@Override
	public RepositoryEntryRow createRepositoryEntryRow(RepositoryEntryMyView entry) {
		return new RepositoryEntryRow();
	}
}
