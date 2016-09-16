package ch.uzh.campus.mapper;

import ch.uzh.campus.data.Lecturer;
import ch.uzh.campus.data.LecturerDao;
import ch.uzh.campus.data.Student;
import ch.uzh.campus.data.StudentDao;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 * @author Martin Schraner
 */
@Component
public class UserMappingDeletion implements UserDataDeletable {

    private static final OLog LOG = Tracing.createLoggerFor(UserMappingDeletion.class);

    private final LecturerDao lecturerDao;
    private final StudentDao studentDao;

    @Autowired
    public UserMappingDeletion(LecturerDao lecturerDao, StudentDao studentDao) {
        this.lecturerDao = lecturerDao;
        this.studentDao = studentDao;
    }


    // This method will be called when a OLAT-user is deleted via deletion-manager
    @Override
    public void deleteUserData(Identity identity, String newDeletedUserName) {
        LOG.debug("deleteUserData start");

        // Remove mapping for lecturers mapped to identity
        for (Lecturer lecturer : lecturerDao.getLecturersMappedToOlatUserName(identity.getName())) {
            LOG.info("Delete mapping for lecturer '" + lecturer + "'");
            lecturerDao.removeMapping(lecturer.getPersonalNr());
        }

        // Remove mapping for students mapped to identity
        for (Student student : studentDao.getStudentsMappedToOlatUserName(identity.getName())) {
            LOG.info("Delete mapping for student '" + student + "'");
            studentDao.removeMapping(student.getId());
        }
    }
}
