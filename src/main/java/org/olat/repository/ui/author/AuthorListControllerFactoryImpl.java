package org.olat.repository.ui.author;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Martin Schraner
 */
@Component
public class AuthorListControllerFactoryImpl implements AuthorListControllerFactory {

	private final DB dbInstance;
	private final UserManager userManager;
	private final MarkManager markManager;
	private final RepositoryModule repositoryModule;
	private final RepositoryService repositoryService;
	private final RepositoryManager repositoryManager;
	private final RepositoryHandlerFactory repositoryHandlerFactory;

	@Autowired
	public AuthorListControllerFactoryImpl(DB dbInstance,
										   UserManager userManager,
										   MarkManager markManager,
										   RepositoryModule repositoryModule,
										   RepositoryService repositoryService,
										   RepositoryManager repositoryManager,
										   RepositoryHandlerFactory repositoryHandlerFactory) {
		this.dbInstance = dbInstance;
		this.userManager = userManager;
		this.markManager = markManager;
		this.repositoryModule = repositoryModule;
		this.repositoryService = repositoryService;
		this.repositoryManager = repositoryManager;
		this.repositoryHandlerFactory = repositoryHandlerFactory;
	}

	@Override
	public AuthorListController create(UserRequest ureq,
									   WindowControl wControl,
									   String i18nName,
									   SearchAuthorRepositoryEntryViewParams searchParams,
									   boolean withSearch) {

		return new AuthorListController(
				ureq,
				wControl,
				i18nName,
				searchParams,
				withSearch,
				dbInstance,
				userManager,
				markManager,
				repositoryModule,
				repositoryService,
				repositoryManager,
				repositoryHandlerFactory);
	}
}
