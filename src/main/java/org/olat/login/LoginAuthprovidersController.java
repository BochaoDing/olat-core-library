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
package org.olat.login;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.olat.admin.sysinfo.InfoMessageManager;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.login.auth.AuthenticationEvent;
import org.olat.login.auth.AuthenticationProvider;

/**
 * Description:<br>
 * TODO: patrickb Class Description for LoginAuthprovidersController
 * 
 * <P>
 * Initial Date:  02.09.2007 <br>
 * @author patrickb
 */
public class LoginAuthprovidersController extends MainLayoutBasicController implements Activateable2 {

	private static final String ACTION_LOGIN = "login";
	public  static final String ATTR_LOGIN_PROVIDER = "lp";

	private VelocityContainer content;
	private Controller authController;
	private final List<Controller> authControllers = new ArrayList<Controller>();
	private StackedPanel dmzPanel;
	
	public LoginAuthprovidersController(UserRequest ureq, WindowControl wControl) {
		// Use fallback translator from full webapp package to translate accessibility stuff
		super(ureq, wControl, Util.createPackageTranslator(BaseFullWebappController.class, ureq.getLocale()));
		
		if(ureq.getUserSession().getEntry("error.change.email") != null) {
			wControl.setError(ureq.getUserSession().getEntry("error.change.email").toString());
			ureq.getUserSession().removeEntryFromNonClearedStore("error.change.email");
		}
		if(ureq.getUserSession().getEntry("error.change.email.time") != null) {
			wControl.setError(ureq.getUserSession().getEntry("error.change.email.time").toString());
			ureq.getUserSession().removeEntryFromNonClearedStore("error.change.email.time");
		}
		
		MainPanel panel = new MainPanel("content");
		panel.setCssClass("o_loginscreen");
		content = initLoginContent(ureq, null);
		panel.pushContent(content);
		dmzPanel = putInitialPanel(panel);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("browsercheck".equals(type)) {
			showBrowserCheckPage(ureq);
		} else if ("accessibility".equals(type)) {
			showAccessibilityPage();
		} else if ("about".equals(type)) {
			showAboutPage();
		} else if ("registration".equals(type)) {
			// make sure the OLAT authentication controller is activated as only this one can handle registration requests
			AuthenticationProvider OLATProvider = LoginModule.getAuthenticationProvider(BaseSecurityModule.getDefaultAuthProviderIdentifier());
			if (OLATProvider.isEnabled()) {
				initLoginContent(ureq, BaseSecurityModule.getDefaultAuthProviderIdentifier());
				if(authController instanceof Activateable2) {
					((Activateable2)authController).activate(ureq, entries, state);
				}
			}			
			// don't know what to do when the OLAT provider is not enabled
		} else if(authController instanceof Activateable2) {
			((Activateable2)authController).activate(ureq, entries, state);
		}
	}

