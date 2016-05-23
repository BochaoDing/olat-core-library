package ch.uzh.campus.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/*import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.area.BGArea;
import org.olat.data.group.area.BGAreaDao;
import org.olat.data.group.area.BGAreaDaoImpl;
import org.olat.data.group.context.BGContext;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.CourseTitleHelper;
import org.olat.lms.course.ICourse;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.Settings;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
*/
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;


/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
public class CampusCourse {
    
	private static final OLog log = Tracing.createLoggerFor(CampusCourse.class);

    private ICourse course;
    private RepositoryEntry repositoryEntry;

    private Translator translator;

    private boolean defaultTemplate;

    public boolean isDefaultTemplate() {
        return defaultTemplate;
    }

    public void setDefaultTemplate(boolean defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

    public void setTranslator(Translator translator) {
        this.translator = translator;
    }

    /**
     * @param course
     * @param repositoryEntry
     */
    public CampusCourse(ICourse course, RepositoryEntry repositoryEntry) {
        this.course = course;
        this.repositoryEntry = repositoryEntry;

    }

    public void setDescription(String eventDescription) {
        log.debug("set description=" + eventDescription);
        repositoryEntry.setDescription(eventDescription);
    }

    /*public void setTitle(String title) {
        log.debug("set title=" + title);
        String trimedTitle = getTrimedTitle(title);
        repositoryEntry.setDisplayname(trimedTitle);
    }

    public void setCourseTitleAndLearningObjectivesInCourseModel(String title, String vvzLink) {
        String trimedTitle = getTrimedTitle(title);
        String externalLink = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + repositoryEntry.getKey();
        String sentFromCourse = Formatter.getHtmlHref(externalLink, title);
        CourseTitleHelper.saveCourseTitleInCourseModel(course, trimedTitle, translator, defaultTemplate, vvzLink, sentFromCourse);
    }*/

    /*public void SetBusinessGroups(Identity identity) {

        BGContext defaultBGContext = getDefaultBGContext(getCourse());

        BGAreaDao areaManager = BGAreaDaoImpl.getInstance();
        BGArea campusLernArea = areaManager.findBGArea(translator.translate("campus.course.learningArea.name"), defaultBGContext);
        if (campusLernArea == null) {
            // CREATE THE BGArea
            campusLernArea = areaManager.createAndPersistBGAreaIfNotExists(translator.translate("campus.course.learningArea.name"),
                    translator.translate("campus.course.learningArea.desc"), defaultBGContext);
        }

        BusinessGroupService businessGroupService = (BusinessGroupService) CoreSpringFactory.getBean(BusinessGroupService.class);

        // CREATE THE BusinessGroup(s) ADD THEM TO THE APPROPRIATE BGArea IF NOT ALREADY EXIST

        BusinessGroup bgA = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, identity,
                translator.translate("campus.course.businessGroupA.name"), translator.translate("campus.course.businessGroupA.desc"), null, null, false, false,
                defaultBGContext);
        // if (bgA == null) {
        // bgA = CampusGroupHelper.lookupCampusGroup(getCourse(), translator.translate("campus.course.businessGroupA.name"));
        // }
        if (bgA != null) {
            areaManager.addBGToBGArea(businessGroupService.loadBusinessGroup(bgA), campusLernArea);
        }

        BusinessGroup bgB = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, identity,
                translator.translate("campus.course.businessGroupB.name"), translator.translate("campus.course.businessGroupB.desc"), null, null, false, false,
                defaultBGContext);

        // if (bgB == null) {
        // bgB = CampusGroupHelper.lookupCampusGroup(getCourse(), translator.translate("campus.course.businessGroupB.name"));
        // }
        if (bgB != null) {
            areaManager.addBGToBGArea(businessGroupService.loadBusinessGroup(bgB), campusLernArea);
        }
    }*/

    /*private BGContext getDefaultBGContext(ICourse course) {
        CourseGroupManager courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();
        List<BGContext> courseLGContextes = courseGroupManager.getLearningGroupContexts(course.getCourseEnvironment().getCourseOLATResourceable());

        for (BGContext bctxt : courseLGContextes) {
            if (bctxt.isDefaultContext()) {
                return bctxt;
            }
        }
        return null;

    }*/

    public ICourse getCourse() {
        return course;
    }

    public RepositoryEntry getRepositoryEntry() {
        return repositoryEntry;
    }

    /**
     * @param accessRights
     *            Use access-constants in RepositoryEntry e.g. RepositoryEntry.ACC_USERS_GUESTS
     */
    public void setRepositoryAccessRights(int accessRights) {
        getRepositoryEntry().setAccess(accessRights);
    }

    public boolean descriptionChanged(String newDescription) {
        if (repositoryEntry.getDescription() == null && newDescription != null) {
            return true;
        }
        return !repositoryEntry.getDescription().equals(newDescription);
    }

    /*public boolean titleChanged(String newTitle) {
        if (repositoryEntry.getDisplayname() == null && newTitle != null) {
            return true;
        }
        return !repositoryEntry.getDisplayname().equals(getTrimedTitle(newTitle));
    }

    private String getTrimedTitle(String title) {
        return Formatter.truncate(title, RepositoryEntry.MAX_DISPLAYNAME_LENGTH);
    }*/

}