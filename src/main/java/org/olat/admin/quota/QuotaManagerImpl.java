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
*/

package org.olat.admin.quota;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.Constants;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * <h3>Description:</h3>
 * Quota manager implementation for the OLAT LMS. This is a singleton that must
 * be specified in the spring configuration and be properly initialized!
 * <p>
 * Initial Date: 23.05.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class QuotaManagerImpl extends QuotaManager {

	private static final String QUOTA_CATEGORY = "quot";
	private OLATResource quotaResource;
	private OLATResourceManager resourceManager;
	private PropertyManager propertyManager;
	private static Map<String,Quota> defaultQuotas;
	
	/**
	 * [used by spring]
	 */
	private QuotaManagerImpl(OLATResourceManager resourceManager, PropertyManager propertyManager) {
		this.resourceManager = resourceManager;
		this.propertyManager = propertyManager;
		INSTANCE = this;
	}

	/**
	 * @see org.olat.core.util.vfs.QuotaManager#createQuota(java.lang.String, java.lang.Long, java.lang.Long)
	 */
	public Quota createQuota(String path, Long quotaKB, Long ulLimitKB) {
		return new QuotaImpl(path, quotaKB, ulLimitKB);
	}

	/**
	 * [called by spring]
	 *
	 */
	public void init() {
		quotaResource = resourceManager.findOrPersistResourceable(OresHelper.lookupType(Quota.class));
		initDefaultQuotas(); // initialize default quotas
		DBFactory.getInstance(false).intermediateCommit();
		Tracing.logInfo("Successfully initialized Quota Manager", QuotaManagerImpl.class);
	}

	private void initDefaultQuotas() {
		defaultQuotas = new HashMap<String,Quota>();
		Quota defaultQuotaUsers = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_USERS);
		defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_USERS, defaultQuotaUsers);
		Quota defaultQuotaPowerusers = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_POWER);
		defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_POWER, defaultQuotaPowerusers);
		Quota defaultQuotaGroups = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS);
		defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS, defaultQuotaGroups);
		Quota defaultQuotaRepository = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_REPO);
		defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_REPO, defaultQuotaRepository);
		Quota defaultQuotaCourseFolder = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_COURSE);
		defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_COURSE, defaultQuotaCourseFolder);
		Quota defaultQuotaNodeFolder = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
		defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_NODES, defaultQuotaNodeFolder);
	}

	private Quota initDefaultQuota(String quotaIdentifier) {
		Quota q = null;
		Property p = propertyManager.findProperty(null, null, quotaResource, QUOTA_CATEGORY, quotaIdentifier);
		if (p != null) q = parseQuota(p);
		if (q != null) return q;
		// initialize default quota
		q = createQuota(quotaIdentifier, new Long(FolderConfig.getDefaultQuotaKB()), new Long(FolderConfig.getLimitULKB()));
		setCustomQuotaKB(q);
		return q;
	}

	/**
	 * Get the identifyers for the default quotas
	 * @return
	 */
	public Set getDefaultQuotaIdentifyers() {
		if (defaultQuotas == null) {
			throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
		}
		return defaultQuotas.keySet();
	}
	
	/**
	 * Get the default quota for the given identifyer or NULL if no such quota
	 * found
	 * 
	 * @param identifyer
	 * @return
	 */
	public Quota getDefaultQuota(String identifyer) {
		if (defaultQuotas == null) {
			throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
		}
		return defaultQuotas.get(identifyer);
	}

	/**
	 * Get the quota (in KB) for this path. Important: Must provide a path with a
	 * valid base.
	 * 
	 * @param path
	 * @return Quota object.
	 */
	public Quota getCustomQuota(String path) {
		if (defaultQuotas == null) {
			throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
		}
		PropertyManager pm = PropertyManager.getInstance();
		Property p = pm.findProperty(null, null, quotaResource, QUOTA_CATEGORY, path);
		if (p == null) return null;
		else return parseQuota(p);
	}

	/**
	 * Sets or updates the quota (in KB) for this path. Important: Must provide a
	 * path with a valid base.
	 * 
	 * @param quota
	 */
	public void setCustomQuotaKB(Quota quota) {
		if (defaultQuotas == null) {
			throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
		}
		PropertyManager pm = PropertyManager.getInstance();
		Property p = pm.findProperty(null, null, quotaResource, QUOTA_CATEGORY, quota.getPath());
		if (p == null) { // create new entry
			p = pm.createPropertyInstance(null, null, quotaResource, QUOTA_CATEGORY, quota.getPath(), null, null, assembleQuota(quota), null);
			pm.saveProperty(p);
		} else {
			p.setStringValue(assembleQuota(quota));
			pm.updateProperty(p);
		}
		// if the quota is a default quota, rebuild the default quota list
		if (quota.getPath().startsWith(QuotaConstants.IDENTIFIER_DEFAULT)) {
			initDefaultQuotas();
		}
	}

	/**
	 * @param quota to be deleted
	 * @return true if quota successfully deleted or no such quota, false if quota
	 *         not deleted because it was a default quota that can not be deleted
	 */
	public boolean deleteCustomQuota(Quota quota) {
		if (defaultQuotas == null) {
			throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
		}
		// do not allow to delete default quotas!
		if (quota.getPath().startsWith(QuotaConstants.IDENTIFIER_DEFAULT)) {
			return false;
		}
		PropertyManager pm = PropertyManager.getInstance();
		Property p = pm.findProperty(null, null, quotaResource, QUOTA_CATEGORY, quota.getPath());
		if (p != null) pm.deleteProperty(p);
		return true;
	}

	/**
	 * Get a list of all objects which have an individual quota.
	 * 
	 * @return list of quotas.
	 */
	public List listCustomQuotasKB() {
		if (defaultQuotas == null) {
			throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
		}
		List results = new ArrayList();
		PropertyManager pm = PropertyManager.getInstance();
		List props = pm.listProperties(null, null, quotaResource, QUOTA_CATEGORY, null);
		if (props == null || props.size() == 0) return results;
		for (Iterator iter = props.iterator(); iter.hasNext();) {
			Property prop = (Property) iter.next();
			results.add(parseQuota(prop));
		}
		return results;
	}

	/**
	 * @param p
	 * @return Parsed quota object.
	 */
	private Quota parseQuota(Property p) {
		String s = p.getStringValue();
		int delim = s.indexOf(':');
		if (delim == -1) return null;
		Quota q = null;
		try {
			Long quotaKB = new Long(s.substring(0, delim));
			Long ulLimitKB = new Long(s.substring(delim + 1));
			q = createQuota(p.getName(), quotaKB, ulLimitKB);
		} catch (NumberFormatException e) {
			// will return null if quota parsing failed
		}
		return q;
	}

	private String assembleQuota(Quota quota) {
		return quota.getQuotaKB() + ":" + quota.getUlLimitKB();
	}

	/**
	 * call to get appropriate quota depending on role. Authors have normally
	 * bigger quotas than normal users.
	 * 
	 * @param identity
	 * @return
	 */
	public Quota getDefaultQuotaDependingOnRole(Identity identity) {
		if (BaseSecurityManager.getInstance().isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR)) { return getDefaultQuotaPowerUsers(); }
		if (BaseSecurityManager.getInstance().isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN)) { return getDefaultQuotaPowerUsers(); }
		return getDefaultQuotaUsers();
	}

	/**
	 * call to get appropriate quota depending on role. Authors have normally
	 * bigger quotas than normal users. The method checks also if the user has a custom quota on the path specified. If yes the custom quota is retuned
	 * 
	 * @param identity
	 * @return custom quota or quota depending on role
	 */
	public Quota getCustomQuotaOrDefaultDependingOnRole(Identity identity, String relPath) {
		Quota quota = getCustomQuota(relPath);
		if (quota == null) { // no custom quota
			if (BaseSecurityManager.getInstance().isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR)) { return createQuota(relPath, getDefaultQuotaPowerUsers().getQuotaKB(), getDefaultQuotaPowerUsers().getUlLimitKB());
			}
			if (BaseSecurityManager.getInstance().isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN)) { return createQuota(relPath, getDefaultQuotaPowerUsers().getQuotaKB(), getDefaultQuotaPowerUsers().getUlLimitKB());
			}
			return createQuota(relPath, getDefaultQuotaUsers().getQuotaKB(), getDefaultQuotaUsers().getUlLimitKB());
		}
		return quota;
	}

	/**
	 * get default quota for normal users. On places where you have users with
	 * different roles use
	 * 
	 * @see getDefaultQuotaDependingOnRole(Identity identity)
	 * @return Quota
	 */
	private Quota getDefaultQuotaUsers() {
		if (defaultQuotas == null) {
			throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
		}
		return defaultQuotas.get(QuotaConstants.IDENTIFIER_DEFAULT_USERS);
	}

	/**
	 * get default quota for power users (authors). On places where you have users
	 * with different roles use
	 * 
	 * @see getDefaultQuotaDependingOnRole(Identity identity)
	 * @return Quota
	 */
	private Quota getDefaultQuotaPowerUsers() {
		if (defaultQuotas == null) {
			throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
		}
		return defaultQuotas.get(QuotaConstants.IDENTIFIER_DEFAULT_POWER);
	}

	/**
	 * Return upload-limit depending on quota-limit and upload-limit values. 
	 * @param quotaKB2          Quota limit in KB, can be Quota.UNLIMITED
	 * @param uploadLimitKB2    Upload limit in KB, can be Quota.UNLIMITED
	 * @param currentContainer2 Upload container (folder)
	 * @return Upload limit on KB 
	 */
	public int getUploadLimitKB(long quotaKB2, long uploadLimitKB2, VFSContainer currentContainer2) {
		if (quotaKB2 == Quota.UNLIMITED) {
			if (uploadLimitKB2 == Quota.UNLIMITED) {
				return (int)Quota.UNLIMITED; // quote & upload un-limited
			} else {
				return (int)uploadLimitKB2;  // only upload limited
			}
		} else {
			// initialize default UL limit
			// prepare quota checks
			long quotaLeftKB = VFSManager.getQuotaLeftKB(currentContainer2);
			if (quotaLeftKB < 0) { 
				quotaLeftKB = 0; 
			}
			if (uploadLimitKB2 == Quota.UNLIMITED) {
				return (int)quotaLeftKB;// quote:limited / upload:unlimited 
			} else {
        // quote:limited / upload:limited 
				if (quotaLeftKB > uploadLimitKB2) {
					return (int)uploadLimitKB2; // upload limit cut the upload
				} else {
					return (int)quotaLeftKB; // quota-left space cut the upload
				}
			} 
		}	
	}
	
	/**
	 * Check if a quota path is valid
	 * @param path
	 * @return
	 */
	public boolean isValidQuotaPath(String path) {
		if (path.startsWith(QuotaConstants.IDENTIFIER_DEFAULT) && !defaultQuotas.containsKey(path)) {
			return false;
		}
		return true;
	}


	/**
	 * @see org.olat.core.util.vfs.QuotaManager#getQuotaEditorInstance(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl, java.lang.String, boolean)
	 */
	public Controller getQuotaEditorInstance(UserRequest ureq, WindowControl wControl, String relPath, boolean modalMode) {
		Controller ctr = new GenericQuotaEditController(ureq, wControl, relPath, modalMode);
		return ctr;
	}

}
