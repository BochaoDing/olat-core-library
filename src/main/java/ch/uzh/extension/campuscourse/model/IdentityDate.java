package ch.uzh.extension.campuscourse.model;

import org.olat.core.id.Identity;

import java.util.Date;

/**
 * @author Martin Schraner
 */
public class IdentityDate {

	private final Identity identity;
	private final Date date;

	public IdentityDate(Identity identity, Date date) {
		this.identity = identity;
		this.date = date;
	}

	public Identity getIdentity() {
		return identity;
	}

	public Date getDate() {
		return date;
	}
}
