package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.CampusCourseTool;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
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

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This class holds the helper methods to create a campus course.
 *
 * Initial Date: 07.06.2012 <br>
 * 
 * @author cg
 */
@Component
public class CampusCourseCreator {

    private static final String DEFAULT_VVZ_LINK = "http://www.vorlesungen.uzh.ch/";     // this is expected to be found in the defaultTemplate
    private static final String OLAT_SUPPORT_EMAIL_NODE_TYPE = "co";                     // this is expected to be found in the defaultTemplate
    private static final String OLAT_SUPPORT_EMAIL_NODE_SHORT_TITLE_SUBSTRING = "@OLAT"; // this is expected to be found in the defaultTemplate


    private RepositoryManager repositoryManager;
    private RepositoryService repositoryService;
    private BGAreaManager areaManager;
    private BusinessGroupService businessGroupService;
    private CampusCourseConfiguration campusCourseConfiguration;
    private CampusCourseDescriptionBuilder campusCourseDescriptionBuilder;

    @Autowired
    public CampusCourseCreator(RepositoryManager repositoryManager, RepositoryService repositoryService, BGAreaManager areaManager, BusinessGroupService businessGroupService, CampusCourseConfiguration campusCourseConfiguration, CampusCourseDescriptionBuilder campusCourseDescriptionBuilder) {
        this.repositoryManager = repositoryManager;
        this.repositoryService = repositoryService;
        this.areaManager = areaManager;
        this.businessGroupService = businessGroupService;
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.campusCourseDescriptionBuilder = campusCourseDescriptionBuilder;
    }

    public CampusCourse createCampusCourseFromTemplate(CampusCourseImportTO campusCourseImportData, Long templateCourseResourceableId, Identity owner, boolean isDefaultTemplateUsed) {

        // 1. Lookup template
        ICourse template = CourseFactory.loadCourse(templateCourseResourceableId);
        RepositoryEntry sourceRepositoryEntry = repositoryManager.lookupRepositoryEntry(template, true);

        // 2. Copy repository entry and implicit the Course
        // NB: New displayname must be set when calling repositoryService.copy(). Otherwise copyCourse.getCourseTitle()
        // (see 4.) will still yield the old displayname from sourceRepositoryName. Reason: copyCourse.getCourseTitle()
        // gets its value from a cache, which is not updated when displayname of repositoryEntry is changed!
        String displayname = CampusCourseTool.getTruncatedDisplayname(campusCourseImportData.getTitle());
        RepositoryEntry cloneOfRepositoryEntry = repositoryService.copy(sourceRepositoryEntry, owner, displayname);
        OLATResourceable cloneOfCourseOlatResourcable = repositoryService.loadRepositoryEntryResource(cloneOfRepositoryEntry.getKey());

        // 3. Set correct description and access and update repository entry
        String lvLanguage = campusCourseConfiguration.getTemplateLanguage(campusCourseImportData.getLanguage());
        String description = campusCourseDescriptionBuilder.buildDescriptionFrom(campusCourseImportData, lvLanguage);
        cloneOfRepositoryEntry.setDescription(description);
        if (isDefaultTemplateUsed) {
            cloneOfRepositoryEntry.setAccess(RepositoryEntry.ACC_USERS_GUESTS);
        }
        repositoryService.update(cloneOfRepositoryEntry);

        // 4. Set access permissions
        cloneOfRepositoryEntry.setAccess(RepositoryEntry.ACC_OWNERS);
        cloneOfRepositoryEntry.setMembersOnly(true);
        cloneOfRepositoryEntry.setLastModified(new Date());

        // 5. Load copy of course
        ICourse copyCourse = CourseFactory.loadCourse(cloneOfCourseOlatResourcable.getResourceableId());

        return new CampusCourse(copyCourse, cloneOfRepositoryEntry);
    }

