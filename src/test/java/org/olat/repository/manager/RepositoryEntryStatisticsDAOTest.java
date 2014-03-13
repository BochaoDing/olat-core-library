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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.manager.UserCommentsDAO;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryStatisticsDAOTest extends OlatTestCase {
	
	private static final OLog log = Tracing.createLoggerFor(RepositoryEntryStatisticsDAOTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserRatingsDAO userRatingsDao;
	@Autowired
	private UserCommentsDAO userCommentsDao;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryStatisticsDAO reStatisticsDao;
	
	@Test
	public void createRepositoryEntry() {
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "-", "Statistics", "", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getStatistics());
		
		RepositoryEntryStatistics stats = reStatisticsDao.loadStatistics(re);
		Assert.assertNotNull(stats);
		Assert.assertNotNull(stats.getKey());
		Assert.assertNotNull(stats.getCreationDate());
		Assert.assertNotNull(stats.getLastModified());
		Assert.assertEquals(0, stats.getLaunchCounter());
		Assert.assertEquals(0, stats.getDownloadCounter());
		Assert.assertNull(stats.getRating());
	}
	
	@Test
	public void updateRatingStatistics() {
		//create an entry
		Identity id = JunitTestHelper.createAndPersistIdentityAsAuthor("update-mark-");
		RepositoryEntry re = repositoryService.create(id, "-", "Statistics", "", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getStatistics());
		
		//set a rating
		userRatingsDao.updateRating(id, re, null, 5);
		dbInstance.commitAndCloseSession();

		RepositoryEntryStatistics stats = reStatisticsDao.loadStatistics(re);
		Assert.assertNotNull(stats);
		Assert.assertEquals(5d, stats.getRating(), 0.001);
	}
	
	@Test
	public void updateCommentsStatistics() {
		//create an entry
		Identity id = JunitTestHelper.createAndPersistIdentityAsAuthor("update-comment-");
		RepositoryEntry re = repositoryService.create(id, "-", "Statistics", "", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getStatistics());
		
		//set a rating
		userCommentsDao.createComment(id, re, null, "Hello, a new comment");
		dbInstance.commitAndCloseSession();

		RepositoryEntryStatistics stats = reStatisticsDao.loadStatistics(re);
		Assert.assertNotNull(stats);
		Assert.assertEquals(1, stats.getNumOfComments());
	}
	
	@Test
	public void incrementLaunchCounter() {
		RepositoryEntry repositoryEntry = repositoryService.create("Rei Ayanami", "-", "T1_perf1", "T1_perf1", null);
		
		final Long keyRepo = repositoryEntry.getKey();
		final OLATResourceable resourceable = repositoryEntry.getOlatResource();
		Assert.assertNotNull(resourceable);
		
		dbInstance.closeSession();
		assertEquals("Launch counter was not 0", 0, repositoryEntry.getStatistics().getLaunchCounter() );
		final int mainLoop = 10;
		final int loop = 50;  // 10 * 50 = 500
		for (int m = 0; m < mainLoop; m++) {
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < loop; i++) {
				// 1. load RepositoryEntry
				RepositoryEntry repositoryEntryT1 = repositoryManager.lookupRepositoryEntry(keyRepo);
				repositoryService.incrementLaunchCounter(repositoryEntryT1);
				dbInstance.closeSession();
			}
			long endTime = System.currentTimeMillis();
			log.info("testIncrementLaunchCounter time=" + (endTime - startTime) + " for " + loop + " incrementLaunchCounter calls");
		}
		RepositoryEntry repositoryEntry2 = repositoryManager.lookupRepositoryEntry(keyRepo);
		assertEquals("Wrong value of incrementLaunch counter",mainLoop * loop,repositoryEntry2.getStatistics().getLaunchCounter());
		log.info("testIncrementLaunchCounter finished");
	}
	
	/**
	 * Compare async increment-call with sync 'setDscription' call.
	 */
	@Test
	public void incrementLaunchCounter_setDescription() {
		RepositoryEntry repositoryEntry = repositoryService.create("Rei Ayanami", "-", "T1_perf1b", "T1_perf1b", null);
		dbInstance.closeSession();
		
		final Long keyRepo = repositoryEntry.getKey();
		RepositoryEntry repositoryEntryT1 = repositoryManager.lookupRepositoryEntry(keyRepo);
		log.info("RepositoryManagerTest: call incrementLaunchCounter");
		long t1 = System.nanoTime();
		repositoryService.incrementLaunchCounter(repositoryEntryT1);
		long t2 = System.nanoTime();
		log.info("RepositoryManagerTest: call incrementLaunchCounter DONE");
		String displayName = "DisplayName_testIncrementLaunchCounterSetDescription";
		String description = "Description_testIncrementLaunchCounterSetDescription";
		log.info("RepositoryManagerTest: call setDescriptionAndName");
		long t3 = System.nanoTime();
		repositoryManager.setDescriptionAndName(repositoryEntryT1, displayName,description);
		long t4 = System.nanoTime();
		log.info("RepositoryManagerTest: call setDescriptionAndName DONE");
		log.info("RepositoryManagerTest: increments take=" + (t2 - t1) + " setDescription take=" + (t4 -t3) );
		dbInstance.closeSession();
		
		RepositoryEntry repositoryEntryT1Reloaded = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
		assertEquals("Wrong displayName value",displayName,repositoryEntryT1Reloaded.getDisplayname());
		assertEquals("Wrong description value",description,repositoryEntryT1Reloaded.getDescription());
		log.info("testIncrementLaunchCounterSetDescription: FINISHED");
	}
	
	@Test
	public void incrementDownloadCounter() {
		RepositoryEntry repositoryEntry = repositoryService.create("Rei Ayanami", "-", "T1_perf2", "T1_perf2", null);	
		final Long keyRepo = repositoryEntry.getKey();
		final OLATResourceable resourceable = repositoryEntry.getOlatResource();
		assertNotNull(resourceable);
		dbInstance.closeSession();
		assertEquals("Download counter was not 0", 0, repositoryEntry.getStatistics().getDownloadCounter() );
		final int mainLoop = 10;
		final int loop = 50;  // 10 * 50 = 500
		for (int m = 0; m < mainLoop; m++) {
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < loop; i++) {
				// 1. load RepositoryEntry
				RepositoryEntry repositoryEntryT1 = repositoryManager.lookupRepositoryEntry(keyRepo);
				repositoryService.incrementDownloadCounter(repositoryEntryT1);
				dbInstance.closeSession();
			}
			long endTime = System.currentTimeMillis();
			log.info("testIncrementDownloadCounter time=" + (endTime - startTime) + " for " + loop + " incrementDownloadCounter calls");
		}
		RepositoryEntry repositoryEntry2 = repositoryManager.lookupRepositoryEntry(keyRepo);
		assertEquals("Wrong value of incrementLaunch counter",mainLoop * loop,repositoryEntry2.getStatistics().getDownloadCounter());
		log.info("testIncrementDownloadCounter finished");
	}

	
	@Test
	public void concurrentIncrementLaunchCounter() {
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

		final int loop = 100;
		final int numberOfThreads = 3;
		final RepositoryEntry repositoryEntry = repositoryService.create("Rei Ayanami", "-", "T1_concurrent1", "T1_concurrent1", null);	
		final Long keyRepo = repositoryEntry.getKey();
		assertNotNull(repositoryEntry.getOlatResource());
		dbInstance.commitAndCloseSession();
		assertEquals("Launch counter was not 0", 0, repositoryEntry.getStatistics().getLaunchCounter() );
		long startTime = System.currentTimeMillis();

		final CountDownLatch doneSignal = new CountDownLatch(3);
		
		// start thread 1 : incrementLaunchCounter / setAccess
		Thread thread1 = new Thread(){
			public void run() {
				try {
					sleep(10);
					for (int i = 1; i <= loop; i++) {
						// 1. load RepositoryEntry
						RepositoryEntry re = repositoryManager.lookupRepositoryEntry(keyRepo);
						repositoryService.incrementLaunchCounter(re);
						if (i % 20 == 0 ) {
							int ACCESS_VALUE = 4;
							re = repositoryManager.setAccess(re, ACCESS_VALUE, false);
							assertEquals("Wrong access value", ACCESS_VALUE, re.getAccess());
						} else if (i % 10 == 0 ) {
							int ACCESS_VALUE = 1;
							re = repositoryManager.setAccess(re, ACCESS_VALUE, false);
							assertEquals("Wrong access value",ACCESS_VALUE,re.getAccess());
						}
						dbInstance.commitAndCloseSession();
					}
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						dbInstance.closeSession();
					} catch (Exception e) {
						// ignore
					}
					doneSignal.countDown();
				}	
			}
		};
		
		// start thread 2 : incrementLaunchCounter / setDescriptionAndName
		Thread thread2 = new Thread() {
			public void run() {
				try {
					sleep(10);
					for (int i = 1; i <= loop; i++) {
						// 1. load RepositoryEntry
						RepositoryEntry re = repositoryManager.lookupRepositoryEntry(keyRepo);
						repositoryService.incrementLaunchCounter(re);
						if (i % 25 == 0 ) {
							String displayName = "DisplayName" + i;
							String description = "Description" + i;
							re = repositoryManager.setDescriptionAndName(re, displayName,description);
							assertEquals("Wrong displayName value", displayName, re.getDisplayname());
							assertEquals("Wrong description value", description, re.getDescription());
						}
						dbInstance.commitAndCloseSession();
					}
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						dbInstance.closeSession();
					} catch (Exception e) {
						// ignore
					}
					doneSignal.countDown();
				}	
			}
		};
		
		// start thread 3
		Thread thread3 = new Thread() {
			public void run() {
				try {
					sleep(10);
					for (int i = 1; i <= loop; i++) {
						// 1. load RepositoryEntry
						RepositoryEntry re = repositoryManager.lookupRepositoryEntry(keyRepo);
						repositoryService.incrementLaunchCounter(re);
						if (i % 30 == 0 ) {
							re = repositoryManager.setProperties(re, true, false, true, false);	
							assertEquals("Wrong canCopy value", true, re.getCanCopy());
							assertEquals("Wrong getCanReference value",false, re.getCanReference());
							assertEquals("Wrong getCanLaunch value", true, re.getCanLaunch());
							assertEquals("Wrong getCanDownload value", false, re.getCanDownload());
						} else 	if (i % 15 == 0 ) {
							re = repositoryManager.setProperties(re, false, true, false, true);	
							assertEquals("Wrong canCopy value", false, re.getCanCopy());
							assertEquals("Wrong getCanReference value", true, re.getCanReference());
							assertEquals("Wrong getCanLaunch value", false, re.getCanLaunch() );
							assertEquals("Wrong getCanDownload value", true, re.getCanDownload());
						}
						dbInstance.commitAndCloseSession();
					}
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						dbInstance.closeSession();
					} catch (Exception e) {
						// ignore
					}
					doneSignal.countDown();
				}	
			}
		};
		
		//go! go! go!
		thread1.start();
		thread2.start();
		thread3.start();

		try {
			boolean interrupt = doneSignal.await(30, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}

		for(Exception e:exceptionHolder) {
			e.printStackTrace();
		}
		assertEquals("Exceptions", 0, exceptionHolder.size());

		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(keyRepo);
		assertEquals("Wrong value of incrementLaunch counter", loop * numberOfThreads, re.getStatistics().getLaunchCounter());
		assertEquals("DisplayName" + loop, re.getDisplayname());//check if the displayname is correct
		assertEquals("Description" + loop, re.getDescription());
		log.info("testConcurrentIncrementLaunchCounter time=" + (System.currentTimeMillis() - startTime) + " for " + loop + " incrementLaunchCounter calls");
		log.info("testConcurrentIncrementLaunchCounter finished");
	}
	
	/**
	 * Modify a repository-entry from three thread to check if the exception does not pop up.  
	 * Thread 1 : call incrementDownloadCounter after 100ms
	 * Thread 2 : call incrementDownloadCounter after 300ms
	 * Thread 3 : update access-value on repository-entry directly after 200ms
	 * Codepoint-breakpoint at IncrementDownloadCounterBackgroundTask in executeTask before update
	 */
	@Test
	public void concurrentIncrementDownloadCounter() {
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));

		RepositoryEntry repositoryEntry = repositoryService.create("Rei Ayanami", "-", "T1_concurrent3", "T1_concurrent3", null);	
		final Long keyRepo = repositoryEntry.getKey();
		final OLATResourceable resourceable = repositoryEntry.getOlatResource();
		assertNotNull(resourceable);
		final int access = 4;
		dbInstance.closeSession();
		assertEquals("Launch counter was not 0", 0, repositoryEntry.getStatistics().getLaunchCounter() );

		final CountDownLatch doneSignal = new CountDownLatch(3);
		
		// thread 1
		Thread thread1 = new Thread() {
			public void run() {
				try {
					Thread.sleep(100);
					RepositoryEntry repositoryEntryT1 = repositoryManager.lookupRepositoryEntry(keyRepo);
					repositoryService.incrementDownloadCounter(repositoryEntryT1);
					log.info("testConcurrentIncrementLaunchCounterWithCodePoints: Thread1 incremented download-counter");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					dbInstance.commitAndCloseSession();
					doneSignal.countDown();
				}
			}};
		
		// thread 2
		Thread thread2 = new Thread() {
			public void run() {
				try {
					Thread.sleep(300);
					RepositoryEntry repositoryEntryT2 = repositoryManager.lookupRepositoryEntry(keyRepo);
					repositoryService.incrementDownloadCounter(repositoryEntryT2);
					log.info("testConcurrentIncrementLaunchCounterWithCodePoints: Thread2 incremented download-counter");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					dbInstance.commitAndCloseSession();
					doneSignal.countDown();
				}
			}};

		// thread 3
		Thread thread3 = new Thread() {
			public void run() {
				try {
					Thread.sleep(200);
					RepositoryEntry repositoryEntryT3 = repositoryManager.lookupRepositoryEntry(keyRepo);
					repositoryEntryT3 = repositoryManager.setAccess(repositoryEntryT3, access, false);
					dbInstance.closeSession();
					log.info("testConcurrentIncrementLaunchCounterWithCodePoints: Thread3 setAccess DONE");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					dbInstance.commitAndCloseSession();
					doneSignal.countDown();
				}
			}};
			
		thread1.start();
		thread2.start();
		thread3.start();

		try {
			boolean interrupt = doneSignal.await(10, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}

		RepositoryEntry repositoryEntry2 = repositoryManager.lookupRepositoryEntry(keyRepo);
		assertEquals("Wrong value of incrementLaunch counter",2,repositoryEntry2.getStatistics().getDownloadCounter());
		assertEquals("Wrong access value",access,repositoryEntry2.getAccess());
		log.info("testConcurrentIncrementLaunchCounterWithCodePoints finish successful");		
	}
	
}
