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
package org.olat.modules.coach.ui;

import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.coach.ui.StudentsTableDataModel.Columns;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Overview of all students under the scrutiny of a coach.
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class StudentListController extends FormBasicController implements Activateable2 {
	
	private FlexiTableElement tableEl;
	private StudentsTableDataModel model;
	
	private StudentCoursesController studentCtrl;
	
	private boolean hasChanged;

	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CoachingService coachingService;
	
	public StudentListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, isAdministrativeUser);

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.name, "select"));
		}
		
		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(UserListController.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select", false, null));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.countCourse));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.initialLaunch, new LightIconRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.countPassed, new ProgressRenderer(false, getTranslator())));
		
		model = new StudentsTableDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setEmtpyTableMessageKey("error.no.found");
		tableEl.setAndLoadPersistedPreferences(ureq, "fStudentListController");
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				StudentStatEntry selectedRow = model.getObject(se.getIndex());
				if("select".equals(cmd)) {
					selectStudent(ureq, selectedRow);
				}
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}
	
	private void loadModel() {
		List<StudentStatEntry> students = coachingService.getStudentsStatistics(getIdentity(), userPropertyHandlers);
		model.setObjects(students);
		tableEl.reset();
		tableEl.reloadData();
	}

	private void reloadModel() {
		if(hasChanged) {
			loadModel();
			hasChanged = false;
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event == Event.BACK_EVENT) {
			reloadModel();
			initialPanel.popContent();
			removeAsListenerAndDispose(studentCtrl);
			studentCtrl = null;
			addToHistory(ureq);
		} else if (source == studentCtrl) {
			if(event == Event.CHANGED_EVENT) {
				hasChanged = true;
			} else if("next.student".equals(event.getCommand())) {
				nextStudent(ureq);
			} else if("previous.student".equals(event.getCommand())) {
				previousStudent(ureq);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		ContextEntry ce = entries.get(0);
		OLATResourceable ores = ce.getOLATResourceable();
		if("Identity".equals(ores.getResourceableTypeName())) {
			Long identityKey = ores.getResourceableId();
			for(StudentStatEntry entry:model.getObjects()) {
				if(identityKey.equals(entry.getIdentityKey())) {
					selectStudent(ureq, entry)
						.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
					break;
				}
			}
		}
	}
	
	protected void previousStudent(UserRequest ureq) {
		StudentStatEntry currentEntry = studentCtrl.getEntry();
		int previousIndex = model.getObjects().indexOf(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= model.getRowCount()) {
			previousIndex = model.getRowCount() - 1;
		}
		StudentStatEntry previousEntry = model.getObject(previousIndex);
		selectStudent(ureq, previousEntry);
	}
	
	protected void nextStudent(UserRequest ureq) {
		StudentStatEntry currentEntry = studentCtrl.getEntry();
		int nextIndex = model.getObjects().indexOf(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= model.getRowCount()) {
			nextIndex = 0;
		}
		StudentStatEntry nextEntry = model.getObject(nextIndex);
		selectStudent(ureq, nextEntry);
	}

	protected StudentCoursesController selectStudent(UserRequest ureq, StudentStatEntry studentStat) {
		if(studentCtrl != null) {
			initialPanel.popContent();
			removeAsListenerAndDispose(studentCtrl);
		}
		
		Identity student = securityManager.loadIdentityByKey(studentStat.getIdentityKey());
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, student.getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		
		int index = model.getObjects().indexOf(studentStat);
		studentCtrl = new StudentCoursesController(ureq, bwControl, studentStat, student, index, model.getRowCount(), false);
		listenTo(studentCtrl);
		
		initialPanel.pushContent(studentCtrl.getInitialComponent());
		return studentCtrl;
	}
}