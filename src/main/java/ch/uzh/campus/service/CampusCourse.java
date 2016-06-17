package ch.uzh.campus.service;

import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;

import java.util.List;



/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
public class CampusCourse {
	
	private static final int MAX_DISPLAYNAME_LENGTH = 140;
    
	private static final OLog log = Tracing.createLoggerFor(CampusCourse.class);

    private ICourse course;
    private RepositoryEntry repositoryEntry;
    
    private BGAreaManager areaManager;
    private BusinessGroupService businessGroupService;

    private Translator translator;

    private boolean defaultTemplate;

    public void setDefaultTemplate(boolean defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

    public void setTranslator(Translator translator) {
        this.translator = translator;
    }

    public CampusCourse(ICourse course, RepositoryEntry repositoryEntry, BGAreaManager areaManager, BusinessGroupService businessGroupService) {
        this.course = course;
        this.repositoryEntry = repositoryEntry;
        this.areaManager = areaManager;
        this.businessGroupService = businessGroupService;
    }

    public void setDescription(String eventDescription) {
        log.debug("set description=" + eventDescription);
        repositoryEntry.setDescription(eventDescription);
    }

    public void setTitle(String title) {
        log.debug("set title=" + title);
        String trimedTitle = getTrimedTitle(title);
        repositoryEntry.setDisplayname(trimedTitle);
    }

    public void setCourseTitleAndLearningObjectivesInCourseModel(String title, String vvzLink) {
        String trimedTitle = getTrimedTitle(title);
        String externalLink = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + repositoryEntry.getKey();
        String sentFromCourse = "<a href=\"" + externalLink + "\">" + title + "</a>";
        CourseTitleHelper.saveCourseTitleInCourseModel(course, trimedTitle, translator, defaultTemplate, vvzLink, sentFromCourse);
    }

    public void createCampusLearningAreaAndCampusBusinessGroups(Identity creatorIdentity) {

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

    private boolean doesBusinessGroupExist(List<BusinessGroup> groupsOfArea, String groupName) {
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
        return (repositoryEntry.getDescription() == null && newDescription != null) || !repositoryEntry.getDescription().equals(newDescription);
    }

    public boolean titleChanged(String newTitle) {
        return (repositoryEntry.getDisplayname() == null && newTitle != null) || !repositoryEntry.getDisplayname().equals(getTrimedTitle(newTitle));
    }

    private String getTrimedTitle(String title) {
        return Formatter.truncate(title, MAX_DISPLAYNAME_LENGTH);
    }

}