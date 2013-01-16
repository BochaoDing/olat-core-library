/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.instantMessaging.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 20.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="imrosterentry")
@Table(name="o_im_roster_entry")
@NamedQueries({
	@NamedQuery(name="loadIMRosterEntryByIdentityandResource", query="select entry from imrosterentry entry where entry.identityKey=:identityKey and entry.resourceId=:resid and entry.resourceTypeName=:resname"),
	@NamedQuery(name="loadIMRosterEntryByResource", query="select entry from imrosterentry entry where entry.resourceId=:resid and entry.resourceTypeName=:resname"),
	@NamedQuery(name="clearIMRosterEntry", query="delete from imrosterentry entry"),
	@NamedQuery(name="deleteIMRosterEntryByIdentityAndResource", query="delete from imrosterentry entry where entry.identityKey=:identityKey and entry.resourceId=:resid and entry.resourceTypeName=:resname"),
})
public class RosterEntryImpl implements Persistable, CreateInfo {

	private static final long serialVersionUID = -4265724240924748369L;

	@Id
  @GeneratedValue(generator = "system-uuid")
  @GenericGenerator(name = "system-uuid", strategy = "hilo")
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="fk_identity_id", nullable=false, insertable=true, updatable=false)
	private Long identityKey;
	@Column(name="r_nickname", nullable=true, insertable=true, updatable=true)
	private String nickName;
	@Column(name="r_fullname", nullable=true, insertable=true, updatable=true)
	private String fullName;
	@Column(name="r_anonym", nullable=true, insertable=true, updatable=true)
	private boolean anonym;
	@Column(name="r_vip", nullable=true, insertable=true, updatable=false)
	private boolean vip;
	
	@Column(name="r_resname", nullable=false, insertable=true, updatable=false)
	private String resourceTypeName;
	@Column(name="r_resid", nullable=false, insertable=true, updatable=false)
	private Long resourceId;
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
		
	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public boolean isVip() {
		return vip;
	}

	public void setVip(boolean vip) {
		this.vip = vip;
	}

	public boolean isAnonym() {
		return anonym;
	}

	public void setAnonym(boolean anonym) {
		this.anonym = anonym;
	}
	
	public String getResourceTypeName() {
		return resourceTypeName;
	}

	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	@Override
	public int hashCode() {
		return key == null ? 92867 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof RosterEntryImpl) {
			RosterEntryImpl entry = (RosterEntryImpl)obj;
			return key != null && key.equals(entry.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {	
		return equals(persistable);
	}
}