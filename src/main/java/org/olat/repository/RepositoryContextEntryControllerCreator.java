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
package org.olat.repository;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;

/**
 * <h3>Description:</h3>
 * <p>
 * This class can create run controllers for repository entries in the given
 * context
 * <p>
 * Initial Date: 19.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class RepositoryContextEntryControllerCreator extends DefaultContextEntryControllerCreator {

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#createController(org.olat.core.id.context.ContextEntry,
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public Controller createController(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		OLATResourceable ores = ce.getOLATResourceable();

		RepositoryManager repom = RepositoryManager.getInstance();
		RepositoryEntry re = repom.lookupRepositoryEntry(ores.getResourceableId());
		Controller ctrl = RepositoyUIFactory.createLaunchController(re, null, ureq, wControl);
		return ctrl;

	}

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#getTabName(org.olat.core.id.context.ContextEntry)
	 */
	@Override
	public String getTabName(ContextEntry ce, UserRequest ureq) {
		OLATResourceable ores = ce.getOLATResourceable();

		RepositoryManager repom = RepositoryManager.getInstance();
		RepositoryEntry re = repom.lookupRepositoryEntry(ores.getResourceableId());
		return re.getDisplayname();
	}
	
	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#getSiteClassName(org.olat.core.id.context.ContextEntry)
	 */
	@Override
	public String getSiteClassName(ContextEntry ce, UserRequest ureq) {
		return null;
	}

	@Override
	public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		OLATResourceable ores = ce.getOLATResourceable();
		RepositoryManager repom = RepositoryManager.getInstance();
		RepositoryEntry re = repom.lookupRepositoryEntry(ores.getResourceableId());
		return re != null;
	}	

}
