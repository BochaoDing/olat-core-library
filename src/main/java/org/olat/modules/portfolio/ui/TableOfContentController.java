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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.model.SectionRefImpl;
import org.olat.modules.portfolio.ui.event.SectionSelectionEvent;
import org.olat.modules.portfolio.ui.model.PageRow;
import org.olat.modules.portfolio.ui.renderer.PortfolioRendererHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TableOfContentController extends BasicController implements TooledController, Activateable2 {
	
	private Link newSectionTool, newSectionButton, newEntryLink, editBinderMetadataLink;
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;
	
	private CloseableModalController cmc;
	private SectionEditController newSectionCtrl;
	private SectionEditController editSectionCtrl;
	private BinderMetadataEditController binderMetadataCtrl;
	
	private PageRunController pageCtrl;
	private PageMetadataEditController newPageCtrl;
	
	private int counter = 0;
	private Binder binder;
	private final List<Identity> owners;
	private final BinderConfiguration config;
	private final BinderSecurityCallback secCallback;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioService portfolioService;

	public TableOfContentController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Binder binder, BinderConfiguration config) {
		super(ureq, wControl);
		this.binder = binder;
		this.config = config;
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("table_of_contents");

		owners = portfolioService.getMembers(binder, PortfolioRoles.owner.name());
		StringBuilder ownerSb = new StringBuilder();
		for(Identity owner:owners) {
			if(ownerSb.length() > 0) ownerSb.append(", ");
			ownerSb.append(userManager.getUserDisplayName(owner));
		}
		mainVC.contextPut("owners", ownerSb.toString());

		putInitialPanel(mainVC);
		loadModel();
	}
	
	@Override
	public void initTools() {
		if(secCallback.canEditMetadataBinder()) {
			editBinderMetadataLink = LinkFactory.createToolLink("edit.binder.metadata", translate("edit.binder.metadata"), this);
			editBinderMetadataLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			stackPanel.addTool(editBinderMetadataLink, Align.left);
		}
		
		if(secCallback.canAddSection()) {
			newSectionTool = LinkFactory.createToolLink("new.section", translate("create.new.section"), this);
			newSectionTool.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			stackPanel.addTool(newSectionTool, Align.right);
		
			newSectionButton = LinkFactory.createButton("create.new.section", mainVC, this);
			newSectionButton.setCustomEnabledLinkCSS("btn btn-primary");
		}
		
		if(secCallback.canAddPage()) {
			newEntryLink = LinkFactory.createToolLink("new.page", translate("create.new.page"), this);
			newEntryLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			stackPanel.addTool(newEntryLink, Align.right);
		}
	}
	
	protected void loadModel() {
		mainVC.contextPut("binderTitle", StringHelper.escapeHtml(binder.getTitle()));
		
		List<SectionRow> sectionList = new ArrayList<>();
		Map<Long,SectionRow> sectionMap = new HashMap<>();
		
		List<AssessmentSection> assessmentSections = portfolioService.getAssessmentSections(binder, getIdentity());
		Map<Section,AssessmentSection> sectionToAssessmentSectionMap = new HashMap<>();
		for(AssessmentSection assessmentSection:assessmentSections) {
			sectionToAssessmentSectionMap.put(assessmentSection.getSection(), assessmentSection);
		}

		List<Section> sections = portfolioService.getSections(binder);
		for(Section section:sections) {
			SectionRow sectionRow = forgeSectionRow(section, sectionToAssessmentSectionMap.get(section));
			sectionList.add(sectionRow);
			sectionMap.put(section.getKey(), sectionRow);
		}

		List<Page> pages = portfolioService.getPages(binder, null);
		for(Page page:pages) {
			if(secCallback.canViewElement(page)) {
				Section section = page.getSection();
				SectionRow sectionRow = sectionMap.get(section.getKey());
				PageRow pageRow = forgePageRow(page, sectionRow);
				sectionRow.getPages().add(pageRow);
			}
		}
		mainVC.contextPut("sections", sectionList);
	}
	
	private SectionRow forgeSectionRow(Section section, AssessmentSection assessmentSection) {
		String sectionId = "section" + (++counter);
		String title = StringHelper.escapeHtml(section.getTitle());
		
		Link sectionLink = LinkFactory.createCustomLink(sectionId, "open_section", title, Link.LINK | Link.NONTRANSLATED, mainVC, this);
		SectionRow sectionRow = new SectionRow(section, sectionLink, assessmentSection);
		sectionLink.setUserObject(sectionRow);
		
		if(secCallback.canEditSection()) {
			Dropdown editDropdown = new Dropdown(sectionId.concat("_down"), null, false, getTranslator());
			editDropdown.setTranslatedLabel("");
			editDropdown.setOrientation(DropdownOrientation.right);
			editDropdown.setIconCSS("o_icon o_icon_actions");
			mainVC.put(editDropdown.getComponentName(), editDropdown);
		
			Link editSectionLink = LinkFactory.createLink(sectionId.concat("_edit"), "section.edit", "edit_section", mainVC, this);
			editSectionLink.setIconLeftCSS("o_icon o_icon_edit");
			editDropdown.addComponent(editSectionLink);
			Link deleteSectionLink = LinkFactory.createLink(sectionId.concat("_delete"), "section.delete", "delete_section", mainVC, this);
			deleteSectionLink.setIconLeftCSS("o_icon o_icon_delete_item");
			editDropdown.addComponent(deleteSectionLink);
			
			sectionRow.setEditDropdown(editDropdown);
			editSectionLink.setUserObject(sectionRow);
			deleteSectionLink.setUserObject(sectionRow);
		}
		
		return sectionRow;
	}
	
	private PageRow forgePageRow(Page page, SectionRow sectionRow) {
		PageRow pageRow = new PageRow(page, sectionRow.getSection(), null, false, config.isAssessable());
		
		String pageId = "page" + (++counter);
		String title = StringHelper.escapeHtml(page.getTitle());
		Link openLink = LinkFactory.createCustomLink(pageId, "open_page", title, Link.LINK | Link.NONTRANSLATED, mainVC, this);
		openLink.setUserObject(pageRow);
		pageRow.setOpenLink(openLink);
		
		return pageRow;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			return;
		}
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Page".equalsIgnoreCase(resName) || "Entry".equalsIgnoreCase(resName)) {
			Long pageKey = entries.get(0).getOLATResourceable().getResourceableId();
			Page page = portfolioService.getPageByKey(pageKey);
			if(page != null && page.getSection() != null && binder.equals(page.getSection().getBinder())) {
				Activateable2 activateable = doOpenPage(ureq, page);
				if(activateable != null) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					activateable.activate(ureq, subEntries, entries.get(0).getTransientState());
				}
			}
		} else if("Section".equalsIgnoreCase(resName)) {
			Long sectionKey = entries.get(0).getOLATResourceable().getResourceableId();
			Section section = portfolioService.getSection(new SectionRefImpl(sectionKey));
			if(section != null && binder.equals(section.getBinder())) {
				doOpenSection(ureq, section);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newSectionCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editSectionCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(newPageCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(binderMetadataCtrl == source) {
			if(event == Event.DONE_EVENT) {
				binder = binderMetadataCtrl.getBinder();
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(binderMetadataCtrl);
		removeAsListenerAndDispose(editSectionCtrl);
		removeAsListenerAndDispose(newSectionCtrl);
		removeAsListenerAndDispose(newPageCtrl);
		removeAsListenerAndDispose(cmc);
		binderMetadataCtrl = null;
		editSectionCtrl = null;
		newSectionCtrl = null;
		newPageCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(newSectionTool == source || newSectionButton == source) {
			doCreateNewSection(ureq);
		} else if(newEntryLink == source) {
			doCreateNewPage(ureq);
		} else if(editBinderMetadataLink == source) {
			doEditBinderMetadata(ureq);
		} else if(source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if("open_section".equals(cmd)) {
				SectionRow row = (SectionRow)link.getUserObject();
				doOpenSection(ureq, row.getSection());
			} else if("edit_section".equals(cmd)) {
				SectionRow row = (SectionRow)link.getUserObject();
				doEditSection(ureq, row); 
			} else if("open_page".equals(cmd)) {
				PageRow row = (PageRow)link.getUserObject();
				doOpenPage(ureq, row.getPage());
			}
		}
	}
	
	private void doOpenSection(UserRequest ureq, Section section) {
		fireEvent(ureq, new SectionSelectionEvent(section));
	}
	
	private void doEditSection(UserRequest ureq, SectionRow sectionRow) {
		if(editSectionCtrl != null) return;
		
		editSectionCtrl = new SectionEditController(ureq, getWindowControl(), sectionRow.getSection());
		editSectionCtrl.setUserObject(sectionRow);
		listenTo(editSectionCtrl);
		
		String title = translate("section.edit");
		cmc = new CloseableModalController(getWindowControl(), null, editSectionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateNewSection(UserRequest ureq) {
		if(newSectionCtrl != null) return;
		
		newSectionCtrl = new SectionEditController(ureq, getWindowControl(), binder);
		listenTo(newSectionCtrl);
		
		String title = translate("create.new.section");
		cmc = new CloseableModalController(getWindowControl(), null, newSectionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private PageRunController doOpenPage(UserRequest ureq, Page page) {
		removeAsListenerAndDispose(pageCtrl);

		OLATResourceable pageOres = OresHelper.createOLATResourceableInstance("Entry", page.getKey());
		WindowControl swControl = addToHistory(ureq, pageOres, null);
		Page reloadedPage = portfolioService.getPageByKey(page.getKey());
		pageCtrl = new PageRunController(ureq, swControl, stackPanel, secCallback, reloadedPage);
		listenTo(pageCtrl);
		stackPanel.pushController(StringHelper.escapeHtml(page.getTitle()), pageCtrl);
		return pageCtrl;
	}
	
	private void doCreateNewPage(UserRequest ureq) {
		if(newPageCtrl != null) return;
		
		newPageCtrl = new PageMetadataEditController(ureq, getWindowControl(), binder, false, null, true);
		listenTo(newPageCtrl);
		
		String title = translate("create.new.page");
		cmc = new CloseableModalController(getWindowControl(), null, newPageCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditBinderMetadata(UserRequest ureq) {
		if(binderMetadataCtrl != null) return;
		
		Binder reloadedBinder = portfolioService.getBinderByKey(binder.getKey());
		binderMetadataCtrl = new BinderMetadataEditController(ureq, getWindowControl(), reloadedBinder);
		listenTo(binderMetadataCtrl);
		
		String title = translate("edit.binder.metadata");
		cmc = new CloseableModalController(getWindowControl(), null, binderMetadataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	public class SectionRow {
		
		private final Section section;
		private final Link sectionLink;
		private Dropdown editDropdown;
		private final List<PageRow> pages = new ArrayList<>();
		
		private AssessmentSection assessmentSection;
		
		public SectionRow(Section section, Link sectionLink, AssessmentSection assessmentSection) {
			this.section = section;
			this.sectionLink = sectionLink;
			this.assessmentSection = assessmentSection;
		}

		public String getTitle() {
			return section.getTitle();
		}
		
		public String getCssClassStatus() {
			return section.getSectionStatus() == null
					? SectionStatus.notStarted.cssClass() : section.getSectionStatus().cssClass();
		}
		
		public boolean isAssessable() {
			return config.isAssessable();
		}
		
		/**
		 * It use the same format as the cell renderer.
		 * @return
		 */
		public String getFormattedResult() {
			if(config.isAssessable()) {
				return PortfolioRendererHelper.getFormattedResult(assessmentSection, getTranslator());
			}
			return "";
		}
		
		public Section getSection() {
			return section;
		}
		
		public List<PageRow> getPages() {
			return pages;
		}

		public void setEditDropdown(Dropdown editDropdown) {
			this.editDropdown = editDropdown;
		}
		
		public boolean hasEditDropdown() {
			return editDropdown != null;
		}
		
		public String getEditDropdownName() {
			return editDropdown == null ? null : editDropdown.getComponentName();
		}
		
		public String getSectionLinkName() {
			return sectionLink.getComponentName();
		}
	}
}
