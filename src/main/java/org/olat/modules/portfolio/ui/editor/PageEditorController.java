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
package org.olat.modules.portfolio.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.ui.PageController;
import org.olat.modules.portfolio.ui.editor.event.AddElementEvent;
import org.olat.modules.portfolio.ui.editor.event.ChangePartEvent;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageEditorController extends BasicController {

	private final VelocityContainer mainVC;
	
	private CloseableModalController cmc;
	private PageElementAddController addCtrl;
	private AddElementsController addElementsCtrl;
	private CloseableCalloutWindowController addCalloutCtrl;
	
	private int counter;
	private PageEditorProvider provider;
	private List<EditorFragment> fragments = new ArrayList<>();
	private Map<String,PageElementHandler> handlerMap = new HashMap<>();

	public PageEditorController(UserRequest ureq, WindowControl wControl, PageEditorProvider provider) {
		super(ureq, wControl);
		this.provider = provider;
		setTranslator(Util.createPackageTranslator(PageController.class, getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("page_editor");
		for(PageElementHandler handler:provider.getAvailableHandlers()) {
			handlerMap.put(handler.getType(), handler);
		}

		List<String> addElements = new ArrayList<>();
		for(PageElementHandler handler:provider.getCreateHandlers()) {
			if(handler instanceof InteractiveAddPageElementHandler || handler instanceof SimpleAddPageElementHandler) {
				String id = "add." + handler.getType();
				Link addLink = LinkFactory.createLink(id, id, "add", mainVC, this);
				addLink.setIconLeftCSS("o_icon o_icon-lg " + handler.getIconCssClass());
				addLink.setUserObject(handler);
				mainVC.put(id, addLink);
				addElements.add(id);
			}
		}
		
		mainVC.contextPut("addElementLinks", addElements);
		loadModel(ureq);
		putInitialPanel(mainVC);
	}

	private void loadModel(UserRequest ureq) {
		List<? extends PageElement> elements = provider.getElements();
		List<EditorFragment> newFragments = new ArrayList<>(elements.size());
		for(PageElement element:elements) {
			EditorFragment fragment = createFragment(ureq, element);
			if(fragment != null) {
				newFragments.add(fragment);
			}
		}
		fragments = newFragments;
		mainVC.contextPut("fragments", newFragments);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				PageElement element = addCtrl.getPageElement();
				AddElementInfos uobject = addCtrl.getUserObject();
				doAddPageElement(ureq, element, uobject.getReferenceFragment(), uobject.getTarget());
			}
			cmc.deactivate();
			cleanUp();
		} else if(addElementsCtrl == source) {
			addCalloutCtrl.deactivate();
			cleanUp();
			if(event instanceof AddElementEvent) {
				AddElementEvent aee = (AddElementEvent)event;
				doAddElement(ureq, aee.getReferenceFragment(), aee.getHandler(), aee.getTarget());
			}
		} else if(addCalloutCtrl == source) {
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if(isEditorPartController(source)) {
			EditorFragment fragment = getEditorFragment(source);
			if(event instanceof ChangePartEvent) {
				ChangePartEvent changeEvent = (ChangePartEvent)event;
				PagePart part = changeEvent.getPagePart();
				fragment.setPageElement(part);
				mainVC.setDirty(true);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}	
		}
		super.event(ureq, source, event);
	}
	
	private boolean isEditorPartController(Controller source) {
		for(EditorFragment fragment:fragments) {
			if(fragment.getEditorPart() == source) {
				return true;
			}
		}
		return false;
	}
	
	private EditorFragment getEditorFragment(Controller source) {
		for(EditorFragment fragment:fragments) {
			if(fragment.getEditorPart() == source) {
				return fragment;
			}
		}
		return null;
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addElementsCtrl);
		removeAsListenerAndDispose(addCalloutCtrl);
		removeAsListenerAndDispose(addCtrl);
		removeAsListenerAndDispose(cmc);
		addElementsCtrl = null;
		addCalloutCtrl = null;
		addCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if("add".equals(cmd)) {
				PageElementHandler handler = (PageElementHandler)link.getUserObject();
				doAddElement(ureq, null, handler, PageElementTarget.atTheEnd);
			} else if("add.element.above".equals(cmd)) {
				EditorFragment refEl = (EditorFragment)link.getUserObject();
				openAddElementCallout(ureq, link, refEl, PageElementTarget.above);
			} else if("add.element.below".equals(cmd)) {
				EditorFragment refEl = (EditorFragment)link.getUserObject();
				openAddElementCallout(ureq, link, refEl, PageElementTarget.below);
			} else if("delete.element".equals(cmd)) {
				EditorFragment fragment = (EditorFragment)link.getUserObject();
				doDeleteElement(ureq, fragment);
			} else if("move.up.element".equals(cmd)) {
				EditorFragment fragment = (EditorFragment)link.getUserObject();
				doMoveUpElement(ureq, fragment);
			} else if("move.down.element".equals(cmd)) {
				EditorFragment fragment = (EditorFragment)link.getUserObject();
				doMoveDownElement(ureq, fragment);
			}
		} else if(mainVC == source) {
			if("edit_fragment".equals(event.getCommand())) {
				String fragmentId = ureq.getParameter("fragment");
				EditorFragment selectedFragment = null;
				for(EditorFragment f:fragments) {
					if(f.getComponentName().equals(fragmentId)) {
						selectedFragment = f;
					}
				}
				doEditElement(selectedFragment);
			}
		}
	}
	
	private void doEditElement(EditorFragment fragment) {
		for(EditorFragment eFragment:fragments) {
			eFragment.setEditMode(eFragment == fragment);
		}
		if(fragment.getAddElementAboveLink() == null) {
			String calloutId = "add.dd." + (++counter);
			Link addLink = LinkFactory.createLink(calloutId, "add.element.above", "add.element.above", mainVC, this);
			addLink.setIconLeftCSS("o_icon o_icon-sm o_icon_element_before");
			addLink.setElementCssClass("o_sel_add_element_above");
			addLink.setUserObject(fragment);
			fragment.setAddElementAboveLink(addLink);
		}
		if(fragment.getAddElementBelowLink() == null) {
			String calloutId = "add.dd." + (++counter);
			Link addLink = LinkFactory.createLink(calloutId, "add.element.below", "add.element.below", mainVC, this);
			addLink.setIconLeftCSS("o_icon o_icon-sm o_icon_element_after");
			addLink.setElementCssClass("o_sel_add_element_below");
			addLink.setUserObject(fragment);
			fragment.setAddElementBelowLink(addLink);
		}
		if(fragment.getDeleteLink() == null) {
			String linkId = "del.dd." + (++counter);
			Link deleteLink = LinkFactory.createLink(linkId, "delete", "delete.element", mainVC, this);
			deleteLink.setIconLeftCSS("o_icon o_icon-sm o_icon_delete_item");
			deleteLink.setElementCssClass("o_sel_delete_element");
			deleteLink.setUserObject(fragment);
			fragment.setDeleteLink(deleteLink);
		}
		if(fragment.getMoveUpLink() == null) {
			String linkId = "up.dd." + (++counter);
			Link moveUpLink = LinkFactory.createLink(linkId, "move.up", "move.up.element", mainVC, this);
			moveUpLink.setIconLeftCSS("o_icon o_icon-sm o_icon_move_up");
			moveUpLink.setElementCssClass("o_sel_move_up_element");
			moveUpLink.setUserObject(fragment);
			fragment.setMoveUpLink(moveUpLink);
		}
		fragment.getMoveUpLink().setEnabled(fragments.indexOf(fragment) > 0);
		
		if(fragment.getMoveDownLink() == null) {
			String linkId = "down.dd." + (++counter);
			Link moveDownLink = LinkFactory.createLink(linkId, "move.down", "move.down.element", mainVC, this);
			moveDownLink.setIconLeftCSS("o_icon o_icon-sm o_icon_move_down");
			moveDownLink.setElementCssClass("o_sel_move_down_element");
			moveDownLink.setUserObject(fragment);
			fragment.setMoveDownLink(moveDownLink);
		}
		fragment.getMoveDownLink().setEnabled(fragments.indexOf(fragment) < (fragments.size() - 1));
		
		mainVC.setDirty(true);
	}
	
	private void openAddElementCallout(UserRequest ureq, Link link, EditorFragment referenceFragment, PageElementTarget target) {
		addElementsCtrl = new AddElementsController(ureq, getWindowControl(), provider, referenceFragment, target);
		listenTo(addElementsCtrl);

		addCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				addElementsCtrl.getInitialComponent(), link.getDispatchID(), "", true, "");
		listenTo(addCalloutCtrl);
		addCalloutCtrl.activate();
	}
	
	private void doAddElement(UserRequest ureq, EditorFragment refenceFragment, PageElementHandler handler, PageElementTarget target) {
		if(addCtrl != null) return;
		
		if(handler instanceof InteractiveAddPageElementHandler) {
			InteractiveAddPageElementHandler interactiveHandler = (InteractiveAddPageElementHandler)handler;
			addCtrl = interactiveHandler.getAddPageElementController(ureq, getWindowControl());
			if(addCtrl == null) {
				showWarning("not.implement");
			} else {
				addCtrl.setUserObject(new AddElementInfos(refenceFragment, handler, target));
				listenTo(addCtrl);
				String title = translate("add." + handler.getType());
				cmc = new CloseableModalController(getWindowControl(), null, addCtrl.getInitialComponent(), true, title, true);
				listenTo(cmc);
				cmc.activate();
			}
		} else if(handler instanceof SimpleAddPageElementHandler) {
			SimpleAddPageElementHandler simpleHandler = (SimpleAddPageElementHandler)handler;
			doAddPageElement(ureq, simpleHandler.createPageElement(), null, PageElementTarget.atTheEnd);
		}
	}
	
	private void doAddPageElement(UserRequest ureq, PageElement element, EditorFragment referenceFragment, PageElementTarget target) {
		if(target == PageElementTarget.atTheEnd) {
			doAddPageElementAtTheEnd(ureq, element);
		} else if(target == PageElementTarget.above || target == PageElementTarget.below) {
			int index = fragments.indexOf(referenceFragment);
			if(target == PageElementTarget.below) {
				index = index + 1;
			}
			
			if(index >= fragments.size()) {
				doAddPageElementAtTheEnd(ureq, element);
			} else {
				if(index < 0) {
					index = 0;
				}

				PageElement pageElement = provider.appendPageElementAt(element, index);
				EditorFragment fragment = createFragment(ureq, pageElement);
				fragments.add(index, fragment);
			}
		}

		mainVC.setDirty(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private void doAddPageElementAtTheEnd(UserRequest ureq, PageElement element) {
		PageElement pageElement = provider.appendPageElement(element);
		EditorFragment fragment = createFragment(ureq, pageElement);
		fragments.add(fragment);
	}
	
	private void doDeleteElement(UserRequest ureq, EditorFragment fragment) {
		provider.removePageElement(fragment.getPageElement());
		fragments.remove(fragment);
		mainVC.setDirty(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doMoveUpElement(UserRequest ureq, EditorFragment fragment) {
		int index = fragments.indexOf(fragment) - 1;
		if(index >= 0) {
			provider.moveUpPageElement(fragment.getPageElement());
			fragments.remove(fragment);
			fragments.add(index, fragment);
			mainVC.setDirty(true);
			doEditElement(fragment);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private void doMoveDownElement(UserRequest ureq, EditorFragment fragment) {
		int index = fragments.indexOf(fragment) + 1;
		if(index < fragments.size()) {
			provider.moveDownPageElement(fragment.getPageElement());
			fragments.remove(fragment);
			fragments.add(index, fragment);
			mainVC.setDirty(true);
			doEditElement(fragment);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private EditorFragment createFragment(UserRequest ureq, PageElement element) {
		PageElementHandler handler = handlerMap.get(element.getType());
		if(handler == null) {
			logError("Cannot find an handler of type: " + element.getType(), null);
		}
		Controller editorPart = handler.getEditor(ureq, getWindowControl(), element);
		listenTo(editorPart);
		String cmpId = "frag-" + (++counter);
		EditorFragment fragment = new EditorFragment(element, handler, cmpId, editorPart);
		mainVC.put(cmpId, editorPart.getInitialComponent());
		return fragment;
	}
	
	public static class EditorFragment  {
		
		private boolean editMode;
		private PageElement element;
		private final PageElementHandler handler;

		private final String cmpId;
		private Controller editorPart;
		private Link addElementAboveLink, addElementBelowLink, deleteLink, moveUpLink, moveDownLink;
		
		public EditorFragment(PageElement element, PageElementHandler handler, String cmpId, Controller editorPart) {
			this.element = element;
			this.handler = handler;
			this.cmpId = cmpId;
			this.editorPart = editorPart;
		}

		public boolean isEditMode() {
			return editMode;
		}

		public void setEditMode(boolean editMode) {
			this.editMode = editMode;
		}

		public PageElement getPageElement() {
			return element;
		}
		
		public void setPageElement(PageElement element) {
			this.element = element;
		}

		public String getComponentName() {
			return cmpId;
		}
		
		public Controller getEditorPart() {
			return editorPart;
		}
		
		public Link getAddElementAboveLink() {
			return addElementAboveLink;
		}
		
		public String getAddElementAboveLinkName() {
			return addElementAboveLink.getComponentName();
		}

		public void setAddElementAboveLink(Link addElementAboveLink) {
			this.addElementAboveLink = addElementAboveLink;
		}

		public Link getAddElementBelowLink() {
			return addElementBelowLink;
		}
		
		public String getAddElementBelowLinkName() {
			return addElementBelowLink.getComponentName();
		}

		public void setAddElementBelowLink(Link addElementBelowLink) {
			this.addElementBelowLink = addElementBelowLink;
		}

		public Link getDeleteLink() {
			return deleteLink;
		}
		
		public String getDeleteLinkName() {
			return deleteLink.getComponentName();
		}

		public void setDeleteLink(Link deleteLink) {
			this.deleteLink = deleteLink;
		}

		public Link getMoveUpLink() {
			return moveUpLink;
		}
		
		public String getMoveUpLinkName() {
			return moveUpLink.getComponentName();
		}

		public void setMoveUpLink(Link moveUpLink) {
			this.moveUpLink = moveUpLink;
		}

		public Link getMoveDownLink() {
			return moveDownLink;
		}
		
		public String getMoveDownLinkName() {
			return moveDownLink.getComponentName();
		}

		public void setMoveDownLink(Link moveDownLink) {
			this.moveDownLink = moveDownLink;
		}

		public PageElementHandler getHandler() {
			return handler;
		}
	}

}
