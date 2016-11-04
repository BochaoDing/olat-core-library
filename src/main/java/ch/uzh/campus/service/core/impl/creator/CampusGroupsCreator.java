package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.service.data.CampusGroups;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * @author Martin Schraner
 */
@Service
public class CampusGroupsCreator {

    private final BGAreaManager bgAreaManager;
    private final BusinessGroupService businessGroupService;
    private final CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    public CampusGroupsCreator(BGAreaManager bgAreaManager, BusinessGroupService businessGroupService, CampusCourseConfiguration campusCourseConfiguration) {
        this.bgAreaManager = bgAreaManager;
        this.businessGroupService = businessGroupService;
        this.campusCourseConfiguration = campusCourseConfiguration;
    }

    public CampusGroups createCampusLearningAreaAndCampusGroupsIfNecessary(RepositoryEntry repositoryEntry, Identity creatorIdentity, String lvLanguage) {

        Translator translator = getTranslator(lvLanguage);

        // Check if course has a learning area called campusCourseConfiguration.getCampusGroupsLearningAreaName(). If not, create a learning area with this name.
        String campusLearningAreaName = campusCourseConfiguration.getCampusGroupsLearningAreaName();
        BGArea campusLearningArea = bgAreaManager.findBGArea(campusLearningAreaName, repositoryEntry.getOlatResource());
        if (campusLearningArea == null) {
            campusLearningArea = bgAreaManager.createAndPersistBGArea(campusLearningAreaName, translator.translate("campus.course.learningArea.desc"), repositoryEntry.getOlatResource());
        }

        String groupNameA = campusCourseConfiguration.getCampusGroupADefaultName();
        String groupDescriptionA = translator.translate("campus.course.businessGroupA.desc");
        String managedFlagsA = campusCourseConfiguration.getCampusGroupAManagedFlags();
        String groupNameB = campusCourseConfiguration.getCampusGroupBDefaultName();
        String groupDescriptionB = translator.translate("campus.course.businessGroupB.desc");
        String managedFlagsB = campusCourseConfiguration.getCourseGroupBManagedFlags();

        List<BusinessGroup> businessGroupsOfCampusLearningArea = bgAreaManager.findBusinessGroupsOfArea(campusLearningArea);

        BusinessGroup campusGroupA = findOrCreateCampusGroup(repositoryEntry, creatorIdentity, groupNameA, groupDescriptionA, managedFlagsA, campusLearningArea, businessGroupsOfCampusLearningArea);
        BusinessGroup campusGroupB = findOrCreateCampusGroup(repositoryEntry, creatorIdentity, groupNameB, groupDescriptionB, managedFlagsB, campusLearningArea, businessGroupsOfCampusLearningArea);

        return new CampusGroups(campusGroupA, campusGroupB);
    }

    private BusinessGroup findOrCreateCampusGroup(RepositoryEntry repositoryEntry, Identity creatorIdentity, String campusGroupName, String groupDescription, String managedFlags, BGArea campusLearningArea, List<BusinessGroup> businessGroupsOfCampusLearningArea) {

        // Look for campus group in business groups of campus learning area
        BusinessGroup campusGroup = findCampusGroupInBusinessGroupsOfCampusLearningArea(businessGroupsOfCampusLearningArea, campusGroupName);
        if (campusGroup != null) {
            campusGroup = setManagedFlagsAndUpdateBusinessGroup(creatorIdentity, campusGroup, managedFlags);
            return campusGroup;
        }

        // Look for campus group in business groups of olat campus course
        campusGroup = findCampusGroupInBusinessGroupsOfOlatCampusCourse(repositoryEntry, campusGroupName);
        if (campusGroup != null) {
            campusGroup = setManagedFlagsAndUpdateBusinessGroup(creatorIdentity, campusGroup, managedFlags);
            addCampusGroupToCampusLearningArea(campusGroup, campusLearningArea);
            return campusGroup;
        }

        // Create campus group with managed flags
        campusGroup = businessGroupService.createBusinessGroup(creatorIdentity, campusGroupName, groupDescription, null, managedFlags, null, null, false, false, repositoryEntry);
        addCampusGroupToCampusLearningArea(campusGroup, campusLearningArea);
        return campusGroup;
    }

    private BusinessGroup findCampusGroupInBusinessGroupsOfCampusLearningArea(List<BusinessGroup> businessGroupsOfCampusLearningArea, String campusGroupName) {
        for (BusinessGroup businessGroupOfCampusLearningArea : businessGroupsOfCampusLearningArea) {
            if (businessGroupOfCampusLearningArea.getName().equals(campusGroupName)) {
                return businessGroupOfCampusLearningArea;
            }
        }
        return null;
    }

    private BusinessGroup findCampusGroupInBusinessGroupsOfOlatCampusCourse(RepositoryEntry repositoryEntry, String campusGroupName) {
        ICourse course = CourseFactory.loadCourse(repositoryEntry);
        CourseGroupManager courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();
        List <BusinessGroup> businessGroupsFound = courseGroupManager.getAllBusinessGroups();

        for (BusinessGroup businessGroup : businessGroupsFound ) {
            if (businessGroup.getName().equals(campusGroupName)) {
                return businessGroup;
            }
        }
        return null;
    }

    private BusinessGroup setManagedFlagsAndUpdateBusinessGroup(Identity creatorIdentity, BusinessGroup campusGroup, String managedFlags) {
        return businessGroupService.updateBusinessGroup(creatorIdentity, campusGroup, campusGroup.getName(), campusGroup.getDescription(),
                campusGroup.getExternalId(), managedFlags, campusGroup.getMinParticipants(), campusGroup.getMaxParticipants());
    }

    private void addCampusGroupToCampusLearningArea(BusinessGroup campusGroup, BGArea campusLearningArea) {
        bgAreaManager.addBGToBGArea(campusGroup, campusLearningArea);
    }

    Translator getTranslator(String lvLanguage) {
        String supportedLvLanguage = campusCourseConfiguration.getSupportedTemplateLanguage(lvLanguage);
        return  Util.createPackageTranslator(this.getClass(), new Locale(supportedLvLanguage));
    }

}
