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

import java.util.List;

import org.olat.core.id.Identity;



/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
public class CampusCourseImportTO {

    private String title;
    private String semester;
    private String language;
    private List<Identity> lecturersOfCourseAndParentCourses;
    private List<Identity> delegateesOfCourseAndParentCourses;
    private List<Identity> participantsOfCourseAndParentCourses;
    private String eventDescription;
    private Long resourceableId;
    private Long sapCourseId;
    private final String vvzLink;

    /**
     * just for testing
     *
     */
    public CampusCourseImportTO(String title, String semester, List<Identity> lecturersOfCourseAndParentCourses, List<Identity> participantsOfCourseAndParentCourses, String eventDescription, Long resourceableId, String language) {
        this.title = title;
        this.semester = semester;
        this.lecturersOfCourseAndParentCourses = lecturersOfCourseAndParentCourses;
        this.participantsOfCourseAndParentCourses = participantsOfCourseAndParentCourses;
        this.eventDescription = eventDescription;
        this.resourceableId = resourceableId;
        this.language = language;
        this.vvzLink = "";
    }

    public CampusCourseImportTO(String title, String semester, List<Identity> lecturersOfCourseAndParentCourses, List<Identity> delegateesOfCourseAndParentCourses, List<Identity> participantsOfCourseAndParentCourses, String eventDescription,
                                Long resourceableId, Long sapCourseId, String language, String vvzLink) {
        this.title = title;
        this.semester = semester;
        this.lecturersOfCourseAndParentCourses = lecturersOfCourseAndParentCourses;
        this.delegateesOfCourseAndParentCourses = delegateesOfCourseAndParentCourses;
        this.participantsOfCourseAndParentCourses = participantsOfCourseAndParentCourses;
        this.eventDescription = eventDescription;
        this.resourceableId = resourceableId;
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

    public List<Identity> getLecturersOfCourseAndParentCourses() {
        return lecturersOfCourseAndParentCourses;
    }

    public List<Identity> getDelegateesOfCourseAndParentCourses() {
        return delegateesOfCourseAndParentCourses;
    }

    public List<Identity> getParticipantsOfCourseAndParentCourses() {
        return participantsOfCourseAndParentCourses;
    }

    public Long getOlatResourceableId() {
        return resourceableId;
    }

    public boolean isOlatResourceableIdUndefined() {
        return getOlatResourceableId() == null;
    }

    public Long getSapCourseId() {
        return sapCourseId;
    }

    public String getLanguage() {
        return language;
    }

    public List<Identity> getLecturersAndDelegatees() {
        List<Identity> lecturersAndDelegatees = getLecturersOfCourseAndParentCourses();
        lecturersAndDelegatees.addAll(getDelegateesOfCourseAndParentCourses());
        return lecturersAndDelegatees;
    }

    public String getVvzLink() {
        return vvzLink;
    }

}