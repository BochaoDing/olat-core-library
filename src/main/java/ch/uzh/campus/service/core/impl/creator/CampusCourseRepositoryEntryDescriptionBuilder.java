package ch.uzh.campus.service.core.impl.creator;

import ch.uzh.campus.service.data.SapCampusCourseTO;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.springframework.stereotype.Component;

import java.text.Collator;
import java.util.*;

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
 *
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
@Component
public class CampusCourseRepositoryEntryDescriptionBuilder {

    static final String KEY_DESCRIPTION_TEMPLATE = "campus.course.description.template";
    private static final String KEY_DESCRIPTION_MULTI_SEMESTER_TEMPLATE = "campus.course.multisemester.description.template";

    public Translator translator;

    public CampusCourseRepositoryEntryDescriptionBuilder() {
        translator = Util.createPackageTranslator(CampusCourseRepositoryEntryDescriptionBuilder.class, new Locale("de"));
    }

    public String buildDescriptionFrom(SapCampusCourseTO sapCampusCourseTO, String language) {
        return buildDescriptionFrom(sapCampusCourseTO, null, language);
    }

    public String buildDescriptionFrom(SapCampusCourseTO sapCampusCourseTO, List<String> titlesOfCourseAndParentCourses, String language) {

        String multiSemesterTitle = null;
        if (titlesOfCourseAndParentCourses != null) {
            multiSemesterTitle = createMultiSemesterTitle(titlesOfCourseAndParentCourses);
        }

        String[] args = new String[3];
        args[0] = (multiSemesterTitle != null) ? multiSemesterTitle : sapCampusCourseTO.getSemester().getSemesterNameYear();
        args[1] = getAlphabeticallySortedLecturerList(sapCampusCourseTO.getLecturersOfCourse());
        args[2] = sapCampusCourseTO.getEventDescription();

        if (language != null) {
            translator.setLocale(new Locale(language));
        }

        return translator.translate((multiSemesterTitle != null) ? KEY_DESCRIPTION_MULTI_SEMESTER_TEMPLATE : KEY_DESCRIPTION_TEMPLATE, args);
    }

    String createMultiSemesterTitle(List<String> titlesOfCourseAndParentCourses) {
        StringBuilder multiSemesterTitle = new StringBuilder();
        for (String title : titlesOfCourseAndParentCourses) {
            multiSemesterTitle.append(title).append("<br>");
        }
        // Remove last "<br>"
        if (multiSemesterTitle.length() >= 4) {
            multiSemesterTitle.setLength(multiSemesterTitle.length() - 4);
        }
        return multiSemesterTitle.toString();
    }

    String getAlphabeticallySortedLecturerList(Collection<Identity> lecturers) {
        List<FirstNameLastName> firstNameLastNameList = new ArrayList<>();
        for (Identity lecturer : lecturers) {
            String firstname = lecturer.getUser().getProperty(UserConstants.FIRSTNAME, null);
            String lastName = lecturer.getUser().getProperty(UserConstants.LASTNAME, null);
            firstNameLastNameList.add(new FirstNameLastName(firstname, lastName));
        }
        // Sort by name
        Collections.sort(firstNameLastNameList);

        StringBuilder namesAsStringBuilder = new StringBuilder();
        for (FirstNameLastName firstNameLastName : firstNameLastNameList) {
            namesAsStringBuilder.append(firstNameLastName);
            namesAsStringBuilder.append(", ");
        }

        // Remove last ", "
        namesAsStringBuilder.setLength(namesAsStringBuilder.length() - 2);

        return namesAsStringBuilder.toString();
    }

    private class FirstNameLastName implements Comparable<FirstNameLastName> {

        private String firstName;
        private String lastName;

        FirstNameLastName(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public int compareTo(FirstNameLastName other) {
            Collator collator = Collator.getInstance(Locale.GERMAN);
            collator.setStrength(Collator.SECONDARY);// a == A, a < Ä
            int result = collator.compare(lastName, other.lastName);
            if (result == 0) {
                result = collator.compare(firstName, other.firstName);
            }
            return result;
        }

        @Override
        public String toString() {
            return firstName + " " + lastName;
        }
    }

}
