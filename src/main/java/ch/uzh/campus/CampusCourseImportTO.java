package ch.uzh.campus;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.resource.OLATResource;

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
public class CampusCourseImportTO {

    private final String title;
    private final String semester;
    private final String language;
    private final List<Identity> lecturersOfCourse;
    private final List<Identity> delegateesOfCourse;
    private final List<Identity> participantsOfCourse;
    private final String eventDescription;
    private final OLATResource olatResource;
    private final Long sapCourseId;
    private final String vvzLink;

    public CampusCourseImportTO(String title, String semester,
								List<Identity> lecturersOfCourse,
								List<Identity> delegateesOfCourse,
								List<Identity> participantsOfCourse,
								String eventDescription, OLATResource olatResource,
								Long sapCourseId, String language, String vvzLink) {
        this.title = title;
        this.semester = semester;
		assert lecturersOfCourse != null;
        this.lecturersOfCourse = lecturersOfCourse;
		assert delegateesOfCourse != null;
        this.delegateesOfCourse = delegateesOfCourse;
		assert participantsOfCourse != null;
        this.participantsOfCourse = participantsOfCourse;
        this.eventDescription = eventDescription;
        this.olatResource = olatResource;
        this.sapCourseId = sapCourseId;
        this.language = language;
        this.vvzLink = vvzLink;
    }

    public String getSemester() {
        return semester;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getTitle() {
        return title;
    }

    public List<Identity> getLecturersOfCourse() {
        return lecturersOfCourse;
    }

    public List<Identity> getDelegateesOfCourse() {
        return delegateesOfCourse;
    }

    public List<Identity> getParticipantsOfCourse() {
        return participantsOfCourse;
    }

    public OLATResource getOlatResource() {
        return olatResource;
    }

    public boolean isOlatResourceUndefined() {
        return getOlatResource() == null;
    }

    public Long getSapCourseId() {
        return sapCourseId;
    }

    public String getLanguage() {
        return language;
    }

    public String getVvzLink() {
        return vvzLink;
    }
}