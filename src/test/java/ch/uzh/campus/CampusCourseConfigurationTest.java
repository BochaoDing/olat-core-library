package ch.uzh.campus;

import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author cg
 */
@ContextConfiguration(locations = {"classpath:/org/olat/_spring/mainContext.xml"})
public class CampusCourseConfigurationTest extends OlatTestCase {

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
        campusCourseConfiguration.deleteTemplateRepositoryEntryIdPropertyIfExists(null);

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
