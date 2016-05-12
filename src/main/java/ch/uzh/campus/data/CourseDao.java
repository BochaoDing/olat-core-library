package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    public void addLecturerById(Long lecturerId, Long courseId) {
        Lecturer lecturer = dbInstance.getCurrentEntityManager().getReference(Lecturer.class, lecturerId);
        Course course = dbInstance.getCurrentEntityManager().getReference(Course.class, courseId);
        lecturer.getCourses().add(course);
        course.getLecturers().add(lecturer);
        dbInstance.saveObject(course);
    }

    public void addLecturerById(LecturerIdCourseId lecturerIdCourseId) {
        addLecturerById(lecturerIdCourseId.getLecturerId(), lecturerIdCourseId.getCourseId());
    }

    public void addLecturersById(List<LecturerIdCourseId> lecturerIdCourseIds) {
        for (LecturerIdCourseId lecturerIdCourseId : lecturerIdCourseIds) {
            addLecturerById(lecturerIdCourseId.getLecturerId(), lecturerIdCourseId.getCourseId());
        }
    }

    public List<Course> getPilotCoursesByLecturerId(Long lecturerId) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_PILOT_COURSES_BY_LECTURER_ID, Course.class)
                .setParameter("lecturerId", lecturerId)
                .getResultList();
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

    /**
     * TODO: check if used, if not used remove it
     * @param studentId
     * @return
     */
    public List<Course> getPilotCoursesByStudentId(Long studentId) {               
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_PILOT_COURSES_BY_STUDENT_ID, Course.class)
                .setParameter("studentId", studentId)
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

//    public void deleteResourceableId(Long resourceableId) {
//        Query query = genericDao.getNamedQuery(Course.DELETE_RESOURCEABLE_ID);
//        query.setParameter("resId", resourceableId);
//        query.executeUpdate();
//    }
//
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
//    public List<Long> getIdsOfAllCreatedCourses() {
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        return genericDao.getNamedQueryEntityIds(Course.GET_IDS_OF_ALL_CREATED_COURSES, parameters);
//    }
//
//    public List<Long> getResourceableIdsOfAllCreatedCourses() {
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        return genericDao.getNamedQueryEntityIds(Course.GET_RESOURCEABLEIDS_OF_ALL_CREATED_COURSES, parameters);
//    }
//
//    public List<Long> getIdsOfAllNotCreatedCourses() {
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        return genericDao.getNamedQueryEntityIds(Course.GET_IDS_OF_ALL_NOT_CREATED_COURSES, parameters);
//    }
//
    public List<Course> getAllCreatedCourses() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_ALL_CREATED_COURSES, Course.class)
                .getResultList();
    }
//
//    public List<Long> getAllNotUpdatedCourses(Date date) {
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        parameters.put("lastImportDate", date);
//        return genericDao.getNamedQueryEntityIds(Course.GET_ALL_NOT_UPDATED_COURSES, parameters);
//    }
//
//    public boolean existResourceableId(Long resourceableId) {
//        Map<String, Object> restrictionMap = new HashMap<String, Object>();
//        restrictionMap.put("resourceableId", resourceableId);
//        List<Course> courses = genericDao.findByCriteria(restrictionMap);
//
//        if (!courses.isEmpty()) {
//            return true;
//        }
//        return false;
//    }

}
