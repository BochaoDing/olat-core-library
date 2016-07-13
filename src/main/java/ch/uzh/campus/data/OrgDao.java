package ch.uzh.campus.data;

import ch.uzh.campus.utils.DateUtil;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

/**
 * Initial Date: 07.12.2012 <br>
 *
 * @author aabouc
 * @author Martin Schraner
 */
@Repository
public class OrgDao implements CampusDao<Org> {

    @Autowired
    private DB dbInstance;

    @Override
    public void save(List<Org> orgs) {
        for (Org org : orgs) {
            dbInstance.saveObject(org);
        }
    }

    @Override
    public void saveOrUpdate(List<Org> orgs) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        for (Org org : orgs) {
            em.merge(org);
        }
    }

    Org getOrgById(Long id) {
        return dbInstance.findObject(Org.class, id);
    }

    List<Long> getIdsOfAllEnabledOrgs() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Org.GET_IDS_OF_ALL_ENABLED_ORGS, Long.class)
                .getResultList();
    }

    List<Long> getAllOrphanedOrgs() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Org.GET_ALL_ORPHANED_ORGS, Long.class)
                .getResultList();
    }

    /**
     * Deletes also according entries of the join table ck_course_org.
     * We cannot use a bulk delete here, since deleting the join table is not possible.
     */
    void deleteByOrgId(Long orgId) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        deleteOrgBidirectionally(em.getReference(Org.class, orgId), em);
    }

    /**
     * Deletes also according entries of the join table ck_course_org.
     * We cannot use a bulk delete here, since deleting the join table is not possible.
     */
    void deleteByOrgIds(List<Long> orgIds) {
        int count = 0;
        EntityManager em = dbInstance.getCurrentEntityManager();
        for (Long orgId : orgIds) {
            deleteOrgBidirectionally(em.getReference(Org.class, orgId), em);
            // Avoid memory problems caused by loading too many objects into the persistence context
            // (cf. C. Bauer and G. King: Java Persistence mit Hibernate, 2nd edition, p. 477)
            if (++count % 100 == 0) {
                em.flush();
                em.clear();
            }
        }
    }

    private void deleteOrgBidirectionally(Org org, EntityManager em) {
        for (Course course : org.getCourses()) {
            course.getOrgs().remove(org);
        }
        // Use em.remove() instead of dbInstance.deleteObject() since the latter calls dbInstance.getCurrentEntityManager()
        // at every call, which may has an impact on the performance
        em.remove(org);
    }

}

