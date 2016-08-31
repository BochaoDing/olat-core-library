package ch.uzh.campus.mapper;

import ch.uzh.campus.data.Lecturer;
import ch.uzh.campus.data.SapOlatUserDao;
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

    private final SapOlatUserDao userMappingDao;
    private final UserMapper userMapper;
    private final BaseSecurity baseSecurity;

    private SecurityGroup authorGroup;

    @Autowired
    public LecturerMapper(SapOlatUserDao userMappingDao, UserMapper userMapper, BaseSecurity baseSecurity) {
        this.userMappingDao = userMappingDao;
        this.userMapper = userMapper;
        this.baseSecurity = baseSecurity;
    }

    @PostConstruct
    public void init() throws Exception {
        authorGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_AUTHORS);
	}

    MappingResult synchronizeLecturerMapping(Lecturer lecturer) {
        if (!userMappingDao.existsMappingForSapUserId(lecturer.getPersonalNr())) {

            // First try to map by personal number
            Identity mappedIdentity = userMapper.tryToMapByPersonalNumber(lecturer.getPersonalNr());
            if (mappedIdentity != null) {
                userMappingDao.saveMapping(lecturer, mappedIdentity);
                addAuthorRoleToMappedLecturer(mappedIdentity);
                return MappingResult.NEW_MAPPING_BY_PERSONAL_NR;
            }

            // Second try to map by email
            mappedIdentity = userMapper.tryToMapByEmail(lecturer.getEmail());
            if (mappedIdentity != null) {
                userMappingDao.saveMapping(lecturer, mappedIdentity);
                addAuthorRoleToMappedLecturer(mappedIdentity);
                return MappingResult.NEW_MAPPING_BY_EMAIL;
            }

            // Third try to map by additional personal number
            if (StringUtils.isNotBlank(lecturer.getAdditionalPersonalNrs())) {
                List<String> additionalPersonalNrs = Arrays.asList(lecturer.getAdditionalPersonalNrs().split("\\s*,\\s*"));
                for (String additionalPersonalNr : additionalPersonalNrs) {
                    mappedIdentity = userMapper.tryToMapByAdditionalPersonalNumber(additionalPersonalNr);
                    if (mappedIdentity != null) {
                        userMappingDao.saveMapping(lecturer, mappedIdentity);
                        addAuthorRoleToMappedLecturer(mappedIdentity);
                        return MappingResult.NEW_MAPPING_BY_ADDITIONAL_PERSONAL_NR;
                    }
                }
            }

            // Fourth try to map by first name and last name
            mappedIdentity = userMapper.tryToMapByFirstNameLastName(lecturer.getFirstName(), lecturer.getLastName());
            if (mappedIdentity != null) {
                // DO NOT SAVE THIS MAPPING, BECAUSE IT HAS TO BE DONE MANUALLY
                return MappingResult.COULD_BE_MAPPED_MANUALLY;
            } else {
                return MappingResult.COULD_NOT_MAP;
            }
        }

        return MappingResult.MAPPING_ALREADY_EXIST;
    }

    private void addAuthorRoleToMappedLecturer(Identity identity) {
        if (!baseSecurity.isIdentityInSecurityGroup(identity, authorGroup)) {
            baseSecurity.addIdentityToSecurityGroup(identity, authorGroup);
        }
    }
}
