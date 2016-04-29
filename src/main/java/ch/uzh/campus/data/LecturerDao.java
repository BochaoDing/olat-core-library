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
        dbInstance.deleteObject(lecturer);
    }

    public int deleteByLecturerIds(List<Long> lecturerIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Lecturer.DELETE_BY_LECTURER_IDS)
                .setParameter("lecturerIds", lecturerIds)
                .executeUpdate();
    }
}
