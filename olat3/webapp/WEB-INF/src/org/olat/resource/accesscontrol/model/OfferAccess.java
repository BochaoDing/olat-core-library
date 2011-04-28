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
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.resource.accesscontrol.model;

import java.util.Date;


/**
 * 
 * Description:<br>
 * Link beetwen an offer and a method to access the resource
 * 
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface OfferAccess {
	
	public Long getKey();
	
	public boolean isValid();
	
	public Offer getOffer();
	
	public AccessMethod getMethod();
	
	public Date getValidFrom();

	public void setValidFrom(Date validFrom);

	public Date getValidTo();

	public void setValidTo(Date validTo);
}
