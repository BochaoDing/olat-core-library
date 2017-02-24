package ch.uzh.extension.campuscourse.service.delegation;

import ch.uzh.extension.campuscourse.data.dao.DelegationDao;
import ch.uzh.extension.campuscourse.data.entity.Delegation;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Iterator;

/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 *
 * Initial Date: 21.09.2016 <br>
 *
 * @author Martin Schraner
 */
@Component
public class DelegationDeletion implements UserDataDeletable {

    private static final OLog LOG = Tracing.createLoggerFor(DelegationDeletion.class);

    private final DelegationDao delegationDao;

    @Autowired
    public DelegationDeletion(DelegationDao delegationDao) {
        this.delegationDao = delegationDao;
    }

    // This method will be called when a OLAT-user is deleted via deletion-manager
    @Override
    public void deleteUserData(Identity identity, String newDeletedUserName, File archivePath) {
        LOG.debug("deleteUserData start");

        Iterator<Delegation> iterator = delegationDao.getDelegationsByDelegator(identity.getKey()).iterator();
        while (iterator.hasNext()) {
            Delegation delegation = iterator.next();
            LOG.info("Delete delegation " + delegation);
            delegationDao.delete(delegation);
        }

        iterator = delegationDao.getDelegationsByDelegatee(identity.getKey()).iterator();
        while (iterator.hasNext()) {
            Delegation delegation = iterator.next();
            LOG.info("Delete delegation " + delegation);
            delegationDao.delete(delegation);
        }
    }
}
