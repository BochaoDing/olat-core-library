package ch.uzh.campus.data;


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
        /*Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("email", email);
        List<Student> students = genericDao.findByCriteria(restrictionMap);
        */
    	List<Student> students = dbInstance.getCurrentEntityManager()
				.createNamedQuery(Student.GET_STUDENTS_WITH_EMAIL, Student.class)
				.setParameter("emailValue", email)				
				.getResultList();
    	if (students != null && !students.isEmpty()) {
            return students.get(0);
        }
        return null;
    }
    /*

    public Student getStudentByRegistrationNr(String registrationNr) {
        Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("registrationNr", registrationNr);
        List<Student> students = genericDao.findByCriteria(restrictionMap);
        if (students != null && !students.isEmpty()) {
            return students.get(0);
        }
        return null;
    }

    public List<Student> getAllStudents() {
        // return genericDao.findAll();
        return getAllPilotStudents();
    }

    public List<Student> getAllPilotStudents() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        return genericDao.getNamedQueryListResult(Student.GET_ALL_PILOT_STUDENTS, parameters);
    }

    public List<Long> getAllNotUpdatedStudents(Date date) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("lastImportDate", date);
        return genericDao.getNamedQueryEntityIds(Student.GET_ALL_NOT_UPDATED_STUDENTS, parameters);
    }

    public void delete(Student student) {
        genericDao.delete(student);
    }

    public void deleteByStudentIds(List<Long> studentIds) {
        Query query = genericDao.getNamedQuery(Student.DELETE_BY_STUDENT_IDS);
        query.setParameterList("studentIds", studentIds);
        query.executeUpdate();
    }
*/
}
