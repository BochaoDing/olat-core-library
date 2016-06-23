/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package ch.uzh.campus.service.core.impl.creator;

import org.olat.core.id.Identity;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.editor.PublishProcess;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
@Component
public class CampusCoursePublisher {

    public void publish(ICourse course, Identity publisherIdentity) {
        Locale locale = new Locale("de");
        publish(course, locale, publisherIdentity);
    }

    public void publish(ICourse course, Locale locale, Identity publisherIdentity) {
        CourseFactory.openCourseEditSession(course.getResourceableId());
        CourseEditorTreeModel cetm = course.getEditorTreeModel();
        PublishProcess pp = PublishProcess.getInstance(course, cetm, locale);
        pp.createPublishSetFor(getAllPublishNodeIds(course.getEditorTreeModel()));
        pp.applyPublishSet(publisherIdentity, locale);
        CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
    }

    List<String> getAllPublishNodeIds(CourseEditorTreeModel editorTreeModel) {
        final List<String> nodeIds = new ArrayList<>();
        addChildNodeIdRecursive(nodeIds, editorTreeModel.getRootNode());
        return nodeIds;
    }

    private void addChildNodeIdRecursive(List<String> nodeIds, INode node) {
        nodeIds.add(node.getIdent());
        for (int i = 0; i < node.getChildCount(); i++) {
            if (node.getChildAt(i).getClass() == CourseEditorTreeNode.class) {
                // CourseEditorTreeNodes will only be published if new, dirty or deleted
                CourseEditorTreeNode child = (CourseEditorTreeNode) node.getChildAt(i);
                if (child.isNewnode() || child.isDirty() || child.isDeleted()) {
                    addChildNodeIdRecursive(nodeIds, node.getChildAt(i));
                }
            } else {
                addChildNodeIdRecursive(nodeIds, node.getChildAt(i));
            }
        }
    }

}
