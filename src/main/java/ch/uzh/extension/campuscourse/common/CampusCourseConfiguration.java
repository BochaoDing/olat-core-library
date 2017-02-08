package ch.uzh.extension.campuscourse.common;

import org.apache.commons.lang.StringUtils;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.group.BusinessGroup;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple campus-course configuration with Spring properties (olat.local.properties).
 * 
 * @author cg
 */
@Component
public class CampusCourseConfiguration {

	public final static String RESOURCEABLE_TYPE_NAME = "CampusCourse";
	public final static String STUDENT_RESOURCEABLE_TYPE_NAME = "Student" + RESOURCEABLE_TYPE_NAME;
	public final static String LECTURER_RESOURCEABLE_TYPE_NAME = "Lecturer"  + RESOURCEABLE_TYPE_NAME;
	public final static String AUTHOR_LECTURER_RESOURCEABLE_TYPE_NAME = "AuthorLecturer"  + RESOURCEABLE_TYPE_NAME;
	public final static Long NOT_CREATED_CAMPUS_COURSE_KEY = 0L;
	public final static Long NOT_CREATED_CAMPUS_COURSE_RESOURCE_ID = 0L;

    private static final Identity NO_IDENTITY = null;
    private static final BusinessGroup NO_GROUP = null;
    private static final OLATResourceable NO_RESOURCEABLE = null;

    private static final String CAMPUS_COURSE_PROPERTY_CATEGORY = "campus.course.property";
    private static final String TEMPLATE_COURSE_REPOSITORY_ENTRY_ID_PROPERTY_KEY = "_template.course.repositoryEntry.id";

    private final PropertyManager propertyManager;

    @Autowired
    public CampusCourseConfiguration(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
    }

    @Value("${campus.mapping.institutionalName:uzh.ch}")
    private String mappingInstitutionalName;

    @Value("${campus.import.process.maxYearsToKeepCkData:3}")
    private int maxYearsToKeepCkData;

    @Value("${campus.template.repositoryEntry.id}")
    private String defaultTemplateRepositoryEntryId;

    @Value("${campus.template.supportedLanguages}")
    private String supportedTemplateLanguages;

    @Value("${campus.groupA.defaultName}")
    private String campusGroupADefaultName;

    @Value("${campus.groupB.defaultName}")
    private String campusGroupBDefaultName;

    @Value("${campus.groupA.managedFlags}")
    private String campusGroupAManagedFlags;

    @Value("${campus.groupB.managedFlags}")
    private String courseGroupBManagedFlags;

    @Value("${campus.groups.learningArea.name}")
    private String campusGroupsLearningAreaName;

    @Value("${campus.template.course.vvzLink}")
    private String templateCourseVvzLink;

    @Value("${campus.template.course.olat.support.emailNodeType}")
    private String templateCourseOlatSupportEmailNodeType;

    @Value("${campus.template.course.olat.support.shortTitleSubstring}")
    private String templateCourseOlatSupportShortTitleSubstring;

    @Value("${campus.course.default.co.owner.usernames}")
    private String defaultCoOwnerUserNames;

    @Value("${campus.description.startsWith.string}")
    private String descriptionStartWithString;

    @Value("${campus.enable.synchronizeDisplaynameAndDescription}")
    private boolean synchronizeDisplaynameAndDescription;

    @Value("${campus.template.defaultLanguage}")
    private String defaultTemplateLanguage;

    @Value("${campus.import.process.sap.resources}")
    private String sapImportPath;

	@Value("${campus.import.process.sap.controlFile.filename}")
    private String sapImportControlFileFilename;

	@Value("${campus.import.process.sap.orgs.filenameWithoutSuffix}")
	private String sapImportOrgsFilenameWithoutSuffix;

	@Value("${campus.import.process.sap.courses.filenameWithoutSuffix}")
	private String sapImportCoursesFilenameWithoutSuffix;

	@Value("${campus.import.process.sap.students.filenameWithoutSuffix}")
	private String sapImportStudentsFilenameWithoutSuffix;

