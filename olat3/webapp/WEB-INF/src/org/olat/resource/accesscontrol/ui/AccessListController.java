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

package org.olat.resource.accesscontrol.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.OfferAccess;

/**
 * 
 * Description:<br>
 * A simple step to choose the way to access a resource if
 * several methods are available
 * 
 * <P>
 * Initial Date:  27 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AccessListController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final List<Controller> accessCtrls = new ArrayList<Controller>();
	
	private final AccessControlModule acModule;
	private final ACFrontendManager acFrontendManager;



	public AccessListController(UserRequest ureq, WindowControl wControl, List<OfferAccess> links) {
		super(ureq, wControl);
		
		acModule = (AccessControlModule)CoreSpringFactory.getBean("acModule");
		acFrontendManager = (ACFrontendManager)CoreSpringFactory.getBean("acFrontendManager");
		
		mainVC = createVelocityContainer("access_method_list");
		
		for(OfferAccess link:links) {
			AccessMethodHandler handler = acModule.getAccessMethodHandler(link.getMethod().getType());
			Controller accessCtrl = handler.createAccessController(ureq, getWindowControl(), link, null);
			listenTo(accessCtrl);
			accessCtrls.add(accessCtrl);
			mainVC.put("ac_" + link.getKey(), accessCtrl.getInitialComponent());
		}
		mainVC.contextPut("links", links);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(accessCtrls.contains(source)) {
			if(event instanceof AccessEvent) {
				if(event.equals(AccessEvent.ACCESS_OK_EVENT)) {
					fireEvent(ureq, AccessEvent.ACCESS_OK_EVENT);
				} else {
					String msg = ((AccessEvent)event).getMessage();
					if(StringHelper.containsNonWhitespace(msg)) {
						getWindowControl().setError(msg);
					} else {
						showError("error.accesscontrol");
					}
				}
			}
		}
		super.event(ureq, source, event);
	}
}
