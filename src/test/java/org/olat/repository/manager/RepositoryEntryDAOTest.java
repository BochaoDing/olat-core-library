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
package org.olat.repository.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	
	@Test
	public void loadByKey() {
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "-", "Repository entry DAO Test 1", "", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		
		RepositoryEntry loadedRe = repositoryEntryDao.loadByKey(re.getKey());
		Assert.assertNotNull(loadedRe.getStatistics());
		Assert.assertNotNull(loadedRe.getOlatResource());
	}
	
	@Test
	public void loadByResourceKey() {
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "-", "Repository entry DAO Test 2", "", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		
		RepositoryEntry loadedRe = repositoryEntryDao.loadByResourceKey(re.getOlatResource().getKey());
		Assert.assertNotNull(loadedRe.getStatistics());
		Assert.assertEquals(re.getOlatResource(), loadedRe.getOlatResource());
	}
	
	@Test
	public void loadByResourceKeys() {
		RepositoryEntry re1 = repositoryService.create("Rei Ayanami", "-", "Repository entry DAO Test 3a", "", null);
		RepositoryEntry re2 = repositoryService.create("Rei Ayanami", "-", "Repository entry DAO Test 3b", "", null);
		dbInstance.commitAndCloseSession();
		
		List<Long> resourceKeys = new ArrayList<>(2);
		resourceKeys.add(re1.getOlatResource().getKey());
		resourceKeys.add(re2.getOlatResource().getKey());
		
		//load 2 resources
		List<RepositoryEntry> loadedRes = repositoryEntryDao.loadByResourceKeys(resourceKeys);
		Assert.assertNotNull(loadedRes);
		Assert.assertEquals(2,  loadedRes.size());
		Assert.assertTrue(loadedRes.contains(re1));
		Assert.assertTrue(loadedRes.contains(re2));
		
		//try with empty list
		List<RepositoryEntry> emptyRes = repositoryEntryDao.loadByResourceKeys(Collections.<Long>emptyList());
		Assert.assertNotNull(emptyRes);
		Assert.assertEquals(0,  emptyRes.size());
	}
	
	@Test
	public void searchByIdAndRefs() {
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "-", "Repository entry DAO Test 4", "", null);
		dbInstance.commit();
		String externalId = UUID.randomUUID().toString();
		String externalRef = UUID.randomUUID().toString();
		re = repositoryManager.setDescriptionAndName(re, null, null, null, null, externalId, externalRef, null, null);
		dbInstance.commitAndCloseSession();
		
		//by primary key
		List<RepositoryEntry> primaryKeyList = repositoryEntryDao.searchByIdAndRefs(Long.toString(re.getKey()));
		Assert.assertNotNull(primaryKeyList);
		Assert.assertEquals(1,  primaryKeyList.size());
		Assert.assertEquals(re, primaryKeyList.get(0));
		
		//by soft key
		List<RepositoryEntry> softKeyList = repositoryEntryDao.searchByIdAndRefs(re.getSoftkey());
		Assert.assertNotNull(softKeyList);
		Assert.assertEquals(1, softKeyList.size());
		Assert.assertEquals(re, softKeyList.get(0));
		
		//by resourceable id key
		List<RepositoryEntry> resourceableIdList = repositoryEntryDao.searchByIdAndRefs(Long.toString(re.getResourceableId()));
		Assert.assertNotNull(resourceableIdList);
		Assert.assertEquals(1, resourceableIdList.size());
		Assert.assertEquals(re, resourceableIdList.get(0));
		
		//by resource resourceable id
		Long resResourceableId = re.getOlatResource().getResourceableId();
		List<RepositoryEntry> resResourceableIdList = repositoryEntryDao.searchByIdAndRefs(resResourceableId.toString());
		Assert.assertNotNull(resResourceableIdList);
		Assert.assertEquals(1,  resResourceableIdList.size());
		Assert.assertEquals(re, resResourceableIdList.get(0));
		
		//by external id
		List<RepositoryEntry> externalIdList = repositoryEntryDao.searchByIdAndRefs(externalId);
		Assert.assertNotNull(externalIdList);
		Assert.assertEquals(1,  externalIdList.size());
		Assert.assertEquals(re, externalIdList.get(0));
		
		//by external ref
		List<RepositoryEntry> externalRefList = repositoryEntryDao.searchByIdAndRefs(externalRef);
		Assert.assertNotNull(externalRefList);
		Assert.assertEquals(1, externalRefList.size());
		Assert.assertEquals(re, externalRefList.get(0));
		
	}
	
	@Test
	public void getAllRepositoryEntries() {
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "-", "Repository entry DAO Test 4", "", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		
		List<RepositoryEntry> allRes = repositoryEntryDao.getAllRepositoryEntries(0, 25);
		Assert.assertNotNull(allRes);
		Assert.assertFalse(allRes.isEmpty());
		Assert.assertTrue(allRes.size() < 26);
	}
	
	@Test
	public void loadRepositoryEntryResource() {
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "-", "Repository entry DAO Test 5", "", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getSoftkey());
		Assert.assertNotNull(re.getOlatResource());
		
		OLATResource loadedResource = repositoryEntryDao.loadRepositoryEntryResource(re.getKey());
		Assert.assertNotNull(loadedResource);
		Assert.assertEquals(re.getOlatResource(), loadedResource);
	}
	
	@Test
	public void loadRepositoryEntryResourceBySoftKey() {
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "-", "Repository entry DAO Test 5", "", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getSoftkey());
		Assert.assertNotNull(re.getOlatResource());
		
		OLATResource loadedResource = repositoryEntryDao.loadRepositoryEntryResourceBySoftKey(re.getSoftkey());
		Assert.assertNotNull(loadedResource);
		Assert.assertEquals(re.getOlatResource(), loadedResource);
	}
}