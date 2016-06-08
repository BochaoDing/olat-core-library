package ch.uzh.campus;

import org.apache.commons.lang.StringUtils;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.group.BusinessGroup;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.olat.properties.Property;

/**
 * Simple campus-course configuration with Spring properties (olat.local.properties).
 * 
 * @author cg
 */
@Component
public class CampusConfiguration {

    private static final Identity NO_IDENTITY = null;
    private static final BusinessGroup NO_GROUP = null;
    private static final OLATResourceable NO_RESOURCEABLE = null;

    private static final String CAMPUS_COURSE_PROPERTY_CATEGORY = "campus.course.property";
    private static final String TEMPLATE_COURSE_RESOURCEABLE_ID_PROPERTY_KEY = "_template.course.resourceable.id";

    @Value("${campus.template.course.resourceable.id}")
    private String defaultTemplateCourseResourcableId;

    @Value("${campus.template.supportedLanguages}")
    private String templateSupportedLanguages;

    @Value("${campus.template.course.groupA.name}")
    private String courseGroupAName;

    @Value("${campus.template.course.groupB.name}")
    private String courseGroupBName;

    @Value("${campus.course.default.co.owner.usernames}")
    private String defaultCoOwnerUserNames;

    @Value("${campus.description.startsWith.string}")
    private String descriptionStartWithString;

    @Value("${campus.enable.synchronizeTitleAndDescription}")
    private boolean synchronizeTitleAndDescription;

    @Value("${campus.template.defaultLanguage}")
    private String defaultTemplateLanguage;

    @Value("${campus.import.process.sap.files.suffix}")
    private String sapFilesSuffix;

    @Value("${campus.entities.sublistMaxSize}")
    private int entitiesSublistMaxSize;

    @Value("${campus.import.mustCompletedImportedFiles}")
    private int mustCompletedImportedFiles;

    @Autowired
    PropertyManager propertyManager;

    // @Value("${campus.start.autumn.semester}")
    // private String startDateAutumnSemester;
    //
    // @Value("${campus.start.spring.semester}")
    // private String startDateSpringSemester;

    public String getTemplateLanguage(String language) {
        if (StringUtils.isBlank(language) || !StringUtils.contains(getTemplateSupportedLanguages(), language)) {
            language = getDefaultTemplateLanguage();
        }
        return language;
    }

    public Long getTemplateCourseResourcableId(String language) {
        language = getTemplateLanguage(language);

        String propertyStringValue = null;
        try {
            propertyStringValue = getPropertyOrDefaultValue(language.concat(TEMPLATE_COURSE_RESOURCEABLE_ID_PROPERTY_KEY), defaultTemplateCourseResourcableId);
            return Long.valueOf(propertyStringValue);
        } catch (NumberFormatException ex) {
            throw new AssertException("Could not convert to Long-value '" + propertyStringValue + "' , check properties");
        }
    }

    String getPropertyOrDefaultValue(String propertyKey, String defaultValue) {
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

    public void saveTemplateCourseResourcableId(Long templateCourseResourcableId, String language) {
        saveCampusProperty(language.concat(TEMPLATE_COURSE_RESOURCEABLE_ID_PROPERTY_KEY), templateCourseResourcableId.toString());
    }

    void saveCampusProperty(String propertyKey, String propertyValue) {
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

    public String getCourseGroupAName() {
        return courseGroupAName;
    }

    public String getCourseGroupBName() {
        return courseGroupBName;
    }

    public String getDefaultCoOwnerUserNames() {
        return defaultCoOwnerUserNames;
    }

    public boolean isSynchronizeTitleAndDescriptionEnabled() {
        return synchronizeTitleAndDescription;
    }

    public String getDescriptionStartWithString() {
        return descriptionStartWithString;
    }

    public String[] getDescriptionStartWithStringAsArray() {
        String[] splittArray = null;
        if (!StringUtils.isBlank(descriptionStartWithString)) {
            splittArray = descriptionStartWithString.split(",");
        }
        return splittArray;
    }

    public String getTemplateSupportedLanguages() {
        return templateSupportedLanguages;
    }

    public String getDefaultTemplateLanguage() {
        return defaultTemplateLanguage;
    }

    public String getSapFilesSuffix() {
        return sapFilesSuffix;
    }

    public int getEntitiesSublistMaxSize() {
        return entitiesSublistMaxSize;
    }

    public int getMustCompletedImportedFiles() {
        return mustCompletedImportedFiles;
    }

    // TODO: How to handle start-date of semester ?
    // public String getStartDateAutumnSemester() {
    // return startDateAutumnSemester;
    // }
    //
    // public String getStartDateSpringSemester() {
    // return startDateSpringSemester;
    // }

    // TODO: Configuration via Admin-GUI/JMX: Add setter methods and use PropertyManager to save values.
    // The Spring properties values can be used as default values.

}