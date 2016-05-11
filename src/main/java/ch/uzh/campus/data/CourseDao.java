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

//    public Course updateCourse(Course course) {
//        return genericDao.update(course);
//    }
//


//    public List<Course> getPilotCoursesByLecturerId(Long lecturerId) {
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        parameters.put("lecturerId", lecturerId);
//        return genericDao.getNamedQueryListResult(Course.GET_PILOT_COURSES_BY_LECTURER_ID, parameters);
//    }
//
//    public List<Course> getCreatedCoursesByLecturerIds(List<Long> lecturerIds) {
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        parameters.put("lecturerIds", lecturerIds);
//        return genericDao.getNamedQueryListResult(Course.GET_CREATED_COURSES_BY_LECTURER_IDS, parameters);
//    }
//
//    public List<Course> getNotCreatedCoursesByLecturerIds(List<Long> lecturerIds) {
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        parameters.put("lecturerIds", lecturerIds);
//        return genericDao.getNamedQueryListResult(Course.GET_NOT_CREATED_COURSES_BY_LECTURER_IDS, parameters);
//    }
//
//    public List<Course> getPilotCoursesByStudentId(Long studentId) {
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        parameters.put("studentId", studentId);
//        return genericDao.getNamedQueryListResult(Course.GET_PILOT_COURSES_BY_STUDENT_ID, parameters);
//    }
//
//    public List<Course> getCreatedCoursesByStudentId(Long studentId) {
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        parameters.put("studentId", studentId);
//        return genericDao.getNamedQueryListResult(Course.GET_CREATED_COURSES_BY_STUDENT_ID, parameters);
//    }
//
//    public List<Course> getNotCreatedCoursesByStudentId(Long studentId) {
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        parameters.put("studentId", studentId);
//        return genericDao.getNamedQueryListResult(Course.GET_NOT_CREATED_COURSES_BY_STUDENT_ID, parameters);
//    }
//
//    public void delete(Course course) {
//        genericDao.delete(course);
//    }
//
//    public void saveResourceableId(Long courseId, Long resourceableId) {
//        Query query = genericDao.getNamedQuery(Course.SAVE_RESOURCEABLE_ID);
//        query.setParameter("courseId", courseId);
//        query.setParameter("resId", resourceableId);
//        query.executeUpdate();
//    }
//
//    public void disableSynchronization(Long courseId) {
//        Query query = genericDao.getNamedQuery(Course.DISABLE_SYNCHRONIZATION);
//        query.setParameter("courseId", courseId);
//        query.executeUpdate();
//    }
//
//    public void deleteResourceableId(Long resourceableId) {
//        Query query = genericDao.getNamedQuery(Course.DELETE_RESOURCEABLE_ID);
//        query.setParameter("resId", resourceableId);
//        query.executeUpdate();
//    }
//
//    public void deleteByCourseId(Long courseId) {
//        Query query = genericDao.getNamedQuery(Course.DELETE_BY_COURSE_ID);
//        query.setParameter("courseId", courseId);
//        query.executeUpdate();
//    }
//
//    public void deleteByCourseIds(List<Long> courseIds) {
//        Query query = genericDao.getNamedQuery(Course.DELETE_BY_COURSE_IDS);
//        query.setParameterList("courseIds", courseIds);
//        query.executeUpdate();
//    }
//
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
//    public List<Course> getAllCreatedCourses() {
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        return genericDao.getNamedQueryListResult(Course.GET_ALL_CREATED_COURSES, parameters);
//    }
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
