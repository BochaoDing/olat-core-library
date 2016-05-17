package ch.uzh.campus.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
//import org.olat.data.basesecurity.Identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    /*
    public List<SapOlatUser> getSapOlatUsersByOlatUserNameAndSapUserType(String olatUserName, SapOlatUser.SapUserType sapUserType) {
        Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("olatUserName", olatUserName);
        restrictionMap.put("sapUserType", sapUserType);
        List<SapOlatUser> sapOlatUsers = genericDao.findByCriteria(restrictionMap);

        return sapOlatUsers;
    }

    public List<SapOlatUser> getSapOlatUsersBySapIds(Set<Long> sapUserIds) {
        if (sapUserIds == null || sapUserIds.isEmpty()) {
            return new ArrayList<SapOlatUser>();
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(SapOlatUser.SAP_IDS_PARAM, sapUserIds);
        return genericDao.getNamedQueryListResult(SapOlatUser.GET_SAP_OLAT_USERS_BY_SAP_IDS, parameters);
    }

    public void saveMapping(Student student, Identity mappedIdentity) {
        SapOlatUser sapOlatUser = new SapOlatUser(student.getId(), mappedIdentity.getName(), SapUserType.STUDENT);
        genericDao.saveOrUpdate(sapOlatUser);
    }

    public void saveMapping(Lecturer lecturer, Identity mappedIdentity) {
        SapOlatUser sapOlatUser = new SapOlatUser(lecturer.getPersonalNr(), mappedIdentity.getName(), SapUserType.LECTURER);
        genericDao.saveOrUpdate(sapOlatUser);
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
        genericDao.delete(sapUser);
    }

    public void deleteMappingBySapLecturerIds(List<Long> sapIds) {
        Query query = genericDao.getNamedQuery(SapOlatUser.DELETE_SAP_OLAT_LECTURERS_BY_SAP_IDS);
        query.setParameterList("sapIds", sapIds);
        query.executeUpdate();
    }

    public void deleteMappingBySapStudentIds(List<Long> sapIds) {
        Query query = genericDao.getNamedQuery(SapOlatUser.DELETE_SAP_OLAT_STUDENTS_BY_SAP_IDS);
        query.setParameterList("sapIds", sapIds);
        query.executeUpdate();
    }

    public void deleteOldLecturerMapping() {
        Query query = genericDao.getNamedQuery(SapOlatUser.DELETE_SAP_OLAT_LECTURERS);
        query.executeUpdate();
    }

    public void deleteOldStudentMapping() {
        Query query = genericDao.getNamedQuery(SapOlatUser.DELETE_SAP_OLAT_STUDENTS);
        query.executeUpdate();
    }
*/
}
