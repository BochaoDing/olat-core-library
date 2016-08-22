package ch.uzh.campus.mapper;

import ch.uzh.campus.CampusCourseConfiguration;
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
import java.util.Map;

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
public class UserMapper {

    private static final OLog LOG = Tracing.createLoggerFor(UserMapper.class);

    private final DB dbInstance;
    private final CampusCourseConfiguration campusCourseConfiguration;
    private final BaseSecurity baseSecurity;

    @Autowired
    public UserMapper(DB dbInstance, CampusCourseConfiguration campusCourseConfiguration, BaseSecurity baseSecurity) {
        this.dbInstance = dbInstance;
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.baseSecurity = baseSecurity;
    }

    Identity tryToMapByMatriculationNumber(String matriculationNumber) {

        String matriculationNumberDigitsOnly = StringUtils.remove(matriculationNumber, "-");

        // As a first check, execute cheap query
        TypedQuery<Long> query = dbInstance.getCurrentEntityManager().createQuery("select up1.propertyId.userId from userproperty up1 where " +
                "up1.propertyId.name = :propname1 and up1.value = :propvalue1 " +
                "and exists (select up2.propertyId.userId from userproperty up2 where up1.propertyId.userId = up2.propertyId.userId and up2.propertyId.name = :propname2 and up2.value = :propvalue2)", Long.class);

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
        return findIdentityByPowerSearch(userProperties);
    }

    Identity tryToMapByPersonalNumber(Long employeeNumber) {

        // Employee numbers in user property can contain leading zeroes. Such numbers should also map, so we use a "like" in our SQL query.
        String employeeNumberAllowLeadingZeroes = "%" + employeeNumber.toString();

        // As a first check, execute cheap query
        TypedQuery<Long> query = dbInstance.getCurrentEntityManager().createQuery("select up1.propertyId.userId from userproperty up1 where " +
                "up1.propertyId.name = :propname1 and up1.value like :propvalue1 " +
                "and exists (select up2.propertyId.userId from userproperty up2 where up1.propertyId.userId = up2.propertyId.userId and up2.propertyId.name = :propname2 and up2.value = :propvalue2)", Long.class);

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
        Identity mappedIdentity = findIdentityByPowerSearch(userProperties);

        // We have to check that we only found personal numbers with leading ZEROES with the "like" in the query above
        if (mappedIdentity != null) {
            String employeeNumberOfUserProperty = mappedIdentity.getUser().getProperty(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, null);
            try {
                Long employeeNumberOfUserPropertyAsLong = Long.valueOf(employeeNumberOfUserProperty);
                if (employeeNumberOfUserPropertyAsLong.equals(employeeNumber)) {
                    return mappedIdentity;
                }
            } catch (NumberFormatException ex) {
                LOG.warn("Could not convert personal-number to Long");
            }
        }
        return null;
    }

    Identity tryToMapByAdditionalPersonalNumber(String additionalEmployeeNumber) {

        // As a first check, execute cheap query
        TypedQuery<Long> query = dbInstance.getCurrentEntityManager().createQuery("select up1.propertyId.userId from userproperty up1 where " +
                "up1.propertyId.name = :propname1 and up1.value = :propvalue1 " +
                "and exists (select up2.propertyId.userId from userproperty up2 where up1.propertyId.userId = up2.propertyId.userId and up2.propertyId.name = :propname2 and up2.value = :propvalue2)", Long.class);

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
        return findIdentityByPowerSearch(userProperties);
    }

    Identity tryToMapByEmail(String email) {

        if (StringUtils.isBlank(email)) {
            return null;
        }

        // As a first check, execute cheap query
        TypedQuery<Long> query = dbInstance.getCurrentEntityManager().createQuery("select up1.propertyId.userId from userproperty up1 where " +
                "up1.propertyId.name = :propname1 and up1.value = :propvalue1 " +
                "and exists (select up2.propertyId.userId from userproperty up2 where up1.propertyId.userId = up2.propertyId.userId and up2.propertyId.name = :propname2 and up2.value = :propvalue2)", Long.class);

        List<Long> userIdsFound = query.setParameter("propname1", UserConstants.EMAIL)
                .setParameter("propname1", UserConstants.EMAIL)
                .setParameter("propvalue1", email)
                .setParameter("propname2", UserConstants.INSTITUTIONALNAME)
                .setParameter("propvalue2", campusCourseConfiguration.getMappingInstitutionalName())
                .getResultList();

        if (userIdsFound.isEmpty()) {
            return null;
        }

        // Perform expensive power search in case we found someone
        HashMap<String, String> userProperties = new HashMap<>();
        userProperties.put(UserConstants.EMAIL, email);
        userProperties.put(UserConstants.INSTITUTIONALNAME, campusCourseConfiguration.getMappingInstitutionalName());
        return findIdentityByPowerSearch(userProperties);
    }

    Identity tryToMapByFirstNameLastName(String firstName, String lastName) {

        // As a first check, execute cheap query
        TypedQuery<Long> query = dbInstance.getCurrentEntityManager().createQuery("select up1.propertyId.userId from userproperty up1 where " +
                "up1.propertyId.name = :propname1 and up1.value = :propvalue1 " +
                "and exists (select up2.propertyId.userId from userproperty up2 where up1.propertyId.userId = up2.propertyId.userId and up2.propertyId.name = :propname2 and up2.value = :propvalue2) " +
                "and exists (select up3.propertyId.userId from userproperty up3 where up1.propertyId.userId = up3.propertyId.userId and up3.propertyId.name = :propname3 and up3.value = :propvalue3)", Long.class);

        List<Long> userIdsFound = query.setParameter("propname1", UserConstants.FIRSTNAME)
                .setParameter("propvalue1", firstName)
                .setParameter("propname2", UserConstants.LASTNAME)
                .setParameter("propvalue2", lastName)
                .setParameter("propname3", UserConstants.INSTITUTIONALNAME)
                .setParameter("propvalue3", campusCourseConfiguration.getMappingInstitutionalName())
                .getResultList();

        if (userIdsFound.isEmpty()) {
            return null;
        }

        // Perform expensive power search in case we found someone
        HashMap<String, String> userProperties = new HashMap<>();
        userProperties.put(UserConstants.FIRSTNAME, firstName);
        userProperties.put(UserConstants.LASTNAME, lastName);
        userProperties.put(UserConstants.INSTITUTIONALNAME, campusCourseConfiguration.getMappingInstitutionalName());
        return findIdentityByPowerSearch(userProperties);
    }

    private Identity findIdentityByPowerSearch(Map<String, String> userProperties) {

        List<Identity> visibleIdentitiesFound = baseSecurity.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);

        if (visibleIdentitiesFound.isEmpty()) {
            return null;
        } else if (visibleIdentitiesFound.size() == 1) {
            return visibleIdentitiesFound.get(0);
        } else {
            StringBuilder warningMsgSb = new StringBuilder("Tried to map by ");
            for (Map.Entry<String, String> entry : userProperties.entrySet()) {
                warningMsgSb.append(entry.getKey()).append(" = ").append(entry.getValue()).append(", ");
            }
            // Remove last ", "
            warningMsgSb.setLength(warningMsgSb.length() - 2);
            warningMsgSb.append(" and found more than one matching identity.");
            LOG.warn(warningMsgSb.toString());
            return null;
        }
    }

}
