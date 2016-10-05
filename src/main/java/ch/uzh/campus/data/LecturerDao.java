package ch.uzh.campus.data;


import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.utils.DateUtil;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
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

    private static final OLog LOG = Tracing.createLoggerFor(LecturerDao.class);

    private final CampusCourseConfiguration campusCourseConfiguration;
    private final DB dbInstance;

    @Autowired
    public LecturerDao(CampusCourseConfiguration campusCourseConfiguration, DB dbInstance) {
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.dbInstance = dbInstance;
    }

    public void save(Lecturer lecturer) {
        dbInstance.saveObject(lecturer);
    }

    public void saveOrUpdate(Lecturer lecturer) {
        /*
		 * A database merge with a detached course entity would override the
		 * values of the mapping attributes with "null".
		 */
        Lecturer lecturerFound = getLecturerById(lecturer.getPersonalNr());
        if (lecturerFound == null) {
            dbInstance.saveObject(lecturer);
            return;
        }
        lecturer.mergeAllExceptMappingAttributes(lecturerFound);
    }

    @Override
    public void save(List<Lecturer> lecturers) {
        for (Lecturer lecturer : lecturers) {
            save(lecturer);
        }
    }

    @Override
    public void saveOrUpdate(List<Lecturer> lecturers) {
        for (Lecturer lecturer : lecturers) {
            saveOrUpdate(lecturer);
        }
    }

    public void addMapping(Long lecturerId, Identity identity) {
        Lecturer lecturer = getLecturerById(lecturerId);
        if (lecturer != null) {
            lecturer.setMappedIdentity(identity);
            lecturer.setKindOfMapping("AUTO");
            lecturer.setDateOfMapping(new Date());
        } else {
            LOG.warn("No lecturer found with id " + lecturerId + " for table ck_lecturer.");
        }

    }

    public void removeMapping(Long lecturerId) {
        Lecturer lecturer = getLecturerById(lecturerId);
        if (lecturer != null) {
            lecturer.setMappedIdentity(null);
            lecturer.setKindOfMapping(null);
            lecturer.setDateOfMapping(null);
        } else {
            LOG.warn("No lecturer found with id " + lecturerId + " for table ck_lecturer.");
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

    List<Long> getAllNotManuallyMappedOrTooOldOrphanedLecturers(Date date) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Lecturer.GET_ALL_NOT_MANUALLY_MAPPED_OR_TOO_OLD_ORPHANED_LECTURERS, Long.class)
                .setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
                .getResultList();
    }

    public List<Lecturer> getLecturersByMappedIdentityKey(Long mappedIdentityKey) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Lecturer.GET_LECTURERS_BY_MAPPED_IDENTITY_KEY, Lecturer.class)
                .setParameter("mappedIdentityKey", mappedIdentityKey)
                .getResultList();
    }

    /**
     * Deletes also according entries of the join table ck_lecturer_course.
     */
    void delete(Lecturer lecturer) {
        deleteLecturerBidirectionally(lecturer, dbInstance.getCurrentEntityManager());
    }

    /**
     * Deletes also according entries of the join table ck_lecturer_course.
     */
    void deleteByLecturerIds(List<Long> lecturerIds) {
        int count = 0;
        EntityManager em = dbInstance.getCurrentEntityManager();
        for (Long lecturerId : lecturerIds) {
            deleteLecturerBidirectionally(em.getReference(Lecturer.class, lecturerId), em);
            // Avoid memory problems caused by loading too many objects into the persistence context
            // (cf. C. Bauer and G. King: Java Persistence mit Hibernate, 2nd edition, p. 477)
            if (++count % 100 == 0) {
                em.flush();
                em.clear();
            }
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

    private void deleteLecturerBidirectionally(Lecturer lecturer, EntityManager em) {
        for (LecturerCourse lecturerCourse : lecturer.getLecturerCourses()) {
            lecturerCourse.getCourse().getLecturerCourses().remove(lecturerCourse);
            // Use em.remove() instead of dbInstance.deleteObject() since the latter calls dbInstance.getCurrentEntityManager()
            // at every call, which may has an impact on the performance
            em.remove(lecturerCourse);
        }
        em.remove(lecturer);
    }

}
