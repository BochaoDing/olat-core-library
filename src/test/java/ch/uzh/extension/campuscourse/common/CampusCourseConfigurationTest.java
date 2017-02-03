package ch.uzh.extension.campuscourse.common;

import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.junit.Assert.assertEquals;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author cg
 */
@Component
public class CampusCourseConfigurationTest extends CampusCourseTestCase {

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Value("${campus.template.repositoryEntry.id}")
    String defaultValue;

    @Value("${campus.template.defaultLanguage}")
    private String defaultTemplateLanguage;

    @Test
    public void getTemplateCourseOlatResourceKey_DefaultValue() {
        // Delete property entry (if exists)
        Long oldValue = campusCourseConfiguration.getTemplateRepositoryEntryId(defaultTemplateLanguage);
        campusCourseConfiguration.deleteTemplateRepositoryEntryIdPropertyIfExists();

        Long configValue = campusCourseConfiguration.getTemplateRepositoryEntryId(null);
        assertEquals("Wrong default value, config-value is different to value in olat.properties", defaultValue, configValue.toString());

        campusCourseConfiguration.saveTemplateRepositoryEntryId(oldValue, defaultTemplateLanguage);
    }

    @Test
    public void saveTemplateCourseOlatResourceKey() {
        Long oldValue = campusCourseConfiguration.getTemplateRepositoryEntryId(defaultTemplateLanguage);
        Long newValue = 1234L;
        campusCourseConfiguration.saveTemplateRepositoryEntryId(newValue, defaultTemplateLanguage);
        Long configValue = campusCourseConfiguration.getTemplateRepositoryEntryId(defaultTemplateLanguage);
        assertEquals("Get wrong config-value after save new value", newValue, configValue);
        campusCourseConfiguration.saveTemplateRepositoryEntryId(oldValue, defaultTemplateLanguage);
    }
}
