package org.olat.repository.ui.author;

import org.olat.core.util.event.MultiUserEvent;

/**
 * Initial date: 2016-07-01<br />
 * @author sev26 (UZH)
 */
public class AuthoringListChangeEvent extends MultiUserEvent {

	public AuthoringListChangeEvent(String command) {
		super(command);
	}
}
