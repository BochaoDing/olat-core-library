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
import java.util.List;

import ch.uzh.campus.data.Org;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

/**
 * This is an implementation of {@link ItemProcessor} that modifies the input OrgProcessor item <br>
 * according to some criteria and returns it as output OrgProcessor item. <br>
 * 
 * Initial Date: 07.12.2012 <br>
 * 
 * @author aabouc
 */
public class OrgProcessor implements ItemProcessor<Org, Org> {

    @Value("#{'${campus.org.identifiers}'.split(',')}")
    private List<String> identifiers;

    /**
     * Modifies the input org and returns it as output
     * 
     * @param org
     *            the Org to be processed
     */
    public Org process(Org org) throws Exception {
        for (String identifier : identifiers) {
            if (org.getShortName() != null && org.getShortName().startsWith(identifier.trim())) {
                org.setModifiedDate(new Date());
                return org;
            }
        }
        return null;
    }

}
