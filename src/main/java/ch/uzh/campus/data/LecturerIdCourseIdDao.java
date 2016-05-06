package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Initial Date: 04.06.2012 <br>
 *
 * @author aabouc
 * @author Martin Schraner
 */
public class LecturerIdCourseIdDao implements CampusDao<LecturerIdCourseId> {

    @Autowired
    private DB dbInstance;

    @Autowired
    private CourseDao courseDao;

    // Only necessary for campusBatchJob!
    @Override
    //TODO: call CourseDao.addLecturerById()
    public void save(List<LecturerIdCourseId> lecturerIdCourseIds) {

    }

//    public List<LecturerCourse> getAllLecturerCourses() {
//        return dbInstance.getCurrentEntityManager()
//                .createNamedQuery(LecturerCourse.GET_ALL_LECTURER_COURSES, LecturerCourse.class)
//                .getResultList();
//    }
//
//    public void delete(LecturerCourse lecturerCourse) {
//        dbInstance.deleteObject(lecturerCourse);
//    }
//
//    public int deleteByCourseId(Long courseId) {
//        return dbInstance.getCurrentEntityManager()
//                .createNamedQuery(LecturerCourse.DELETE_LECTURER_COURSE_BY_COURSE_ID)
//                .setParameter("courseId", courseId)
//                .executeUpdate();
//    }

//    public int deleteByCourseIds(List<Long> courseIds) {
//        return dbInstance.getCurrentEntityManager()
//                .createNamedQuery(LecturerCourse.DELETE_LECTURER_BY_LECTURER_ID)
//                .setParameter("courseIds", courseIds)
//                .executeUpdate();
//    }

//    public void deleteByLecturerId(Long lecturerId) {
//        Query query = genericDao.getNamedQuery(LecturerCourse.DELETE_LECTURER_BY_LECTURER_ID);
//        query.setParameter("lecturerId", lecturerId);
//        query.executeUpdate();
//    }
//
//    public void deleteByLecturerIds(List<Long> lecturerIds) {
//        Query query = genericDao.getNamedQuery(LecturerCourse.DELETE_LECTURERS_BY_LECTURER_IDS);
//        query.setParameterList("lecturerIds", lecturerIds);
//        query.executeUpdate();
//    }
//
//    public int deleteAllNotUpdatedLCBooking(Date date) {
//        Query query = genericDao.getNamedQuery(LecturerCourse.DELETE_ALL_NOT_UPDATED_LC_BOOKING);
//        query.setParameter("lastImportDate", date);
//        return query.executeUpdate();
//    }


}
