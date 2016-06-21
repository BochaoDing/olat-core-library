package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.service.CampusCourse;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeConfigFormController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.co.COEditController;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This class holds the helper methods to create a campus course.
 *
 * Initial Date: 07.06.2012 <br>
 * 
 * @author cg
 */
@Component
public class CourseCreator {

    private static final int MAX_DISPLAYNAME_LENGTH = 140;
    private static final String DEFAULT_VVZ_LINK = "http://www.vorlesungen.uzh.ch/";     // this is expected to be found in the defaultTemplate
    private static final String OLAT_SUPPORT_EMAIL_NODE_TYPE = "co";                     // this is expected to be found in the defaultTemplate
    private static final String OLAT_SUPPORT_EMAIL_NODE_SHORT_TITLE_SUBSTRING = "@OLAT"; // this is expected to be found in the defaultTemplate

    @Autowired
    private RepositoryManager repositoryManager;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    DaoManager daoManager;

    @Autowired
    private BGAreaManager areaManager;

    @Autowired
    private BusinessGroupService businessGroupService;

    CampusCourse createCampusCourseFromTemplate(Long templateCourseResourceableId, Identity owner) {
        // 1. Lookup template
        OLATResourceable templateCourse = CourseFactory.loadCourse(templateCourseResourceableId);
        RepositoryEntry sourceRepositoryEntry = repositoryManager.lookupRepositoryEntry(templateCourse, true);

        // 2. Copy RepositoryEntry and implicit the Course
        RepositoryEntry copyOfRepositoryEntry = repositoryService.copy(sourceRepositoryEntry, owner, sourceRepositoryEntry.getDisplayname());
        OLATResourceable copyCourseOlatResourcable = repositoryService.loadRepositoryEntryResource(copyOfRepositoryEntry.getKey());
        ICourse copyCourse = CourseFactory.loadCourse(copyCourseOlatResourcable.getResourceableId());

        return new CampusCourse(copyCourse, copyOfRepositoryEntry);
    }

    void setCourseTitleAndLearningObjectivesInCourseModel(CampusCourse campusCourse, String title, String vvzLink, boolean defaultTemplateUsed, Translator translator) {
        String truncatedTitle = getTruncatedTitle(title);
        String externalLink = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + campusCourse.getRepositoryEntry().getKey();
        String sentFromCourse = "<a href=\"" + externalLink + "\">" + title + "</a>";
        saveCourseTitleInCourseModel(campusCourse.getCourse(), truncatedTitle, translator, defaultTemplateUsed, vvzLink, sentFromCourse);
    }

    void createCampusLearningAreaAndCampusBusinessGroups(CampusCourse campusCourse, Identity creatorIdentity, Translator translator) {
        // Check if course has an area called campus.course.learningArea.name. If not, create an area with this name.
        String areaName = translator.translate("campus.course.learningArea.name");
        BGArea campusLearningArea = areaManager.findBGArea(areaName, campusCourse.getRepositoryEntry().getOlatResource());
        if (campusLearningArea == null) {
            campusLearningArea = areaManager.createAndPersistBGArea(areaName, translator.translate("campus.course.learningArea.desc"), campusCourse.getRepositoryEntry().getOlatResource());
        }

        // Check if the learning area contains business groups campus.course.businessGroupA.name and campus.course.businessGroupB.name.
        // If not, create them and add them to the learning area.
        String groupNameA = translator.translate("campus.course.businessGroupA.name");
        String groupDescriptionA = translator.translate("campus.course.businessGroupA.desc");
        String groupNameB = translator.translate("campus.course.businessGroupB.name");
        String groupDescriptionB = translator.translate("campus.course.businessGroupB.desc");
        List<BusinessGroup> groupsOfArea = areaManager.findBusinessGroupsOfArea(campusLearningArea);
        if (!doesBusinessGroupExist(groupsOfArea, groupNameA)) {
            createBusinessGroupAndAddItToArea(campusLearningArea, businessGroupService, creatorIdentity, groupNameA, groupDescriptionA);
        }
        if (!doesBusinessGroupExist(groupsOfArea, groupNameB)) {
            createBusinessGroupAndAddItToArea(campusLearningArea, businessGroupService, creatorIdentity, groupNameB, groupDescriptionB);
        }
    }

    private static boolean doesBusinessGroupExist(List<BusinessGroup> groupsOfArea, String groupName) {
        for (BusinessGroup businessGroup : groupsOfArea) {
            if (businessGroup.getName().equals(groupName)) {
                return true;
            }
        }
        return false;
    }

    private void createBusinessGroupAndAddItToArea(BGArea campusLernArea, BusinessGroupService businessGroupService, Identity creatorIdentity, String groupName, String description) {
        BusinessGroup bgA = businessGroupService.createBusinessGroup(creatorIdentity, groupName, description, null, null, false, false, null);
        areaManager.addBGToBGArea(bgA, campusLernArea);
    }

    public String getTruncatedTitle(String title) {
        return Formatter.truncate(title, MAX_DISPLAYNAME_LENGTH);
    }

    public boolean descriptionChanged(CampusCourse campusCourse, String newDescription) {
        return (campusCourse.getRepositoryEntry().getDescription() == null && newDescription != null) || !campusCourse.getRepositoryEntry().getDescription().equals(newDescription);
    }

    public boolean titleChanged(CampusCourse campusCourse, String newTitle) {
        return (campusCourse.getRepositoryEntry().getDisplayname() == null && newTitle != null) || !campusCourse.getRepositoryEntry().getDisplayname().equals(getTruncatedTitle(newTitle));
    }

    /**
     * Sets the short title, long title and the learning objective for both course-run and course-editor model. <br>
     * Adds the vvzLink to the learning objectives, if not null.
     * Sets SentFromCourse in the @OLAT-Support email node, if the node exists.
     */
    private void saveCourseTitleInCourseModel(ICourse course, String title, Translator translator, boolean defaultTemplateUsed, String vvzLink, String sentFromCourse) {
        // openCourseEditSession
        CourseFactory.openCourseEditSession(course.getResourceableId());

        // update course run model
        CourseNode runRoot = course.getRunStructure().getRootNode();
        runRoot.setShortTitle(Formatter.truncate(title, NodeConfigFormController.SHORT_TITLE_MAX_LENGTH));
        runRoot.setLongTitle(title);

        String newObjective = getModelObjectivesWithVVZLink(runRoot, translator, defaultTemplateUsed, vvzLink);
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

        String newEditorObjective = getModelObjectivesWithVVZLink(rootNode, translator, defaultTemplateUsed, vvzLink);
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
    private String getModelObjectivesWithVVZLink(CourseNode root, Translator translator, boolean defaultTemplate, String vvzLink) {
        String newObjective;
        if (vvzLink == null || vvzLink.isEmpty()) {
            return null;
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
    private CourseNode findOlatSupportEmailNode(CourseNode rootNode) {
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
     */
    private void setSentFromCourse(CourseNode olatSupportEmailNode, String sentFromCourse) {
    	ModuleConfiguration moduleConfiguration = olatSupportEmailNode.getModuleConfiguration();
        if (moduleConfiguration ==  null) {
            return;
        }
        moduleConfiguration.set(COEditController.CONFIG_KEY_SENT_FROM_COURSE, sentFromCourse);
    }

}
