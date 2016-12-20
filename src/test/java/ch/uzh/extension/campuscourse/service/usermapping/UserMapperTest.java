package ch.uzh.extension.campuscourse.service.usermapping;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Martin Schraner
 */

@Component
public class UserMapperTest extends CampusCourseTestCase {

    private String firstNameTestUser1;
    private String firstNameTestUser2;
    private String lastNameTestUser1;
    private String lastNameTestUser2;
    private String emailTestUser1;
    private String emailTestUser2;
    private String matriculationNumberTestUser1;
    private String matriculationNumberTestUser2;
    private String additionalEmployeeNumber1;
    private String additionalEmployeeNumber2;
    private Long employeeNumber3;
    private Long employeeNumber4;
    private Long employeeNumber5;
    private Long employeeNumber6;
    private Identity identity1;
    private Identity identity3;
    private Identity identity4;

    @Autowired
    private DB dbInstance;

    @Autowired
    private UserManager userManager;

    @Autowired
    private BaseSecurity securityManager;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;

    @Before
    public void setUp() {
        Random random = new Random();

        // Create user 1 (with correct institutional name)
        String username1 = "createid-" + UUID.randomUUID();
        firstNameTestUser1 = "first" + username1;
        lastNameTestUser1 = "last" + username1;
        emailTestUser1 = username1 + "@uzh.ch";
        matriculationNumberTestUser1 = StringUtils.remove("matriculation_nr_" + UUID.randomUUID(), "-");  // Must not contain "-"s
        additionalEmployeeNumber1 = "additional-employee-number-" + UUID.randomUUID();
        User user1 = userManager.createUser(firstNameTestUser1, lastNameTestUser1, emailTestUser1);
        // Matriculation number
        user1.setProperty(UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER, matriculationNumberTestUser1);
        // Additional personal number
        user1.setProperty(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, additionalEmployeeNumber1);
        // Correct institutionalName
        user1.setProperty(UserConstants.INSTITUTIONALNAME, campusCourseConfiguration.getMappingInstitutionalName());
        identity1 = securityManager.createAndPersistIdentityAndUser(username1, null, user1, BaseSecurityModule.getDefaultAuthProviderIdentifier(), username1, "secret");

        // Create user 2 (with wrong institutional name) (-> should not map)
        String username2 = "createid-" + UUID.randomUUID();
        firstNameTestUser2 = "first" + username2;
        lastNameTestUser2 = "last" + username2;
        emailTestUser2 = username2 + "@uzh.ch";
        matriculationNumberTestUser2 = StringUtils.remove("matriculation_nr_" + UUID.randomUUID(), "-");  // Must not contain "-"s
        additionalEmployeeNumber2 = "additional-employee-number-" + UUID.randomUUID();
        User user2 = userManager.createUser(firstNameTestUser2, lastNameTestUser2, emailTestUser2);
        // Matriculation number
        user2.setProperty(UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER, matriculationNumberTestUser2);
        // Additional personal number
        user2.setProperty(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, additionalEmployeeNumber2);
        // Wrong institutionalName
        user2.setProperty(UserConstants.INSTITUTIONALNAME, "dummy");
        securityManager.createAndPersistIdentityAndUser(username2, null, user2, BaseSecurityModule.getDefaultAuthProviderIdentifier(), username2, "secret");

        // Create user 3 (for testing mapping by personal number with correct institutional name)
        String username3 = "createid-" + UUID.randomUUID();
        User user3 = userManager.createUser("first" + username3, "last" + username3, username3 + "@uzh.ch");
        employeeNumber3 = getPositiveRandomLong(random);
        user3.setProperty(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, Long.toString(employeeNumber3));
        // Correct institutionalName
        user3.setProperty(UserConstants.INSTITUTIONALNAME, campusCourseConfiguration.getMappingInstitutionalName());
        identity3 = securityManager.createAndPersistIdentityAndUser(username3, null, user3, BaseSecurityModule.getDefaultAuthProviderIdentifier(), username3, "secret");

        // Create user 4: as user 3, but with leading zeroes (-> should map)
        String username4 = "createid-" + UUID.randomUUID();
        User user4 = userManager.createUser("first" + username4, "last" + username4, username4 + "@uzh.ch");
        employeeNumber4 = getPositiveRandomLong(random);
        // Leading zeroes
        user4.setProperty(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, "00" + Long.toString(employeeNumber4));
        // Correct institutionalName
        user4.setProperty(UserConstants.INSTITUTIONALNAME, campusCourseConfiguration.getMappingInstitutionalName());
        identity4 = securityManager.createAndPersistIdentityAndUser(username4, null, user4, BaseSecurityModule.getDefaultAuthProviderIdentifier(), username4, "secret");

        // Create user 5: as user 3, but with leading one (-> should not map)
        String username5 = "createid-" + UUID.randomUUID();
        User user5 = userManager.createUser("first" + username5, "last" + username5, username5 + "@uzh.ch");
        employeeNumber5 = getPositiveRandomLong(random);
        // Leading one
        user5.setProperty(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, "1" + Long.toString(employeeNumber5));
        // Correct institutionalName
        user5.setProperty(UserConstants.INSTITUTIONALNAME, campusCourseConfiguration.getMappingInstitutionalName());
        securityManager.createAndPersistIdentityAndUser(username5, null, user5, BaseSecurityModule.getDefaultAuthProviderIdentifier(), username5, "secret");

        // Create user 6 (for testing mapping by personal number with wrong institutional name) (-> should not map)
        String username6 = "createid-" + UUID.randomUUID();
        User user6 = userManager.createUser("first" + username6, "last" + username6, username6 + "@uzh.ch");
        employeeNumber6 = getPositiveRandomLong(random);
        user6.setProperty(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, Long.toString(employeeNumber6));
        // Correct institutionalName
        user6.setProperty(UserConstants.INSTITUTIONALNAME, "dummy");
        securityManager.createAndPersistIdentityAndUser(username6, null, user6, BaseSecurityModule.getDefaultAuthProviderIdentifier(), username6, "secret");

        dbInstance.flush();
    }

