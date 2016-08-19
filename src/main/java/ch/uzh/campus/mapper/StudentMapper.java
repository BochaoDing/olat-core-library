package ch.uzh.campus.mapper;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.data.SapOlatUserDao;
import ch.uzh.campus.data.Student;
import org.apache.commons.lang.StringUtils;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.TypedQuery;
import java.util.HashMap;
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
public class StudentMapper {

    private static final OLog LOG = Tracing.createLoggerFor(StudentMapper.class);

    private final DB dbInstance;
    private final CampusCourseConfiguration campusCourseConfiguration;
    private final SapOlatUserDao userMappingDao;
    private final BaseSecurity baseSecurity;

    @Autowired
    public StudentMapper(DB dbInstance, CampusCourseConfiguration campusCourseConfiguration, SapOlatUserDao userMappingDao, BaseSecurity baseSecurity) {
        this.dbInstance = dbInstance;
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.userMappingDao = userMappingDao;
        this.baseSecurity = baseSecurity;
    }

    MappingResult synchronizeStudentMapping(Student student) {

        if (!userMappingDao.existsMappingForSapUserId(student.getId())) {

            TypedQuery<Long> queryTwoProps = dbInstance.getCurrentEntityManager()
                    .createQuery("select up1.propertyId.userId from userproperty up1 where " +
                            "up1.propertyId.name = :propname1 and up1.value = :propvalue1 " +
                            "and exists (select up2.propertyId.userId from userproperty up2 where up1.propertyId.userId = up2.propertyId.userId and up2.propertyId.name = :propname2 and up2.value = :propvalue2)", Long.class);

            TypedQuery<Long> queryThreeProps = dbInstance.getCurrentEntityManager()
                    .createQuery("select up1.propertyId.userId from userproperty up1 where " +
                            "up1.propertyId.name = :propname1 and up1.value = :propvalue1 " +
                            "and exists (select up2.propertyId.userId from userproperty up2 where up1.propertyId.userId = up2.propertyId.userId and up2.propertyId.name = :propname2 and up2.value = :propvalue2) " +
                            "and exists (select up3.propertyId.userId from userproperty up3 where up1.propertyId.userId = up3.propertyId.userId and up3.propertyId.name = :propname3 and up3.value = :propvalue3)", Long.class);

            // First try to map by matriculation number
            Identity mappedIdentity = tryToMapByMatriculationNumber(student, queryTwoProps);
            if (mappedIdentity != null) {
                userMappingDao.saveMapping(student, mappedIdentity);
                return MappingResult.NEW_MAPPING_BY_MATRICULATION_NR;
            }

            // Second try to map by Email
            mappedIdentity = tryToMapByEmail(student, queryTwoProps);
            if (mappedIdentity != null) {
                userMappingDao.saveMapping(student, mappedIdentity);
                return MappingResult.NEW_MAPPING_BY_EMAIL;
            }

            // Third try to map by firstName and lastName
            mappedIdentity = tryToMapByFirstNameLastName(student, queryThreeProps);
            if (mappedIdentity != null) {
                // DO NOT SAVE THIS MAPPING, BECAUSE IT HAS TO BE DONE MANUALLY
                return MappingResult.COULD_BE_MAPPED_MANUALLY;
            } else {
                return MappingResult.COULD_NOT_MAP;
            }
        }

        return MappingResult.MAPPING_ALREADY_EXIST;
    }

    private Identity tryToMapByMatriculationNumber(Student student, TypedQuery<Long> query) {

        String matriculationNumberDigitsOnly = StringUtils.remove(student.getRegistrationNr(), "-");

        // As a first check, execute cheap query
        List<Long> userIdsFound = query.setParameter("propname1", UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER)
                .setParameter("propvalue1", matriculationNumberDigitsOnly)
                .setParameter("propname2", UserConstants.INSTITUTIONALNAME)
                .setParameter("propvalue2", campusCourseConfiguration.getMappingInstitutionalName())
                .getResultList();

        if (userIdsFound.isEmpty()) {
            return null;
        }

        // Perform expensive power search in case we found someone
        HashMap<String, String> userProperties = new HashMap<>();
        userProperties.put(UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER, matriculationNumberDigitsOnly);
        userProperties.put(UserConstants.INSTITUTIONALNAME, campusCourseConfiguration.getMappingInstitutionalName());
        return findIdentityByPowerSearch(userProperties, student);
    }

    private Identity tryToMapByEmail(Student student, TypedQuery<Long> query) {

        if (StringUtils.isBlank(student.getEmail())) {
            return null;
        }

        // As a first check, execute cheap query
        List<Long> userIdsFound = query.setParameter("propname1", UserConstants.EMAIL)
                .setParameter("propvalue1", student.getEmail())
                .setParameter("propname2", UserConstants.INSTITUTIONALNAME)
                .setParameter("propvalue2", campusCourseConfiguration.getMappingInstitutionalName())
                .getResultList();

        if (userIdsFound.isEmpty()) {
            return null;
        }

        // Perform expensive power search in case we found someone
        HashMap<String, String> userProperties = new HashMap<>();
        userProperties.put(UserConstants.EMAIL, student.getEmail());
        userProperties.put(UserConstants.INSTITUTIONALNAME, campusCourseConfiguration.getMappingInstitutionalName());
        return findIdentityByPowerSearch(userProperties, student);
    }

    private Identity tryToMapByFirstNameLastName(Student student, TypedQuery<Long> query) {

        // As a first check, execute cheap query
        List<Long> userIdsFound = query.setParameter("propname1", UserConstants.FIRSTNAME)
                .setParameter("propvalue1", student.getFirstName())
                .setParameter("propname2", UserConstants.LASTNAME)
                .setParameter("propvalue2", student.getLastName())
                .setParameter("propname3", UserConstants.INSTITUTIONALNAME)
                .setParameter("propvalue3", campusCourseConfiguration.getMappingInstitutionalName())
                .getResultList();

        if (userIdsFound.isEmpty()) {
            return null;
        }

        // Perform expensive power search in case we found someone
        HashMap<String, String> userProperties = new HashMap<>();
        userProperties.put(UserConstants.FIRSTNAME, student.getFirstName());
        userProperties.put(UserConstants.LASTNAME, student.getLastName());
        userProperties.put(UserConstants.INSTITUTIONALNAME, campusCourseConfiguration.getMappingInstitutionalName());
        return findIdentityByPowerSearch(userProperties, student);
    }

    private Identity findIdentityByPowerSearch(HashMap<String, String> userProperties, Student student) {

        List<Identity> visibleIdentitiesFound = baseSecurity.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);

        if (visibleIdentitiesFound.isEmpty()) {
            return null;
        } else if (visibleIdentitiesFound.size() == 1) {
            return visibleIdentitiesFound.get(0);
        } else {
            LOG.warn("Found multiple matching students for student from ck_student with id " + student.getId());
            return null;
        }
    }

}
