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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.commons.persistence;

import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.BeanFactory;

/**
 * Factories with static method are deprecated because they cannot be
 * replaced.
 */
@Deprecated
public class DBFactory {

	private static final OLog LOG = Tracing.createLoggerFor(DBFactory.class);
	private static DB db;

	/**
	 * !!IMPORTANT!!
	 * Get the DB instance. The DB Session is lazy initialized.
	 * Make sure you don't save the instance in a class variable as you do not
	 * have a guarantee that a Hibernate session is already initialized.
	 *
	 * @return the DB instance.
	 */
	public static DB getInstance() {
		if (db == null) {
			/*
			 * This is an unstable hack in order that the DB instance can be
			 * easily replaced/mocked by Spring. Unstable because it depends
			 * on the initialization order of Spring.
			 */
			BeanFactory beanFactory = CoreSpringFactory.getBeanFactory();
			db = beanFactory.getBean(DB.class);
		}
		return db;
	}
}
