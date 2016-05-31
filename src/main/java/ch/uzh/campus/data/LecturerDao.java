package ch.uzh.campus.data;


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
 * @author Martin Schraner
 */
@Repository
public class LecturerDao implements CampusDao<Lecturer> {

    @Autowired
    private DB dbInstance;

    @Override
    public void save(List<Lecturer> lecturers) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        for (Lecturer lecturer : lecturers) {
            em.merge(lecturer);
        }
    }

    public void addLecturerToCourse(Long lecturerId, Long courseId) {
        Lecturer lecturer = dbInstance.getCurrentEntityManager().getReference(Lecturer.class, lecturerId);
        Course course = dbInstance.getCurrentEntityManager().getReference(Course.class, courseId);
        lecturer.getCourses().add(course);
        course.getLecturers().add(lecturer);
    }

    public void addLecturerToCourse(LecturerIdCourseId lecturerIdCourseId) {
        addLecturerToCourse(lecturerIdCourseId.getLecturerId(), lecturerIdCourseId.getCourseId());
    }

    public void addLecturersToCourse(List<LecturerIdCourseId> lecturerIdCourseIds) {
        for (LecturerIdCourseId lecturerIdCourseId : lecturerIdCourseIds) {
            addLecturerToCourse(lecturerIdCourseId.getLecturerId(), lecturerIdCourseId.getCourseId());
        }
    }

    public Lecturer getLecturerById(Long id) {
        return dbInstance.findObject(Lecturer.class, id);
    }

    public Lecturer getLecturerByEmail(String email) {
        List<Lecturer> lecturers = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Lecturer.GET_LECTURER_BY_EMAIL, Lecturer.class)
                .setParameter("email", email)
                .getResultList();
        if (lecturers != null && !lecturers.isEmpty()) {
            return lecturers.get(0);
        }
        return null;
    }

    public List<Lecturer> getAllLecturers() {
        // return genericDao.findAll();
        return getAllPilotLecturers();
    }

    public List<Lecturer> getAllPilotLecturers() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Lecturer.GET_ALL_PILOT_LECTURERS, Lecturer.class)
                .getResultList();
    }

    public List<Long> getAllNotUpdatedLecturers(Date date) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Lecturer.GET_ALL_NOT_UPDATED_LECTURERS, Long.class)
                .setParameter("lastImportDate", date)
                .getResultList();
    }

    public void delete(Lecturer lecturer) {
        deleteLecturerBidirectionally(lecturer);
    }

    public void deleteByLecturerIds(List<Long> lecturerIds) {
        for (Long lecturerId : lecturerIds) {
            deleteLecturerBidirectionally(dbInstance.getCurrentEntityManager().getReference(Lecturer.class, lecturerId));
        }
    }

    private void deleteLecturerBidirectionally(Lecturer lecturer) {
        for (Course course : lecturer.getCourses()) {
            course.getLecturers().remove(lecturer);
        }
        dbInstance.deleteObject(lecturer);
    }

}
