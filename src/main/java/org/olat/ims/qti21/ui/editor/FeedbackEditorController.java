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
package org.olat.ims.qti21.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.ModalFeedbackBuilder;
import org.olat.ims.qti21.model.xml.SingleChoiceAssessmentItemBuilder;

/**
 * 
 * Initial date: 09.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FeedbackEditorController extends FormBasicController implements EditorController {
	
	private TextElement feedbackCorrectTitleEl, feedbackIncorrectTitleEl;
	private RichTextElement feedbackCorrectTextEl, feedbackIncorrectTextEl;

	private SingleChoiceAssessmentItemBuilder itemBuilder;
	
	public FeedbackEditorController(UserRequest ureq, WindowControl wControl, SingleChoiceAssessmentItemBuilder itemBuilder) {
		super(ureq, wControl);
		this.itemBuilder = itemBuilder;
		initForm(ureq);
	}

	@Override
	public void updateFromBuilder() {
		//
	}

	@Override
	public AssessmentItemBuilder getBuilder() {
		return itemBuilder;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//correct feedback
		ModalFeedbackBuilder correctFeedback = itemBuilder.getCorrectFeedback();
		String correctTitle = correctFeedback == null ? "" : correctFeedback.getTitle();
		feedbackCorrectTitleEl = uifactory.addTextElement("correctTitle", "form.imd.correct.title", -1, correctTitle, formLayout);
		feedbackCorrectTitleEl.setUserObject(correctFeedback);
		String correctText = correctFeedback == null ? "" : correctFeedback.getText();
		feedbackCorrectTextEl = uifactory.addRichTextElementForStringData("correctText", "form.imd.correct.text", correctText, 8, -1, true, null, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		RichTextConfiguration richTextConfig = feedbackCorrectTextEl.getEditorConfiguration();
		richTextConfig.setFileBrowserUploadRelPath("media");// set upload dir to the media dir

		//incorrect feedback
		ModalFeedbackBuilder incorrectFeedback = itemBuilder.getIncorrectFeedback();
		String incorrectTitle = incorrectFeedback == null ? "" : incorrectFeedback.getTitle();
		feedbackIncorrectTitleEl = uifactory.addTextElement("incorrectTitle", "form.imd.incorrect.title", -1, incorrectTitle, formLayout);
		feedbackIncorrectTitleEl.setUserObject(incorrectFeedback);
		String incorrectText = incorrectFeedback == null ? "" : incorrectFeedback.getText();
		feedbackIncorrectTextEl = uifactory.addRichTextElementForStringData("incorrectText", "form.imd.incorrect.text", incorrectText, 8, -1, true, null, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		RichTextConfiguration richTextConfig2 = feedbackIncorrectTextEl.getEditorConfiguration();
		richTextConfig2.setFileBrowserUploadRelPath("media");// set upload dir to the media dir
	
		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String correctTitle = feedbackCorrectTitleEl.getValue();
		String correctText = feedbackCorrectTextEl.getValue();
		if(StringHelper.containsNonWhitespace(FilterFactory.getHtmlTagsFilter().filter(correctText))) {
			ModalFeedbackBuilder correctBuilder = itemBuilder.getCorrectFeedback();
			if(correctBuilder == null) {
				correctBuilder = itemBuilder.createCorrectFeedback();
			}
			correctBuilder.setTitle(correctTitle);
			correctBuilder.setText(correctText);
		}
		
		String incorrectTitle = feedbackIncorrectTitleEl.getValue();
		String incorrectText = feedbackIncorrectTextEl.getValue();
		if(StringHelper.containsNonWhitespace(correctTitle)) {
			ModalFeedbackBuilder incorrectBuilder = (ModalFeedbackBuilder)feedbackIncorrectTitleEl.getUserObject();
			incorrectBuilder.setTitle(incorrectTitle);
			incorrectBuilder.setText(incorrectText);
		}

		fireEvent(ureq, AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED);
	}

	@Override
	protected void doDispose() {
		//
	}
}
