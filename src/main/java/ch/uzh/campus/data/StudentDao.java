package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */

@Repository
public class StudentDao implements CampusDao<Student> {  
	
	@Autowired
    private DB dbInstance;   

    @Override
    public void save(List<Student> students) {
    	for(Student student:students) {
    		dbInstance.saveObject(student);
    	}        
    }

    public void addStudentToCourse(Long studentId, Long courseId) {
        Student student = dbInstance.getCurrentEntityManager().getReference(Student.class, studentId);
        Course course = dbInstance.getCurrentEntityManager().getReference(Course.class, courseId);
        student.getCourses().add(course);
        course.getStudents().add(student);
        dbInstance.saveObject(course);
    }

    public void addStudentToCourse(StudentIdCourseId studentIdCourseId) {
        addStudentToCourse(studentIdCourseId.getStudentId(), studentIdCourseId.getCourseId());
    }

    public void addStudentsToCourse(List<StudentIdCourseId> studentIdCourseIds) {
        for (StudentIdCourseId studentIdCourseId : studentIdCourseIds) {
            addStudentToCourse(studentIdCourseId.getStudentId(), studentIdCourseId.getCourseId());
        }
    }

    public Student getStudentById(Long id) {
    	return dbInstance.findObject(Student.class, id);
    }

    public Student getStudentByEmail(String email) {        
    	List<Student> students = dbInstance.getCurrentEntityManager()
				.createNamedQuery(Student.GET_STUDENTS_BY_EMAIL, Student.class)
				.setParameter("email", email)
				.getResultList();
    	if (students != null && !students.isEmpty()) {
            return students.get(0);
        }
        return null;
    }    

    public Student getStudentByRegistrationNr(String registrationNr) {        
    	List<Student> students = dbInstance.getCurrentEntityManager()
				.createNamedQuery(Student.GET_STUDENTS_WITH_REGISTRATION_NUMBER, Student.class)
				.setParameter("registrationNr", registrationNr)
				.getResultList();
        if (students != null && !students.isEmpty()) {
            return students.get(0);
        }
        return null;
    }

    public List<Student> getAllStudents() {
        // return genericDao.findAll();
        return getAllPilotStudents();
    }
    
    public List<Long> getAllNotUpdatedStudents(Date date) {                
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Student.GET_ALL_NOT_UPDATED_STUDENTS, Long.class)
                .setParameter("lastImportDate", date)
                .getResultList();
    }
    
    public void delete(Student student) {
        // http://stackoverflow.com/questions/1082095/how-to-remove-entity-with-manytomany-relationship-in-jpa-and-corresponding-join
    	dbInstance.deleteObject(student);
        deleteJoinTableEntries(student);
    }

    public void deleteByStudentIds(List<Long> studentIds) {
        for (Long studentId : studentIds) {
            deleteJoinTableEntries(dbInstance.getCurrentEntityManager().getReference(Student.class, studentId));
        }
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

    private void deleteJoinTableEntries(Student student) {
        for (Course course : student.getCourses()) {
            course.getStudents().remove(student);
        }
    }

}
