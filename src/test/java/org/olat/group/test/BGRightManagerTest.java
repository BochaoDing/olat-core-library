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

package org.olat.group.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Policy;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.right.BGRightManager;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR>
 * Initial Date: Aug 18, 2004
 * 
 * @author gnaegi
 */
public class BGRightManagerTest extends OlatTestCase {

	private static Logger log = Logger.getLogger(BGRightManagerTest.class.getName());
	private Identity id1, id2, id3, id4;

	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BGRightManager rightManager;
	@Autowired
	private BaseSecurity securityManager;

	/**
	 * SetUp is called before each test.
	 */
	@Before
	public void setUp() {
		try {
			id1 = JunitTestHelper.createAndPersistIdentityAsUser("one");
			id2 = JunitTestHelper.createAndPersistIdentityAsUser("twoo");
			id3 = JunitTestHelper.createAndPersistIdentityAsUser("three");
			id4 = JunitTestHelper.createAndPersistIdentityAsUser("four");
			Assert.assertNotNull(id1);
			Assert.assertNotNull(id2);
			Assert.assertNotNull(id3);
			Assert.assertNotNull(id4);
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
		}
	}

	/**
	 * TearDown is called after each test.
	 * 
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		try {
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
			throw e;
		}
	}

	/** BGContextManagerImpl:deleteBGContext() * */
	@Test
	public void testBGRights() {
		OLATResource c1 = JunitTestHelper.createRandomResource();
		OLATResource c2 = JunitTestHelper.createRandomResource();

		BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "g1", null, -1, -1, false, false, c1);
		BusinessGroup g2 = businessGroupService.createBusinessGroup(null, "g2", null, -1, -1, false, false, c1);
		BusinessGroup g3 = businessGroupService.createBusinessGroup(null, "g3", null, -1, -1, false, false, c2);

		securityManager.addIdentityToSecurityGroup(id1, g1.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id1, g2.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id3, g3.getPartipiciantGroup());

		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, g1);
		rightManager.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g1);
		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, g2);
		rightManager.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g3);
		DBFactory.getInstance().closeSession(); // simulate user clicks

		// secm.createAndPersistPolicy(rightGroup.getPartipiciantGroup(), bgRight,
		// rightGroup.getGroupContext());
		List<SecurityGroup> groups = securityManager.getGroupsWithPermissionOnOlatResourceable(CourseRights.RIGHT_ARCHIVING, c1);
		Assert.assertEquals(2, groups.size());

		List<Identity> identities = securityManager.getIdentitiesWithPermissionOnOlatResourceable(CourseRights.RIGHT_ARCHIVING, c1);
		Assert.assertEquals(2, identities.size());

		List<Policy> policies = securityManager.getPoliciesOfSecurityGroup(g1.getPartipiciantGroup());
		Assert.assertEquals(3, policies.size()); // read, archiving, courseeditor

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c2));
		assertTrue(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c1));
		assertTrue(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id2, c1));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, id2, c1));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id3, c2));
		assertTrue(rightManager.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, id3, c2));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, id3, c1));

		/*
		 * assertTrue(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, g1));
		 * assertTrue(rm.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, g1));
		 * assertTrue(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, g2));
		 * assertFalse(rm.hasBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, g1));
		 */
		Assert.assertEquals(2, rightManager.findBGRights(g1).size());
		Assert.assertEquals(1, rightManager.findBGRights(g2).size());

		DBFactory.getInstance().closeSession(); // simulate user clicks
		rightManager.removeBGRight(CourseRights.RIGHT_ARCHIVING, g1);
		rightManager.removeBGRight(CourseRights.RIGHT_COURSEEDITOR, g1);
		rightManager.removeBGRight(CourseRights.RIGHT_ARCHIVING, g2);
		rightManager.removeBGRight(CourseRights.RIGHT_COURSEEDITOR, g3);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c1));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id2, c1));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, id3, c2));

		Assert.assertEquals(0, rightManager.findBGRights(g1).size());
		Assert.assertEquals(0, rightManager.findBGRights(g2).size());
	}
}