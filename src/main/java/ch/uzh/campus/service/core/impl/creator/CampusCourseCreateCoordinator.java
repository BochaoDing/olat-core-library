/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.data.*;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.CampusCourseTool;
import ch.uzh.campus.service.core.impl.syncer.CampusCourseGroupSynchronizer;
import org.apache.commons.lang.StringUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.tree.PublishTreeModel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Initial Date: 30.05.2012 <br>
 * 
 * @author aabouc
 */
@Component
public class CampusCourseCreateCoordinator {
    
	private static final OLog log = Tracing.createLoggerFor(CampusCourseCreateCoordinator.class);

    private final CampusCourseConfiguration campusCourseConfiguration;
    private final CampusCoursePublisher campusCoursePublisher;
    private final CampusCourseGroupSynchronizer campusCourseGroupSynchronizer;
    private final CampusCourseDescriptionBuilder campusCourseDescriptionBuilder;
    private final RepositoryService repositoryService;
    private final OLATResourceManager olatResourceManager;
    private final DB dbInstance;
    private final DaoManager daoManager;
    private final CampusCourseCreator campusCourseCreator;

    @Autowired
    public CampusCourseCreateCoordinator(
            CampusCourseConfiguration campusCourseConfiguration,
            CampusCoursePublisher campusCoursePublisher,
            CampusCourseGroupSynchronizer campusCourseGroupSynchronizer,
            CampusCourseDescriptionBuilder campusCourseDescriptionBuilder,
            RepositoryService repositoryService,
            OLATResourceManager olatResourceManager,
            DB dbInstance,
            DaoManager daoManager,
            CampusCourseCreator campusCourseCreator) {
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.campusCoursePublisher = campusCoursePublisher;
        this.campusCourseGroupSynchronizer = campusCourseGroupSynchronizer;
        this.campusCourseDescriptionBuilder = campusCourseDescriptionBuilder;
        this.repositoryService = repositoryService;
        this.olatResourceManager = olatResourceManager;
        this.dbInstance = dbInstance;
        this.daoManager = daoManager;
        this.campusCourseCreator = campusCourseCreator;
    }

    public CampusCourse continueCampusCourse(CampusCourse campusCourse, CampusCourseImportTO campusCourseImportData, Identity creator) {

        // Update display name, description and initial author
        String oldTitle = campusCourse.getRepositoryEntry().getDisplayname();
        String newTitle = campusCourseImportData.getTitle();
        String displayName = StringUtils.left(newTitle, 4).concat("/").concat(StringUtils.left(oldTitle, 4)).concat(StringUtils.substring(newTitle, 4));
        String campusCourseSemester = oldTitle.concat("<br>").concat(newTitle);
        campusCourse.getRepositoryEntry().setDisplayname(CampusCourseTool.getTruncatedDisplayname(displayName));
        campusCourse.getRepositoryEntry().setDescription(campusCourseDescriptionBuilder.buildDescriptionFrom(campusCourseImportData, campusCourseSemester, campusCourseImportData.getLanguage()));
        campusCourse.getRepositoryEntry().setInitialAuthor(creator.getName());
        repositoryService.update(campusCourse.getRepositoryEntry());

        // Update course run and editor models
        campusCourseCreator.updateCourseRunAndEditorModels(campusCourse, displayName, campusCourseImportData.getVvzLink(), false, campusCourseImportData.getLanguage());

        List<StudentCourse> studentCourses = new ArrayList<>();
        for (Identity identity : campusCourseGroupSynchronizer.getCampusGroupAParticipants(campusCourse)) {
            SapOlatUser sapOlatUser = daoManager.getStudentSapOlatUserByOlatUserName(identity.getName());
            if (sapOlatUser != null) {
                if (daoManager.getStudentById(sapOlatUser.getSapUserId()) != null) {
                	//StudentCourse studentCourse = new StudentCourse(new StudentCoursePK(sapOlatUser.getSapUserId(), campusCourseImportData.getSapCourseId()));
                	Course course = daoManager.getCourseById(campusCourseImportData.getSapCourseId());
                	Student student = daoManager.getStudentById(sapOlatUser.getSapUserId());
                	StudentCourse studentCourse = new StudentCourse(student,course, new Date());                    
                    studentCourses.add(studentCourse);
                }
            }
        }

        if (!studentCourses.isEmpty()) {
            daoManager.saveStudentCourses(studentCourses);
        }

        return campusCourse;
    }

