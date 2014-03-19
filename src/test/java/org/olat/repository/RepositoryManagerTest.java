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

package org.olat.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JMSCodePointServerJunitHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryManagerTest extends OlatTestCase {
	
	private static final OLog log = Tracing.createLoggerFor(RepositoryManagerTest.class);
	private static String CODEPOINT_SERVER_ID = "RepositoryManagerTest";

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	
	@Before
	public void setup() {
		try {
			// Setup for code-points
			JMSCodePointServerJunitHelper.startServer(CODEPOINT_SERVER_ID);
		} catch (Exception e) {
			log.error("Error while setting up activeMq or Codepointserver", e);
		}
	}

	@After public void tearDown() {
		try {
			JMSCodePointServerJunitHelper.stopServer();
		} catch (Exception e) {
			log.error("tearDown failed", e);
		}
	}

	/**
	 * Test creation of a repository entry.
	 */
	@Test
	public void testRawRepositoryEntryCreate() {
		try {
			DB db = DBFactory.getInstance();
			OLATResourceManager rm = OLATResourceManager.getInstance();
			// create course and persist as OLATResourceImpl
			OLATResourceable resourceable = new OLATResourceable() {
					public String getResourceableTypeName() {	return "RepoMgrTestCourse";}
					public Long getResourceableId() {return CodeHelper.getForeverUniqueID();}
			};
			OLATResource r =  rm.createOLATResourceInstance(resourceable);
			db.saveObject(r);
	
			// now make a repository entry for this course
			RepositoryEntry d = new RepositoryEntry();
			d.setStatistics(new RepositoryEntryStatistics());
			d.setOlatResource(r);
			d.setResourcename("Lernen mit OLAT");
			d.setInitialAuthor("Florian Gnägi");
			d.setDisplayname("JunitTest_RepositoryEntry");
			db.saveObject(d);
		} catch(Exception ex) {
			fail("No Exception allowed. ex=" + ex.getMessage());
		}
	}
	
	@Test
	public void lookupRepositoryEntryByOLATResourceable() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry loadedRe = repositoryManager.lookupRepositoryEntry(re.getOlatResource(), false);
		
		Assert.assertNotNull(loadedRe);
		Assert.assertEquals(re, loadedRe);
	}
	
	@Test
	public void lookupRepositoryEntryBySoftkey() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry loadedRe = repositoryManager.lookupRepositoryEntryBySoftkey(re.getSoftkey(), false);
		Assert.assertNotNull(loadedRe);
		Assert.assertEquals(re, loadedRe);
	}
	
	@Test
	public void lookupRepositoryEntryKey() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		//check with a return value
		Long repoKey1 = repositoryManager.lookupRepositoryEntryKey(re.getOlatResource(), false);
		Assert.assertNotNull(repoKey1);
		Assert.assertEquals(re.getKey(), repoKey1);
		
		//check with a return value
		Long repoKey2 = repositoryManager.lookupRepositoryEntryKey(re.getOlatResource(), true);
		Assert.assertNotNull(repoKey2);
		Assert.assertEquals(re.getKey(), repoKey2);
		
		//check with a return value
		OLATResourceable dummy = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), 0l);
		Long repoKey3 = repositoryManager.lookupRepositoryEntryKey(dummy, false);
		Assert.assertNull(repoKey3);
	}
	
	@Test
	public void lookupRepositoryEntries() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		//check with a return value
		List<Long> keys = Collections.singletonList(re.getKey());
		List<RepositoryEntry> entries = repositoryManager.lookupRepositoryEntries(keys);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(re));
	}
	
	@Test(expected=AssertException.class)
	public void lookupRepositoryEntryKeyStrictFailed() {
		//check with a return value
		OLATResourceable dummy = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), 0l);
		Long repoKey3 = repositoryManager.lookupRepositoryEntryKey(dummy, true);
		Assert.assertNull(repoKey3);
	}
	
	@Test
	public void lookupDisplayNameByOLATResourceableId() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		String displayName = repositoryManager.lookupDisplayNameByOLATResourceableId(re.getOlatResource().getResourceableId());
		Assert.assertNotNull(displayName);
		Assert.assertEquals(re.getDisplayname(), displayName);
	}
	@Test
	public void lookupResource() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		OLATResource resource = repositoryManager.lookupRepositoryEntryResource(re.getKey());
		Assert.assertNotNull(resource);
		Assert.assertEquals(re.getOlatResource(), resource);
	}
	
	@Test
	public void queryByEditor() {
		//create a repository entry with an owner
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-owner-la-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntry> entries = repositoryManager.queryByEditor(id);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(re));
	}
	
	@Test
	public void queryByOwner() {
		//create a repository entry with an owner
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-owner-la-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntry> entries = repositoryManager.queryByOwner(id);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(re));
	}
	
	@Test
	public void queryByOwnerLimitAccess() {
		//create a repository entry with an owner
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-owner-la-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntry> entries = repositoryManager.queryByOwnerLimitAccess(id, RepositoryEntry.ACC_OWNERS, Boolean.TRUE);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(re));
	}
	
	@Test
	public void getLearningResourcesAsStudent() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-stud-la-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> entries = repositoryManager.getLearningResourcesAsStudent(id, 0, -1, RepositoryEntryOrder.nameAsc);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		
		Set<Long> duplicates = new HashSet<Long>();
		for(RepositoryEntry entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.getAccess() >= RepositoryEntry.ACC_USERS);
			}
		}
	}
	
	@Test
	public void getLearningResourcesAsStudentWithGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-stud-lb-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "studg", "tg", null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> entries = repositoryManager.getLearningResourcesAsStudent(id, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		
		Set<Long> duplicates = new HashSet<Long>();
		for(RepositoryEntry entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.getAccess() >= RepositoryEntry.ACC_USERS);
			}
		}
	}
	
	@Test
	public void getParticipantRepositoryEntry() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-stud-lc-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntryLight> entries = repositoryManager.getParticipantRepositoryEntry(id, -1, RepositoryEntryOrder.nameAsc);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		
		boolean found = false;
		Set<Long> duplicates = new HashSet<Long>();
		for(RepositoryEntryLight entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			if(entry.getKey().equals(re.getKey())) {
				found = true;
			}
		
			if(entry.getAccess() >= RepositoryEntry.ACC_USERS) {
				//OK
			} else if(entry.getAccess() == RepositoryEntry.ACC_OWNERS && entry.isMembersOnly()) {
				RepositoryEntry reloadedRe = repositoryManager.lookupRepositoryEntry(entry.getKey());
				boolean member = repositoryEntryRelationDao.hasRole(id, reloadedRe, GroupRoles.participant.name());
				Assert.assertTrue(member);
			} else {
				Assert.fail();
			}
		}
		
		Assert.assertTrue(found);
	}
	
	@Test
	public void getParticipantRepositoryEntryWithGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-stud-ld-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "studh", "th", null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntryLight> entries = repositoryManager.getParticipantRepositoryEntry(id, -1);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		
		boolean found = false;
		Set<Long> duplicates = new HashSet<Long>();
		for(RepositoryEntryLight entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			
			if(entry.getKey().equals(re.getKey())) {
				found = true;
			}
		}
		
		Assert.assertTrue(found);
	}
	
	@Test
	public void getLearningResourcesAsTeacher() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-teac-la-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> entries = repositoryManager.getLearningResourcesAsTeacher(id, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		
		Set<Long> duplicates = new HashSet<Long>();
		for(RepositoryEntry entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.getAccess() >= RepositoryEntry.ACC_USERS);
			}
		}
	}
	
	@Test
	public void getLearningResourcesAsTeacherWithGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-teac-lb-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "teacherg", "tg", null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> entries = repositoryManager.getLearningResourcesAsTeacher(id, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		
		Set<Long> duplicates = new HashSet<Long>();
		for(RepositoryEntry entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.getAccess() >= RepositoryEntry.ACC_USERS);
			}
		}
	}
	
	@Test
	public void getFavoritLearningResourcesAsTeacher() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-fav-1-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		markManager.setMark(re, id, null, "[RepositoryEntry:" + re.getKey() + "]");
		dbInstance.commitAndCloseSession();
		
		//check get forbidden favorit
		List<RepositoryEntry> forbiddenEntries = repositoryManager.getFavoritLearningResourcesAsTeacher(id, null, 0, -1);
		Assert.assertNotNull(forbiddenEntries);
		Assert.assertEquals(0, forbiddenEntries.size());
		int countForbiddenEntries = repositoryManager.countFavoritLearningResourcesAsTeacher(id, null);
		Assert.assertEquals(0, countForbiddenEntries);
		
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		//check get favorit
		List<RepositoryEntry> entries = repositoryManager.getFavoritLearningResourcesAsTeacher(id, null, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(re));
		
		//check count
		int countEntries = repositoryManager.countFavoritLearningResourcesAsTeacher(id, null);
		Assert.assertEquals(1, countEntries);
	}
	
	@Test
	public void getTutorRepositoryEntry() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-stud-le-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntryLight> entries = repositoryManager.getTutorRepositoryEntry(id, -1, RepositoryEntryOrder.nameAsc);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		
		boolean found = false;
		Set<Long> duplicates = new HashSet<Long>();
		for(RepositoryEntryLight entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			if(entry.getKey().equals(re.getKey())) {
				found = true;
			}
		
			if(entry.getAccess() >= RepositoryEntry.ACC_USERS) {
				//OK
			} else if(entry.getAccess() == RepositoryEntry.ACC_OWNERS && entry.isMembersOnly()) {
				RepositoryEntry reloadedRe = repositoryManager.lookupRepositoryEntry(entry.getKey());
				boolean member = repositoryEntryRelationDao.hasRole(id, reloadedRe, GroupRoles.coach.name());
				Assert.assertTrue(member);
			} else {
				Assert.fail();
			}
		}
		
		Assert.assertTrue(found);
	}
	
	@Test
	public void getTutorRepositoryEntryWithGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-stud-lf-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "studi", "ti", null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntryLight> entries = repositoryManager.getTutorRepositoryEntry(id, -1);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		
		boolean found = false;
		Set<Long> duplicates = new HashSet<Long>();
		for(RepositoryEntryLight entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			
			if(entry.getKey().equals(re.getKey())) {
				found = true;
			}
		}
		
		Assert.assertTrue(found);
	}
	
	@Test
	public void getFavoritLearningResourcesAsTeacher_restrictedTypes() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-fav-1-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		markManager.setMark(re, id, null, "[RepositoryEntry:" + re.getKey() + "]");
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		//check get favorite
		List<String> types = Collections.singletonList(re.getOlatResource().getResourceableTypeName());
		List<RepositoryEntry> entries = repositoryManager.getFavoritLearningResourcesAsTeacher(id, types, 0, -1, RepositoryEntryOrder.nameAsc);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(re));
		
		//check count
		int countEntries = repositoryManager.countFavoritLearningResourcesAsTeacher(id, types);
		Assert.assertEquals(1, countEntries);
	}
	
	@Test
	public void getFavoritLearningResourcesAsTeacher_negativeTypes() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-fav-1-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		markManager.setMark(re, id, null, "[RepositoryEntry:" + re.getKey() + "]");
		dbInstance.commitAndCloseSession();
		
		//check get favorite
		List<String> types = Collections.singletonList("CourseModule");
		List<RepositoryEntry> entries = repositoryManager.getFavoritLearningResourcesAsTeacher(id, types, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertEquals(0, entries.size());
		
		//check count
		int countEntries = repositoryManager.countFavoritLearningResourcesAsTeacher(id, types);
		Assert.assertEquals(0, countEntries);
	}
	
	@Test
	public void queryByTypeLimitAccess() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("qbtla-1-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "qbtla-1", "tg", null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		//check
		List<String> types = Collections.singletonList(re.getOlatResource().getResourceableTypeName());
		List<RepositoryEntry> entries = repositoryManager.queryByTypeLimitAccess(id,
				types, new Roles(false, false, false, false, false, false, false));
		
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		for(RepositoryEntry entry:entries) {
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.getAccess() >= RepositoryEntry.ACC_USERS);
			}
		}
	}
	
	@Test
	public void queryByTypeLimitAccess_withoutInstitution() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("qbtla-2-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "qbtla-2", "tg", null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		//check
		List<String> types = Collections.singletonList(re.getOlatResource().getResourceableTypeName());
		List<RepositoryEntry> entries = repositoryManager.queryByTypeLimitAccess(id,
				new Roles(false, false, false, false, false, false, false), types);
		
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		for(RepositoryEntry entry:entries) {
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.getAccess() >= RepositoryEntry.ACC_USERS);
			}
		}
	}
	
	@Test
	public void queryByTypeLimitAccess_withInstitution() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("qbtla-3-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "qbtla-3", "tg", null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();

		//promote id to institution resource manager
		id.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "openolat.org");
		userManager.updateUserFromIdentity(id);
		SecurityGroup institutionalResourceManagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_INST_ORES_MANAGER);
		securityManager.addIdentityToSecurityGroup(id, institutionalResourceManagerGroup);
		dbInstance.commitAndCloseSession();
		
		//check
		List<String> types = Collections.singletonList(re.getOlatResource().getResourceableTypeName());
		List<RepositoryEntry> entries = repositoryManager.queryByTypeLimitAccess(id,
				new Roles(false, false, false, false, false, true, false), types);
		
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		for(RepositoryEntry entry:entries) {
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.getAccess() >= RepositoryEntry.ACC_USERS);
			}
		}
	}
	
	@Test
	public void queryResourcesLimitType() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("re-member-lc-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		List<String> resourceTypes = Collections.singletonList(re.getOlatResource().getResourceableTypeName());
		List<RepositoryEntry> entries = repositoryManager
				.queryResourcesLimitType(id, resourceTypes, "re-member", "me", "no", true, true);
		Assert.assertNotNull(entries);
	}
	
	@Test
	public void queryReferencableResourcesLimitType() {
		final String FG_TYPE = UUID.randomUUID().toString().replace("_", "");
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsAuthor("id1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsAuthor("id2");

		// generate 5000 repo entries
		int numbRes = 500;
		long startCreate = System.currentTimeMillis();
		for (int i = 1; i < numbRes; i++) {
			// create course and persist as OLATResourceImpl
			Identity owner = (i % 2 > 0) ? id1 : id2;

			OLATResourceable resourceable = OresHelper.createOLATResourceableInstance(FG_TYPE, new Long(i));
			OLATResource r =  OLATResourceManager.getInstance().createOLATResourceInstance(resourceable);
			dbInstance.getCurrentEntityManager().persist(r);
			
			// now make a repository entry for this course
			RepositoryEntry re = repositoryService.create(owner, "Lernen mit OLAT " + i,
					"JunitTest_RepositoryEntry_" + i, "yo man description bla bla + i", r);
			re.setAccess(RepositoryEntry.ACC_OWNERS_AUTHORS);			
			if ((i % 2 > 0)) {
				re.setCanReference(true);
			}
			// save the repository entry
			repositoryService.update(re);
			
			// Create course admin policy for owner group of repository entry
			// -> All owners of repository entries are course admins
			//securityManager.createAndPersistPolicy(re.getOwnerGroup(), Constants.PERMISSION_ADMIN, re.getOlatResource());	
			
			// flush database and hibernate session cache after 10 records to improve performance
			// without this optimization, the first entries will be fast but then the adding new 
			// entries will slow down due to the fact that hibernate needs to adjust the size of
			// the session cache permanently. flushing or transactions won't help since the problem
			// is in the session cache. 
			if (i%10 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
		long endCreate = System.currentTimeMillis();
		log.info("created " + numbRes + " repo entries in " + (endCreate - startCreate) + "ms");
		
		List<String> typelist = Collections.singletonList(FG_TYPE);
		// finally the search query
		long startSearchReferencable = System.currentTimeMillis();
		List<RepositoryEntry> results = repositoryManager.queryReferencableResourcesLimitType(id1, new Roles(false, false, false, true, false, false, false), typelist, null, null, null);
		long endSearchReferencable = System.currentTimeMillis();
		log.info("found " + results.size() + " repo entries " + (endSearchReferencable - startSearchReferencable) + "ms");

		// only half of the items should be found
		assertEquals(numbRes / 2, results.size());
		
		// inserting must take longer than searching, otherwhise most certainly we have a problem somewhere in the query
		assertTrue((endCreate - startCreate) > (endSearchReferencable - startSearchReferencable));
	}
	
	@Test
	public void isMember() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("re-member-lc-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("re-member-lc-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "memberg", "tg", null, null, false, false, re);
	    businessGroupRelationDao.addRole(id1, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();

		//id1 is member
		boolean member1 = repositoryManager.isMember(id1, re);
		Assert.assertTrue(member1);
		//id2 is not member
		boolean member2 = repositoryManager.isMember(id2, re);
		Assert.assertFalse(member2);
	}
	
	@Test
	public void isMember_v2() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("re-is-member-1-lc-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("re-is-member-2-lc-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("re-is-member-3-lc-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("re-is-member-4-lc-" + UUID.randomUUID().toString());
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsUser("re-is-member-5-lc-" + UUID.randomUUID().toString());
		Identity id6 = JunitTestHelper.createAndPersistIdentityAsUser("re-is-member-6-lc-" + UUID.randomUUID().toString());
		Identity idNull = JunitTestHelper.createAndPersistIdentityAsUser("re-is-member-null-lc-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "member-1-g", "tg", null, null, false, false, re);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "member-2-g", "tg", null, null, false, false, re);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "member-3-g", "tg", null, null, true, false, re);
		BusinessGroup groupNull = businessGroupService.createBusinessGroup(null, "member-null-g", "tg", null, null, true, false, null);
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.participant.name());
	    businessGroupRelationDao.addRole(id4, group1, GroupRoles.coach.name());
	    businessGroupRelationDao.addRole(id5, group2, GroupRoles.participant.name());
	    businessGroupRelationDao.addRole(id6, group3, GroupRoles.waiting.name());
	    businessGroupRelationDao.addRole(idNull, groupNull, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//id1 is owner
		boolean member1 = repositoryManager.isMember(id1, re);
		Assert.assertTrue(member1);
		//id2 is tutor
		boolean member2 = repositoryManager.isMember(id2, re);
		Assert.assertTrue(member2);
		//id3 is repo participant
		boolean member3 = repositoryManager.isMember(id3, re);
		Assert.assertTrue(member3);
		//id4 is group coach
		boolean member4= repositoryManager.isMember(id4, re);
		Assert.assertTrue(member4);
		//id5 is group participant
		boolean member5 = repositoryManager.isMember(id5, re);
		Assert.assertTrue(member5);
		//id6 is waiting
		boolean member6 = repositoryManager.isMember(id6, re);
		Assert.assertFalse(member6);
		//idNull is not member
		boolean memberNull = repositoryManager.isMember(idNull, re);
		Assert.assertFalse(memberNull);
	}
	
	@Test
	public void isOwnerOfRepositoryEntry() {
		//create a repository entry with an owner and a participant
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("re-owner-is-" + UUID.randomUUID().toString());
		Identity part = JunitTestHelper.createAndPersistIdentityAsUser("re-owner-is-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		repositoryEntryRelationDao.addRole(owner, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(part, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//check
		boolean isOwnerOwner = repositoryManager.isOwnerOfRepositoryEntry(owner, re);
		Assert.assertTrue(isOwnerOwner);
		boolean isPartOwner = repositoryManager.isOwnerOfRepositoryEntry(part, re);
		Assert.assertFalse(isPartOwner);
	}
	
	@Test
	public void countLearningResourcesAsOwner() {
		//create a repository entry with an owner and a participant
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("re-owner-is-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		repositoryEntryRelationDao.addRole(owner, re, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		//check
		int count = repositoryManager.countLearningResourcesAsOwner(owner);
		Assert.assertEquals(1, count);
	}
	
	@Test
	public void countLearningResourcesAsStudent() {
		//create a repository entry with an owner and a participant
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("re-participant-is-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		repositoryEntryRelationDao.addRole(owner, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//check
		int count = repositoryManager.countLearningResourcesAsStudent(owner);
		Assert.assertTrue(1 <= count);
	}
	
	@Test
	public void isIdentityInTutorSecurityGroup() {
		//create a repository entry with an owner and a participant
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("re-tutor-is-" + UUID.randomUUID().toString());
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re3 = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		repositoryEntryRelationDao.addRole(identity, re1, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(identity, re2, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(identity, re3, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		//check
		boolean isTutor1 = repositoryManager.isIdentityInTutorSecurityGroup(identity, re1);
		Assert.assertTrue(isTutor1);
		boolean isTutor2 = repositoryManager.isIdentityInTutorSecurityGroup(identity, re2);
		Assert.assertFalse(isTutor2);
		boolean isTutor3 = repositoryManager.isIdentityInTutorSecurityGroup(identity, re3);
		Assert.assertFalse(isTutor3);
	}
	
	@Test
	public void getRepositoryentryMembership() {
		//create a repository entry with an owner and a participant
		Identity admin = securityManager.findIdentityByName("administrator");
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("re-m-is-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("re-m-is-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("re-m-is-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("re-m-is-" + UUID.randomUUID().toString());
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsUser("re-m-is-" + UUID.randomUUID().toString());
		Identity id6 = JunitTestHelper.createAndPersistIdentityAsUser("re-m-is-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		if(repositoryEntryRelationDao.hasRole(admin, re, GroupRoles.owner.name())) {
			repositoryEntryRelationDao.removeRole(admin, re, GroupRoles.owner.name());
		}
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id4, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id5, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(id6, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.participant.name());
		
		dbInstance.commitAndCloseSession();
		
		Set<Long> identityKeys = new HashSet<Long>();
		identityKeys.add(id1.getKey());
		identityKeys.add(id2.getKey());
		identityKeys.add(id3.getKey());
		identityKeys.add(id4.getKey());
		identityKeys.add(id5.getKey());
		identityKeys.add(id6.getKey());
		
		//check with all identities
		List<RepositoryEntryMembership> memberships = repositoryManager.getRepositoryEntryMembership(re);
		Assert.assertNotNull(memberships);
		Assert.assertEquals(6, memberships.size());
		
		int countOwner = 0;
		int countTutor = 0;
		int countParticipant = 0;
		for(RepositoryEntryMembership membership:memberships) {
			if(membership.isOwner()) {
				countOwner++;
				Assert.assertEquals(re.getKey(), membership.getRepoKey());
			}
			if (membership.isCoach()) {
				countTutor++;
				Assert.assertEquals(re.getKey(), membership.getRepoKey());
			}
			if (membership.isParticipant()) {
				countParticipant++;
				Assert.assertEquals(re.getKey(), membership.getRepoKey());
			}
			Assert.assertTrue(identityKeys.contains(membership.getIdentityKey()));
		}
		Assert.assertEquals(2, countOwner);
		Assert.assertEquals(2, countTutor);
		Assert.assertEquals(3, countParticipant);
		
		//check with id1
		List<RepositoryEntryMembership> membership1s = repositoryManager.getRepositoryEntryMembership(re, id1);
		Assert.assertNotNull(membership1s);
		Assert.assertEquals(2, membership1s.size());
		for(RepositoryEntryMembership membership:membership1s) {
			if(membership.isOwner()) {
				Assert.assertEquals(re.getKey(), membership.getRepoKey());
			} else if (membership.isParticipant()) {
				Assert.assertEquals(re.getKey(), membership.getRepoKey());
			} else {
				Assert.assertTrue(false);
			}
			Assert.assertEquals(id1.getKey(), membership.getIdentityKey());
		}
	}
	
	@Test
	public void getRepositoryentryMembershipAgainstDummy() {
		//no repo, no identities
		List<RepositoryEntryMembership> membership2s = repositoryManager.getRepositoryEntryMembership(null);
		Assert.assertNotNull(membership2s);
		Assert.assertTrue(membership2s.isEmpty());
	}
	
	/**
	 * How can be a resource manager if Constants.ORESOURCE_USERMANAGER is never used?
	 */
	@Test
	public void isInstitutionalRessourceManagerFor() {
		Identity owner1 = JunitTestHelper.createAndPersistIdentityAsUser("instit-" + UUID.randomUUID().toString());
		Identity owner2 = JunitTestHelper.createAndPersistIdentityAsUser("instit-" + UUID.randomUUID().toString());
		Identity part3 = JunitTestHelper.createAndPersistIdentityAsUser("instit-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(owner1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(owner2, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(part3, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		//set the institutions
		owner1.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "volks");
		owner2.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "volks");
		part3.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "volks");
		userManager.updateUserFromIdentity(owner1);
		userManager.updateUserFromIdentity(owner2);
		userManager.updateUserFromIdentity(part3);
		dbInstance.commit();
		
		//promote owner1 to institution resource manager
		SecurityGroup institutionalResourceManagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_INST_ORES_MANAGER);
		securityManager.addIdentityToSecurityGroup(owner1, institutionalResourceManagerGroup);
		dbInstance.commitAndCloseSession();
		
		//check
		Roles roles = new Roles(false, false, false, false, false, true, false);
		boolean institutionMgr1 = repositoryManager.isInstitutionalRessourceManagerFor(owner1, roles, re);
		boolean institutionMgr2 = repositoryManager.isInstitutionalRessourceManagerFor(owner2, roles, re);
		boolean institutionMgr3 = repositoryManager.isInstitutionalRessourceManagerFor(part3, roles, re);
	
		Assert.assertTrue(institutionMgr1);
		Assert.assertFalse(institutionMgr2);
		Assert.assertFalse(institutionMgr3);
	}

	@Test
	public void testCountByTypeLimitAccess() {
		String TYPE = UUID.randomUUID().toString().replace("-", "");
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("re-gen-1-" + UUID.randomUUID().toString());
		
		int count = repositoryManager.countByTypeLimitAccess("unkown", RepositoryEntry.ACC_OWNERS_AUTHORS);
		assertEquals("Unkown type must return 0 elements", 0,count);
		int countValueBefore = repositoryManager.countByTypeLimitAccess(TYPE, RepositoryEntry.ACC_OWNERS_AUTHORS);
		// add 1 entry
		RepositoryEntry re = createRepositoryEntry(TYPE, owner, 999999l);
		// create security group
		repositoryService.update(re);
		count = repositoryManager.countByTypeLimitAccess(TYPE, RepositoryEntry.ACC_OWNERS_AUTHORS);
		// check count must be one more element
		assertEquals("Add one course repository-entry, but countByTypeLimitAccess does NOT return one more element", countValueBefore + 1,count);
	}
	
	@Test
	public void genericANDQueryWithRolesRestrictionMembersOnly() {
		//create 2 identities (repo owner and tutor)
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("re-gen-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("re-gen-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("re-gen-3-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "teacherg", "tg", null, null, false, false, re);
	    businessGroupRelationDao.addRole(id2, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		
		//check for id 1 (owner of the repository entry)
		SearchRepositoryEntryParameters params1 = new SearchRepositoryEntryParameters();
		params1.setIdentity(id1);
		params1.setRoles(new Roles(false, false, false, false, false, false, false));
		params1.setOnlyExplicitMember(true);
		List<RepositoryEntry> entries1 = repositoryManager.genericANDQueryWithRolesRestriction(params1, 0, -1, true);
		Assert.assertNotNull(entries1);
		Assert.assertFalse(entries1.isEmpty());
		Assert.assertTrue(entries1.contains(re));
		for(RepositoryEntry entry:entries1) {
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.getAccess() >= RepositoryEntry.ACC_USERS);
			}
		}
		
		//check for id2 (tutor)
		SearchRepositoryEntryParameters params2 = new SearchRepositoryEntryParameters();
		params2.setIdentity(id2);
		params2.setRoles(new Roles(false, false, false, false, false, false, false));
		params2.setOnlyExplicitMember(true);
		List<RepositoryEntry> entries2 = repositoryManager.genericANDQueryWithRolesRestriction(params2, 0, -1, true);
		Assert.assertNotNull(entries2);
		Assert.assertFalse(entries2.isEmpty());
		Assert.assertTrue(entries2.contains(re));
		for(RepositoryEntry entry:entries2) {
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.getAccess() >= RepositoryEntry.ACC_USERS);
			}
		}
		
		//check for id3 (negative test)
		SearchRepositoryEntryParameters params3 = new SearchRepositoryEntryParameters();
		params3.setIdentity(id3);
		params3.setRoles(new Roles(false, false, false, false, false, false, false));
		params3.setOnlyExplicitMember(true);
		List<RepositoryEntry> entries3 = repositoryManager.genericANDQueryWithRolesRestriction(params3, 0, -1, true);
		Assert.assertNotNull(entries3);
		Assert.assertFalse(entries3.contains(re));
		for(RepositoryEntry entry:entries3) {
			Assert.assertTrue(entry.getAccess() >= RepositoryEntry.ACC_USERS);
		}
	}
	
	@Test
	public void genericANDQueryWithRolesWithStandardUser() {
		//create 2 identities (repo owner and tutor)
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("re-gen-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("re-gen-2-" + UUID.randomUUID().toString());
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id1, re2, GroupRoles.participant.name());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "teacherg", "tg", null, null, false, false, re1);
	    businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		
		//check for guest (negative test)
		SearchRepositoryEntryParameters params1 = new SearchRepositoryEntryParameters();
		params1.setRoles(new Roles(false, false, false, false, true, false, false));
		List<RepositoryEntry> entries1 = repositoryManager.genericANDQueryWithRolesRestriction(params1, 0, -1, true);
		Assert.assertNotNull(entries1);
		Assert.assertFalse(entries1.contains(re1));
		Assert.assertFalse(entries1.contains(re2));
		for(RepositoryEntry entry:entries1) {
			Assert.assertTrue(entry.getAccess() >= RepositoryEntry.ACC_USERS_GUESTS);
		}
		
		//check for identity 1 (participant re2 + re1 accessible to all users)
		SearchRepositoryEntryParameters params2 = new SearchRepositoryEntryParameters();
		params2.setIdentity(id1);
		params2.setRoles(new Roles(false, false, false, false, false, false, false));
		List<RepositoryEntry> entries2 = repositoryManager.genericANDQueryWithRolesRestriction(params2, 0, -1, true);
		Assert.assertNotNull(entries2);
		Assert.assertFalse(entries2.isEmpty());
		Assert.assertTrue(entries2.contains(re1));
		Assert.assertTrue(entries2.contains(re2));
		for(RepositoryEntry entry:entries2) {
			if(!entry.equals(re2)) {
				Assert.assertTrue(entry.getAccess() >= RepositoryEntry.ACC_USERS);
			}
		}
		
		//check for identity 1 (re1 accessible to all users)
		SearchRepositoryEntryParameters params3 = new SearchRepositoryEntryParameters();
		params3.setIdentity(id2);
		params3.setRoles(new Roles(false, false, false, false, false, false, false));
		List<RepositoryEntry> entries3 = repositoryManager.genericANDQueryWithRolesRestriction(params3, 0, -1, true);
		Assert.assertNotNull(entries3);
		Assert.assertFalse(entries3.isEmpty());
		Assert.assertTrue(entries3.contains(re1));
		Assert.assertFalse(entries3.contains(re2));
		for(RepositoryEntry entry:entries3) {
			Assert.assertTrue(entry.getAccess() >= RepositoryEntry.ACC_USERS);
		}
	}

	@Test
	public void genericANDQueryWithRoles_managed() {
		RepositoryEntry managedRe = JunitTestHelper.createAndPersistRepositoryEntry();
		managedRe.setManagedFlagsString("all");
		managedRe = dbInstance.getCurrentEntityManager().merge(managedRe);
		RepositoryEntry freeRe = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		//search managed
		SearchRepositoryEntryParameters paramsManaged = new SearchRepositoryEntryParameters();
		paramsManaged.setRoles(new Roles(true, false, false, false, false, false, false));
		paramsManaged.setManaged(Boolean.TRUE);
		List<RepositoryEntry> managedEntries = repositoryManager.genericANDQueryWithRolesRestriction(paramsManaged, 0, -1, true);
		Assert.assertNotNull(managedEntries);
		Assert.assertTrue(managedEntries.size() > 0);
		Assert.assertTrue(managedEntries.contains(managedRe));
		Assert.assertFalse(managedEntries.contains(freeRe));

		//search unmanaged
		SearchRepositoryEntryParameters paramsFree = new SearchRepositoryEntryParameters();
		paramsFree.setRoles(new Roles(true, false, false, false, false, false, false));
		paramsFree.setManaged(Boolean.FALSE);
		List<RepositoryEntry> freeEntries = repositoryManager.genericANDQueryWithRolesRestriction(paramsFree, 0, -1, true);
		Assert.assertNotNull(freeEntries);
		Assert.assertTrue(freeEntries.size() > 0);
		Assert.assertFalse(freeEntries.contains(managedRe));
		Assert.assertTrue(freeEntries.contains(freeRe));
	}
	
	@Test
	public void genericANDQueryWithRoles_owned() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("owned-re-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(owner, re, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
		params.setRoles(new Roles(false, false, false, true, false, false, false));
		params.setOnlyOwnedResources(true);
		params.setIdentity(owner);
		List<RepositoryEntry> myEntries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		Assert.assertNotNull(myEntries);
		Assert.assertEquals(1, myEntries.size());
		Assert.assertTrue(myEntries.contains(re));
	}
	
	@Test
	public void genericANDQueryWithRoles_byauthor() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("author-re-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(owner, re, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
		params.setRoles(new Roles(true, false, false, false, false, false, false));
		params.setAuthor(owner.getName());
		List<RepositoryEntry> myEntries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		Assert.assertNotNull(myEntries);
		Assert.assertEquals(1, myEntries.size());
		Assert.assertTrue(myEntries.contains(re));
	}

	@Test
	public void setDescriptionAndName() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		
		String newName = "Brand new name";
		String newDesc = "Brand new description";
		re = repositoryManager.setDescriptionAndName(re, newName, newDesc);
		Assert.assertNotNull(re);
		
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry reloaded = repositoryManager.lookupRepositoryEntry(re.getKey());
		Assert.assertNotNull(reloaded);
		Assert.assertEquals("Brand new name", reloaded.getDisplayname());
		Assert.assertEquals("Brand new description", reloaded.getDescription());
	}
	
	@Test
	public void setDescriptionAndName_lifecycle() {
		RepositoryEntryLifecycle publicCycle
			= lifecycleDao.create("Public 1", "Soft public 1", false, new Date(), new Date());

		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		
		String newName = "Brand new name";
		String newDesc = "Brand new description";
		re = repositoryManager.setDescriptionAndName(re, newName, newDesc, publicCycle);
		Assert.assertNotNull(re);
		
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry reloaded = repositoryManager.lookupRepositoryEntry(re.getKey());
		Assert.assertNotNull(reloaded);
		Assert.assertEquals("Brand new name", reloaded.getDisplayname());
		Assert.assertEquals("Brand new description", reloaded.getDescription());
		Assert.assertEquals(publicCycle, reloaded.getLifecycle());
	}
	

	private RepositoryEntry createRepositoryEntry(final String type, Identity owner, long i) {
		OLATResourceable resourceable = OresHelper.createOLATResourceableInstance(type, new Long(i));
		OLATResource r =  resourceManager.createOLATResourceInstance(resourceable);
		dbInstance.saveObject(r);
		
		// now make a repository entry for this course
		final RepositoryEntry re = repositoryService.create(owner, "Lernen mit OLAT " + i,
				"JunitTest_RepositoryEntry_" + i, "yo man description bla bla + i", r);	
		re.setAccess(RepositoryEntry.ACC_OWNERS_AUTHORS);
		return re;
	}
}
