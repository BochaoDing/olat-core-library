package org.olat.group.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.properties.BusinessGroupPropertyManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupDAOTest extends OlatTestCase {
	
	private OLog log = Tracing.createLoggerFor(BusinessGroupDAOTest.class);
	
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;

	
	@After
	public void tearDown() throws Exception {
		try {
			DBFactory.getInstance().commitAndCloseSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
			throw e;
		}
	}
	
	@Test
	public void should_service_present() {
		Assert.assertNotNull(businessGroupDao);
	}
	
	@Test
	public void createBusinessGroup() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		dbInstance.commit();

		Assert.assertNotNull(group);
		Assert.assertNull(group.getMinParticipants());
		Assert.assertNull(group.getMaxParticipants());
		Assert.assertNotNull(group.getLastUsage());
		Assert.assertNotNull(group.getCreationDate());
		Assert.assertNotNull(group.getLastModified());
		Assert.assertNotNull(group.getOwnerGroup());
		Assert.assertNotNull(group.getPartipiciantGroup());
		Assert.assertNotNull(group.getWaitingGroup());
		Assert.assertEquals("gdao", group.getName());
		Assert.assertEquals("gdao-desc", group.getDescription());
		Assert.assertFalse(group.getWaitingListEnabled());
		Assert.assertFalse(group.getAutoCloseRanksEnabled());
	}
	
	@Test
	public void loadBusinessGroupStandard() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdbo", "gdbo-desc", -1, -1, false, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		
		Assert.assertNotNull(reloadedGroup);
		Assert.assertNull(reloadedGroup.getMinParticipants());
		Assert.assertNull(reloadedGroup.getMaxParticipants());
		Assert.assertNotNull(reloadedGroup.getLastUsage());
		Assert.assertNotNull(reloadedGroup.getCreationDate());
		Assert.assertNotNull(reloadedGroup.getLastModified());
		Assert.assertNotNull(reloadedGroup.getOwnerGroup());
		Assert.assertNotNull(reloadedGroup.getPartipiciantGroup());
		Assert.assertNotNull(reloadedGroup.getWaitingGroup());
		Assert.assertEquals("gdbo", reloadedGroup.getName());
		Assert.assertEquals("gdbo-desc", reloadedGroup.getDescription());
		Assert.assertFalse(reloadedGroup.getWaitingListEnabled());
		Assert.assertFalse(reloadedGroup.getAutoCloseRanksEnabled());
	}
	
	@Test
	public void loadBusinessGroup() {
		//create business group
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdco", "gdco-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();
		
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		//check the saved values
		Assert.assertNotNull(reloadedGroup);
		Assert.assertNotNull(reloadedGroup.getMinParticipants());
		Assert.assertNotNull(reloadedGroup.getMaxParticipants());
		Assert.assertEquals(0, reloadedGroup.getMinParticipants().intValue());
		Assert.assertEquals(10, reloadedGroup.getMaxParticipants().intValue());
		Assert.assertNotNull(reloadedGroup.getLastUsage());
		Assert.assertNotNull(reloadedGroup.getCreationDate());
		Assert.assertNotNull(reloadedGroup.getLastModified());
		Assert.assertNotNull(reloadedGroup.getOwnerGroup());
		Assert.assertNotNull(reloadedGroup.getPartipiciantGroup());
		Assert.assertNotNull(reloadedGroup.getWaitingGroup());
		Assert.assertEquals("gdco", reloadedGroup.getName());
		Assert.assertEquals("gdco-desc", reloadedGroup.getDescription());
		Assert.assertTrue(reloadedGroup.getWaitingListEnabled());
		Assert.assertTrue(reloadedGroup.getAutoCloseRanksEnabled());
	}
	
	@Test
	public void loadBusinessGroupWithOwner() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-1-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gddo", "gddo-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();
		
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		//check if the owner is in the owner security group
		Assert.assertNotNull(reloadedGroup);
		Assert.assertNotNull(reloadedGroup.getOwnerGroup());
		boolean isOwner = securityManager.isIdentityInSecurityGroup(owner, reloadedGroup.getOwnerGroup());
		Assert.assertTrue(isOwner);
	}
	
	@Test
	public void loadBusinessGroupsByIds() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-2-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(owner, "gdeo", "gdeo-desc", 0, 10, true, true, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(owner, "gdfo", "gdfo-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//check if the method is robust against empty list fo keys
		List<BusinessGroup> groups1 = businessGroupDao.load(Collections.<Long>emptyList());
		Assert.assertNotNull(groups1);
		Assert.assertEquals(0, groups1.size());
		
		//check load 1 group
		List<BusinessGroup> groups2 = businessGroupDao.load(Collections.singletonList(group1.getKey()));
		Assert.assertNotNull(groups2);
		Assert.assertEquals(1, groups2.size());
		Assert.assertEquals(group1, groups2.get(0));
		
		//check load 2 groups
		List<Long> groupKeys = new ArrayList<Long>(2);
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		List<BusinessGroup> groups3 = businessGroupDao.load(groupKeys);
		Assert.assertNotNull(groups3);
		Assert.assertEquals(2, groups3.size());
		Assert.assertTrue(groups3.contains(group1));
		Assert.assertTrue(groups3.contains(group2));
	}
	
	@Test
	public void loadAllBusinessGroups() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-3-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(owner, "gdgo", "gdgo-desc", 0, 10, true, true, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(owner, "gdho", "gdho-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//load all business groups
		List<BusinessGroup> allGroups = businessGroupDao.loadAll();
		Assert.assertNotNull(allGroups);
		Assert.assertTrue(allGroups.size() >= 2);
		Assert.assertTrue(allGroups.contains(group1));
		Assert.assertTrue(allGroups.contains(group2));
	}

	
	@Test
	public void mergeBusinessGroup() {
		//create a business group
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-3-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gdho", "gdho-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//delete a business group
		group.setAutoCloseRanksEnabled(false);
		group.setName("gdho-2");

		//merge business group
		BusinessGroup mergedGroup = businessGroupDao.merge(group);
		Assert.assertNotNull(mergedGroup);
		Assert.assertEquals(group, mergedGroup);
		Assert.assertEquals("gdho-2", mergedGroup.getName());
		Assert.assertEquals(Boolean.FALSE, mergedGroup.getAutoCloseRanksEnabled());
		
		dbInstance.commitAndCloseSession();
		
		//reload the merged group and check values
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		Assert.assertEquals(group, reloadedGroup);
		Assert.assertEquals("gdho-2", reloadedGroup.getName());
		Assert.assertEquals(Boolean.FALSE, reloadedGroup.getAutoCloseRanksEnabled());
	}
	
	@Test
	public void updateBusinessGroup() {
		//create a business group
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-4-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gdio", "gdio-desc", 1, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//delete a business group
		group.setWaitingListEnabled(false);
		group.setDescription("gdio-2-desc");

		//update business group (semantic of Hibernate before JPA)
		BusinessGroup updatedGroup = businessGroupDao.update(group);
		Assert.assertNotNull(updatedGroup);
		Assert.assertEquals(group, updatedGroup);
		Assert.assertEquals("gdio-2-desc", updatedGroup.getDescription());
		Assert.assertEquals(Boolean.FALSE, updatedGroup.getWaitingListEnabled());
		Assert.assertTrue(updatedGroup == group);
		
		dbInstance.commitAndCloseSession();
		
		//reload the merged group and check values
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		Assert.assertEquals(group, reloadedGroup);
		Assert.assertEquals("gdio-2-desc", reloadedGroup.getDescription());
		Assert.assertEquals(Boolean.FALSE, reloadedGroup.getWaitingListEnabled());
	}
	
	@Test
	public void findBusinessGroupBySecurityGroup() {
		//create 2 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdjo", "gdjo-desc", -1, -1, false, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdko", "gdko-desc", -1, -1, false, false, false, false, false);
		dbInstance.commitAndCloseSession();

		//check find by owner group
		BusinessGroup byOwnerGroup = businessGroupDao.findBusinessGroup(group1.getOwnerGroup());
		Assert.assertNotNull(byOwnerGroup);
		Assert.assertEquals(group1, byOwnerGroup);
		Assert.assertNotSame(group2, byOwnerGroup);

		//check find by participant group
		BusinessGroup byParticipantGroup = businessGroupDao.findBusinessGroup(group1.getPartipiciantGroup());
		Assert.assertNotNull(byParticipantGroup);
		Assert.assertEquals(group1, byParticipantGroup);
		
		//check find by waiting group
		BusinessGroup byWaitingGroup = businessGroupDao.findBusinessGroup(group2.getWaitingGroup());
		Assert.assertNotNull(byWaitingGroup);
		Assert.assertEquals(group2, byWaitingGroup);
		Assert.assertNotSame(group1, byWaitingGroup);
		
	}
	
	@Test
	public void findBusinessGroupsWithWaitingListAttendedBy() {
		//3 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("bdao-5-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("bdao-6-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("bdao-7-" + UUID.randomUUID().toString());

		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdlo", "gdlo-desc", 0, 5, true, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdmo", "gdmo-desc", 0, 5, true, false, false, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdno", "gdno-desc", 0, 5, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//id1 -> group 1 and 2
		securityManager.addIdentityToSecurityGroup(id1, group1.getWaitingGroup());
		securityManager.addIdentityToSecurityGroup(id1, group2.getWaitingGroup());
		//id2 -> group 1 and 3
		securityManager.addIdentityToSecurityGroup(id2, group1.getWaitingGroup());
		securityManager.addIdentityToSecurityGroup(id2, group3.getWaitingGroup());

		//check:
		//id1: group 1 and 2
		List<BusinessGroup> groupOfId1 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id1,  null);
		Assert.assertNotNull(groupOfId1);
		Assert.assertTrue(groupOfId1.contains(group1));
		Assert.assertTrue(groupOfId1.contains(group2));
		//id2 -> group 1 and 3
		List<BusinessGroup> groupOfId2 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id2,  null);
		Assert.assertNotNull(groupOfId2);
		Assert.assertTrue(groupOfId2.contains(group1));
		Assert.assertTrue(groupOfId2.contains(group3));

		List<BusinessGroup> groupOfId3 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id3,  null);
		Assert.assertNotNull(groupOfId3);
		Assert.assertTrue(groupOfId3.isEmpty());
	}
	
	@Test
	public void testVisibilityOfSecurityGroups() {
		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdro", "gdro-desc", 0, 5, true, false, true, true, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdso", "gdso-desc", 0, 5, true, false, false, true, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdto", "gdto-desc", 0, 5, true, false, false, false, true);
		dbInstance.commitAndCloseSession();
		
		//check the value
		BusinessGroupPropertyManager bgpm1 = new BusinessGroupPropertyManager(group1);
		Assert.assertTrue(bgpm1.showOwners());
		Assert.assertTrue(bgpm1.showPartips());
		Assert.assertFalse(bgpm1.showWaitingList());
		
		BusinessGroupPropertyManager bgpm2 = new BusinessGroupPropertyManager(group2);
		Assert.assertFalse(bgpm2.showOwners());
		Assert.assertTrue(bgpm2.showPartips());
		Assert.assertFalse(bgpm2.showWaitingList());
		
		BusinessGroupPropertyManager bgpm3 = new BusinessGroupPropertyManager(group3);
		Assert.assertFalse(bgpm3.showOwners());
		Assert.assertFalse(bgpm3.showPartips());
		Assert.assertTrue(bgpm3.showWaitingList());
	}
	
	@Test
	public void testContacts() {
		//5 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-3-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-4-" + UUID.randomUUID().toString());
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-5-" + UUID.randomUUID().toString());
		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdlo", "gdlo-desc", 0, 5, true, false, true, true, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdmo", "gdmo-desc", 0, 5, true, false, false, true, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdno", "gdno-desc", 0, 5, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//id1 -> group 1 and 2
		securityManager.addIdentityToSecurityGroup(id1, group1.getOwnerGroup());
		securityManager.addIdentityToSecurityGroup(id1, group2.getPartipiciantGroup());
		//id2 -> group 1 and 2
		securityManager.addIdentityToSecurityGroup(id2, group1.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id2, group2.getOwnerGroup());
		//id3 -> group 1 and 3
		securityManager.addIdentityToSecurityGroup(id3, group1.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id3, group3.getPartipiciantGroup());
		//id4 -> group 2
		securityManager.addIdentityToSecurityGroup(id4, group2.getOwnerGroup());
		//id5 -> group 3
		securityManager.addIdentityToSecurityGroup(id5, group3.getOwnerGroup());
		dbInstance.commitAndCloseSession();	
		
		//check identity1 : contact to id2 and id3
		int numOfContact1 = businessGroupDao.countContacts(id1);
		Assert.assertEquals(2, numOfContact1);
		List<Identity> contactList1 = businessGroupDao.findContacts(id1, 0, -1);
		Assert.assertEquals(2, contactList1.size());
		Assert.assertTrue(contactList1.contains(id2));
		Assert.assertTrue(contactList1.contains(id3));
		
		//check identity2 : contact to id1 and id3
		int numOfContact2 = businessGroupDao.countContacts(id2);
		Assert.assertEquals(2, numOfContact2);
		List<Identity> contactList2 = businessGroupDao.findContacts(id2, 0, -1);
		Assert.assertEquals(2, contactList2.size());
		Assert.assertTrue(contactList2.contains(id1));
		Assert.assertTrue(contactList2.contains(id3));
		
		//check identity3 : contact to id1 and id2
		int numOfContact3 = businessGroupDao.countContacts(id3);
		Assert.assertEquals(2, numOfContact3);
		List<Identity> contactList3 = businessGroupDao.findContacts(id3, 0, -1);
		Assert.assertEquals(2, contactList3.size());
		Assert.assertTrue(contactList3.contains(id1));
		Assert.assertTrue(contactList3.contains(id2));
		
		//check identity4 : contact to id1
		int numOfContact4 = businessGroupDao.countContacts(id4);
		Assert.assertEquals(1, numOfContact4);//!!! ne marche pas
		List<Identity> contactList4 = businessGroupDao.findContacts(id4, 0, -1);
		Assert.assertEquals(1, contactList4.size());
		Assert.assertTrue(contactList4.contains(id1));
		
		//check identity5 : contact to id2
		int numOfContact5 = businessGroupDao.countContacts(id5);
		Assert.assertEquals(0, numOfContact5);
		List<Identity> contactList5 = businessGroupDao.findContacts(id5, 0, -1);
		Assert.assertEquals(0, contactList5.size());
	}
	
	@Test
	public void testContactsWithMoreExclusions() {
		//5 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-3-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-4-" + UUID.randomUUID().toString());
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-5-" + UUID.randomUUID().toString());
		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdoo", "gdoo-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdpo", "gdpo-desc", 0, 5, true, false, false, true, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdqo", "gdqo-desc", 0, 5, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//id1 -> group 1 and 2
		securityManager.addIdentityToSecurityGroup(id1, group1.getOwnerGroup());				//visible
		securityManager.addIdentityToSecurityGroup(id1, group2.getPartipiciantGroup());	//visible
		//id2 -> group 1 and 2
		securityManager.addIdentityToSecurityGroup(id2, group1.getPartipiciantGroup()); //not
		securityManager.addIdentityToSecurityGroup(id2, group2.getOwnerGroup());        //not
		//id3 -> group 1 and 3
		securityManager.addIdentityToSecurityGroup(id3, group2.getOwnerGroup());        //not
		securityManager.addIdentityToSecurityGroup(id3, group3.getPartipiciantGroup()); //not
		//id4 -> group 2
		securityManager.addIdentityToSecurityGroup(id4, group2.getOwnerGroup());        //not
		securityManager.addIdentityToSecurityGroup(id4, group3.getOwnerGroup());        //not
		//id5 -> group 3
		securityManager.addIdentityToSecurityGroup(id5, group3.getOwnerGroup());        //not
		dbInstance.commitAndCloseSession();	
		
		//check identity1 : contact to id2 and id3
		int numOfContact1 = businessGroupDao.countContacts(id1);
		Assert.assertEquals(0, numOfContact1);
		List<Identity> contactList1 = businessGroupDao.findContacts(id1, 0, -1);
		Assert.assertEquals(0, contactList1.size());
		
		//check identity2 : contact to id1 and id3
		int numOfContact2 = businessGroupDao.countContacts(id2);
		Assert.assertEquals(1, numOfContact2);
		List<Identity> contactList2 = businessGroupDao.findContacts(id2, 0, -1);
		Assert.assertEquals(1, contactList2.size());
		Assert.assertTrue(contactList2.contains(id1));
		
		//check identity3 : contact to id1 and id2
		int numOfContact3 = businessGroupDao.countContacts(id3);
		Assert.assertEquals(1, numOfContact3);
		List<Identity> contactList3 = businessGroupDao.findContacts(id3, 0, -1);
		Assert.assertEquals(1, contactList3.size());
		Assert.assertTrue(contactList3.contains(id1));
		
		//check identity4 : contact to id1
		int numOfContact4 = businessGroupDao.countContacts(id4);
		Assert.assertEquals(1, numOfContact4);//!!! ne marche pas
		List<Identity> contactList4 = businessGroupDao.findContacts(id4, 0, -1);
		Assert.assertEquals(1, contactList4.size());
		Assert.assertTrue(contactList4.contains(id1));
		
		//check identity5 : contact to id2
		int numOfContact5 = businessGroupDao.countContacts(id5);
		Assert.assertEquals(0, numOfContact5);
		List<Identity> contactList5 = businessGroupDao.findContacts(id5, 0, -1);
		Assert.assertEquals(0, contactList5.size());
	}

	@Test
	public void findBusinessGroups() {
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gduo", "gduo-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdvo", "gdvo-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams(); 
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, false, false, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertTrue(groups.size() >= 2);
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));

		dbInstance.commit();

		List<BusinessGroup> groupLimit = businessGroupDao.findBusinessGroups(params, null, false, false, null, 0, 1);
		Assert.assertNotNull(groupLimit);
		Assert.assertEquals(1, groupLimit.size());
	}
	
	@Test
	public void findBusinessGroupsByExactName() {
		String exactName = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, exactName, "gdwo-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, exactName + "x", "gdxo-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "y" +exactName, "gdyo-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExactName(exactName);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, false, false, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertFalse(groups.contains(group2));
		Assert.assertFalse(groups.contains(group3));
	}
	
	@Test
	public void findBusinessGroupsByName() {
		String name = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, name.toUpperCase(), "fingbg-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, name + "xxx", "fingbg-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "yyy" + name.toUpperCase(), "fingbg-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setName(name);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, false, false, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertFalse(groups.contains(group3));
	}
	
	@Test
	public void findBusinessGroupsByNameFuzzy() {
		String name = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, name.toUpperCase(), "fingbg-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, name + "xxx", "fingbg-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "yyy" + name.toUpperCase(), "fingbg-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setName("*" + name + "*");
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, false, false, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));
	}
	
	@Test
	public void findBusinessGroupsByDescription() {
		String name = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", name.toUpperCase() + "-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "desc-" + name, 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "fingbg-3", "desc-" + name + "-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setDescription(name);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, false, false, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertFalse(groups.contains(group2));
		Assert.assertFalse(groups.contains(group3));
	}
	
	@Test
	public void findBusinessGroupsByDescriptionFuzzy() {
		String name = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", name + "-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "desc-" + name.toUpperCase(), 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "fingbg-3", "desc-" + name + "-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setDescription("*" + name + "*");
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, false, false, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));
	}
	
	@Test
	public void findBusinessGroupsByNameOrDesc() {
		String name = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", name.toUpperCase() + "-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "fingbg-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, name.toUpperCase() + "-xxx", "desc-fingb-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setNameOrDesc(name);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, false, false, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertFalse(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));
	}
	
	@Test
	public void findBusinessGroupsByNameOrDescFuzzy() {
		String name = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", name + "-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "desc-" + name.toUpperCase(), 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "fingbg-3", "desc-" + name + "-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setNameOrDesc("*" + name + "*");
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, false, false, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));
	}
	
	@Test
	public void findBusinessGroupsByOwner() {
		//5 identities
		String name = UUID.randomUUID().toString();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser(name);
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-2-" + name);
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser(name + "-ddao-3");

		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "fingbgown-1", "fingbgown-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "fingbgown-2", "fingbgown-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(id3, "fingbgown-3", "fingbgown-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setOwner(name);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, false, false, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertFalse(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));
	}
	
	@Test
	public void findBusinessGroupsByOwnerFuzzy() {
		String name = UUID.randomUUID().toString();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser(name);
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-2-" + name.toUpperCase());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser(name + "-ddao-3-");
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "fingbg-own-1-1", "fingbg-own-1-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "fingbg-own-1-2", "fingbg-own-1-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(id3, "fingbg-own-1-3", "fingbg-own-1-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setOwner("*" + name + "*");
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, false, false, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));
	}
	

}
