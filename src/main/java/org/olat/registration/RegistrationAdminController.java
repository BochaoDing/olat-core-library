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
package org.olat.registration;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.user.UserPropertiesConfig;
import org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Admin panel to configure the registration settings: should link appear on the login page...
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RegistrationAdminController extends FormBasicController {
	
	private MultipleSelectionElement registrationElement;
	private MultipleSelectionElement registrationLinkElement;
	private MultipleSelectionElement registrationLoginElement;
	private MultipleSelectionElement staticPropElement;
	private SingleSelection propertyElement;
	private TextElement propertyValueElement;
	private TextElement exampleElement;
	private TextElement domainListElement;
	private FormLayoutContainer domainsContainer;
	private FormLayoutContainer staticPropContainer;
	
	private static final String[] enableRegistrationKeys = new String[]{ "on" };
	private static final String[] enableRegistrationValues = new String[1];
	private String[] propertyKeys, propertyValues;
	
	private final RegistrationModule registrationModule;
	private final RegistrationManager registrationManager;
	private final UserPropertiesConfig userPropertiesConfig;
	
	public RegistrationAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin");
		
		registrationModule = CoreSpringFactory.getImpl(RegistrationModule.class);
		registrationManager = CoreSpringFactory.getImpl(RegistrationManager.class);
		userPropertiesConfig = CoreSpringFactory.getImpl(UserPropertiesConfig.class);
		//decorate the translator
		setTranslator(userPropertiesConfig.getTranslator(getTranslator()));

		enableRegistrationValues[0] = translate("admin.enableRegistration.on");
		
		List<UserPropertyHandler> allPropertyHandlers = userPropertiesConfig.getAllUserPropertyHandlers();
		List<UserPropertyHandler> propertyHandlers = new ArrayList<UserPropertyHandler>(allPropertyHandlers.size());
		for(UserPropertyHandler handler:allPropertyHandlers) {
			if(handler instanceof Generic127CharTextPropertyHandler) {
				propertyHandlers.add(handler);
			}
		}

		propertyKeys = new String[propertyHandlers.size() + 1];
		propertyValues = new String[propertyHandlers.size() + 1];
		int count = 0;
		//Translator translator = userPropertiesConfig.getTranslator(getTranslator());
		propertyKeys[0] = "";
		propertyValues[0] = "";
		for(UserPropertyHandler propertyHandler:propertyHandlers) {
			propertyKeys[1 + count] = propertyHandler.getName();
			propertyValues[1 + count++] = translate(propertyHandler.i18nFormElementLabelKey());
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		//settings
		FormLayoutContainer settingsContainer = FormLayoutContainer.createDefaultFormLayout("settings", getTranslator());
		settingsContainer.setRootForm(mainForm);
		settingsContainer.contextPut("off_title", translate("admin.registration.title"));
		formLayout.add(settingsContainer);
		
		registrationElement = uifactory.addCheckboxesHorizontal("enable.self.registration", "admin.enableRegistration", settingsContainer, enableRegistrationKeys, enableRegistrationValues, null);
		registrationElement.addActionListener(this, FormEvent.ONCHANGE);
		registrationElement.select("on", registrationModule.isSelfRegistrationEnabled());
		
		registrationLoginElement = uifactory.addCheckboxesHorizontal("enable.registration.login", "admin.enableRegistrationLogin", settingsContainer, enableRegistrationKeys, enableRegistrationValues, null);
		registrationLoginElement.addActionListener(this, FormEvent.ONCHANGE);
		registrationLoginElement.select("on", registrationModule.isSelfRegistrationLoginEnabled());

		registrationLinkElement = uifactory.addCheckboxesHorizontal("enable.registration.link", "admin.enableRegistrationLink", settingsContainer, enableRegistrationKeys, enableRegistrationValues, null);
		registrationLinkElement.addActionListener(this, FormEvent.ONCHANGE);
		registrationLinkElement.select("on", registrationModule.isSelfRegistrationLinkEnabled());
		
		String example = generateExampleCode();
		exampleElement = uifactory.addTextAreaElement("registration.link.example", "admin.registrationLinkExample", 2000, 4, 65, true, example, settingsContainer);
		
		//domain configuration
		domainsContainer = FormLayoutContainer.createDefaultFormLayout("domains", getTranslator());
		domainsContainer.setRootForm(mainForm);
		domainsContainer.contextPut("off_title", translate("admin.registration.domains.title"));
		formLayout.add(domainsContainer);
		
		uifactory.addStaticTextElement("admin.registration.domains.error", null, translate("admin.registration.domains.desc"), domainsContainer);
		String domainsList = registrationModule.getDomainListRaw();
		domainListElement = uifactory.addTextAreaElement("registration.domain.list", "admin.registration.domains", 2000, 10, 65, true, domainsList, domainsContainer);

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonGroupLayout);
		formLayout.add(buttonGroupLayout);
		
		//static property
		staticPropContainer = FormLayoutContainer.createDefaultFormLayout("domains", getTranslator());
		staticPropContainer.setRootForm(mainForm);
		staticPropContainer.contextPut("off_title", translate("admin.registration.staticprop.title"));
		formLayout.add(staticPropContainer);
		
		uifactory.addStaticTextElement("admin.registration.staticprop.error", null, translate("admin.registration.staticprop.desc"), staticPropContainer);
		
		staticPropElement = uifactory.addCheckboxesHorizontal("enable.staticprop", "admin.enableStaticProp", staticPropContainer, enableRegistrationKeys, enableRegistrationValues, null);
		staticPropElement.addActionListener(this, FormEvent.ONCHANGE);
		staticPropElement.select("on", registrationModule.isStaticPropertyMappingEnabled());

		propertyElement = uifactory.addDropdownSingleselect("property", "admin.registration.property", staticPropContainer, propertyKeys, propertyValues, null);
		String propertyName = registrationModule.getStaticPropertyMappingName();
		UserPropertyHandler handler = userPropertiesConfig.getPropertyHandler(propertyName);
		if(handler != null) {
			propertyElement.select(handler.getName(), true);
		} else {
			propertyElement.select("", true);
		}
		propertyElement.addActionListener(this, FormEvent.ONCHANGE);
		
		String propertyValue = registrationModule.getStaticPropertyMappingValue();
		propertyValueElement = uifactory.addTextElement("admin.registration.prop.value", "admin.registration.propertyValue", 255, propertyValue, staticPropContainer);
		
		updateUI();	
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == registrationElement) {
			boolean  enable = registrationElement.isSelected(0);
			registrationModule.setSelfRegistrationEnabled(enable);
			updateUI();
		} else if(source == registrationLinkElement) {
			registrationModule.setSelfRegistrationLinkEnabled(registrationLinkElement.isSelected(0));
			updateUI();
		} else if(source == registrationLoginElement) {
			registrationModule.setSelfRegistrationLoginEnabled(registrationLoginElement.isSelected(0));
			updateUI();
		} else if (source == staticPropElement) {
			registrationModule.setStaticPropertyMappingEnabled(staticPropElement.isSelected(0));
			updateUI();
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateUI() {
		boolean  enableMain = registrationElement.isSelected(0);
		registrationLinkElement.setEnabled(enableMain);
		registrationLoginElement.setEnabled(enableMain);
		
		boolean example = enableMain && registrationLinkElement.isSelected(0);
		exampleElement.setVisible(example);
		
		boolean enableDomains = enableMain && (registrationLinkElement.isSelected(0) || registrationLoginElement.isSelected(0));
		domainsContainer.setVisible(enableDomains);
		
		//static prop
		staticPropContainer.setVisible(enableMain);
		boolean enabledProp = staticPropElement.isSelected(0);
		propertyElement.setVisible(enableMain && enabledProp);
		propertyValueElement.setVisible(enableMain && enabledProp);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		String whiteList = domainListElement.getValue();
		domainListElement.clearError();
		if(StringHelper.containsNonWhitespace(whiteList)) {
			List<String> normalizedList = registrationModule.getDomainList(whiteList);
			List<String> errors = registrationManager.validateWhiteList(normalizedList);
			if(!errors.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for(String error:errors) {
					if(sb.length() > 0) sb.append(" ,");
					sb.append(error);
				}
				domainListElement.setErrorKey("admin.registration.domains.error", new String[]{sb.toString()});
				allOk &= false;
			}
		}
		
		if(staticPropElement.isSelected(0)) {
			if(propertyElement.isOneSelected()) {
				String propertyName = propertyElement.getSelectedKey();
				String value = propertyValueElement.getValue();
				UserPropertyHandler handler = userPropertiesConfig.getPropertyHandler(propertyName);
				ValidationError validationError = new ValidationError();
				boolean valid = handler.isValidValue(value, validationError, getLocale());
				if(!valid) {
					String errorKey = validationError.getErrorKey();
					if(errorKey == null) {
						propertyValueElement.setErrorKey("admin.registration.propertyValue.error", null);
					} else {
						propertyValueElement.setErrorKey(errorKey, null);
					}
				}
			}
		}

		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		registrationModule.setSelfRegistrationEnabled(registrationElement.isSelected(0));
		registrationModule.setSelfRegistrationLinkEnabled(registrationLinkElement.isSelected(0));
		registrationModule.setSelfRegistrationLoginEnabled(registrationLoginElement.isSelected(0));
		
		String domains = domainListElement.getValue();
		registrationModule.setDomainListRaw(domains);
		
		registrationModule.setStaticPropertyMappingEnabled(staticPropElement.isSelected(0));
		if(propertyElement.isOneSelected()) {
			registrationModule.setStaticPropertyMappingName(propertyElement.getSelectedKey());
		} else {
			registrationModule.setStaticPropertyMappingName(null);
		}
		registrationModule.setStaticPropertyMappingValue(propertyValueElement.getValue());
	}
	
	private String generateExampleCode() {
		StringBuilder code = new StringBuilder();
		code.append("<form name=\"openolatregistration\" action=\"")
		    .append(Settings.getServerContextPathURI()).append("/url/registration/0")
		    .append("\" method=\"post\" target=\"OpenOLAT\" onsubmit=\"var openolat=window.open(,'OpenOLAT',''); openolat.focus();\">\n")
		    .append("  <input type=\"submit\" value=\"Go to registration\">\n")
		    .append("</form>");
		return code.toString();
	}
}
