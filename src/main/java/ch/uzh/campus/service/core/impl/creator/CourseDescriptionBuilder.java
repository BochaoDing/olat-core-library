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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.util.Util;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.springframework.stereotype.Component;
import ch.uzh.campus.CampusCourseImportTO;

/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
@Component
public class CourseDescriptionBuilder {

    public static final String KEY_DESCRIPTION_TEMPLATE = "campus.course.description.template";
    public static final String KEY_DESCRIPTION_MULTI_SEMESTER_TEMPLATE = "campus.course.multisemester.description.template";

    public Translator translator;

    public CourseDescriptionBuilder() {
        translator = Util.createPackageTranslator(CourseDescriptionBuilder.class, new Locale("de"));
    }

    public String buildDescriptionFrom(CampusCourseImportTO campusCourseData, String language) {
        return buildDescriptionFrom(campusCourseData, null, language);

    }

    public String buildDescriptionFrom(CampusCourseImportTO campusCourseData, String campusCourseMultiSemester, String language) {
        String[] args = new String[3];
        args[0] = (campusCourseMultiSemester != null) ? campusCourseMultiSemester : campusCourseData.getSemester();

        if (campusCourseData.getDelegatees() != null && !campusCourseData.getDelegatees().isEmpty()) {
            List<Identity> lecturersWithoutDelegatees = new ArrayList<Identity>(campusCourseData.getLecturers());
            lecturersWithoutDelegatees.removeAll(campusCourseData.getDelegatees());
            args[1] = getLectureList(lecturersWithoutDelegatees);
        } else {
            args[1] = getLectureList(campusCourseData.getLecturers());
        }

        args[2] = campusCourseData.getEventDescription();
        if (language != null) {
            translator.setLocale(new Locale(language));
        }
        return translator.translate((campusCourseMultiSemester != null) ? KEY_DESCRIPTION_MULTI_SEMESTER_TEMPLATE : KEY_DESCRIPTION_TEMPLATE, args);
    }

    public String getLectureList(List<Identity> lecturers) {
        StringBuilder builder = new StringBuilder();
        boolean firstEntry = true;
        for (Identity lecture : lecturers) {
            if (!firstEntry) {
                builder.append(", ");
            }
            String firstName = lecture.getUser().getProperty(UserConstants.FIRSTNAME, null);
            String lastName = lecture.getUser().getProperty(UserConstants.LASTNAME, null);
            builder.append(firstName);
            builder.append(" ");
            builder.append(lastName);
            firstEntry = false;
        }
        return builder.toString();
    }

}
