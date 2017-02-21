package ch.uzh.extension.campuscourse.batchprocessing.sapimport;

import ch.uzh.extension.campuscourse.model.EventCourseId;
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
 * This is an implementation of {@link ItemProcessor} that validates the input Event item, <br>
 * modifies it according to some criteria and returns it as output Event item.<br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */
@Component
@Scope("step")
public class EventProcessor implements ItemProcessor<EventCourseId, EventCourseId> {

    /**
     * Modifies the input event and returns it as output
     * 
     * @param event
     *            the Event to be processed
     */
    @Override
    public EventCourseId process(EventCourseId event) throws Exception {
        event.setDateOfLatestImport(new Date());
        return event;
    }
}
