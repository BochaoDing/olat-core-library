package ch.uzh.campus.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Provider;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Initial Date: Oct 27, 2014 <br>
 * 
 * @author aabouc
 * @author lavinia
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class SapOlatUserDaoTest  extends OlatTestCase {

	@Autowired
	private DB dbInstance;

	@Autowired
    private SapOlatUserDao sapOlatUserDao;

    @Autowired
    private LecturerDao lecturerDao;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private Provider<MockDataGenerator> mockDataGeneratorProvider;

    @Before
    public void setup() {
        List<SapOlatUser> sapOlatUsers = mockDataGeneratorProvider.get().getSapOlatUsers();
        sapOlatUserDao.save(sapOlatUsers);
        dbInstance.flush();
    }
    
    @After
    public void after() {
    	dbInstance.rollback();
    }
    
    @Test
    public void testGetSapOlatUserBySapUserId_Null() {
        assertNull(sapOlatUserDao.getSapOlatUserBySapUserId(500L));
    }

    @Test
    public void testGetSapOlatUserBySapUserId_NotNull() {
        assertNotNull(sapOlatUserDao.getSapOlatUserBySapUserId(100L));
    }

    @Test
    public void testGetSapOlatUserListByOlatUserName_Empty() {
        assertTrue(sapOlatUserDao.getSapOlatUserListByOlatUserName("olatUserName500").isEmpty());
    }

    @Test
    public void testGetSapOlatUserListByOlatUserName_NotEmpty() {
        assertFalse(sapOlatUserDao.getSapOlatUserListByOlatUserName("olatUserName1").isEmpty());
        assertEquals(1, sapOlatUserDao.getSapOlatUserListByOlatUserName("olatUserName1").size());
    }

    @Test
    public void testGetSapOlatUserByOlatUserNameAndSapUserType_Null() {
        assertNull(sapOlatUserDao.getSapOlatUserByOlatUserNameAndSapUserType("olatUserName500", SapOlatUser.SapUserType.LECTURER));
        assertNull(sapOlatUserDao.getSapOlatUserByOlatUserNameAndSapUserType("olatUserName500", SapOlatUser.SapUserType.STUDENT));
    }

    @Test
    public void testGetSapOlatUserByOlatUserNameAndSapUserType_NotNull() {
        assertNotNull(sapOlatUserDao.getSapOlatUserByOlatUserNameAndSapUserType("olatUserName2", SapOlatUser.SapUserType.LECTURER));
        assertNotNull(sapOlatUserDao.getSapOlatUserByOlatUserNameAndSapUserType("olatUserName1", SapOlatUser.SapUserType.STUDENT));
    }
    
    @Test
    public void testGetSapOlatUsersByOlatUserNameAndSapUserType_Empty() {
        assertTrue(sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType("olatUserName500", SapOlatUser.SapUserType.LECTURER).isEmpty());
        assertTrue(sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType("olatUserName500", SapOlatUser.SapUserType.STUDENT).isEmpty());
    }

    @Test
    public void testGetSapOlatUsersByOlatUserNameAndSapUserType_NotEmpty() {
        assertFalse(sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType("olatUserName2", SapOlatUser.SapUserType.LECTURER).isEmpty());
        assertFalse(sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType("olatUserName1", SapOlatUser.SapUserType.STUDENT).isEmpty());
    }
   
    @Test
    public void testGetSapOlatUsersBySapIds_Empty() {
        assertTrue(sapOlatUserDao.getSapOlatUsersBySapIds(Collections.emptySet()).isEmpty());
    }

    @Test
    public void testNotEmptyGetSapOlatUsersBySapIds() {
        Set<Long> sapIds = new HashSet<>();
        sapIds.add(100L);
        sapIds.add(200L);
        assertFalse(sapOlatUserDao.getSapOlatUsersBySapIds(sapIds).isEmpty());
        assertEquals(2, sapOlatUserDao.getSapOlatUsersBySapIds(sapIds).size());
    }
    
    @Test
    public void testNotExistsMappingForSapUserId() {
        assertFalse(sapOlatUserDao.existsMappingForSapUserId(500L));
    }

    @Test
    public void testExistsMappingForSapUserId() {
        assertTrue(sapOlatUserDao.existsMappingForSapUserId(100L));
    }

    @Test
    public void testSaveMappingForStudent() {
        assertFalse(sapOlatUserDao.existsMappingForSapUserId(901L));

        Student student = new Student();
        student.setId(901L);
        Identity identity = mock(Identity.class);
        when(identity.getName()).thenReturn("studentOlatUserName");

        sapOlatUserDao.saveMapping(student, identity);

        assertTrue(sapOlatUserDao.existsMappingForSapUserId(901L));
    }

    @Test
    public void testSaveMappingForLecturer() {
        assertFalse(sapOlatUserDao.existsMappingForSapUserId(902L));

        Lecturer lecturer = new Lecturer();
        lecturer.setPersonalNr(902L);
        Identity identity = mock(Identity.class);
        when(identity.getName()).thenReturn("lecturerOlatUserName");

        sapOlatUserDao.saveMapping(lecturer, identity);

        assertTrue(sapOlatUserDao.existsMappingForSapUserId(902L));
    }
    
    @Test
    public void testDeleteMappingAsBulkDelete() {
        assertEquals(1, sapOlatUserDao.getSapOlatUserListByOlatUserName("olatUserName1").size());

        sapOlatUserDao.deleteMapping(sapOlatUserDao.getSapOlatUserListByOlatUserName("olatUserName1").get(0));

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, sapOlatUserDao.getSapOlatUserListByOlatUserName("olatUserName1").size());
    }
    
    @Test
    public void testDeleteMappingBySapLecturerIdsAsBulkDelete() {
        assertEquals(1, sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType("olatUserName2", SapOlatUser.SapUserType.LECTURER).size());

        List<Long> sapIds = new LinkedList<>();
        sapIds.add(200L);

        sapOlatUserDao.deleteMappingBySapLecturerIdsAsBulkDelete(sapIds);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType("olatUserName1", SapOlatUser.SapUserType.LECTURER).size());
    }
    
    @Test
    public void testDeleteMappingBySapStudentIdsAsBulkDelete() {
        assertEquals(1, sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType("olatUserName1", SapOlatUser.SapUserType.STUDENT).size());

        List<Long> sapIds = new LinkedList<>();
        sapIds.add(100L);

        sapOlatUserDao.deleteMappingBySapStudentIdsAsBulkDelete(sapIds);

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType("olatUserName1", SapOlatUser.SapUserType.STUDENT).size());
    }
    
    @Test
    public void testDeleteOldLecturerMappingAsBulkDelete() {
        assertEquals(1, sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType("olatUserName2", SapOlatUser.SapUserType.LECTURER).size());

        lecturerDao.delete(mockDataGeneratorProvider.get().getLecturers().get(1));
        sapOlatUserDao.deleteOldLecturerMappingAsBulkDelete();

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType("olatUserName2", SapOlatUser.SapUserType.LECTURER).size());
    }
        
    @Test
    public void testDeleteOldStudentMappingAsBulkDelete() {
        assertEquals(1, sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType("olatUserName1", SapOlatUser.SapUserType.STUDENT).size());

        studentDao.delete(mockDataGeneratorProvider.get().getStudents().get(0));
        sapOlatUserDao.deleteOldStudentMappingAsBulkDelete();

        dbInstance.flush();
        dbInstance.clear();

        assertEquals(0, sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType("olatUserName2", SapOlatUser.SapUserType.STUDENT).size());
    }
    
}
