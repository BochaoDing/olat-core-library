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
 */
package ch.uzh.campus.service.core.impl.syncer;

import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.utils.ListUtil;
import org.olat.core.commons.persistence.DB;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;

/**
 * This class is an implementation of {@link ItemReader} that reads all already created campus-course records from the database <br>
 * and converts them to the transfer objects of {@link CampusCourseImportTO}. <br>
 * It delegates the actual reading of data from the database to the DaoManager. <br>
 * 
 * Initial Date: 31.10.2012 <br>
 * 
 * @author aabouc
 */
public class CampusCourseSynchronizationReader implements ItemReader<CampusCourseImportTO> {

    private DaoManager daoManager;
    private DB dbInstance;
    private List<Long> sapCoursesIds = Collections.emptyList();

    @Autowired
    public CampusCourseSynchronizationReader(DaoManager daoManager, DB dbInstance) {
        this.daoManager = daoManager;
        this.dbInstance = dbInstance;
    }

    @PostConstruct
    public void init() {
        if (daoManager.checkImportedData()) {
            sapCoursesIds = daoManager.getAllCreatedSapCourcesIds();
        }
        dbInstance.closeSession();
    }

    @PreDestroy
    public void destroy() {
        if (ListUtil.isNotBlank(sapCoursesIds)) {
            sapCoursesIds.clear();
        }
    }

    /**
     * Reads a {@link CampusCourseImportTO} via the {@link DaoManager} with the given course id from the list of the sapCoursesIds. <br>
     * It returns null at the end of the list of the sapCoursesIds
     */
    public synchronized CampusCourseImportTO read() throws Exception {
        if (ListUtil.isNotBlank(sapCoursesIds)) {
            return daoManager.getSapCampusCourse(sapCoursesIds.remove(0));
        }
        return null;
    }

}