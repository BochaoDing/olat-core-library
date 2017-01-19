package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.synchronization;

import ch.uzh.extension.campuscourse.model.CampusCourseTO;
import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import ch.uzh.extension.campuscourse.util.ListUtil;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;

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
 * This class is an implementation of {@link ItemReader} that reads all already created campus-course records from the database <br>
 * and converts them to the transfer objects of {@link CampusCourseTO}. <br>
 * It delegates the actual reading of data from the database to the DaoManager. <br>
 * 
 * Initial Date: 31.10.2012 <br>
 * 
 * @author aabouc
 */
@Component
@Scope("step")
public class CampusCourseSynchronizationReader implements ItemReader<CampusCourseTO> {

	private static final OLog LOG = Tracing.createLoggerFor(CampusCourseSynchronizationReader.class);

    private DaoManager daoManager;
    private DB dbInstance;
    private List<Long> sapCourseIds = Collections.emptyList();

    @Autowired
    public CampusCourseSynchronizationReader(DaoManager daoManager, DB dbInstance) {
        this.daoManager = daoManager;
        this.dbInstance = dbInstance;
    }

    @PostConstruct
    public void init() {
        if (daoManager.wasLastSapImportSuccessful()) {
            sapCourseIds = daoManager.getSapIdsOfAllCreatedOlatCampusCourses();
        } else {
            LOG.warn("No Campus Course synchronization will be performed, since last sap import was not successful!");
        }
        dbInstance.closeSession();
    }

    @PreDestroy
    public void destroy() {
        if (ListUtil.isNotBlank(sapCourseIds)) {
            sapCourseIds.clear();
        }
    }

    /**
     * Reads a {@link CampusCourseTO} via the {@link DaoManager} with the given course id from the list of the sapCourseIds. <br>
     * It returns null at the end of the list of the sapCourseIds
     */
    public synchronized CampusCourseTO read() throws Exception {
        if (ListUtil.isNotBlank(sapCourseIds)) {
            CampusCourseTO campusCourseTO = daoManager.loadCampusCourseTO(sapCourseIds.remove(0));
            dbInstance.closeSession();
            return campusCourseTO;
        }
        return null;
    }

}
