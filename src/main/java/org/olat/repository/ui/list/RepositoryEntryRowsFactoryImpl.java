package org.olat.repository.ui.list;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.gui.UserRequest;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
public class RepositoryEntryRowsFactoryImpl implements RepositoryEntryRowsFactory {

	private final RepositoryEntryDataSourceUIFactory uifactory;
	private final RepositoryManager repositoryManager;

	public RepositoryEntryRowsFactoryImpl(RepositoryManager repositoryManager,
								   RepositoryModule repositoryModule,
								   MapperService mapperService,
								   UserRequest userRequest) {
		this.repositoryManager = repositoryManager;
		this.uifactory = new RepositoryEntryDataSourceUIFactory(repositoryModule, mapperService, userRequest);
	}

	@Override
	public Map<RepositoryEntryMyView, RepositoryEntryRow> create(List<RepositoryEntryMyView> repositoryEntryMyViews) {

		Map<RepositoryEntryMyView, RepositoryEntryRow> mapOfRepositoryEntryMyViewsAndRepositoryEntryRows = new HashMap<>();

		for (RepositoryEntryMyView repositoryEntryMyView : repositoryEntryMyViews) {

			RepositoryEntryRow repositoryEntryRow = new RepositoryEntryRow();
			/*
			 * TODO sev26
			 * The comment of
			 * {@link RepositoryEntryDataSourceUIFactory#forgeLinks(RepositoryEntryRow)}
			 * applies here as well.
			 */
			forgeRepositoryEntryRow(repositoryEntryMyView, repositoryEntryRow);

			mapOfRepositoryEntryMyViewsAndRepositoryEntryRows.put(repositoryEntryMyView, repositoryEntryRow);
		}
		return mapOfRepositoryEntryMyViewsAndRepositoryEntryRows;
	}

	protected void forgeRepositoryEntryRow(RepositoryEntryMyView repositoryEntryMyView,
										 RepositoryEntryRow repositoryEntryRow) {

		VFSLeaf image = repositoryManager.getImage(repositoryEntryMyView);
		if (image != null) {
			repositoryEntryRow.setThumbnailRelPath(uifactory.getMapperThumbnailUrl() + "/" + image.getName());
		}

		uifactory.forgeLinks(repositoryEntryRow);
	}

	public RepositoryEntryDataSourceUIFactory getUiFactory() {
		return uifactory;
	}
}
