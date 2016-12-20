package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.data.entity.Delegation;
import ch.uzh.extension.campuscourse.data.entity.DelegatorDelegateeId;
import org.olat.basesecurity.IdentityImpl;
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
 * Initial Date: 03.05.2013 <br>
 * 
 * @author aabouc
 * @author Martin Schraner
 */
@Repository
public class DelegationDao implements CampusDao<Delegation> {

    private static final OLog LOG = Tracing.createLoggerFor(DelegationDao.class);

    private final DB dbInstance;

    @Autowired
    public DelegationDao(DB dbInstance) {
        this.dbInstance = dbInstance;
    }

    public void save(List<Delegation> delegations) {
        delegations.forEach(dbInstance::saveObject);
    }

    @Override
    public void saveOrUpdate(List<Delegation> delegations) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        delegations.forEach(em::merge);
    }

    public void save(Delegation delegation) {
        dbInstance.saveObject(delegation);
    }

    public void save(Long delegatorKey, Long delegateeKey) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        Identity delegator = em.find(IdentityImpl.class, delegatorKey);
        Identity delegatee = em.find(IdentityImpl.class, delegateeKey);
        if (delegator == null) {
            LOG.warn("No delegator found with id " + delegatorKey + ". Cannot save delegation.");
            return;
        }
        if (delegatee == null) {
            LOG.warn("No delegatee found with id " + delegateeKey + ". Cannot save delegation.");
            return;
        }
        Delegation delegation = new Delegation(delegator, delegatee, new Date());
        save(delegation);
    }

    public Delegation getDelegationById(Long delegatorKey, Long delegateeKey) {
        return dbInstance.getCurrentEntityManager().find(Delegation.class, new DelegatorDelegateeId(delegatorKey, delegateeKey));
    }

    public boolean existsDelegation(Long delegatorId, Long delegateeId) {
        return getDelegationById(delegatorId, delegateeId) != null;
    }

    public List<Delegation> getDelegationsByDelegator(Long delegatorKey) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Delegation.GET_BY_DELEGATOR, Delegation.class)
                .setParameter("delegatorKey", delegatorKey)
                .getResultList();
    }

    public List<Delegation> getDelegationsByDelegatee(Long delegateeKey) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Delegation.GET_BY_DELEGATEE, Delegation.class)
                .setParameter("delegateeKey", delegateeKey)
                .getResultList();
    }

    public void delete(Delegation delegation) {
        dbInstance.deleteObject(delegation);
    }

    public void deleteDelegationById(Long delegatorKey, Long delegateeKey) {
        Delegation delegationToBeDeleted = getDelegationById(delegatorKey, delegateeKey);
        if (delegationToBeDeleted != null) {
            dbInstance.deleteObject(delegationToBeDeleted);
        } else {
            LOG.warn("Cannot delete delegation: No delegation with delegator key " + delegatorKey + " and delegatee key " + delegateeKey);
        }
    }

}
