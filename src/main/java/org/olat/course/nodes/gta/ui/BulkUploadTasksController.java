package org.olat.course.nodes.gta.ui;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.course.nodes.gta.model.TaskDefinition;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * Initial date: 30.09.2016<br>
 * @author furredir, dirk.furrer@uzh.ch
 *
 */
public class BulkUploadTasksController extends FormBasicController {

	private FileElement fileEl;

	List<TaskDefinition> taskList;
	private final File taskContainer;


	public BulkUploadTasksController(UserRequest ureq, WindowControl wControl, File taskContainer) {
		super(ureq, wControl);
		this.taskList = new ArrayList<TaskDefinition>();
		this.taskContainer = taskContainer;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_upload_task_form");

		fileEl = uifactory.addFileElement(getWindowControl(), "file", "task.file", formLayout);
		fileEl.setExampleKey("add.multipleTasks.hint",null);
		fileEl.setMandatory(true);
		fileEl.addActionListener(FormEvent.ONCHANGE);

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		fileEl.clearError();
		if(fileEl.getInitialFile() == null && fileEl.getUploadFile() == null) {
			fileEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		File unzipDir = new File(taskContainer,"unzipDir");
		ZipUtil.unzip(fileEl.getUploadFile(),unzipDir);

		List<File> newFiles = null;
		try {
			newFiles = Files.walk(Paths.get(unzipDir.getPath()))
                    .filter(Files::isRegularFile)
					.filter(p -> !p.getFileName()
							.toString().startsWith("."))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

			for(File taskFile: newFiles) {
				if(taskFile.isFile()) {
					TaskDefinition task = new TaskDefinition();
					task.setFilename(taskFile.getName());
					task.setTitle(FilenameUtils.getBaseName(taskFile.getName()));
					taskList.add(task);
					try {
						Path upload = taskFile.toPath();
						File target = new File(taskContainer, taskFile.getName());
						Files.move(upload, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (Exception ex) {
						logError("", ex);
					}
				}
			}

			FileUtils.deleteDirectory(unzipDir);
		}catch(IOException e){
				logWarn("wasnt able to delete unzip dir in tasknode", e);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	public List<TaskDefinition> getTaskList(){
		return taskList;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}