package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Calendar;
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

    public List<Long> getIdsOfAllEnabledOrgs() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Org.GET_IDS_OF_ALL_ENABLED_ORGS, Long.class)
                .getResultList();
    }

    public List<Long> getAllNotUpdatedOrgs(Date date) {
        // Subtract one second since modifiedDate (used in query) is rounded to seconds
        Calendar dateMinusOneSecond = Calendar.getInstance();
        dateMinusOneSecond.setTime(date);
        dateMinusOneSecond.add(Calendar.SECOND, -1);
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Org.GET_ALL_NOT_UPDATED_ORGS, Long.class)
                .setParameter("lastImportDate", dateMinusOneSecond.getTime())
                .getResultList();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteByOrgIdsAsBulkDelete(List<Long> orgIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Org.DELETE_BY_ORG_IDS)
                .setParameter("orgIds", orgIds)
                .executeUpdate();
    }

}

