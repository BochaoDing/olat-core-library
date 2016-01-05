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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.items.SingleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 26.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceEditorController extends FormBasicController {

	private TextElement titleEl;
	private RichTextElement textEl;
	private SingleSelection shuffleEl;
	private FormLayoutContainer answersCont;
	private final List<SimpleChoiceWrapper> choiceWrappers = new ArrayList<>();
	
	private int count = 0;
	private final SingleChoiceAssessmentItemBuilder itemBuilder;
	
	private static final String[] yesnoKeys = new String[]{ "y", "n"};

	public SingleChoiceEditorController(UserRequest ureq, WindowControl wControl, SingleChoiceAssessmentItemBuilder itemBuilder) {
		super(ureq, wControl, "simple_choices_editor");
		this.itemBuilder = itemBuilder;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("editor.sc.title");
		
		FormLayoutContainer metadata = FormLayoutContainer.createDefaultFormLayout("metadata", getTranslator());
		metadata.setRootForm(mainForm);
		formLayout.add(metadata);
		formLayout.add("metadata", metadata);

		titleEl = uifactory.addTextElement("title", "form.imd.title", -1, itemBuilder.getTitle(), metadata);
		titleEl.setMandatory(true);
		
		String description = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForStringData("desc", "form.imd.descr", description, 8, -1, true, null, null,
				metadata, ureq.getUserSession(), getWindowControl());
		RichTextConfiguration richTextConfig = textEl.getEditorConfiguration();
		richTextConfig.setFileBrowserUploadRelPath("media");// set upload dir to the media dir
				
		//points -> in other controller
		
		//shuffle
		String[] yesnoValues = new String[]{ translate("yes"), translate("no") };
		shuffleEl = uifactory.addRadiosHorizontal("shuffle", "form.imd.shuffle", formLayout, yesnoKeys, yesnoValues);
		if (itemBuilder.isShuffle()) {
			shuffleEl.select("y", true);
		} else {
			shuffleEl.select("n", true);
		}

		//responses
		String page = velocity_root + "/simple_choices.html";
		answersCont = FormLayoutContainer.createCustomFormLayout("answers", getTranslator(), page);
		answersCont.setRootForm(mainForm);
		formLayout.add(answersCont);
		formLayout.add("answers", answersCont);

		ChoiceInteraction interaction = itemBuilder.getChoiceInteraction();
		if(interaction != null) {
			
			List<SimpleChoice> choices = itemBuilder.getSimpleChoices();
			for(SimpleChoice choice:choices) {
				wrapAnswer(ureq, choice);
			}
		}
		answersCont.contextPut("choices", choiceWrappers);
		recalculateUpDownLinks();

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		formLayout.add("buttons", buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}

	private void wrapAnswer(UserRequest ureq, SimpleChoice choice) {
		String choiceContent =  itemBuilder.getHtmlHelper().flowStaticString(choice.getFlowStatics());
		String choiceId = "answer" + count++;
		RichTextElement choiceEl = uifactory.addRichTextElementForStringData(choiceId, "form.imd.answer", choiceContent, 8, -1, true, null, null,
				answersCont, ureq.getUserSession(), getWindowControl());
		choiceEl.setUserObject(choice);
		answersCont.add("choiceId", choiceEl);
		
		FormLink removeLink = uifactory.addFormLink("rm-".concat(choiceId), "rm", "", null, answersCont, Link.NONTRANSLATED);
		removeLink.setIconLeftCSS("o_icon o_icon-lg o_icon_delete");
		answersCont.add(removeLink);
		answersCont.add("rm-".concat(choiceId), removeLink);
		
		FormLink addLink = uifactory.addFormLink("add-".concat(choiceId), "add", "", null, answersCont, Link.NONTRANSLATED);
		addLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		answersCont.add(addLink);
		answersCont.add("add-".concat(choiceId), addLink);
		
		FormLink upLink = uifactory.addFormLink("up-".concat(choiceId), "up", "", null, answersCont, Link.NONTRANSLATED);
		upLink.setIconLeftCSS("o_icon o_icon-lg o_icon_move_up");
		answersCont.add(upLink);
		answersCont.add("up-".concat(choiceId), upLink);
		
		FormLink downLink = uifactory.addFormLink("down-".concat(choiceId), "down", "", null, answersCont, Link.NONTRANSLATED);
		downLink.setIconLeftCSS("o_icon o_icon-lg o_icon_move_down");
		answersCont.add(downLink);
		answersCont.add("down-".concat(choiceId), downLink);
		
		boolean correct = itemBuilder.isCorrect(choice);
		choiceWrappers.add(new SimpleChoiceWrapper(choice, correct, choiceEl, removeLink, addLink, upLink, downLink));
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//title
		itemBuilder.setTitle(titleEl.getValue());
		//question
		String questionText = textEl.getValue();
		itemBuilder.setQuestion(questionText);
		
		//correct response
		String correctAnswer = ureq.getParameter("correct");
		Identifier correctAnswerIdentifier = Identifier.parseString(correctAnswer);
		itemBuilder.setCorrectAnswer(correctAnswerIdentifier);
		
		//shuffle
		itemBuilder.setShuffle(shuffleEl.isOneSelected() && shuffleEl.isSelected(0));
		
		//replace simple choices
		List<SimpleChoice> choiceList = new ArrayList<>();
		for(SimpleChoiceWrapper choiceWrapper:choiceWrappers) {
			SimpleChoice choice = choiceWrapper.getSimpleChoice();
			choiceWrapper.setCorrect(correctAnswerIdentifier.equals(choiceWrapper.getIdentifier()));
			//text
			String answer = choiceWrapper.getAnswer().getValue();
			itemBuilder.getHtmlHelper().appendHtml(choice, answer);
			choiceList.add(choice);
		}
		itemBuilder.setSimpleChoices(choiceList);

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem()));
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			String cmd = button.getCmd();
			if("rm".equals(cmd)) {
				doRemoveSimpleChoice((SimpleChoiceWrapper)button.getUserObject());
			} else if("add".equals(cmd)) {
				doAddSimpleChoice(ureq);
			} else if("up".equals(cmd)) {
				doMoveSimpleChoiceUp((SimpleChoiceWrapper)button.getUserObject());
			} else if("down".equals(cmd)) {
				doMoveSimpleChoiceDown((SimpleChoiceWrapper)button.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doAddSimpleChoice(UserRequest ureq) {
		ChoiceInteraction interaction = itemBuilder.getChoiceInteraction();
		SimpleChoice newChoice = new SimpleChoice(interaction);
		newChoice.setIdentifier(IdentifierGenerator.newAsIdentifier("sc"));
		P firstChoiceText = AssessmentItemFactory.getParagraph(newChoice, "New answer");
		newChoice.getFlowStatics().add(firstChoiceText);
		
		wrapAnswer(ureq, newChoice);
		flc.setDirty(true);
	}
	
	private void doRemoveSimpleChoice(SimpleChoiceWrapper choiceWrapper) {
		choiceWrappers.remove(choiceWrapper);
		flc.setDirty(true);
	}
	
	private void doMoveSimpleChoiceUp(SimpleChoiceWrapper choiceWrapper) {
		int index = choiceWrappers.indexOf(choiceWrapper) - 1;
		if(index >= 0 && index < choiceWrappers.size()) {
			choiceWrappers.remove(choiceWrapper);
			choiceWrappers.add(index, choiceWrapper);
		}
		recalculateUpDownLinks();
		flc.setDirty(true);
	}
	
	private void doMoveSimpleChoiceDown(SimpleChoiceWrapper choiceWrapper) {
		int index = choiceWrappers.indexOf(choiceWrapper) + 1;
		if(index > 0 && index < choiceWrappers.size()) {
			choiceWrappers.remove(choiceWrapper);
			choiceWrappers.add(index, choiceWrapper);
		}
		recalculateUpDownLinks();
		flc.setDirty(true);
	}
	
	private void recalculateUpDownLinks() {
		int numOfChoices = choiceWrappers.size();
		for(int i=0; i<numOfChoices; i++) {
			SimpleChoiceWrapper choiceWrapper = choiceWrappers.get(i);
			choiceWrapper.getUp().setEnabled(i != 0);
			choiceWrapper.getDown().setEnabled(i < (numOfChoices - 1));
		}
	}

	public static final class SimpleChoiceWrapper {
		
		private final SimpleChoice choice;
		private final RichTextElement answerEl;
		private final FormLink removeLink, addLink, upLink, downLink;
		
		private boolean correct;
		private final Identifier choiceIdentifier;
		
		public SimpleChoiceWrapper(SimpleChoice choice, boolean correct, RichTextElement answerEl,
				FormLink removeLink, FormLink addLink, FormLink upLink, FormLink downLink) {
			this.choice = choice;
			this.correct = correct;
			this.choiceIdentifier = choice.getIdentifier();
			this.answerEl = answerEl;
			answerEl.setUserObject(this);
			this.removeLink = removeLink;
			removeLink.setUserObject(this);
			this.addLink = addLink;
			addLink.setUserObject(this);
			this.upLink = upLink;
			upLink.setUserObject(this);
			this.downLink = downLink;
			downLink.setUserObject(this);
		}
		
		public Identifier getIdentifier() {
			return choiceIdentifier;
		}
		
		public String getIdentifierString() {
			return choiceIdentifier.toString();
		}
		
		public boolean isCorrect() {
			return correct;
		}
		
		public void setCorrect(boolean correct) {
			this.correct = correct;
		}
		
		public SimpleChoice getSimpleChoice() {
			return choice;
		}
		
		public RichTextElement getAnswer() {
			return answerEl;
		}

		public FormLink getRemove() {
			return removeLink;
		}

		public FormLink getAdd() {
			return addLink;
		}

		public FormLink getUp() {
			return upLink;
		}

		public FormLink getDown() {
			return downLink;
		}
	}
}
