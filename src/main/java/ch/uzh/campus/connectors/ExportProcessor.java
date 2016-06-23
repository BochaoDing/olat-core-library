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
package ch.uzh.campus.connectors;

import java.util.Date;

import ch.uzh.campus.data.Export;
import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.utils.DateUtil;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is an implementation of {@link ItemProcessor} that validates the input Export item, <br>
 * modifies it according to some criteria and returns it as output Export item. <br>
 * 
 * Initial Date: 23.06.2012 <br>
 * 
 * @author aabouc
 */
public class ExportProcessor implements ItemProcessor<Export, Export> {

    private static final OLog LOG = Tracing.createLoggerFor(ExportProcessor.class);

    @Autowired
    CampusCourseConfiguration campusCourseConfiguration;

    /**
     * Checks the export and returns it modified
     * 
     * @param export
     *            the Export to be processed
     * 
     * @return the modified export
     * 
     * @throws CampusException
     *             if the exportDate is older than one day
     */
    public Export process(Export export) throws Exception {
        if (!export.getFileName().contains(campusCourseConfiguration.getSapFilesSuffix())) {
            return null;
        }
        if (DateUtil.isMoreThanOneDayBefore(export.getExportDate())) {
            LOG.error("THE FILE [" + export.getFileName() + "] WILL NOT BE IMPORTED BECAUSE OF THE OLD EXPORT DATE [" + export.getExportDate() + "]");
            throw new CampusException("THE FILE [" + export.getFileName() + "] WILL NOT BE IMPORTED BECAUSE OF THE OLD EXPORT DATE [" + export.getExportDate() + "]");
        }
        export.setCreationDate(new Date());
        return export;
    }

}
