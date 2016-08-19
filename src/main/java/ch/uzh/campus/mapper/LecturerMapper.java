package ch.uzh.campus.mapper;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.data.Lecturer;
import ch.uzh.campus.data.SapOlatUserDao;
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
import java.util.Arrays;
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
public class LecturerMapper {

    private static final OLog LOG = Tracing.createLoggerFor(LecturerMapper.class);

    private final DB dbInstance;
    private final CampusCourseConfiguration campusCourseConfiguration;
    private final SapOlatUserDao userMappingDao;
    private final BaseSecurity baseSecurity;

    @Autowired
    public LecturerMapper(DB dbInstance, CampusCourseConfiguration campusCourseConfiguration, SapOlatUserDao userMappingDao, BaseSecurity baseSecurity) {
        this.dbInstance = dbInstance;
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.userMappingDao = userMappingDao;
        this.baseSecurity = baseSecurity;
    }

    MappingResult synchronizeLecturerMapping(Lecturer lecturer) {
        if (!userMappingDao.existsMappingForSapUserId(lecturer.getPersonalNr())) {

            TypedQuery<Long> queryTwoProps = dbInstance.getCurrentEntityManager()
                    .createQuery("select up1.propertyId.userId from userproperty up1 where " +
                            "up1.propertyId.name = :propname1 and up1.value = :propvalue1 " +
                            "and exists (select up2.propertyId.userId from userproperty up2 where up1.propertyId.userId = up2.propertyId.userId and up2.propertyId.name = :propname2 and up2.value = :propvalue2)", Long.class);

            TypedQuery<Long> queryTwoPropsWithLike = dbInstance.getCurrentEntityManager()
                    .createQuery("select up1.propertyId.userId from userproperty up1 where " +
                            "up1.propertyId.name = :propname1 and up1.value like :propvalue1 " +
                            "and exists (select up2.propertyId.userId from userproperty up2 where up1.propertyId.userId = up2.propertyId.userId and up2.propertyId.name = :propname2 and up2.value = :propvalue2)", Long.class);

            TypedQuery<Long> queryThreeProps = dbInstance.getCurrentEntityManager()
                    .createQuery("select up1.propertyId.userId from userproperty up1 where " +
                            "up1.propertyId.name = :propname1 and up1.value = :propvalue1 " +
                            "and exists (select up2.propertyId.userId from userproperty up2 where up1.propertyId.userId = up2.propertyId.userId and up2.propertyId.name = :propname2 and up2.value = :propvalue2) " +
                            "and exists (select up3.propertyId.userId from userproperty up3 where up1.propertyId.userId = up3.propertyId.userId and up3.propertyId.name = :propname3 and up3.value = :propvalue3)", Long.class);

            // First try to map by personal number
            Identity mappedIdentity = tryToMapByPersonalNumber(lecturer, queryTwoPropsWithLike);
            if (mappedIdentity != null) {
                userMappingDao.saveMapping(lecturer, mappedIdentity);
                return MappingResult.NEW_MAPPING_BY_PERSONAL_NR;
            }

            // second try to map by Email
            mappedIdentity = tryToMapByEmail(lecturer, queryTwoProps);
            if (mappedIdentity != null) {
                userMappingDao.saveMapping(lecturer, mappedIdentity);
                return MappingResult.NEW_MAPPING_BY_EMAIL;
            }

            // third try to map by additional personal number
            if (StringUtils.isNotBlank(lecturer.getAdditionalPersonalNrs())) {
                List<String> personalNrs = Arrays.asList(lecturer.getAdditionalPersonalNrs().split("\\s*,\\s*"));
                for (String personalNr : personalNrs) {
                    mappedIdentity = tryToMapByAdditionalPersonalNumber(personalNr, lecturer, queryTwoProps);
                    if (mappedIdentity != null) {
                        userMappingDao.saveMapping(lecturer, mappedIdentity);
                        return MappingResult.NEW_MAPPING_BY_ADDITIONAL_PERSONAL_NR;
                    }
                }
            }

            // Third try to map by firstName and lastName
            mappedIdentity = tryToMapByFirstNameLastName(lecturer, queryThreeProps);
            if (mappedIdentity != null) {
                // DO NOT SAVE THIS MAPPING, BECAUSE IT HAS TO BE DONE MANUALLY
                return MappingResult.COULD_BE_MAPPED_MANUALLY;
            } else {
                return MappingResult.COULD_NOT_MAP;
            }
        }
        return MappingResult.MAPPING_ALREADY_EXIST;
    }

