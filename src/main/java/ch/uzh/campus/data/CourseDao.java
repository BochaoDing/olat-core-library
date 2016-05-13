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
public class CourseDao implements CampusDao<Course> {

	@Autowired
	private DB dbInstance;

    @Override
    public void save(List<Course> courses) {
    	for (Course course : courses) {
            dbInstance.saveObject(course);
        }
    }

    public Course getCourseById(Long id) {
        return dbInstance.findObject(Course.class, id);
    }

    public List<Course> getCreatedCoursesByLecturerIds(List<Long> lecturerIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_COURSES_BY_LECTURER_IDS, Course.class)
                .setParameter("lecturerIds", lecturerIds)
                .getResultList();
    }

    public List<Course> getNotCreatedCoursesByLecturerIds(List<Long> lecturerIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_NOT_CREATED_COURSES_BY_LECTURER_IDS, Course.class)
                .setParameter("lecturerIds", lecturerIds)
                .getResultList();
    }

    public List<Course> getCreatedCoursesByStudentId(Long studentId) {               
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_COURSES_BY_STUDENT_ID, Course.class)
                .setParameter("studentId", studentId)
                .getResultList(); 
    }

    public List<Course> getNotCreatedCoursesByStudentId(Long studentId) {       
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_NOT_CREATED_COURSES_BY_STUDENT_ID, Course.class)
                .setParameter("studentId", studentId)
                .getResultList(); 
    }

    public void delete(Course course) {
        dbInstance.deleteObject(course);
    }

    public void saveResourceableId(Long courseId, Long resourceableId) {  
        dbInstance.getCurrentEntityManager().createNamedQuery(Course.SAVE_RESOURCEABLE_ID)
				.setParameter("courseId", courseId)
				.setParameter("resId", resourceableId)
				.executeUpdate();
    }

    public void disableSynchronization(Long courseId) {               
        dbInstance.getCurrentEntityManager().createNamedQuery(Course.DISABLE_SYNCHRONIZATION)
		        .setParameter("courseId", courseId)
		        .executeUpdate();
    }

    public void deleteResourceableId(Long resourceableId) {                
        dbInstance.getCurrentEntityManager().createNamedQuery(Course.DELETE_RESOURCEABLE_ID)
                .setParameter("resId", resourceableId)
		        .executeUpdate();
    }

    public int deleteByCourseId(Long courseId) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.DELETE_BY_COURSE_ID)
                .setParameter("courseId", courseId)
                .executeUpdate();
    }

    public int deleteByCourseIds(List<Long> courseIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.DELETE_BY_COURSE_IDS)
                .setParameter("courseIds", courseIds)
                .executeUpdate();
    }
    
    public List<Long> getIdsOfAllCreatedCourses() {               
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_IDS_OF_ALL_CREATED_COURSES, Long.class)                			
                .getResultList();
    }

    public List<Long> getResourceableIdsOfAllCreatedCourses() {  
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_RESOURCEABLEIDS_OF_ALL_CREATED_COURSES, Long.class)                			
                .getResultList();
    }

    public List<Long> getIdsOfAllNotCreatedCourses() {     
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_IDS_OF_ALL_NOT_CREATED_COURSES, Long.class)                			
                .getResultList();
    }

    public List<Course> getAllCreatedCourses() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_ALL_CREATED_COURSES, Course.class)
                .getResultList();
    }

    public List<Long> getAllNotUpdatedCourses(Date date) {                
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_ALL_NOT_UPDATED_COURSES, Long.class)  
                .setParameter("lastImportDate", date)
                .getResultList();
    }

    public boolean existResourceableId(Long resourceableId) {
        List<Long> courseIds = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_COURSE_IDS_BY_RESOURCEABLE_ID, Long.class)
                .setParameter("resourceableId", resourceableId)
                .getResultList();
        return !courseIds.isEmpty();
    }

}
