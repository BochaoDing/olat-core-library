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

package org.olat.resource.accesscontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.resource.accesscontrol.manager.ACMethodManager;
import org.olat.resource.accesscontrol.manager.ACOrderManager;
import org.olat.resource.accesscontrol.manager.ACOfferManager;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.resource.accesscontrol.model.OfferAccess;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderLine;
import org.olat.resource.accesscontrol.model.OrderPart;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * test the order manager
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACOrderManagerTest extends OlatTestCase {
	
	private static Identity ident1, ident2, ident3;
	private static Identity ident4, ident5, ident6;
	private static Identity ident7, ident8;
	private static boolean isInitialized = false;
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private ACOfferManager acOfferManager;
	
	@Autowired
	private ACFrontendManager acFrontendManager;
	
	@Autowired
	private ACMethodManager acMethodManager;

	@Autowired
	private OLATResourceManager resourceManager;
	
	@Autowired
	private ACOrderManager acOrderManager;
	
	@Before
	public void setUp() {
		if(!isInitialized) {
			ident1 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident2 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident3 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident4 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident5 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident6 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident7 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident8 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
		}
	}
	
	@After
	public void tearDown() {
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testManagers() {
		assertNotNull(acOfferManager);
		assertNotNull(acFrontendManager);
		assertNotNull(dbInstance);
		assertNotNull(acMethodManager);
		assertNotNull(acOrderManager);
	}
	
	@Test
	public void testSaveOrder() {
		//create an offer to buy
		OLATResource randomOres = createResource();
		Offer offer = acFrontendManager.createOffer(randomOres, "TestSaveOrder");
		acFrontendManager.save(offer);
		
		dbInstance.commitAndCloseSession();
		
		//create and save an order
		Order order = acOrderManager.createOrder(ident1);
		OrderPart part = acOrderManager.addOrderPart(order);
		OrderLine item = acOrderManager.addOrderLine(part, offer);
		
		assertNotNull(order);
		assertNotNull(order.getDelivery());
		assertEquals(ident1, order.getDelivery());
		acOrderManager.save(order);
		
		dbInstance.commitAndCloseSession();
		
		//check what's on DB
		Order retrievedOrder = acOrderManager.loadOrderByKey(order.getKey());
		assertNotNull(retrievedOrder);
		assertNotNull(retrievedOrder.getDelivery());
		assertEquals(ident1, retrievedOrder.getDelivery());
		assertEquals(order, retrievedOrder);
		
		List<OrderPart> parts = retrievedOrder.getParts();
		assertNotNull(parts);
		assertEquals(1, parts.size());
		assertEquals(part, parts.get(0));
		
		OrderPart retrievedPart = parts.get(0);
		assertNotNull(retrievedPart.getOrderLines());
		assertEquals(1, retrievedPart.getOrderLines().size());
		assertEquals(item, retrievedPart.getOrderLines().get(0));
		
		OrderLine retrievedItem = retrievedPart.getOrderLines().get(0);
		assertNotNull(retrievedItem.getOffer());
		assertEquals(offer, retrievedItem.getOffer());
	}
	
	@Test
	public void testSaveOneClickOrders() {
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer1 = acFrontendManager.createOffer(randomOres1, "TestSaveOneClickOrders 1");
		acFrontendManager.save(offer1);
		
		OLATResource randomOres2 = createResource();
		Offer offer2 = acFrontendManager.createOffer(randomOres2, "TestSaveOneClickOrders 2");
		acFrontendManager.save(offer2);
		
		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access1 = acMethodManager.createOfferAccess(offer1, method);
		acMethodManager.save(access1);
		
		OfferAccess access2 = acMethodManager.createOfferAccess(offer2, method);
		acMethodManager.save(access2);

		dbInstance.commitAndCloseSession();
		
		//one clicks
		acOrderManager.saveOneClick(ident1, access1);
		acOrderManager.saveOneClick(ident2, access2);
		acOrderManager.saveOneClick(ident3, access1);
		acOrderManager.saveOneClick(ident3, access2);
		
		dbInstance.commitAndCloseSession();
		
		//retrieves by identity
		List<Order> ordersIdent3 = acOrderManager.findOrdersByDelivery(ident3);
		assertEquals(2, ordersIdent3.size());
		assertEquals(ident3, ordersIdent3.get(0).getDelivery());
		assertEquals(ident3, ordersIdent3.get(1).getDelivery());
		
		//retrieves by resource
		List<Order> ordersResource2 = acOrderManager.findOrdersByResource(randomOres2);
		assertEquals(2, ordersResource2.size());
	}
	
	@Test
	public void testSaveOneClickOrder() {
	//make extensiv test on one order
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer1 = acFrontendManager.createOffer(randomOres1, "TestSaveOneClickOrder 1");
		acFrontendManager.save(offer1);

		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access1 = acMethodManager.createOfferAccess(offer1, method);
		acMethodManager.save(access1);

		dbInstance.commitAndCloseSession();
		
		//one clicks
		Order order = acOrderManager.saveOneClick(ident7, access1);
		
		dbInstance.commitAndCloseSession();
		
		//load order
		Order retrivedOrder = acOrderManager.loadOrderByKey(order.getKey());
		
		assertNotNull(retrivedOrder);
		assertNotNull(retrivedOrder.getCreationDate());
		assertNotNull(retrivedOrder.getDelivery());
		assertNotNull(retrivedOrder.getOrderNr());
		assertNotNull(retrivedOrder.getParts());
		
		assertEquals(ident7, retrivedOrder.getDelivery());
		assertEquals(1, retrivedOrder.getParts().size());
		
		OrderPart orderPart = retrivedOrder.getParts().get(0);
		assertNotNull(orderPart);
		assertEquals(1, orderPart.getOrderLines().size());
		
		OrderLine line = orderPart.getOrderLines().get(0);
		assertNotNull(line);
		assertNotNull(line.getOffer());
		assertEquals(offer1, line.getOffer());
	}
	
	@Test
	public void testLoadBy() {
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer1 = acFrontendManager.createOffer(randomOres1, "TestLoadBy 1");
		acFrontendManager.save(offer1);
		
		OLATResource randomOres2 = createResource();
		Offer offer2 = acFrontendManager.createOffer(randomOres2, "TestLoadBy 2");
		acFrontendManager.save(offer2);
		
		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access1 = acMethodManager.createOfferAccess(offer1, method);
		acMethodManager.save(access1);
		
		OfferAccess access2 = acMethodManager.createOfferAccess(offer2, method);
		acMethodManager.save(access2);

		dbInstance.commitAndCloseSession();
		
		//one clicks
		Order order1 = acOrderManager.saveOneClick(ident4, access1);
		Order order2 = acOrderManager.saveOneClick(ident5, access2);
		Order order3_1 = acOrderManager.saveOneClick(ident6, access1);
		Order order3_2 = acOrderManager.saveOneClick(ident6, access2);
		
		dbInstance.commitAndCloseSession();
		
		//load by delivery: ident 1
		List<Order> retrivedOrder1 = acOrderManager.findOrdersByDelivery(ident4);
		assertNotNull(retrivedOrder1);
		assertEquals(1, retrivedOrder1.size());
		assertEquals(order1, retrivedOrder1.get(0));
		
		//load by delivery: ident 2
		List<Order> retrievedOrder2 = acOrderManager.findOrdersByDelivery(ident5);
		assertNotNull(retrievedOrder2);
		assertEquals(1, retrievedOrder2.size());
		assertEquals(order2, retrievedOrder2.get(0));
		
		//load by delivery: ident 3
		List<Order> retrievedOrder3 = acOrderManager.findOrdersByDelivery(ident6);
		assertNotNull(retrievedOrder3);
		assertEquals(2, retrievedOrder3.size());
		assertTrue(order3_1.equals(retrievedOrder3.get(0)) || order3_1.equals(retrievedOrder3.get(1)));
		assertTrue(order3_2.equals(retrievedOrder3.get(0)) || order3_2.equals(retrievedOrder3.get(1)));

		dbInstance.commitAndCloseSession();
		
		//load by resource: ores 1
		List<Order> retrievedOrderOres1 = acOrderManager.findOrdersByResource(randomOres1);
		assertNotNull(retrievedOrderOres1);
		assertEquals(2, retrievedOrderOres1.size());
		assertTrue(order1.equals(retrievedOrderOres1.get(0)) || order1.equals(retrievedOrderOres1.get(1)));
		assertTrue(order3_1.equals(retrievedOrderOres1.get(0)) || order3_1.equals(retrievedOrderOres1.get(1)));
		
		//load by resource: ores 2
		List<Order> retrievedOrderOres2 = acOrderManager.findOrdersByResource(randomOres2);
		assertNotNull(retrievedOrderOres2);
		assertEquals(2, retrievedOrderOres2.size());
		assertTrue(order2.equals(retrievedOrderOres2.get(0)) || order2.equals(retrievedOrderOres2.get(1)));
		assertTrue(order3_2.equals(retrievedOrderOres2.get(0)) || order3_2.equals(retrievedOrderOres2.get(1)));
		
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testDeleteResource() {
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer1 = acFrontendManager.createOffer(randomOres1, "TestDeleteResource 1");
		acFrontendManager.save(offer1);

		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access = acMethodManager.createOfferAccess(offer1, method);
		acMethodManager.save(access);

		dbInstance.commitAndCloseSession();
		
		//save an order
		Order order1 = acOrderManager.saveOneClick(ident8, access);
		dbInstance.commitAndCloseSession();

		//delete the resource
		randomOres1 = (OLATResource)dbInstance.loadObject(OLATResourceImpl.class, randomOres1.getKey());
		dbInstance.deleteObject(randomOres1);
		
		dbInstance.commitAndCloseSession();
		
		//load order
		Order retrievedOrder1 = acOrderManager.loadOrderByKey(order1.getKey());
		assertNotNull(retrievedOrder1);
	}
	
	private OLATResource createResource() {
		//create a repository entry
		OLATResourceable resourceable = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource r =  resourceManager.createOLATResourceInstance(resourceable);
		dbInstance.saveObject(r);
		return r;
	}
}
