package ch.uzh.campus.data;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    private final BaseSecurity baseSecurity;

    @Autowired
    public DataConverter(DelegationDao delegationDao, BaseSecurity baseSecurity) {
        this.delegationDao = delegationDao;
        this.baseSecurity = baseSecurity;
    }

    List<Identity> convertStudentsToIdentities(Set<StudentCourse> studentCourses) {
        List<Identity> identitiesOfStudents = new ArrayList<>();
        for (StudentCourse studentCourse : studentCourses) {
            Identity mappedIdentity = studentCourse.getStudent().getMappedIdentity();
            if (mappedIdentity == null || mappedIdentity.getStatus().equals(Identity.STATUS_DELETED)) {
                continue;
            }
            if (!identitiesOfStudents.contains(mappedIdentity)) {
                identitiesOfStudents.add(mappedIdentity);
            }
        }
        return identitiesOfStudents;
    }

    List<Identity> convertLecturersToIdentities(Set<LecturerCourse> lecturerCourses) {
        List<Identity> identitiesOfLecturers = new ArrayList<>();
        for (LecturerCourse lecturerCourse : lecturerCourses) {
            Identity mappedIdentity = lecturerCourse.getLecturer().getMappedIdentity();
            if (mappedIdentity == null || mappedIdentity.getStatus().equals(Identity.STATUS_DELETED)) {
                continue;
            }
            if (!identitiesOfLecturers.contains(mappedIdentity)) {
                identitiesOfLecturers.add(mappedIdentity);
            }

            // Also add delegatees of lecturer
            List<Delegation> delegations = delegationDao.getDelegationsByDelegator(mappedIdentity.getName());
            for (Delegation delegation : delegations) {
                Identity delegatee = findIdentity(delegation.getDelegatee());
                if (delegatee != null && !identitiesOfLecturers.contains(delegatee)) {
                    identitiesOfLecturers.add(delegatee);
                }
            }
        }
        return identitiesOfLecturers;
    }

    List<Identity> convertDelegateesToIdentities(Set<LecturerCourse> lecturerCourses) {
        List<Identity> identitiesOfDelegatees = new ArrayList<>();
        for (LecturerCourse lecturerCourse : lecturerCourses) {
            Identity mappedIdentity = lecturerCourse.getLecturer().getMappedIdentity();
            if (mappedIdentity == null || mappedIdentity.getStatus().equals(Identity.STATUS_DELETED)) {
                continue;
            }
            List<Delegation> delegations = delegationDao.getDelegationsByDelegator(mappedIdentity.getName());
            for (Delegation delegation : delegations) {
                Identity delegatee = findIdentity(delegation.getDelegatee());
                if (delegatee != null && !identitiesOfDelegatees.contains(delegatee)) {
                    identitiesOfDelegatees.add(delegatee);
                }
            }
        }
        return identitiesOfDelegatees;
    }

    List<Object[]> getDelegatees(Identity delegator) {
        List<Object[]> identitiesOfDelegatees = new ArrayList<>();
        List<Delegation> delegations = delegationDao.getDelegationsByDelegator(delegator.getName());
        for (Delegation delegation : delegations) {
            Identity identity = findIdentity(delegation.getDelegatee());

            if (identity != null) {
                identitiesOfDelegatees.add(new Object[] { identity, delegation.getModifiedDate() });
            }
        }
        return identitiesOfDelegatees;
    }

    private Identity findIdentity(String olatUserName) {
        Identity identity = baseSecurity.findIdentityByName(olatUserName);
        if (identity != null && identity.getStatus().equals(Identity.STATUS_DELETED)) {
            return null;
        }
        return identity;
    }

}
