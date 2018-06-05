package org.olat.repository.ui.author;

import org.olat.repository.RepositoryEntryAuthorView;
import org.springframework.stereotype.Component;

/**
 * @author Martin Schraner
 */
@Component
public class AuthoringEntryRowFactoryImpl implements AuthoringEntryRowFactory {

	@Override
	public AuthoringEntryRow create(RepositoryEntryAuthorView view, String fullnameAuthor) {
		return new AuthoringEntryRow(view, fullnameAuthor);
	}
}