	@Value("${campus.import.process.sap.lecturers.filenameWithoutSuffix}")
	private String sapImportLecturersFilenameWithoutSuffix;

	@Value("${campus.import.process.sap.studentCourses.filenameWithoutSuffix}")
	private String sapImportStudentCoursesFilenameWithoutSuffix;

	@Value("${campus.import.process.sap.lecturerCourses.filenameWithoutSuffix}")
	private String sapImportLecturerCoursesFilenameWithoutSuffix;

	@Value("${campus.import.process.sap.texts.filenameWithoutSuffix}")
	private String sapImportTextsFilenameWithoutSuffix;

	@Value("${campus.import.process.sap.events.filenameWithoutSuffix}")
	private String sapImportEventsFilenameWithoutSuffix;

    @Value("${campus.import.process.sap.files.suffix}")
    private String sapImportFilesSuffix;

    @Value("${campus.entities.sublistMaxSize}")
    private int entitiesSublistMaxSize;

    @Value("${db.hibernate.hikari.leakDetectionThreshold}")
    private int connectionPoolTimeout;

	public Set<String> getSapFilesToBeImported() {
		Set<String> requiredSapImportFiles = new HashSet<>();
		requiredSapImportFiles.add(sapImportOrgsFilenameWithoutSuffix + sapImportFilesSuffix);
		requiredSapImportFiles.add(sapImportCoursesFilenameWithoutSuffix + sapImportFilesSuffix);
		requiredSapImportFiles.add(sapImportStudentsFilenameWithoutSuffix + sapImportFilesSuffix);
		requiredSapImportFiles.add(sapImportLecturersFilenameWithoutSuffix + sapImportFilesSuffix);
		requiredSapImportFiles.add(sapImportStudentCoursesFilenameWithoutSuffix + sapImportFilesSuffix);
		requiredSapImportFiles.add(sapImportLecturerCoursesFilenameWithoutSuffix + sapImportFilesSuffix);
		requiredSapImportFiles.add(sapImportTextsFilenameWithoutSuffix + sapImportFilesSuffix);
		// DISABLED FOR NOW
//		requiredSapImportFiles.add(sapImportEventsFilenameWithoutSuffix + sapImportFilesSuffix);
		return requiredSapImportFiles;
	}

	public int getNumberOfBatchStepsOfSapImportProcess() {
		return getSapFilesToBeImported().size();
	}

	private String getSupportedTemplateLanguage() {
		return getSupportedTemplateLanguage("");
	}

    public String getSupportedTemplateLanguage(String language) {
        if (StringUtils.isBlank(language) || !StringUtils.contains(getSupportedTemplateLanguages(), language)) {
            language = getDefaultTemplateLanguage();
        }
        return language;
    }

    public Long getTemplateRepositoryEntryId(String language) {
        language = getSupportedTemplateLanguage(language);

        String propertyStringValue = null;
        try {
            propertyStringValue = getPropertyOrDefaultValue(language.concat(TEMPLATE_COURSE_REPOSITORY_ENTRY_ID_PROPERTY_KEY), defaultTemplateRepositoryEntryId);
            return (propertyStringValue == null ? null : Long.valueOf(propertyStringValue));
        } catch (NumberFormatException ex) {
            throw new AssertException("Could not convert to Long-value '" + propertyStringValue + "' , check properties");
        }
    }

    private String getPropertyOrDefaultValue(String propertyKey, String defaultValue) {
    	Property property = findCampusProperty(propertyKey);
        if (property == null) {
            return defaultValue;
        } else {
            return property.getStringValue();
        }
    }

    private Property findCampusProperty(String propertyKey) {
        return propertyManager.findProperty(NO_IDENTITY, NO_GROUP, NO_RESOURCEABLE, CAMPUS_COURSE_PROPERTY_CATEGORY, propertyKey);
    }

    public void saveTemplateRepositoryEntryId(Long templateCourseRepositoryEntryId, String language) {
        saveCampusProperty(language.concat(TEMPLATE_COURSE_REPOSITORY_ENTRY_ID_PROPERTY_KEY), templateCourseRepositoryEntryId.toString());
    }

