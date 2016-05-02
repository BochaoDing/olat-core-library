package ch.uzh.campus.data;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.hibernate.Query;
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
public class StudentCourseDao implements CampusDao<StudentCourse> {

	@Autowired
	private DB dbInstance;
	
    @Override
    public void save(List<StudentCourse> studentCourses) {
      for (StudentCourse studentCourse : studentCourses) {
	     dbInstance.saveObject(studentCourse);
	  }
    }

    /*
    public List<StudentCourse> getAllSudentCourses() {
        return genericDao.findAll();
    }

    public void delete(StudentCourse studentCourse) {
        genericDao.delete(studentCourse);
    }

    public void deleteByCourseId(Long courseId) {
        Query query = genericDao.getNamedQuery(StudentCourse.DELETE_STUDENT_BY_COURSE_ID);
        query.setParameter("courseId", courseId);
        query.executeUpdate();
    }

    public void deleteByCourseIds(List<Long> courseIds) {
        Query query = genericDao.getNamedQuery(StudentCourse.DELETE_STUDENTS_BY_COURSE_IDS);
        query.setParameterList("courseIds", courseIds);
        query.executeUpdate();
    }

    public void deleteByStudentId(Long studentId) {
        Query query = genericDao.getNamedQuery(StudentCourse.DELETE_STUDENT_BY_STUDENT_ID);
        query.setParameter("studentId", studentId);
        query.executeUpdate();
    }

    public void deleteByStudentIds(List<Long> studentIds) {
        Query query = genericDao.getNamedQuery(StudentCourse.DELETE_STUDENTS_BY_STUDENT_IDS);
        query.setParameterList("studentIds", studentIds);
        query.executeUpdate();
    }

    public int deleteAllNotUpdatedSCBooking(Date date) {
        Query query = genericDao.getNamedQuery(StudentCourse.DELETE_ALL_NOT_UPDATED_SC_BOOKING);
        query.setParameter("lastImportDate", date);
        return query.executeUpdate();
    }
*/
}

