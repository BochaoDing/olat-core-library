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

import ch.uzh.campus.CampusConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.data.*;
import ch.uzh.campus.service.CampusCourse;
import ch.uzh.campus.service.core.impl.syncer.CampusCourseGroupSynchronizer;
import org.apache.commons.lang.StringUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
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
import java.util.Locale;


/**
 * Initial Date: 30.05.2012 <br>
 * 
 * @author aabouc
 */
@Component
public class CourseCreateCoordinator {
    
	private static final OLog log = Tracing.createLoggerFor(CourseCreateCoordinator.class);

    private final CampusConfiguration campusConfiguration;
    private final CoursePublisher coursePublisher;
    private final CampusCourseGroupSynchronizer campusCourseGroupSynchronizer;
    private final CourseDescriptionBuilder courseDescriptionBuilder;
    private final RepositoryService repositoryService;
    private final OLATResourceManager olatResourceManager;
    private final DB dbInstance;
    private final DaoManager daoManager;
    private final CourseCreator courseCreator;

    @Autowired
    public CourseCreateCoordinator(
            CampusConfiguration campusConfiguration,
            CoursePublisher coursePublisher,
            CampusCourseGroupSynchronizer campusCourseGroupSynchronizer,
            CourseDescriptionBuilder courseDescriptionBuilder,
            RepositoryService repositoryService,
            OLATResourceManager olatResourceManager,
            DB dbInstance,
            DaoManager daoManager,
            CourseCreator courseCreator) {
        this.campusConfiguration = campusConfiguration;
        this.coursePublisher = coursePublisher;
        this.campusCourseGroupSynchronizer = campusCourseGroupSynchronizer;
        this.courseDescriptionBuilder = courseDescriptionBuilder;
        this.repositoryService = repositoryService;
        this.olatResourceManager = olatResourceManager;
        this.dbInstance = dbInstance;
        this.daoManager = daoManager;
        this.courseCreator = courseCreator;
    }

    public CampusCourse continueCampusCourse(CampusCourse campusCourse, CampusCourseImportTO campusCourseImportData, Identity creator) {
        RepositoryEntry repositoryEntry = campusCourse.getRepositoryEntry();

        // Titel
        String oldTitle = repositoryEntry.getDisplayname();
        String newTitle = campusCourseImportData.getTitle();
        String displayName = StringUtils.left(newTitle, 4).concat("/").concat(StringUtils.left(oldTitle, 4)).concat(StringUtils.substring(newTitle, 4));
        String truncatedTitle = courseCreator.getTruncatedTitle(displayName);
        campusCourse.getRepositoryEntry().setDisplayname(truncatedTitle);

        // Set title and vvz link in course model
        courseCreator.setCourseTitleAndLearningObjectivesInCourseModel(campusCourse, displayName, campusCourseImportData.getVvzLink(), false, getTranslator(campusCourseImportData.getLanguage()));

        // Description
        String campusCourseSemester = oldTitle.concat("<br>").concat(newTitle);
        campusCourse.getRepositoryEntry().setDescription(courseDescriptionBuilder.buildDescriptionFrom(campusCourseImportData, campusCourseSemester, campusCourseImportData.getLanguage()));

        updateDisplaynameDescriptionOfRepositoryEntry(campusCourse.getRepositoryEntry());

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

        campusCourse.getRepositoryEntry().setInitialAuthor(creator.getName());

        if (!studentCourses.isEmpty()) {
            daoManager.saveStudentCourses(studentCourses);
        }

        return campusCourse;
    }
    
    
    private RepositoryEntry updateDisplaynameDescriptionOfRepositoryEntry(final RepositoryEntry repositoryEntry) {
        final RepositoryEntry reloaded = repositoryService.loadByKey(repositoryEntry.getKey());
        reloaded.setDisplayname(repositoryEntry.getDisplayname());
        reloaded.setDescription(repositoryEntry.getDescription());        
        dbInstance.getCurrentEntityManager().merge(reloaded);
        return reloaded;
    }

    /**
     * Create campus course from a template course if courseResourceableId == null, else use the given course as a template.
     */
    public CampusCourse createCampusCourse(Long courseResourceableId, CampusCourseImportTO campusCourseImportData, Identity creator) {
        final Long templateCourseResourceableId;

        boolean defaultTemplateUsed = (courseResourceableId == null);
        if (defaultTemplateUsed) {
            templateCourseResourceableId = campusConfiguration.getTemplateCourseResourcableId(campusCourseImportData.getLanguage());

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
            // Copy the CampusCourse from the appropriate template (default or custom)
            campusCourse = courseCreator.createCampusCourseFromTemplate(templateCourseResourceableId, creator);
            String truncatedTitle = courseCreator.getTruncatedTitle(campusCourseImportData.getTitle());
            campusCourse.getRepositoryEntry().setDisplayname(truncatedTitle);
            String lvLanguage = campusConfiguration.getTemplateLanguage(campusCourseImportData.getLanguage());
            courseCreator.setCourseTitleAndLearningObjectivesInCourseModel(campusCourse, campusCourseImportData.getTitle(), campusCourseImportData.getVvzLink(), defaultTemplateUsed, getTranslator(lvLanguage));
            campusCourse.getRepositoryEntry().setDescription(courseDescriptionBuilder.buildDescriptionFrom(campusCourseImportData, lvLanguage));

            if (!defaultTemplateUsed) {
                courseCreator.createCampusLearningAreaAndCampusBusinessGroups(campusCourse, creator, getTranslator(lvLanguage));
            }

            // Execute the first synchronization
            campusCourseGroupSynchronizer.addAllLecturesAsOwner(campusCourse, campusCourseImportData.getLecturers());
            campusCourseGroupSynchronizer.addDefaultCoOwnersAsOwner(campusCourse);
            campusCourseGroupSynchronizer.synchronizeCourseGroups(campusCourse, campusCourseImportData);

            repositoryService.update(campusCourse.getRepositoryEntry());

            //TODO: olatng -> is this still needed? Wahrscheilich nicht mehr notwendig, evtl. abklären mit REs / Frentix
            // ADD ADMIN RIGHTS TO OWNER GROUP            
            //final BaseSecurity securityManager = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
            //securityManager.createAndPersistPolicy(campusCourse.getRepositoryEntry().getOwnerGroup(), Constants.PERMISSION_ADMIN, campusCourse.getCourse());

            if (defaultTemplateUsed) {
                // SET THE BARG
                campusCourse.getRepositoryEntry().setAccess(RepositoryEntry.ACC_USERS_GUESTS);
                // PUBLISH THE CREATED CampusCourse
                coursePublisher.publish(campusCourse.getCourse(), creator);
            }
            
            //TODO: olatng load course again to see the updates in title, etc. ? Als Abhilfe für fehlschlagenden Test; funktionierte nicht
            //ICourse reloadedCourse = CourseFactory.loadCourse(campusCourse.getCourse().getResourceableId());
            //campusCourse.reloadCourse();
            
           
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

    private Translator getTranslator(String lvLanguage) {       
        return  Util.createPackageTranslator(this.getClass(), new Locale(lvLanguage));
    }
}
