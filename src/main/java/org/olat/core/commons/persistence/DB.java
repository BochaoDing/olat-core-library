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

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.stat.Statistics;
import org.hibernate.type.Type;
import org.olat.core.id.Persistable;

public interface DB {

	/** temp debug only **/
	public void forceSetDebugLogLevel(boolean enabled);
	
	/**
	 * Add an ITransactionListener to this DB instance.
	 * <p>
	 * The ITransactionListener will be informed about commit and rollbacks.
	 * <p>
	 * Adding the same listener twice has no effect.
	 * <p>
	 * @param listener the listener to be added
	 */
	public void addTransactionListener(ITransactionListener listener);

	/**
	 * Removes an ITransactionListener from this DB instance.
	 * <p>
	 * If the ITransactionListener is currently not registered, this call
	 * has no effect.
	 * @param listener
	 */
	public void removeTransactionListener(ITransactionListener listener);
	
	/**
	 * Close the database session.
	 */
	public void closeSession();
	
	/**
	 * Close the database session, clean threadlocal but only if necessary
	 */
	public void cleanUpSession();

	/**
	 * Create a DBQuery
	 * 
	 * @param query
	 * @return DBQuery
	 */
	public DBQuery createQuery(String query);

	/**
	 * Delete an object.
	 * 
	 * @param object
	 */
	public void deleteObject(Object object);

	/**
	 * Find objects based on query
	 * 
	 * @param query
	 * @param value
	 * @param type
	 * @return List of results.
	 */
	public List find(String query, Object value, Type type);

	/**
	 * Find objects based on query
	 * 
	 * @param query
	 * @param values
	 * @param types
	 * @return List of results.
	 */
	public List find(String query, Object[] values, Type[] types);

	/**
	 * Find an object.
	 * 
	 * @param theClass
	 * @param key
	 * @return Object, if any found. or null otherwise
	 */
	public Object findObject(Class theClass, Long key);

	/**
	 * Find objects based on query
	 * 
	 * @param query
	 * @return List of results.
	 */
	public List find(String query);

	/**
	 * Load an object.
	 * 
	 * @param theClass
	 * @param key
	 * @return Object.
	 */
	public Object loadObject(Class<?> theClass, Long key);

	/**
	 * Save an object.
	 * 
	 * @param object
	 */
	public void saveObject(Object object);

	/**
	 * Update an object.
	 * 
	 * @param object
	 */
	public void updateObject(Object object);

	/**
	 * Deletion query.
	 * 
	 * @param query
	 * @param value
	 * @param type
	 * @return nr of values deleted
	 */
	public abstract int delete(String query, Object value, Type type);

	/**
	 * Deletion query.
	 * 
	 * @param query
	 * @param values
	 * @param types
	 * @return nr of deleted rows
	 */
	public int delete(String query, Object[] values, Type[] types);

	/**
	 * see DB.loadObject(Persistable persistable, boolean forceReloadFromDB)
	 * 
	 * @param persistable
	 * @return the loaded object, never null
	 */
	public Persistable loadObject(Persistable persistable);

	/**
	 * loads an object if needed. this makes sense if you have an object which had
	 * been generated in a previous hibernate session AND you need to access a Set
	 * or a attribute which was defined as a proxy.
	 * 
	 * @param persistable the object which needs to be reloaded
	 * @param forceReloadFromDB if true, force a reload from the db (e.g. to catch
	 *          up to an object commited by another thread which is still in this
	 *          thread's session cache
	 * @return the loaded Object, never null
	 */
	public Persistable loadObject(Persistable persistable, boolean forceReloadFromDB);

	/**
	 * Checks if the transaction needs to be committed and does so if this is the case,
	 * plus closes the connection in any case guaranteed.
	 * <p>
	 * Use this rather than commit() directly wherever possible!
	 */
	public void commitAndCloseSession();
	
	/**
	 * Call this to commit current changes.
	 */
	public void commit();
  
	/**
	 * Calls rollback and closes the connection guaranteed.
	 * <p>
	 * Note that this method checks whether the connection and the transaction are open
	 * and if they're not, then this method doesn't do anything.
	 */
	public void rollbackAndCloseSession();
	
	/**
	 * Call this to rollback current changes.
	 */
	public void rollback();
  
	/**
	 * Statistics must be enabled first, when you want to use it. 
	 * @return Return Hibernates statistics object.
	 */
	public Statistics getStatistics();
	
	public String getDbVendor();

	/**
	 * Call this to intermediate commit current changes.
	 * Use this method in startup process and bulk changes.
	 */
	public void intermediateCommit();

	/**
	 * @return True if any errors occured in the previous DB call.
	 */
	public boolean isError();
	
	
	//
	// fxdiff
	// Extensions used for native SQL queries
	//
	/**
	 * Create a named hibernate query for the given name. Optionally the database
	 * vendor is prepended to load database specific queries if available. Use
	 * this only when absolutely necessary.
	 * 
	 * @param queryName The query name
	 * @param vendorSpecific true: prepend the database vendor name to the query
	 *          name, e.g. mysql_queryName; false: use queryName as is
	 * @return the query or NULL if no such named query exists
	 */
	public DBQuery createNamedQuery(final String queryName, boolean vendorSpecific);
	
	/**
	 * 
	 * Return the current entity manager to work with JPA2
	 * 
	 * @return
	 */
	public EntityManager getCurrentEntityManager();

}