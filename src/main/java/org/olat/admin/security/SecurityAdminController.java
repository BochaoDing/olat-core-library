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
package org.olat.admin.security;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 23.12.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SecurityAdminController extends FormBasicController {
	
	private MultipleSelectionElement wikiEl, topFrameEl;
	private final BaseSecurityModule securityModule;
	
	public SecurityAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("sec.title");
		setFormDescription("sec.description");
		setFormContextHelp("org.olat.admin.security", "ced-sec.html", "help.hover.sec");
		
		String[] frameKeys = new String[]{ "on" };
		String[] frameValues = new String[]{ "" };
		
		topFrameEl = uifactory.addCheckboxesHorizontal("sec.topframe", "sec.topframe", formLayout, frameKeys, frameValues, null);
		topFrameEl.select("on", securityModule.isForceTopFrame());
		topFrameEl.addActionListener(this, FormEvent.ONCHANGE);
		
		wikiEl = uifactory.addCheckboxesHorizontal("sec.wiki", "sec.wiki", formLayout, frameKeys, frameValues, null);
		wikiEl.select("on", securityModule.isWikiEnabled());
		wikiEl.addActionListener(this, FormEvent.ONCHANGE);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(topFrameEl == source) {
			boolean enabled = topFrameEl.isAtLeastSelected(1);
			securityModule.setForceTopFrame(enabled);
		} else if(wikiEl == source) {
			boolean enabled = wikiEl.isAtLeastSelected(1);
			securityModule.setWikiEnabled(enabled);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}


	

}
