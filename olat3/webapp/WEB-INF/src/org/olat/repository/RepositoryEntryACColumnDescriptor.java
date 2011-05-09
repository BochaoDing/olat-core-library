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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.repository;

import java.util.Collection;
import java.util.Locale;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for RepositoryEntryACColumnDescriptor
 * 
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse
 */
//fxdiff VCRP-1,2: access control of resources
public class RepositoryEntryACColumnDescriptor implements CustomCellRenderer {

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if(val instanceof Collection) {
			Collection<String> accessTypes = (Collection<String>)val;
			for(String accessType:accessTypes) {
				sb.append("<span class='b_small_icon ").append(accessType).append("_icon'>").append("</span>");
			}
		} else if(val instanceof Boolean) {
			boolean acessControlled = ((Boolean)val).booleanValue();
			if(acessControlled) {
				sb.append("<span class='b_small_icon b_group_accesscontrolled'>").append("</span>");
			}
		}
	}

}
