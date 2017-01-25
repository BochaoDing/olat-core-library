package ch.uzh.extension.campuscourse.service.coursecreation;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.service.synchronization.CampusCourseRepositoryEntryDescriptionBuilder;
import ch.uzh.extension.campuscourse.service.synchronization.CampusCourseRepositoryEntrySynchronizer;
import ch.uzh.extension.campuscourse.model.CampusCourseTO;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeConfigFormController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.co.COEditController;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Locale;

/**
 * This class holds the helper methods to create a campus course.
 *
 * Initial Date: 07.06.2012 <br>
 * 
 * @author cg
 */
@Component
public class OlatCampusCourseCreator {

    private final RepositoryService repositoryService;
    private final CampusCourseConfiguration campusCourseConfiguration;
    private final CampusCourseRepositoryEntryDescriptionBuilder campusCourseRepositoryEntryDescriptionBuilder;

    @Autowired
    public OlatCampusCourseCreator(RepositoryService repositoryService, CampusCourseConfiguration campusCourseConfiguration, CampusCourseRepositoryEntryDescriptionBuilder campusCourseRepositoryEntryDescriptionBuilder) {
        this.repositoryService = repositoryService;
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.campusCourseRepositoryEntryDescriptionBuilder = campusCourseRepositoryEntryDescriptionBuilder;
    }

    public RepositoryEntry createOlatCampusCourseFromTemplate(RepositoryEntry templateRepositoryEntry, CampusCourseTO campusCourseTO, Identity owner, boolean isStandardTemplateUsed) {

        // 1. Copy repository entry and implicit the Course
        // NB: New displayname must be set when calling repositoryService.copy(). Otherwise copyCourse.getCourseTitle()
        // (see 4.) will still yield the old displayname from sourceRepositoryName. Reason: copyCourse.getCourseTitle()
        // gets its value from a cache, which is not updated when displayname of repositoryEntry is changed!
        String newDisplayname = CampusCourseRepositoryEntrySynchronizer.truncateTitleForRepositoryEntryDisplayname(campusCourseTO.getTitleToBeDisplayed());
        RepositoryEntry cloneOfTemplateRepositoryEntry = repositoryService.copy(templateRepositoryEntry, owner, newDisplayname);

        // 2. Set correct description and access and update repository entry
        String newDescription = campusCourseRepositoryEntryDescriptionBuilder.buildDescription(campusCourseTO);
        cloneOfTemplateRepositoryEntry.setDescription(newDescription);

        // 3. Set access permissions
		if (isStandardTemplateUsed) {
			cloneOfTemplateRepositoryEntry.setAccess(RepositoryEntry.ACC_USERS_GUESTS);
		} else {
			cloneOfTemplateRepositoryEntry.setAccess(RepositoryEntry.ACC_OWNERS);
		}
        cloneOfTemplateRepositoryEntry.setLastModified(new Date());

		// 4. Persist
		repositoryService.update(cloneOfTemplateRepositoryEntry);

        return cloneOfTemplateRepositoryEntry;
    }

    /**
     * Sets the short title, long title and the learning objective for both course-run and course-editor model. <br>
     * Adds the vvzLink to the learning objectives, if not null. <br>
     * Sets SentFromCourse in the @OLAT-Support email node, if the node exists.
     */
    public ICourse updateCourseRunAndEditorModels(RepositoryEntry repositoryEntry, CampusCourseTO campusCourseTO, boolean isStandardTemplateUsed) {

        Translator translator = getTranslator(campusCourseTO.getLanguage());

        // Load course
        ICourse course = CourseFactory.loadCourse(repositoryEntry);

        // Open courseEditSession
        CourseFactory.openCourseEditSession(course.getResourceableId());

        // Update course run model
        CourseNode runRoot = course.getRunStructure().getRootNode();
        runRoot.setShortTitle(Formatter.truncate(campusCourseTO.getTitleToBeDisplayed(), NodeConfigFormController.SHORT_TITLE_MAX_LENGTH));
        runRoot.setLongTitle(campusCourseTO.getTitleToBeDisplayed());

        String newObjective = getModelObjectivesWithVVZLink(runRoot, translator, isStandardTemplateUsed, campusCourseTO.getVvzLink());
        if (newObjective != null && !newObjective.isEmpty()) {
            runRoot.setLearningObjectives(newObjective);
        }

        // Set sentFromCourse in the @OLAT-Support email node
        CourseNode olatSupportEmailNode = null;
        String externalLink = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + repositoryEntry.getKey();
        String sentFromCourse = "<a href=\"" + externalLink + "\">" + campusCourseTO.getTitleToBeDisplayed() + "</a>";
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
        rootNode.setShortTitle(Formatter.truncate(campusCourseTO.getTitleToBeDisplayed(), NodeConfigFormController.SHORT_TITLE_MAX_LENGTH));
        rootNode.setLongTitle(campusCourseTO.getTitleToBeDisplayed());

        String newEditorObjective = getModelObjectivesWithVVZLink(rootNode, translator, isStandardTemplateUsed, campusCourseTO.getVvzLink());
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

        return course;
    }

    /**
     * This is only CampusCourse relevant. The vvzLink should only be not null and not empty at creating campus courses. <br>
     * if vvzLink is null or empty, returns null <br>
     * if no default template is used, the new objectives string is a parameterized translation <br>
     * if a default template is used, the default learningObjectives of the template is reused and vvzLink replaces DEFAULT_VVZ_LINK
     * 
     */
    private String getModelObjectivesWithVVZLink(CourseNode root, Translator translator, boolean isStandardTemplateUsed, String vvzLink) {
        String newObjective;
        if (vvzLink == null || vvzLink.isEmpty()) {
            return null;
        }
        if (!isStandardTemplateUsed) {
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

    private Translator getTranslator(String lvLanguage) {
        String supportedLvLanguage = campusCourseConfiguration.getSupportedTemplateLanguage(lvLanguage);
        return  Util.createPackageTranslator(this.getClass(), new Locale(supportedLvLanguage));
    }
}
