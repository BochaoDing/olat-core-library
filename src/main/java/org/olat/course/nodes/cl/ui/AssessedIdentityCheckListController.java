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
package org.olat.course.nodes.cl.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.AssessmentBatch;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.model.DBCheck;
import org.olat.course.nodes.cl.model.DBCheckbox;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 10.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedIdentityCheckListController extends FormBasicController {

	private static final String[] onKeys = new String[]{ "on" };

	private final boolean withScore;
	private final ModuleConfiguration config;
	private final CheckListCourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;
	private final OLATResourceable courseOres;
	private final Identity assessedIdentity;
	private final CheckboxList checkboxList;
	private List<CheckboxWrapper> wrappers;
	
	private final CheckboxManager checkboxManager;
	
	public AssessedIdentityCheckListController(UserRequest ureq, WindowControl wControl,
			Identity assessedIdentity, OLATResourceable courseOres,
			UserCourseEnvironment userCourseEnv, CheckListCourseNode courseNode) {
		super(ureq, wControl);

		this.courseNode = courseNode;
		this.courseOres = courseOres;
		this.userCourseEnv = userCourseEnv;
		config = courseNode.getModuleConfiguration();
		Boolean hasScore = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		withScore = (hasScore == null || hasScore.booleanValue());	

		this.assessedIdentity = assessedIdentity;
		CheckboxList configCheckboxList = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		if(configCheckboxList == null) {
			checkboxList = new CheckboxList();
			checkboxList.setList(Collections.<Checkbox>emptyList());
		} else {
			checkboxList = configCheckboxList;
		}
		
		checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
		
			List<DBCheck> checks = checkboxManager.loadCheck(assessedIdentity, courseOres, courseNode.getIdent());
			Map<String, DBCheck> uuidToCheckMap = new HashMap<>();
			for(DBCheck check:checks) {
				uuidToCheckMap.put(check.getCheckbox().getCheckboxId(), check);
			}
			
			List<Checkbox> list = checkboxList.getList();
			wrappers = new ArrayList<>(list.size());
			for(Checkbox checkbox:list) {
				DBCheck check = uuidToCheckMap.get(checkbox.getCheckboxId());
				boolean readOnly = false;
				CheckboxWrapper wrapper = forgeCheckboxWrapper(checkbox, check, readOnly, formLayout);
				wrappers.add(wrapper);
			}
			layoutCont.contextPut("checkboxList", wrappers);
		}
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonCont);
		FormSubmit saveButton = uifactory.addFormSubmitButton("save", "save", buttonCont);
		saveButton.setEnabled(checkboxList.getNumOfCheckbox() > 0);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}
	
	private CheckboxWrapper forgeCheckboxWrapper(Checkbox checkbox, DBCheck check, boolean readOnly, FormItemContainer formLayout) {
		String[] values = new String[]{ translate(checkbox.getLabel().i18nKey()) };
		
		String boxId = "box_" + checkbox.getCheckboxId();
		MultipleSelectionElement boxEl = uifactory
				.addCheckboxesHorizontal(boxId, null, formLayout, onKeys, values, null);
		boxEl.setEnabled(!readOnly);
		boxEl.setLabel(checkbox.getTitle(), null, false);
		boxEl.showLabel(true);
		boxEl.addActionListener(this, FormEvent.ONCHANGE);
		
		TextElement pointEl = null;
		if(withScore) {
			String pointId = "point_" + checkbox.getCheckboxId();
			String points = AssessmentHelper.getRoundedScore(checkbox.getPoints());
			pointEl = uifactory.addTextElement(pointId, null, 16, points, formLayout);
			pointEl.setDisplaySize(5);
			pointEl.setExampleKey("checklist.point.example", new String[]{ "0", "1"});
		}
		
		CheckboxWrapper wrapper = new CheckboxWrapper(checkbox, check, boxEl, pointEl);
		boxEl.setUserObject(wrapper);
		if(check != null && check.getChecked() != null && check.getChecked().booleanValue()) {
			boxEl.select(onKeys[0], true);
			wrapper.setDbCheckbox(check.getCheckbox());
		}
		return wrapper;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<AssessmentBatch> batchElements = new ArrayList<>();
		for(CheckboxWrapper wrapper:wrappers) {
			Float editedPoint = null;
			if(wrapper.getPointEl() != null) {
				String val = wrapper.getPointEl().getValue();
				if(StringHelper.containsNonWhitespace(val)) {
					try {
						editedPoint = new Float(val);
					} catch (NumberFormatException e) {
						editedPoint = null;
					}	
				}
			}
			
			boolean editedValue = wrapper.getCheckboxEl().isAtLeastSelected(1);
			
			Float currentPoint = null;
			boolean currentValue = false;
			if(wrapper.getCheck() != null) {
				currentPoint = wrapper.getCheck().getScore();
				Boolean checkObj = wrapper.getCheck().getChecked();
				if(checkObj != null && checkObj.booleanValue()) {
					currentValue = checkObj.booleanValue();
				}
			}
			
			if((editedValue != currentValue)
					|| ((currentPoint == null && editedPoint != null)
					|| (currentPoint != null &&  editedPoint == null)
					|| (currentPoint != null && !currentPoint.equals(editedPoint)))) {
				
				String boxId = wrapper.getCheckbox().getCheckboxId();
				batchElements.add(new AssessmentBatch(assessedIdentity.getKey(), boxId, editedPoint, editedValue));
			}
		}
		checkboxManager.check(courseOres, courseNode.getIdent(), batchElements);
		
		courseNode.updateScoreEvaluation(userCourseEnv, assessedIdentity);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	public static class CheckboxWrapper {
		
		private final TextElement pointEl;
		private final MultipleSelectionElement checkboxEl;
		private final Checkbox checkbox;
		private DBCheck check;
		private DBCheckbox dbCheckbox;
		
		public CheckboxWrapper(Checkbox checkbox, DBCheck check, MultipleSelectionElement checkboxEl, TextElement pointEl) {
			this.checkboxEl = checkboxEl;
			this.check = check;
			this.pointEl = pointEl;
			this.checkbox = checkbox;
		}

		public Checkbox getCheckbox() {
			return checkbox;
		}
		
		public DBCheck getCheck() {
			return check;
		}

		public void setCheck(DBCheck check) {
			this.check = check;
		}

		/**
		 * This value is lazy loaded and can be null!
		 * @return
		 */
		public DBCheckbox getDbCheckbox() {
			return dbCheckbox;
		}

		public void setDbCheckbox(DBCheckbox dbCheckbox) {
			this.dbCheckbox = dbCheckbox;
		}

		public String getTitle() {
			return checkbox.getTitle();
		}
		
		public boolean isPointsAvailable() {
			return checkbox.getPoints() != null;
		}
		
		public TextElement getPointEl() {
			return pointEl;
		}

		public MultipleSelectionElement getCheckboxEl() {
			return checkboxEl;
		}
		
		public String getCheckboxElName() {
			return checkboxEl.getName();//getComponent().getComponentName();
		}
	}
}