    private Identity tryToMapByPersonalNumber(Lecturer lecturer, TypedQuery<Long> query) {

        String employeeNumberAllowLeadingZeroes = "%" + lecturer.getPersonalNr().toString();

        // As a first check, execute cheap query
        List<Long> userIdsFound = query.setParameter("propname1", UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER)
                .setParameter("propvalue1", employeeNumberAllowLeadingZeroes)
                .setParameter("propname2", UserConstants.INSTITUTIONALNAME)
                .setParameter("propvalue2", campusCourseConfiguration.getMappingInstitutionalName())
                .getResultList();

        if (userIdsFound.isEmpty()) {
            return null;
        }

        // Perform expensive power search in case we found someone
        HashMap<String, String> userProperties = new HashMap<>();
        userProperties.put(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, employeeNumberAllowLeadingZeroes);
        userProperties.put(UserConstants.INSTITUTIONALNAME, campusCourseConfiguration.getMappingInstitutionalName());

        Identity mappedIdentity = findIdentityByPowerSearch(userProperties, lecturer);
        if (mappedIdentity != null) {
            String personalNumber = findProperty(mappedIdentity, UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER);
            try {
                Long personalNumberAsLong = Long.valueOf(personalNumber);
                if (personalNumberAsLong.equals(lecturer.getPersonalNr())) {
                    return mappedIdentity;
                }
                LOG.warn("User-Property as Long (" + personalNumberAsLong + ") has not the same value as lecturer personal-number=" + lecturer.getPersonalNr());
            } catch (NumberFormatException ex) {
                LOG.warn("Could not convert personal-number to Long");
            }
        }
        return null;
    }

    private Identity tryToMapByEmail(Lecturer lecturer, TypedQuery<Long> query) {

        if (StringUtils.isBlank(lecturer.getEmail())) {
            return null;
        }

        // As a first check, execute cheap query
        List<Long> userIdsFound = query.setParameter("propname1", UserConstants.EMAIL)
                .setParameter("propvalue1", lecturer.getEmail())
                .setParameter("propname2", UserConstants.INSTITUTIONALNAME)
                .setParameter("propvalue2", campusCourseConfiguration.getMappingInstitutionalName())
                .getResultList();

        if (userIdsFound.isEmpty()) {
            return null;
        }

        // Perform expensive power search in case we found someone
        HashMap<String, String> userProperties = new HashMap<>();
        userProperties.put(UserConstants.EMAIL, lecturer.getEmail());
        userProperties.put(UserConstants.INSTITUTIONALNAME, campusCourseConfiguration.getMappingInstitutionalName());
        return findIdentityByPowerSearch(userProperties, lecturer);
    }

    private Identity tryToMapByAdditionalPersonalNumber(String additionalEmployeeNumber, Lecturer lecturer, TypedQuery<Long> query) {

        // As a first check, execute cheap query
        List<Long> userIdsFound = query.setParameter("propname1", UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER)
                .setParameter("propvalue1", additionalEmployeeNumber)
                .setParameter("propname2", UserConstants.INSTITUTIONALNAME)
                .setParameter("propvalue2", campusCourseConfiguration.getMappingInstitutionalName())
                .getResultList();

        if (userIdsFound.isEmpty()) {
            return null;
        }

        // Perform expensive power search in case we found someone
        HashMap<String, String> userProperties = new HashMap<>();
        userProperties.put(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, additionalEmployeeNumber);
        userProperties.put(UserConstants.INSTITUTIONALNAME, campusCourseConfiguration.getMappingInstitutionalName());
        return findIdentityByPowerSearch(userProperties, lecturer);
    }

    private Identity tryToMapByFirstNameLastName(Lecturer lecturer, TypedQuery<Long> query) {

        // As a first check, execute cheap query
        List<Long> userIdsFound = query.setParameter("propname1", UserConstants.FIRSTNAME)
                .setParameter("propvalue1", lecturer.getFirstName())
                .setParameter("propname2", UserConstants.LASTNAME)
                .setParameter("propvalue2", lecturer.getLastName())
                .setParameter("propname3", UserConstants.INSTITUTIONALNAME)
                .setParameter("propvalue3", campusCourseConfiguration.getMappingInstitutionalName())
                .getResultList();

        if (userIdsFound.isEmpty()) {
            return null;
        }

        // Perform expensive power search in case we found someone
        HashMap<String, String> userProperties = new HashMap<>();
        userProperties.put(UserConstants.FIRSTNAME, lecturer.getFirstName());
        userProperties.put(UserConstants.LASTNAME, lecturer.getLastName());
        userProperties.put(UserConstants.INSTITUTIONALNAME, campusCourseConfiguration.getMappingInstitutionalName());
        return findIdentityByPowerSearch(userProperties, lecturer);
    }

    private Identity findIdentityByPowerSearch(HashMap<String, String> userProperties, Lecturer lecturer) {

        List<Identity> visibleIdentitiesFound = baseSecurity.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);

        if (visibleIdentitiesFound.isEmpty()) {
            return null;
        } else if (visibleIdentitiesFound.size() == 1) {
            return visibleIdentitiesFound.get(0);
        } else {
            LOG.warn("Found multiple matching students for lecturer from ck_lecturer with id " + lecturer.getPersonalNr());
            return null;
        }
    }

    /**
     * Returns the property from identity using the same fall-back logic as tryToMap()
     */
    private String findProperty(Identity mappedIdentity, String propertyName) {
        String personalNumber = mappedIdentity.getUser().getProperty(propertyName, null);
        if (personalNumber == null) {
            personalNumber = mappedIdentity.getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
        }
        return personalNumber;
    }

}
