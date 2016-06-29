package org.olat.repository.ui.list;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.gui.UserRequest;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
public class RepositoryEntryRowFactory {

	final RepositoryEntryDataSourceUIFactory uifactory;

	private final RepositoryManager repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);

	public RepositoryEntryRowFactory(RepositoryModule repositoryModule,
									 MapperService mapperService,
									 UserRequest userRequest) {
		uifactory = new RepositoryEntryDataSourceUIFactory(repositoryModule,
				mapperService, userRequest);
	}

	public RepositoryEntryRow create(RepositoryEntryMyView entry) {
		RepositoryEntryRow row = new RepositoryEntryRow(entry);

		/**
		 * TODO sev26
		 * The comment of
		 * {@link RepositoryEntryDataSourceUIFactory#forgeLinks(RepositoryEntryRow)}
		 * applies here as well.
		 */
		VFSLeaf image = repositoryManager.getImage(entry);
		if (image != null) {
			row.setThumbnailRelPath(uifactory.getMapperThumbnailUrl() + "/" + image.getName());
		}

		uifactory.forgeLinks(row);
		return row;
	}
}
