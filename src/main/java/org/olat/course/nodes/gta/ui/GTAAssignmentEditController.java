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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAAssignmentEditController extends AbstractAssignmentEditController {
	
	private static final String[] typeKeys = new String[] { GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL, GTACourseNode.GTASK_ASSIGNEMENT_TYPE_AUTO };
	private static final String[] previewKeys = new String[] { "enabled", "disabled" };
	private static final String[] samplingKeys = new String[] { GTACourseNode.GTASK_SAMPLING_UNIQUE, GTACourseNode.GTASK_SAMPLING_REUSE };
	private static final String[] enableKeys = new String[] { "on" };

	private RichTextElement textEl, emailTextEl;
	private SingleSelection typeEl, previewEl, samplingEl;
	private MultipleSelectionElement coachAllowedTasksEl, emailConfirmationEl;

	public GTAAssignmentEditController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, ModuleConfiguration config, CourseEnvironment courseEnv, boolean readOnly) {
		super(ureq, wControl, gtaNode, config, courseEnv, readOnly);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//tasks
		super.initForm(formLayout, listener, ureq);
		
		//config
		FormLayoutContainer configCont = FormLayoutContainer.createDefaultFormLayout("config", getTranslator());
		configCont.setFormTitle(translate("assignment.config.title"));
		configCont.setElementCssClass("o_sel_course_gta_task_config_form");
		configCont.setRootForm(mainForm);
		formLayout.add(configCont);
		
		//coach allowed to upload tasks
		String[] onValues = new String[]{ "" };
		coachAllowedTasksEl = uifactory.addCheckboxesVertical("coachTasks", "task.coach.allowed.upload", configCont, enableKeys, onValues, 1);
		coachAllowedTasksEl.addActionListener(FormEvent.ONCHANGE);
		boolean coachUpload = config.getBooleanSafe(GTACourseNode.GTASK_COACH_ALLOWED_UPLOAD_TASKS, false);
		if(coachUpload) {
			coachAllowedTasksEl.select(enableKeys[0], true);
		}
		
		//task assignment configuration
		String[] typeValues = new String[]{
				translate("task.assignment.type.manual"),
				translate("task.assignment.type.auto")
		};
		typeEl = uifactory.addRadiosVertical("task.assignment.type", configCont, typeKeys, typeValues);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		String type = config.getStringValue(GTACourseNode.GTASK_ASSIGNEMENT_TYPE);
		if(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL.equals(type)) {
			typeEl.select(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL, true);
		} else if(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_AUTO.equals(type)) {
			typeEl.select(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_AUTO, true);
		} else {
			typeEl.select(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL, true);
		}
		
		String[] previewValues = new String[] { translate("preview.enabled"), translate("preview.disabled") };
		previewEl = uifactory.addRadiosVertical("preview", configCont, previewKeys, previewValues);
		boolean preview = config.getBooleanSafe(GTACourseNode.GTASK_PREVIEW);
		if(preview) {
			previewEl.select(previewKeys[0], true);
		} else {
			previewEl.select(previewKeys[1], true);
		}
		previewEl.setVisible(typeEl.isSelected(0));
		
		String[] samplingValues = new String[]{ translate("sampling.unique"), translate("sampling.reuse") };
		samplingEl = uifactory.addRadiosVertical("sampling", configCont, samplingKeys, samplingValues);
		String sampling = config.getStringValue(GTACourseNode.GTASK_SAMPLING);
		if(GTACourseNode.GTASK_SAMPLING_UNIQUE.equals(sampling)) {
			samplingEl.select(GTACourseNode.GTASK_SAMPLING_UNIQUE, true);
		} else if(GTACourseNode.GTASK_SAMPLING_REUSE.equals(sampling)) {
			samplingEl.select(GTACourseNode.GTASK_SAMPLING_REUSE, true);
		} else {
			samplingEl.select(GTACourseNode.GTASK_SAMPLING_UNIQUE, true);
		}

		//message for users
		String text = config.getStringValue(GTACourseNode.GTASK_USERS_TEXT);
		if (!StringHelper.containsNonWhitespace(text)) {
			text = translate("assignment.text.template");
		}
		textEl = uifactory.addRichTextElementForStringDataMinimalistic("task.text", "assignment.text.label", text, 10, -1, configCont, getWindowControl());
		textEl.setMandatory(true);

		//confirmation
		FormLayoutContainer confirmationCont = FormLayoutContainer.createDefaultFormLayout("confirmation", getTranslator());
		confirmationCont.setFormTitle(translate("assignment.confirmation.title"));
		confirmationCont.setRootForm(mainForm);
		formLayout.add(confirmationCont);

		//message for users
		String mailText = config.getStringValue(GTACourseNode.GTASK_ASSIGNMENT_TEXT);
		if(!StringHelper.containsNonWhitespace(mailText)) {
			mailText = translate("assignment.email.template");
		}
		emailTextEl = uifactory.addRichTextElementForStringDataMinimalistic("task.mail.text", "assignment.email.label", mailText, 10, -1, confirmationCont, getWindowControl());
		emailTextEl.setMandatory(true);

		String[] enableValues = new String[]{ translate("enabled") };
		emailConfirmationEl = uifactory.addCheckboxesHorizontal("confirmation", "assignment.email.confirmation", confirmationCont, enableKeys, enableValues);
		boolean confirm = config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT_MAIL_CONFIRMATION);
		emailConfirmationEl.select(enableKeys[0], confirm);

		//save
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setElementCssClass("o_sel_course_gta_task_config_buttons");
		buttonsCont.setRootForm(mainForm);
		confirmationCont.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(typeEl == source) {
			boolean allowPreview = typeEl.isSelected(0);
			previewEl.setVisible(allowPreview);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean coachUploadAllowed = coachAllowedTasksEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_COACH_ALLOWED_UPLOAD_TASKS, coachUploadAllowed);
		
		// assignment type
		String type = typeEl.isSelected(0) ? GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL : GTACourseNode.GTASK_ASSIGNEMENT_TYPE_AUTO;
		config.setStringValue(GTACourseNode.GTASK_ASSIGNEMENT_TYPE, type);
		// preview
		if(previewEl.isVisible()) {
			config.setBooleanEntry(GTACourseNode.GTASK_PREVIEW, previewEl.isSelected(0));
		} else {
			config.setBooleanEntry(GTACourseNode.GTASK_PREVIEW, Boolean.FALSE);
		}
		// sampling
		String sampling = samplingEl.isSelected(0) ? GTACourseNode.GTASK_SAMPLING_UNIQUE : GTACourseNode.GTASK_SAMPLING_REUSE;
		config.setStringValue(GTACourseNode.GTASK_SAMPLING, sampling);

		// messagetext
		String text = textEl.getValue();
		if(StringHelper.containsNonWhitespace(text)) {
			config.setStringValue(GTACourseNode.GTASK_USERS_TEXT, text);
		} else {
			config.remove(GTACourseNode.GTASK_USERS_TEXT);
		}

		// email confirmation text
		String emailText = emailTextEl.getValue();
		config.setStringValue(GTACourseNode.GTASK_ASSIGNMENT_TEXT, emailText);
		boolean emailConfirmation = emailConfirmationEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_ASSIGNMENT_MAIL_CONFIRMATION, emailConfirmation);

		fireEvent(ureq, Event.DONE_EVENT);
	}
}