    /**
     * Create campus course from a template course if courseResourceableId == null, else use the given course as a template.
     */
    public CampusCourse createCampusCourse(Long courseResourceableId, CampusCourseImportTO campusCourseImportData, Identity creator) {
        Long templateCourseResourceableId;
        boolean isDefaultTemplateUsed = (courseResourceableId == null);

        if (isDefaultTemplateUsed) {
            templateCourseResourceableId = campusCourseConfiguration.getTemplateCourseResourcableId(campusCourseImportData.getLanguage());

            // The case that no template was found
            if (templateCourseResourceableId == null) {
                return null;
            }

            // Check if template has unpublished changes. If so return.
            ICourse defaultTemplateCourse = CourseFactory.loadCourse(templateCourseResourceableId);
            PublishTreeModel publishTreeModel = new PublishTreeModel(defaultTemplateCourse.getEditorTreeModel(), defaultTemplateCourse.getRunStructure());
            if (publishTreeModel.hasPublishableChanges()) {
                log.warn("Campuskurs template course " + defaultTemplateCourse.getCourseTitle() + " (" + defaultTemplateCourse.getResourceableId()
                        + ") is not published completely.");
                return null;
            }
        } else {
            templateCourseResourceableId = courseResourceableId;
        }

        CampusCourse campusCourse = null;
        try {
            // Create the campus course by copying the appropriate template (default or custom)
            String displayname = CampusCourseTool.getTruncatedDisplayname(campusCourseImportData.getTitle());
            String lvLanguage = campusCourseConfiguration.getTemplateLanguage(campusCourseImportData.getLanguage());
            String description = campusCourseDescriptionBuilder.buildDescriptionFrom(campusCourseImportData, lvLanguage);
            campusCourse = campusCourseCreator.createCampusCourseFromTemplate(templateCourseResourceableId, creator, displayname, description, isDefaultTemplateUsed);

            // Update the copied course run and editor models
            campusCourseCreator.updateCourseRunAndEditorModels(campusCourse, campusCourseImportData.getTitle(), campusCourseImportData.getVvzLink(), isDefaultTemplateUsed, lvLanguage);

            // Publish run and editor models
            if (isDefaultTemplateUsed) {
                campusCoursePublisher.publish(campusCourse.getCourse(), creator);
            }

            // Create campus learning area and business groups A and B if necessary
            if (!isDefaultTemplateUsed) {
                campusCourseCreator.createCampusLearningAreaAndCampusBusinessGroups(campusCourse.getRepositoryEntry(), creator, lvLanguage);
            }

            // Execute the first synchronization
            campusCourseGroupSynchronizer.addAllLecturesAsOwner(campusCourse, campusCourseImportData.getLecturers());
            campusCourseGroupSynchronizer.addDefaultCoOwnersAsOwner(campusCourse);
            campusCourseGroupSynchronizer.synchronizeCourseGroups(campusCourse, campusCourseImportData);

            return campusCourse;

        } catch (Exception ex) {
            // CLEAN UP TO ENSURE CONSISTENT STATE
            if (campusCourse != null) {
                if (campusCourse.getRepositoryEntry() != null) {
                    try {
                    	//TODO: olatng überprüfen, ob noch ausreichend
                        //repositoryService.deleteRepositoryEntryAndBasesecurity(campusCourse.getRepositoryEntry());
                    	repositoryService.deleteRepositoryEntryAndBaseGroups(campusCourse.getRepositoryEntry());
                    } catch (Exception e) {
                        // we tried best to delete entry - ignore exceptions during deletion
                    }
                }
                if (campusCourse.getCourse() != null) {
                    try {
                        olatResourceManager.deleteOLATResourceable(campusCourse.getCourse());
                    } catch (Exception e) {
                        // we tried best to delete entry - ignore exceptions during deletion
                    }
                }
            }
            return null;
        }
    }
}
