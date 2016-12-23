package ch.uzh.campus.service.data;

import ch.uzh.campus.data.Semester;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

import java.util.List;
import java.util.Set;

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
public class CampusCourseTO {

    private final String titleToBeDisplayed;
    private final Semester semester;
    private final String language;
    private final Set<Identity> lecturersOfCourse;
    private final Set<Identity> delegateesOfCourse;
    private final Set<Identity> participantsOfCourse;
    private final boolean isContinuedCourse;
    private final List<String> titlesOfCourseAndParentCourses;
    private final String eventDescription;
    private final RepositoryEntry repositoryEntry;
    private final CampusGroups campusGroups;
    private final Long sapCourseId;
    private final String vvzLink;

	public CampusCourseTO(String titleToBeDisplayed,
                          Semester semester,
                          Set<Identity> lecturersOfCourse,
                          Set<Identity> delegateesOfCourse,
                          Set<Identity> participantsOfCourse,
                          boolean isContinuedCourse,
                          List<String> titlesOfCourseAndParentCourses,
                          String eventDescription,
                          RepositoryEntry repositoryEntry,
                          CampusGroups campusGroups,
                          Long sapCourseId,
                          String language,
                          String vvzLink) {
        this.titleToBeDisplayed = titleToBeDisplayed;
        this.semester = semester;
        this.isContinuedCourse = isContinuedCourse;
        this.titlesOfCourseAndParentCourses = titlesOfCourseAndParentCourses;
        this.lecturersOfCourse = lecturersOfCourse;
        this.delegateesOfCourse = delegateesOfCourse;
        this.participantsOfCourse = participantsOfCourse;
        this.eventDescription = eventDescription;
        this.repositoryEntry = repositoryEntry;
        this.campusGroups = campusGroups;
        this.sapCourseId = sapCourseId;
        this.language = language;
        this.vvzLink = vvzLink;
    }

    public String getTitleToBeDisplayed() {
        return titleToBeDisplayed;
    }

    public Semester getSemester() {
        return semester;
    }

    public Set<Identity> getLecturersOfCourse() {
        return lecturersOfCourse;
    }

    public Set<Identity> getDelegateesOfCourse() {
        return delegateesOfCourse;
    }

    public Set<Identity> getParticipantsOfCourse() {
        return participantsOfCourse;
    }

    public boolean isContinuedCourse() {
        return isContinuedCourse;
    }

    public List<String> getTitlesOfCourseAndParentCourses() {
        return titlesOfCourseAndParentCourses;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public RepositoryEntry getRepositoryEntry() {
        return repositoryEntry;
    }

    public CampusGroups getCampusGroups() {
		return campusGroups;
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