package ch.uzh.campus.data;

import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
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
 */

@Component
public class DataConverter {

    private final DelegationDao delegationDao;

    @Autowired
    public DataConverter(DelegationDao delegationDao) {
        this.delegationDao = delegationDao;
    }

    public Set<Identity> convertStudentsToIdentities(Set<StudentCourse> studentCourses) {
        Set<Identity> identitiesOfStudents = new HashSet<>();
        for (StudentCourse studentCourse : studentCourses) {
            Identity mappedIdentity = studentCourse.getStudent().getMappedIdentity();
            if (!Identity.STATUS_DELETED.equals(mappedIdentity.getStatus())) {
                identitiesOfStudents.add(mappedIdentity);
            }
        }
        return identitiesOfStudents;
    }

    public Set<Identity> convertLecturersToIdentities(Set<LecturerCourse> lecturerCourses) {
        Set<Identity> identitiesOfLecturers = new HashSet<>();
        for (LecturerCourse lecturerCourse : lecturerCourses) {
            Identity mappedIdentity = lecturerCourse.getLecturer().getMappedIdentity();
            if (!Identity.STATUS_DELETED.equals(mappedIdentity.getStatus())) {
                identitiesOfLecturers.add(mappedIdentity);
            }
        }
        return identitiesOfLecturers;
    }

    public Set<Identity> convertDelegateesToIdentities(Set<LecturerCourse> lecturerCourses) {
        Set<Identity> identitiesOfDelegatees = new HashSet<>();
        for (LecturerCourse lecturerCourse : lecturerCourses) {
            Identity mappedIdentity = lecturerCourse.getLecturer().getMappedIdentity();
            if (!Identity.STATUS_DELETED.equals(mappedIdentity.getStatus())) {
                List<Delegation> delegations = delegationDao.getDelegationsByDelegator(mappedIdentity.getKey());
                for (Delegation delegation : delegations) {
                    if (!delegation.getDelegatee().getStatus().equals(Identity.STATUS_DELETED)) {
                        identitiesOfDelegatees.add(delegation.getDelegatee());
                    }
                }
            }
        }
        return identitiesOfDelegatees;
    }

    List<Object[]> getDelegatees(Identity delegator) {
        List<Object[]> identitiesOfDelegatees = new ArrayList<>();
        List<Delegation> delegations = delegationDao.getDelegationsByDelegator(delegator.getKey());
        for (Delegation delegation : delegations) {
            if (!Identity.STATUS_DELETED.equals(delegation.getDelegatee().getStatus())) {
                identitiesOfDelegatees.add(new Object[] { delegation.getDelegatee(), delegation.getCreationDate() });
            }
        }
        return identitiesOfDelegatees;
    }
}
