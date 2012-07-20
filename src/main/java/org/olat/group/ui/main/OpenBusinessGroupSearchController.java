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

package org.olat.group.ui.main;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Description:<br>
 * Search form for public available Business groups
 * 
 * <P>
 * Initial Date:  21 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenBusinessGroupSearchController extends FormBasicController{

	private TextElement displayName;
	private TextElement owner;
	private TextElement description;
	private FormSubmit searchButton;

	/**
	 * Generic search form.
	 * @param name Internal form name.
	 * @param translator Translator
	 * @param withCancel Display a cancel button?
	 * @param isAdmin Is calling identity an administrator? If yes, allow search by ID
	 * @param limitTypes Limit searches to specific types.
	 */
	public OpenBusinessGroupSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		displayName = uifactory.addTextElement("cif_displayname", "cif.displayname", 255, "", formLayout);
		displayName.setFocus(true);
		
		owner = uifactory.addTextElement("cif_owner", "cif.owner", 255, "", formLayout);
		description = uifactory.addTextElement("cif_description", "cif.description", 255, "", formLayout);

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		searchButton = uifactory.addFormSubmitButton("search", "search", buttonLayout);
	}

	@Override
	protected void doDispose() {
		//
	}

	/**
	 * @return Display name filed value.
	 */
	public String getName() {
		return displayName.getValue();
	}

	/**
	 * @return Author field value.
	 */
	public String getOwner() {
		return owner.getValue();
	}

	/**
	 * @return Description field value.
	 */
	public String getDescription() {
		return description.getValue();
	}
	
	public boolean isEmpty() {
		return displayName.isEmpty() && owner.isEmpty() && description.isEmpty();
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		if (isEmpty())	{
			showWarning("cif.error.allempty", null);
			allOk &= false;
		}
		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK (UserRequest ureq) {
		fireSearchEvent(ureq);
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchButton) {
			fireSearchEvent(ureq);
		}
	}
	
	private void fireSearchEvent(UserRequest ureq) {
		SearchEvent e = new SearchEvent();
		e.setName(getName());
		e.setDescription(getDescription());
		e.setOwnerName(getOwner());
		fireEvent(ureq, e);
	}
}