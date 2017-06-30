package org.olat.repository.ui.list;

import org.olat.repository.RepositoryEntryMyView;

import java.util.List;
import java.util.Map;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
public interface RepositoryEntryRowsFactory {

	Map<RepositoryEntryMyView, RepositoryEntryRow> create(List<RepositoryEntryMyView> repositoryEntryViews);

	RepositoryEntryDataSourceUIFactory getUiFactory();
}
