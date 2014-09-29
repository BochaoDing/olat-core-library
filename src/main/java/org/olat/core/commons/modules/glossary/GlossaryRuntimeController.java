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
package org.olat.core.commons.modules.glossary;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.glossary.GlossaryEditSettingsController;
import org.olat.modules.glossary.GlossaryRegisterSettingsController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;

/**
 * 
 * Initial date: 15.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GlossaryRuntimeController extends RepositoryEntryRuntimeController {
	
	private Link registerLink, permissionLink;
	
	public GlossaryRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}

	@Override
	protected void initToolbar(Dropdown toolsDropdown, Dropdown settingsDropdown) {
		super.initToolbar(toolsDropdown, settingsDropdown);
		
		if (reSecurity.isEntryAdmin()) {
			registerLink = LinkFactory.createToolLink("register", translate("tab.glossary.register"), this, "o_sel_glossary_register");
			registerLink.setIconLeftCSS("o_icon o_icon_pageing o_icon-fw");
			settingsDropdown.addComponent(registerLink);
			
			permissionLink = LinkFactory.createToolLink("permissions", translate("tab.glossary.edit"), this, "o_sel_glossary_permission");
			permissionLink.setIconLeftCSS("o_icon o_icon_edit o_icon-fw");
			settingsDropdown.addComponent(permissionLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(registerLink == source) {
			doRegister(ureq);
		} else if(permissionLink == source) {
			doPermission(ureq);
		} else {
			super.event(ureq, source, event);
		}
	}
	
	private void doRegister(UserRequest ureq) {
		RepositoryEntry glossary = getRepositoryEntry();
		GlossaryRegisterSettingsController glossRegisterSetCtr
			= new GlossaryRegisterSettingsController(ureq, getWindowControl(), glossary.getOlatResource());
		pushController(ureq, translate("tab.glossary.register"), glossRegisterSetCtr);
		setActiveTool(registerLink);
	}
	
	private void doPermission(UserRequest ureq) {
		RepositoryEntry glossary = getRepositoryEntry();
		GlossaryEditSettingsController glossEditCtr
			= new GlossaryEditSettingsController(ureq, getWindowControl(), glossary.getOlatResource());
		pushController(ureq, translate("tab.glossary.edit"), glossEditCtr);
		setActiveTool(permissionLink);
	}
}
