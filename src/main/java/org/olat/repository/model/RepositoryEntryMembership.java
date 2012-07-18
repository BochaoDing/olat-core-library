package org.olat.repository.model;

import org.olat.core.commons.persistence.PersistentObject;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryEntryMembership extends PersistentObject {

	private static final long serialVersionUID = 3795108974268603941L;
	
	private Long repoParticipantKey;
	private Long repoTutorKey;
	private Long repoOwnerKey;
	private Long groupParticipantKey;
	private Long groupOwnerKey;
	
	private int access;
	private boolean membersOnly;

	public Long getRepoParticipantKey() {
		return repoParticipantKey;
	}
	
	public void setRepoParticipantKey(Long repoParticipantKey) {
		this.repoParticipantKey = repoParticipantKey;
	}
	
	public Long getRepoTutorKey() {
		return repoTutorKey;
	}

	public void setRepoTutorKey(Long repoTutorKey) {
		this.repoTutorKey = repoTutorKey;
	}

	public Long getRepoOwnerKey() {
		return repoOwnerKey;
	}
	
	public void setRepoOwnerKey(Long repoOwnerKey) {
		this.repoOwnerKey = repoOwnerKey;
	}
	
	public Long getGroupParticipantKey() {
		return groupParticipantKey;
	}
	
	public void setGroupParticipantKey(Long groupParticipantKey) {
		this.groupParticipantKey = groupParticipantKey;
	}
	
	public Long getGroupOwnerKey() {
		return groupOwnerKey;
	}
	
	public void setGroupOwnerKey(Long groupOwnerKey) {
		this.groupOwnerKey = groupOwnerKey;
	}

	public int getAccess() {
		return access;
	}

	public void setAccess(int access) {
		this.access = access;
	}

	public boolean isMembersOnly() {
		return membersOnly;
	}

	public void setMembersOnly(boolean membersOnly) {
		this.membersOnly = membersOnly;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 3768 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		return false;
	}
}