    private void saveCampusProperty(String propertyKey, String propertyValue) {
        Property property = findCampusProperty(propertyKey);
        if (property == null) {
            property = propertyManager.createPropertyInstance(NO_IDENTITY, NO_GROUP, NO_RESOURCEABLE, CAMPUS_COURSE_PROPERTY_CATEGORY, propertyKey, null, null,
                    propertyValue, null);
            propertyManager.saveProperty(property);
        } else {
            property.setStringValue(propertyValue);
            propertyManager.updateProperty(property);
        }
    }

    void deleteTemplateRepositoryEntryIdPropertyIfExists() {
		String language = getSupportedTemplateLanguage();
        Property property = findCampusProperty(language.concat(TEMPLATE_COURSE_REPOSITORY_ENTRY_ID_PROPERTY_KEY));
        if (property != null) {
            propertyManager.deleteProperty(property);
        }
    }

    public String getMappingInstitutionalName() {
        return mappingInstitutionalName;
    }

    public void setMaxYearsToKeepCkData(int maxYearsToKeepCkData) {
        this.maxYearsToKeepCkData = maxYearsToKeepCkData;
    }

    public String getCampusGroupADefaultName() {
        return campusGroupADefaultName;
    }

    public String getCampusGroupBDefaultName() {
        return campusGroupBDefaultName;
    }

    public String getCampusGroupAManagedFlags() {
        return campusGroupAManagedFlags;
    }

    public String getCourseGroupBManagedFlags() {
        return courseGroupBManagedFlags;
    }

    public String getCampusGroupsLearningAreaName() {
        return campusGroupsLearningAreaName;
    }

    public String getTemplateCourseVvzLink() {
        return templateCourseVvzLink;
    }

    public String getTemplateCourseOlatSupportEmailNodeType() {
        return templateCourseOlatSupportEmailNodeType;
    }

    public String getTemplateCourseOlatSupportShortTitleSubstring() {
        return templateCourseOlatSupportShortTitleSubstring;
    }

    public String getDefaultCoOwnerUserNames() {
        return defaultCoOwnerUserNames;
    }

    public boolean isSynchronizeDisplaynameAndDescriptionEnabled() {
        return synchronizeDisplaynameAndDescription;
    }

    private String getSupportedTemplateLanguages() {
        return supportedTemplateLanguages;
    }

    private String getDefaultTemplateLanguage() {
        return defaultTemplateLanguage;
    }

	public String getSapImportControlFilenameWithPath() {
		return sapImportPath + File.separator + sapImportControlFileFilename;
	}

	public String getSapImportOrgsFilenameWithoutSuffix() {
		return sapImportOrgsFilenameWithoutSuffix;
	}

	public String getSapImportCoursesFilenameWithoutSuffix() {
		return sapImportCoursesFilenameWithoutSuffix;
	}

	public String getSapImportStudentsFilenameWithoutSuffix() {
		return sapImportStudentsFilenameWithoutSuffix;
	}

	public String getSapImportLecturersFilenameWithoutSuffix() {
		return sapImportLecturersFilenameWithoutSuffix;
	}

	public String getSapImportStudentCoursesFilenameWithoutSuffix() {
		return sapImportStudentCoursesFilenameWithoutSuffix;
	}

	public String getSapImportLecturerCoursesFilenameWithoutSuffix() {
		return sapImportLecturerCoursesFilenameWithoutSuffix;
	}

	public String getSapImportTextsFilenameWithoutSuffix() {
		return sapImportTextsFilenameWithoutSuffix;
	}

	public String getSapImportEventsFilenameWithoutSuffix() {
		return sapImportEventsFilenameWithoutSuffix;
	}

	public String getSapImportFilesSuffix() {
        return sapImportFilesSuffix;
    }

	public int getEntitiesSublistMaxSize() {
        return entitiesSublistMaxSize;
    }

    public int getConnectionPoolTimeout() {
        return connectionPoolTimeout;
    }

    public int getMaxYearsToKeepCkData() {
        return maxYearsToKeepCkData;
    }
}
