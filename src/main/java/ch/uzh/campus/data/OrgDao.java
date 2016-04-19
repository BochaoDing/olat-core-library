package ch.uzh.campus.data;

import java.util.ArrayList;
/**
 * Created by Martin Schraner on 15.04.16.
 */
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.hibernate.Query;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ch.uzh.campus.connectors.CampusProcess;

/**
 * Initial Date: 07.12.2012 <br>
 *
 * @author aabouc
 */
@Repository
public class OrgDao implements CampusDao<Org> {
	
	private static final OLog LOG = Tracing.createLoggerFor(CampusProcess.class);

    @Autowired
    private DB dbInstance;

    @PostConstruct
    void initType() {
    	LOG.info("TODO: implement - @PostConstruct - OrgDao");
        //genericDao.setType(Org.class);
    }

    // TODO do we also need update?
    public void save(List<Org> orgs) {
        for (Org org : orgs) {
            dbInstance.saveObject(org);
        }
    }

    public List<Long> getIdsOfAllEnabledOrgs() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        //return genericDao.getNamedQueryEntityIds(Org.GET_IDS_OF_ALL_ENABLED_ORGS, parameters);
        LOG.info("TODO: implement - getIdsOfAllEnabledOrgs - OrgDao");
        return new ArrayList<Long>();
    }

    public List<Long> getAllNotUpdatedOrgs(Date date) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("lastImportDate", date);
        //return genericDao.getNamedQueryEntityIds(Org.GET_ALL_NOT_UPDATED_ORGS, parameters);
        LOG.info("TODO: implement - getAllNotUpdatedOrgs - OrgDao");
        return new ArrayList<Long>();
    }

    public int deleteByOrgIds(List<Long> orgIds) {
        //Query query = genericDao.getNamedQuery(Org.DELETE_BY_ORG_IDS);
        //query.setParameterList("orgIds", orgIds);
        //return query.executeUpdate();
    	LOG.info("TODO: implement - deleteByOrgIds - OrgDao");
        return 0;
    }

}

