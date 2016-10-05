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
public class CampusCourseCoOwners {
    
	private static final OLog log = Tracing.createLoggerFor(CampusCourseCoOwners.class);

    private static final String DELIMITER = ",";

    @Autowired
    public CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    public BaseSecurity baseSecurity;

    public void setCampusCourseConfiguration(CampusCourseConfiguration campusCourseConfiguration) {
        this.campusCourseConfiguration = campusCourseConfiguration;
    }

    private List<Identity> identites;

    List<Identity> getDefaultCoOwners() {
        if (identites != null) {
            return identites;
        } else {
            identites = initCoOwnerIdentities();
            return identites;
        }

    }

    private List<Identity> initCoOwnerIdentities() {
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
                log.warn("getDefaultCoOwners: Could not found an OLAT identity for username:'" + identityName + "' , check Campuskurs configuration-value:'"
                        + defaultCoOwnerUserNamesPropertyValue + "'");
            }
        }
        return identites;
    }

}

