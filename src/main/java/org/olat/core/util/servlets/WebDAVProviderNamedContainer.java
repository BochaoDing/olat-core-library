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
package org.olat.core.util.servlets;

import org.olat.core.id.Identity;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * Lazy loading of the sub folders
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class WebDAVProviderNamedContainer extends NamedContainerImpl {
	
	private Identity identity;
	private final WebDAVProvider provider;
	private VFSContainer parentContainer;
	
	public WebDAVProviderNamedContainer(Identity identity, WebDAVProvider provider) {
		super(provider.getMountPoint(), null);
		this.provider = provider;
		this.identity = identity;
	}

	@Override
	public VFSContainer getDelegate() {
		if(super.getDelegate() == null) {
			setDelegate(provider.getContainer(identity));
			if(parentContainer != null) {
				super.setParentContainer(parentContainer);
				parentContainer = null;
			}
			identity = null;
		}
		return super.getDelegate();
	}

	@Override
	public VFSItemFilter getDefaultItemFilter() {
		return null;
	}

	@Override
	public void setParentContainer(VFSContainer parentContainer) {
		if(super.getDelegate() == null) {
			this.parentContainer = parentContainer;
		} else {
			super.setParentContainer(parentContainer);
		}
	}
}