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
package org.olat.repository.ui.author;

import org.olat.core.gui.control.Event;
import org.olat.core.id.context.StateEntry;

/**
 * 
 * Initial date: 02.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchEvent extends Event implements StateEntry {

	private static final long serialVersionUID = -1222660688926846838L;
	
	private String id;
	private String displayname;
	private String author;
	private String description;
	private String type;
	private boolean ownedResourcesOnly;
	
	public SearchEvent() {
		super("re-search");
	}

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayname() {
		return displayname;
	}

	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isOwnedResourcesOnly() {
		return ownedResourcesOnly;
	}

	public void setOwnedResourcesOnly(boolean ownedResourcesOnly) {
		this.ownedResourcesOnly = ownedResourcesOnly;
	}

	@Override
	public SearchEvent clone() {
		SearchEvent clone = new SearchEvent();
		clone.id = id;
		clone.displayname = displayname;
		clone.author = author;
		clone.description = description;
		clone.type = type;
		clone.ownedResourcesOnly = ownedResourcesOnly;
		return clone;
	}
}
