package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.service.core.impl.syncer.CampusCourseRepositoryEntrySynchronizer;
import ch.uzh.campus.service.data.OlatCampusCourse;
import ch.uzh.campus.service.data.SapCampusCourseTO;
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
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
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

    private final RepositoryManager repositoryManager;
    private final RepositoryService repositoryService;
    private final CampusCourseConfiguration campusCourseConfiguration;
    private final CampusCourseRepositoryEntryDescriptionBuilder campusCourseRepositoryEntryDescriptionBuilder;

    @Autowired
    public OlatCampusCourseCreator(RepositoryManager repositoryManager, RepositoryService repositoryService, CampusCourseConfiguration campusCourseConfiguration, CampusCourseRepositoryEntryDescriptionBuilder campusCourseRepositoryEntryDescriptionBuilder) {
        this.repositoryManager = repositoryManager;
        this.repositoryService = repositoryService;
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.campusCourseRepositoryEntryDescriptionBuilder = campusCourseRepositoryEntryDescriptionBuilder;
    }

    public OlatCampusCourse createOlatCampusCourseFromTemplate(SapCampusCourseTO sapCampusCourseTO, OLATResource templateOlatResource, Identity owner, boolean isStandardTemplateUsed) {

        // 1. Lookup template
        ICourse templateCourse = CourseFactory.loadCourse(templateOlatResource.getResourceableId());
        RepositoryEntry templateRepositoryEntry = repositoryManager.lookupRepositoryEntry(templateCourse, true);

        // 2. Copy repository entry and implicit the Course
        // NB: New displayname must be set when calling repositoryService.copy(). Otherwise copyCourse.getCourseTitle()
        // (see 4.) will still yield the old displayname from sourceRepositoryName. Reason: copyCourse.getCourseTitle()
        // gets its value from a cache, which is not updated when displayname of repositoryEntry is changed!
        String newDisplayname = CampusCourseRepositoryEntrySynchronizer.truncateTitleForRepositoryEntryDisplayname(sapCampusCourseTO.getTitleToBeDisplayed());
        RepositoryEntry cloneOfTemplateRepositoryEntry = repositoryService.copy(templateRepositoryEntry, owner, newDisplayname);
        OLATResourceable cloneOfTemplateCourseOlatResourcable = repositoryService.loadRepositoryEntryResource(cloneOfTemplateRepositoryEntry.getKey());

        // 3. Set correct description and access and update repository entry
        String lvLanguage = campusCourseConfiguration.getSupportedTemplateLanguage(sapCampusCourseTO.getLanguage());
        String newDescription = campusCourseRepositoryEntryDescriptionBuilder.buildDescriptionFrom(sapCampusCourseTO, lvLanguage);
        cloneOfTemplateRepositoryEntry.setDescription(newDescription);

        // 4. Set access permissions
		if (isStandardTemplateUsed) {
			cloneOfTemplateRepositoryEntry.setAccess(RepositoryEntry.ACC_USERS_GUESTS);
		} else {
			cloneOfTemplateRepositoryEntry.setAccess(RepositoryEntry.ACC_OWNERS);
		}
        cloneOfTemplateRepositoryEntry.setLastModified(new Date());

		// 5. Persist
		repositoryService.update(cloneOfTemplateRepositoryEntry);

        // 6. Load copy of course
        ICourse copyOfTemplateCourse = CourseFactory.loadCourse(cloneOfTemplateCourseOlatResourcable.getResourceableId());

        return new OlatCampusCourse(copyOfTemplateCourse, cloneOfTemplateRepositoryEntry);
    }

    /**
     * Sets the short title, long title and the learning objective for both course-run and course-editor model. <br>
     * Adds the vvzLink to the learning objectives, if not null. <br>
     * Sets SentFromCourse in the @OLAT-Support email node, if the node exists.
     */
    public void updateCourseRunAndEditorModels(ICourse course, Long repositoryEntryKey, String title, String vvzLink, String lvLanguage, boolean isStandardTemplateUsed) {

        Translator translator = getTranslator(lvLanguage);

        // Open courseEditSession
        CourseFactory.openCourseEditSession(course.getResourceableId());

        // Update course run model
        CourseNode runRoot = course.getRunStructure().getRootNode();
        runRoot.setShortTitle(Formatter.truncate(title, NodeConfigFormController.SHORT_TITLE_MAX_LENGTH));
        runRoot.setLongTitle(title);

        String newObjective = getModelObjectivesWithVVZLink(runRoot, translator, isStandardTemplateUsed, vvzLink);
        if (newObjective != null && !newObjective.isEmpty()) {
            runRoot.setLearningObjectives(newObjective);
        }

        // Set sentFromCourse in the @OLAT-Support email node
        CourseNode olatSupportEmailNode = null;
        String externalLink = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + repositoryEntryKey;
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
        rootNode.setShortTitle(Formatter.truncate(title, NodeConfigFormController.SHORT_TITLE_MAX_LENGTH));
        rootNode.setLongTitle(title);

        String newEditorObjective = getModelObjectivesWithVVZLink(rootNode, translator, isStandardTemplateUsed, vvzLink);
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
