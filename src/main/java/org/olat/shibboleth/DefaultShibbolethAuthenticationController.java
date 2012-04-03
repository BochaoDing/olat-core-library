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
*/
package org.olat.shibboleth;

import java.util.Locale;

import org.olat.basesecurity.AuthHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationController;
import org.olat.core.dispatcher.DispatcherAction;

/**
 * 
 * Description:<br>
 * Simple ShibbolethAuthenticationController. 
 * It just has a link for redirecting the requests to the /shib/.
 * 
 * <P>
 * Initial Date:  08.07.2009 <br>
 * @author Lavinia Dumitrescu
 */
public class DefaultShibbolethAuthenticationController extends AuthenticationController {

	private VelocityContainer loginComp;	
	private Link shibLink;
	private Link guestLink;
	private Panel mainPanel;

	/**
	 * @param ureq
	 * @param wControl
	 */
	public DefaultShibbolethAuthenticationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

//	 extends authControll which is a BasicController, so we have to set the
		// Base new to resolve our velocity pages
		setBasePackage(this.getClass());
		// Manually set translator that uses a fallback translator to the login module
		// Can't use constructor with fallback translator because it gets overriden by setBasePackage call above
		setTranslator(Util.createPackageTranslator(this.getClass(), ureq.getLocale(), Util.createPackageTranslator(LoginModule.class, ureq.getLocale())));
				
		if (!ShibbolethModule.isEnableShibbolethLogins()) throw new OLATSecurityException("Shibboleth is not enabled.");
		
		loginComp = createVelocityContainer("default_shibbolethlogin");				
		shibLink = LinkFactory.createLink("shib.redirect", loginComp, this);	
		
		if (LoginModule.isGuestLoginLinksEnabled()) {
			guestLink = LinkFactory.createLink("menu.guest", loginComp, this);
			guestLink.setCustomEnabledLinkCSS("o_login_guests b_with_small_icon_left");
		}
		
		mainPanel = putInitialPanel(loginComp);
	}

	@Override
	public void changeLocale(Locale newLocale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == shibLink) {
			DispatcherAction.redirectTo(ureq.getHttpResp(), WebappHelper.getServletContextPath() + "/shib/");
		}	else if (source == guestLink) {
			int loginStatus = AuthHelper.doAnonymousLogin(ureq, ureq.getLocale());
			if (loginStatus == AuthHelper.LOGIN_OK) {
				return;
			} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE){
				//getWindowControl().setError(translate("login.notavailable", OLATContext.getSupportaddress()));
				DispatcherAction.redirectToServiceNotAvailable( ureq.getHttpResp() );
			} else {
				getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailSupport")));
			}	
		}
	}
	
}
