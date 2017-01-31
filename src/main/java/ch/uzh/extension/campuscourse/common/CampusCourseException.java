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
package ch.uzh.extension.campuscourse.common;

/**
 * This Exception can be used as business or wrapper exception for campuskurs.<br>
 * 
 * Initial Date: 12.07.2012 <br>
 * 
 * @author aabouc
 */
@SuppressWarnings("serial")
public class CampusCourseException extends Exception {

    /**
     * Create a new CampusCourseException with the given message.
     * 
     * @param message
     *            the descriptive message
     */
    public CampusCourseException(String message) {
        super(message);
    }
}
