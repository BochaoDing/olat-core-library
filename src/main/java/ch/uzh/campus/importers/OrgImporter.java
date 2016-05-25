package ch.uzh.campus.importers;

import ch.uzh.campus.data.*;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrgImporter extends Importer {

    @Autowired
    private OrgDao orgDao;

    @Autowired
    private DaoManager daoManager;

    @Value("#{'${campus.org.identifiers}'.split(',')}")
    private List<String> identifiers;

    private List<Org> orgs = new ArrayList<Org>();

    @Override
    void processEntry(String[] entry) {
        try {
            String shortName = entry[10];
            // check if entry should be processed
            if (!startsWithIdentifier(shortName)) {
                skipEntry(entry, Importer.SKIP_REASON_FILTERED_OUT);
                return;
            }
            // prepare Org object for persistence
            Org org = new Org();
            org.setId(Long.parseLong(entry[0]));
            org.setName(entry[12]);
            org.setShortName(shortName);
            org.setModifiedDate(new Date());
            orgs.add(org);
            if (orgs.size() % Importer.COMMIT_INTERVAL == 0) {
                persist();
            }
        } catch (Exception e) {
            System.out.println("Exception while processing Org entry: " + e.getMessage());
            cntFailed++;
        }
    }

    @Override
    int getEntryFieldCount() {
        return 22;
    }

    @Override
    void persist() {
        persistList(orgs, orgDao);
    }

    private boolean startsWithIdentifier(String shortName) throws Exception {
        for (String identifier : identifiers) {
            if (shortName != null && shortName.startsWith(identifier.trim())) {
                return true;
            }
        }
        LOG.warn(shortName + " is not found in identifiers " + identifiers.toString());
        return false;
    }

    // TODO find the right place to use. In CampusInterceptor old data is used in afterStep() - why?
    private void deleteOldRecords() {
        List<Long> orgsToBeRemoved = daoManager.getAllOrgsToBeDeleted(this.startTime);
        LOG.info("ORGS TO BE REMOVED [" + orgsToBeRemoved.size() + "]");
        if (!orgsToBeRemoved.isEmpty()) {
            daoManager.deleteOrgByIds(orgsToBeRemoved);
        }
    }
}
