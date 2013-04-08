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
package org.olat.portfolio.ui.structel.edit;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeDropEvent;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.tree.TreeHelper;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPStructureManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.EPStructureElement;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.ui.structel.EPAddElementsController;
import org.olat.portfolio.ui.structel.EPArtefactClicked;
import org.olat.portfolio.ui.structel.EPStructureChangeEvent;

/**
 * Description:<br>
 * Controller shows a TOC (table of content) of the given PortfolioStructure
 * elements can be moved around by d&d
 * 
 * <P>
 * Initial Date:  13.09.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPTOCController extends BasicController {

	protected static final String ARTEFACT_NODE_CLICKED = "artefactNodeClicked";
	private static final String DELETE_LINK_CMD = "delete";
	private static final String ARTEFACT_NODE_IDENTIFIER = "art";
	private static final String ROOT_NODE_IDENTIFIER = "rootStruct";
	protected final EPFrontendManager ePFMgr;
	protected final EPStructureManager eSTMgr;
	protected PortfolioStructureMap rootNode;
	protected final EPSecurityCallback secCallback;
	private MenuTree treeCtr;
	private VelocityContainer tocV;
	private PortfolioStructure structureClicked;
	private AbstractArtefact artefactClicked;
	
	protected final Map<Long,String> idToPath = new HashMap<Long,String>();
	protected final Map<String,PortfolioStructure> pathToStructure = new HashMap<String,PortfolioStructure>();
	private EPAddElementsController addElCtrl;
	private Link delButton;

	public EPTOCController(UserRequest ureq, WindowControl wControl, PortfolioStructure selectedEl, 
			PortfolioStructureMap rootNode, EPSecurityCallback secCallback) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		tocV = createVelocityContainer("toc");
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		eSTMgr = (EPStructureManager) CoreSpringFactory.getBean("epStructureManager");
		this.rootNode = rootNode;
		TreeModel treeModel = buildTreeModel();
		treeCtr = new MenuTree("toc");
		treeCtr.setTreeModel(treeModel);
		treeCtr.setSelectedNode(treeModel.getRootNode());
		treeCtr.setDragEnabled(true);
		treeCtr.setDropEnabled(true);
		treeCtr.setDropSiblingEnabled(true);
		treeCtr.addListener(this);
		treeCtr.setRootVisible(true);

		tocV.put("tocTree", treeCtr);		
		delButton = LinkFactory.createCustomLink("deleteButton", DELETE_LINK_CMD, "&nbsp;&nbsp;&nbsp;", Link.NONTRANSLATED, tocV, this);
		delButton.setTooltip(translate("deleteButton"));
		delButton.setCustomEnabledLinkCSS("b_delete_icon b_eportfolio_del_link ");
		tocV.put("deleteButton", delButton);		

		if(selectedEl == null) {
			refreshAddElements(ureq, rootNode);
		} else {
			TreeNode selectedNode = TreeHelper.findNodeByUserObject(selectedEl, treeModel.getRootNode());
			if(selectedNode != null) {
				structureClicked = selectedEl;
				treeCtr.setSelectedNode(selectedNode);
				refreshAddElements(ureq, selectedEl);
			}
		}
		
		putInitialPanel(tocV);
	}
	
	protected void refreshTree(PortfolioStructureMap root) {
		this.rootNode = root;
	}
	
	/**
	 * refreshing the add elements link to actual structure
	 * @param ureq
	 * @param struct maybe null -> hiding the add-button
	 */
	private void refreshAddElements(UserRequest ureq, PortfolioStructure struct){
		tocV.remove(tocV.getComponent("addElement"));
		removeAsListenerAndDispose(addElCtrl);
		if (struct != null){
			addElCtrl = new EPAddElementsController(ureq, getWindowControl(), struct);
			if (struct instanceof EPPage) {
				if(secCallback.canAddStructure()) {
					addElCtrl.setShowLink(EPAddElementsController.ADD_STRUCTUREELEMENT);
				}
				if(secCallback.canAddArtefact()) {
					addElCtrl.setShowLink(EPAddElementsController.ADD_ARTEFACT);
				}
			} else if (struct instanceof EPAbstractMap) {
				if(secCallback.canAddPage()) {
					addElCtrl.setShowLink(EPAddElementsController.ADD_PAGE);
				}
			} else { // its a structure element
				if(secCallback.canAddArtefact()) {
					addElCtrl.setShowLink(EPAddElementsController.ADD_ARTEFACT);
				}
			}
			listenTo(addElCtrl);
			tocV.put("addElement", addElCtrl.getInitialComponent());
		}		
	}
	
	private TreeModel buildTreeModel() {
		idToPath.put(rootNode.getKey(), "/" + ROOT_NODE_IDENTIFIER);
		return new EPTOCTreeModel(rootNode, translate("toc.root"));
		
	}
	
	public void update(UserRequest ureq, PortfolioStructure structure) {
		String path = idToPath.get(structure.getKey());
		if(path != null) {
		//TODO jquery treeCtr.reloadPath(path);
		//TODO jquery treeCtr.selectPath(path);
		}
		refreshAddElements(ureq, structure);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link link = (Link) source;
			if (link.getCommand().equals(DELETE_LINK_CMD)) {
				if (artefactClicked != null) {
					AbstractArtefact artefact = artefactClicked;
					PortfolioStructure parentStruct = getArtefactParentStruct(artefactClicked);
					PortfolioStructure mergedStruct = ePFMgr.removeArtefactFromStructure(artefact, parentStruct);
					// refresh the view
					fireEvent(ureq, Event.CHANGED_EVENT);
				} else if (structureClicked != null) {
					if ((structureClicked instanceof EPPage)
							&& !(structureClicked instanceof EPAbstractMap)) {
						PortfolioStructure ps = structureClicked;
						while (ePFMgr.loadStructureParent(ps) != null) {
							ps = ePFMgr.loadStructureParent(ps);
						}
						int childPages = ePFMgr.countStructureChildren(ps);
						if (childPages > 1) {
							eSTMgr.removeStructureRecursively(structureClicked);
							// refresh the view
							fireEvent(ureq, Event.CHANGED_EVENT);
						} else {
							showError("last.page.not.deletable");
						}
					} else if(structureClicked instanceof EPStructureElement 
							&& !(structureClicked instanceof EPAbstractMap)) {
						//structures should always be deletable
						eSTMgr.removeStructureRecursively(structureClicked);
						// refresh the view
						fireEvent(ureq, Event.CHANGED_EVENT);
					} else {
						showInfo("element.not.deletable");
					}
				}
			}
		} else if (source == treeCtr) {
			if(event instanceof TreeEvent) {
				TreeEvent te = (TreeEvent)event;
				if(MenuTree.COMMAND_TREENODE_CLICKED.equals(te.getCommand())) {
					doSelectTreeElement(ureq, te);
				}
			} else if(event instanceof TreeDropEvent) {
				TreeDropEvent te = (TreeDropEvent)event;
				doDrop(ureq, te.getDroppedNodeId(), te.getTargetNodeId(), te.isAsChild(), te.isAtTheEnd());
			}
		}
	}
	
	private void doSelectTreeElement(UserRequest ureq, TreeEvent te) {
		TreeNode selectedNode = treeCtr.getTreeModel().getNodeById(te.getNodeId());
		Object userObj = selectedNode.getUserObject();
		if (userObj instanceof PortfolioStructure){
			//structure clicked
			structureClicked = (PortfolioStructure)userObj;
			refreshAddElements(ureq, structureClicked);
			delButton.setVisible(true);
			//send event to load this page
			fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.SELECTED, structureClicked));
		} else if (userObj instanceof AbstractArtefact) {
			//artefact clicked
			Object parentObj = ((TreeNode)selectedNode.getParent()).getUserObject();
			if(parentObj instanceof PortfolioStructure) {
				artefactClicked = (AbstractArtefact)userObj;
				PortfolioStructure structure = (PortfolioStructure)parentObj;
				refreshAddElements(ureq, null);
				delButton.setVisible(true);
				fireEvent(ureq, new EPArtefactClicked(ARTEFACT_NODE_CLICKED, structure));
			}
		} else {
			// root tree node clicked, no add/delete link
			delButton.setVisible(false);
			refreshAddElements(ureq, null);
			fireEvent(ureq, new Event(ARTEFACT_NODE_CLICKED));
		}
	}
	
	private void reloadTreeModel(PortfolioStructure oldStruct, PortfolioStructure newStruct) {
		if(oldStruct != null && newStruct != null && oldStruct.equals(newStruct)) {
			newStruct = null;//only 1 reload
		}
		if(oldStruct != null ) {
			reloadTreeModel(oldStruct);
		}
		if(newStruct != null) {
			reloadTreeModel(newStruct);
		}
	}
	
	private void reloadTreeModel(PortfolioStructure struct) {
		EPTOCTreeModel model = (EPTOCTreeModel)treeCtr.getTreeModel();
		if(struct != null) {
			TreeNode node = TreeHelper.findNodeByUserObject(struct, model.getRootNode());
			if(node != null) {
				model.loadChildNode(struct, node);
			}
		}
	}
	
	private void doDrop(UserRequest ureq, String droppedNodeId, String targetNodeId, boolean child, boolean atTheEnd) {
		TreeNode droppedNode = treeCtr.getTreeModel().getNodeById(droppedNodeId);
		TreeNode targetNode = treeCtr.getTreeModel().getNodeById(targetNodeId);
		if(droppedNode == null || targetNode == null) return;
		
		Object droppedObj = droppedNode.getUserObject();
		Object droppedParentObj = null;
		if(droppedNode.getParent() != null) {
			droppedParentObj = ((TreeNode)droppedNode.getParent()).getUserObject();
		}
		Object targetObj = targetNode.getUserObject();
		Object targetParentObj = null;
		if(targetNode.getParent() != null) {
			targetParentObj = ((TreeNode)targetNode.getParent()).getUserObject();
		}

		if (droppedObj instanceof AbstractArtefact) {
			AbstractArtefact artefact = (AbstractArtefact)droppedObj;
			if (checkNewArtefactTarget(artefact, targetObj)){
				moveArtefactToNewParent(ureq, artefact, droppedParentObj, targetObj);
			} else if(targetParentObj != null && targetParentObj.equals(droppedParentObj)) {
				reorder(ureq, artefact, (TreeNode)targetNode.getParent(), targetObj);
			}
		} else if (droppedObj instanceof PortfolioStructure) {
			PortfolioStructure droppedStruct = (PortfolioStructure)droppedObj;
			if (checkNewStructureTarget(droppedStruct, droppedParentObj, targetNode)) {
				int newPos = 0;// moveEvent.getPosition();
				moveStructureToNewParent(ureq, droppedStruct, droppedParentObj, targetObj, newPos);
			}
		}
	}
	
	private boolean moveStructureToNewParent(UserRequest ureq, PortfolioStructure structToBeMvd,
			Object oldParent, Object newParent, int newPos) {
		
		if(oldParent instanceof PortfolioStructure && newParent instanceof PortfolioStructure) {
			PortfolioStructure oldParStruct = (PortfolioStructure)oldParent;
			PortfolioStructure newParStruct = (PortfolioStructure)newParent;
			if (oldParStruct.equals(newParStruct)) {
				// this is only a position move
				if(ePFMgr.moveStructureToPosition(structToBeMvd, newPos)) {
					reloadTreeModel(structToBeMvd, null);
					fireEvent(ureq, new EPMoveEvent());
					return true;
				}
			} else if(ePFMgr.moveStructureToNewParentStructure(structToBeMvd, oldParStruct, newParStruct, newPos)) {
				reloadTreeModel(oldParStruct, newParStruct);
				fireEvent(ureq, new EPMoveEvent());
				return true;
			}
		}
		return false;
	}
	
	private boolean checkNewArtefactTarget(AbstractArtefact artefact, Object  targetObj){
		PortfolioStructure newParStruct;
		if (targetObj instanceof EPAbstractMap ) {
			return false;
		} else if(targetObj instanceof PortfolioStructure) {
			newParStruct = (PortfolioStructure)targetObj;
		} else {
			return false;
		}

		boolean sameTarget = ePFMgr.isArtefactInStructure(artefact, newParStruct);
		if (sameTarget) {
			return false;
		}
		return true;
	}
	
	// really do the move!
	private boolean moveArtefactToNewParent(UserRequest ureq, AbstractArtefact artefact, Object oldParent, Object newParent){
		if(!(oldParent instanceof PortfolioStructure) || !(newParent instanceof PortfolioStructure)) {
			return false;
		}

		try {
			PortfolioStructure oldParStruct = (PortfolioStructure)oldParent;
			PortfolioStructure newParStruct = (PortfolioStructure)newParent;
			
			if(ePFMgr.moveArtefactFromStructToStruct(artefact, oldParStruct, newParStruct)) {
				reloadTreeModel(oldParStruct, newParStruct);
				fireEvent(ureq, new EPMoveEvent());
				return true;
			}
		} catch (Exception e) {
			logError("could not load artefact, old and new parent", e);
		}
		return false;
	}
	
	private boolean reorder(UserRequest ureq, AbstractArtefact artefact, TreeNode parentNode, Object target){
		Object parentObj = parentNode.getUserObject();
		if(!(parentObj instanceof PortfolioStructure)) {
			return false;
		}
		
		int position = TreeHelper.indexOfByUserObject(target, parentNode);
		int current = TreeHelper.indexOfByUserObject(artefact, parentNode);
		if(current == position) {
			return false;//nothing to do
		} else {
			position++;//drop after
		}

		try {
			PortfolioStructure parStruct = (PortfolioStructure)parentObj;
			//translate in the position in the list of artefacts
			int numOfChildren = ePFMgr.countStructureChildren(parStruct);
			position = position - numOfChildren;
			if(position < 0) {
				position = 0;
			}
			
			if(ePFMgr.moveArtefactInStruct(artefact, parStruct, position)) {
				reloadTreeModel(parStruct, null);
				fireEvent(ureq, new EPMoveEvent());
				return true;
			}
		} catch (Exception e) {
			logError("could not load artefact, old and new parent", e);
		}
		return false;
	}
	
	private boolean checkNewStructureTarget(Object droppedObj, Object droppedParentObj, Object targetObj){
		if(targetObj == null || droppedParentObj == null) {
			return false;
		}
		if (droppedParentObj.equals(targetObj)) {
			return true; // seems only to be a move in order
		}
		if (droppedObj instanceof EPPage && targetObj instanceof EPPage) {
			return false;
		}
		if (droppedObj instanceof EPStructureElement && !(targetObj instanceof EPPage)) {
			return false;
		}
		return true;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		//TODO jquery implements drag and drop logic
		/*
		if (event instanceof TreeNodeClickedEvent) {
			resetClickedNodes();
			TreeNodeClickedEvent treeEv = (TreeNodeClickedEvent) event;
			String nodeClicked = treeEv.getNodeId();
			boolean isArtefactNode = nodeClicked.startsWith(ARTEFACT_NODE_IDENTIFIER);
			if (!nodeClicked.equals(ROOT_NODE_IDENTIFIER) && !isArtefactNode){
				structureClicked = ePFMgr.loadPortfolioStructureByKey(new Long(nodeClicked));
				refreshAddElements(ureq, structureClicked);
				delButton.setVisible(true);
				//send event to load this page
				fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.SELECTED, structureClicked));
				// needed because refreshAddElements set flc dirty, therefore selected node gets lost
				String path = idToPath.get(structureClicked.getKey());
			//TODO jquery treeCtr.selectPath(path);
			} else if (isArtefactNode) {
				artefactNodeClicked = nodeClicked;
				refreshAddElements(ureq, null);
				delButton.setVisible(true);
				String artIdent = getArtefactIdFromNodeId(nodeClicked);
				String path = idToPath.get(new Long(artIdent));
				PortfolioStructure structure = pathToStructure.get(path);
				fireEvent(ureq, new EPArtefactClicked(ARTEFACT_NODE_CLICKED, structure));
				// needed because refreshAddElements set flc dirty, therefore selected node gets lost
			//TODO jquery treeCtr.selectPath(path); 
			} else {
				// root tree node clicked, no add/delete link
				delButton.setVisible(false);
				refreshAddElements(ureq, null);
				fireEvent(ureq, new Event(ARTEFACT_NODE_CLICKED));
			}
		} else if (event instanceof MoveTreeNodeEvent) {
			resetClickedNodes();
			MoveTreeNodeEvent moveEvent = (MoveTreeNodeEvent) event;
			String movedNode = moveEvent.getNodeId();
			String oldParent = moveEvent.getOldParentNodeId();
			String newParent = moveEvent.getNewParentNodeId();
			int newPos = moveEvent.getPosition();
			boolean isArtefactNode = movedNode.startsWith(ARTEFACT_NODE_IDENTIFIER);
			if (isArtefactNode) {
				String nodeId = getArtefactIdFromNodeId(movedNode);
				if (checkNewArtefactTarget(nodeId, newParent)){
					if (moveArtefactToNewParent(nodeId, oldParent, newParent)) {
						if (isLogDebugEnabled()) logInfo("moved artefact " + nodeId + " from structure " + oldParent + " to " + newParent, null);
						moveEvent.setResult(true, null, null);
						// refresh the view
						EPMoveEvent movedEvent = new EPMoveEvent(newParent, nodeId);
						fireEvent(ureq, movedEvent);						
					} else {
						moveEvent.setResult(false, translate("move.error.title"), translate("move.artefact.error.move"));	
					}						
				} else if(oldParent.equals(newParent)) {
					int position = moveEvent.getPosition();
					reorder(nodeId, newParent, position);
					moveEvent.setResult(true, null, null);
					// refresh the view
					EPMoveEvent movedEvent = new EPMoveEvent(newParent, nodeId);
					fireEvent(ureq, movedEvent);
				} else {
					moveEvent.setResult(false, translate("move.error.title"), translate("move.artefact.error.target"));
				}
			} else {
				if (checkNewStructureTarget(movedNode, oldParent, newParent)){
					if (moveStructureToNewParent(movedNode, oldParent, newParent, newPos)) {
						if (isLogDebugEnabled()) logInfo("moved structure " + movedNode + " from structure " + oldParent + " to " + newParent, null);
						moveEvent.setResult(true, null, null);
						// refresh the view
						EPMoveEvent movedEvent = new EPMoveEvent(newParent, movedNode);
						fireEvent(ureq, movedEvent);
					} else {
						moveEvent.setResult(false, translate("move.error.title"), translate("move.struct.error.move"));
					}
				} else {					
					moveEvent.setResult(false, translate("move.error.title"), translate("move.struct.error.target"));
				}
			
			}
		} else */ if (source == addElCtrl){
			// refresh the view, this is a EPStructureChangeEvent
			fireEvent(ureq, event);	
		}
	}
	
	// reset previously choosen nodes. reference were there to be able to delete a node.
	private void resetClickedNodes(){
		structureClicked = null;
		artefactClicked = null;
	}
	
	private String getArtefactIdFromNodeId(String nodeId){
		String artId = nodeId.substring(ARTEFACT_NODE_IDENTIFIER.length());
		if (artId.contains("_")){
			artId = artId.substring(artId.indexOf("_")+1);
		}
		return artId;
	}
	
	private PortfolioStructure getArtefactParentStruct(AbstractArtefact artefact) {
		TreeNode artefactNode = TreeHelper.findNodeByUserObject(artefact, treeCtr.getTreeModel().getRootNode());
		if(artefactNode != null && artefactNode.getParent() != null) {
			Object parentObj = ((TreeNode)artefactNode.getParent()).getUserObject();
			if(parentObj instanceof PortfolioStructure) {
				return (PortfolioStructure)parentObj;
			}
		}

		return null;
	}
	
	/**
	 * check if an artefact might be moved to this new parent node
	 * artefact might be moved to pages or structureElements, but not on maps
	 * @param artefactId
	 * @param structureId
	 * @return
	 */
	private boolean checkNewArtefactTarget(String artefactId, String structureId){
		//artefact cannot be moved directly under root
		if(ROOT_NODE_IDENTIFIER.equals(structureId)) return false;
		
		PortfolioStructure newParStruct;
		AbstractArtefact artefact;
		try {
			artefact = ePFMgr.loadArtefactByKey(new Long(artefactId));
			newParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(structureId));
		} catch (Exception e) {
			logWarn("could not check for valid artefact target", e);
			return false;
		}
		boolean sameTarget = ePFMgr.isArtefactInStructure(artefact, newParStruct);
		if (sameTarget) return false;
		if (newParStruct instanceof EPAbstractMap ) return false;
		return true;
	}
	
	// really do the move!
	private boolean moveArtefactToNewParent(String artefactId, String oldParentId, String newParentId){
		PortfolioStructure newParStruct;
		PortfolioStructure oldParStruct;
		AbstractArtefact artefact;
		try {
			artefact = ePFMgr.loadArtefactByKey(new Long(artefactId));
			oldParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(oldParentId));
			newParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(newParentId));
		} catch (Exception e) {
			logError("could not load artefact, old and new parent", e);
			return false;
		}
		return ePFMgr.moveArtefactFromStructToStruct(artefact, oldParStruct, newParStruct);
	}
	
	// really do the move!
	private boolean reorder(String artefactId, String parentId, int position){
		PortfolioStructure parStruct;
		AbstractArtefact artefact;
		try {
			artefact = ePFMgr.loadArtefactByKey(new Long(artefactId));
			parStruct = ePFMgr.loadPortfolioStructureByKey(new Long(parentId));
			
			//translate in the position in the list of artefacts
			int numOfChildren = ePFMgr.countStructureChildren(parStruct);
			position = position - numOfChildren;
			if(position < 0) {
				position = 0;
			}
		} catch (Exception e) {
			logError("could not load artefact, old and new parent", e);
			return false;
		}
		return ePFMgr.moveArtefactInStruct(artefact, parStruct, position);
	}
	
	/**
	 * check if a structure (page/structEl/map may be dropped here!
	 * its only allowed to move:
	 * - StructureElement from page -> page
	 * - change the order of pages
	 * - change the order of structures 
	 * @param subjectStructId
	 * @param oldParStructId
	 * @param newParStructId
	 * @return
	 */	
	private boolean checkNewStructureTarget(String subjectStructId, String oldParStructId, String newParStructId){
		PortfolioStructure structToBeMvd;
		PortfolioStructure newParStruct;
		if (oldParStructId.equals(newParStructId)) return true; // seems only to be a move in order
		if (newParStructId.equals(ROOT_NODE_IDENTIFIER)) return false;
		try {
			structToBeMvd = ePFMgr.loadPortfolioStructureByKey(new Long(subjectStructId));
			newParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(newParStructId));
		} catch (Exception e) {
			logError("could not check for valid structure target", e);
			return false;
		}
		if (structToBeMvd instanceof EPPage && newParStruct instanceof EPPage) return false;
		if (structToBeMvd instanceof EPStructureElement && !(newParStruct instanceof EPPage)) return false;

		return true;
	}
	
	// really do the move
	private boolean moveStructureToNewParent(String subjectStructId, String oldParStructId, String newParStructId, int newPos){
		PortfolioStructure structToBeMvd;
		PortfolioStructure oldParStruct;
		PortfolioStructure newParStruct;
		try {
			structToBeMvd = ePFMgr.loadPortfolioStructureByKey(new Long(subjectStructId));
			oldParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(oldParStructId));
			newParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(newParStructId));
		} catch (Exception e) {
			logError("could not load: structure to be moved, old or new structure while trying to move", e);
			return false;
		}
		
		if (oldParStructId.equals(newParStructId)) {
			// this is only a position move
			return ePFMgr.moveStructureToPosition(structToBeMvd, newPos);
		}
		
		return ePFMgr.moveStructureToNewParentStructure(structToBeMvd, oldParStruct, newParStruct, newPos);		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//
	}

}
