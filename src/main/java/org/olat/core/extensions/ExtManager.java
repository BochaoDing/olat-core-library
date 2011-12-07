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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreBeanTypes;
import org.olat.core.CoreSpringFactory;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;


/**
 * Description:<br>
 * Initial Date:  02.08.2005 <br>
 * @author Felix
 * @author guido
 */
public class ExtManager extends LogDelegator {
	
	private static ExtManager instance;
	private long timeOfExtensionStartup;
	private List<Extension> extensions;
	private Object lockObject = new Object();
	
  private Map<Long,Extension> idExtensionlookup;
	
  private Map<String,GenericActionExtension> navKeyGAExtensionlookup;
  
	/**
	 * @return the instance
	 */
	public static ExtManager getInstance() {
		if (instance == null) {
			return instance = (ExtManager) CoreSpringFactory.getBean("extManager");
		}
		return instance;
	}
	
	/**
	 * [used by spring]
	 */
	public ExtManager() {
		// for spring framework and..
		timeOfExtensionStartup = System.currentTimeMillis();
		instance = this;
	}

	/**
	 * @return the number of extensions
	 */
	public int getExtensionCnt() {
		return (getExtensions() == null? 0 : extensions.size());
	}

	/**
	 * @param i
	 * @return the extension at pos i
	 */
	public Extension getExtension(int i) {
		return getExtensions().get(i);
	}

	/**
	 * returns the corresponding extension for a given unique extension id.
	 * if no Extension is found for the specified id, null is returned instead.
	 * 
	 * @param id
	 * @return the corresponding extension or null, if no extension is found for given id
	 */
	public Extension getExtensionByID(long id){
		if(idExtensionlookup.containsKey(id))
			return idExtensionlookup.get(id);
		else return null;
	}
	
	/**
	 * returns the GenericActionExtension that corresponds to the given NavKey. if
	 * no suiting GAE is found, null is returned. 
	 * 
	 * @param navKey
	 * @return the GenericActionExtension or null
	 */
	public GenericActionExtension getActionExtensioByNavigationKey(String navKey) {
		if (navKeyGAExtensionlookup.containsKey(navKey)) return navKeyGAExtensionlookup.get(navKey);
		return null;
	}
	
	/**
	 * [used by spring]
	 * @return list
	 */
	public List<Extension> getExtensions() {
		if (extensions == null) {
			synchronized(lockObject) {
				if (extensions == null) {
					initExtentions();
				}
			}
		}
		return extensions;
	}

	/**
	 * @return the time when the extmanager was initialized
	 */
	public long getTimeOfExtensionStartup() {
		return timeOfExtensionStartup;
	}

	/**
	 * @param extensionPoint
	 * @param anExt
	 * @param addInfo additional info to log
	 */
	public void inform(Class extensionPoint, Extension anExt, String addInfo) {
		//Tracing.logAudit(this.getClass(), info: "+addInfo);		// TODO Auto-generated method stub		
	}
	
	private void initExtentions() {
		logInfo("****** start loading extensions *********");
		Map<Integer, Extension> orderKeys = new HashMap<Integer, Extension>();
		idExtensionlookup = new HashMap<Long, Extension>();
		navKeyGAExtensionlookup = new HashMap<String, GenericActionExtension>();
		
		extensions = new ArrayList<Extension>();
		Map<String, Object> extensionMap = CoreSpringFactory.getBeansOfType(CoreBeanTypes.extension);
		Collection<Object> extensionValues = extensionMap.values();

		int count_disabled = 0;
		int count_duplid = 0;
		int count_duplnavkey = 0;
		
		// first build ordered list
		for (Object object : extensionValues) {
			Extension extension = (Extension) object;
			if (!extension.isEnabled()){
				count_disabled++;
				logWarn("* Disabled Extension got loaded :: " + extension + ".  Check yourself that you don't use it or that extension returns null for getExtensionFor() when disabled, resp. overwrite isEnabled().",null);
			}
			int orderKey = extension.getOrder();
			
			if(orderKey == 0){
				//not configured via spring (order not set)
				logDebug("Extension-Configuration Warning: Order-value was not set for extension=" + extension + ", set order-value to config positionioning of extension...",null);
				if(extension instanceof AbstractExtension){
					((AbstractExtension)extension).setOrder(1000);
				}
			}
			if (orderKeys.containsKey(orderKey)) {
				Extension occupant = orderKeys.get(orderKey);
				logDebug("Extension-Configuration Problem: Dublicate order-value ("+extension.getOrder()+") for extension=" + extension + ", orderKey already occupied by "+occupant,null);
			} else {
				orderKeys.put(orderKey, extension);
			}
		
			Long uid = CodeHelper.getUniqueIDFromString(extension.getUniqueExtensionID());
			if(idExtensionlookup.containsKey(uid)){
					count_duplid++;
					logWarn("Devel-Info :: duplicate unique id generated for extensions :: "+uid+" [ ["+idExtensionlookup.get(uid)+"]  and ["+extension+"] ]",null);
			}else{
				extensions.add(extension);
				idExtensionlookup.put(uid, extension);
				if (extension instanceof GenericActionExtension) {
					GenericActionExtension gAE = (GenericActionExtension) extension;
					if (StringHelper.containsNonWhitespace(gAE.getNavigationKey())) {
						if (!navKeyGAExtensionlookup.containsKey(gAE.getNavigationKey())) {
							navKeyGAExtensionlookup.put(gAE.getNavigationKey(), gAE);
						} else {
							count_duplnavkey++;
							logInfo(
									"Devel-Info :: duplicate navigation-key for extension :: " + gAE.getNavigationKey() + " [ [" + idExtensionlookup.get(uid)
											+ "]  and [" + extension + "] ]", null);
						}
					}
				}
			}
			logDebug("Created unique-id "+uid+" for extension:: "+extension);
		}
		logInfo("Devel-Info :: initExtensions done. :: "+count_disabled+" disabled Extensions, "+count_duplid+" extensions with duplicate ids, "+count_duplnavkey+ " extensions with duplicate navigationKeys");
		Collections.sort(extensions);
	}

}
