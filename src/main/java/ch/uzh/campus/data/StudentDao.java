package ch.uzh.campus.data;

import java.util.Date;
import java.util.List;

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
    	 dbInstance.getCurrentEntityManager()
                 .createNamedQuery(Student.DELETE_BY_STUDENT_IDS)
                 .setParameter("studentIds", studentIds)
                 .executeUpdate();
    }
    
    public List<Student> getAllPilotStudents() {               
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_ALL_PILOT_STUDENTS, Student.class)                		
                .getResultList();
    }
      
    
    public List<Student> getAllStudents() {
        // return genericDao.findAll();
        return getAllPilotStudents();
    }

}
