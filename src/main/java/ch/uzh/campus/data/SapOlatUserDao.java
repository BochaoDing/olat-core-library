package ch.uzh.campus.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ch.uzh.campus.data.SapOlatUser.SapUserType;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 */
@Repository
public class SapOlatUserDao {
	
	@Autowired
    private DB dbInstance;
	   
    public void save(List<SapOlatUser> sapOlatUsers) {
    	for(SapOlatUser sapOlatUser:sapOlatUsers) {
    		dbInstance.saveObject(sapOlatUser);
    	}
    }

    public SapOlatUser getSapOlatUserBySapUserId(Long sapUserId) {
    	return dbInstance.findObject(SapOlatUser.class, sapUserId);
    }

    public List<SapOlatUser> getSapOlatUserListByOlatUserName(String olatUserName) {              
        return dbInstance.getCurrentEntityManager()
        		.createNamedQuery(SapOlatUser.GET_SAP_OLAT_USER_BY_OLAT_USERNAME, SapOlatUser.class)
        		.setParameter("olatUserName", olatUserName)
        		.getResultList();
    }

    public SapOlatUser getSapOlatUserByOlatUserNameAndSapUserType(String olatUserName, SapOlatUser.SapUserType sapUserType) { 
        List<SapOlatUser> sapOlatUsers = dbInstance.getCurrentEntityManager()
        		.createNamedQuery(SapOlatUser.GET_SAP_OLAT_USER_BY_OLAT_USERNAME_AND_TYPE, SapOlatUser.class)
        		.setParameter("olatUserName", olatUserName)
        		.setParameter("sapUserType", sapUserType)
        		.getResultList();
        if (!sapOlatUsers.isEmpty()) {
            return sapOlatUsers.get(0);
        }
        return null;
    }

    
    public List<SapOlatUser> getSapOlatUsersByOlatUserNameAndSapUserType(String olatUserName, SapOlatUser.SapUserType sapUserType) {
        return dbInstance.getCurrentEntityManager()
        		.createNamedQuery(SapOlatUser.GET_SAP_OLAT_USER_BY_OLAT_USERNAME_AND_TYPE, SapOlatUser.class)
        		.setParameter("olatUserName", olatUserName)
        		.setParameter("sapUserType", sapUserType)
        		.getResultList();
    }
    
    
    public List<SapOlatUser> getSapOlatUsersBySapIds(Set<Long> sapUserIds) {
        if (sapUserIds == null || sapUserIds.isEmpty()) {
            return new ArrayList<SapOlatUser>();
        }                
        return dbInstance.getCurrentEntityManager()
        		.createNamedQuery(SapOlatUser.GET_SAP_OLAT_USERS_BY_SAP_IDS, SapOlatUser.class)
        		.setParameter(SapOlatUser.SAP_IDS_PARAM, sapUserIds)        		
        		.getResultList();
    }
    
    public void saveMapping(Student student, Identity mappedIdentity) {
        SapOlatUser sapOlatUser = new SapOlatUser(student.getId(), mappedIdentity.getName(), SapUserType.STUDENT);        
        dbInstance.saveObject(sapOlatUser);
    }
    
    public void saveMapping(Lecturer lecturer, Identity mappedIdentity) {
        SapOlatUser sapOlatUser = new SapOlatUser(lecturer.getPersonalNr(), mappedIdentity.getName(), SapUserType.LECTURER);
        dbInstance.saveObject(sapOlatUser);
    }

    public boolean existsMappingForSapUserId(Long sapUserId) {
        SapOlatUser sapOlatUser = getSapOlatUserBySapUserId(sapUserId);
        if (sapOlatUser != null) {
            return true;
        } else {
            return false;
        }
    }

   
    public void deleteMapping(SapOlatUser sapUser) {        
        dbInstance.deleteObject(sapUser);
    }
    
    public void deleteMappingBySapLecturerIds(List<Long> sapIds) { 
        dbInstance.getCurrentEntityManager().createNamedQuery(SapOlatUser.DELETE_SAP_OLAT_LECTURERS_BY_SAP_IDS)        
		.setParameter("sapIds", sapIds)		
		.executeUpdate();
    }
    
    public void deleteMappingBySapStudentIds(List<Long> sapIds) { 
        dbInstance.getCurrentEntityManager().createNamedQuery(SapOlatUser.DELETE_SAP_OLAT_STUDENTS_BY_SAP_IDS)        
		.setParameter("sapIds", sapIds)		
		.executeUpdate();
    }
    
    
    public void deleteOldLecturerMapping() {  
        dbInstance.getCurrentEntityManager().createNamedQuery(SapOlatUser.DELETE_SAP_OLAT_LECTURERS) 
		.executeUpdate();
    }
    
    public void deleteOldStudentMapping() {   
        dbInstance.getCurrentEntityManager().createNamedQuery(SapOlatUser.DELETE_SAP_OLAT_STUDENTS) 
		.executeUpdate();
    }

}
