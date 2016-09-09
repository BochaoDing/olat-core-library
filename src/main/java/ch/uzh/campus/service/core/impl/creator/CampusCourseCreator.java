package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.CampusCourseGroups;
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
import org.olat.course.groupsandrights.CourseGroupManager;
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

    private final RepositoryManager repositoryManager;
    private final RepositoryService repositoryService;
    private final BGAreaManager bgAreaManager;
    private final BusinessGroupService businessGroupService;
    private final CampusCourseConfiguration campusCourseConfiguration;
    private final CampusCourseDescriptionBuilder campusCourseDescriptionBuilder;

    @Autowired
    public CampusCourseCreator(RepositoryManager repositoryManager, RepositoryService repositoryService, BGAreaManager bgAreaManager, BusinessGroupService businessGroupService, CampusCourseConfiguration campusCourseConfiguration, CampusCourseDescriptionBuilder campusCourseDescriptionBuilder) {
        this.repositoryManager = repositoryManager;
        this.repositoryService = repositoryService;
        this.bgAreaManager = bgAreaManager;
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

        // 4. Set access permissions
		if (isDefaultTemplateUsed) {
			cloneOfRepositoryEntry.setAccess(RepositoryEntry.ACC_USERS_GUESTS);
		} else {
			cloneOfRepositoryEntry.setAccess(RepositoryEntry.ACC_OWNERS);
		}
        cloneOfRepositoryEntry.setLastModified(new Date());

		// 5. Persist
		repositoryService.update(cloneOfRepositoryEntry);

        // 6. Load copy of course
        ICourse copyCourse = CourseFactory.loadCourse(cloneOfCourseOlatResourcable.getResourceableId());

        return new CampusCourse(copyCourse, cloneOfRepositoryEntry);
    }

    public CampusCourseGroups createCampusLearningAreaAndCampusBusinessGroups(ICourse course, RepositoryEntry repositoryEntry, Identity creatorIdentity, String lvLanguage) {

        Translator translator = getTranslator(lvLanguage);

        // Check if course has a learning area called campusCourseConfiguration.getCampusCourseLearningAreaName(). If not, create an area with this name.
        String learningAreaName = campusCourseConfiguration.getCampusCourseLearningAreaName();
        BGArea campusLearningArea = bgAreaManager.findBGArea(learningAreaName, repositoryEntry.getOlatResource());
        if (campusLearningArea == null) {
            campusLearningArea = bgAreaManager.createAndPersistBGArea(learningAreaName, translator.translate("campus.course.learningArea.desc"), repositoryEntry.getOlatResource());
        }

        // Check if the learning area contains the business groups called campusCourseConfiguration.getCourseGroupAName() and campusCourseConfiguration.getCourseGroupBName().
        // If not, check if such groups exist outside the learning area. If so, add them to the learning area.
        // If not, create them and add them to the learning area.
        String groupNameA = campusCourseConfiguration.getCourseGroupAName();
        String groupDescriptionA = translator.translate("campus.course.businessGroupA.desc");
        String managedFlagsA = "title,settings,membersmanagement,resources,bookings,delete";
        String groupNameB = campusCourseConfiguration.getCourseGroupBName();
        String groupDescriptionB = translator.translate("campus.course.businessGroupB.desc");
        String managedFlagsB = "title,resources,delete";
        List<BusinessGroup> groupsOfArea = bgAreaManager.findBusinessGroupsOfArea(campusLearningArea);
        BusinessGroup bgA = findCampusCourseGroupInGroupArea(groupsOfArea, groupNameA);
        if (bgA == null) {
            bgA = findCampusCourseGroupInCourseAndAddItToBusinessGroup(course, groupNameA, campusLearningArea);
            if (bgA == null) {
                bgA = createCampusCourseGroupAndAddItToArea(campusLearningArea, creatorIdentity, groupNameA, groupDescriptionA, managedFlagsA, repositoryEntry);
            }
        }
        BusinessGroup bgB = findCampusCourseGroupInGroupArea(groupsOfArea, groupNameB);
        if (bgB == null) {
            bgB = findCampusCourseGroupInCourseAndAddItToBusinessGroup(course, groupNameB, campusLearningArea);
            if (bgB == null) {
                bgB = createCampusCourseGroupAndAddItToArea(campusLearningArea, creatorIdentity, groupNameB, groupDescriptionB, managedFlagsB, repositoryEntry);
            }
        }
        return new CampusCourseGroups(bgA, bgB);
    }

    private BusinessGroup findCampusCourseGroupInGroupArea(List<BusinessGroup> groupsOfArea, String groupName) {
        for (BusinessGroup businessGroup : groupsOfArea) {
            if (businessGroup.getName().equals(groupName)) {
                return businessGroup;
            }
        }
        return null;
    }

    private BusinessGroup findCampusCourseGroupInCourseAndAddItToBusinessGroup(ICourse course, String groupName, BGArea campusLearningArea) {
        BusinessGroup bg = findCampusCourseInCourse(course, groupName);
        if (bg != null) {
            bgAreaManager.addBGToBGArea(bg, campusLearningArea);
        }
        return bg;
    }

    private BusinessGroup findCampusCourseInCourse(ICourse course, String groupName) {
        CourseGroupManager courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();

        List <BusinessGroup> foundCampusGroups = courseGroupManager.getAllBusinessGroups();

        for (BusinessGroup businessGroup : foundCampusGroups ) {
            if (businessGroup.getName().equals(groupName)) {
                return businessGroup;
            }
        }
        return null;
    }

    private BusinessGroup createCampusCourseGroupAndAddItToArea(BGArea campusLearningArea, Identity creatorIdentity, String groupName, String description, String managedFlags, RepositoryEntry repositoryEntry) {
        BusinessGroup bg = businessGroupService.createBusinessGroup(creatorIdentity, groupName, description, null, managedFlags, null, null, false, false, repositoryEntry);
        bgAreaManager.addBGToBGArea(bg, campusLearningArea);
        return bg;
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
            // replace template course vvz link with vvzLink
            newObjective = root.getLearningObjectives().replaceFirst(campusCourseConfiguration.getTemplateCourseVvzLink(), vvzLink);
        }
        return newObjective;
    }

    /**
     * Find the @OLAT-Support email node. Returns null if no such node could be found.
     */
    private CourseNode findOlatSupportEmailNode(CourseNode rootNode) {
        CourseNode olatSupportEmailNode = null;
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            CourseNode element = (CourseNode) rootNode.getChildAt(i);
            if (element.getType().equals(campusCourseConfiguration.getTemplateCourseOlatSupportEmailNodeType()) && element.getShortTitle().contains(campusCourseConfiguration.getTemplateCourseOlatSupportShortTitleSubstring())) {
                olatSupportEmailNode = element;
                break;
            }
        }
        return olatSupportEmailNode;
    }

    /**
     * Sets SentFromCourse in the @OLAT-Support email node.
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
