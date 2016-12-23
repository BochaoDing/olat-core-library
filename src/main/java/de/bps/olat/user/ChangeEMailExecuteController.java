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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.olat.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.home.HomeMainController;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.olat.user.ProfileAndHomePageEditController;
import org.olat.user.UserManager;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Description:<br>
 * This controller do change the email from a user after he has clicked a link in email. 
 * 
 * <P>
 * Initial Date:  19.05.2009 <br>
 * @author bja
 */
public class ChangeEMailExecuteController extends ChangeEMailController implements SupportsAfterLoginInterceptor {
	
	private static final String PRESENTED_EMAIL_CHANGE_REMINDER = "presentedemailchangereminder";
	

	protected static final String PACKAGE_HOME = ProfileAndHomePageEditController.class.getPackage().getName();
	
	public ChangeEMailExecuteController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		this.userRequest = ureq;
		pT = Util.createPackageTranslator(ProfileAndHomePageEditController.class, userRequest.getLocale());
		pT = UserManager.getInstance().getPropertyHandlerTranslator(pT);
		emKey = userRequest.getHttpReq().getParameter("key");
		if (emKey == null) {
			emKey = userRequest.getIdentity().getUser().getProperty("emchangeKey", null);
		}
		if (emKey != null) {
			// key exist
			// we check if given key is a valid temporary key
			tempKey = rm.loadTemporaryKeyByRegistrationKey(emKey);
		}
	}
	
	@Override
	public boolean isUserInteractionRequired(UserRequest ureq) {
		User user = ureq.getIdentity().getUser();
		if(StringHelper.containsNonWhitespace(user.getProperty("emchangeKey", null))) {
			if (isLinkTimeUp()) {
				deleteRegistrationKey();
			} else {
				if (isLinkClicked()) {
					changeEMail(getWindowControl());
				} else {
		    		Boolean alreadySeen = ((Boolean)ureq.getUserSession().getEntry(PRESENTED_EMAIL_CHANGE_REMINDER));
		    		if (alreadySeen == null) {
		    			getWindowControl().setWarning(getPackageTranslator().translate("email.change.reminder"));
		    			ureq.getUserSession().putEntry(PRESENTED_EMAIL_CHANGE_REMINDER, Boolean.TRUE);
		    		}
				}
			}
		} else {
			String value = user.getProperty("emailDisabled", null);
			if (value != null && value.equals("true")) {
				Translator translator = Util.createPackageTranslator(HomeMainController.class, ureq.getLocale());
				getWindowControl().setWarning(translator.translate("email.disabled"));
			}
		}
		return false;
	}

	/**
	 * change email
	 * @param wControl
	 * @return
	 */
	public boolean changeEMail(WindowControl wControl) {
		XStream xml = new XStream();
		@SuppressWarnings("unchecked")
		HashMap<String, String> mails = (HashMap<String, String>) xml.fromXML(tempKey.getEmailAddress());
		
		String currentMail = mails.get("currentEMail");
		List<Identity> identities = UserManager.getInstance()
				.findIdentitiesByEmail(Collections.singletonList(currentMail));
		if (identities != null && identities.size() == 1) {
			// change mail address
			Identity ident = identities.get(0);
			ident.getUser().setProperty("email", mails.get("changedEMail"));
			// if old mail address closed then set the new mail address
			// unclosed
			String value = ident.getUser().getProperty("emailDisabled", null);
			if (value != null && value.equals("true")) {
				ident.getUser().setProperty("emailDisabled", "false");
			}
			// success info message
			wControl.setInfo(pT.translate("success.change.email", new String[] { mails.get("currentEMail"), mails.get("changedEMail") }));
			// remove keys
			ident.getUser().setProperty("emchangeKey", null);
			userRequest.getUserSession().removeEntryFromNonClearedStore(ChangeEMailController.CHANGE_EMAIL_ENTRY);
		} else {
			// error message
			wControl.setWarning(pT.translate("error.change.email.unexpected", new String[] { mails.get("currentEMail"), mails.get("changedEMail") }));
		}
		// delete registration key
		rm.deleteTemporaryKeyWithId(tempKey.getRegistrationKey());
		
		return true;
	}
	
	public boolean isLinkClicked() {
		Object entry = userRequest.getUserSession().getEntry(ChangeEMailController.CHANGE_EMAIL_ENTRY);
		return (entry != null);
	}
	
	public Translator getPackageTranslator() {
		return pT;
	}
}
