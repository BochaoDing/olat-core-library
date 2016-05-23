package ch.uzh.campus.service;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeConfigFormController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.co.COEditController;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.ModuleConfiguration;

/**
 * Initial Date: 07.06.2012 <br>
 * 
 * @author cg
 */
public class CourseTitleHelper {

    private static final String DEFAULT_VVZ_LINK = "http://www.vorlesungen.uzh.ch/";     // this is expected to be found in the defaultTemplate
    private static final String OLAT_SUPPORT_EMAIL_NODE_TYPE = "co";                     // this is expected to be found in the defaultTemplate
    private static final String OLAT_SUPPORT_EMAIL_NODE_SHORT_TITLE_SUBSTRING = "@OLAT"; // this is expected to be found in the defaultTemplate

    public static void saveCourseTitleInCourseModel(ICourse course, String title) {
        saveCourseTitleInCourseModel(course, title, null, true, null, null);
    }

    /**
     * Sets the short title, long title and the learning objective for both course-run and course-editor model. <br>
     * Adds the vvzLink to the learning objectives, if not null.
     * Sets SentFromCourse in the @OLAT-Support email node, if the node exists.
     */
    public static void saveCourseTitleInCourseModel(ICourse course, String title, Translator translator, boolean defaultTemplate, String vvzLink, String sentFromCourse) {
        // openCourseEditSession
        CourseFactory.openCourseEditSession(course.getResourceableId());

        // update course run model
        CourseNode runRoot = course.getRunStructure().getRootNode();
        runRoot.setShortTitle(Formatter.truncate(title, NodeConfigFormController.SHORT_TITLE_MAX_LENGTH));
        runRoot.setLongTitle(title);

        String newObjective = getModelObjectivesWithVVZLink(runRoot, translator, defaultTemplate, vvzLink);
        if (newObjective != null && !newObjective.isEmpty()) {
            // System.out.println(newObjective);
            runRoot.setLearningObjectives(newObjective);
        }

        // Set sentFromCourse in the @OLAT-Support email node
        CourseNode olatSupportEmailNode = null;
        if (sentFromCourse != null && !sentFromCourse.isEmpty()) {
            olatSupportEmailNode = findOlatSupportEmailNode(runRoot);
            if (olatSupportEmailNode != null) {
                setSentFromCourse(olatSupportEmailNode, sentFromCourse);
            }
        }

        CourseFactory.saveCourse(course.getResourceableId());

        // update course editor model
        CourseEditorTreeModel cetm = course.getEditorTreeModel();
        CourseNode rootNode = cetm.getCourseNode(course.getRunStructure().getRootNode().getIdent());
        rootNode.setShortTitle(Formatter.truncate(title, NodeConfigFormController.SHORT_TITLE_MAX_LENGTH));
        rootNode.setLongTitle(title);

        String newEditorObjective = getModelObjectivesWithVVZLink(rootNode, translator, defaultTemplate, vvzLink);
        if (newEditorObjective != null && !newEditorObjective.isEmpty()) {
            rootNode.setLearningObjectives(newEditorObjective);
            cetm.setLatestPublishTimestamp(-1);
        }

        // Set sentFromCourse in the @OLAT-Support email node
        if (sentFromCourse != null && !sentFromCourse.isEmpty() && olatSupportEmailNode != null) {
            setSentFromCourse(cetm.getCourseNode(olatSupportEmailNode.getIdent()), sentFromCourse);
        }

        course.getEditorTreeModel().nodeConfigChanged(course.getRunStructure().getRootNode());
        CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
        CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
    }

    /**
     * This is only CampusCourse relevant. The vvzLink should only be not null and not empty at creating campus courses. <br>
     * if vvzLink is null or empty, returns null <br>
     * if no default template is used, the new objectives string is a parameterized translation <br>
     * if a default template is used, the default learningObjectives of the template is reused and vvzLink replaces DEFAULT_VVZ_LINK
     * 
     */
    private static String getModelObjectivesWithVVZLink(CourseNode root, Translator translator, boolean defaultTemplate, String vvzLink) {
        String newObjective = null;
        if (vvzLink == null || vvzLink.isEmpty()) {
            return newObjective;
        }
        if (!defaultTemplate) {
            newObjective = translator.translate("campus.course.learningObj", new String[] { vvzLink });
        } else {
            // replace DEFAULT_VVZ_LINK with vvzLink
            newObjective = root.getLearningObjectives().replaceFirst(DEFAULT_VVZ_LINK, vvzLink);
        }
        return newObjective;
    }

    /**
     * Find the @OLAT-Support email node. Returns null if no such node could be found.
     * This is only CampusCourse relevant.
     */
    private static CourseNode findOlatSupportEmailNode(CourseNode rootNode) {
        CourseNode olatSupportEmailNode = null;
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            CourseNode element = (CourseNode) rootNode.getChildAt(i);
            if (element.getType().equals(OLAT_SUPPORT_EMAIL_NODE_TYPE)  && element.getShortTitle().contains(OLAT_SUPPORT_EMAIL_NODE_SHORT_TITLE_SUBSTRING)) {
                olatSupportEmailNode = element;
                break;
            }
        }
        return olatSupportEmailNode;
    }

    /**
     * Sets SentFromCourse in the @OLAT-Support email node.
     * This is only CampusCourse relevant.
     * <p>
     * 
     */
    private static void setSentFromCourse(CourseNode olatSupportEmailNode, String sentFromCourse) {
    	//TODO: olatng
    	/*ModuleConfiguration moduleConfiguration = olatSupportEmailNode.getModuleConfiguration();
        if (moduleConfiguration ==  null) {
            return;
        }
        moduleConfiguration.set(COEditController.CONFIG_KEY_SENT_FROM_COURSE, sentFromCourse);
        */
    }

}
