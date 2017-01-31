package ch.uzh.extension.campuscourse.batchprocessing.sapimport;

import ch.uzh.extension.campuscourse.data.entity.Lecturer;
import org.apache.commons.lang.StringUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;

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
 * This is an implementation of {@link ItemProcessor} that validates the input Lecturer item, <br>
 * modifies it according to some criteria and returns it as output Lecturer item. <br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */
@Component
@Scope("step")
public class LecturerProcessor implements ItemProcessor<Lecturer, Lecturer> {

    /**
     * Modifies lecturer according to some criteria and returns it as output.
     * 
     * @param lecturer the Lecturer to be processed
     */
    @Override
    public Lecturer process(Lecturer lecturer) throws Exception {

        lecturer.setDateOfImport(new Date());

        if (StringUtils.isBlank(lecturer.getEmail())) {
            lecturer.setEmail(lecturer.getPrivateEmail());
        }

        return lecturer;
    }

}
