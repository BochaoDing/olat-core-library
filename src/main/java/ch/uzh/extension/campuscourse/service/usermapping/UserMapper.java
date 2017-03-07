package ch.uzh.extension.campuscourse.service.usermapping;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import org.apache.commons.lang.StringUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Martin Schraner
 */
@Component
public class UserMapper {

    private static final OLog LOG = Tracing.createLoggerFor(UserMapper.class);

    private final DB dbInstance;
    private final CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    public UserMapper(DB dbInstance, CampusCourseConfiguration campusCourseConfiguration) {
        this.dbInstance = dbInstance;
        this.campusCourseConfiguration = campusCourseConfiguration;
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

        // Execute more expensive query to find identity in case we found someone
        String multipleIdentitiesFoundWarning = "Tried to map by " + UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER + " = " + matriculationNumberDigitsOnly
                + ", " + UserConstants.INSTITUTIONALNAME + " = " + campusCourseConfiguration.getMappingInstitutionalName()
                + " and found more than one matching identity.";

        return findIdentity(userIdsFound, multipleIdentitiesFoundWarning);
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

        // We have to check that we only found personal numbers with leading ZEROES with the "like" in the query above
        // -> remove userId from list if it does not match
        Iterator<Long> iterator = userIdsFound.iterator();
        while (iterator.hasNext()) {
            Long userId = iterator.next();
            UserImpl user = dbInstance.getCurrentEntityManager().find(UserImpl.class, userId);
            if (user != null) {
                String employeeNumberOfUserProperty = user.getProperty(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, null);
                try {
                    Long employeeNumberOfUserPropertyAsLong = Long.valueOf(employeeNumberOfUserProperty);
                    if (employeeNumberOfUserPropertyAsLong.equals(employeeNumber)) {
                        // everything fine
                        continue;
                    }
                } catch (NumberFormatException ex) {
                    LOG.warn("Could not convert personal-number " + employeeNumberOfUserProperty + " to Long");
                }
            }
            iterator.remove();
        }

        if (userIdsFound.isEmpty()) {
            return null;
        }

        // Execute more expensive query to find identity in case we found someone
        String multipleIdentitiesFoundWarning = "Tried to map by " + UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER + " = " + employeeNumberAllowLeadingZeroes
                + ", " + UserConstants.INSTITUTIONALNAME + " = " + campusCourseConfiguration.getMappingInstitutionalName()
                + " and found more than one matching identity.";

        return findIdentity(userIdsFound, multipleIdentitiesFoundWarning);
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

        // Execute more expensive query to find identity in case we found someone
        String multipleIdentitiesFoundWarning = "Tried to map by " + UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER + " = " + additionalEmployeeNumber
                + ", " + UserConstants.INSTITUTIONALNAME + " = " + campusCourseConfiguration.getMappingInstitutionalName()
                + " and found more than one matching identity.";

        return findIdentity(userIdsFound, multipleIdentitiesFoundWarning);
    }

    Identity tryToMapByEmail(String email) {

        if (StringUtils.isBlank(email)) {
            return null;
        }

        // Execute cheap query to find user id
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

        // Execute more expensive query to find identity in case we found someone
        String multipleIdentitiesFoundWarning = "Tried to map by " + UserConstants.EMAIL + " = " + email
                + ", " + UserConstants.INSTITUTIONALNAME + " = " + campusCourseConfiguration.getMappingInstitutionalName()
                + " and found more than one matching identity.";

        return findIdentity(userIdsFound, multipleIdentitiesFoundWarning);
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

        // Execute more expensive query to find identity in case we found someone
        String multipleIdentitiesFoundWarning = "Tried to map by " + UserConstants.FIRSTNAME + " = " + firstName
                + ", " + UserConstants.LASTNAME + " = " + lastName
                + ", " + UserConstants.INSTITUTIONALNAME + " = " + campusCourseConfiguration.getMappingInstitutionalName()
                + " and found more than one matching identity.";

        return findIdentity(userIdsFound, multipleIdentitiesFoundWarning);
    }

    private Identity findIdentity(List<Long> userIds, String multipleIdentitiesFoundWarning) {

        List<UserImpl> users = new ArrayList<>();
        for (Long userId : userIds) {
            users.add(dbInstance.getCurrentEntityManager().getReference(UserImpl.class, userId));
        }

        // Identity must be visible
        @SuppressWarnings("JpaQlInspection")
        TypedQuery<Identity> query = dbInstance.getCurrentEntityManager().createQuery("select ident from org.olat.core.id.Identity as ident where " +
                "ident.user in :users and ident.status < :status", Identity.class);

        List<Identity> identitiesFound = query.setParameter("users", users)
                .setParameter("status", Identity.STATUS_VISIBLE_LIMIT)
                .getResultList();

        if (identitiesFound.isEmpty()) {
            return null;
        } else if (identitiesFound.size() == 1) {
            return identitiesFound.get(0);
        } else {
            LOG.warn(multipleIdentitiesFoundWarning);
            return null;
        }
    }
}