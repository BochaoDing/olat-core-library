package ch.uzh.extension.campuscourse.service.synchronization;

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
 * @author Martin Schraner
 */
public class CampusCourseSynchronizationResult {

    private final String courseTitle;
    private final int addedCoaches;
    private final int removedCoaches;
    private final int addedParticipants;
    private final int removedParticipants;

	public CampusCourseSynchronizationResult(String courseTitle, int addedCoaches, int removedCoaches, int addedParticipants, int removedParticipants) {
		this.courseTitle = courseTitle;
		this.addedCoaches = addedCoaches;
		this.removedCoaches = removedCoaches;
		this.addedParticipants = addedParticipants;
		this.removedParticipants = removedParticipants;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public int getAddedCoaches() {
		return addedCoaches;
	}

	public int getRemovedCoaches() {
		return removedCoaches;
	}

	public int getAddedParticipants() {
		return addedParticipants;
	}

	public int getRemovedParticipants() {
		return removedParticipants;
	}
}
