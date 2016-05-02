package ch.uzh.campus.data;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 */
@Repository
public class StudentDao implements CampusDao<Student> {
  
	
	@Autowired
    private DB dbInstance;
   

    @Override
    public void save(List<Student> students) {
    	for(Student student:students) {
    		//TODO: check if possible to save an entire list at once, like it used to work in genericDao
    		//genericDao.saveOrUpdate(students);
    		dbInstance.saveObject(student);
    	}        
    }

    public Student getStudentById(Long id) {
        //return genericDao.findById(id);
    	return dbInstance.findObject(Student.class, id);
    }

    public Student getStudentByEmail(String email) {        
    	List<Student> students = dbInstance.getCurrentEntityManager()
				.createNamedQuery(Student.GET_STUDENTS_WITH_EMAIL, Student.class)
				.setParameter("emailValue", email)				
				.getResultList();
    	if (students != null && !students.isEmpty()) {
            return students.get(0);
        }
        return null;
    }
    

    public Student getStudentByRegistrationNr(String registrationNr) {
        /*Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("registrationNr", registrationNr);
        List<Student> students = genericDao.findByCriteria(restrictionMap);*/
    	
    	List<Student> students = dbInstance.getCurrentEntityManager()
				.createNamedQuery(Student.GET_STUDENTS_WITH_REGISTRATION_NUMBER, Student.class)
				.setParameter("registrationNrValue", registrationNr)				
				.getResultList();
    	    	
        if (students != null && !students.isEmpty()) {
            return students.get(0);
        }
        return null;
    }
    
    public List<Long> getAllNotUpdatedStudents(Date date) {
        /*Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("lastImportDate", date);
        return genericDao.getNamedQueryEntityIds(Student.GET_ALL_NOT_UPDATED_STUDENTS, parameters);*/
        
        return dbInstance.getCurrentEntityManager()
        .createNamedQuery(Student.GET_ALL_NOT_UPDATED_STUDENTS, Long.class)
        .setParameter("lastImportDate", date)			
        .getResultList();
    }
    
    public void delete(Student student) {
        //genericDao.delete(student);
    	dbInstance.deleteObject(student);
    }

    public void deleteByStudentIds(List<Long> studentIds) {
        /*Query query = genericDao.getNamedQuery(Student.DELETE_BY_STUDENT_IDS);
        query.setParameterList("studentIds", studentIds);
        query.executeUpdate();*/
    	
    	/*List<Object> values = new ArrayList<Object>();
    	values.addAll(studentIds);
    	
    	Type[] types = new Type[values.size()];
    	Arrays.fill(types, StandardBasicTypes.LONG);
    	
    	dbInstance.delete(Student.DELETE_BY_STUDENT_IDS, values.toArray(), types);    */
    	
    	 dbInstance.getCurrentEntityManager()
                 .createNamedQuery(Student.DELETE_BY_STUDENT_IDS)
                 .setParameter("studentIds", studentIds)
                 .executeUpdate();
    }

    /*
    public List<Student> getAllStudents() {
        // return genericDao.findAll();
        return getAllPilotStudents();
    }

    public List<Student> getAllPilotStudents() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        return genericDao.getNamedQueryListResult(Student.GET_ALL_PILOT_STUDENTS, parameters);
    }
    

    
*/
}
