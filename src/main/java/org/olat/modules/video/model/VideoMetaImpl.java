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
package org.olat.modules.video.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.video.VideoMeta;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * The Class VideoMetaImpl.
 * Initial Date: January 2017
 * @author fkiefer fabian.kiefer@frentix.com
 */
@Entity(name="videometadata")
@Table(name="o_vid_metadata")
public class VideoMetaImpl implements VideoMeta, Persistable, ModifiedInfo {

	private static final long serialVersionUID = 8360426958L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@OneToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_resource_id", nullable=false, insertable=true, updatable=false)
	private OLATResource videoResource;
	
	@Column(name="vid_width", nullable=true, insertable=true, updatable=true)
	private int width;
	@Column(name="vid_height", nullable=true, insertable=true, updatable=true)
	private int height;
	@Column(name="vid_size", nullable=true, insertable=true, updatable=true)
	private long size;
	@Column(name="vid_format", nullable=true, insertable=true, updatable=true)
	private String format;
	@Column(name="vid_length", nullable=true, insertable=true, updatable=true)
	private String length;	
	


	public VideoMetaImpl(OLATResource videoResource, int width, int height, long size, String format, String length) {
		super();
		this.creationDate = new Date();
		this.lastModified = new Date();
		this.videoResource = videoResource;
		this.width = width;
		this.height = height;
		this.size = size;
		this.format = format;
		this.length = length;
	}
	
	public VideoMetaImpl(int width, int height, long size) {
		this.width = width;
		this.height = height;
		this.size = size;
	}	
	
	public VideoMetaImpl() {
		// make JAXB happy
	}
	
	@Override
	public void setVideoResource(OLATResource videoResource) {
		this.videoResource = videoResource;
	}

	@Override 
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return false;
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public OLATResource getVideoResource() {
		return videoResource;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public void setSize(long size) {
		this.size = size;
	}

	@Override
	public String getFormat() {
		return format;
	}

	@Override
	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public String getLength() {
		return length;
	}

	@Override
	public void setLength(String length) {
		this.length = length;
	}


}