	private VelocityContainer initLoginContent(UserRequest ureq, String provider) {
		// in every case we build the container for pages to fill the panel
		VelocityContainer contentBorn = createVelocityContainer("main_loging", "login");

		// browser not supported messages
		// true if browserwarning should be showed
		boolean bwo = Settings.isBrowserAjaxBlacklisted(ureq);
		contentBorn.contextPut("browserWarningOn", bwo ? Boolean.TRUE : Boolean.FALSE);
		
		// prepare login
		if (provider == null)	provider = LoginModule.getDefaultProviderName();
		AuthenticationProvider authProvider = LoginModule.getAuthenticationProvider(provider);
		if (authProvider == null)
			throw new AssertException("Invalid authentication provider: " + provider);
		
		//clean-up controllers
		if(authController != null) {
			removeAsListenerAndDispose(authController);
		}
		for(Controller controller:authControllers) {
			removeAsListenerAndDispose(controller);
		}
		authControllers.clear();
		
		//recreate controllers
		authController = authProvider.createController(ureq, getWindowControl());
		listenTo(authController);
		contentBorn.put("loginComp", authController.getInitialComponent());
		Collection<AuthenticationProvider> providers = LoginModule.getAuthenticationProviders();
		List<AuthenticationProvider> providerSet = new ArrayList<AuthenticationProvider>(providers.size());
		int count = 0;
		for (AuthenticationProvider prov : providers) {
			if (prov.isEnabled()) {
				providerSet.add(prov);
				if(!prov.getName().equals(authProvider.getName())) {
					//hang these components to the component tree, for state-less behavior
					Controller controller = prov.createController(ureq, getWindowControl());
					authControllers.add(controller);
					Component cmp = controller.getInitialComponent();
					contentBorn.put("dormant_" + count++, cmp);
					listenTo(controller);
				}
			}
		}
		providerSet.remove(authProvider); // remove active authProvider from list of alternate authProviders
		contentBorn.contextPut("providerSet", providerSet);
		contentBorn.contextPut("locale", ureq.getLocale());

		// prepare info message
		InfoMessageManager mrg = CoreSpringFactory.getImpl(InfoMessageManager.class);
		String infomsg = mrg.getInfoMessage();
		if (infomsg != null && infomsg.length() > 0) {
			contentBorn.contextPut("infomsg", infomsg);
		}
		
		String infomsgNode = mrg.getInfoMessageNodeOnly();
		if (infomsgNode != null && infomsgNode.length() > 0) {
			contentBorn.contextPut("infomsgNode", infomsgNode);
		}
		
		// add additional login intro message for custom content
		String customMsg = translate("login.custommsg");
		if(!StringUtils.isBlank(customMsg)) {
			contentBorn.contextPut("logincustommsg",customMsg);
		}
		
		//login is blocked?
		if(AuthHelper.isLoginBlocked()) {
			contentBorn.contextPut("loginBlocked", Boolean.TRUE);
		}
		
		return contentBorn;
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//auto-disposed
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (event.getCommand().equals(ACTION_LOGIN)) { 
			// show traditional login page
			dmzPanel.popContent();
			content = initLoginContent(ureq, ureq.getParameter(ATTR_LOGIN_PROVIDER));
			dmzPanel.pushContent(content);
		}
	}

	protected void showAccessibilityPage() {
		VelocityContainer accessibilityVC = createVelocityContainer("accessibility");
		dmzPanel.pushContent(accessibilityVC);
	}

	protected void showBrowserCheckPage(UserRequest ureq) {
		VelocityContainer browserCheck = createVelocityContainer("browsercheck");
		browserCheck.contextPut("isBrowserAjaxReady", Boolean.valueOf(!Settings.isBrowserAjaxBlacklisted(ureq)));
		dmzPanel.pushContent(browserCheck);
	}

	protected void showAboutPage() {
		VelocityContainer aboutVC = createVelocityContainer("about");
		// Add version info and licenses
		aboutVC.contextPut("version", Settings.getFullVersionInfo());
		// Add translator and languages info
		I18nManager i18nMgr = I18nManager.getInstance();
		Set<String> enabledKeysSet = I18nModule.getEnabledLanguageKeys();
		Map<String, String> langNames = new HashMap<String, String>();
		Map<String, String> langTranslators = new HashMap<String, String>();
		String[] enabledKeys = ArrayHelper.toArray(enabledKeysSet);
		String[] names = new String[enabledKeys.length];
		for (int i = 0; i < enabledKeys.length; i++) {
			String key = enabledKeys[i];
			String langName = i18nMgr.getLanguageInEnglish(key, I18nModule.isOverlayEnabled());
			langNames.put(key, langName);
			names[i] = langName;
			String author = i18nMgr.getLanguageAuthor(key);
			langTranslators.put(key, author);
		}
		ArrayHelper.sort(enabledKeys, names, true, true, true);
		aboutVC.contextPut("enabledKeys", enabledKeys);
		aboutVC.contextPut("langNames", langNames);
		aboutVC.contextPut("langTranslators", langTranslators);
		dmzPanel.pushContent(aboutVC);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event == Event.CANCELLED_EVENT) {
			// is a Form cancelled, show Login Form
			content = initLoginContent(ureq, null);
			dmzPanel.setContent(content);
		}else if (event instanceof AuthenticationEvent) {
			AuthenticationEvent authEvent = (AuthenticationEvent)event;
			Identity identity = authEvent.getIdentity();
			int loginStatus = AuthHelper.doLogin(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier(), ureq);
			if (loginStatus == AuthHelper.LOGIN_OK) {
				return;
			} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE){
				DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
			} else {
				// fxdiff: show useradmin-mail for pw-requests
				getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailReplyTo")));
			}
		
		}
	}
}