    public void createCampusLearningAreaAndCampusBusinessGroups(RepositoryEntry repositoryEntry, Identity creatorIdentity, String lvLanguage) {

        Translator translator = getTranslator(lvLanguage);

        // Check if course has an area called campus.course.learningArea.name. If not, create an area with this name.
        String areaName = translator.translate("campus.course.learningArea.name");
        BGArea campusLearningArea = areaManager.findBGArea(areaName, repositoryEntry.getOlatResource());
        if (campusLearningArea == null) {
            campusLearningArea = areaManager.createAndPersistBGArea(areaName, translator.translate("campus.course.learningArea.desc"), repositoryEntry.getOlatResource());
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



    /**
     * Sets the short title, long title and the learning objective for both course-run and course-editor model. <br>
     * Adds the vvzLink to the learning objectives, if not null. <br>
     * Sets SentFromCourse in the @OLAT-Support email node, if the node exists.
     */
    public void updateCourseRunAndEditorModels(CampusCourse campusCourse, String title, String vvzLink, boolean isDefaultTemplateUsed, String lvLanguage) {

        ICourse course = campusCourse.getCourse();
        String truncatedDisplayname = CampusCourseTool.getTruncatedDisplayname(title);
        Translator translator = getTranslator(lvLanguage);

        // Open courseEditSession
        CourseFactory.openCourseEditSession(course.getResourceableId());

        // Update course run model
        CourseNode runRoot = course.getRunStructure().getRootNode();
        runRoot.setShortTitle(Formatter.truncate(truncatedDisplayname, NodeConfigFormController.SHORT_TITLE_MAX_LENGTH));
        runRoot.setLongTitle(truncatedDisplayname);

        String newObjective = getModelObjectivesWithVVZLink(runRoot, translator, isDefaultTemplateUsed, vvzLink);
        if (newObjective != null && !newObjective.isEmpty()) {
            runRoot.setLearningObjectives(newObjective);
        }

        // Set sentFromCourse in the @OLAT-Support email node
        CourseNode olatSupportEmailNode = null;
        String externalLink = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + campusCourse.getRepositoryEntry().getKey();
        String sentFromCourse = "<a href=\"" + externalLink + "\">" + title + "</a>";
        if (!sentFromCourse.isEmpty()) {
            olatSupportEmailNode = findOlatSupportEmailNode(runRoot);
            if (olatSupportEmailNode != null) {
                setSentFromCourse(olatSupportEmailNode, sentFromCourse);
            }
        }

        CourseFactory.saveCourse(course.getResourceableId());

        // Update course editor model
        CourseEditorTreeModel cetm = course.getEditorTreeModel();
        CourseNode rootNode = cetm.getCourseNode(course.getRunStructure().getRootNode().getIdent());
        rootNode.setShortTitle(Formatter.truncate(truncatedDisplayname, NodeConfigFormController.SHORT_TITLE_MAX_LENGTH));
        rootNode.setLongTitle(truncatedDisplayname);

        String newEditorObjective = getModelObjectivesWithVVZLink(rootNode, translator, isDefaultTemplateUsed, vvzLink);
        if (newEditorObjective != null && !newEditorObjective.isEmpty()) {
            rootNode.setLearningObjectives(newEditorObjective);
            cetm.setLatestPublishTimestamp(-1);
        }

        // Set sentFromCourse in the @OLAT-Support email node
        if (!sentFromCourse.isEmpty() && olatSupportEmailNode != null) {
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
    private String getModelObjectivesWithVVZLink(CourseNode root, Translator translator, boolean isDefaultTemplateUsed, String vvzLink) {
        String newObjective;
        if (vvzLink == null || vvzLink.isEmpty()) {
            return null;
        }
        if (!isDefaultTemplateUsed) {
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
            if (element.getType().equals(OLAT_SUPPORT_EMAIL_NODE_TYPE) && element.getShortTitle().contains(OLAT_SUPPORT_EMAIL_NODE_SHORT_TITLE_SUBSTRING)) {
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

    Translator getTranslator(String lvLanguage) {
        String supportedLvLanguage = campusCourseConfiguration.getTemplateLanguage(lvLanguage);
        return  Util.createPackageTranslator(this.getClass(), new Locale(supportedLvLanguage));
    }
}
