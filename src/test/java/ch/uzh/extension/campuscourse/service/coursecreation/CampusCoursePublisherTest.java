package ch.uzh.extension.campuscourse.service.coursecreation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.course.tree.CourseEditorTreeModel;

/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
public class CampusCoursePublisherTest {

    private CampusCoursePublisher campusCoursePublisherTestObject;

    private String rootNodeIdent = "rootNodeIdent";
    private CourseEditorTreeModel editorTreeModel;

    @Before
    public void setup() {
        campusCoursePublisherTestObject = new CampusCoursePublisher();

        GenericTreeNode rootNode = new GenericTreeNode("rootNode", null);
        rootNode.setIdent(rootNodeIdent);
        editorTreeModel = mock(CourseEditorTreeModel.class);
        when(editorTreeModel.getRootNode()).thenReturn(rootNode);
    }

    @Test
    public void getAllPublishNodeIds_onlyRootNode() {
        List<String> allNodeIds = campusCoursePublisherTestObject.getAllPublishNodeIds(editorTreeModel);
        assertEquals("Wrong size of nodes", 1, allNodeIds.size());
        allNodeIds.contains(rootNodeIdent);
    }

    @Test
    public void getAllPublishNodeIds_twoChildNodesWithtwoChildNodes() {
        String firstChildNodeIdent = "childIdent1";
        String secondChildNodeIdent = "childIdent2";

        appendChildNodeWithSubChildNode(editorTreeModel.getRootNode(), firstChildNodeIdent);
        appendChildNodeWithSubChildNode(editorTreeModel.getRootNode(), secondChildNodeIdent);

        List<String> allNodeIds = campusCoursePublisherTestObject.getAllPublishNodeIds(editorTreeModel);

        assertEquals("Wrong size of nodes", 7, allNodeIds.size());
        assertTrue(allNodeIds.contains(rootNodeIdent));
        assertTrue(allNodeIds.contains(firstChildNodeIdent));
        assertTrue(allNodeIds.contains(secondChildNodeIdent));
        assertTrue(allNodeIds.contains(firstChildNodeIdent + 1));
        assertTrue(allNodeIds.contains(firstChildNodeIdent + 2));
        assertTrue(allNodeIds.contains(secondChildNodeIdent + 1));
        assertTrue(allNodeIds.contains(secondChildNodeIdent + 2));
    }

    private void appendChildNodeWithSubChildNode(TreeNode rootNode, String childNodeIdent) {
        GenericTreeNode childNode = new GenericTreeNode(childNodeIdent, null);
        childNode.setIdent(childNodeIdent);
        rootNode.addChild(childNode);
        appendSubChildNode(childNode, childNodeIdent + 1);
        appendSubChildNode(childNode, childNodeIdent + 2);
    }

    private void appendSubChildNode(GenericTreeNode childNode, String childNodeIdent) {
        GenericTreeNode subChild = new GenericTreeNode(childNodeIdent, null);
        subChild.setIdent(childNodeIdent);
        childNode.addChild(subChild);
    }

}
