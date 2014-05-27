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
* <p>
*/ 

package org.olat.core.gui.components.choice;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Initial Date: Feb 2, 2004 A <b>ChoiceRenderer </b> is
 * 
 * @author Felix Jost
 */
public class ChoiceRenderer extends DefaultComponentRenderer {

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	@Override
	public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {

		// Get the model object
		Choice choice = (Choice) source;
		ChoiceModel model = choice.getModel();

		boolean iframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();
		// form header
		String id = choice.getComponentName() + "_" + choice.hashCode();
		target.append("<form method=\"post\" name=\"").append(id).append("\" id=\"").append(id).append("\" action=\"");
		ubu.buildURI(target, null, null, iframePostEnabled? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		target.append("\"");
		if (iframePostEnabled) {
			ubu.appendTarget(target);
		}
		target.append(">");

		target.append("<table class=\"b_choice\">");
		int rows = model.getRowCount();
		for (int i = 0; i < rows; i++) {
			Boolean val = model.isEnabled(i);
			boolean selected = val == null ? false : val.booleanValue();
			boolean disabled = model.isDisabled(i);
			
			String keyN = "c" + i;
			target.append("<tr><td class='b_choice_checkrow'><input type='checkbox' class='b_checkbox'")
			      .append(" checked='checked'", selected)
			      .append(" disabled='disabled'", disabled)
			      .append(" name='").append(keyN).append("' onchange=\"return setFormDirty('").append(id).append("')\"  />")
			      .append("</td>");
			
			String label = model.getLabel(i);
			target.append("<td class='b_choice_textrow'>")
			      .append(StringEscapeUtils.escapeHtml(label))
			      .append("</td></tr>");
		}
		// Toggle all on/off
		target.append("<tr><td colspan='2' class=\"b_togglecheck\">")
		      .append("<div class=\"b_togglecheck\">")
		      .append("<a href=\"#\" onclick=\"javascript:b_choice_toggleCheck('" + id + "', true)\">")
		      .append("<input type=\"checkbox\" checked=\"checked\" disabled=\"disabled\" />")
		      .append(translator.translate("checkall"))
		      .append("</a> <a href=\"#\" onclick=\"javascript:b_choice_toggleCheck('" + id + "', false)\">")
		      .append("<input type=\"checkbox\" disabled=\"disabled\" />")
		      .append(translator.translate("uncheckall"))
		      .append("</a></div></td></tr>");
		
		//buttons
		target.append("<tr><td colspan='2'><div class='btn-group'>");
		// Submit button
		target.append("<input type='submit' name='olat_fosm' value=\"")
		      .append(StringEscapeUtils.escapeHtml(translator.translate(choice.getSubmitKey())))
		      .append("\" class='btn btn-primary' />");

		//Reset button
		String resetKey = choice.getResetKey();
		if (resetKey != null) {
			target.append("<input type='submit' name='").append(Choice.RESET_IDENTIFICATION)
			      .append("' value=\"").append(StringEscapeUtils.escapeHtml(translator.translate(resetKey)))
			      .append("\" class='btn btn-default' />");
		}
		
		// Cancel button
		String cancelKey = choice.getCancelKey();
		if (cancelKey != null) {
			target.append("<input type='submit' name='").append(Choice.CANCEL_IDENTIFICATION)
			      .append("' value=\"").append(StringEscapeUtils.escapeHtml(translator.translate(cancelKey)))
			      .append("\" class='btn btn-default' />");
		}
		target.append("</div></td></tr></table></form>");
	}
}