package org.olat.instantMessaging;

import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.manager.RosterDAO;
import org.olat.instantMessaging.model.RosterEntryImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RosterDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RosterDAO rosterDao;
	
	@Test
	public void testCreateRosterEntry() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-1-" + UUID.randomUUID().toString(), System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsAdmin("im-roster-1-" + UUID.randomUUID().toString());
		
		RosterEntryImpl entry = rosterDao.createRosterEntry(chatResource, id, "My full name", "A nick name", false);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(entry);
		Assert.assertNotNull(entry.getKey());
		Assert.assertEquals(id.getKey(), entry.getIdentityKey());
		Assert.assertEquals("My full name", entry.getFullName());
		Assert.assertEquals("A nick name", entry.getNickName());
		Assert.assertFalse(entry.isAnonym());
		Assert.assertEquals(chatResource.getResourceableTypeName(), entry.getResourceTypeName());
		Assert.assertEquals(chatResource.getResourceableId(), entry.getResourceId());
	}

	@Test
	public void testLoadRosterEntries() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-2-" + UUID.randomUUID().toString(), System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsAdmin("im-roster-2-" + UUID.randomUUID().toString());
		rosterDao.createRosterEntry(chatResource, id, "My full name", "A nick name", false);
		dbInstance.commitAndCloseSession();
		
		//load the entries
		List<RosterEntryImpl> entries = rosterDao.getRoster(chatResource, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		
		RosterEntryImpl entry = entries.get(0);
		Assert.assertNotNull(entry);
		Assert.assertNotNull(entry.getKey());
		Assert.assertEquals(id.getKey(), entry.getIdentityKey());
		Assert.assertEquals("My full name", entry.getFullName());
		Assert.assertEquals("A nick name", entry.getNickName());
		Assert.assertFalse(entry.isAnonym());
		Assert.assertEquals(chatResource.getResourceableTypeName(), entry.getResourceTypeName());
		Assert.assertEquals(chatResource.getResourceableId(), entry.getResourceId());
	}
	
	@Test
	public void testUpdateRosterEntry() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-7-" + UUID.randomUUID().toString(), System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsAdmin("im-roster-7-" + UUID.randomUUID().toString());
		rosterDao.createRosterEntry(chatResource, id, "My name", "Nick", false);
		dbInstance.commitAndCloseSession();
		
		//load the entry
		rosterDao.updateRosterEntry(chatResource, id, "My updated full name", "My updated nick name", true);
		dbInstance.commitAndCloseSession();
		
		//load the entry
		List<RosterEntryImpl> entries = rosterDao.getRoster(chatResource, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
			
		RosterEntryImpl entry = entries.get(0);
		
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		
		RosterEntryImpl reloadEntry = entries.get(0);
		Assert.assertNotNull(reloadEntry);
		Assert.assertNotNull(reloadEntry.getKey());
		Assert.assertEquals(id.getKey(), reloadEntry.getIdentityKey());
		Assert.assertEquals("My updated full name", reloadEntry.getFullName());
		Assert.assertEquals("My updated nick name", reloadEntry.getNickName());
		Assert.assertTrue(entry.isAnonym());
		Assert.assertEquals(chatResource.getResourceableTypeName(), reloadEntry.getResourceTypeName());
		Assert.assertEquals(chatResource.getResourceableId(), reloadEntry.getResourceId());
	}
	
	@Test
	public void testDeleteRosterEntries() {
		//create an entry
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-3-" + UUID.randomUUID().toString(), System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsAdmin("im-roster-3-" + UUID.randomUUID().toString());
		rosterDao.createRosterEntry(chatResource, id, "My full name", "A nick name", false);
		dbInstance.commitAndCloseSession();
		
		//check the presence of the entry
		List<RosterEntryImpl> entries = rosterDao.getRoster(chatResource, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		dbInstance.commitAndCloseSession();
		
		//delete the entry
		rosterDao.deleteEntry(id, chatResource);
		dbInstance.commitAndCloseSession();
		
		//check the absence of the entry
		List<RosterEntryImpl> reloadedEntries = rosterDao.getRoster(chatResource, 0, -1);
		Assert.assertNotNull(reloadedEntries);
		Assert.assertTrue(reloadedEntries.isEmpty());
	}
	
	@Test
	public void testClearRosterEntries() {
		//create an entry
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-4-" + UUID.randomUUID().toString(), System.currentTimeMillis());
		for(int i=0; i<10; i++) {
			Identity id = JunitTestHelper.createAndPersistIdentityAsAdmin("im-roster-4-" + UUID.randomUUID().toString());
			rosterDao.createRosterEntry(chatResource, id, "My full name", "A nick name", false);
		}
		dbInstance.commitAndCloseSession();
		
		//check the presence of the entries
		List<RosterEntryImpl> entries = rosterDao.getRoster(chatResource, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertEquals(10, entries.size());
		dbInstance.commitAndCloseSession();
		
		//delete the entry
		rosterDao.clear();
		dbInstance.commitAndCloseSession();
		
		//check the absence of the entry
		List<RosterEntryImpl> reloadedEntries = rosterDao.getRoster(chatResource, 0, -1);
		Assert.assertNotNull(reloadedEntries);
		Assert.assertTrue(reloadedEntries.isEmpty());
	}
}
