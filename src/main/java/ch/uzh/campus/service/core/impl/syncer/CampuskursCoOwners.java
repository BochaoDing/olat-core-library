package ch.uzh.campus.service.core.impl.syncer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.uzh.campus.CampusConfiguration;

/**
 * Initial Date: 12.07.2012 <br>
 * 
 * @author cg
 */
@Component
public class CampuskursCoOwners {
    //private static final Logger log = LoggerHelper.getLogger();
	private static final OLog log = Tracing.createLoggerFor(CampuskursCoOwners.class);

    private static final String DELIMITER = ",";

    @Autowired
    CampusConfiguration campusConfiguration;
    @Autowired
    BaseSecurity baseSecurity;

    private List<Identity> identites;

    public List<Identity> getDefaultCoOwners() {
        if (identites != null) {
            return identites;
        } else {
            identites = initCoOwnerIdentities();
            return identites;
        }

    }

    private List<Identity> initCoOwnerIdentities() {
        String defaultCoOwnerUserNamesPropertyValue = campusConfiguration.getDefaultCoOwnerUserNames();
        List<Identity> identites = new ArrayList<Identity>();
        StringTokenizer tok = new StringTokenizer(defaultCoOwnerUserNamesPropertyValue, DELIMITER);
        while (tok.hasMoreTokens()) {
            String identityName = tok.nextToken();
            Identity identity = baseSecurity.findIdentityByName(identityName);
            if (identity != null) {
                if (!identites.contains(identity)) {
                    identites.add(identity);
                }
            } else {
                log.warn("getDefaultCoOwners: Could not found an OLAT identity for username:'" + identityName + "' , check Campuskurs configuration-value:'"
                        + defaultCoOwnerUserNamesPropertyValue + "'");
            }
        }
        return identites;
    }

}

