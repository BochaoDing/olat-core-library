package ch.uzh.campus.service;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.service.core.impl.CampusCourseTool;
import ch.uzh.campus.service.core.impl.creator.CampusCourseCreator;
import ch.uzh.campus.service.core.impl.creator.CampusCourseDescriptionBuilder;
import ch.uzh.campus.service.core.impl.creator.CampusCoursePublisher;
import ch.uzh.campus.service.core.impl.syncer.CampusCourseGroupSynchronizer;
import org.apache.commons.lang.StringUtils;
import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;


/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
public class CampusCourse {

    private final ICourse course;
    private final RepositoryEntry repositoryEntry;

    public CampusCourse(ICourse course, RepositoryEntry repositoryEntry) {
        this.course = course;
        this.repositoryEntry = repositoryEntry;
    }

    public ICourse getCourse() {
        return course;
    }

    public RepositoryEntry getRepositoryEntry() {
        return repositoryEntry;
    }

    public void continueCampusCourse(CampusCourseImportTO newCampusCourseImportTo, Identity creator, RepositoryService repositoryService, CampusCourseDescriptionBuilder campusCourseDescriptionBuilder, CampusCourseCreator campusCourseCreator) {

        // Update display name, description and initial author
        String oldTitle = repositoryEntry.getDisplayname();
        String newTitle = newCampusCourseImportTo.getTitle();
        String displayName = StringUtils.left(newTitle, 4).concat("/").concat(StringUtils.left(oldTitle, 4)).concat(StringUtils.substring(newTitle, 4));
        String campusCourseSemester = oldTitle.concat("<br>").concat(newTitle);
        repositoryEntry.setDisplayname(CampusCourseTool.getTruncatedDisplayname(displayName));
        repositoryEntry.setDescription(campusCourseDescriptionBuilder.buildDescriptionFrom(newCampusCourseImportTo, campusCourseSemester, newCampusCourseImportTo.getLanguage()));
        repositoryEntry.setInitialAuthor(creator.getName());
        repositoryService.update(repositoryEntry);

        // Update course run and editor models
        campusCourseCreator.updateCourseRunAndEditorModels(this, displayName, newCampusCourseImportTo.getVvzLink(), false, newCampusCourseImportTo.getLanguage());
    }

    public void updateCampusCourseCreatedFromTemplate(CampusCourseImportTO campusCourseImportData, Identity creator, boolean isDefaultTemplateUsed,
                                                      CampusCourseCreator campusCourseCreator, CampusCoursePublisher campusCoursePublisher, CampusCourseGroupSynchronizer campusCourseGroupSynchronizer, CampusCourseConfiguration campusCourseConfiguration) {

        String lvLanguage = campusCourseConfiguration.getTemplateLanguage(campusCourseImportData.getLanguage());

        // Update the copied course run and editor models
        campusCourseCreator.updateCourseRunAndEditorModels(this, campusCourseImportData.getTitle(), campusCourseImportData.getVvzLink(), isDefaultTemplateUsed, lvLanguage);

        // Publish run and editor models
        if (isDefaultTemplateUsed) {
            campusCoursePublisher.publish(course, creator);
        }

        // Create campus learning area and business groups A and B if necessary
        campusCourseCreator.createCampusLearningAreaAndCampusBusinessGroups(repositoryEntry, creator, lvLanguage);

        // Execute the first synchronization
        campusCourseGroupSynchronizer.addAllLecturesAsOwner(this, campusCourseImportData.getLecturers());
        campusCourseGroupSynchronizer.addDefaultCoOwnersAsOwner(this);
        campusCourseGroupSynchronizer.synchronizeCourseGroups(this, campusCourseImportData);
    }

}