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
package org.olat.modules.video.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.course.CorruptedCourseException;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.ui.TranscodingQueueTableModel.TranscodingQueueTableCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.user.HomePageConfig;
import org.olat.user.HomePageDisplayController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial Date: 31.01.2017
 * The Class VideoAdminErrorController.
 * @author fkiefer fabian.kiefer@frentix.com
 * 
 * shows a list of all FAILED transcoding orders 
 */
public class VideoAdminErrorController extends FormBasicController {
	
	private TranscodingQueueTableModel tableModel;
	private FlexiTableElement tableEl;
	private FormItemContainer formLayout;
	private FormLink refreshButton;
	private CloseableModalController closeableModalController;
	private HomePageDisplayController homePageDisplayController;

	
	private int counter = 0;

	@Autowired
	private VideoManager videoManager;
	@Autowired
	private UserManager userManager;
	@Autowired 
	private RepositoryService repositoryService;
	@Autowired
	private BaseSecurity baseSecurity;   
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;

	
	public VideoAdminErrorController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl,"transcoding_queue");
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingQueueTableCols.resid));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingQueueTableCols.displayname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingQueueTableCols.failureReason, new TranscodingErrorIconRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingQueueTableCols.creator));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingQueueTableCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingQueueTableCols.dimension));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingQueueTableCols.format));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingQueueTableCols.retranscode));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingQueueTableCols.delete));
		tableModel = new TranscodingQueueTableModel(columnsModel, getTranslator());
		
		initForm(ureq);
	}
	

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		this.formLayout = formLayout;
		setFormTitle("number.transcodings");
		setFormDescription("number.transcodings");
		setFormContextHelp("Portfolio template: Administration and editing#configuration");
		
		initTable();
	}
	
	private void initTable () {
		List<VideoTranscoding> videoTranscodings = videoManager.getFailedVideoTranscodings();
		List<TranscodingQueueTableRow> rows = new ArrayList<>();
		
		for (VideoTranscoding videoTranscoding : videoTranscodings) {

			String title = videoManager.getDisplayTitleForResolution(videoTranscoding.getResolution(), getTranslator());
			String resid = String.valueOf(videoTranscoding.getVideoResource().getResourceableId());
			FormLink resourceLink = uifactory.addFormLink("res_" + counter++, "viewResource", resid, resid, flc, Link.LINK + Link.NONTRANSLATED);
			resourceLink.setUserObject(videoTranscoding);
			FormLink deleteLink = uifactory.addFormLink("del_" + counter++, "deleteQuality", "quality.delete", "quality.delete", flc, Link.LINK);
			deleteLink.setUserObject(videoTranscoding);
			deleteLink.setIconLeftCSS("o_icon o_icon_delete_item o_icon-fw");
			FormLink retranscodeLink = uifactory.addFormLink("trans_" + counter++, "retranscode", "queue.retranscode", "queue.retranscode", flc, Link.LINK);
			retranscodeLink.setUserObject(videoTranscoding);
			retranscodeLink.setIconLeftCSS("o_icon o_icon_refresh o_icon-fw");

			Object[] failureReason = new Object[]{-1,""};
			if (videoTranscoding.getStatus() == VideoTranscoding.TRANSCODING_STATUS_INEFFICIENT) {
				failureReason[0] = VideoTranscoding.TRANSCODING_STATUS_INEFFICIENT;
				failureReason[1] = translate("transcoding.inefficient");
			} else if (videoTranscoding.getStatus() == VideoTranscoding.TRANSCODING_STATUS_ERROR) {
				failureReason[0] = VideoTranscoding.TRANSCODING_STATUS_ERROR;
				failureReason[1] = translate("transcoding.error");
			} else if (videoTranscoding.getStatus() == VideoTranscoding.TRANSCODING_STATUS_TIMEOUT) {
				failureReason[0] = VideoTranscoding.TRANSCODING_STATUS_TIMEOUT;
				failureReason[1] = translate("transcoding.timeout");
			} 

			RepositoryEntry videoRe = repositoryService.loadByResourceKey(videoTranscoding.getVideoResource().getKey());
			if (videoRe == null) continue;
			String displayname = videoRe.getDisplayname();
			String initialAuthor = videoRe.getInitialAuthor();
			String fullName = userManager.getUserDisplayName(initialAuthor);
			FormLink authorLink = uifactory.addFormLink("author_" + counter++, "viewAuthor",
					fullName, fullName, flc, Link.LINK + Link.NONTRANSLATED);
			authorLink.setUserObject(initialAuthor);
			Date creationDate = videoTranscoding.getCreationDate();
			TranscodingQueueTableRow transcodingrow = new TranscodingQueueTableRow(resourceLink, displayname, creationDate, authorLink,
					title, null, videoTranscoding.getFormat(), deleteLink);
			transcodingrow.setFailureReason(failureReason);
			transcodingrow.setRetranscodeLink(retranscodeLink);
			
			rows.add(transcodingrow);
		}
		tableModel.setObjects(rows);
		
		if (formLayout.hasFormComponent(tableEl)){
			formLayout.remove(tableEl);
		}
		if (formLayout.hasFormComponent(refreshButton)){
			formLayout.remove(refreshButton);
		}
		
		tableEl = uifactory.addTableElement(getWindowControl(), "queue", tableModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		
		refreshButton = uifactory.addFormLink("button.refresh", formLayout,Link.BUTTON);
		refreshButton.setIconLeftCSS("o_icon o_icon_refresh o_icon-fw");
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == homePageDisplayController || source == closeableModalController){
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink && ((FormLink) source).getCmd().equals("deleteQuality")) {
			FormLink link = (FormLink) source;
			VideoTranscoding videoTranscoding = (VideoTranscoding) link.getUserObject();
			videoManager.deleteVideoTranscoding(videoTranscoding);
		} else if (source instanceof FormLink && ((FormLink) source).getCmd().equals("viewAuthor")) {
			showUserInfo(ureq, baseSecurity.findIdentityByName((String) source.getUserObject()));
		} else if (source instanceof FormLink && ((FormLink) source).getCmd().equals("retranscode")) {
			FormLink link = (FormLink) source;
			VideoTranscoding videoTranscoding = (VideoTranscoding) link.getUserObject();
			videoManager.retranscodeFailedVideoTranscoding(videoTranscoding);
		} else if (source instanceof FormLink && ((FormLink) source).getCmd().equals("viewResource")) {
			FormLink link = (FormLink) source;
			VideoTranscoding videoTranscoding = (VideoTranscoding) link.getUserObject();
			launch(ureq, videoTranscoding);
		}
		initTable();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
	}

	@Override
	protected void doDispose() {
		
	}
	
	protected void launch(UserRequest ureq, VideoTranscoding videoTranscoding) {
		RepositoryEntry videoRe = repositoryService.loadByResourceKey(videoTranscoding.getVideoResource().getKey());
		try {
			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler("FileResource.VIDEO");
			if(handler != null) {

				String businessPath = "[RepositoryEntry:" + videoRe.getKey() + "]";
				if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
					tableEl.reloadData();
				}
			}
		} catch (CorruptedCourseException e) {
			logError("Course corrupted: " + videoRe.getKey(), e);
			showError("cif.error.corrupted");
		}
	}
	
	/**
	 * Method to open the users visiting card in a new tab. Public to call it also from the parent controller
	 * @param ureq
	 */
	public void showUserInfo(UserRequest ureq, Identity userID) {
		
		homePageDisplayController = new HomePageDisplayController(ureq, getWindowControl(), userID, new HomePageConfig());
		
		closeableModalController = new CloseableModalController(getWindowControl(), translate("close"), 
				homePageDisplayController.getInitialComponent(), true, translate("video.contact"));
		listenTo(closeableModalController);
		
		closeableModalController.activate();
	}
	
	private void cleanUp(){
		closeableModalController.deactivate();
		removeAsListenerAndDispose(closeableModalController);
		closeableModalController = null;
		homePageDisplayController = null;
	}

}
