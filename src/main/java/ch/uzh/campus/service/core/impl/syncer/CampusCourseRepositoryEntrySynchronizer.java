package ch.uzh.campus.service.core.impl.syncer;

import ch.uzh.campus.service.core.impl.creator.CampusCourseRepositoryEntryDescriptionBuilder;
import ch.uzh.campus.service.data.CampusCourseTO;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
 *
 * Initial Date: 20.08.2012 <br>
 * 
 * @author cg
 * @author Martin Schraner
 */
@Component
public class CampusCourseRepositoryEntrySynchronizer {

    private static final int MAX_DISPLAYNAME_LENGTH = 140;

    private final CampusCourseRepositoryEntryDescriptionBuilder campusCourseRepositoryEntryDescriptionBuilder;
    private final RepositoryService repositoryService;

    @Autowired
    public CampusCourseRepositoryEntrySynchronizer(CampusCourseRepositoryEntryDescriptionBuilder campusCourseRepositoryEntryDescriptionBuilder, RepositoryService repositoryService) {
        this.campusCourseRepositoryEntryDescriptionBuilder = campusCourseRepositoryEntryDescriptionBuilder;
        this.repositoryService = repositoryService;
    }

    void synchronizeDisplaynameAndDescription(CampusCourseTO campusCourseTO) {
        synchronizeDisplaynameAndDescriptionAndInitialAuthor(campusCourseTO, null);
    }

    public void synchronizeDisplaynameAndDescriptionAndInitialAuthor(CampusCourseTO campusCourseTO, Identity creator) {

        // New display name
        String newDisplayname = truncateTitleForRepositoryEntryDisplayname(campusCourseTO.getTitleToBeDisplayed());

        // New description
        String newDescription = campusCourseRepositoryEntryDescriptionBuilder.buildDescription(campusCourseTO);

        // New initial author
        String newInitialAuthor = (creator != null ? creator.getName() : null);

        // Update repository entry in case an attribute has changed
        RepositoryEntry repositoryEntry = campusCourseTO.getRepositoryEntry();
        boolean repositoryEntryHasToBeUpdated = false;
        if (!newDisplayname.equals(repositoryEntry.getDisplayname())) {
            repositoryEntry.setDisplayname(newDisplayname);
            repositoryEntryHasToBeUpdated = true;
        }
        if (!newDescription.equals(repositoryEntry.getDescription())) {
            repositoryEntry.setDescription(newDescription);
            repositoryEntryHasToBeUpdated = true;
        }
        if (newInitialAuthor != null && !newInitialAuthor.equals(repositoryEntry.getInitialAuthor())) {
            repositoryEntry.setInitialAuthor(newInitialAuthor);
            repositoryEntryHasToBeUpdated = true;
        }

        // Commit
        if (repositoryEntryHasToBeUpdated) {
            repositoryService.update(repositoryEntry);
        }
    }

    public static String truncateTitleForRepositoryEntryDisplayname(String title) {
        return Formatter.truncate(title, MAX_DISPLAYNAME_LENGTH);
    }
}