    @After
    public void tearDown() throws Exception {
        dbInstance.rollback();
    }

    private Long getPositiveRandomLong(Random random) {
        Long postitiveRandom;
        do {
            postitiveRandom = random.nextLong();
        } while (postitiveRandom < 0);
        return postitiveRandom;
    }

    @Test
    public void tryToMapByMatriculationNumber() {
        // Try to map user 1 by matriculation number (correct institutional name)
        assertEquals(identity1, userMapper.tryToMapByMatriculationNumber(matriculationNumberTestUser1));

        // Try to map user 1 by matriculation number with additional "-"s (correct institutional name)
        assertEquals(identity1, userMapper.tryToMapByMatriculationNumber("-" + matriculationNumberTestUser1 + "-"));

        // Try to map user 2 by matriculation number (wrong institutional name)
        assertNull(userMapper.tryToMapByMatriculationNumber(matriculationNumberTestUser2));

        // Try to map non existing matriculation number
        assertNull(userMapper.tryToMapByMatriculationNumber(matriculationNumberTestUser1 + "dummy"));
    }

    @Test
    public void tryToMapByPersonalNumber() {
        // Try to map user 3 by personal number (correct institutional name)
        assertEquals(identity3, userMapper.tryToMapByPersonalNumber(employeeNumber3));

        // Try to map user 4 by personal number (correct institutional name, leading zeroes)
        assertEquals(identity4, userMapper.tryToMapByPersonalNumber(employeeNumber4));

        // Try to map user 5 by personal number (correct institutional name, leading one)
        assertNull(userMapper.tryToMapByPersonalNumber(employeeNumber5));

        // Try to map user 6 by personal number (wrong institutional name)
        assertNull(userMapper.tryToMapByPersonalNumber(employeeNumber6));
    }

    @Test
    public void tryToMapByAdditionalPersonalNumber() {
        // Try to map user 1 by additional personal number (correct institutional name)
        assertEquals(identity1, userMapper.tryToMapByAdditionalPersonalNumber(additionalEmployeeNumber1));

        // Try to map user 2 by additional personal number (wrong institutional name)
        assertNull(userMapper.tryToMapByAdditionalPersonalNumber(additionalEmployeeNumber2));

        // Try to map non existing matriculation number
        assertNull(userMapper.tryToMapByAdditionalPersonalNumber(additionalEmployeeNumber2 + "dummy"));
    }

    @Test
    public void tryToMapByEmail() {
        // Try to map user 1 by email (correct institutional name)
        assertEquals(identity1, userMapper.tryToMapByEmail(emailTestUser1));

        // Try to map user 2 by email (wrong institutional name)
        assertNull(userMapper.tryToMapByEmail(emailTestUser2));

        // Try to map non existing email
        assertNull(userMapper.tryToMapByEmail(emailTestUser1 + "dummy"));

        // Try to map empty email
        assertNull(userMapper.tryToMapByEmail(""));
    }

    @Test
    public void tryToMapByFirstNameLastName() {
        // Try to map user 1 by email (correct institutional name)
        assertEquals(identity1, userMapper.tryToMapByFirstNameLastName(firstNameTestUser1, lastNameTestUser1));

        // Try to map user 2 by email (wrong institutional name)
        assertNull(userMapper.tryToMapByFirstNameLastName(firstNameTestUser2, lastNameTestUser2));

        // Try to map non first name and last name
        assertNull(userMapper.tryToMapByFirstNameLastName(firstNameTestUser1 + "dummy", lastNameTestUser1 + "dummy"));
    }

}