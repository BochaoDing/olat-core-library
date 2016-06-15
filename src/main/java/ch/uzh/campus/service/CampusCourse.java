package ch.uzh.campus.service;

import java.util.List;


import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;


import ch.uzh.campus.service.core.impl.syncer.CampusGroupHelper;

import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;



/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
public class CampusCourse {
	
	public static final int MAX_DISPLAYNAME_LENGTH = 140;
    
	private static final OLog log = Tracing.createLoggerFor(CampusCourse.class);

    private ICourse course;
    private RepositoryEntry repositoryEntry;
    
    private BGAreaManager areaManager;
    private BusinessGroupService businessGroupService;

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
    public CampusCourse(ICourse course, RepositoryEntry repositoryEntry, BGAreaManager areaManager) {
        this.course = course;
        this.repositoryEntry = repositoryEntry;
        this.areaManager = areaManager;
        businessGroupService = (BusinessGroupService) CoreSpringFactory.getBean(BusinessGroupService.class);
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
        String sentFromCourse = getHtmlHref(externalLink, title);
        CourseTitleHelper.saveCourseTitleInCourseModel(course, trimedTitle, translator, defaultTemplate, vvzLink, sentFromCourse);
    }
    
    /**
     * @return an HTML href string.
     */
    public static String getHtmlHref(String url, String title) {
        return "<a href=\"" + url + "\">" + title + "</a>";
    }

    /**
     * Checks if this course has an area, if not, create an area with a default name.
     * If the area exists, check that it has groupA and a groupB, else creates groups and add them to area.
     * 
     * @param creatorIdentity
     */
    public void addGroupsToArea(Identity creatorIdentity) {
    	//TODO: olatng: please review
    	String areaName = translator.translate("campus.course.learningArea.name");
    	
    	boolean areaExists = areaManager.existArea(areaName,repositoryEntry.getOlatResource()) ;
    	BGArea campusLernArea = null;
    	if(!areaExists) {
    		campusLernArea = areaManager.createAndPersistBGArea(areaName, translator.translate("campus.course.learningArea.desc"), repositoryEntry.getOlatResource());
    	} else {
    		List<BGArea> areas = areaManager.findBGAreasInContext(repositoryEntry.getOlatResource());
    		for(BGArea area: areas) {
    			if(areaName.equals(area.getName())) {
    				campusLernArea = area;
    			}
    		}
    	}    	    	
    	    	    	
    	// CREATE THE BusinessGroup(s) ADD THEM TO THE APPROPRIATE BGArea IF NOT ALREADY EXIST
    	
    	List<BusinessGroup> groupsOfArea = areaManager.findBusinessGroupsOfArea(campusLernArea);    	
    	
    	String groupNameA = translator.translate("campus.course.businessGroupA.name");
		String groupDescriptionA = translator.translate("campus.course.businessGroupA.desc");
		String groupNameB = translator.translate("campus.course.businessGroupB.name");
		String groupDescriptionB = translator.translate("campus.course.businessGroupB.desc");
		
		if(groupsOfArea.size()==0) {
    		createBusinessGroupInArea(campusLernArea, businessGroupService, creatorIdentity, groupNameA, groupDescriptionA);    		
			createBusinessGroupInArea(campusLernArea, businessGroupService, creatorIdentity, groupNameB, groupDescriptionB);    		
    	} else {
    		BusinessGroup bgA = CampusGroupHelper.lookupCampusGroup(course,  groupNameA);
    		if(bgA==null) {
    			createBusinessGroupInArea(campusLernArea, businessGroupService, creatorIdentity, groupNameA, groupDescriptionA);    	
    		}
    		BusinessGroup bgB = CampusGroupHelper.lookupCampusGroup(course,  groupNameB);
    		if(bgB==null) {
    			createBusinessGroupInArea(campusLernArea, businessGroupService, creatorIdentity, groupNameB, groupDescriptionB);    	
    		}
    	}    	
    }

	private void createBusinessGroupInArea(BGArea campusLernArea, BusinessGroupService businessGroupService, Identity creatorIdentity, String name, String description) {
		BusinessGroup bgA = businessGroupService.createBusinessGroup(creatorIdentity, name, description, null, null, false, false, null);
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
        if (repositoryEntry.getDescription() == null && newDescription != null) {
            return true;
        }
        return !repositoryEntry.getDescription().equals(newDescription);
    }

    public boolean titleChanged(String newTitle) {
        if (repositoryEntry.getDisplayname() == null && newTitle != null) {
            return true;
        }
        return !repositoryEntry.getDisplayname().equals(getTrimedTitle(newTitle));
    }

    private String getTrimedTitle(String title) {
        return Formatter.truncate(title, MAX_DISPLAYNAME_LENGTH);
    }

}