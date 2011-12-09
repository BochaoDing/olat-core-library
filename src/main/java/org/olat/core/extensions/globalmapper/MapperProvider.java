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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.extensions.globalmapper;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.extensions.ExtensionElement;

/**
 * Description:<br>
 * Initial Date:  02.08.2005 <br>
 * @author Felix
 */
public interface MapperProvider extends ExtensionElement {
	/**
	 * @return the mapper this extension provides
	 */
	public Mapper getMapper();

	/**
	 * @param path
	 */
	public void setMapPath(String path);
}
