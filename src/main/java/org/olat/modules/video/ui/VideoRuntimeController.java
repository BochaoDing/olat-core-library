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
package org.olat.modules.video.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.RootEvent;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;

/**
 *
 * The runtime add configuration management and delivery options.
 *
 * Initial date: 01.04.2015<br>
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoRuntimeController extends RepositoryEntryRuntimeController {

	private Link settingsLink;
	private Link changeVideoLink;
	private VideoSettingsController settingsCtr;
	private RepositoryEntry repositoryEntry;

	public VideoRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
		this.repositoryEntry = re;
	}

	@Override
	protected void initEditionTools(Dropdown settingsDropdown) {
		super.initEditionTools(settingsDropdown);
		if (reSecurity.isEntryAdmin()) {
			settingsLink = LinkFactory.createToolLink("metaDataConfig", translate("tab.video.settings"), this);
			settingsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_quota o_icon_settings");
			settingsDropdown.addComponent(4, settingsLink);
			
			changeVideoLink = LinkFactory.createToolLink("changeVideo", translate("tab.video.exchange"), this);
			changeVideoLink.setIconLeftCSS("o_icon o_icon_refresh o_icon-fw");
			settingsDropdown.addComponent(3, changeVideoLink);			

			settingsDropdown.addComponent(new Spacer("metadata-poster"));
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(settingsLink == source){
			doSettingsconfig(ureq);
		} else if (source == changeVideoLink) {
			doReplaceVideo(ureq);
		} else {
			if (event instanceof RootEvent || event instanceof PopEvent) {
				// reload the video, maybe some new transcoded files available
				VideoDisplayController videoDisplayCtr = (VideoDisplayController)getRuntimeController();
				videoDisplayCtr.reloadVideo(ureq);
			}
			// maybe something else needs to be done
			super.event(ureq, source, event);
		}
		doRefreshVideoPosterIfEntryAdmin();		
	}
	
	private void doReplaceVideo (UserRequest ureq) {
		VideoResourceEditController resourceCtrl = new VideoResourceEditController(ureq, getWindowControl(), repositoryEntry);
		listenTo(resourceCtrl);
		pushController(ureq, translate("tab.video.settings"), resourceCtrl);
		setActiveTool(changeVideoLink);
	}

	private void doSettingsconfig(UserRequest ureq) {
		if (settingsCtr != null) {
			removeAsListenerAndDispose(settingsCtr);
		}
		RepositoryEntry entry = getRepositoryEntry();
		VideoSettingsController configCtrl = new VideoSettingsController(ureq, getWindowControl(), entry);
		listenTo(configCtrl);
		settingsCtr = pushController(ureq, translate("tab.video.settings"), configCtrl);
		setActiveTool(settingsLink);
	}
	
	private void doRefreshVideoPosterIfEntryAdmin() {
		if (reSecurity.isEntryAdmin()){
			VideoDisplayController videoDisplayCtr = (VideoDisplayController)getRuntimeController();
			videoDisplayCtr.reloadVideoPoster();
		}
	}

}