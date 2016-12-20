package ch.uzh.extension.campuscourse.service.usermapping;

import ch.uzh.extension.campuscourse.data.entity.Student;
import ch.uzh.extension.campuscourse.data.dao.StudentDao;
import org.olat.core.id.Identity;
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
public class StudentMapper {

    private final StudentDao studentDao;
    private final UserMapper userMapper;

    @Autowired
    public StudentMapper(StudentDao studentDao, UserMapper userMapper) {
        this.studentDao = studentDao;
        this.userMapper = userMapper;
    }

    public UserMappingResult tryToMap(Student student) {

        if (student.getMappedIdentity() != null) {
            return UserMappingResult.ALREADY_MAPPED;
        }

        // First try to map by matriculation number
        Identity mappedIdentity = userMapper.tryToMapByMatriculationNumber(student.getRegistrationNr());
        if (mappedIdentity != null) {
            studentDao.addMapping(student.getId(), mappedIdentity);
            return UserMappingResult.NEW_MAPPING_BY_MATRICULATION_NR;
        }

        // Second try to map by email
        mappedIdentity = userMapper.tryToMapByEmail(student.getEmail());
        if (mappedIdentity != null) {
            studentDao.addMapping(student.getId(), mappedIdentity);
            return UserMappingResult.NEW_MAPPING_BY_EMAIL;
        }

        // Third try to map by first name and last name
        mappedIdentity = userMapper.tryToMapByFirstNameLastName(student.getFirstName(), student.getLastName());
        if (mappedIdentity != null) {
            // DO NOT SAVE THIS MAPPING, BECAUSE IT HAS TO BE DONE MANUALLY
            return UserMappingResult.COULD_BE_MAPPED_MANUALLY;
        } else {
            return UserMappingResult.COULD_NOT_MAP;
        }
    }

}
