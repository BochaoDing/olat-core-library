package ch.uzh.campus.data;


import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
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
        for (Lecturer lecturer : lecturers) {
            dbInstance.saveObject(lecturer);
        }
    }

    @Override
    public void saveOrUpdate(List<Lecturer> lecturers) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        for (Lecturer lecturer : lecturers) {
            em.merge(lecturer);
        }
    }

    Lecturer getLecturerById(Long id) {
        return dbInstance.findObject(Lecturer.class, id);
    }

    Lecturer getLecturerByEmail(String email) {
        List<Lecturer> lecturers = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Lecturer.GET_LECTURER_BY_EMAIL, Lecturer.class)
                .setParameter("email", email)
                .getResultList();
        if (lecturers != null && !lecturers.isEmpty()) {
            return lecturers.get(0);
        }
        return null;
    }

    List<Lecturer> getAllLecturersWithCreatedOrNotCreatedCreatableCourses() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Lecturer.GET_ALL_LECTURERS_WITH_CREATED_OR_NOT_CREATED_CREATABLE_COURSES, Lecturer.class)
                .getResultList();
    }

    List<Long> getAllOrphanedLecturers() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Lecturer.GET_ALL_ORPHANED_LECTURERS, Long.class)
                .getResultList();
    }

    /**
     * Deletes also according entries of the join table ck_lecturer_course.
     */
    void delete(Lecturer lecturer) {
        deleteLecturerBidirectionally(lecturer);
    }

    /**
     * Deletes also according entries of the join table ck_lecturer_course.
     */
    void deleteByLecturerIds(List<Long> lecturerIds) {
        for (Long lecturerId : lecturerIds) {
            deleteLecturerBidirectionally(dbInstance.getCurrentEntityManager().getReference(Lecturer.class, lecturerId));
        }
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries.
     * Does not delete according entries of join table ck_lecturer_course (-> must be deleted explicitly)!
     * Does not update persistence context!
     */
    int deleteByLecturerIdsAsBulkDelete(List<Long> lecturerIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Lecturer.DELETE_BY_LECTURER_IDS)
                .setParameter("lecturerIds", lecturerIds)
                .executeUpdate();
    }

    private void deleteLecturerBidirectionally(Lecturer lecturer) {
        for (LecturerCourse lecturerCourse : lecturer.getLecturerCourses()) {
            lecturerCourse.getCourse().getLecturerCourses().remove(lecturerCourse);
            dbInstance.deleteObject(lecturerCourse);
        }
        dbInstance.deleteObject(lecturer);
    }

}
