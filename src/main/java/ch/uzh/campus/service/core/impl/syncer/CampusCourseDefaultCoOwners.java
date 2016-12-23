package ch.uzh.campus.service.core.impl.syncer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.uzh.campus.CampusCourseConfiguration;

/**
 * Initial Date: 12.07.2012 <br>
 * 
 * @author cg
 */
@Component
public class CampusCourseDefaultCoOwners {
    
	private static final OLog LOG = Tracing.createLoggerFor(CampusCourseDefaultCoOwners.class);
    private static final String DELIMITER = ",";

    private final CampusCourseConfiguration campusCourseConfiguration;
    private final BaseSecurity baseSecurity;

    private List<Identity> defaultCoOnwerIdentites;

    @Autowired
    public CampusCourseDefaultCoOwners(CampusCourseConfiguration campusCourseConfiguration, BaseSecurity baseSecurity) {
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.baseSecurity = baseSecurity;
    }

    public List<Identity> getDefaultCoOwners() {
        if (defaultCoOnwerIdentites != null) {
            return defaultCoOnwerIdentites;
        } else {
            defaultCoOnwerIdentites = initDefaultCoOwnerIdentities();
            return defaultCoOnwerIdentites;
        }
    }

    private List<Identity> initDefaultCoOwnerIdentities() {
        String defaultCoOwnerUserNamesPropertyValue = campusCourseConfiguration.getDefaultCoOwnerUserNames();
        List<Identity> identites = new ArrayList<>();
        StringTokenizer tok = new StringTokenizer(defaultCoOwnerUserNamesPropertyValue, DELIMITER);
        while (tok.hasMoreTokens()) {
            String identityName = tok.nextToken();
            Identity identity = baseSecurity.findIdentityByName(identityName);
            if (identity != null) {
                if (!identites.contains(identity)) {
                    identites.add(identity);
                }
            } else {
                LOG.warn("getDefaultCoOwners: Could not found an OLAT identity for username:'" + identityName + "' , check Campuskurs configuration-value:'"
                        + defaultCoOwnerUserNamesPropertyValue + "'");
            }
        }
        return identites;
    }

}

