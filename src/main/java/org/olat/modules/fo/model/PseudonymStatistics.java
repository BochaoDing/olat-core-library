/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.fo.model;

import java.util.Date;

/**
 * 
 * Initial date: 19 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PseudonymStatistics {
	
	private final Long key;
	private final Date creationDate;
	private final String pseudonym;
	private final Long numOfMessages;
	
	public PseudonymStatistics(Long key, Date creationDate, String pseudonym, Long numOfMessages) {
		this.key = key;
		this.creationDate = creationDate;
		this.pseudonym = pseudonym;
		this.numOfMessages = numOfMessages;
	}

	public Long getKey() {
		return key;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getPseudonym() {
		return pseudonym;
	}

	public Long getNumOfMessages() {
		return numOfMessages;
	}
}
