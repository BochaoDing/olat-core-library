package ch.uzh.extension.campuscourse.service.usermapping;

import ch.uzh.extension.campuscourse.data.entity.Lecturer;
import ch.uzh.extension.campuscourse.data.dao.LecturerDao;
import org.apache.commons.lang.StringUtils;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

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
public class LecturerMapper {

    private final LecturerDao lecturerDao;
    private final UserMapper userMapper;
    private final BaseSecurity baseSecurity;

    private SecurityGroup authorGroup;

    @Autowired
    public LecturerMapper(LecturerDao lecturerDao, UserMapper userMapper, BaseSecurity baseSecurity) {
        this.lecturerDao = lecturerDao;
        this.userMapper = userMapper;
        this.baseSecurity = baseSecurity;
    }

    @PostConstruct
    public void init() throws Exception {
        authorGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_AUTHORS);
	}

    public UserMappingResult tryToMap(Lecturer lecturer) {

        if (lecturer.getMappedIdentity() != null) {
            return UserMappingResult.ALREADY_MAPPED;
        }

        // First try to map by personal number
        Identity mappedIdentity = userMapper.tryToMapByPersonalNumber(lecturer.getPersonalNr());
        if (mappedIdentity != null) {
            lecturerDao.addMapping(lecturer.getPersonalNr(), mappedIdentity);
            addAuthorRoleToMappedLecturer(mappedIdentity);
            return UserMappingResult.NEW_MAPPING_BY_PERSONAL_NR;
        }

        // Second try to map by email
        mappedIdentity = userMapper.tryToMapByEmail(lecturer.getEmail());
        if (mappedIdentity != null) {
            lecturerDao.addMapping(lecturer.getPersonalNr(), mappedIdentity);
            addAuthorRoleToMappedLecturer(mappedIdentity);
            return UserMappingResult.NEW_MAPPING_BY_EMAIL;
        }

        // Third try to map by additional personal number
        if (StringUtils.isNotBlank(lecturer.getAdditionalPersonalNrs())) {
            List<String> additionalPersonalNrs = Arrays.asList(lecturer.getAdditionalPersonalNrs().split("\\s*,\\s*"));
            for (String additionalPersonalNr : additionalPersonalNrs) {
                mappedIdentity = userMapper.tryToMapByAdditionalPersonalNumber(additionalPersonalNr);
                if (mappedIdentity != null) {
                    lecturerDao.addMapping(lecturer.getPersonalNr(), mappedIdentity);
                    addAuthorRoleToMappedLecturer(mappedIdentity);
                    return UserMappingResult.NEW_MAPPING_BY_ADDITIONAL_PERSONAL_NR;
                }
            }
        }

        // Fourth try to map by first name and last name
        mappedIdentity = userMapper.tryToMapByFirstNameLastName(lecturer.getFirstName(), lecturer.getLastName());
        if (mappedIdentity != null) {
            // DO NOT SAVE THIS MAPPING, BECAUSE IT HAS TO BE DONE MANUALLY
            return UserMappingResult.COULD_BE_MAPPED_MANUALLY;
        } else {
            return UserMappingResult.COULD_NOT_MAP;
        }
    }

    private void addAuthorRoleToMappedLecturer(Identity identity) {
        if (!baseSecurity.isIdentityInSecurityGroup(identity, authorGroup)) {
            baseSecurity.addIdentityToSecurityGroup(identity, authorGroup);
        }
    }
}
