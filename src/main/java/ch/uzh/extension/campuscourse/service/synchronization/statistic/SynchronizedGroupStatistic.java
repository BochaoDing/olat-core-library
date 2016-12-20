package ch.uzh.extension.campuscourse.service.synchronization.statistic;

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
 * Initial Date: 25.06.2012 <br>
 * 
 * @author cg
 */
public class SynchronizedGroupStatistic {

    private String courseTitle;
    private SynchronizedSecurityGroupStatistic coachGroupStatistic;
    private SynchronizedSecurityGroupStatistic participantGroupStatistic;

    public SynchronizedGroupStatistic(String courseTitle, SynchronizedSecurityGroupStatistic coachGroupStatistic,
            SynchronizedSecurityGroupStatistic participantGroupStatistic) {
        this.courseTitle = courseTitle;
        this.coachGroupStatistic = coachGroupStatistic;
        this.participantGroupStatistic = participantGroupStatistic;
    }

    public String toString() {
        return "SynchronizedGroupStatistic: " + "course='" + courseTitle + "' " +
                "Coach-Group: " + coachGroupStatistic + "Participant-Group: " + participantGroupStatistic;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public SynchronizedSecurityGroupStatistic getCoachGroupStatistic() {
        return coachGroupStatistic;
    }

    public SynchronizedSecurityGroupStatistic getParticipantGroupStatistic() {
        return participantGroupStatistic;
    }
}
