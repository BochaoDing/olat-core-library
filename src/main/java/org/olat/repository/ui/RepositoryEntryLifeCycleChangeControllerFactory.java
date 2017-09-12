package org.olat.repository.ui;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.model.RepositoryEntrySecurity;

/**
 * @author Martin Schraner
 */
public class RepositoryEntryLifeCycleChangeControllerFactory {

	private final DB dbInstance;
	private final RepositoryService repositoryService;
	private final RepositoryManager repositoryManager;
	private final RepositoryModule repositoryModule;
	private final UserRequest userRequest;
	private final WindowControl windowControl;
	private final RepositoryEntrySecurity repositoryEntrySecurity;
	private final RepositoryHandler repositoryHandler;

	public RepositoryEntryLifeCycleChangeControllerFactory(DB dbInstance,
														   RepositoryService repositoryService,
														   RepositoryManager repositoryManager,
														   RepositoryModule repositoryModule,
														   UserRequest userRequest,
														   WindowControl windowControl,
														   RepositoryEntrySecurity repositoryEntrySecurity,
														   RepositoryHandler repositoryHandler) {
		this.dbInstance = dbInstance;
		this.repositoryService = repositoryService;
		this.repositoryManager = repositoryManager;
		this.repositoryModule = repositoryModule;
		this.userRequest = userRequest;
		this.windowControl = windowControl;
		this.repositoryEntrySecurity = repositoryEntrySecurity;
		this.repositoryHandler = repositoryHandler;
	}

	public RepositoryEntryLifeCycleChangeController create(RepositoryEntry repositoryEntry) {

		return new RepositoryEntryLifeCycleChangeController(
				dbInstance,
				repositoryService,
				repositoryManager,
				repositoryModule,
				userRequest,
				windowControl,
				repositoryEntry,
				repositoryEntrySecurity,
				repositoryHandler,
				Util.createPackageTranslator(RepositoryService.class, userRequest.getLocale(), null));
	}
}
