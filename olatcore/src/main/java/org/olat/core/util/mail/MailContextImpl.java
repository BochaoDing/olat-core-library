/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.core.util.mail;

import org.olat.core.id.OLATResourceable;


/**
 * 
 * Description:<br>
 * A default implementation of the mail context
 * 
 * <P>
 * Initial Date:  30 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailContextImpl implements MailContext {
	
	private String resourseableTypeName;
	private Long resourceableId;
	private String resSubPath;
	private String businessPath;
	
	public MailContextImpl() {
		//
	}
	
	public MailContextImpl(OLATResourceable ores) {
		this(ores, null, null);
	}
	
	public MailContextImpl(String businessPath) {
		this(null, null, businessPath);
	}
	
	public MailContextImpl(OLATResourceable ores, String resSubPath, String businessPath) {
		setOLATResourceable(ores);
		this.resSubPath = resSubPath;
		this.businessPath = businessPath;
	}
	
	@Override
	public OLATResourceable getOLATResourceable() {
		final Long id = resourceableId;
		final String name = resourseableTypeName;
		if(id == null || name == null) return null;
		
		return new OLATResourceable() {
			@Override
			public Long getResourceableId() {
				return id;
			}

			@Override
			public String getResourceableTypeName() {
				return name;
			}
		};
	}
	
	public void setOLATResourceable(OLATResourceable ores) {
		if(ores != null) {
			resourseableTypeName = ores.getResourceableTypeName();
			resourceableId = ores.getResourceableId();
		}
	}

	@Override
	public String getResSubPath() {
		return resSubPath;
	}

	public void setResSubPath(String resSubPath) {
		this.resSubPath = resSubPath;
	}

	@Override
	public String getBusinessPath() {
		return businessPath;
	}
	
	public void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}

	public String getResourseableTypeName() {
		return resourseableTypeName;
	}

	public void setResourseableTypeName(String resourseableTypeName) {
		this.resourseableTypeName = resourseableTypeName;
	}

	public Long getResourceableId() {
		return resourceableId;
	}

	public void setResourceableId(Long resourceableId) {
		this.resourceableId = resourceableId;
	}
}
