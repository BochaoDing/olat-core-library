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
package ch.uzh.campus;

import org.junit.Before;
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
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class CampusCourseConfigurationTest extends OlatTestCase {

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Value("${campus.template.course.resourceable.id}")
    String defaultValue;

    @Value("${campus.template.defaultLanguage}")
    private String defaultTemplateLanguage;

    @Before
    public void setup() {
    }

    @Test
    public void getTemplateCourseResourcableId_DefaultValue() {
        // Delete property entry (if exists)
        Long oldValue = campusCourseConfiguration.getTemplateCourseResourcableId(defaultTemplateLanguage);
        campusCourseConfiguration.deleteTemplateCourseResourcableIdPropertyIfExists(null);

        Long configValue = campusCourseConfiguration.getTemplateCourseResourcableId(null);
        assertEquals("Wrong default value, config-value is different to value in olat.properties", defaultValue, configValue.toString());

        campusCourseConfiguration.saveTemplateCourseResourcableId(oldValue, defaultTemplateLanguage);
    }

    @Test
    public void saveTemplateCourseResourcableId() {
        Long oldValue = campusCourseConfiguration.getTemplateCourseResourcableId(defaultTemplateLanguage);
        Long newValue = 1234L;
        campusCourseConfiguration.saveTemplateCourseResourcableId(newValue, defaultTemplateLanguage);
        Long configValue = campusCourseConfiguration.getTemplateCourseResourcableId(defaultTemplateLanguage);
        assertEquals("Get wrong config-value after save new value", newValue, configValue);
        campusCourseConfiguration.saveTemplateCourseResourcableId(oldValue, defaultTemplateLanguage);
    }
}
