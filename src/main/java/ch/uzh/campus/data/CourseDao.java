package ch.uzh.campus.data;

import ch.uzh.campus.utils.DateUtil;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
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

    @Override
    public void saveOrUpdate(List<Course> courses) {
        EntityManager em = dbInstance.getCurrentEntityManager();
    	for (Course course : courses) {
            em.merge(course);
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
        deleteCourseBidirectionally(course);
    }

    /**
     * Bulk update for efficient update of a big number of entries. Does not update persistence context!
     */
    public void saveResourceableIdAsBulkUpdate(Long courseId, Long resourceableId) {
        dbInstance.getCurrentEntityManager().createNamedQuery(Course.SAVE_RESOURCEABLE_ID)
				.setParameter("courseId", courseId)
				.setParameter("resId", resourceableId)
				.executeUpdate();
    }

    /**
     * Bulk update for efficient update of a big number of entries. Does not update persistence context!
     */
    public void disableSynchronizationAsBulkUpdate(Long courseId) {
        dbInstance.getCurrentEntityManager().createNamedQuery(Course.DISABLE_SYNCHRONIZATION)
		        .setParameter("courseId", courseId)
		        .executeUpdate();
    }

    /**
     * Bulk update for efficient update of a big number of entries. Does not update persistence context!
     */
    public void deleteResourceableIdAsBulkUpdate(Long resourceableId) {
        dbInstance.getCurrentEntityManager().createNamedQuery(Course.DELETE_RESOURCEABLE_ID)
                .setParameter("resId", resourceableId)
		        .executeUpdate();
    }

    /**
     * Deletes also according entries of the join tables ck_lecturer_course and ck_student_course and of the related tables ck_text and ck_event.
     */
    public void deleteByCourseId(Long courseId) {
        deleteCourseBidirectionally(dbInstance.getCurrentEntityManager().getReference(Course.class, courseId));
    }

    /**
     * Deletes also according entries of the join tables ck_lecturer_course and ck_student_course and of the related tables ck_text and ck_event.
     */
    public void deleteByCourseIds(List<Long> courseIds) {
        for (Long courseId : courseIds) {
            deleteCourseBidirectionally(dbInstance.getCurrentEntityManager().getReference(Course.class, courseId));
        }
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries.
     *
     * Does not delete according entries of join tables ck_lecturer_course and ck_student_course (-> must be deleted explicitly)!
     * Does not delete according entries of ck_text and ck_event (-> must be deleted explicitly)!
     * Does not update persistence context!
     */
    public int deleteByCourseIdsAsBulkDelete(List<Long> courseIds) {
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
        // Subtract one second from date since modifiedDate (used in query) is rounded to seconds
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_ALL_NOT_UPDATED_COURSES, Long.class)  
                .setParameter("lastImportDate", DateUtil.addSecondsToDate(date, -1))
                .getResultList();
    }

    public boolean existResourceableId(Long resourceableId) {
        List<Long> courseIds = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_COURSE_IDS_BY_RESOURCEABLE_ID, Long.class)
                .setParameter("resourceableId", resourceableId)
                .getResultList();
        return !courseIds.isEmpty();
    }

    public List<Course> getPilotCoursesByLecturerId(Long lecturerId) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_PILOT_COURSES_BY_LECTURER_ID, Course.class)
                .setParameter("lecturerId", lecturerId)
                .getResultList();
    }

    public List<Course> getPilotCoursesByStudentId(Long studentId) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_PILOT_COURSES_BY_STUDENT_ID, Course.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }

    private void deleteCourseBidirectionally(Course course) {
        // Delete join table entries
        for (LecturerCourse lecturerCourse : course.getLecturerCourses()) {
            lecturerCourse.getLecturer().getLecturerCourses().remove(lecturerCourse);
            dbInstance.deleteObject(lecturerCourse);
        }
        for (StudentCourse studentCourse : course.getStudentCourses()) {
            studentCourse.getStudent().getStudentCourses().remove(studentCourse);
            dbInstance.deleteObject(studentCourse);
        }
        dbInstance.deleteObject(course);
    }
}
